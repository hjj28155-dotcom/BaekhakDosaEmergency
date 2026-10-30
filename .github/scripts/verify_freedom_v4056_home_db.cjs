const fs=require('node:fs'),assert=require('node:assert/strict');
const {chromium}=require('playwright');
const root='build405/FREEDOM_V402_BUILD_THIS/app/';
const html=fs.readFileSync(root+'src/freedom/assets/index.html','utf8');
const gradle=fs.readFileSync(root+'build.gradle','utf8');
assert.match(gradle,/versionCode 40056/);
assert.match(gradle,/4\.0\.56-HOME-CUMULATIVE-DB/);
assert.equal((html.match(/id="v4056HomeCumulativeDb"/g)||[]).length,1);
assert.match(html,/<button id="v4056HomeCumulativeDb"[^>]*onclick="openM\('db'\)"/);
assert.match(html,/if\(k==='db'\)return dbPage\(\)/);
assert.match(html,/function dbPage\(\)/);
assert.match(html,/id="approvalMessage"/);
assert.match(html,/function requireSpecialApprovalV117\(/);
assert.match(html,/function v4044ReviewRound\(/);
assert.match(html,/function v4054LossVoice\(/);
const styles=[...html.matchAll(/<style[^>]*>([\s\S]*?)<\/style>/g)].map(m=>m[1]).join('\n');
const main=html.match(/<main class="finalHome simpleHome"[\s\S]*?<\/main>/)?.[0];
assert.ok(main,'Original V4 simpleHome must be present');
const testHtml='<!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><style>'+styles+'\nhtml,body,#wrap{height:100vh!important;width:100vw!important;max-width:none!important;padding:0!important;margin:0!important;overflow:hidden!important}</style></head><body><div id="wrap">'+main+'</div><div id="modal" class="modal" style="display:none"><div class="card"></div></div><script>window.__route=null;function openM(k){window.__route=k}function openSimpleRaw(){}function exitV3(){}</script></body></html>';
(async()=>{
 const browser=await chromium.launch({headless:true});fs.mkdirSync('layout_checks',{recursive:true});
 for(const [width,height] of [[320,568],[347,768],[360,800],[393,851],[412,915],[430,932]]){
  const page=await browser.newPage({viewport:{width,height}});
  await page.setContent(testHtml);
  const info=await page.evaluate(()=>{
   const b=document.getElementById('v4056HomeCumulativeDb'),r=b.getBoundingClientRect();
   const version=document.querySelector('.simpleVersion').getBoundingClientRect();
   const hot=[...document.querySelectorAll('.simpleHot')].map(x=>{const a=x.getBoundingClientRect();return {id:x.id,x:a.x,y:a.y,right:a.right,bottom:a.bottom}});
   return {button:{x:r.x,y:r.y,right:r.right,bottom:r.bottom,w:r.width,h:r.height},version:{x:version.x,y:version.y,right:version.right,bottom:version.bottom},hot,display:getComputedStyle(b).display,visibility:getComputedStyle(b).visibility};
  });
  const b=info.button,v=info.version;
  const intersects=(a,c)=>a.x<c.right&&a.right>c.x&&a.y<c.bottom&&a.bottom>c.y;
  assert.ok(b.x>=0&&b.right<=width+1&&b.y>=0&&b.bottom<=height+1,'button outside viewport '+JSON.stringify({width,height,info}));
  assert.ok(b.h>=44&&b.w>=110,'button too small '+JSON.stringify({width,height,info}));
  assert.ok(!intersects(b,v),'button overlaps version '+JSON.stringify({width,height,info}));
  for(const hot of info.hot)assert.ok(!intersects(b,hot),'button overlaps '+hot.id+' '+JSON.stringify({width,height,info}));
  assert.notEqual(info.display,'none');assert.notEqual(info.visibility,'hidden');
  await page.locator('#v4056HomeCumulativeDb').click();
  assert.equal(await page.evaluate(()=>window.__route),'db','button must open existing cumulative DB route');
  await page.screenshot({path:'layout_checks/v4056_home_db_'+width+'x'+height+'.png'});
  await page.close();
 }
 await browser.close();
 console.log('PASS V4.0.56: home cumulative DB button visible, tappable, no overlap with existing home controls/version at six viewport sizes; original DB/approval/auto20 code retained.');
})().catch(e=>{console.error(e);process.exit(1)});
