/* Integrated cumulative DB — result-after only. No guessed combinations, scores or archive writes. */
(function(){
 'use strict';
 var $=function(id){return document.getElementById(id);};
 var root=$('cumulativeAfterDashboard');
 if(!root)return;
 var roomIds=['v3-1','v3-2','v3-3','v3-4','v7','v4-1','v4-2'],officialCache=null,officialPromise=null,seq=0;
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
   var results=await window.captainCumulativeFetchAfter(n);
   if(serial!==seq||!root.isConnected||root.hidden)return;
   if(!Array.isArray(results)||results.length!==3||
      results.some(function(x){return !x||!['v3','v7','freedom'].includes(x.app);})||
      new Set(results.map(function(x){return x.app;})).size!==3)
      throw Error('3앱 결과후 조회 계약 불일치');
   var stats=[],failCount=0,waitingCount=0;
   results.forEach(function(result){
    var app=result.app;
    var targets=app==='v3'?roomIds.slice(0,4):app==='v7'?['v7']:['v4-1','v4-2'];
    if(result.error){
     failCount++;
     targets.forEach(function(room){state(room,'서버 접속 불가 · 원본 미확인');});
     stats.push((app==='freedom'?'V4':app.toUpperCase())+' 접속 불가');
     return;
    }
    var data=result.data;
    if(!data||data.app!==app||data.stage!=='after'||data.read_only!==true||
       data.raw_predictions_included!==false||typeof data.linked!=='boolean'||
       !Array.isArray(data.items)){
     failCount++;
     targets.forEach(function(room){state(room,'응답 검증 실패 · 원본 미확인');});
     stats.push((app==='freedom'?'V4':app.toUpperCase())+' 검증 실패');
     return;
    }
    if(!data.linked){
     waitingCount++;
     targets.forEach(function(room){state(room,'결과후 원본 자동연계 대기');});
     stats.push((app==='freedom'?'V4':app.toUpperCase())+' 연계 대기');
     return;
    }
    var records=data.items.filter(function(item){return Number(item.round_no)===n;});
    if(app==='v3'){
     var grouped={'v3-1':0,'v3-2':0,'v3-3':0,'v3-4':0},unassigned=0;
     records.forEach(function(item){
      var key=String(item.room_key||item.room_id||'').trim().toLowerCase();
      if(Object.prototype.hasOwnProperty.call(grouped,key))grouped[key]++;
      else unassigned++;
     });
     targets.forEach(function(room){
      var count=grouped[room];
      state(room,count?'결과후 메타 '+count+'건 · 6번호 미수신':
       (unassigned?'방 구분 미확인 · 조합 원본 미수신':'결과후 원본 수신 대기'));
     });
     stats.push('V3 메타 '+records.length+'건');
    }else if(app==='v7'){
     var count=records.filter(function(item){
      return ['v7','v7-control','control'].includes(String(item.room_key||item.room_id||'').trim().toLowerCase());
     }).length;
     state('v7',count?'결과후 메타 '+count+'건 · 6번호 미수신':
       (records.length?'방 구분 미확인 · 6번호 미수신':'결과후 원본 수신 대기'));
     stats.push('V7 메타 '+records.length+'건');
    }else{
     // V4 has two *independent* 5-combination runs. Never split app-wide
     // metadata between them unless an explicit verified run identifier exists.
     var v4Counts={'v4-1':0,'v4-2':0},unknown=0;
     records.forEach(function(item){
      var key=String(item.room_key||item.room_id||'').trim().toLowerCase();
      if(['v4-1','v4-captain'].includes(key))v4Counts['v4-1']++;
      else if(['v4-2','v4-liaison'].includes(key))v4Counts['v4-2']++;
      else unknown++;
     });
     targets.forEach(function(room){
      state(room,v4Counts[room]?'결과후 메타 '+v4Counts[room]+'건 · 6번호 미수신':
       (unknown?'V4 메타 '+unknown+'건 · 1·2차 미분류':'독립 결과후 원본 수신 대기'));
     });
     stats.push('V4 메타 '+records.length+'건');
    }
   });
   $('cumStatus').textContent='제'+n+'회 · '+stats.join(' / ')+
      (failCount?' · 서버 접속 불가 '+failCount+'앱':'')+
      ' · 원본 미수신은 적중 0개가 아님';
  }catch(e){
   if(serial!==seq)return;
   roomIds.forEach(function(room){state(room,'결과후 연결 확인 필요 · 원본 보존');});
   $('cumStatus').textContent='제'+n+'회 · 결과후 서버 조회 중단 · 원본은 그대로 보존';
  }
 }
 window.captainCumulativeLoad=function(){return load(Number(roundInput.value)||currentRound);};
 $('cumPrev').addEventListener('click',function(){load(currentRound-1);});
 $('cumNext').addEventListener('click',function(){load(currentRound+1);});
 $('cumRefresh').addEventListener('click',function(){load(Number(roundInput.value));});
 $('cumJumpMore').addEventListener('click',function(){
  var card=root.querySelector('[data-room="v7"]');
  if(card){card.scrollIntoView({behavior:'smooth',block:'start',inline:'nearest'});$('cumRoomGrid').focus({preventScroll:true});}
 });
 roundInput.addEventListener('keydown',function(e){if(e.key==='Enter'){e.preventDefault();load(Number(roundInput.value));}});
 window.captainCumulativeRender=function(round){
  var n=Number.isInteger(round)&&round>=1&&round<=1247?round:currentRound;
  return load(n);
 };
 emptyCards();
})();
