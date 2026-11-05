// V13 captain-only R25 full HTML browser regression; read-only; no deployment.
const { chromium } = require('playwright');
const path = require('node:path');
const { pathToFileURL } = require('node:url');
const assert = require('node:assert/strict');

const APP = path.resolve(__dirname, '../releases/ui/v13-ui-r25-captain-staging.html');
const expected = ['선장방','신서방(구 도사방)','항해사방','항행의자유방','성적 관리','누적 DB','원문 채팅방','작업 기록','설정'];
const forbidden = ['원문 분석','1~300회차 복구 연구실','항행 자동 A방','항행 자동 B방'];
const checks = [];

async function runViewport(browser,width,height) {
 const context = await browser.newContext({ viewport: {width,height}, isMobile:width<500, deviceScaleFactor:1 });
 // No external network requests. Never connect a real native bridge or remote DB.
 await context.route(/^https?:/,route=>route.abort('blockedbyclient'));
 const page = await context.newPage();
 const record = async (name,fn) => {
   await fn();
   checks.push({viewport:width+'x'+height,name,ok:true});
 };
 try {
   await page.goto(pathToFileURL(APP).href,{waitUntil:'domcontentloaded',timeout:40000});
   await page.waitForFunction(()=>document.querySelector('#stage')?.dataset.mode === 'home',{timeout:15000});
   await record('initial home and hidden menu',async()=>{
     assert.equal(await page.locator('#stage').getAttribute('data-mode'),'home');
     assert.equal(await page.locator('#rail').isVisible(),false);
   });
   await record('central raw hotspot exists',async()=>{
     assert.equal(await page.locator('#v13CentralRawHotspot').count(),1);
   });
   await page.locator('[data-top="menu"]').click();
   await record('menu is overlay above full width background',async()=>{
     assert.equal(await page.locator('#workspace').evaluate(el=>el.classList.contains('menu-visible')),true);
     assert.equal(await page.locator('#rail').isVisible(),true);
     const geometry = await page.evaluate(()=>{
       const w=document.querySelector('#workspace').getBoundingClientRect();
       const s=document.querySelector('#stage').getBoundingClientRect();
       return {width:w.width,stage:s.width};
     });
     assert.ok(Math.abs(geometry.width-geometry.stage)<2,JSON.stringify(geometry));
   });
   await record('exact approved nine menu buttons and safe handlers',async()=>{
     const buttons=await page.locator('#menuItems button[data-choose]').all();
     assert.equal(buttons.length,9);
     const actual=(await page.locator('#menuItems button[data-choose] span:last-child').allTextContents()).map(s=>s.trim());
     assert.deepEqual(actual,expected);
     for(const name of forbidden)assert.equal(actual.includes(name),false);
   });
   await page.locator('#railClose').click();
   await record('close menu returns unchanged scenic home',async()=>{
     assert.equal(await page.locator('#rail').isVisible(),false);
     assert.equal(await page.locator('#stage').getAttribute('data-mode'),'home');
   });
   await page.locator('#v13CentralRawHotspot').click();
   await record('central raw button opens original raw panel',async()=>{
     assert.equal(await page.locator('#stage').getAttribute('data-mode'),'panel');
     assert.ok((await page.locator('#stage').innerText()).includes('원문'));
   });
   const panelClose=page.locator('#panelClose');
   if(await panelClose.count()){
      await panelClose.click();
      await record('panel close returns home',async()=>{
        assert.equal(await page.locator('#stage').getAttribute('data-mode'),'home');
      });
   }
   await page.locator('[data-top="menu"]').click();
   await page.locator('#menuItems [data-choose="7"]').click();
   await record('captain room opens existing panel',async()=>{
      assert.equal(await page.locator('#stage').getAttribute('data-mode'),'panel');
   });
   await page.locator('[data-top="menu"]').click();
   await record('top menu returns to home menu',async()=>{
      assert.equal(await page.locator('#stage').getAttribute('data-mode'),'home');
      assert.equal(await page.locator('#rail').isVisible(),true);
   });
 } finally {await context.close()}
}

(async()=>{
 let browser;
 try{
   browser=await chromium.launch({headless:true,args:['--no-sandbox']});
   for(const [w,h] of [[360,780],[920,1400]])await runViewport(browser,w,h);
   console.log(JSON.stringify({result:'PASS',checks:checks.length,details:checks},null,2));
 } catch(e) {
   console.error(JSON.stringify({result:'FAIL',checks:checks.length,details:checks,error:String(e?.stack||e)},null,2));
   process.exitCode=1;
 } finally {await browser?.close()}
})();
