/* V30 official-result gated score announcement adapter */
(function (root) {
  'use strict';
  function validSix(numbers) {
    return Array.isArray(numbers) && numbers.length === 6 &&
      numbers.every(n => Number.isInteger(n) && n >= 1 && n <= 45) &&
      new Set(numbers).size === 6;
  }
  function evaluate(official, entry) {
    if (!official || official.confirmed !== true || !validSix(official.numbers) ||
        !Number.isInteger(official.bonus) || official.bonus < 1 ||
        official.bonus > 45 || official.numbers.includes(official.bonus)) {
      return {status: 'BLOCKED', reason: 'OFFICIAL_DRAW_NOT_CONFIRMED'};
    }
    if (!validSix(entry)) return {status: 'INVALID', reason: 'INVALID_COMBINATION'};
    const hits = entry.filter(n => official.numbers.includes(n)).length;
    const bonusHit = entry.includes(official.bonus);
    const rank = hits === 6 ? 1 : hits === 5 && bonusHit ? 2 :
      hits === 5 ? 3 : hits === 4 ? 4 : hits === 3 ? 5 : 'loss';
    return {status: 'FINAL', hits, bonusHit, rank};
  }
  function announcement(data, official, entry) {
    const outcome = evaluate(official, entry);
    if (outcome.status !== 'FINAL') return {...outcome, speech: null, effect: 'none'};
    const message = data.messages[String(outcome.rank)];
    if (!message) throw new Error('Missing approved message for rank ' + outcome.rank);
    return {...outcome, speech: message.speech, effect: message.effect, tone: message.tone};
  }
  root.V30ScoreCore = {evaluate, announcement, validSix};
})(typeof window !== 'undefined' ? window : globalThis);