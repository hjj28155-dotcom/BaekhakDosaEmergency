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
  var v7Ref=$('cumV7ArchiveReference');if(v7Ref)v7Ref.hidden=true;
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
  // Server verifies DB-1 original SHA, receipt canonical SHA and official draw.
  // Missing V3/V7/R1 sources remain pending, never zero-score placeholders.
  try{
   if(typeof window.captainSevenRoomScoreRead!=='function')throw Error('NO_VERIFIED_SCORE_BRIDGE');
   var scored=await window.captainSevenRoomScoreRead(n);
   if(serial!==seq||!root.isConnected||root.hidden)return;
   if(scored.round!==n||!Array.isArray(scored.details))throw Error('SCORE_CONTRACT');
   var found=0;
   roomIds.forEach(function(key){
    var item=scored.details.find(function(x){return x.room_key===key&&x.official_eligible===false&&
     x.provenance==='RETROSPECTIVE_RESEARCH_ONLY'&&Array.isArray(x.rows)&&x.rows.length===5;});
    if(!item){
     if(key==='v4-1'||key==='v4-2')state(key,key==='v4-2'?'통제실장 R1 독립 원본 미수신 · 0점 아님':'선장 R46 검증 연구 원본 미수신 · 0점 아님');return;
    }
    if(!draw||!item.draw||!Array.isArray(item.draw.numbers)||
      draw.numbers.slice().sort(function(a,b){return a-b;}).join(',')!==
      item.draw.numbers.slice().sort(function(a,b){return a-b;}).join(',')||
      draw.bonus!==item.draw.bonus){
     state(key,'공식 당첨번호 대조 불일치 · 연구 채점 보류');return;
    }
    var card=root.querySelector('[data-room="'+key+'"]');
    card.querySelectorAll('li').forEach(function(li,i){
     var row=item.rows[i],nums=row.numbers;
     if(!validSix(nums))return;
     renderBalls(li.querySelector('.cumBalls'),nums,draw.numbers,draw.bonus);
     var hit=li.querySelector('.cumHit');
     hit.textContent=row.bucket===6?'5+보':row.hits+'/6';
     hit.classList.remove('pending');
    });
    state(key,(key==='v4-1'?'선장 R46':key==='v4-2'?'통제실장 R1':key.toUpperCase())+' · 검증 연구 5조합 사후 대조 · 공식 잠금 아님');
    found++;
   });
   $('cumStatus').textContent+=' · '+found+'/7방 실제 원본 '+(found*5)+'조합 검증된 연구 사후 대조';
  }catch(e){
   if(serial!==seq)return;
   roomIds.forEach(function(key){state(key,'연구 채점 서버 조회 실패 · 원본 보존');});
  }
  // Separate legacy V7 captain archive reference, NOT V7 controller room.
  try{
   var receipt=await window.captainEngineRoundRead(n);
   if(serial!==seq||!root.isConnected||root.hidden)return;
   var archive=receipt&&receipt.archived_v7_captain_reference;
   var refPanel=$('cumV7ArchiveReference');
   if(refPanel){
    refPanel.hidden=!(archive&&Array.isArray(archive.combinations)&&archive.combinations.length===5&&archive.combinations.every(validSix));
    if(!refPanel.hidden){
     $('cumV7ArchiveRound').textContent=String(n);
     $('cumV7ArchiveRows').replaceChildren();
     archive.combinations.forEach(function(nums,i){
      var li=document.createElement('li');
      li.textContent=(i+1)+'. '+nums.map(function(v){return String(v).padStart(2,'0');}).join(' · ');
      $('cumV7ArchiveRows').appendChild(li);
     });
    }
   }
  }catch(e){if(serial!==seq)return;}

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
 // A completed receipt is visible in the central room immediately when
 // this page is open, without requiring a second manual analysis.
 window.addEventListener('captain-v4-archive-saved',function(e){
  if(!root.hidden&&Number(e.detail&&e.detail.round)===currentRound)load(currentRound);
 });
 window.addEventListener('captain-auth-ready',function(){
  if(!root.hidden&&$('materialsBody').classList.contains('cumulativeOnlyMode'))load(currentRound);
 });
 emptyCards();
 // The cumulative performance room never computes statistics from metadata.
 // Each grade is populated ONLY after all 6 source numbers, draw, provenance,
 // and deduplication are verified in a future source connector.
 var scoreSeq=0;
 var scoreRoot=$('cumulativeScoreDashboard');
 var scoreKeys=['all','v3-1','v3-2','v3-3','v3-4','v7','v4-1','v4-2'];
 function resetScore(){
  scoreRoot.querySelectorAll('[data-score-grade]').forEach(function(el){el.textContent='—';});
  scoreRoot.querySelectorAll('[data-score-grade-percent]').forEach(function(el){el.textContent='—%';});
  scoreKeys.forEach(function(key){
   var total=scoreRoot.querySelector('[data-score-total="'+key+'"]');
   var success=scoreRoot.querySelector('[data-score-success="'+key+'"]');
   if(total)total.textContent='미집계';
   if(success)success.textContent='—%';
  });
  $('scoreTotalCount').textContent='미집계';
  $('scoreSuccessPercent').textContent='—%';
 }
 function scoreActive(){
  return scoreRoot.isConnected&&!scoreRoot.hidden&&
   $('materialsBody').classList.contains('cumulativeScoreMode');
 }
 function researchRate(grades,threshold){
  var n=grades.reduce(function(a,b){return a+b;},0);
  if(!n)return null;
  var from=threshold===6?7:threshold;
  var success=grades.slice(from).reduce(function(a,b){return a+b;},0);
  return (100*success/n).toFixed(2)+'%';
 }
 async function renderScore(){
  var serial=++scoreSeq,threshold=Number($('scoreSuccessThreshold').value)||3;
  resetScore();
  $('scoreSyncStatus').textContent='7방 원본·당첨번호·해시 검증 서버 조회 중…';
  $('scoreStatus').textContent='연구 기록만 사후 검증합니다. 추첨 전 공식 구매 성적과 혼합하지 않습니다.';
  if(typeof window.captainCumulativeIsAuthorized==='function'&&!window.captainCumulativeIsAuthorized()){
   $('scoreSyncStatus').textContent='선장 인증 대기 · 원본 자료 보존';return;
  }
  if(typeof window.captainSevenRoomScoreRead!=='function'){
   $('scoreSyncStatus').textContent='7방 연구 채점 연결 미설치 · 기존 성적은 미집계';return;
  }
  try{
   var result=await window.captainSevenRoomScoreRead(null);
   if(serial!==scoreSeq||!scoreActive())return;
   if(!result||result.ok!==true||result.read_only!==true||result.official_eligible!==false||
      result.scope!=='SEVEN_ROOM_RESEARCH_ONLY'||!result.rooms||!result.total||
      !Array.isArray(result.total.grades)||result.total.grades.length!==8)throw Error('SCORE_CONTRACT');
   var total=result.total,denom=total.graded_games;
   if(denom){
    total.grades.forEach(function(count,i){
     var countNode=scoreRoot.querySelector('[data-score-grade="'+i+'"]');
     var percentNode=scoreRoot.querySelector('[data-score-grade-percent="'+i+'"]');
     if(countNode)countNode.textContent=String(count);
     if(percentNode)percentNode.textContent=(100*count/denom).toFixed(2)+'%';
    });
    $('scoreTotalCount').textContent=String(denom)+'조합 (연구)';
    $('scoreSuccessPercent').textContent=researchRate(total.grades,threshold)||'—%';
   }
   scoreKeys.forEach(function(key){
    var row=key==='all'?total:result.rooms[key];
    if(!row||!Number.isInteger(row.graded_games)||!Array.isArray(row.grades)||row.grades.length!==8)return;
    var totalNode=scoreRoot.querySelector('[data-score-total="'+key+'"]');
    var successNode=scoreRoot.querySelector('[data-score-success="'+key+'"]');
    if(row.graded_games){
     if(totalNode)totalNode.textContent=row.graded_games+'조합 · 연구';
     if(successNode)successNode.textContent=researchRate(row.grades,threshold)||'—%';
    }
   });
   var names={'v3-1':'V3 도사방','v3-2':'V3 항해사방','v3-3':'V3 자동1방','v3-4':'V3 자동2방',
    'v7':'V7 통제관방','v4-1':'V4 선장 R46','v4-2':'V4 통제실장 R1'};
   $('scoreSyncStatus').textContent=scoreKeys.slice(1).map(function(key){
    var room=result.rooms[key];
    return names[key]+': '+(room&&room.graded_games?
     '검증된 연구 '+room.verified_rounds+'회차 · '+room.graded_games+'조합 사후 대조':
     '독립 원본 미수신 · 미집계 (0점 아님)');
   }).join('\n');
   $('scoreStatus').textContent='총 '+denom+'조합은 실제 수신증·DB-1 SHA·공식 당첨번호가 검증된 과거 연구 표본입니다. '+
    '7방 전체 35조합/회차 완료가 아니며, 미래 당첨 확률이나 공식 추첨 전 구매 실적으로 해석하지 마세요.';
  }catch(e){
   if(serial!==scoreSeq||!scoreActive())return;
   $('scoreSyncStatus').textContent='검증된 연구 성적 서버 조회 실패 · 원본 삭제 및 0점 처리 없음';
   $('scoreStatus').textContent='인증·연결 확인 필요 · 기존 자료 보존';
  }
 }
 $('cumGoTotalScore').addEventListener('click',function(){
  if(typeof window.captainCumulativeScoreOpen==='function')window.captainCumulativeScoreOpen();
 });
 $('scoreBack').addEventListener('click',function(){
  if(typeof window.captainCumulativeOpen==='function')window.captainCumulativeOpen(Number(roundInput.value)||1);
 });
 $('scoreRefresh').addEventListener('click',renderScore);
 $('scoreSuccessThreshold').addEventListener('change',renderScore);
 window.captainCumulativeScoreRender=renderScore;

})();
