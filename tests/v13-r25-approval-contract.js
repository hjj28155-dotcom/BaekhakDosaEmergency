/* V13 R25 approval contract: compares full original R24 with staged R25.
 * Pure JavaScript: no runtime DB, APK, or UI mutation.
 */
(function(root){
"use strict";
function check(r24,r25){
 const assertions=[];
 const test=(name,pass)=>assertions.push({name,pass:!!pass});
 const one=(s,re)=>s.match(re)?.[1]??null;
 const inlineScripts=s=>[...s.matchAll(/<script(?:\s+[^>]*)?>([\s\S]*?)<\/script>/g)].map(x=>x[1]);
 const orig=inlineScripts(r24),next=inlineScripts(r25);
 const originalRaw=one(r24,/<script id="raw-data" type="application\/json">([\s\S]*?)<\/script>/);
 const nextRaw=one(r25,/<script id="raw-data" type="application\/json">([\s\S]*?)<\/script>/);
 const oldNav=one(r24,/const visibleMenuOrder=Object.freeze\(\[([^\]]+)\]\)/);
 const newNav=one(r25,/const visibleMenuOrder=Object.freeze\(\[([^\]]+)\]\)/);
 const css=r25.includes("V13_R25_CAPTAIN_APPROVED_REAR_MENU");
 test("R24 raw data exists",originalRaw!==null);
 test("DB-1 raw six sentence JSON unmodified",originalRaw===nextRaw);
 test("script count unchanged",orig.length===next.length);
 test("research engine script unchanged",one(r24,/<script id="V13_NAVIGATOR_INDEPENDENT_V1_STAGING">([\s\S]*?)<\/script>/)===one(r25,/<script id="V13_NAVIGATOR_INDEPENDENT_V1_STAGING">([\s\S]*?)<\/script>/));
 test("baseline menu present",oldNav==="0,7,8,9,10,6,3,11,12,13");
 test("nine approved menu entries",newNav==="7,8,10,9,6,3,11,12,13");
 test("renamed displayed DOSA room",r25.includes("신서방(구 도사방)"));
 test("central raw button exists",r25.includes('id="v13CentralRawHotspot"'));
 test("central raw click uses original pick(0)",r25.includes("if(btn.hasAttribute('data-v13-home-raw')){pick(0);return}"));
 test("menu overlay keeps background width",css&&r25.includes("grid-template-columns:minmax(0,1fr)")&&r25.includes("#workspace.menu-visible #rail{display:flex;position:absolute"));
 test("close returns full-screen home",r25.includes("function closeRightPanel(){open=false;view='home';render()}"));
 test("native update handler preserved",r24.includes("window.V13Native.checkForUpdates(true)")&&r25.includes("window.V13Native.checkForUpdates(true)"));
 test("R25 doc revision set",r25.includes("const V13_REMOTE_UI_DOCUMENT_REVISION=25;"));
 test("legacy results and DB handlers unchanged by deletion",r25.includes("function v13CumulativeReadOnlyView()")&&r25.includes("function applyActiveRound(value, message)"));
 const passed=assertions.filter(x=>x.pass).length;
 return {passed,total:assertions.length,ok:passed===assertions.length,assertions};
}
root.V13R25ApprovalContract=Object.freeze({check});
})(globalThis);
