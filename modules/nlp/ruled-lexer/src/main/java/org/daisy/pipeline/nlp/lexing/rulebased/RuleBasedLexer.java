package org.daisy.pipeline.nlp.lexing.rulebased;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.daisy.pipeline.nlp.RuleBasedTextCategorizer;
import org.daisy.pipeline.nlp.TextCategorizer;
import org.daisy.pipeline.nlp.TextCategorizer.CategorizedWord;
import org.daisy.pipeline.nlp.TextCategorizer.Category;
import org.daisy.pipeline.nlp.TextCategorizer.MatchMode;
import org.daisy.pipeline.nlp.lexing.LexService;
import org.daisy.pipeline.nlp.ruledcategorizers.RuledFrenchCategorizer;
import org.daisy.pipeline.nlp.ruledcategorizers.RuledMultilangCategorizer;

import org.osgi.service.component.annotations.Component;

/**
 * RuleLexerToken uses RuledBasedTextCategorizer in PREFIX_MODE for splitting
 * the input stream into words. Then it calls a SentenceDectector to group them
 * together in sentences. It can share categorizers and sentence detectors with
 * other pipeline jobs when they are thread-safe, since they may load big
 * dictionaries.
 */
@Component(
	name = "ruled-lex-service",
	service = { LexService.class }
)
public class RuleBasedLexer implements LexService {

	class RuleLexerToken extends LexService.LexerToken {
		private Map<Locale, ISentenceDetector> mSentDetectors = new HashMap<Locale, ISentenceDetector>();
		private Map<Locale, RuleBasedTextCategorizer> mTextCategorizers = new HashMap<Locale, RuleBasedTextCategorizer>();
		private RuleBasedTextCategorizer mGenericCategorizer;
		private ISentenceDetector mGenericSentDetector;

		public RuleLexerToken(RuleBasedLexer l) {
			super(l);
		}

		@Override
		public List<Sentence> split(String input, Locale lang, List<String> parsingErrors) {
			if (input.length() == 0)
				return Collections.EMPTY_LIST;

			RuleBasedTextCategorizer categorizer = mTextCategorizers.get(lang);
			ISentenceDetector splitter = mSentDetectors.get(lang);

			// call the categorizer and the sentence detector
			List<CategorizedWord> words = splitIntoWords(input, lang, categorizer);

			for (int k = 0; k < words.size(); ++k) {
				if (words.get(k).category == Category.UNKNOWN) {
					StringBuilder error = new StringBuilder(getName()
					        + ": the lexeme between square brackets could not be recognized: ");
					for (int i = Math.max(0, k - 10); i < k; ++i)
						error.append(words.get(i).word);
					error.append("[" + words.get(k).word + "]");
					for (int i = k + 1; i < Math.min(words.size(), k + 10); ++i)
						error.append(words.get(i).word);
					parsingErrors.add(error.toString());
				}
			}

			if (words.size() == 0
			        || (words.size() == 1 && !TextCategorizer.isSpeakable(words.iterator()
			                .next().category)))
				return Collections.EMPTY_LIST;

			List<List<CategorizedWord>> sentences = splitter.split(words);

			// build the sentences in the expected format
			int currentPos = 0;
			List<Sentence> res = new ArrayList<Sentence>();
			for (List<CategorizedWord> sentence : sentences) {
				//discard empty sentences
				int emptySize = 0;
				for (CategorizedWord word : sentence) {
					if (TextCategorizer.isSpeakable(word.category)) {
						emptySize = -1;
						break;
					}
					emptySize += word.word.length();
				}
				if (emptySize != -1) {
					currentPos += emptySize;
					continue;
				}

				Sentence s = new Sentence();
				s.boundaries = new TextBoundaries();
				res.add(s);

				//NOTE: Now, the StringComposer should already trim the sentences 

				//find the beginning of the sentence
				ListIterator<CategorizedWord> it = sentence.listIterator();
				while (it.hasNext()) {
					CategorizedWord word = it.next();
					if (word.category != Category.SPACE) {
						s.boundaries.left = currentPos;
						it.previous();
						break;
					}
					currentPos += word.word.length();
				}

				//content
				s.words = new ArrayList<LexService.TextBoundaries>();
				while (it.hasNext()) {
					CategorizedWord word = it.next();
					if (TextCategorizer.isSpeakable(word.category)) {
						TextBoundaries bounds = new TextBoundaries();
						bounds.left = currentPos;
						bounds.right = bounds.left + word.word.length();
						s.words.add(bounds);
					}
					currentPos += word.word.length();
				}

				//go backward to find the end of the sentence
				int end = currentPos;
				while (it.hasPrevious()) {
					CategorizedWord word = it.previous();
					if (word.category != Category.SPACE) {
						s.boundaries.right = end;
						break;
					}
					end -= word.word.length();
				}
			}

			return res;
		}

		private List<CategorizedWord> splitIntoWords(String input, Locale locale,
		        RuleBasedTextCategorizer categorizer) {
			String lowerCase = input.toLowerCase(locale);
			LinkedList<CategorizedWord> result = new LinkedList<CategorizedWord>();

			int shift = 0;
			while (shift < input.length()) {
				String right = input.substring(shift);
				String lowerCaseRight = lowerCase.substring(shift);
				CategorizedWord w = categorizer.categorize(right, lowerCaseRight);
				result.add(w);
				shift += w.word.length();
			}

			return result;
		}

		@Override
		public void shareResourcesWith(LexerToken other, Locale lang) {
			RuleLexerToken rother = (RuleLexerToken) other;

			if (mGenericCategorizer == null) {
				RuleBasedTextCategorizer genericCategorizer = rother.mGenericCategorizer;
				if (genericCategorizer != null && genericCategorizer.threadsafe()) {
					mGenericCategorizer = genericCategorizer;
				}
			}

			if (mGenericSentDetector == null) {
				ISentenceDetector genericSentDetector = rother.mGenericSentDetector;
				if (genericSentDetector != null && genericSentDetector.threadsafe()) {
					mGenericSentDetector = genericSentDetector;
				}
			}

			if (mTextCategorizers.get(lang) == null) {
				RuleBasedTextCategorizer categorizer = rother.mTextCategorizers.get(lang);
				if (categorizer != null && categorizer.threadsafe()) {
					mTextCategorizers.put(lang, categorizer);
				}
			}

			if (mSentDetectors.get(lang) == null) {
				ISentenceDetector sentDetector = rother.mSentDetectors.get(lang);
				if (sentDetector != null && sentDetector.threadsafe()) {
					mSentDetectors.put(lang, sentDetector);
				}
			}
		}

		@Override
		public void addLang(Locale lang) throws LexerInitException {
			if (mGenericSentDetector == null) {
				mGenericSentDetector = new EuroSentenceDetector();
			}

			if (mSentDetectors.get(lang) == null) {
				mSentDetectors.put(lang, mGenericSentDetector);
			}

			try {
				if (mGenericCategorizer == null) {
					mGenericCategorizer = new RuledMultilangCategorizer();
					mGenericCategorizer.init(MatchMode.PREFIX_MATCH);
					mGenericCategorizer.compile();
				}

				if (mTextCategorizers.get(lang) == null) {
					String iso639_2lang = lang.getISO3Language();
					RuleBasedTextCategorizer rtc = mGenericCategorizer;
					if ("fre".equals(iso639_2lang) || "fra".equals(iso639_2lang)
					        || "frm".equals(iso639_2lang) || "fro".equals(iso639_2lang)) {
						rtc = new RuledFrenchCategorizer();
						rtc.init(MatchMode.PREFIX_MATCH);
						rtc.compile();
					}

					mTextCategorizers.put(lang, rtc);
				}

			} catch (IOException e) {
				throw new LexerInitException(e.getCause());
			}
		}

	}

	@Override
	public int getLexQuality(Locale lang) {
		String language = lang.getLanguage();

		if (language.equals(new Locale("fr").getLanguage()))
			return 3 * LexService.MinSpecializedLexQuality;

		if (language.equals(new Locale("en").getLanguage()))
			return 3 * LexService.MinSpecializedLexQuality;

		if (okLanguages.contains(language)) {
			return 2 * LexService.MinSpecializedLexQuality;
		}

		return 0;
	}

	@Override
	public String getName() {
		return "rule-based-lexer";
	}

	@Override
	public LexerToken newToken() {
		return new RuleLexerToken(this);
	}

	@Override
	public void globalInit() throws LexerInitException {
	}

	@Override
	public void globalRelease() {
	}

	@Override
	public int getOverallQuality() {
		return -1; //cannot handle all the languages
	}

	private static Set<String> okLanguages;
	static {
		okLanguages = new HashSet<String>();
		for (String code : new String[]{
		        "it", "pt"
		}) {
			okLanguages.add(new Locale(code).getLanguage());
		}
	}
}
