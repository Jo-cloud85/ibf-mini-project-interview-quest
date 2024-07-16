if ('serviceWorker' in navigator) {
  window.addEventListener('load', function() {
    navigator.serviceWorker.register('/reg_sw.js')
    .then(function(registration) {
      console.log('ServiceWorker registration successful with scope: ', registration.scope);
    })
    .catch(function(error) {
      console.log('ServiceWorker registration failed: ', error);
    });
  });
}