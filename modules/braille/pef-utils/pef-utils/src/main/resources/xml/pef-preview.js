var braillePages, textPages;
var metadata;
var brailleButton, textButton, metadataButton;
var nav;
var volumes;
var volumeLabels;
var volumeLabelHeight;
var currentVolume;
var transitioning;
function init() {
    braillePages = Array.prototype.slice.call(document.getElementsByClassName('braille-page'), 0);
    textPages = Array.prototype.slice.call(document.getElementsByClassName('text-page'), 0);
    metadata = document.getElementById('metadata');
    brailleButton = document.getElementById('view-braille');
    textButton = document.getElementById('view-text');
    metadataButton = document.getElementById('view-metadata');
    volumes = Array.prototype.slice.call(document.getElementsByClassName('nav-volume'), 0);
    volumeLabels = Array.prototype.slice.call(document.getElementsByClassName('volume-label'), 0).map(function(elem) {
        var dup = elem.cloneNode(true);
        dup.className = 'volume-label top';
        elem.parentNode.appendChild(dup);
        elem.style.visibility = 'hidden';
        return dup;
    });
    currentVolume = 1;
    transitioning = false;
    volumeLabels[0].className = 'volume-label fixed';
    volumeLabelHeight = volumeLabels[0].offsetHeight;
    nav = document.getElementById('nav');
    nav.onscroll = function(e) {
        var previousVolume = currentVolume;
        for (currentVolume = 1; currentVolume < volumes.length; currentVolume++) {
            if (volumes[currentVolume].offsetTop > nav.scrollTop) {
                break;
            }
        }
        var wasTransitioning = transitioning;
        transitioning = (
            currentVolume < volumes.length
            && volumes[currentVolume].offsetTop < (nav.scrollTop + volumeLabelHeight)
        );
        if (currentVolume != previousVolume) {
            volumeLabels[previousVolume - 1].className = 'volume-label top';
        }
        if (currentVolume != previousVolume || transitioning != wasTransitioning) {
            if (transitioning) {
                volumeLabels[currentVolume - 1].className = 'volume-label bottom';
            } else {
                volumeLabels[currentVolume - 1].className = 'volume-label fixed';
            }
        }
    };
}
function show(elem, index, array) {
    elem.style.display = 'block';
}
function hide(elem, index, array) {
    elem.style.display = 'none';
}
function isHidden(elem) {
    elem.style.display == 'none'
};
function activate(button) {
    addClass(button, 'active');
};
function deactivate(button) {
    removeClass(button, 'active');
};
function addClass(elem, className) {
    elem.classList.add(className);
}
function removeClass(elem, className) {
    elem.classList.remove(className);
}
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
