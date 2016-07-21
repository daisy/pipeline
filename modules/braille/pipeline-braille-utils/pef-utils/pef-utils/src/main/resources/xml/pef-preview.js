
var braillePages,textPages;
var metadata;
var brailleButton,textButton,metadataButton;
function init() {
    braillePages = Array.prototype.slice.call(document.getElementsByClassName('braille-page'),0);
    textPages = Array.prototype.slice.call(document.getElementsByClassName('text-page'),0);
    metadata = document.getElementById('metadata');
    brailleButton = document.getElementById('view-braille');
    textButton = document.getElementById('view-text');
    metadataButton = document.getElementById('view-metadata');
}
function show(elem,index,array) {
    elem.style.display='block';
}
function hide(elem,index,array) {
    elem.style.display='none';
}
function isHidden(elem) {
    elem.style.display=='none'
};
function activate(button) {
    button.className += ' active';
};
function deactivate(button) {
    button.className = button.className.replace('active', '');
};
var viewState = true;
function toggleView() {
    viewState = !viewState;
    if (viewState) {
        activate(brailleButton);
        deactivate(textButton);
        braillePages.forEach(show);
        textPages.forEach(hide);
    } else {
        deactivate(brailleButton);
        activate(textButton);
        braillePages.forEach(hide);
        textPages.forEach(show);
    }
};
var metadataState = false;
function toggleMetadata() {
    metadataState = !metadataState;
    if (metadataState) {
        activate(metadataButton);
        show(metadata, null, null);
    } else {
        deactivate(metadataButton);
        hide(metadata, null, null);
    }
};
window.onload = init;
