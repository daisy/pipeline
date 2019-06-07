package org.daisy.dotify.formatter.impl.segment;

import java.util.Optional;

import org.daisy.dotify.api.translator.ResolvableText;

public interface Segment extends ResolvableText {
	//{PCDATA, LEADER, MARKER, ANCHOR, BR, EVALUATE, BLOCK, TOC_ENTRY, PAGE_NUMBER}
	enum SegmentType {Text, NewLine, Leader, Reference, Marker, Anchor, Identifier, Evaluate, Style};
	
	public SegmentType getSegmentType();
	
	@Override
	public default Optional<String> getLocale() {
		return Optional.empty();
	}

	@Override
	public default boolean shouldHyphenate() {
		return false;
	}

	@Override
	public default boolean shouldMarkCapitalLetters() {
		return true;
	}

}