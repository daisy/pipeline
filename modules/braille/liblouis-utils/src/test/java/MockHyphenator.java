import java.util.Locale;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

import org.daisy.pipeline.braille.common.AbstractHyphenator;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultFullHyphenator;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.HyphenatorProvider;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;
import org.daisy.pipeline.braille.css.CSSStyledText;

import org.osgi.service.component.annotations.Component;

public class MockHyphenator extends AbstractHyphenator {
	
	private static final LineBreaker lineBreaker = new DefaultLineBreaker() {
		protected Break breakWord(String word, Locale language, int limit, boolean force) {
			if (limit >= 3 && word.equals("foobarz"))
				return new Break("fubbarz", 3, true);
			else if (limit >= word.length())
				return new Break(word, word.length(), false);
			else if (force)
				return new Break(word, limit, false);
			else
				return new Break(word, 0, false);
		}
	};
	
	private final static char SHY = '\u00AD';
	private final static char ZWSP = '\u200B';
	
	private final FullHyphenator fullHyphenator = new DefaultFullHyphenator() {
		protected boolean isCodePointAware() { return false; }
		protected boolean isLanguageAdaptive() { return false; }
		protected byte[] getHyphenationOpportunities(String textWithoutHyphens, Locale language) throws NonStandardHyphenationException {
			if (textWithoutHyphens.contains("foobarz"))
				throw new NonStandardHyphenationException();
			return extractHyphens(textWithoutHyphens.replace("foobar", "foo\u00ADbar"), true, SHY, ZWSP)._2;
		}
	};
	
	@Override
	public FullHyphenator asFullHyphenator() {
		return fullHyphenator;
	}
	
	@Override
	public LineBreaker asLineBreaker() {
		return lineBreaker;
	}
	
	private static final MockHyphenator instance = new MockHyphenator() {
		@Override
		public String getIdentifier() {
			return "mock";
		}
	};
	
	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper("MockHyphenator");
	}
	
	@Component(
		name = "mock-hyphenator-provider",
		service = { HyphenatorProvider.class }
	)
	public static class Provider extends AbstractTransformProvider<MockHyphenator>
	                             implements HyphenatorProvider<MockHyphenator> {
		{
			get(query("(hyphenator:mock)")); // in order to save instance in id-based cache
		}
		public Iterable<MockHyphenator> _get(Query query) {
			if (!query.containsKey("hyphenator") || !"mock".equals(query.getOnly("hyphenator").getValue().get()))
				return AbstractTransformProvider.util.Iterables.<MockHyphenator>empty();
			return AbstractTransformProvider.util.Iterables.<MockHyphenator>of(instance);
		}
	}
}
