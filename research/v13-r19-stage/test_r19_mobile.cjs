'use strict';
const assert=require('node:assert/strict'),path=require('node:path');
const {chromium}=require('playwright');
(async()=>{
 const b=await chromium.launch({headless:true,args:['--no-sandbox','--disable-dev-shm-usage']});
 let count=0;
 try{
  for(const [w,h] of [[393,854],[320,700],[768,880]]){
   const page=await b.newPage({viewport:{width:w,height:h},deviceScaleFactor:1});
   const errs=[];page.on('pageerror',e=>errs.push(e.message));
   await page.goto('file://'+path.resolve(__dirname,'v13-ui-r19.html'),{waitUntil:'domcontentloaded',timeout:60000});
   await page.waitForSelector('#v13R19HomeRun',{timeout:20000});
   assert.ok(await page.locator('#v13R19HomeRun').isVisible(),'home one-tap visible');
   const home=await page.locator('#v13R19HomeRun').boundingBox();
   assert.ok(home.x>=0&&home.x+home.width<=w+1&&home.y>=0&&home.y+home.height<=h+1,'home button inside phone viewport');
   await page.locator('#v13R19HomeRun').click();
   await page.waitForSelector('[data-run-raw-analysis]');
   const status=await page.locator('#status').innerText();
   assert.ok(status.includes('원문'),'missing current round handled clearly');
   await page.evaluate(()=>document.querySelector('#menuItems button[data-choose="9"]').click());
   await page.waitForSelector('#v13R19FreedomOverlay:not([hidden])');
   const r=await page.locator('#v13R19FreedomOverlay').boundingBox();
   assert.ok(Math.abs(r.x)<=1&&Math.abs(r.y)<=1&&Math.abs(r.width-w)<=1&&Math.abs(r.height-h)<=1,'V3 full top viewport');
   assert.equal(await page.locator('#v13R19FreedomFrame').getAttribute('src'),'v12_v3_master/index.html');
   await page.locator('#v13R19FreedomClose').click();
   assert.ok(await page.locator('#v13R19FreedomOverlay').isHidden(),'return to V13');
   await page.evaluate(()=>document.querySelector('#menuItems button[data-choose="6"]').click());
   await page.waitForSelector('#scoreReport');
   const score=await page.locator('#scoreReport').innerText();
   assert.ok(score.includes('30조합')&&score.includes('당첨번호'),'score menu preserves live draw and 30 combinations');
   assert.ok(!(await page.locator('#v13R19HomeRun').isVisible()),'home button hidden outside home');
   assert.deepEqual(errs,[],'no JS exceptions');
   console.log('PHONE_QA_PASS',w+'x'+h,'home, original V3 fullscreen, return, grade, JS=0');
   count++;
   await page.close();
  }
 }finally{await b.close();}
 assert.equal(count,3);
})().catch(e=>{console.error('PHONE_QA_FAILED',e);process.exitCode=1;});
