package org.daisy.dotify.translator.impl.liblouis;

class LiblouisTranslatable {
    private final String text;
    private final int[] charAtts;
    private final int[] interCharAtts;

    LiblouisTranslatable(String text, int[] charAtts, int[] interCharAtts) {
        this.text = text;
        this.charAtts = charAtts;
        this.interCharAtts = interCharAtts;
    }

    String getText() {
        return text;
    }

    int[] getCharAtts() {
        return charAtts;
    }

    int[] getInterCharAtts() {
        return interCharAtts;
    }

}
