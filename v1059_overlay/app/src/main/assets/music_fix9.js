/* FIX9 shared music controls. Native storage is authoritative; WebView reload cannot clear it. */
(function () {
  'use strict';
  let timer = null, view = '', lastItems = [], visibleCount = 200;
  const h = () => window.AndroidHost || window.BaekhakNative;
  const e = value => String(value == null ? '' : value).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  function read(method) {
    const host = h();
    if (!host || typeof host[method] !== 'function') throw new Error('음악 FIX9 APK로 업데이트한 뒤 사용해 주세요.');
    return JSON.parse(String(host[method]()));
  }
  function time(ms) { let n=Math.max(0,Math.floor(Number(ms||0)/1000));return Math.floor(n/60)+':'+String(n%60).padStart(2,'0'); }
  function status(message) { const node=document.getElementById('musicActionState');if(node)node.textContent=message; }
  function common(s) {
    const volume = Number.isFinite(Number(s.volume)) ? Math.max(0, Math.min(100,Number(s.volume))) : 42;
    return `<div class="panel goldline music9-now"><h3>🎵 우리 음악 · FIX9</h3>
      <div id="music9Name" class="music9-name">${e(s.name||'저장된 음악을 선택해 주세요')}</div>
      <div id="music9Time">${time(s.position)} / ${time(s.duration)}</div>
      <div id="musicActionState" role="status" aria-live="polite"></div>
      <div class="music9-controls">
        <button class="b green" id="music9Play" onclick="musicDo('play')">▶ 재생 / 이어듣기</button>
        <button class="b" onclick="musicDo('pause')">⏸ 일시정지</button>
        <button class="b" onclick="musicDo('prev')">⏮ 이전곡</button>
        <button class="b" onclick="musicDo('next')">⏭ 다음곡</button>
        <button class="b danger" onclick="musicDo('stop')">⏹ 음악 종료</button>
      </div>
      <div class="label">음량 <span id="musicVolText">${volume}%</span></div>
      <input aria-label="음량" class="musicRange" type="range" min="0" max="100" value="${volume}" oninput="musicVolText.textContent=this.value+'%';setMusicVolume(this.value)">
      </div>`;
  }
  function style() {return `<style>
    #mc .music9-controls{display:flex;gap:6px;flex-wrap:wrap;margin:8px 0}
    #mc .music9-controls .b{flex:1 1 120px;min-height:44px;font-size:14px;padding:7px 8px}
    #mc #music9Play{flex-basis:220px;font-weight:bold;font-size:17px}
    #mc .music9-name{font-size:18px;font-weight:700;overflow-wrap:anywhere;margin:5px 0;line-height:1.25}
    #mc .music9-wide{width:100%;min-height:48px;font-size:15px;margin:5px 0;padding:7px 8px}
    #mc .music9-track{display:flex;gap:12px;align-items:center;justify-content:space-between;padding:12px 0;border-bottom:1px solid #356079}
    #mc .music9-track>span{min-width:0;overflow-wrap:anywhere}
    #mc .music9-track .b{flex:0 0 auto;min-height:48px}
    #mc #musicActionState{min-height:26px;color:#ffe5a1;overflow-wrap:anywhere}
    #mc .music9-small{color:#bdd5e2;line-height:1.35;font-size:12px}
    #mc #music9Time{margin:4px 0;font-variant-numeric:tabular-nums}
    .modal.musicOneScreen{padding:12px 4px 4px!important;overflow:hidden!important}.modal.musicOneScreen>.card{width:min(940px,calc(100vw - 8px))!important;max-width:940px!important;height:calc(100dvh - 16px)!important;margin:0 auto!important;padding:8px 12px!important;overflow:hidden!important}.modal.musicOneScreen>.card>h2{font-size:22px!important;margin:0 0 4px!important}.modal.musicOneScreen #mc{height:calc(100% - 34px)!important;overflow:auto!important}.modal.musicOneScreen .panel{margin:6px 0!important;padding:9px!important}.modal.musicOneScreen .label{margin:4px 0 2px!important}.modal.musicOneScreen .musicRange{margin:3px 0!important}@media(max-width:760px){.modal.musicOneScreen>.card{padding:6px 8px!important}.modal.musicOneScreen>.card>h2{font-size:19px!important}.modal.musicOneScreen #mc .music9-controls .b{min-height:40px!important;font-size:12px!important}.modal.musicOneScreen #mc .music9-wide{min-height:42px!important;font-size:13px!important}}

    #mc .music9-library-panel{padding:8px!important}
    #mc .music9-library-head{display:grid;grid-template-columns:minmax(0,1fr) auto;gap:8px;align-items:center;margin-bottom:5px}
    #mc .music9-folder-title{border:0;background:transparent;color:#ffe58a;font-size:18px;font-weight:900;text-align:left;padding:4px;white-space:nowrap}
    #mc .music9-mini-list{border:1px solid #376a8f;border-radius:10px;overflow:hidden;background:#041f36}
    #mc .music9-minirow{display:grid;grid-template-columns:30px 28px minmax(0,1fr) 30px 20px;gap:4px;align-items:center;min-height:34px;padding:3px 8px;border-bottom:1px solid #214964;cursor:pointer}
    #mc .music9-minirow:last-child{border-bottom:0}
    #mc .music9-minirow.current{background:#0a456a}
    #mc .music9-no{color:#d7e8f3;font-variant-numeric:tabular-nums}
    #mc .music9-bars{color:#ffd84e;font-size:14px;text-align:center}
    #mc .music9-title{overflow:hidden;text-overflow:ellipsis;white-space:nowrap;font-weight:700}
    #mc .music9-heart{font-size:20px;color:#fff;text-align:center}
    #mc .music9-minirow.current .music9-heart{color:#ff3558}
    #mc .music9-more{font-size:20px;text-align:center}
    #mc .music9-source-controls{display:grid!important;grid-template-columns:1fr 1fr!important;gap:8px!important;margin:8px 0!important}
    #mc .music9-source-controls .b{min-height:46px!important;font-size:14px!important}
    #mc .music9-tagline{text-align:center;color:#d8e8f2;font-size:12px;margin:8px 0 2px}
    #mc .fm9{border:2px solid #c23cff!important;box-shadow:0 0 16px rgba(194,60,255,.35);background:linear-gradient(180deg,#102652,#061e3a)!important}
    #mc .fm9-head{display:flex;align-items:center;justify-content:space-between;gap:8px;margin-bottom:8px}
    #mc .fm9-head strong{font-size:19px;color:#ffe58a}
    #mc .fm9-now{display:flex;align-items:center;justify-content:space-between;gap:8px;padding:9px;border:1px solid #4a85c8;border-radius:12px;background:#082c50}
    #mc .fm9-now b{display:block;font-size:14px}
    #mc .fm9-now small{color:#c9dfef}
    #mc .fm9-presets{display:grid;grid-template-columns:repeat(4,1fr);gap:5px;margin-top:8px}
    #mc .fm9-presets .b{padding:7px 3px!important;min-height:48px!important;font-size:10px!important;line-height:1.2}
    #mc .fm9-listen{width:100%;margin-top:8px!important;min-height:44px!important;font-size:14px!important}
    @media(max-width:520px){
      .modal.musicOneScreen{padding:2px!important}
      .modal.musicOneScreen>.card{height:calc(100dvh - 4px)!important;padding:3px 6px!important}
      .modal.musicOneScreen>.card>h2{font-size:16px!important;line-height:1.05!important;margin:0 0 2px!important}
      .modal.musicOneScreen #mc{height:calc(100% - 23px)!important;overflow:hidden!important}
      .modal.musicOneScreen .panel{margin:3px 0!important;padding:5px!important;border-radius:12px!important}
      #mc .music9-now h3{font-size:14px!important;margin:0 0 2px!important}
      #mc .music9-name{font-size:12px!important;line-height:1.12!important;margin:1px 0!important;max-height:28px!important;overflow:hidden!important}
      #mc #music9Time{font-size:11px!important;margin:1px 0!important}
      #mc #musicActionState{min-height:13px!important;font-size:10px!important;line-height:1.05!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important}
      #mc .music9-controls{display:grid!important;grid-template-columns:repeat(4,1fr)!important;gap:3px!important;margin:3px 0!important}
      #mc .music9-controls .b{min-height:27px!important;height:27px!important;font-size:10px!important;line-height:1!important;padding:2px 3px!important;border-radius:10px!important}
      #mc #music9Play{grid-column:span 2!important;font-size:11px!important}
      #mc .music9-controls .danger{grid-column:span 2!important}
      #mc .label{font-size:10px!important;line-height:1!important;margin:1px 0!important}
      #mc .musicRange{height:14px!important;margin:0!important}
      #mc .music9-library-panel{padding:5px!important}
      #mc .music9-library-head{gap:4px!important;margin-bottom:2px!important}
      #mc .music9-folder-title{font-size:13px!important;padding:1px!important}
      #mc .music9-library-head>.b{min-height:28px!important;height:28px!important;font-size:10px!important;padding:2px 5px!important}
      #mc .music9-mini-list{border-radius:8px!important}
      #mc .music9-minirow{grid-template-columns:23px 20px minmax(0,1fr) 23px 15px!important;gap:2px!important;min-height:23px!important;height:23px!important;padding:1px 4px!important;font-size:10px!important;line-height:1!important}
      #mc .music9-bars{font-size:9px!important}
      #mc .music9-heart,#mc .music9-more{font-size:13px!important}
      #mc .music9-source-controls{grid-template-columns:1fr 1fr!important;gap:4px!important;margin:3px 0!important}
      #mc .music9-source-controls .b{min-height:28px!important;height:28px!important;font-size:10px!important;padding:2px 4px!important}
      #mc .fm9{padding:5px!important}
      #mc .fm9-head{margin-bottom:3px!important}
      #mc .fm9-head strong{font-size:13px!important}
      #mc .fm9-head .music9-small{font-size:9px!important}
      #mc .fm9-now{padding:4px!important;border-radius:8px!important}
      #mc .fm9-now b{font-size:10px!important}
      #mc .fm9-now small{font-size:9px!important}
      #mc #fm9Freq{font-size:10px!important}
      #mc .fm9-listen{margin-top:3px!important;min-height:27px!important;height:27px!important;font-size:10px!important;padding:2px 4px!important}
      #mc .fm9-presets{grid-template-columns:repeat(4,1fr)!important;gap:3px!important;margin-top:3px!important}
      #mc .fm9-presets .b{padding:2px 1px!important;min-height:31px!important;height:31px!important;font-size:8px!important;line-height:1.05!important}
      #mc .fm9 .music9-small[style]{font-size:8px!important;line-height:1.1!important;margin-top:3px!important}
      #mc .music9-tagline{font-size:9px!important;margin:2px 0 0!important}
    }

    @media(max-width:520px){
      .modal.musicOneScreen>.card{height:calc(100dvh - 2px)!important;padding:2px 5px!important}
      .modal.musicOneScreen>.card>h2{height:24px!important;font-size:15px!important;margin:0!important;line-height:24px!important}
      .modal.musicOneScreen #mc{height:calc(100% - 24px)!important;overflow:hidden!important;display:flex!important;flex-direction:column!important}
      #mc .music9-now{flex:0 0 116px!important;height:116px!important;padding:3px 5px!important;margin:1px 0!important;overflow:hidden!important}
      #mc .music9-now h3{font-size:11px!important;line-height:12px!important;margin:0!important}
      #mc .music9-name{font-size:10px!important;line-height:12px!important;height:12px!important;margin:0!important;white-space:nowrap!important;text-overflow:ellipsis!important;overflow:hidden!important}
      #mc #music9Time{font-size:9px!important;line-height:10px!important;margin:0!important}
      #mc #musicActionState{height:10px!important;min-height:10px!important;font-size:8px!important;line-height:10px!important;margin:0!important}
      #mc .music9-now .music9-controls{display:grid!important;grid-template-columns:repeat(5,1fr)!important;gap:2px!important;margin:1px 0!important}
      #mc .music9-now .music9-controls .b,#mc #music9Play,#mc .music9-now .music9-controls .danger{grid-column:auto!important;height:24px!important;min-height:24px!important;font-size:8px!important;padding:1px!important;border-radius:8px!important}
      #mc .music9-now .label{font-size:8px!important;height:10px!important;line-height:10px!important;margin:0!important}
      #mc .music9-now .musicRange{height:9px!important;margin:0!important}
      #mc .music9-library-panel{flex:1 1 auto!important;min-height:0!important;height:auto!important;padding:3px!important;margin:1px 0!important;display:flex!important;flex-direction:column!important;overflow:hidden!important}
      #mc .music9-library-head{flex:0 0 26px!important;height:26px!important;margin:0 0 2px!important}
      #mc .music9-folder-title{font-size:11px!important;line-height:24px!important;height:24px!important;padding:0 2px!important}
      #mc .music9-library-head>.b{height:24px!important;min-height:24px!important;font-size:8px!important;padding:0 4px!important}
      #mc .music9-mini-list{flex:0 0 207px!important;height:207px!important}
      #mc .music9-minirow{height:23px!important;min-height:23px!important;padding:0 4px!important;font-size:9px!important;line-height:23px!important}
      #mc .music9-source-controls{flex:0 0 25px!important;height:25px!important;display:grid!important;grid-template-columns:1fr 1fr!important;gap:3px!important;margin:2px 0!important}
      #mc .music9-source-controls .b{height:25px!important;min-height:25px!important;font-size:8px!important;padding:0!important}
      #mc .fm9{flex:0 0 154px!important;height:154px!important;padding:3px 5px!important;margin:1px 0!important;overflow:hidden!important}
      #mc .fm9-head{height:20px!important;margin:0 0 2px!important}
      #mc .fm9-head strong{font-size:11px!important}
      #mc .fm9-head .music9-small{font-size:8px!important}
      #mc .fm9-now{height:30px!important;min-height:30px!important;padding:2px 4px!important}
      #mc .fm9-now b{font-size:9px!important;line-height:11px!important}
      #mc .fm9-now small,#mc #fm9Freq{font-size:8px!important}
      #mc .fm9-listen{height:24px!important;min-height:24px!important;margin:2px 0!important;font-size:8px!important;padding:0!important}
      #mc .fm9-presets{height:32px!important;grid-template-columns:repeat(4,1fr)!important;gap:2px!important;margin:1px 0!important}
      #mc .fm9-presets .b{height:32px!important;min-height:32px!important;font-size:7px!important;line-height:1.05!important;padding:1px!important}
      #mc #fm9Status{height:10px!important;line-height:10px!important;font-size:7px!important;margin:1px 0 0!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important}
      #mc .music9-tagline{display:none!important}
    }

    /* V3.0.89 PHONE ONE-SCREEN HARD LOCK */
    @media(max-width:520px){
      .modal.musicOneScreen{padding:0!important}
      .modal.musicOneScreen>.card{height:100dvh!important;max-height:100dvh!important;padding:1px 4px!important;margin:0!important;border-radius:0!important;overflow:hidden!important}
      .modal.musicOneScreen>.card>h2{height:20px!important;line-height:20px!important;font-size:13px!important;margin:0!important}
      .modal.musicOneScreen #mc{height:calc(100dvh - 20px)!important;overflow:hidden!important;display:flex!important;flex-direction:column!important;gap:0!important}
      #mc .panel{margin:1px 0!important;border-radius:9px!important}
      #mc .music9-now{flex:0 0 78px!important;height:78px!important;padding:2px 4px!important;overflow:hidden!important}
      #mc .music9-now h3{font-size:10px!important;line-height:11px!important;height:11px!important;margin:0!important}
      #mc .music9-name{font-size:9px!important;line-height:10px!important;height:10px!important;margin:0!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important}
      #mc #music9Time,#mc #musicActionState{font-size:7px!important;line-height:8px!important;height:8px!important;min-height:8px!important;margin:0!important}
      #mc .music9-now .music9-controls{grid-template-columns:repeat(5,1fr)!important;gap:1px!important;margin:1px 0!important}
      #mc .music9-now .music9-controls .b,#mc #music9Play,#mc .music9-now .music9-controls .danger{height:19px!important;min-height:19px!important;font-size:6.5px!important;padding:0!important;border-radius:6px!important}
      #mc .music9-now .label{font-size:7px!important;height:8px!important;line-height:8px!important;margin:0!important}
      #mc .music9-now .musicRange{height:7px!important;margin:0!important}
      #mc .music9-library-panel{flex:1 1 auto!important;min-height:0!important;padding:2px!important;margin:1px 0!important;display:flex!important;flex-direction:column!important;overflow:hidden!important}
      #mc .music9-library-head{flex:0 0 22px!important;height:22px!important;margin:0 0 1px!important;gap:2px!important}
      #mc .music9-folder-title{font-size:9px!important;line-height:20px!important;height:20px!important;padding:0 1px!important}
      #mc .music9-library-head>.b{height:20px!important;min-height:20px!important;font-size:7px!important;padding:0 2px!important}
      #mc .music9-mini-list{flex:0 0 171px!important;height:171px!important;border-radius:6px!important}
      #mc .music9-minirow{grid-template-columns:19px 15px minmax(0,1fr) 18px 12px!important;gap:1px!important;height:19px!important;min-height:19px!important;padding:0 3px!important;font-size:8px!important;line-height:19px!important}
      #mc .music9-bars{font-size:7px!important}.modal.musicOneScreen #mc .music9-heart,#mc .music9-more{font-size:10px!important}
      #mc .music9-source-controls{flex:0 0 21px!important;height:21px!important;gap:2px!important;margin:1px 0!important}
      #mc .music9-source-controls .b{height:21px!important;min-height:21px!important;font-size:7px!important;padding:0!important}
      #mc .fm9{flex:0 0 120px!important;height:120px!important;padding:2px 4px!important;margin:1px 0!important;overflow:hidden!important}
      #mc .fm9-head{height:16px!important;margin:0 0 1px!important}
      #mc .fm9-head strong{font-size:9px!important;line-height:16px!important}
      #mc .fm9-head .music9-small{font-size:6.5px!important}
      #mc .fm9-now{height:24px!important;min-height:24px!important;padding:1px 3px!important;border-radius:6px!important}
      #mc .fm9-now b{font-size:8px!important;line-height:9px!important}
      #mc .fm9-now small,#mc #fm9Freq{font-size:6.5px!important;line-height:8px!important}
      #mc .fm9-listen{height:20px!important;min-height:20px!important;margin:1px 0!important;font-size:7px!important;padding:0!important}
      #mc .fm9-presets{height:26px!important;grid-template-columns:repeat(4,1fr)!important;gap:1px!important;margin:1px 0!important}
      #mc .fm9-presets .b{height:26px!important;min-height:26px!important;font-size:6px!important;line-height:1!important;padding:0!important}
      #mc #fm9Status{height:8px!important;line-height:8px!important;font-size:6px!important;margin:0!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important}
      #mc .music9-tagline{display:none!important}
    }


    /* V3.0.90 FINAL TABLET/FOLD ONE-SCREEN LAYOUT — fill the screen without blank space */
    @media (min-width:521px) and (max-width:900px){
      .modal.musicOneScreen{padding:0!important;overflow:hidden!important}
      .modal.musicOneScreen>.card{width:100vw!important;max-width:none!important;height:100dvh!important;max-height:100dvh!important;margin:0!important;padding:3px 8px!important;border-radius:0!important;overflow:hidden!important}
      .modal.musicOneScreen>.card>h2{height:38px!important;line-height:38px!important;font-size:24px!important;margin:0!important}
      .modal.musicOneScreen #mc{height:calc(100dvh - 38px)!important;overflow:hidden!important;display:flex!important;flex-direction:column!important;gap:3px!important}
      #mc .panel{margin:0!important;border-radius:12px!important}
      #mc .music9-now{flex:0 0 188px!important;height:188px!important;padding:7px 9px!important;overflow:hidden!important}
      #mc .music9-now h3{font-size:19px!important;line-height:22px!important;margin:0 0 2px!important}
      #mc .music9-name{font-size:16px!important;line-height:20px!important;height:20px!important;margin:1px 0!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important}
      #mc #music9Time{font-size:14px!important;line-height:17px!important;margin:0!important}
      #mc #musicActionState{height:18px!important;min-height:18px!important;font-size:13px!important;line-height:18px!important;margin:0!important}
      #mc .music9-now .music9-controls{display:grid!important;grid-template-columns:repeat(5,1fr)!important;gap:5px!important;margin:5px 0 0!important}
      #mc .music9-now .music9-controls .b,#mc #music9Play,#mc .music9-now .music9-controls .danger{grid-column:auto!important;height:50px!important;min-height:50px!important;font-size:16px!important;padding:3px!important;border-radius:10px!important}
      #mc .music9-now .label,#mc .music9-now .musicRange{display:none!important}

      #mc .music9-library-panel{flex:1 1 auto!important;min-height:0!important;padding:6px!important;margin:0!important;display:flex!important;flex-direction:column!important;overflow:hidden!important}
      #mc .music9-library-head{flex:0 0 46px!important;height:46px!important;margin:0 0 4px!important;gap:6px!important}
      #mc .music9-folder-title{font-size:18px!important;line-height:42px!important;height:42px!important;padding:0 4px!important}
      #mc .music9-library-head>.b{height:42px!important;min-height:42px!important;font-size:15px!important;padding:2px 8px!important}
      #mc .music9-mini-list{flex:0 0 405px!important;height:405px!important;border-radius:8px!important;display:grid!important;grid-template-rows:repeat(9,1fr)!important}
      #mc .music9-minirow{grid-template-columns:34px 28px minmax(0,1fr) 34px 22px!important;gap:3px!important;height:auto!important;min-height:0!important;padding:0 7px!important;font-size:15px!important;line-height:1.1!important}
      #mc .music9-bars{font-size:12px!important}
      #mc .music9-heart,#mc .music9-more{font-size:20px!important}
      #mc .music9-source-controls{flex:0 0 62px!important;height:62px!important;gap:6px!important;margin:5px 0!important}
      #mc .music9-source-controls .b{height:62px!important;min-height:62px!important;font-size:18px!important;padding:2px 4px!important}

      #mc .fm9{flex:1 1 auto!important;min-height:300px!important;padding:7px 9px!important;margin:0!important;overflow:hidden!important;display:flex!important;flex-direction:column!important}
      #mc .fm9-head{flex:0 0 34px!important;height:34px!important;margin:0 0 4px!important}
      #mc .fm9-head strong{font-size:21px!important;line-height:34px!important}
      #mc .fm9-head .music9-small{font-size:13px!important}
      #mc .fm9-now{flex:0 0 62px!important;height:62px!important;padding:6px 8px!important;border-radius:9px!important}
      #mc .fm9-now b{font-size:17px!important;line-height:20px!important}
      #mc .fm9-now small,#mc #fm9Freq{font-size:14px!important}
      #mc .fm9-listen{flex:0 0 54px!important;height:54px!important;min-height:54px!important;margin:5px 0!important;font-size:17px!important;padding:2px 4px!important}
      #mc .fm9-presets{flex:0 0 62px!important;height:62px!important;grid-template-columns:repeat(4,1fr)!important;gap:5px!important;margin:0!important}
      #mc .fm9-presets .b{height:62px!important;min-height:62px!important;font-size:13px!important;line-height:1.15!important;padding:2px!important}
      #mc #fm9RegionNote{font-size:12px!important;line-height:16px!important;margin:5px 0 0!important;white-space:normal!important}
      #mc #fm9Status{font-size:12px!important;line-height:16px!important;margin:2px 0 0!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important}
      #mc .music9-tagline{flex:0 0 34px!important;height:34px!important;font-size:13px!important;line-height:34px!important;margin:2px 0 0!important}
    }


    /* V3.0.93 FINAL PHONE FULL-HEIGHT — remove all blank space */
    @media(max-width:520px){
      .modal.musicOneScreen #mc .music9-library-panel{flex:1 1 auto!important;min-height:0!important;height:auto!important;display:flex!important;flex-direction:column!important;overflow:hidden!important}
      .modal.musicOneScreen #mc .fm9{flex:1 1 auto!important;height:auto!important;min-height:145px!important;display:flex!important;flex-direction:column!important;justify-content:flex-start!important;padding:5px 7px!important;margin:2px 0!important}
      .modal.musicOneScreen #mc .fm9-head{flex:0 0 22px!important;height:22px!important;margin:0 0 3px!important}
      .modal.musicOneScreen #mc .fm9-head strong{font-size:12px!important;line-height:22px!important}
      .modal.musicOneScreen #mc .fm9-head .music9-small{font-size:8px!important}
      .modal.musicOneScreen #mc .fm9-now{flex:0 0 36px!important;height:36px!important;min-height:36px!important;padding:3px 5px!important}
      .modal.musicOneScreen #mc .fm9-now b{font-size:10px!important;line-height:12px!important}
      .modal.musicOneScreen #mc .fm9-now small,.modal.musicOneScreen #mc #fm9Freq{font-size:8px!important;line-height:10px!important}
      .modal.musicOneScreen #mc .fm9-listen{flex:0 0 30px!important;height:30px!important;min-height:30px!important;margin:4px 0!important;font-size:9px!important;padding:0 3px!important}
      .modal.musicOneScreen #mc .fm9-presets{flex:0 0 42px!important;height:42px!important;grid-template-columns:repeat(4,1fr)!important;gap:3px!important;margin:0!important}
      .modal.musicOneScreen #mc .fm9-presets .b{height:42px!important;min-height:42px!important;font-size:7.5px!important;line-height:1.05!important;padding:1px!important}
      .modal.musicOneScreen #mc #fm9RegionNote{flex:0 0 auto!important;font-size:8px!important;line-height:10px!important;margin:4px 0 0!important;white-space:normal!important}
      .modal.musicOneScreen #mc #fm9Status{flex:0 0 auto!important;height:auto!important;min-height:10px!important;line-height:10px!important;font-size:7.5px!important;margin:2px 0 0!important;white-space:normal!important;overflow:visible!important}
      .modal.musicOneScreen #mc .music9-tagline{display:flex!important;flex:0 0 38px!important;height:38px!important;align-items:center!important;justify-content:center!important;margin:2px 0 0!important;border-radius:9px!important;background:linear-gradient(180deg,#08355a,#052640)!important;color:#ffe58a!important;font-size:10px!important;line-height:1.2!important;text-align:center!important}
    }


    /* V3.0.93 TABLET/FOLD FULL-HEIGHT */
    @media (min-width:521px) and (max-width:900px){
      .modal.musicOneScreen{padding:0!important;overflow:hidden!important}
      .modal.musicOneScreen>.card{width:100vw!important;max-width:none!important;height:100dvh!important;max-height:100dvh!important;margin:0!important;padding:3px 8px!important;border-radius:0!important;overflow:hidden!important}
      .modal.musicOneScreen>.card>h2{height:38px!important;line-height:38px!important;font-size:24px!important;margin:0!important}
      .modal.musicOneScreen #mc{height:calc(100dvh - 38px)!important;overflow:hidden!important;display:flex!important;flex-direction:column!important;gap:3px!important}
      .modal.musicOneScreen #mc .panel{margin:0!important;border-radius:12px!important}
      .modal.musicOneScreen #mc .music9-now{flex:0 0 188px!important;height:188px!important;padding:7px 9px!important;overflow:hidden!important}
      .modal.musicOneScreen #mc .music9-now h3{font-size:19px!important;line-height:22px!important;margin:0 0 2px!important}
      .modal.musicOneScreen #mc .music9-name{font-size:16px!important;line-height:20px!important;height:20px!important;margin:1px 0!important;white-space:nowrap!important;overflow:hidden!important;text-overflow:ellipsis!important}
      .modal.musicOneScreen #mc #music9Time{font-size:14px!important;line-height:17px!important;margin:0!important}
      .modal.musicOneScreen #mc #musicActionState{height:18px!important;min-height:18px!important;font-size:13px!important;line-height:18px!important;margin:0!important}
      .modal.musicOneScreen #mc .music9-now .music9-controls{display:grid!important;grid-template-columns:repeat(5,1fr)!important;gap:5px!important;margin:5px 0 0!important}
      .modal.musicOneScreen #mc .music9-now .music9-controls .b,.modal.musicOneScreen #mc #music9Play,.modal.musicOneScreen #mc .music9-now .music9-controls .danger{grid-column:auto!important;height:50px!important;min-height:50px!important;font-size:16px!important;padding:3px!important;border-radius:10px!important}
      .modal.musicOneScreen #mc .music9-now .label,.modal.musicOneScreen #mc .music9-now .musicRange{display:none!important}
      .modal.musicOneScreen #mc .music9-library-panel{flex:1 1 auto!important;min-height:0!important;padding:6px!important;display:flex!important;flex-direction:column!important;overflow:hidden!important}
      .modal.musicOneScreen #mc .music9-library-head{flex:0 0 46px!important;height:46px!important;margin:0 0 4px!important;gap:6px!important}
      .modal.musicOneScreen #mc .music9-folder-title{font-size:18px!important;line-height:42px!important;height:42px!important;padding:0 4px!important}
      .modal.musicOneScreen #mc .music9-library-head>.b{height:42px!important;min-height:42px!important;font-size:15px!important;padding:2px 8px!important}
      .modal.musicOneScreen #mc .music9-mini-list{flex:0 0 405px!important;height:405px!important;border-radius:8px!important;display:grid!important;grid-template-rows:repeat(9,1fr)!important}
      .modal.musicOneScreen #mc .music9-minirow{grid-template-columns:34px 28px minmax(0,1fr) 34px 22px!important;gap:3px!important;height:auto!important;min-height:0!important;padding:0 7px!important;font-size:15px!important;line-height:1.1!important}
      .modal.musicOneScreen #mc .music9-source-controls{flex:0 0 62px!important;height:62px!important;gap:6px!important;margin:5px 0!important}
      .modal.musicOneScreen #mc .music9-source-controls .b{height:62px!important;min-height:62px!important;font-size:18px!important;padding:2px 4px!important}
      .modal.musicOneScreen #mc .fm9{flex:1 1 auto!important;height:auto!important;min-height:300px!important;padding:7px 9px!important;display:flex!important;flex-direction:column!important;overflow:hidden!important}
      .modal.musicOneScreen #mc .fm9-head{flex:0 0 34px!important;height:34px!important;margin:0 0 4px!important}
      .modal.musicOneScreen #mc .fm9-head strong{font-size:21px!important;line-height:34px!important}
      .modal.musicOneScreen #mc .fm9-now{flex:0 0 62px!important;height:62px!important;padding:6px 8px!important;border-radius:9px!important}
      .modal.musicOneScreen #mc .fm9-listen{flex:0 0 54px!important;height:54px!important;min-height:54px!important;margin:5px 0!important;font-size:17px!important}
      .modal.musicOneScreen #mc .fm9-presets{flex:0 0 62px!important;height:62px!important;grid-template-columns:repeat(4,1fr)!important;gap:5px!important;margin:0!important}
      .modal.musicOneScreen #mc .fm9-presets .b{height:62px!important;min-height:62px!important;font-size:13px!important;line-height:1.15!important;padding:2px!important}
      .modal.musicOneScreen #mc #fm9RegionNote{font-size:12px!important;line-height:16px!important;margin:5px 0 0!important;white-space:normal!important}
      .modal.musicOneScreen #mc #fm9Status{font-size:12px!important;line-height:16px!important;margin:2px 0 0!important;white-space:normal!important}
      .modal.musicOneScreen #mc .music9-tagline{display:flex!important;flex:0 0 34px!important;height:34px!important;align-items:center!important;justify-content:center!important;font-size:13px!important;line-height:34px!important;margin:2px 0 0!important}
    }


    /* V3.0.94 FINAL COMPACT RADIO + FULL VISUAL FOOTER */
    @media(max-width:520px){
      .modal.musicOneScreen #mc .fm9{flex:0 0 145px!important;height:145px!important;min-height:145px!important;max-height:145px!important;padding:5px 7px!important;margin:2px 0!important;overflow:hidden!important}
      .modal.musicOneScreen #mc .music9-tagline{display:flex!important;flex:1 1 auto!important;height:auto!important;min-height:76px!important;margin:2px 0 0!important;border-radius:9px!important;align-items:center!important;justify-content:center!important;position:relative!important;overflow:hidden!important;background:
        radial-gradient(circle at 50% 100%,rgba(255,194,70,.42),transparent 34%),
        linear-gradient(180deg,#07395f 0%,#062b49 45%,#031c32 100%)!important;color:#ffe69a!important;font-size:10px!important;line-height:1.35!important;text-align:center!important;padding:26px 10px 8px!important}
      .modal.musicOneScreen #mc .music9-tagline:before{content:'⚓  ♪  ⚓';position:absolute!important;top:7px!important;left:0!important;right:0!important;text-align:center!important;font-size:18px!important;line-height:20px!important;color:#ffd36d!important}
    }

    @media (min-width:521px) and (max-width:900px){
      .modal.musicOneScreen #mc .music9-library-panel{flex:1 1 auto!important;min-height:0!important;height:auto!important;overflow:hidden!important}
      .modal.musicOneScreen #mc .fm9{flex:0 0 300px!important;height:300px!important;min-height:300px!important;max-height:300px!important;padding:7px 9px!important;margin:0!important;overflow:hidden!important}
      .modal.musicOneScreen #mc .music9-tagline{display:flex!important;flex:1 1 auto!important;height:auto!important;min-height:110px!important;margin:3px 0 0!important;border-radius:12px!important;align-items:center!important;justify-content:center!important;position:relative!important;overflow:hidden!important;background:
        radial-gradient(circle at 50% 105%,rgba(255,194,70,.55),transparent 30%),
        linear-gradient(180deg,#0a416c 0%,#073454 44%,#041d31 100%)!important;color:#ffe69a!important;font-size:17px!important;line-height:1.35!important;text-align:center!important;padding:58px 16px 16px!important;box-shadow:inset 0 0 0 1px rgba(255,215,110,.18)!important}
      .modal.musicOneScreen #mc .music9-tagline:before{content:'⚓   ♪   ⚓';position:absolute!important;top:17px!important;left:0!important;right:0!important;text-align:center!important;font-size:34px!important;line-height:38px!important;color:#ffd36d!important;text-shadow:0 1px 8px rgba(0,0,0,.35)!important}
    }

    </style>`;}
  function tick() {
    if(!document.getElementById('music9Name') || document.getElementById('modal')?.style.display==='none') {
      clearInterval(timer);timer=null;return;
    }
    try {
      const s=read('getMusicState');
      document.getElementById('music9Name').textContent=s.name||'저장된 음악을 선택해 주세요';
      document.getElementById('music9Time').textContent=time(s.position)+' / '+time(s.duration);
      status(s.importing?s.progress:s.error||(s.status==='preparing'?'재생 준비 중…':s.playing?'재생 중 · 듣던 위치 자동 저장':s.status==='paused'?'일시정지 · ▶ 재생으로 이어듣기':'대기 · 저장된 음악과 재생 위치 보존'));
      const b=document.getElementById('music9Play');b.textContent=s.playing?'▶ 재생 중':'▶ 재생 / 이어듣기';refreshRadioUi();
    }catch(err){status(err.message);}
  }
  function startTick(){if(timer)clearInterval(timer);tick();timer=setInterval(tick,1000);}
  function state(){try{return read('getMusicState');}catch(err){return {error:err.message};}}
  function compactSavedRows(items,currentId){
    const rows=(Array.isArray(items)?items:[]).slice(0,9);
    if(!rows.length)return `<div class="music9-small" style="padding:10px 2px">아직 저장된 음악이 없습니다. ‘내 파일 MP3 추가’에서 음악을 넣어 주세요.</div>`;
    return rows.map((t,i)=>`<div class="music9-minirow ${String(t.id)===String(currentId)?'current':''}" onclick="playSavedMusicFromMain('${String(t.id).replace(/'/g,"\\'")}')"><span class="music9-no">${String(i+1).padStart(2,'0')}</span><span class="music9-bars">${String(t.id)===String(currentId)?'▮▮▮':''}</span><span class="music9-title">${e(t.name||'음악')}</span><span class="music9-heart">${String(t.id)===String(currentId)?'♥':'♡'}</span><span class="music9-more">⋮</span></div>`).join('');
  }
  function open(){
    view='player';let s=state(),lib={ok:false,items:[]};try{lib=read('getSavedMusicLibrary')||lib;}catch(err){}
    const items=Array.isArray(lib.items)?lib.items:[],count=items.length;
    modal('배경음악',style()+common(s)+`<div class="panel music9-library-panel">
      <div class="music9-library-head"><button class="music9-folder-title" onclick="ourMusicFolderPage()">📁 우리 음악폴더 (${e(count)}곡)</button><button class="b" onclick="musicDo('multi')">＋ 내 파일 MP3 추가</button></div>
      <div class="music9-mini-list">${compactSavedRows(items,s.id)}</div>
      <div class="music9-controls music9-source-controls"><button class="b" onclick="musicDo('reset')">⚙ 기본 음악으로</button><button class="b" onclick="musicDo('library')">⭐ 저장한 음악으로</button></div>
      ${radioPanel()}
      <div class="music9-tagline">음악과 함께하는 더 즐거운 항해, <b>항행의자유</b></div>
      </div>`);
    document.getElementById('modal')?.classList.add('musicOneScreen');
    startTick();
  }
  function library(){
    view='library';visibleCount=200;let l;
    try{l=read('getSavedMusicLibrary');if(!l.ok)throw new Error(l.error||'음악목록 읽기 실패');}
    catch(err){modal('우리 음악폴더',style()+`<div class="panel">${e(err.message)}<p>기존 목록은 삭제하지 않았습니다.</p><button class="b" onclick="musicPage()">음악 화면으로</button></div>`);return;}
    lastItems=Array.isArray(l.items)?l.items:[];
    modal('우리 음악폴더 · 저장된 음악',style()+common(state())+`<div class="panel">
      <h3>저장된 음악 <span id="music9Count">${lastItems.length}</span>곡</h3>
      <div class="music9-controls">
        <button class="b gold" onclick="musicDo('multi')">＋ MP3 여러 곡 추가</button>
        <button class="b green" onclick="musicDo('folder')">📂 휴대폰 Music 폴더 연결</button>
        <button class="b" onclick="musicDo('refreshfolder')">↻ 폴더 전곡 불러오기</button>
      </div>
      <div class="music9-small">${l.folderConnected?'연결한 폴더를 기억하고 있습니다. 전곡 불러오기는 새 곡만 추가합니다.':'폴더를 한 번 연결하면 다음부터 다시 찾지 않아도 됩니다. 기존 MP3 파일은 이동하거나 삭제하지 않습니다.'}<br>음악 보관에는 저장공간이 필요합니다. 앱 삭제·데이터 초기화 시 앱 안의 보관본은 지워집니다.</div>
      <input id="music9Search" placeholder="저장한 곡 이름 찾기" aria-label="저장한 곡 이름 찾기" style="margin-top:12px">
      <div id="music9List"></div><button class="b music9-wide" id="music9More" style="display:none">더 보기</button>
      ${radioPanel()}
      <button class="b music9-wide" onclick="musicPage()">음악 화면으로 돌아가기</button>
      </div>`);
    document.getElementById('modal')?.classList.add('musicOneScreen');
    const search=document.getElementById('music9Search');search.addEventListener('input',()=>{visibleCount=200;renderRows();});
    document.getElementById('music9More').onclick=()=>{visibleCount+=200;renderRows();};
    renderRows();startTick();
  }
  function renderRows(){
    const q=String(document.getElementById('music9Search').value||'').toLocaleLowerCase();
    const items=lastItems.filter(t=>String(t.name).toLocaleLowerCase().includes(q));
    const list=document.getElementById('music9List');list.textContent='';
    items.slice(0,visibleCount).forEach(t=>{
      const row=document.createElement('div');row.className='music9-track';
      const label=document.createElement('span');label.textContent=t.name+(t.available===false?' · 파일 확인 필요':'');row.appendChild(label);
      const button=document.createElement('button');button.className='b green';button.textContent='▶ 재생';button.disabled=t.available===false;
      button.onclick=()=>{try{h().playSavedMusic(String(t.id));status('선택한 곡 준비 중…');setTimeout(tick,500);}catch(err){status(err.message);}};
      row.appendChild(button);list.appendChild(row);
    });
    if(!items.length)list.textContent=lastItems.length?'해당 이름의 곡이 없습니다.':'아직 보관된 곡이 없습니다. 위에서 MP3를 추가하거나 음악폴더를 연결하세요.';
    document.getElementById('music9More').style.display=items.length>visibleCount?'block':'none';
  }

  const radioStations=[
    {id:'kbs',name:'KBS 클래식 FM',freq:'93.1 MHz',stream:'https://radio.bsod.kr/stream?stn=kbs&ch=1fm'},
    {id:'mbc',name:'MBC FM4U',freq:'91.9 MHz',stream:'https://radio.bsod.kr/stream?stn=mbc&ch=fm4u'},
    {id:'sbs',name:'SBS 파워FM',freq:'107.7 MHz',stream:'https://radio.bsod.kr/stream?stn=sbs&ch=powerfm'},
    {id:'happy',name:'KBS 해피FM',freq:'106.1 MHz',stream:'https://radio.bsod.kr/stream?stn=kbs&ch=2radio'}
  ];
  let radioIndex=0, radioFallback=null;
  function radioNativeState(){
    try{const host=h();if(host&&typeof host.getRadioDirectState==='function')return JSON.parse(String(host.getRadioDirectState()));}catch(e){}
    return {playing:false,name:'',url:''};
  }
  function stopRadioAudio(silent){
    try{const host=h();if(host&&typeof host.stopRadioDirect==='function')host.stopRadioDirect();}catch(e){}
    try{if(radioFallback){radioFallback.pause();radioFallback.removeAttribute('src');radioFallback.load();}}catch(e){}
    radioFallback=null;
    const b=document.getElementById('fm9Listen'),st=document.getElementById('fm9Status');
    if(b)b.textContent='▶ 선택 채널 소리만 듣기';
    if(st&&!silent)st.textContent='라디오를 종료했습니다.';
  }
  function radioPanel(){
    const r=radioStations[radioIndex]||radioStations[0],rs=radioNativeState(),same=!!rs.playing&&String(rs.url)===String(r.stream);
    return `<div class="panel fm9"><div class="fm9-head"><strong>📻 FM 라디오</strong><span class="music9-small">V3.0.94 · 소리만 재생</span></div>
      <div class="fm9-now"><div><b id="fm9Name">${e(r.name)}</b><small id="fm9Note">화면 없이 소리만 재생</small></div><b id="fm9Freq">${e(r.freq)}</b></div>
      <button class="b green fm9-listen" id="fm9Listen" onclick="openRadioLive()">${same?'■ 라디오 끄기':'▶ 선택 채널 소리만 듣기'}</button>
      <div class="fm9-presets">${radioStations.map((x,i)=>`<button class="b ${i===radioIndex?'gold':''}" onclick="selectRadioStation(${i})">${e(x.name)}<br>${e(x.freq.replace(' MHz',''))}</button>`).join('')}</div>
      <div class="music9-small" id="fm9RegionNote" style="margin-top:7px">※ 지역에 따라 방송 주파수가 다를 수 있습니다. (현재 표시 주파수는 기본값입니다.)</div>
      <div class="music9-small" id="fm9Status" style="margin-top:3px">V3.0.94 · 방송사 화면은 열리지 않습니다.</div></div>`;
  }
  function refreshRadioUi(){
    const r=radioStations[radioIndex]||radioStations[0],rs=radioNativeState(),same=!!rs.playing&&String(rs.url)===String(r.stream);
    const n=document.getElementById('fm9Name'),f=document.getElementById('fm9Freq'),b=document.getElementById('fm9Listen'),st=document.getElementById('fm9Status');
    if(n)n.textContent=r.name;if(f)f.textContent=r.freq;
    if(b)b.textContent=same?'■ 라디오 끄기':'▶ 선택 채널 소리만 듣기';
    if(st&&rs.playing)st.textContent=(rs.name||'FM 라디오')+' 재생 중 · 화면은 열리지 않습니다.';
    document.querySelectorAll('.fm9-presets .b').forEach((x,j)=>x.classList.toggle('gold',j===radioIndex));
  }
  window.selectRadioStation=function(i){radioIndex=Math.max(0,Math.min(radioStations.length-1,Number(i)||0));refreshRadioUi();};
  window.openRadioLive=function(){
    const r=radioStations[radioIndex]||radioStations[0],st=document.getElementById('fm9Status'),host=h(),rs=radioNativeState();
    try{
      if(rs.playing&&String(rs.url)===String(r.stream)){stopRadioAudio(false);setTimeout(refreshRadioUi,250);return;}
      stopRadioAudio(true);
      try{if(host&&typeof host.musicAction==='function')host.musicAction('pause');}catch(e){}
      if(host&&typeof host.playRadioDirect==='function'){
        host.playRadioDirect(r.stream,r.name);
        if(st)st.textContent=r.name+' 연결 중…';
        setTimeout(refreshRadioUi,1200);setTimeout(refreshRadioUi,3000);return;
      }
      const a=new Audio();radioFallback=a;a.src=r.stream;a.preload='none';
      a.addEventListener('playing',()=>{if(st)st.textContent=r.name+' 재생 중 · 화면은 열리지 않습니다.';refreshRadioUi();},{once:true});
      a.addEventListener('error',()=>{if(st)st.textContent='라디오 연결에 실패했습니다.';});
      a.play();
    }catch(err){if(st)st.textContent='라디오 재생 오류: '+err.message;}
  };
  window.playSavedMusicFromMain=function(id){try{stopRadioAudio(true);h().playSavedMusic(String(id));status('선택한 곡 준비 중…');setTimeout(open,550);}catch(err){status(err.message);}};
  window.MusicV9={open,library};window.musicPage=open;window.ourMusicFolderPage=library;
  window.musicDo=function(action){
    try{
      if(['play','prev','next','reset','library','stop'].includes(String(action))) stopRadioAudio(true);
      if(action==='multi'){h().chooseMultipleMusic();status('내 파일에서 음악을 선택하세요. 취소해도 기존 목록은 유지됩니다.');return;}
      const message=h().musicAction(String(action));status(String(message||'처리 중…'));setTimeout(tick,700);
    }catch(err){status(err.message);}
  };
  window.onMusicLibraryChanged=function(result){
    if(!result?.ok){status((result?.error||'저장 실패')+' · 기존 목록 유지');return;}
    if(document.getElementById('music9Name')){if(view==='library')library();else open();}
  };
  window.onMusicSelected=()=>window.onMusicLibraryChanged({ok:true});
  window.onMusicFolderSelected=()=>window.onMusicLibraryChanged({ok:true});
  window.onDeviceMusicPlaylistSaved=window.onMusicLibraryChanged;
})();