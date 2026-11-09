/* N2 original-chat integration draft. Not applied to APK. */
export function normalizeChatUrl(value) {
  if (typeof value !== 'string' || value.length > 2048) return null;
  try {
    const u = new URL(value.trim());
    if (u.protocol !== 'https:' || !['chatgpt.com','www.chatgpt.com'].includes(u.hostname)) return null;
    if (!/^\/(?:c|g\/[^\/]+\/c|share)\/[A-Za-z0-9-]+\/?$/.test(u.pathname)) return null;
    u.search=''; u.hash='';
    return u.toString();
  } catch { return null; }
}
export function createChatLinkController({storage,openExternal,key='n2_original_chat_url_v1'}) {
  if(!storage||typeof openExternal!=='function') throw Error('CHAT_DEPENDENCIES_REQUIRED');
  return {
    get(){return normalizeChatUrl(storage.getItem(key));},
    save(value){const url=normalizeChatUrl(value);if(!url)throw Error('INVALID_CHATGPT_CONVERSATION_LINK');storage.setItem(key,url);if(storage.getItem(key)!==url)throw Error('CHAT_LINK_WRITE_FAILED');return url;},
    open(){const url=normalizeChatUrl(storage.getItem(key));if(!url)return false;openExternal(url);return true;},
    clear(){storage.removeItem(key);return storage.getItem(key)===null;}
  };
}
export function bindOriginalChat({button,mount,storage=globalThis.localStorage,openExternal=(url)=>window.open(url,'_blank','noopener,noreferrer')}) {
  if(!button||!mount)throw Error('CHAT_UI_HOST_REQUIRED');
  const controller=createChatLinkController({storage,openExternal});
  function showEditor() {
    mount.replaceChildren();
    const panel=document.createElement('section');panel.className='n2-original-chat-editor';
    const label=document.createElement('label');label.textContent='원본챗방 링크';
    const input=document.createElement('input');input.type='url';input.inputMode='url';input.placeholder='https://chatgpt.com/c/...';input.autocomplete='off';input.value=controller.get()||'';
    const save=document.createElement('button');save.type='button';save.textContent='주소 저장';
    const open=document.createElement('button');open.type='button';open.textContent='원본챗방 열기';
    const change=document.createElement('button');change.type='button';change.textContent='주소 변경';
    const close=document.createElement('button');close.type='button';close.textContent='닫기';
    const status=document.createElement('div');status.setAttribute('role','status');
    save.onclick=()=>{try{controller.save(input.value);status.textContent='주소가 저장되었습니다.';}catch{status.textContent='올바른 ChatGPT 대화방 주소를 입력해 주세요.';}};
    open.onclick=()=>{if(!controller.open())status.textContent='주소를 먼저 저장해 주세요.';};
    change.onclick=()=>{input.readOnly=false;input.focus();};
    close.onclick=()=>mount.replaceChildren();
    label.append(input);panel.append(label,save,open,change,close,status);mount.append(panel);input.focus();
  }
  button.addEventListener('click',()=>{if(!controller.open())showEditor();});
  return {controller,showEditor};
}