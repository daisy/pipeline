package org.daisy.pipeline.nlp.calabash.impl;

import java.util.List;

import org.daisy.pipeline.nlp.calabash.impl.StringComposer.SentencePointer;
import org.daisy.pipeline.nlp.calabash.impl.StringComposer.TextPointer;

public class SegmentsPrettyPrinter {

	private String mWordSeparator = "/";

	public void setWordSeperator(String wordSeparator) {
		mWordSeparator = wordSeparator;
	}

	public String convert(Object result, List<String> lexerInput) {
		StringBuilder sb = new StringBuilder();
		dispatch(result, sb, lexerInput);
		return sb.toString();
	}

	private void dispatch(Object v, StringBuilder sb, List<String> references) {
		if (v instanceof List) {
			visit((List<SentencePointer>) v, sb, references);
		} else if (v instanceof SentencePointer) {
			visit((SentencePointer) v, sb, references);
		} else if (v instanceof TextPointer) {
			visit((TextPointer) v, sb, references);
		}
	}

	private void visit(TextPointer t, StringBuilder sb, List<String> references) {
		if (t.firstSegment == t.lastSegment) {
			if (references.get(t.firstSegment) != null)
				sb.append(references.get(t.firstSegment).substring(
				        t.firstIndex, t.lastIndex));
		} else {
			sb.append(references.get(t.firstSegment).substring(t.firstIndex));
			for (int i = t.firstSegment + 1; i < t.lastSegment; ++i) {
				if (references.get(i) != null)
					sb.append(references.get(i));
			}
			sb.append(references.get(t.lastSegment).substring(0, t.lastIndex));
		}
	}

	private void visit(List<SentencePointer> sentences, StringBuilder sb,
	        List<String> references) {
		for (SentencePointer s : sentences)
			dispatch(s, sb, references);
	}

	private void visit(SentencePointer s, StringBuilder sb,
	        List<String> references) {
		sb.append("{");
		if (s.content == null || s.content.isEmpty()) {
			visit(s.boundaries, sb, references);
		} else {
			TextPointer gap = new TextPointer();
			gap.firstIndex = s.boundaries.firstIndex;
			gap.firstSegment = s.boundaries.firstSegment;
			for (TextPointer word : s.content) {
				gap.lastIndex = word.firstIndex;
				gap.lastSegment = word.firstSegment;
				visit(gap, sb, references);
				sb.append(mWordSeparator);
				visit(word, sb, references);
				sb.append(mWordSeparator);
				gap.firstIndex = word.lastIndex;
				gap.firstSegment = word.lastSegment;
			}
			gap.lastIndex = s.boundaries.lastIndex;
			gap.lastSegment = s.boundaries.lastSegment;
			visit(gap, sb, references);
		}
		sb.append("}");
	}
}
