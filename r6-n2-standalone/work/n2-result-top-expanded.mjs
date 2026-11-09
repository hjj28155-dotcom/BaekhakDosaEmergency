/* Attach after identifying existing N2 DOM IDs in assets/index.html. Does not alter APK itself. */
export function expandN2ResultAtTop({overlay,scrollContainer,image}){
 if(!overlay||!scrollContainer||!image||!overlay.contains(scrollContainer)||!scrollContainer.contains(image)) throw Error('N2_RESULT_ELEMENTS_NOT_IDENTIFIED');
 overlay.classList.add('n2-result-overlay-fullscreen');
 scrollContainer.classList.add('n2-result-scroll');
 image.classList.add('n2-result-png');
 overlay.scrollTop=0;scrollContainer.scrollTop=0;
 requestAnimationFrame(()=>{scrollContainer.scrollTop=0});
}