/* Captain V4 PNG: real PNG canvas, validated R46/R1 result events only. No invented combinations. */
(function(){
 'use strict';
 var byId=function(id){return document.getElementById(id);};
 var preview=byId('freedomPngPreview'),save=byId('freedomSavePng'),notice=byId('freedomPngStatus');
 if(!preview||!save||!notice)return;
 var W=960,H=1350,canvas=document.createElement('canvas');
 canvas.width=W;canvas.height=H;
 var ctx=canvas.getContext('2d');
 var current={captain:null,liaison:null},lastPng='',lastRound=null,renderPending=false;
 function validGames(games){
  if(!Array.isArray(games)||games.length!==5)return null;
  var seen=new Set(),clean=[];
  for(var row of games){
   if(!Array.isArray(row)||row.length!==6)return null;
   var nums=row.map(Number);
   if(nums.some(function(n){return !Number.isInteger(n)||n<1||n>45;})||new Set(nums).size!==6)return null;
   nums.sort(function(a,b){return a-b;});
   var key=nums.join(',');
   if(seen.has(key))return null;
   seen.add(key);clean.push(nums);
  }
  return clean;
 }
 function rect(x,y,w,h,r,fill,stroke,lineWidth){
  ctx.beginPath();ctx.roundRect(x,y,w,h,r);ctx.fillStyle=fill;ctx.fill();
  if(stroke){ctx.strokeStyle=stroke;ctx.lineWidth=lineWidth||2;ctx.stroke();}
 }
 function label(text,x,y,size,color,bold,align){
  ctx.font=(bold?'800 ':'500 ')+size+'px "Noto Sans KR","Malgun Gothic",sans-serif';
  ctx.fillStyle=color;ctx.textAlign=align||'left';ctx.fillText(text,x,y);
 }
 function ball(n,x,y,accent){
  ctx.beginPath();ctx.arc(x,y,29,0,Math.PI*2);
  ctx.fillStyle=n==null?'#123a5c':'#174d77';ctx.fill();
  ctx.strokeStyle=n==null?'#5d829d':accent;ctx.lineWidth=2.5;ctx.stroke();
  label(n==null?'—':String(n).padStart(2,'0'),x,y+11,31,n==null?'#8baec5':'#fff4d6',true,'center');
 }
 function section(y,title,sub,games,accent,ready){
  rect(40,y,880,465,20,'#082b49',accent,3);
  rect(40,y,880,82,19,accent);
  label(title,70,y+52,32,'#ffffff',true);
  label(ready?'검증된 5조합':'독립 원본 확인 대기',891,y+52,21,'#f5f3e9',true,'right');
  label(sub,70,y+121,22,'#c8e3f1',false);
  for(var i=0;i<5;i++){
   var cy=y+160+i*60;
   if(i%2===0)rect(64,cy-33,828,57,9,'#103957');
   label(String(i+1)+'.',83,cy+8,26,'#e9f3fb',true);
   var row=ready?games[i]:null;
   for(var j=0;j<6;j++)ball(row?row[j]:null,184+j*130,cy,accent);
  }
 }
 function draw(){
  renderPending=false;
  var raw=String(byId('freedomAnalysisRound').value||'').trim();
  var round=/^[1-9][0-9]*$/.test(raw)?Number(raw):null;
  if(!Number.isInteger(round)||round<1||round>1247)round=null;
  var captain=current.captain&&current.captain.round===round?current.captain:null;
  var liaison=current.liaison&&current.liaison.round===round?current.liaison:null;
  ctx.fillStyle='#041a2f';ctx.fillRect(0,0,W,H);
  rect(13,13,W-26,H-26,22,'#061f37','#d9b36f',5);
  label('통제실장 · 원문분석 보고서',48,83,36,'#ffe0a3',true);
  label('제'+(round==null?'—':round)+'회차 · 독립 엔진 5조합 × 2',49,126,23,'#bcd8e8',false);
  section(160,'선장엔진 R46 · 5조합',
   captain?'DB-1 원문 검증 · 선장엔진 실제 출력':'선장엔진 결과 미확인 · 임의 번호 생성 금지',
   captain&&captain.games,'#167c9c',!!captain);
  section(638,'통제실장엔진 R1 · 5조합',
   liaison?'독립 사전원본 검증 연구 · 공식 잠금 아님':'V3·도사 독립 사전원본 필요 · 통제실장엔진 보류',
   liaison&&liaison.games,'#8956bb',!!liaison);
  rect(40,1118,880,180,12,'#0b324c','#617f93',2);
  label('결과전 연구자료 · 공식 추첨 전 확정 잠금 증빙 아님',61,1175,23,'#f4d6a4',true);
  label('검증된 원본만 표시 · 미확인 조합은 빈칸 유지',61,1220,22,'#c8dfec',false);
  label('중앙 자료실 자동업로드: 별도 검증 필요',61,1261,20,'#b2c7d8',false);
  try{
   lastPng=canvas.toDataURL('image/png');
   if(!lastPng.startsWith('data:image/png;base64,'))throw Error('PNG_ENCODING');
   preview.src=lastPng;
   lastRound=round;
   save.disabled=false;
   notice.textContent='PNG 실제 이미지 생성 완료 · 선장 '+(captain?'5':'0')+'/5 · 통제실장 '+(liaison?'5':'0')+'/5 · 미확인은 빈칸';
  }catch(e){
   lastPng='';preview.removeAttribute('src');save.disabled=true;
   notice.textContent='PNG 생성 실패 · 원본 결과는 보존됩니다';
  }
 }
 function schedule(){if(renderPending)return;renderPending=true;requestAnimationFrame(draw);}
 window.addEventListener('captain-v4-reset',function(){current.captain=null;current.liaison=null;schedule();});
 window.addEventListener('captain-v4-engine-reset',function(e){
  if(e.detail&&['captain','liaison'].includes(e.detail.engine)){current[e.detail.engine]=null;schedule();}
 });
 // A verified server readback must render the identical PNG, but must NOT
 // emit the original engine-run event (that event starts an archive POST).
 window.addEventListener('captain-v4-archived',function(e){
  var d=e.detail||{};
  if(!['captain','liaison'].includes(d.engine)||!Number.isInteger(d.round)||d.round<1||d.round>1247)return;
  var games=validGames(d.games);
  if(!games||!(/^[a-f0-9]{64}$/).test(String(d.sourceSha256||''))||
    !(/^[a-f0-9]{64}$/).test(String(d.reportSha256||'')))return;
  if(d.engine==='captain'&&d.provenance!=='R46_DB1_VERIFIED')return;
  if(d.engine==='liaison'&&d.provenance!=='R1_INDEPENDENT_ARCHIVE_RESEARCH_NOT_LOCKED')return;
  current[d.engine]={round:d.round,games:games,sourceSha256:d.sourceSha256,archived:true};
  schedule();
 });
 window.addEventListener('captain-v4-verified',function(e){
  var d=e.detail||{};
  if(!['captain','liaison'].includes(d.engine)||!Number.isInteger(d.round)||d.round<1||d.round>1247)return;
  var games=validGames(d.games);
  if(!games||!(/^[a-f0-9]{64}$/).test(String(d.sourceSha256||'')))return;
  if(d.engine==='captain'&&d.provenance!=='R46_DB1_VERIFIED')return;
  if(d.engine==='liaison'&&d.provenance!=='R1_INDEPENDENT_ARCHIVE_RESEARCH_NOT_LOCKED')return;
  current[d.engine]={round:d.round,games:games,sourceSha256:d.sourceSha256};
  schedule();
 });
 byId('freedomAnalysisRound').addEventListener('input',schedule);
 byId('freedomPngZoom').addEventListener('click',function(){
  var viewport=document.querySelector('#freedomPngPanel .v4PngViewport');
  if(!viewport)return;
  var zoomed=viewport.classList.toggle('zoomed');
  this.textContent=zoomed?'전체 보기':'확대';
  viewport.scrollTop=0;viewport.scrollLeft=0;
 });
 save.addEventListener('click',function(){
  if(!lastPng){notice.textContent='저장할 PNG 이미지가 없습니다';return;}
  var name='HANGHAE_V4_R'+(lastRound||'UNKNOWN')+'_R46_R1.png';
  try{
   if(window.CrewPngSaver&&typeof window.CrewPngSaver.savePng==='function'){
    var result=String(window.CrewPngSaver.savePng(lastPng,name));
    notice.textContent=result==='SAVED'?'PNG 기기 사진함 저장 완료':result==='PICKER'?'PNG 저장 위치 선택창을 확인하세요':('PNG 저장 실패: '+result.slice(0,80));
   }else{
    var link=document.createElement('a');link.href=lastPng;link.download=name;
    document.body.appendChild(link);link.click();link.remove();
    notice.textContent='PNG 다운로드를 요청했습니다 · 기기 파일에서 확인하세요';
   }
  }catch(e){notice.textContent='PNG 저장 기능을 사용할 수 없습니다 · 이미지 표시는 유지';}
 });
 schedule();
})();

