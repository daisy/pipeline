package org.daisy.dotify.formatter.impl.volume;

import java.util.Map;

import org.daisy.dotify.api.formatter.Condition;
import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.VolumeContentBuilder;
import org.daisy.dotify.api.formatter.VolumeTemplateBuilder;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;


public class VolumeTemplate implements VolumeTemplateBuilder {
	private final Condition condition;
	private final int splitterMax;
	private final VolumeContentBuilderImpl preVolumeContent, postVolumeContent;

	public VolumeTemplate(FormatterCoreContext fc, Map<String, TableOfContentsImpl> tocs, Condition condition, Integer splitterMax) {
		this.condition = condition;
		this.splitterMax = splitterMax;
		this.preVolumeContent = new VolumeContentBuilderImpl(fc, tocs);
		this.postVolumeContent = new VolumeContentBuilderImpl(fc, tocs);
	}

	/**
	 * Test if this Template applies to this combination of volume and volume count.
	 * @param context the context to test
	 * @return returns true if the Template should be applied to the volume
	 */
	public boolean appliesTo(Context context) {
		if (condition==null) {
			return true;
		}
		return condition.evaluate(context);
	}
	
	public Iterable<VolumeSequence> getPreVolumeContent() {
		return preVolumeContent;
	}

	public Iterable<VolumeSequence> getPostVolumeContent() {
		return postVolumeContent;
	}

	/**
	 * Gets the maximum number of sheets allowed.
	 * @return returns the number of sheets allowed
	 */
	public int getVolumeMaxSize() {
		return splitterMax;
	}

	@Override
	public VolumeContentBuilder getPreVolumeContentBuilder() {
		return preVolumeContent;
	}

	@Override
	public VolumeContentBuilder getPostVolumeContentBuilder() {
		return postVolumeContent;
	}

}
