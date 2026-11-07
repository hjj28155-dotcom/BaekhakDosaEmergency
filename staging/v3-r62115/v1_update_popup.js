/* V3 R6.21.15 staged V1 update popup. No engine or analysis data mutation. */
(function(){
"use strict";
const FEED="https://raw.githubusercontent.com/hjj28155-dotcom/BaekhakDosaEmergency/main/v35/update/master.json";
const APP_ID="com.baekhak.dosa.v3", CHANNEL="v35-master";
let downloading=null, busy=false;
function versionCode(){
 try{if(window.AndroidHost&&typeof window.AndroidHost.getNativeVersionCode==="function"){
 const n=Number(window.AndroidHost.getNativeVersionCode());if(Number.isSafeInteger(n)&&n>0)return n;}}catch(_){}
 return null;
}
function validURL(url){try{const u=new URL(url);return u.protocol==="https:"&&!!u.hostname&&!u.username&&!u.password;}catch(_){return false;}}
function valid(d){return d&&d.schema===1&&d.app_id===APP_ID&&d.channel===CHANNEL&&d.update_available===true&&
 Number.isSafeInteger(d.version_code)&&d.version_code>0&&typeof d.version_name==="string"&&d.version_name.length<=80&&
 typeof d.apk_url==="string"&&validURL(d.apk_url)&&typeof d.apk_sha256==="string"&&/^[0-9a-f]{64}$/i.test(d.apk_sha256);}
window.V3Updater={
 async check(){
  if(busy)return;busy=true;
  try{
   const abort=new AbortController(),timer=setTimeout(()=>abort.abort(),7500);let res;
   try{res=await fetch(FEED+"?nocache="+Date.now(),{cache:"no-store",signal:abort.signal});}finally{clearTimeout(timer);}
   if(!res.ok)return;
   const d=await res.json();
   if(!d||d.schema!==1||d.app_id!==APP_ID||d.channel!==CHANNEL)return;
   const n=versionCode(),newer=valid(d)&&n!==null&&d.version_code>n;
   const noticeId=typeof d.notice_id==="string"&&/^[\w.-]{1,96}$/.test(d.notice_id)?d.notice_id:null;
   const key="v3_notice_seen_"+CHANNEL,seen=noticeId?localStorage.getItem(key):null;
   if(!newer&&(!noticeId||seen===noticeId))return;
   downloading=newer?d:null;
   if(noticeId)localStorage.setItem(key,noticeId);
   if(typeof window.modal!=="function"||typeof window.esc!=="function")return;
   const title=newer?"새 버전 안내":"항해사앱 공지사항";
   const msg=typeof d.notice==="string"?d.notice.slice(0,1000):"";
   const body='<div class="panel goldline"><h3>'+window.esc(title)+'</h3>'+
    (newer?"<p>새 버전: <b>"+window.esc(d.version_name)+"</b></p>":"")+
    '<p style="white-space:pre-wrap">'+window.esc(msg)+'</p></div><div class="row">'+
    (newer?'<button class="b green" onclick="V3Updater.download()">업데이트 다운로드</button>':"")+
    '<button class="b" onclick="closeM()">닫기</button></div>';
   window.modal(title,body);
  }catch(_){/* network failures never block analysis */}finally{busy=false;}
 },
 download(){
  const d=downloading;if(!valid(d))return;
  const a=document.createElement("a");a.href=d.apk_url;a.target="_blank";a.rel="noopener noreferrer";
  document.body.appendChild(a);a.click();a.remove();
 }
};
window.addEventListener("load",()=>setTimeout(()=>window.V3Updater.check(),1800));
})();