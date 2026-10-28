const fs=require('node:fs'),vm=require('node:vm'),assert=require('node:assert/strict');
const {chromium}=require('playwright');
const root='build405/FREEDOM_V402_BUILD_THIS/app/';
const html=fs.readFileSync(root+'src/freedom/assets/index.html','utf8');
const css=fs.readFileSync(root+'src/freedom/assets/v4047_compact.css','utf8');
function range(a,b){const i=html.indexOf(a),j=html.indexOf(b,i+a.length);assert.ok(i>=0&&j>i);return html.slice(i,j)}
const ctx={};vm.createContext(ctx);vm.runInContext(range('function v330GroupsResult(games,result,bonus){','function v330Print(){'),ctx);
const games=Array.from({length:20},(_,i)=>[3,5,9,17,20,41+i%2]);
const content='<div class="analysisCompact v330print"><div class="v4047Select"><strong>5조합 × 4묶음</strong><div class="r5selector">'+[1244,1245,1246,1247,1248].map(n=>'<button class="b">'+n+'회</button>').join('')+'</div></div><div class="v4047Winning"><strong>당첨</strong><div class="r5winning">'+[1,13,18,26,34,38].map(n=>'<span class="r5ball hit">'+n+'</span>').join('')+'</div><span>+25</span></div><div class="v4047Summary v4049VoiceSummary">저장 자동 20조합 · 3개 이상 0/20 <button class="v4049Replay">다시듣기</button></div><div class="r5grid">'+ctx.v330GroupsResult(games,[1,13,18,26,34,38],25)+'</div><div class="v4047Footer"><button class="b">인쇄</button><button class="b">1248회 이동</button><button class="b">당첨번호 조회</button><button class="b" onclick="v4048GoHome()">홈</button></div></div>';
const styles=[...html.matchAll(/<style[^>]*>([\s\S]*?)<\/style>/g)].map(m=>m[1]).join('\n');
const pageHtml='<!doctype html><html><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><style>'+styles+'\n'+css+'</style></head><body><div id="modal" class="modal"><div class="card"><button class="close">닫기</button><h2 id="mt"></h2><div id="mc"></div></div></div><script>function stopScoreAudio(){}function v4048GoHome(){document.getElementById("modal").style.display="none"}'+range('function modal(title,html){','function roundHead(')+'</script></body></html>';
(async()=>{
 const browser=await chromium.launch({headless:true});const page=await browser.newPage();fs.mkdirSync('layout_checks',{recursive:true});
 for(const [width,height] of [[347,768],[360,800],[393,851],[412,915],[720,1000],[320,568]]){
  await page.setViewportSize({width,height});await page.setContent(pageHtml);
  await page.evaluate(c=>{modal('제1244회 · 자동 20조합',c);document.getElementById('modal').classList.add('v4047Compact')},content);
  assert.equal(await page.locator('.r5row').count(),20);assert.equal(await page.locator('.r5balls .r5ball').count(),120);
  const result=await page.evaluate(()=>{
   const home=document.querySelector('#v4051HomeDock button').getBoundingClientRect();
   const balls=[...document.querySelectorAll('.r5balls .r5ball')];
   return {home:{x:home.x,y:home.y,right:home.right,bottom:home.bottom},min:Math.min(...balls.map(e=>e.getBoundingClientRect().width)),overflow:balls.some(e=>{const r=e.getBoundingClientRect(),p=e.closest('.r5group').getBoundingClientRect();return r.x<p.x||r.right>p.right+1||r.bottom>home.top-2}),closeVisible:getComputedStyle(document.querySelector('.close')).display!=='none'};
  });
  assert.ok(result.home.x>width/2&&result.home.bottom<=height&&result.home.y>=height-56,'Home bottom-right '+JSON.stringify({width,height,result}));
  assert.ok(!result.closeVisible,'no top close/Home');assert.ok(!result.overflow,'all 120 balls visible without Home overlap '+JSON.stringify({width,height,result}));
  assert.ok(result.min>=(height<650?16:20),'readable ball size '+JSON.stringify({width,height,result}));
  await page.screenshot({path:'layout_checks/score_'+width+'x'+height+'.png'});
 }
 for(const title of ['제1248회 원문 등록','항행의자유 업데이트']){
  await page.setViewportSize({width:393,height:851});await page.setContent(pageHtml);
  await page.evaluate(t=>modal(t,'<div class="panel"><h3>'+t+'</h3><p>확인 화면</p><textarea rows="30">원문 보존</textarea></div>'),title);
  const b=await page.locator('#v4051HomeDock button').boundingBox();assert.ok(b.y>790&&b.x>196,'Home fixed outside content');
  await page.locator('#mc').evaluate(e=>e.scrollTop=1000);const after=await page.locator('#v4051HomeDock button').boundingBox();assert.equal(b.y,after.y);
  await page.locator('#v4051HomeDock button').click();assert.equal(await page.locator('#modal').isVisible(),false);
 }
 await browser.close();console.log('PASS: rendered 20 rows/120 larger balls at six phone sizes; all visible, no overlap; bottom-right Home on results, raw editor and update; Home stable during scrolling.');
})().catch(e=>{console.error(e);process.exit(1)});
