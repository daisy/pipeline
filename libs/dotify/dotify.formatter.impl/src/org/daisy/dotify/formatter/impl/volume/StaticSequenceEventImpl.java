package org.daisy.dotify.formatter.impl.volume;

import org.daisy.dotify.api.formatter.SequenceProperties;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;
import org.daisy.dotify.formatter.impl.core.FormatterContext;
import org.daisy.dotify.formatter.impl.core.FormatterCoreImpl;
import org.daisy.dotify.formatter.impl.page.BlockSequence;
import org.daisy.dotify.formatter.impl.search.CrossReferenceHandler;
import org.daisy.dotify.formatter.impl.search.DefaultContext;

class StaticSequenceEventImpl extends FormatterCoreImpl implements VolumeSequence {
	private static final long serialVersionUID = 4646831324973203983L;
	private final SequenceProperties props;
	private BlockSequence ret;

	
	/**
	 * Creates a new sequence event
	 * @param fc the formatter core context
	 * @param props the sequence properties
	 */
	StaticSequenceEventImpl(FormatterCoreContext fc, SequenceProperties props) {
		super(fc);
		this.props = props;
		this.ret = null;
	}

	@Override
	public SequenceProperties getSequenceProperties() {
		return props;
	}

	@Override
	public BlockSequence getBlockSequence(FormatterContext context, DefaultContext c, CrossReferenceHandler crh) {
		if (ret!=null) {
			//we can return previous result, because static contents does not depend on context.
			return ret;
		} else {
			BlockSequenceManipulator fsm = new BlockSequenceManipulator(
					context.getMasters().get(getSequenceProperties().getMasterName()), 
					getSequenceProperties());
			fsm.appendGroup(this);
			ret = fsm.newSequence();
			return ret;
		}
	}

}
