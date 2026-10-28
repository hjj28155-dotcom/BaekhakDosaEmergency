'use strict';
const {chromium}=require('playwright');
const fs=require('node:fs');
const path=require('node:path');
const http=require('node:http');
const assert=require('node:assert/strict');
const assets=path.resolve(__dirname,'../../../crew-meeting-android/app/src/main/assets');
const aliases={
  'crew-meeting.html':'captain_home_v114.html','crew-v8-home.css':'captain_v8.css',
  'crew-v8-hero.svg':'captain_v8_hero.svg','crew-v8-cinematic.webp':'captain_v8_cinematic.webp',
  'crew-cumulative-after.css':'cumulative_after_only.css','crew-cumulative-after.js':'cumulative_after_only.js',
  'crew-v4-png-report.js':'v4_png_report.js'
};
(async()=>{
  const server=http.createServer((req,res)=>{
    const name=new URL(req.url,'http://local').pathname.split('/').pop();
    const file=path.join(assets,aliases[name]||name);
    if(!fs.existsSync(file)||!fs.statSync(file).isFile()){res.writeHead(404);res.end();return;}
    res.setHeader('Content-Type',file.endsWith('.css')?'text/css':file.endsWith('.js')?'application/javascript':file.endsWith('.html')?'text/html':file.endsWith('.svg')?'image/svg+xml':file.endsWith('.webp')?'image/webp':'application/json');
    res.end(fs.readFileSync(file));
  });
  await new Promise(r=>server.listen(0,'127.0.0.1',r));
  let browser;
  try{
    browser=await chromium.launch({headless:true,args:['--no-sandbox']});
    fs.mkdirSync('ui-evidence',{recursive:true});
    const results=[];
    for(const [width,height] of [[360,740],[390,844],[412,915]]){
      const context=await browser.newContext({viewport:{width,height}});
      const page=await context.newPage();const errors=[];
      page.on('pageerror',e=>errors.push(e.message));
      await page.route('**/*',route=>route.request().url().startsWith('http://127.0.0.1:')?route.continue():route.abort());
      await page.goto(`http://127.0.0.1:${server.address().port}/BaekhakDosaEmergency/crew-meeting.html`);
      await page.locator('#homeDirectorRoomOpen').waitFor({state:'visible'});
      assert.equal(await page.locator('#homeDirectorRoom').isVisible(),false);
      const entrance=await page.locator('#homeDirectorRoomOpen').boundingBox();
      assert(entrance.y>=0&&entrance.y+entrance.height<=height,'director entrance must be visible on home');
      assert.equal(await page.locator('#installedAppBar [data-app-card="v7"]').count(),0);
      assert(await page.locator('#openV3Rooms').isVisible());
      const shortcuts=await page.locator('#originalChatBar').boundingBox(), diagnosis=await page.locator('#v8ConnectionCheck').boundingBox();
      assert(shortcuts.y+shortcuts.height<=diagnosis.y,'hero shortcuts and diagnosis must not overlap');
      await page.screenshot({path:`ui-evidence/v8-804-home-${width}.png`});
      await page.locator('#homeDirectorRoomOpen').click();
      await page.locator('#homeDirectorRoom').waitFor({state:'visible'});
      assert.equal(await page.locator('#freedomAnalysisPanel').isVisible(),false);
      assert.equal(await page.locator('#homeDirectorRoomOpen').getAttribute('aria-expanded'),'true');
      assert((await page.locator('#directorOriginalState').innerText()).includes('아직 없습니다'));
      assert(await page.locator('#directorRoomBack').isVisible());
      assert(await page.locator('#directorReports').isVisible());
      assert(await page.locator('#directorServer').isVisible());
      assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'no horizontal overflow');
      await page.screenshot({path:`ui-evidence/v8-804-director-${width}.png`});
      await page.locator('#directorOriginalOpen').click();
      await page.locator('#originalChatPanel').waitFor({state:'visible'});
      assert((await page.locator('#originalChatLabel').innerText()).includes('통제실장'));
      await page.locator('#homeMoreBackBtn').click();
      await page.locator('#v8DirectorQuick').click();
      await page.locator('#homeDirectorRoom').waitFor({state:'visible'});
      await page.locator('#directorReports').click();
      await page.locator('#homeWorkspace').waitFor({state:'visible'});
      assert(await page.locator('#reportsPanel').isVisible());
      await page.locator('#homeBackBtn').click();
      await page.locator('#homeDirectorRoomOpen').click();
      await page.locator('#directorRoomBack').click();
      assert(await page.locator('#homeDirectorRoomOpen').isVisible());
      assert.deepEqual(errors,[],'page JavaScript errors');
      results.push({width,height,entrance:true,internalRoom:true,originalAddressSetup:true,reports:true,back:true,jsErrors:errors.length});
      await context.close();
    }
    console.log(JSON.stringify({directorRoom:results},null,2));
  }finally{if(browser)await browser.close();await new Promise(r=>server.close(r));}
})().catch(e=>{console.error(e);process.exitCode=1;});
