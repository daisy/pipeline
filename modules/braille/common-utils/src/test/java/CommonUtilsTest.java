import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.inject.Inject;

import com.google.common.base.Optional;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;

import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.ops4j.pax.exam.util.PathUtils;

import org.osgi.framework.BundleContext;

import org.slf4j.Logger;

public class CommonUtilsTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.braille:braille-css:?",
			"org.daisy.dotify:dotify.api:?",
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
	
	@Inject
	private BundleContext context;
	
	@Before
	public void registerUppercaseTransformProvider() {
		UppercaseTransform.Provider provider = new UppercaseTransform.Provider();
		Hashtable<String,Object> properties = new Hashtable<String,Object>();
		context.registerService(BrailleTranslatorProvider.class.getName(), provider, properties);
		context.registerService(TransformProvider.class.getName(), provider, properties);
	}
	
	private static class UppercaseTransform extends AbstractBrailleTranslator implements BrailleTranslator {
		
		public FromStyledTextToBraille fromStyledTextToBraille() {
			return fromStyledTextToBraille;
		}
		
		private FromStyledTextToBraille fromStyledTextToBraille = new FromStyledTextToBraille() {
			public Iterable<String> transform(Iterable<CSSStyledText> styledText) {
				List<String> ret = new ArrayList<String>();
				for (CSSStyledText t : styledText)
					ret.add(t.getText().toUpperCase());
				return ret;
			}
		};
		
		private final URI href = asURI(new File(new File(PathUtils.getBaseDir()), "target/test-classes/uppercase.xpl"));
		
		@Override
		public XProc asXProc() {
			return new XProc(href, null, null);
		}
		
		private static final Iterable<UppercaseTransform> instance = Optional.of(new UppercaseTransform()).asSet();
		
		private static final Iterable<UppercaseTransform> empty = Optional.<UppercaseTransform>absent().asSet();
		
		public static class Provider implements BrailleTranslatorProvider<UppercaseTransform> {
			private Logger logger;
			public Provider() {}
			private Provider(Logger context) {
				logger = context;
			}
			public Iterable<UppercaseTransform> get(Query query) {
				if (query.toString().equals("(uppercase)")) {
					if (logger != null)
						logger.info("Selecting " + instance);
					return instance; }
				else
					return empty;
			}
			public TransformProvider<UppercaseTransform> withContext(Logger context) {
				return new Provider(context);
			}
		}
	}
	
	@Test
	public void testExtractHyphens() throws Exception {
		assertEquals("[0, 0, 1, 0, 0]", Arrays.toString(extractHyphens("foo\u00ADbar", '\u00AD')._2));
		assertEquals("[0, 0, 0, 2, 0, 0]", Arrays.toString(extractHyphens("foo-\u200Bbar", null, '\u200B')._2));
	}
}
