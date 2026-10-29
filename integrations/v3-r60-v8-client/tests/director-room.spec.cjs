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
      await page.addInitScript(()=>{
        window.__distInstalled=true;window.__distLaunches=0;
        window.CrewInstalledApps={
          getLaunchState:()=>JSON.stringify({v3:'AVAILABLE'}),
          getFreedomDistributionState:()=>JSON.stringify(window.__distInstalled?{status:'INSTALLED',versionName:'4.0.51-TEST',versionCode:40051}:{status:'NOT_INSTALLED'}),
          openFreedomDistribution:()=>{if(!window.__distInstalled)return JSON.stringify({status:'NOT_INSTALLED'});window.__distLaunches++;return JSON.stringify({status:'LAUNCH_REQUESTED'});}
        };
      });
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
      await page.screenshot({path:`ui-evidence/v8-806-home-${width}.png`});
      await page.locator('#homeDirectorRoomOpen').click();
      await page.locator('#homeDirectorRoom').waitFor({state:'visible'});
      assert.equal(await page.locator('#freedomAnalysisPanel').isVisible(),false);
      assert.equal(await page.locator('#homeDirectorRoomOpen').getAttribute('aria-expanded'),'true');
      assert((await page.locator('#directorOriginalState').innerText()).includes('아직 없습니다'));
      assert(await page.locator('#directorRoomBack').isVisible());
      assert(await page.locator('#directorReports').isVisible());
      assert(await page.locator('#directorOriginalAnalysis').isVisible());
      await page.locator('#directorOriginalAnalysis').click();
      await page.locator('#freedomAnalysisPanel').waitFor({state:'visible'});
      assert((await page.locator('#freedomAnalysisPanel > .homePaneTop').innerText()).includes('통제실장'));
      assert(await page.locator('#freedomRunRound').isVisible());
      assert.equal(await page.locator('#freedomDistributionPanel').isVisible(),false);
      await page.screenshot({path:`ui-evidence/v8-806-original-analysis-${width}.png`});
      await page.locator('#freedomAnalysisBack').click();
      await page.locator('#homeDirectorRoom').waitFor({state:'visible'});
      assert.equal(await page.locator('#directorServer').isVisible(),false,'secondary director tools start folded');
      assert.equal(await page.locator('#directorOriginalEdit').isVisible(),false);
      await page.locator('#directorRoomSettings > summary').click();
      assert(await page.locator('#directorServer').isVisible());
      assert(await page.locator('#directorOriginalEdit').isVisible());
      await page.locator('#directorRoomSettings > summary').click();
      assert(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),'no horizontal overflow');
      await page.screenshot({path:`ui-evidence/v8-806-director-${width}.png`});
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
      await page.locator('#openV3Rooms').click();
      assert(await page.locator('#homeV3RoomsPanel').isVisible());
      assert.equal(await page.locator('#homeV3RoomsPanel [data-v3-report]:visible').count(),4);
      assert(await page.locator('#v3RoomsOpenNative').isVisible());
      assert.equal(await page.locator('#v3RoomsOpenNavigator').isVisible(),false);
      await page.locator('#v3RoomDetails > summary').click();
      assert(await page.locator('#v3RoomsOpenNavigator').isVisible());
      await page.locator('#v3RoomDetails > summary').click();
      await page.screenshot({path:`ui-evidence/v8-806-v3-${width}.png`});
      await page.locator('#homeV3RoomsBack').click();
      await page.locator('#openFreedomAnalysis').click();
      await page.locator('#freedomDistributionPanel').waitFor({state:'visible'});
      assert.equal(await page.locator('#freedomAnalysisPanel').isVisible(),false,'public app entry does not open research');
      assert((await page.locator('#distributionState').innerText()).includes('4.0.51-TEST'));
      assert.equal(await page.locator('#distributionOfficialDrive').getAttribute('href'),'https://drive.google.com/file/d/1ULoKMeNFSOWDebMfY4tFvgyiK4EDxaJ-/view?usp=drivesdk');
      assert.equal(await page.locator('#distributionDriveQuick').getAttribute('href'),await page.locator('#distributionOfficialDrive').getAttribute('href'));
      await page.screenshot({path:`ui-evidence/v8-806-distribution-${width}.png`});
      await page.locator('#distributionLaunch').click();
      assert.equal(await page.evaluate(()=>window.__distLaunches),1);
      await page.locator('#distributionBack').click();
      await page.evaluate(()=>{window.__distInstalled=false;});
      await page.locator('#openFreedomAnalysis').click();
      assert((await page.locator('#distributionState').innerText()).includes('설치되어 있지 않습니다'));
      await page.locator('#distributionLaunch').click();
      assert((await page.locator('#distributionState').innerText()).includes('배포판이 없습니다'));
      await page.locator('#distributionBack').click();
      await page.locator('#homeDirectorRoomOpen').click();
      await page.locator('#directorOriginalAnalysis').click();
      assert(await page.locator('#freedomRunRound').isVisible());
      assert.equal(await page.locator('#freedomResetActive').isVisible(),false);
      await page.locator('#v4RoomDetails > summary').click();
      assert(await page.locator('#freedomResetActive').isVisible());
      await page.locator('#v4RoomDetails > summary').click();
      await page.evaluate(()=>window.captainV4Route('home'));
      await page.locator('#homeReportBtn').click();
      assert.equal(await page.locator('#reportKind').isVisible(),false,'legacy V7 controls folded');
      await page.locator('#reportRoomDetails > summary').click();
      assert(await page.locator('#reportKind').isVisible());
      await page.locator('#reportRoomDetails > summary').click();
      await page.locator('#homeBackBtn').click();
      assert.deepEqual(errors,[],'page JavaScript errors');
      results.push({width,height,entrance:true,internalRoom:true,originalAddressSetup:true,reports:true,back:true,jsErrors:errors.length});
      await context.close();
    }
    console.log(JSON.stringify({directorRoom:results},null,2));
  }finally{if(browser)await browser.close();await new Promise(r=>server.close(r));}
})().catch(e=>{console.error(e);process.exitCode=1;});
