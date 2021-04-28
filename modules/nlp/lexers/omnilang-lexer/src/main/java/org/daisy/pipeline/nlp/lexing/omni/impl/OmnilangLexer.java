package org.daisy.pipeline.nlp.lexing.omni.impl;

import java.text.BreakIterator;
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
	name = "omnilang-lex-service",
	service = { LexService.class }
)
public class OmnilangLexer implements LexService {

	private static class OmniLexerToken extends LexerToken {

		private Matcher mWordMatcher;
		private Matcher mSpaceMatcher;

		public OmniLexerToken(LexService lexService) {
			super(lexService);
			mWordMatcher = Pattern.compile("[^\\p{P}\\s\\p{Z}\\p{C}]", Pattern.MULTILINE)
			        .matcher("");
			mSpaceMatcher = Pattern.compile("[\\p{Z}\\s]+", Pattern.MULTILINE).matcher("");
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
			if (mSpaceMatcher.matches())
				return Collections.EMPTY_LIST;

			BreakIterator sentIterator;
			BreakIterator wordIterator;
			if (lang == null) {
				sentIterator = BreakIterator.getSentenceInstance();
				wordIterator = BreakIterator.getWordInstance();
			} else {
				sentIterator = BreakIterator.getSentenceInstance(lang);
				wordIterator = BreakIterator.getWordInstance(lang);
			}
			List<Sentence> result = new ArrayList<Sentence>();

			//replace "J.J.R. Tolkien" with "J.J.RA Tolkien"
			input = input.replaceAll("(\\p{Lu})(([.]\\p{Lu})+)[.](?=[\n ]\\p{Lu})", "$1$2A");

			//replace "!)" with ",)" to prevent it from detecting a new sentence
			input = input.replaceAll("[؟:?‥!…។៕。]\\)", ",)");

			if (input.matches(".*https?://.*")) {
				int offset = 0;
				while((offset = input.indexOf('?', offset)) != -1) {
					if (input.charAt(offset) == '?' && input.charAt(offset + 1) != ' ') {
						input = input.substring(0,offset)+'_'+input.substring(offset + 1);
					}
				}
			}

			sentIterator.setText(input);
			int start = sentIterator.first();
			for (int end = sentIterator.next(); end != BreakIterator.DONE; start = end, end = sentIterator
			        .next()) {
				Sentence s = new Sentence();
				s.boundaries = new TextBoundaries();
				s.boundaries.left = start;
				s.boundaries.right = end;
				s.words = new ArrayList<TextBoundaries>();
				result.add(s);

				//TODO: trim the white spaces?

				wordIterator.setText(input.substring(start, end));
				int wstart = wordIterator.first();
				for (int wend = wordIterator.next(); wend != BreakIterator.DONE; wstart = wend, wend = wordIterator
				        .next()) {

					int left = start + wstart;
					int right = start + wend;

					mWordMatcher.reset(input.substring(left, right));
					if (mWordMatcher.lookingAt()) {
						TextBoundaries tb = new TextBoundaries();
						tb.left = left;
						tb.right = right;
						s.words.add(tb);
					}
				}
			}

			return result;
		}

	}

	@Override
	public int getLexQuality(Locale lang) {
		return 3;
	}

	@Override
	public String getName() {
		return "omnilang-lexer";
	}

	@Override
	public LexerToken newToken() {
		return new OmniLexerToken(this);
	}

	@Override
	public void globalInit() throws LexerInitException {
	}

	@Override
	public void globalRelease() {
	}

	@Override
	public int getOverallQuality() {
		return 5;
	}
}
