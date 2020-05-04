package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.BlockBuilder;
import org.daisy.dotify.api.formatter.BlockContentBuilder;
import org.daisy.dotify.api.formatter.TransitionBuilder;
import org.daisy.dotify.api.formatter.TransitionBuilderProperties;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.core.TransitionContent.Type;

/**
 * TODO: Write java doc.
 */
public class TransitionBuilderImpl implements TransitionBuilder {
    private TransitionBuilderProperties props;
    private final FormatterCoreImpl blockResumed;
    private final FormatterCoreImpl blockInterrupted;
    private final FormatterCoreImpl seqResumed;
    private final FormatterCoreImpl seqInterrupted;
    private final FormatterCoreImpl anyResumed;
    private final FormatterCoreImpl anyInterrupted;

    public TransitionBuilderImpl(FormatterCoreContext fc) {
        this.props = new TransitionBuilderProperties.Builder().build();
        this.blockResumed = new FormatterCoreImpl(fc);
        this.blockInterrupted = new FormatterCoreImpl(fc);
        this.seqResumed = new FormatterCoreImpl(fc);
        this.seqInterrupted = new FormatterCoreImpl(fc);
        this.anyResumed = new FormatterCoreImpl(fc);
        this.anyInterrupted = new FormatterCoreImpl(fc);
    }

    public TransitionContent getInterruptTransition() {
        return new TransitionContent(Type.INTERRUPT, blockInterrupted, seqInterrupted, anyInterrupted);
    }

    public TransitionContent getResumeTransition() {
        return new TransitionContent(Type.RESUME, blockResumed, seqResumed, anyResumed);
    }

    @Override
    public BlockContentBuilder getBlockResumedBuilder() {
        return blockResumed;
    }

    @Override
    public BlockContentBuilder getBlockInterruptedBuilder() {
        return blockInterrupted;
    }

    @Override
    public BlockBuilder getSequenceResumedBuilder() {
        return seqResumed;
    }

    @Override
    public BlockBuilder getSequenceInterruptedBuilder() {
        return seqInterrupted;
    }

    @Override
    public TransitionBuilderProperties getProperties() {
        return props;
    }

    @Override
    public void setProperties(TransitionBuilderProperties props) {
        this.props = props;
    }

    @Override
    public BlockBuilder getAnyResumedBuilder() {
        return anyResumed;
    }

    @Override
    public BlockBuilder getAnyInterruptedBuilder() {
        return anyInterrupted;
    }

}
