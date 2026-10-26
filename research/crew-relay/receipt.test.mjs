import test from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import vm from 'node:vm';

// Run from repository root: node --test research/crew-relay/receipt.test.mjs
// No network, Supabase token, production DB, APK or installed-app changes.
const html=readFileSync(new URL('../../crew-meeting.html',import.meta.url),'utf8');
const scriptMatch=html.match(/<script>([\s\S]*?)<\/script>/i);
assert.ok(scriptMatch,'single local script exists');
const script=scriptMatch[1];
new vm.Script(script,{filename:'crew-meeting-inline.js'});
const first=script.indexOf('function threeRoomReplyState(rows){');
const last=script.indexOf('function threeRoomRoleSummary(rows){',first);
assert.ok(first>=0 && last>first,'receipt helper is present');
const classify=vm.runInNewContext(script.slice(first,last)+';threeRoomReplyState;');
const idA='123e4567-e89b-12d3-a456-426614174000';
const idB='123e4567-e89b-12d3-a456-426614174001';
const event=(author_role,body,created_at,source_kind='original_chat_connector',id='other')=>({id,author_role,body,created_at,source_kind});
const captain=(id=idA,time='2026-10-26T02:00:00Z')=>event('captain','선장 업무 지시',time,'captain_app',id);
const reply=(role,id=idA,time='2026-10-26T02:02:00Z',source_kind='original_chat_connector')=>event(role,'회람응답:'+id+'\n업무 검토 회신',time,source_kind);
const scenarios=[
 ['no captain',[reply('navigator')],0],
 ['captain pending',[captain()],0],
 ['one claimed reply',[captain(),reply('navigator')],1],
 ['wrong captain id',[captain(),reply('controller',idB)],0],
 ['timestamp before captain',[reply('navigator',idA,'2026-10-26T01:59:00Z'),captain()],0],
 ['not an original-chat-connector event',[captain(),reply('controller',idA,'2026-10-26T02:03:00Z','captain_app')],0],
 ['same role double report',[captain(),reply('navigator'),reply('navigator')],1],
 ['all three claimed replies',[captain(),reply('navigator'),reply('controller'),reply('linkofficer')],3],
 ['new captain instruction resets receipts',[captain(),reply('navigator'),captain(idB,'2026-10-26T02:05:00Z')],0]
];
for(const [name,rows,expected] of scenarios){
 test(name,()=>{
  const result=classify(rows);
  assert.equal(result.count,expected);
  assert.equal(typeof result.roles.navigator,'boolean');
 });
}
test('no real-session attestation claim',()=>{
 assert.ok(script.includes('원본 세션 미인증'));
 assert.ok(script.includes('auto')||script.includes('자동'));
 assert.ok(script.includes('회람응답:<선장 이벤트 UUID>'));
});
