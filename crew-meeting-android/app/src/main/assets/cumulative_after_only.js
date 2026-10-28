/* Integrated cumulative DB — result-after only. No guessed combinations, scores or archive writes. */
(function(){
 'use strict';
 var $=function(id){return document.getElementById(id);};
 var root=$('cumulativeAfterDashboard');
 if(!root)return;
 var roomIds=['v3-1','v3-2','v3-3','v3-4'],officialCache=null,officialPromise=null,seq=0;
 var roundInput=$('cumRoundInput'),currentRound=1;
 function validSix(nums){
  return Array.isArray(nums)&&nums.length===6&&nums.every(function(n){return Number.isInteger(n)&&n>=1&&n<=45;})&&new Set(nums).size===6;
 }
 function state(room,text){
  var el=root.querySelector('[data-cum-state="'+room+'"]');if(el)el.textContent=text;
 }
 function renderBalls(container,nums,winning,bonus){
  container.replaceChildren();
  for(var i=0;i<6;i++){
   var ball=document.createElement('span');ball.className='cumBall';
   var n=nums&&nums[i];
   ball.textContent=Number.isInteger(n)?String(n).padStart(2,'0'):'—';
   if(Number.isInteger(n)&&winning&&winning.includes(n))ball.classList.add('win');
   else if(Number.isInteger(n)&&n===bonus)ball.classList.add('bonus');
   container.appendChild(ball);
  }
 }
 function emptyCards(){
  roomIds.forEach(function(room){
   state(room,'결과후 원본 확인 전');
   var card=root.querySelector('[data-room="'+room+'"]');
   card.querySelectorAll('li').forEach(function(li){
    renderBalls(li.querySelector('.cumBalls'),null,null,null);
    var hit=li.querySelector('.cumHit');hit.textContent='—/6';hit.classList.add('pending');
   });
  });
 }
 function showDraw(entry){
  var out=$('cumDrawNumbers');out.replaceChildren();
  if(!entry){out.textContent='공식 당첨번호 미등록 · 채점 보류';return;}
  renderBalls(out,entry.numbers,null,null);
  var sep=document.createElement('span');sep.textContent='+';
  out.appendChild(sep);
  var bonus=document.createElement('span');bonus.className='cumBall bonus';
  bonus.textContent=String(entry.bonus).padStart(2,'0');out.appendChild(bonus);
 }
 async function officialDraws(){
  if(officialCache)return officialCache;
  if(!officialPromise)officialPromise=(async function(){
   var url=new URL('crew-official-draws.json',document.baseURI);
   var response=await fetch(url.href,{cache:'no-store'});
   if(!response.ok)throw Error('공식번호 내장파일 오류');
   var data=await response.json();
   if(data.schema!=='CAPTAIN_VERIFIED_OFFICIAL_DRAWS_R1'||data.count!==17||
      !Array.isArray(data.rows)||data.rows.length!==17)throw Error('공식번호 검증자료 불일치');
   var map=new Map();
   data.rows.forEach(function(x){
    if(!Number.isInteger(x.round_no)||x.round_no<1||x.round_no>1247||map.has(x.round_no)||
       !validSix(x.numbers)||!Number.isInteger(x.bonus)||x.bonus<1||x.bonus>45||
       x.numbers.includes(x.bonus)||typeof x.source_url!=='string'||!x.source_url.startsWith('https://www.dhlottery.co.kr/'))
       throw Error('공식번호 무결성 오류');
    map.set(x.round_no,x);
   });
   officialCache=map;return map;
  })().catch(function(e){officialPromise=null;throw e;});
  return officialPromise;
 }
 function setRound(n){
  if(!Number.isInteger(n)||n<1||n>1247){$('cumStatus').textContent='회차는 1~1247 사이로 입력하세요.';return false;}
  currentRound=n;roundInput.value=String(n);$('cumRoundLabel').textContent=n+'회차';
  $('cumPrev').disabled=n===1;$('cumNext').disabled=n===1247;
  return true;
 }
 async function load(n){
  if(!setRound(n))return;
  var serial=++seq;emptyCards();showDraw(null);
  $('cumStatus').textContent='제'+n+'회 · 공식번호와 결과후 원본 조회 중…';
  var draw=null;
  try{var map=await officialDraws();if(serial!==seq)return;draw=map.get(n)||null;showDraw(draw);}
  catch(e){if(serial!==seq)return;$('cumDrawNumbers').textContent='공식번호 내장자료 확인 실패';}
  if(!window.captainCumulativeIsAuthorized||!window.captainCumulativeIsAuthorized()){
   roomIds.forEach(function(room){state(room,'선장 인증 후 결과후 조회');});
   $('cumStatus').textContent='공식번호 '+(draw?'확인':'미등록')+' · 결과후 원본은 선장 인증 필요';return;
  }
  try{
   if(typeof window.captainCumulativeFetchAfter!=='function')throw Error('읽기전용 결과후 연결 없음');
   var data=await window.captainCumulativeFetchAfter(n);
   if(serial!==seq||!root.isConnected||root.hidden)return;
   if(!data||data.app!=='v3'||data.stage!=='after'||data.read_only!==true||
      data.raw_predictions_included!==false||!Array.isArray(data.items))throw Error('결과후 조회 계약 불일치');
   if(!data.linked){
    roomIds.forEach(function(room){state(room,'V3 결과후 원본 연결 대기');});
    $('cumStatus').textContent='제'+n+'회 · 결과후 원본 자동연계 대기 (미등록 ≠ 적중 0개)';return;
   }
   var grouped={};roomIds.forEach(function(r){grouped[r]=[];});
   var unassigned=0;
   data.items.forEach(function(item){
    if(Number(item.round_no)!==n)return;
    var key=String(item.room_key||item.room_id||'').toLowerCase();
    if(roomIds.includes(key))grouped[key].push(item);
    else unassigned++;
   });
   roomIds.forEach(function(room){
    var records=grouped[room];
    // Existing read-only API returns metadata only (raw_predictions_included=false).
    // Never synthesize a scored combination from a report count or an unrelated V7 engine.
    state(room,records.length?('결과후 메타 '+records.length+'건 · 조합 검증 대기'):'해당 회차 결과후 원본 없음');
   });
   $('cumStatus').textContent='제'+n+'회 · 결과후 메타 확인 · 채점 가능한 조합 원본 미연결'+(unassigned?' · 방 미분류 '+unassigned+'건':'');
  }catch(e){
   if(serial!==seq)return;
   roomIds.forEach(function(room){state(room,'결과후 원본 조회 실패');});
   $('cumStatus').textContent='제'+n+'회 · 결과후 원본 조회 실패 ('+String(e.message||e).slice(0,45)+')';
  }
 }
 window.captainCumulativeLoad=function(){return load(Number(roundInput.value)||currentRound);};
 $('cumPrev').addEventListener('click',function(){load(currentRound-1);});
 $('cumNext').addEventListener('click',function(){load(currentRound+1);});
 $('cumRefresh').addEventListener('click',function(){load(Number(roundInput.value));});
 roundInput.addEventListener('keydown',function(e){if(e.key==='Enter'){e.preventDefault();load(Number(roundInput.value));}});
 window.captainCumulativeRender=function(round){
  var n=Number.isInteger(round)&&round>=1&&round<=1247?round:currentRound;
  return load(n);
 };
 emptyCards();
})();
