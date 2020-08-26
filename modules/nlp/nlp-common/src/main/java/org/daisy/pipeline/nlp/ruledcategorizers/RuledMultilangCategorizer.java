package org.daisy.pipeline.nlp.ruledcategorizers;

import java.io.IOException;

import org.daisy.pipeline.nlp.RuleBasedTextCategorizer;
import org.daisy.pipeline.nlp.impl.matchrules.NumberRangeMatchRule;
import org.daisy.pipeline.nlp.impl.matchrules.RegexMatchRule;

public class RuledMultilangCategorizer extends RuleBasedTextCategorizer {

	public static int LOWEST_PRIORITY = 0;
	public static int COMMON_WORD_MAX_PRIORITY = 50;
	public static int SPACE_MAX_PRIORITY = 100;
	public static int QUOTE_MAX_PRIORITY = 125;
	public static int NUMBER_MAX_PRIORITY = 150;
	public static int ACRONYM_MAX_PRIORITY = 160;
	public static int ABBR_MAX_PRIORITY = 220;
	public static int WEBLINK_MAX_PRIORITY = 300;
	public static int SPACE_COMPOSED_MAX_PRIORITY = 500;
	public static int NUMBER_COMPOSED_MAX_PRIORITY = 600;
	public static int DICTIONARY_MAX_PRIORITY = 700;
	protected static String CommonWordPattern = "[@\\p{L}][-_@\\p{L}\\p{Nd}]*";

	//line breaks cannot be written with the usual unicode notation	
	protected static String Space = "";
	static {
		char[] SpaceChars = {
		        0x0020, 0x0085, 0x00A0, 0x1680, 0x180E, 0x2028, 0x2029, 0x202F, 0x205F, 0x3000
		};
		for (char spaceChar : SpaceChars) {
			Space += new Character(spaceChar);
		}
		Space += new Character((char) 0x0009) + "-" + new Character((char) 0x000D);
		Space += new Character((char) 0x2000) + "-" + new Character((char) 0x200A);
	}

	@Override
	public void init(MatchMode matchMode) throws IOException {
		super.init(matchMode);

		RegexMatchRule rsm;

		// ==== DATES ====
		String year = "([1-9][0-9]{1,3}|[0-9]{2})";
		String month = "(1[0-2]|0?[1-9])";
		String days = "(3[01]|[12]0|[0-2]?[1-9])";

		rsm = new RegexMatchRule(Category.DATE, NUMBER_COMPOSED_MAX_PRIORITY, true, mMatchMode);
		rsm.init(year + "-" + month + "-" + days + "(?![-\\p{L}\\p{Nd}])");
		addRule(rsm);

		rsm = new RegexMatchRule(Category.DATE, NUMBER_COMPOSED_MAX_PRIORITY, true, mMatchMode);
		rsm.init(month + "-" + days + "(?![-\\p{L}\\p{Nd}])");
		addRule(rsm);

		rsm = new RegexMatchRule(Category.DATE, NUMBER_COMPOSED_MAX_PRIORITY, true, mMatchMode);
		rsm.init(days + "/" + month + "/" + year + "(?![/\\p{L}\\p{Nd}])");
		addRule(rsm);

		rsm = new RegexMatchRule(Category.DATE, NUMBER_COMPOSED_MAX_PRIORITY, true, mMatchMode);
		rsm.init(days + "/" + month + "(?![/\\p{L}\\p{Nd}])");
		addRule(rsm);

		addRule(new NumberRangeMatchRule(Category.RANGE, NUMBER_COMPOSED_MAX_PRIORITY,
		        mMatchMode));

		rsm = new RegexMatchRule(Category.DATE, NUMBER_COMPOSED_MAX_PRIORITY, true, mMatchMode);
		rsm.init(year + "-" + month + "(?![-\\p{L}\\p{Nd}])");
		addRule(rsm);

		rsm = new RegexMatchRule(Category.DATE, NUMBER_COMPOSED_MAX_PRIORITY, true, mMatchMode);
		rsm.init(month + "/" + year + "(?![/\\p{L}\\p{Nd}])");
		addRule(rsm);

		// ==== TIME ====
		rsm = new RegexMatchRule(Category.TIME, NUMBER_COMPOSED_MAX_PRIORITY, true, mMatchMode);
		rsm.init("(2[0-4]|[01][0-9]):[0-6][0-9](?![0-9])");
		addRule(rsm);

		// TODO: more regexp for the time

		// ==== OTHER NUMBERS ====
		String integer = "([1-9]{1,3}([,' ][0-9]{3})+|[1-9][0-9]*)";
		String real = "(" + integer + "(\\.[0-9]+)?)";
		String currency = "([\\$€£₤¥]|usd|euro[s]?)";

		rsm = new RegexMatchRule(Category.DIMENSIONS, NUMBER_COMPOSED_MAX_PRIORITY, true,
		        mMatchMode);
		rsm.init(real + "(x|[ ]x[ ])" + real + "(?![\\p{L}\\p{Nd}])");
		addRule(rsm);

		rsm = new RegexMatchRule(Category.CURRENCY, NUMBER_COMPOSED_MAX_PRIORITY, false,
		        mMatchMode);
		rsm.init(real + currency + "(?![\\p{L}\\p{Nd}])");
		addRule(rsm);

		rsm = new RegexMatchRule(Category.CURRENCY, NUMBER_COMPOSED_MAX_PRIORITY, false,
		        mMatchMode);
		rsm.init(currency + real + "(?![\\p{L}\\p{Nd}])");
		addRule(rsm);

		rsm = new RegexMatchRule(Category.NUMBERING_ITEM, NUMBER_COMPOSED_MAX_PRIORITY, false,
		        mMatchMode);
		rsm.init("[0-9]+([-.][0-9]+)*\\." + "(?![\\p{L}\\p{Nd}])");
		addRule(rsm);

		rsm = new RegexMatchRule(Category.QUANTITY, NUMBER_MAX_PRIORITY, true, mMatchMode);
		rsm.init(real);
		addRule(rsm);

		rsm = new RegexMatchRule(Category.IDENTIFIER, NUMBER_MAX_PRIORITY - 1, true,
		        mMatchMode);
		rsm.init("[0-9]+([-_:][0-9]+)*" + "(?![\\p{L}\\p{Nd}])");
		addRule(rsm);

		// ==== SPACES ====
		rsm = new RegexMatchRule(Category.SPACE, SPACE_MAX_PRIORITY, true, mMatchMode);
		rsm.init("[" + Space + "]+");
		addRule(rsm);

		// ==== QUOTES ====
		rsm = new RegexMatchRule(Category.QUOTE, QUOTE_MAX_PRIORITY, true, mMatchMode);
		rsm.init("[\\p{Pf}\\p{Pi}\"']");
		addRule(rsm);

		// ==== SPECIAL STRINGS ====
		rsm = new RegexMatchRule(Category.WEB_LINK, WEBLINK_MAX_PRIORITY, true, mMatchMode);
		rsm.init("[a-z]+://[^" + Space + "]*");
		addRule(rsm);

		rsm = new RegexMatchRule(Category.WEB_LINK, WEBLINK_MAX_PRIORITY, true, mMatchMode);
		rsm.init("www\\.[^" + Space + "]+");
		addRule(rsm);

		rsm = new RegexMatchRule(Category.EMAIL_ADDR, WEBLINK_MAX_PRIORITY, true, mMatchMode);
		rsm.init("[\\p{L}][-_.\\p{L}\\p{Nd}]*(@|\\(at\\))[\\p{L}][-_.\\p{L}\\p{Nd}]*");
		addRule(rsm);

		// ==== ACRONYMS, INITIALISMS AND ABBREVIATIONS ====

		String acronymPrefix = "[\\p{L}]\\.([-]?[\\p{L}\\p{Nd}]\\.)+";

		// 2 or more characters acronyms terminated by a point (if the following
		// character is not a capital letter). Example:
		// The U.S. are ...
		// but not: ... to the U.S. As a consequence, ... 
		rsm = new RegexMatchRule(Category.ACRONYM, ACRONYM_MAX_PRIORITY, true, mMatchMode);
		rsm.init(acronymPrefix + "(?=[" + Space + "]+[\\p{Ll}])");
		addRule(rsm);

		// 3 or more characters acronyms not terminated by a point
		rsm = new RegexMatchRule(Category.ACRONYM, ACRONYM_MAX_PRIORITY, true, mMatchMode);
		rsm.init(acronymPrefix + "[\\p{L}\\p{Nd}]");
		addRule(rsm);

		// one-letter acronyms. There are a few cases where they shouldn't be recognized as
		//acronyms, such as 'he and I.', but those as not as frequent as acronyms and initialisms.
		//Examples: "R. Descartes", "B. IV" (B for Book)
		rsm = new RegexMatchRule(Category.ACRONYM, ACRONYM_MAX_PRIORITY, true, mMatchMode);
		rsm.init("[\\p{Lu}]\\.(?=[" + Space + "])");
		addRule(rsm);

		//plausible abbreviation
		rsm = new RegexMatchRule(Category.ABBREVIATION, ABBR_MAX_PRIORITY, true, mMatchMode);
		rsm.init("[\\p{Ll}]\\.(?=[" + Space + "])");
		addRule(rsm);

		// ==== COMMON WORDS ====
		rsm = new RegexMatchRule(Category.COMMON, COMMON_WORD_MAX_PRIORITY, true, mMatchMode);
		rsm.init(CommonWordPattern);
		addRule(rsm);

		// ==== DEFAULT ====
		rsm = new RegexMatchRule(Category.PUNCTUATION, LOWEST_PRIORITY, true, mMatchMode);
		rsm.init(".");
		addRule(rsm);
	}
}
