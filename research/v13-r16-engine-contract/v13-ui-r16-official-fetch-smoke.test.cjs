'use strict';
const test=require('node:test'),assert=require('node:assert/strict');
const vm=require('node:vm'),fs=require('node:fs'),path=require('node:path');
const html=fs.readFileSync(path.join(__dirname,'v13-ui-r16-official-fetch-candidate.html'),'utf8');
const api='https://www.dhlottery.co.kr/lt645/selectPstLt645InfoNew.do?srchDir=center&srchLtEpsd=';
const original='https://www.dhlottery.co.kr/lt645/selectPstLt645Info.do?srchLtEpsd=';
const occurrences=(str,term)=>str.split(term).length-1;
const grab=(start,end)=>{const a=html.indexOf(start),b=html.indexOf(end,a);assert.ok(a>=0&&b>a,'source markers not found: '+start);return html.slice(a,b)};
test('candidate remains self-contained HTML with original schema',()=>{assert.match(html,/<!doctype html>/i);assert.ok(html.includes("function normalizedAutoDraw("));assert.ok(html.includes("function scoreAfterDraw("));});
test('official URL is round-specific 2026 endpoint',()=>{assert.equal(occurrences(html,api),1);assert.equal(occurrences(html,original),0)});
test('mirror unverified cannot score',()=>{assert.equal(occurrences(html,"if(record.verifiedOfficial!==true||record.source!=='DHLOTTERY_2026')return"),1)});
test('no manual winning-number input UI',()=>{assert.equal(occurrences(html,'data-win-manual'),0)});
test('all inline JavaScript parses in Node VM',()=>{
 const re=/<script([^>]*)>([\s\S]*?)<\/script>/gi;let m,count=0;
 while((m=re.exec(html))!==null){if(/\bsrc\s*=|\btype\s*=\s*["']application\/json/i.test(m[1]))continue;new vm.Script(m[2],{filename:'v13-inline-script-'+(++count)});}
 assert.ok(count>=1);
});
const normalized=vm.runInNewContext(grab('function normalizedAutoDraw(', 'function persistAutoDraw(')+'\nnormalizedAutoDraw', {
 validSixNumbers:(x)=>Array.isArray(x)&&x.length===6&&new Set(x).size===6&&x.every(n=>Number.isInteger(n)&&n>=1&&n<=45),
 drawExpectedDate:(r)=>new Date(Date.UTC(2002,11,7)+(r-1)*604800000).toISOString().slice(0,10),
 Date
});
const row=(r,nums=[16,18,27,36,38,42],bonus=22)=>Object.fromEntries([['ltEpsd',r],...nums.map((n,i)=>['tm'+(i+1)+'WnNo',n]),['bnsWnNo',bonus]]);
const wrap=(...rs)=>({data:{list:rs}});
test('official payload selects requested round from multi-round response',()=>{const actual=normalized(1248,wrap(row(1247),row(1248),row(1249)),'DHLOTTERY_2026');assert.equal(actual.round,1248);assert.equal(actual.winning.join(','),'16,18,27,36,38,42');assert.equal(actual.verifiedOfficial,true)});
test('latest-only result for another round rejected',()=>assert.throws(()=>normalized(1247,wrap(row(1248)),'DHLOTTERY_2026'),/회차 불일치/));
test('invalid duplicate six-number result rejected',()=>assert.throws(()=>normalized(1248,wrap(row(1248,[1,1,2,3,4,5],6)),'DHLOTTERY_2026'),/형식 검사/));
test('public mirror remains unverified even for valid numbers',()=>{const v=normalized(1248,{round:1248,date:'2026-10-31',numbers:[16,18,27,36,38,42],bonus:22},'PUBLIC_MIRROR_UNVERIFIED');assert.equal(v.verifiedOfficial,false)});
const score=vm.runInNewContext(grab('function scoreAfterDraw(', 'const scorePNGCache=new Map()')+'\nscoreAfterDraw',{
 afterDrawResults:{1248:{round:1248,winning:[16,18,27,36,38,42],bonus:22,verifiedOfficial:false,source:'PUBLIC_MIRROR_UNVERIFIED'}},
 collectPredraw:()=>({ready:true,combinations:Array.from({length:30},()=>({room:'captain',values:[16,18,27,36,38,42]}))}),
 resultGrade:(nums,wins)=>({matches:nums.filter(n=>wins.includes(n)).length,grade:1})
});
test('unverified mirror cannot trigger score png or win event',()=>{const v=score(1248);assert.equal(v.ready,false);assert.match(v.reason,/공식 당첨번호 직접 확인 전/)});
