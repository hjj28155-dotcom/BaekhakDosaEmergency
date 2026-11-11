/** N2 captain-approved home logic. Isolated candidate; NO DB, APK, installer or release mutations. */
export const ROOMS=Object.freeze(['전체방','1방','2방','3방','4방']);
export function roundNumber(value){const s=String(value??'').trim();if(!/^\d{1,4}$/.test(s))return null;const n=Number(s);return n>=1&&n<=9999?n:null}
export function formatRound(v){const n=roundNumber(v);return n===null?'0000':String(n).padStart(4,'0')}
export function stepRound(v,d){if(d!==1&&d!==-1)throw Error('INVALID_DIRECTION');const n=roundNumber(v);return n===null?null:Math.max(1,Math.min(9999,n+d))}
export function selectSavedRound(v,available){const n=roundNumber(v);return n!==null&&Array.isArray(available)&&available.some(x=>roundNumber(x)===n)?n:null}
export function selectRoom(v){return ROOMS.includes(v)?v:null}
export function presentationRequest({round,room}){const n=roundNumber(round),r=selectRoom(room);return n===null||r===null?{ok:false,error:'INVALID_ROUND_OR_ROOM'}:{ok:true,round_no:n,room:r,four_room_mode:r==='전체방'}}
/** Integration: '0000' is INPUT PLACEHOLDER, not a valid round.
 * Populate saved rounds from EXISTING read-only app API; preserve existing home menu handlers,
 * Android exit confirmation, engine, DB, signing, package and update manifest.
 * Approved visual is the gold/blue sea screenshot with row 이전회차 | 0000 | 다음회차.
 */