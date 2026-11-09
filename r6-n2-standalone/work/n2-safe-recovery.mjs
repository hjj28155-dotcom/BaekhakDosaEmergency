// N2 restoration/overlay sequencing DRAFT. Never overwrite existing records.
// Must connect to actual storage and DOM only after inspecting APK assets/index.html.
export function planResearchRestore(backup, existingRecords=[]) {
 if(backup?.format!=='NAVIGATOR_R615_BACKUP'||!Array.isArray(backup.records)||!Array.isArray(existingRecords))throw Error('INVALID_INPUT');
 const records=backup.records.filter(r=>Number.isInteger(r?.round)&&r.round>=1&&r.round<=100);
 const validSet=a=>Array.isArray(a)&&a.length===6&&new Set(a).size===6&&a.every(n=>Number.isInteger(n)&&n>=1&&n<=45);
 if(records.length!==100||new Set(records.map(r=>r.round)).size!==100||records.some(r=>!Array.isArray(r.rooms)||r.rooms.length!==4||r.rooms.some(room=>!Array.isArray(room.games)||room.games.length!==5||room.games.some(g=>!validSet(g)))))throw Error('INVALID_100_RECORD_BACKUP');
 const existing=new Map();
 for(const r of existingRecords){if(!Number.isInteger(r?.round)||existing.has(r.round))throw Error('INVALID_EXISTING_RECORDS');existing.set(r.round,r);}
 const inserted=[],conflicts=[];
 for(const r of records){const old=existing.get(r.round);if(!old)inserted.push(r);else if(JSON.stringify(old)!==JSON.stringify(r))conflicts.push(r.round);}
 return conflicts.length?{safe:false,conflicts,merged:null}:{safe:true,conflicts:[],inserted:inserted.length,merged:[...existingRecords,...inserted].sort((a,b)=>b.round-a.round)};
}
export async function openOverlayThenPng({showOverlay,drawTwentyPng,showPng,analysisResult}){
 if([showOverlay,drawTwentyPng,showPng].some(f=>typeof f!=='function'))throw Error('MISSING_UI_FUNCTION');
 await showOverlay(analysisResult);
 const png=await drawTwentyPng(analysisResult);
 if(!png)throw Error('PNG_RENDER_FAILED');
 await showPng(png,analysisResult);
 return {overlayFirst:true,pngDisplayed:true};
}