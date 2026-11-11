import assert from 'node:assert/strict';import test from 'node:test';
import {roundNumber,formatRound,stepRound,selectSavedRound,selectRoom,presentationRequest} from './approved-home-controls.mjs';
test('placeholder',()=>{assert.equal(roundNumber('0000'),null);assert.equal(formatRound('0000'),'0000')});
test('round entry',()=>{assert.equal(roundNumber('377'),377);assert.equal(formatRound(377),'0377');assert.equal(roundNumber('1x'),null);assert.equal(roundNumber('10000'),null)});
test('step bounds',()=>{assert.equal(stepRound(1,-1),1);assert.equal(stepRound(377,-1),376);assert.equal(stepRound(377,1),378);assert.equal(stepRound(9999,1),9999);assert.equal(stepRound('0000',1),null)});
test('saved choice',()=>{assert.equal(selectSavedRound(200,[200,377]),200);assert.equal(selectSavedRound(201,[200,377]),null)});
test('rooms',()=>{assert.equal(selectRoom('전체방'),'전체방');assert.equal(selectRoom('5방'),null)});
test('presentation request only',()=>{assert.deepEqual(presentationRequest({round:377,room:'전체방'}),{ok:true,round_no:377,room:'전체방',four_room_mode:true});assert.equal(presentationRequest({round:'0000',room:'전체방'}).ok,false)});