/* Analysis V4 browser button repair. Does not install or change app data. */
(function(){
  function connect(){
    document.querySelectorAll('button[onclick]').forEach(function(button){
      button.style.touchAction='manipulation';
    });
  }
  if(document.readyState==='loading')document.addEventListener('DOMContentLoaded',connect);
  else connect();
})();
