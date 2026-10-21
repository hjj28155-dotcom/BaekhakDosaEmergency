/* Emergency app DB-1 RAW-only patch (1..1247): never contains winning numbers.
   Load after raw_bank.js and before existing v3_round1247.js.
   Does NOT modify official release, score records, or any prior raw entry. */
(function(root){
"use strict";
const patch={"1246": {"text": "첫번째.대장장이의 망치가 녹이 슬었을때 알수가 있는 징표로구나.\n두번째. 개구리는 우물밖으로 나와 검은하늘을 바라보리로다.\n세번째. 흰 바둑알과 검은 바둑알의 위치가 바뀌며 승리의 뒤에 숨는구나.\n네번째. 종이학 천마리중 한마리만이 불에 타는구나.\n다섯째. 나룻배는 안개속에 흘러가며 종소리 나는 쪽으로 항하여 도착하리로다.\n여섯째. 돌위에 더 큰돌을 얹어서 탑을 만드리니 소원이 이루어질것이니로다.", "gi": "", "source": "사용자 제공 제1246회 원문 전사 (v6/data)"}, "1247": {"text": "첫번째. 눈사람의 심장을 찻을지로다.\n두번째. 수탉이 울며 앞서던 인간은 뒤를 돌아 볼지로다.\n세번째. 감꼭지는 땅으로 떨어져 까치의 먹이가 되리로다.\n네번째. 지나간 두더지는 해를 볼수가 없으며 멈춘 두더지는 해를 볼수가 있구나.\n다섯째. 장기판의 졸은 끝에서 멈추며 차를 빼앗아 올지로다.\n여섯째. 늙은 재봉사는 인간의 세월을 꿰매는구나.", "gi": "", "source": "사용자 제공 2026-10-21 제1247회 원문 캡처 전사 (v3-source)"}};
let bank=root.V3_RAW_BANK;
if(!bank||typeof bank!=="object"){root.DB1_PATCH_STATUS={status:"HOLD",reason:"BASE_RAW_BANK_MISSING"};return;}
let collisions=[], inserted=[];
for(const [round,entry] of Object.entries(patch)){
 if(bank[round] && String(bank[round].text||"").trim()){
  if(String(bank[round].text||"").trim()!==entry.text.trim()){collisions.push(round);}
  continue;
 }
 bank[round]={text:entry.text,gi:"",source:entry.source};
 inserted.push(round);
}
const absent=[];
for(let n=1;n<=1247;n++){
 const e=bank[String(n)],raw=String(e&&e.text||"");
 if(!e || !raw.trim() || raw.split(/\n+/).filter(Boolean).length!==6)absent.push(n);
}
const meta=root.V3_RAW_BANK_META||(root.V3_RAW_BANK_META={});
if(!absent.length&&!collisions.length){
 meta.first=1; meta.last=1247;meta.count=1247;
}
root.DB1_PATCH_STATUS={status:!absent.length&&!collisions.length?"PASS":"HOLD",
 version:"DB1_RAW_1-1247_20261022",inserted,collisions,missing:absent.slice(0,20),
 totalMissing:absent.length,resultDataIncluded:false};
})(typeof window!=="undefined"?window:globalThis);