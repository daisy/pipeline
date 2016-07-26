package org.daisy.dotify.formatter.impl;

import java.util.List;
import java.util.Stack;

import org.daisy.dotify.api.formatter.DynamicSequenceBuilder;
import org.daisy.dotify.api.formatter.FormatterCore;
import org.daisy.dotify.api.formatter.ItemSequenceProperties;
import org.daisy.dotify.api.formatter.ReferenceListBuilder;
import org.daisy.dotify.api.formatter.SequenceProperties;

class DynamicSequenceEventImpl implements VolumeSequence, DynamicSequenceBuilder {
	private final SequenceProperties props;
	private final Stack<BlockGroup> formatters;
	private final FormatterCoreContext fc;

	
	/**
	 * Creates a new sequence event
	 * @param props
	 */
	public DynamicSequenceEventImpl(FormatterCoreContext fc, SequenceProperties props) {
		this.fc = fc;
		this.props = props;
		this.formatters = new Stack<>();
	}

	@Override
	public SequenceProperties getSequenceProperties() {
		return props;
	}

	@Override
	public BlockSequence getBlockSequence(FormatterContext context, DefaultContext c, CrossReferenceHandler crh) {
		BlockSequenceManipulator fsm = new BlockSequenceManipulator(
				context.getMasters().get(getSequenceProperties().getMasterName()), 
				getSequenceProperties());
		boolean hasContent = false;
		for (BlockGroup b : formatters) {
			List<Block> g = b.getBlocks(context, c, crh);
			if (!g.isEmpty()) {
				if (b.isGenerated()) {
					hasContent = true;
				}
				fsm.appendGroup(g);
			}
		}
		if (hasContent) {
			return fsm.newSequence();
		} else {
			return null;
		}
	}

	@Override
	public FormatterCore newStaticContext() {
		FormatterCoreImpl n = new FormatterCoreImpl(fc);
		formatters.add(n);
		return n;
	}

	@Override
	public ReferenceListBuilder newReferencesListContext(ItemSequenceProperties props) {
		ItemSequenceEventImpl n = new ItemSequenceEventImpl(fc, props.getRange(), props.getCollectionID());
		formatters.add(n);
		return n;
	}

}
