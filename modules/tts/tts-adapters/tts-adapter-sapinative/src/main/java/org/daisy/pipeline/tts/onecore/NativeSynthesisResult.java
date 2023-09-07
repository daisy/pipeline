package org.daisy.pipeline.tts.onecore;

public class NativeSynthesisResult {
    private byte[] streamData;
    private String[] marksNames;
    private long[] marksPositions;

    public NativeSynthesisResult(byte[] streamData, String[] marksNames, long[] marksPositions) {
        this.streamData = streamData;
        this.marksNames = marksNames;
        this.marksPositions = marksPositions;
    }


    public byte[] getStreamData() {
        return streamData;
    }

    public void setStreamData(byte[] streamData) {
        this.streamData = streamData;
    }

    public String[] getMarksNames() {
        return marksNames;
    }

    public void setMarksNames(String[] marksNames) {
        this.marksNames = marksNames;
    }

    public long[] getMarksPositions() {
        return marksPositions;
    }

    public void setMarksPositions(long[] marksPositions) {
        this.marksPositions = marksPositions;
    }
}
