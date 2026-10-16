/* 항행의자유 V6: 기본 6개 방 · 방제 변경 · 새 방 추가.
   방 설정은 현재 기기에 저장합니다. 서버 수신함/ChatGPT/Drive 자동 연결은 별도 구현 대상입니다. */
(function(G){
"use strict";
var STORAGE="hanghae_v6_rooms_v1";
var DEFAULT=[
 {id:"all",name:"전체방",kind:"broadcast",fixed:true},
 {id:"analysis",name:"항해사방",kind:"individual",fixed:true},
 {id:"controller",name:"통제사방",kind:"individual",fixed:true},
 {id:"helmsman",name:"조타수방",kind:"individual",fixed:true},
 {id:"freedom",name:"항행의자유방",kind:"individual",fixed:true},
 {id:"chief",name:"기관장방",kind:"individual",fixed:true}
];
var core=DEFAULT.map(function(x){return x.id}),inMemory=null;
function clone(x){return JSON.parse(JSON.stringify(x))}
function title(s){
 s=String(s==null?"":s).trim().replace(/\s+/g," ");
 if(!s||s.length>32||/[<>\u0000-\u001f]/.test(s))throw Error("방제는 1~32자, < > 또는 제어문자 불가");
 return s;
}
function validChat(url){
 if(!url)return false;
 try{
  var u=new G.URL(url);
  return u.protocol==="https:"&&/^(chatgpt\.com|chat\.openai\.com)$/i.test(u.hostname)
   &&!u.username&&!u.password&&/^\/c\/[a-z0-9-]+\/?$/i.test(u.pathname);
 }catch(e){return false}
}
function normalize(raw){
 var rows=Array.isArray(raw)?raw:[],byId={};
 rows.forEach(function(x){if(x&&typeof x.id==="string"&&!byId[x.id])byId[x.id]=x});
 var result=DEFAULT.map(function(d){var r=clone(d);if(byId[d.id])try{r.name=title(byId[d.id].name)}catch(e){}return r});
 var seen={};
 rows.forEach(function(x){
  if(!x||result.length>=100)return;
  var id=String(x.id||"");
  if(!/^extra_[a-z0-9_-]{8,54}$/.test(id)||core.includes(id)||seen[id])return;
  seen[id]=true;
  var name="새 방";try{name=title(x.name)}catch(e){}
  var link=String(x.originalChatUrl||"");
  result.push({id:id,name:name,kind:"individual",fixed:false,originalChatUrl:validChat(link)?link:""});
 });
 return result;
}
function list(){
 var raw=null;
 try{raw=G.localStorage?G.localStorage.getItem(STORAGE):null}catch(e){}
 if(raw===null&&inMemory)return clone(inMemory);
 try{return normalize(raw?JSON.parse(raw):null)}catch(e){return normalize(null)}
}
function save(rows){
 inMemory=normalize(rows);
 var persisted=false;
 try{if(G.localStorage){G.localStorage.setItem(STORAGE,JSON.stringify(inMemory));persisted=true}}catch(e){}
 return {rooms:clone(inMemory),persisted:persisted};
}
function rename(id,newTitle){
 var rows=list(),r=rows.find(function(x){return x.id===id});
 if(!r)throw Error("없는 방");
 r.name=title(newTitle);return save(rows);
}
function add(newTitle){
 var rows=list();if(rows.length>=100)throw Error("방 최대 100개");
 var suffix=(G.crypto&&G.crypto.randomUUID?G.crypto.randomUUID().replace(/-/g,""):Date.now().toString(36)+Math.random().toString(36).slice(2,16));
 var id="extra_"+suffix;
 if(rows.some(function(x){return x.id===id}))throw Error("ID 중복. 다시 시도해 주세요.");
 rows.push({id:id,name:title(newTitle),kind:"individual",fixed:false,originalChatUrl:""});
 return save(rows);
}
function linkOriginal(id,url){
 var rows=list(),r=rows.find(function(x){return x.id===id});
 if(!r)throw Error("없는 방");
 if(r.fixed)throw Error("기본방은 기존 V5 원본 대화 링크 설정을 이용하세요.");
 url=String(url||"").trim();
 if(url&&!validChat(url))throw Error("원본 ChatGPT /c/ 주소가 필요합니다.");
 r.originalChatUrl=url;return save(rows);
}
function removeExtra(id){
 var rows=list(),r=rows.find(function(x){return x.id===id});
 if(!r)throw Error("없는 방");
 if(r.fixed)throw Error("기본 6개 방 삭제 불가");
 return save(rows.filter(function(x){return x.id!==id}));
}
G.V6RoomStore=Object.freeze({
 defaults:function(){return clone(DEFAULT)},list:list,rename:rename,add:add,linkOriginal:linkOriginal,removeExtra:removeExtra
});
})(typeof window!=="undefined"?window:globalThis);
