/* Last tested V4.3.15 mobile button event bindings. Include after the main HTML scripts. */

(function(){
'use strict';
var ACTIONS={
 'v4OpenLibrary()':['v4OpenLibrary',[]],
 'v4OpenNotice()':['v4OpenNotice',[]],
 'showRawCurrent()':['showRawCurrent',[]],
 'showFinal5()':['showFinal5',[]],
 'showCentral()':['showCentral',[]],
 'showDbStats()':['showDbStats',[]],
 'showSettings()':['showSettings',[]],
 'startAnalysis()':['startAnalysis',[]],
 'showFiles("ALL")':['showFiles',['ALL']],
 "showFiles('ALL')":['showFiles',['ALL']],
 'showAnalysisResult()':['showAnalysisResult',[]],
 'showBeforeAfter()':['showBeforeAfter',[]],
 'v4OpenInbox()':['v4OpenInbox',[]],
 'openApprovalCenter()':['openApprovalCenter',[]],
 'showUpdate()':['showUpdate',[]],
 'exportIndex()':['exportIndex',[]],
 'shiftRound(-1)':['shiftRound',[-1]],
 'shiftRound(1)':['shiftRound',[1]]
};
function notify(err){
 var msg='버튼 실행 오류: '+String(err&&err.message||err||'알 수 없는 오류');
 try{
  if(typeof window.openModal==='function')window.openModal('⚠ 기능 실행 점검','<div class="fileitem">'+msg.replace(/[&<>"]/g,function(c){return {'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c]})+'</div>');
  else alert(msg);
 }catch(e){alert(msg)}
}
function bind(){
 var count=0;
 document.querySelectorAll('button[onclick],.tile[onclick]').forEach(function(button){
  if(button.dataset.v4SafeBound==='1')return;
  var handler=(button.getAttribute('onclick')||'').trim().replace(/;$/,'');
  var action=ACTIONS[handler];
  if(!action)return;
  button.removeAttribute('onclick');
  button.type='button';
  button.dataset.v4SafeBound='1';
  button.addEventListener('click',function(){
    try{
      var fn=window[action[0]];
      if(typeof fn!=='function')throw Error(action[0]+' 연결되지 않음');
      var result=fn.apply(window,action[1]);
      if(result&&typeof result.catch==='function')result.catch(notify);
    }catch(err){notify(err)}
  },{passive:true});
  count++;
 });
 window.v4LastSafeButtonBindings=count;
}
window.v4ButtonRuntimeCheck=function(){
 var missing=Object.values(ACTIONS).filter(function(a){return typeof window[a[0]]!=='function'}).map(function(a){return a[0]});
 var message=missing.length?'기능 연결 오류: '+Array.from(new Set(missing)).join(', '):'홈화면 기능 연결 확인 · 버튼 이벤트 등록 '+Number(window.v4LastSafeButtonBindings||0)+'개';
 if(typeof window.openModal==='function')window.openModal('홈화면 버튼 진단','<div class="fileitem">'+message+'</div>');
 return {missing:missing,binds:window.v4LastSafeButtonBindings||0};
};
if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',bind);
else bind();
})();

