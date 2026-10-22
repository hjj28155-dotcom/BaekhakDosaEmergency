const assert=require('node:assert/strict'), fs=require('fs'),vm=require('node:vm');
const src=fs.readFileSync('/mnt/data/emergency_app_audit/raw_bank.js','utf8');
const patch=fs.readFileSync('/mnt/data/emergency_app_audit/db1_1245_1247_patch.js','utf8');
for(const mode of ['base','bad','good']) {
  const context={window:{}};context.window.window=context.window;
  vm.runInNewContext(src,context,{timeout:5000});
  if(mode==='bad')context.window.V3_RAW_BANK['1245']={text:'다른 원문'};
  const old=context.window.V3_RAW_BANK['1'].text;
  vm.runInNewContext(patch,context,{timeout:5000});
  const status=context.window.DB1_UPDATE_1247;
  if(mode==='bad'){
    assert.equal(status.status,'HOLD');assert.ok(status.conflicts.includes(1245));
  }else {
    assert.equal(status.status,'PASS',JSON.stringify(status));
    assert.equal(Object.keys(context.window.V3_RAW_BANK).length,1247);
    assert.equal(context.window.V3_RAW_BANK_META.count,1247);
    assert.equal(context.window.V3_RAW_BANK['1'].text,old);
    assert.equal(status.drawNumbersLoaded,false);
  }
  console.log(mode+': '+JSON.stringify(status));
}
console.log('PASS: 1..1247 original-6-line bank additive update + collision detection')