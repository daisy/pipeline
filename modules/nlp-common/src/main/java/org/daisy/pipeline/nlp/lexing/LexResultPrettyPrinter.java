package org.daisy.pipeline.nlp.lexing;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daisy.pipeline.nlp.lexing.LexService.Sentence;
import org.daisy.pipeline.nlp.lexing.LexService.TextBoundaries;

/**
 * Convert one Lexer's result to formatted text. Likely to be used for testing.
 */
public class LexResultPrettyPrinter {

	private String mWordSeparator = "/";
	private boolean mTrimming = false;

	public void setWordSeperator(String wordSeparator) {
		mWordSeparator = wordSeparator;
	}

	public void enableTrimming(boolean mode) {
		mTrimming = mode;
	}

	public String convert(Object result, String lexerInput) {
		StringBuilder sb = new StringBuilder();
		dispatch(result, sb, lexerInput);
		return sb.toString();
	}

	private void dispatch(Object v, StringBuilder sb, String reference) {
		if (v instanceof List) {
			visit((List<Sentence>) v, sb, reference);
		} else if (v instanceof Sentence) {
			visit((Sentence) v, sb, reference);
		} else if (v instanceof TextBoundaries) {
			visit((TextBoundaries) v, sb, reference, mWordSeparator, mWordSeparator);
		}
	}

	private void visit(TextBoundaries t, StringBuilder sb, String reference,
	        String separatorLeft, String separatorRight) {
		String v = reference.substring(t.left, t.right);
		if (mTrimming) {
			Matcher m = Pattern.compile("^\\s+").matcher(v);
			if (m.find()) {
				sb.append(m.group()); //move the white spaces before the separator
				if (m.group().length() == v.length()) {
					//empty word
					return;
				}
				v = v.substring(m.end());
			}
			m = Pattern.compile("\\s+$").matcher(v);
			if (m.find()) {
				if (m.start() != 0) {
					sb.append(separatorLeft);
					sb.append(v.substring(0, m.start()));
					sb.append(separatorRight);
					sb.append(m.group()); //move the white spaces after the separator
				} else
					sb.append(v); //empty word: only white spaces
			} else {
				sb.append(separatorLeft);
				sb.append(v);
				sb.append(separatorRight);
			}
		} else {
			sb.append(separatorLeft);
			sb.append(v);
			sb.append(separatorRight);
		}
	}

	private void visit(List<Sentence> sentences, StringBuilder sb, String references) {
		for (Sentence s : sentences)
			dispatch(s, sb, references);
	}

	private void visit(Sentence s, StringBuilder sb, String reference) {
		if (s.words == null || s.words.isEmpty()) {
			sb.append("{");
			String v = reference.substring(s.boundaries.left, s.boundaries.right);
			if (mTrimming)
				v = v.trim();
			sb.append(v);
			sb.append("}");
		} else {
			int lastPos = s.boundaries.left;
			if (mTrimming) {
				String v = reference.substring(lastPos, s.words.get(0).left);
				Matcher m = Pattern.compile("^\\s+").matcher(v);
				if (m.find()) {
					//sb.append(m.group()); //would move the white spaces before the {
					lastPos += m.end();
				}
			}
			sb.append("{");
			for (TextBoundaries word : s.words) {
				sb.append(reference.substring(lastPos, word.left));
				lastPos = word.right;
				visit(word, sb, reference, mWordSeparator, mWordSeparator);
			}
			String v = reference.substring(lastPos, s.boundaries.right);
			Matcher m = Pattern.compile("\\s+$").matcher(v);
			if (mTrimming && m.find()) {
				sb.append(v.substring(0, m.start()));
				sb.append("}");
				//sb.append(m.group()); //would move the white spaces after the }
			} else {
				sb.append(v);
				sb.append("}");
			}
		}
	}
}
