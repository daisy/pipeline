package org.daisy.pipeline.nlp.lexing.light.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daisy.pipeline.nlp.lexing.LexService;

import org.osgi.service.component.annotations.Component;

/**
 * This is a multi-language lexer that does not support the following features:
 * 
 * - Word segmentation ;
 * 
 * - Period-based sentence segmentation (periods can be ambiguous).
 */
@Component(
	name = "light-lex-service",
	service = { LexService.class }
)
public class LightLexer implements LexService {

	private static class LightLexerToken extends LexerToken {

		private static final String WhiteSpaces = " \t\r\n";
		private ArrayList<Integer> mStartPositions;

		private Matcher mEndMatcher;
		private Matcher mSepMatcher;
		private Matcher mSpaceMatcher;

		public LightLexerToken(LexService lexService) {
			super(lexService);
			mStartPositions = new ArrayList<Integer>(1);
			mStartPositions.add(0);
			mSpaceMatcher = Pattern.compile("[\\s\\p{Z}]+", Pattern.MULTILINE).matcher("");
		}

		@Override
		public void shareResourcesWith(LexerToken other, Locale lang) {
		}

		@Override
		public void addLang(Locale lang) throws LexerInitException {
		}

		@Override
		public List<Sentence> split(String input, Locale lang, List<String> parsingErrors) {
			if (input.length() == 0)
				return Collections.EMPTY_LIST;

			mSpaceMatcher.reset(input);
			if (mSpaceMatcher.matches()) {
				//otherwise it wouldn't work with the whitespace trimming below
				return Collections.EMPTY_LIST;
			}

			String l = lang.getISO3Language();
			//Those are only examples. Feel free to customize them.
			if ("grc".equals(l) || "gre".equals(l) || "ell".equals(l)) {
				compileRegex("[¶]", "[:?!…;]|([.][.][.])"); //+ Greek semicolon
			} else if ("chi".equals(l) || "zho".equals(l)) {
				compileRegex("[¶]", "[:?!…。]|([.][.][.])"); //+ Chinese full stop
			} else {
				compileRegex("[¶]", "[؟:?‥!…។៕]|([.][.][.])");
			}

			mSepMatcher.reset(input);
			int startIndex = 0;
			int lastIndex = 0;
			int sepNumber = 1;
			while (mSepMatcher.find()) {
				mEndMatcher.reset(mSepMatcher.group());
				startIndex = mSepMatcher.start();
				if (mEndMatcher.find()) {
					startIndex += mEndMatcher.end();
				}
				mSpaceMatcher.reset(input.subSequence(lastIndex, mSepMatcher.end()));
				lastIndex = mSepMatcher.end();
				if (mSpaceMatcher.matches() || mSepMatcher.start() == 0) {
					continue; //i.e. discard the sentence
				}
				if (sepNumber == mStartPositions.size())
					mStartPositions.add(0);

				mStartPositions.set(sepNumber++, startIndex);
			}
			mSpaceMatcher.reset(input.substring(mStartPositions.get(sepNumber - 1)));
			if (mStartPositions.get(sepNumber - 1) != input.length()
			        && !mSpaceMatcher.matches()) {
				//add a virtual new sentence only if the sentence is not
				//already terminated by a separator and if the remaining text
				//is not made of white spaces only
				if (sepNumber == mStartPositions.size())
					mStartPositions.add(0);
				mStartPositions.set(sepNumber++, input.length());
			}

			ArrayList<Sentence> result = new ArrayList<Sentence>();
			for (int i = 0; i < sepNumber - 1; ++i) {
				int start = mStartPositions.get(i);
				int nextStart = mStartPositions.get(i + 1);

				//trim the white spaces
				while (WhiteSpaces.indexOf(input.charAt(start)) != -1)
					++start;
				while (WhiteSpaces.indexOf(input.charAt(nextStart - 1)) != -1)
					--nextStart;

				Sentence s = new Sentence();
				result.add(s);
				s.boundaries = new TextBoundaries();
				s.boundaries.left = start;
				s.boundaries.right = nextStart;
			}
			return result;
		}

		private void compileRegex(String startMarks, String endMarks) {
			//pattern to match any group of sentence separators:
			//like "!!?" or "?!!   !!", but not "!)"
			String allMarks = "((" + startMarks + ")|(" + endMarks + "))";
			Pattern p = Pattern.compile("(" + allMarks + "+[\\s\\p{Z}]+" + allMarks + "+)|("
			        + allMarks + "+)(?!\\))", Pattern.MULTILINE);
			mSepMatcher = p.matcher("");

			//pattern to find the ending marks within the group of separators
			p = Pattern.compile("(" + endMarks + "|[\\s\\p{Z}])+", Pattern.MULTILINE);
			mEndMatcher = p.matcher("");
		}

	}

	@Override
	public int getLexQuality(Locale lang) {
		return 1;
	}

	@Override
	public String getName() {
		return "light-lexer";
	}

	@Override
	public LexerToken newToken() {
		return new LightLexerToken(this);
	}

	@Override
	public void globalInit() throws LexerInitException {
	}

	@Override
	public void globalRelease() {
	}

	@Override
	public int getOverallQuality() {
		return 2;
	}

	@Override
	public int hashCode() {
		return getName().hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof LexService))
			return false;
		LexService that = (LexService)o;
		if (!this.getName().equals(that.getName()))
			return false;
		return true;
	}
}
