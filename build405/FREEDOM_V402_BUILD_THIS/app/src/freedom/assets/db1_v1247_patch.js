/* 항행의자유 비상구매앱 DB-1 1245-1247 RAW ONLY 연구패치.
   기존 1~1244 원문, 사용자 결과, 구매조합, 당첨번호 자료를 절대 변경하지 않음.
   1~1247 6문장 1247건 검사 성공할 때만 새 메타데이터 노출.
   출처: 사용자 제공 DB-1_ENGINE_BLIND_1-1245.xlsx + 사용자 제공 1246/1247 원문 전사.
   정답·보너스번호는 내장하지 않음. */
(function(root){
  'use strict';
  var additions={"1245":{"text":"첫번째. 낙타는 바늘구멍을 지나 사막의 문앞에 서리로다.\n두번째. 이상한 시계는 二 가 사라지며 가장 높은곳에설때 의미가 나타나는구나.\n세번째. 씨름판 모래위에 바람이 지나가니 승자의 이름이 나타나리로다.\n네번째. 등대지기는 육지에 존재하는 자신의 불을 끄지 못하는구나.\n다섯째. 스님이 키우는 고양이는 아홉이요 쥐는 한마리로구나.\n여섯째. 매화는 눈속에서 피어나며 그꽃을 바라본 자의 발자국이 남겨지리로다.","gi":"","source":"DB-1_ENGINE_BLIND_1-1245.xlsx 원문 보존"},"1246":{"text":"첫번째.대장장이의 망치가 녹이 슬었을때 알수가 있는 징표로구나.\n두번째. 개구리는 우물밖으로 나와 검은하늘을 바라보리로다.\n세번째. 흰 바둑알과 검은 바둑알의 위치가 바뀌며 승리의 뒤에 숨는구나.\n네번째. 종이학 천마리중 한마리만이 불에 타는구나.\n다섯째. 나룻배는 안개속에 흘러가며 종소리 나는 쪽으로 항하여 도착하리로다.\n여섯째. 돌위에 더 큰돌을 얹어서 탑을 만드리니 소원이 이루어질것이니로다.","gi":"","source":"사용자 제공 제1246회 원문 전사 (v6/data)"},"1247":{"text":"첫번째. 눈사람의 심장을 찻을지로다.\n두번째. 수탉이 울며 앞서던 인간은 뒤를 돌아 볼지로다.\n세번째. 감꼭지는 땅으로 떨어져 까치의 먹이가 되리로다.\n네번째. 지나간 두더지는 해를 볼수가 없으며 멈춘 두더지는 해를 볼수가 있구나.\n다섯째. 장기판의 졸은 끝에서 멈추며 차를 빼앗아 올지로다.\n여섯째. 늙은 재봉사는 인간의 세월을 꿰매는구나.","gi":"","source":"사용자 제공 2026-10-21 제1247회 원문 캡처 전사 (v3-source)"}};
  var bank=root.V3_RAW_BANK;
  if(!bank||typeof bank!=='object') {root.DB1_UPDATE_1247={status:'HOLD',reason:'BANK_ABSENT'};return;}
  var conflicts=[],inserted=[];
  for(var n=1245;n<=1247;n++){
    var key=String(n),actual=bank[key]||{},target=additions[key];
    if(String(actual.text||'').trim()){
      if(String(actual.text).replace(/\r\n/g,'\n').trim()!==target.text.replace(/\r\n/g,'\n').trim())conflicts.push(n);
    }else{bank[key]={text:target.text,lines:target.text.split('\n'),gi:'',source:target.source};inserted.push(n)}
  }
  var invalid=[];
  for(var i=1;i<=1247;i++){
    var x=bank[String(i)],lines=String(x&&x.text||'').split(/\r?\n/).filter(function(z){return z.trim()!==''});
    if(lines.length!==6)invalid.push(i);
  }
  var pass=!invalid.length&&!conflicts.length;
  if(pass){
    var meta=root.V3_RAW_BANK_META||(root.V3_RAW_BANK_META={});
    meta.first=1;meta.last=1247;meta.count=1247;meta.updated='2026-10-22';
  }
  root.DB1_UPDATE_1247={status:pass?'PASS':'HOLD',inserted:inserted,conflicts:conflicts,invalid:invalid.slice(0,40),totalInvalid:invalid.length,last:pass?1247:null,drawNumbersLoaded:false};
})(typeof window!=='undefined'?window:globalThis);