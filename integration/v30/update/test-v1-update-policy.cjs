const assert = require('node:assert/strict');
const fs = require('node:fs'); const vm = require('node:vm');
const box={URL};vm.runInNewContext(fs.readFileSync(__dirname+'/v1-update-policy.js','utf8'),box);
const decide=box.V30V1Update.decide;
const local={versionCode:30,packageName:'com.example.v30',lastSeenNoticeId:'n1'};
const release={versionCode:31,packageName:'com.example.v30',apkUrl:'https://example.org/v30.apk',apkSha256:'a'.repeat(64)};
let n=0;function t(label,fn){fn();n++;console.log('PASS',label)}
t('new version presents update',()=>assert.equal(decide(local,release).kind,'UPDATE'));
t('manual install required',()=>assert.equal(decide(local,release).requiresUserInstall,true));
t('no update without change',()=>assert.equal(decide(local,{...release,versionCode:30}).showPopup,false));
t('new notice only',()=>assert.equal(decide(local,{versionCode:30,packageName:local.packageName,noticeId:'n2',message:'새 공지'}).kind,'NOTICE'));
t('old notice hidden',()=>assert.equal(decide(local,{versionCode:30,packageName:local.packageName,noticeId:'n1'}).showPopup,false));
t('package mismatch rejected',()=>assert.equal(decide(local,{...release,packageName:'other'}).kind,'ERROR'));
t('http url rejected',()=>assert.equal(decide(local,{...release,apkUrl:'http://example.org/app.apk'}).kind,'ERROR'));
t('missing hash rejected',()=>assert.equal(decide(local,{...release,apkSha256:''}).kind,'ERROR'));
t('older release ignored',()=>assert.equal(decide(local,{...release,versionCode:29}).showPopup,false));
console.log(n,'tests passed');
