package org.daisy.dotify.api.formatter;

public interface VolumeContentBuilder {
	
	public FormatterCore newSequence(SequenceProperties props);

	public TocSequenceBuilder newTocSequence(TocProperties props);

}
