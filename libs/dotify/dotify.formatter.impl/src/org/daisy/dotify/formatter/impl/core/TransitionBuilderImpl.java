package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.BlockBuilder;
import org.daisy.dotify.api.formatter.BlockContentBuilder;
import org.daisy.dotify.api.formatter.TransitionBuilder;
import org.daisy.dotify.api.formatter.TransitionBuilderProperties;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.core.TransitionContent.Type;

public class TransitionBuilderImpl implements TransitionBuilder {
	private TransitionBuilderProperties props;
	private final FormatterCoreImpl blockResumed;
	private final FormatterCoreImpl blockInterrupted;
	private final FormatterCoreImpl seqResumed;
	private final FormatterCoreImpl seqInterrupted;
	
	public TransitionBuilderImpl(FormatterCoreContext fc) {
		this.props = new TransitionBuilderProperties.Builder().build();
		this.blockResumed = new FormatterCoreImpl(fc);
		this.blockInterrupted = new FormatterCoreImpl(fc);
		this.seqResumed = new FormatterCoreImpl(fc);
		this.seqInterrupted = new FormatterCoreImpl(fc);		
	}
	
	public TransitionContent getInterruptTransition() {
		return new TransitionContent(Type.INTERRUPT, blockInterrupted, seqInterrupted);
	}
	
	public TransitionContent getResumeTransition() {
		return new TransitionContent(Type.RESUME, blockResumed, seqResumed);
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

}
