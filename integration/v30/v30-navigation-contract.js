/* V30 button routing — staging only. No fake completion or DB writes. */
(function(root){
"use strict";
const APP_VERSION="V30";
const ROUTES=Object.freeze([
{id:"raw",label:"원문 분석",handler:"openRawAnalysis"},
{id:"captain",label:"선장 원문방",handler:"openCaptainRoom"},
{id:"sinseo",label:"신서방",handler:"openSinseoRoom"},
{id:"navigator",label:"항해사방",handler:"openNavigatorRoom"},
{id:"analysis",label:"분석방 (라이브러리)",handler:"openLibraryAnalysisRoom"},
{id:"score",label:"성적 관리",handler:"openScore"},
{id:"cumulative",label:"누적 DB방",handler:"openCumulativeDb"},
{id:"history",label:"분석 기록",handler:"openHistory"},
{id:"update",label:"업데이트",handler:"openUpdate"},
{id:"backup",label:"백업/복원",handler:"openBackupRestore"},
{id:"materials",label:"자료실",handler:"openMaterials"},
{id:"notice",label:"공지사항",handler:"openNotice"}
]);
function bind(host){
 if(!host||typeof host!=="object")throw Error("V30_HOST_REQUIRED");
 const missing=ROUTES.filter(route=>typeof host[route.handler]!=="function");
 function audit(){return {version:APP_VERSION,ready:missing.length===0,required:ROUTES.length,missing:missing.map(r=>r.id)};}
 async function open(id){
  const route=ROUTES.find(r=>r.id===id);
  if(!route)throw Error("UNKNOWN_V30_BUTTON:"+id);
  if(typeof host[route.handler]!=="function")throw Error("V30_BUTTON_NOT_CONNECTED:"+id);
  return await host[route.handler]();
 }
 return Object.freeze({audit,open});
}
root.V30Navigation=Object.freeze({APP_VERSION,ROUTES,bind});
})(globalThis);
