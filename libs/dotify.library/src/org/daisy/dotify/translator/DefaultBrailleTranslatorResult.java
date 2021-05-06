package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.UnsupportedMetricException;
import org.daisy.dotify.common.text.BreakPoint;
import org.daisy.dotify.common.text.BreakPointHandler;

class DefaultBrailleTranslatorResult implements BrailleTranslatorResult {
    private final BreakPointHandler bph;
    private final BrailleFinalizer finalizer;
    private int forceCount;

    public DefaultBrailleTranslatorResult(BreakPointHandler bph, BrailleFinalizer finalizer) {
        this.bph = bph;
        this.finalizer = finalizer;
        this.forceCount = 0;
    }

    private DefaultBrailleTranslatorResult(DefaultBrailleTranslatorResult template) {
        this.bph = template.bph.copy();
        this.finalizer = template.finalizer;
        this.forceCount = template.forceCount;
    }

    @Override
    public String nextTranslatedRow(int limit, boolean force, boolean wholeWordsOnly) {
        BreakPoint bp = bph.nextRow(limit, force, wholeWordsOnly);
        if (bp.isHardBreak()) {
            forceCount++;
        }
        if (finalizer != null) {
            return finalizer.finalizeBraille(bp.getHead());
        } else {
            return bp.getHead();
        }
    }

    @Override
    public boolean hasNext() {
        return bph.hasNext();
    }

    @Override
    public String getTranslatedRemainder() {
        if (finalizer != null) {
            return finalizer.finalizeBraille(bph.getRemaining());
        } else {
            return bph.getRemaining();
        }
    }

    @Override
    public int countRemaining() {
        return getTranslatedRemainder().length();
    }

    @Override
    public boolean supportsMetric(String metric) {
        return METRIC_FORCED_BREAK.equals(metric);
    }

    @Override
    public double getMetric(String metric) {
        if (metric.equals(METRIC_FORCED_BREAK)) {
            return forceCount;
        } else {
            throw new UnsupportedMetricException("Metric not supported: " + metric);
        }
    }

    @Override
    public BrailleTranslatorResult copy() {
        return new DefaultBrailleTranslatorResult(this);
    }

}
