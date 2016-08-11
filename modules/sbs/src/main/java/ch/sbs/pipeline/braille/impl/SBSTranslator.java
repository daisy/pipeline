package ch.sbs.pipeline.braille.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Lists.newArrayList;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;

import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.concat;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.TransformProvider.util.memoize;
import static org.daisy.pipeline.braille.common.util.Files.unpack;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import org.daisy.pipeline.braille.libhyphen.LibhyphenHyphenator;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;

public interface SBSTranslator {
	
	@Component(
		name = "ch.sbs.pipeline.braille.impl.SBSTranslator.Provider",
		service = {
			BrailleTranslatorProvider.class,
			TransformProvider.class
		}
	)
	public class Provider extends AbstractTransformProvider<BrailleTranslator> implements BrailleTranslatorProvider<BrailleTranslator> {
		
		private URI href;
		private Query grade0Table;
		private Query grade1Table;
		private Query grade2Table;
		private URI virtualDisTable;
		
		@Activate
		private void activate(ComponentContext context, final Map<?,?> properties) {
			href = asURI(context.getBundleContext().getBundle().getEntry("xml/block-translate.xpl"));
			File f = new File(makeUnpackDir(context), "virtual.dis");
			unpack(context.getBundleContext().getBundle().getEntry("/liblouis/virtual.dis"), f);
			virtualDisTable = asURI(f);
			grade0Table = mutableQuery().add("liblouis-table", virtualDisTable +
				",http://www.sbs.ch/pipeline/liblouis/tables/" +
				"sbs.dis,sbs-de-core6.cti,sbs-de-accents.cti,sbs-special.cti,sbs-numsign.mod," +
				"sbs-litdigit-upper.mod,sbs-de-core.mod,sbs-de-g0-core.mod,sbs-de-hyph-none.mod,sbs-de-accents-ch.mod," +
				"sbs-special.mod");
			grade1Table = mutableQuery().add("liblouis-table", virtualDisTable +
				",http://www.sbs.ch/pipeline/liblouis/tables/" +
				"sbs.dis,sbs-de-core6.cti,sbs-de-accents.cti,sbs-special.cti,sbs-numsign.mod," +
				"sbs-litdigit-upper.mod,sbs-de-core.mod,sbs-de-g0-core.mod,sbs-de-g1-white.mod,sbs-de-g1-core.mod," +
				"sbs-de-hyph-none.mod,sbs-de-accents-ch.mod,sbs-special.mod");
			grade2Table = mutableQuery().add("liblouis-table", virtualDisTable +
				",http://www.sbs.ch/pipeline/liblouis/tables/" +
				"sbs.dis,sbs-de-core6.cti,sbs-de-accents.cti,sbs-special.cti,sbs-de-letsign.mod," +
				"sbs-numsign.mod,sbs-litdigit-upper.mod,sbs-de-core.mod,sbs-de-g2-white.mod,sbs-de-g2-core.mod," +
				"sbs-de-hyph-none.mod,sbs-de-accents-ch.mod,sbs-special.mod");
		}
		
		private final static Query hyphenTable = mutableQuery().add("libhyphen-table", "http://www.sbs.ch/pipeline/hyphen/hyph_de_DE.dic");
		
		private final static Iterable<BrailleTranslator> empty = Iterables.<BrailleTranslator>empty();
		
		private final static List<String> supportedInput = ImmutableList.of("css","text-css","dtbook","html");
		private final static List<String> supportedOutput = ImmutableList.of("css","braille");
		
		/**
		 * Recognized features:
		 *
		 * - translator: Will only match if the value is `sbs'.
		 * - locale: Will only match if the language subtag is 'de'.
		 * - grade: `0', `1' or `2'.
		 *
		 */
		protected final Iterable<BrailleTranslator> _get(Query query) {
			final MutableQuery q = mutableQuery(query);
			for (Feature f : q.removeAll("input"))
				if (!supportedInput.contains(f.getValue().get()))
					return empty;
			for (Feature f : q.removeAll("output"))
				if (!supportedOutput.contains(f.getValue().get()))
					return empty;
			if (q.containsKey("locale"))
				if (!"de".equals(parseLocale(q.removeOnly("locale").getValue().get()).getLanguage()))
					return empty;
			if (q.containsKey("translator"))
				if ("sbs".equals(q.removeOnly("translator").getValue().get()))
					if (q.containsKey("grade")) {
						String v = q.removeOnly("grade").getValue().get();
						final int grade;
						if (v.equals("0"))
							grade = 0;
						else if (v.equals("1"))
							grade = 1;
						else if (v.equals("2"))
							grade = 2;
						else
							return empty;
						if (q.isEmpty()) {
							Iterable<LibhyphenHyphenator> hyphenators = logSelect(hyphenTable, libhyphenHyphenatorProvider);
							final Query liblouisTable = grade == 2 ? grade2Table : grade == 1 ? grade1Table : grade0Table;
							return concat(
								transform(
									hyphenators,
									new Function<LibhyphenHyphenator,Iterable<BrailleTranslator>>() {
										public Iterable<BrailleTranslator> _apply(final LibhyphenHyphenator h) {
											final Query hyphenatorQuery = mutableQuery().add("hyphenator", h.getIdentifier());
											final Query translatorQuery = mutableQuery(liblouisTable).addAll(hyphenatorQuery);
											return Iterables.transform(
												logSelect(translatorQuery, liblouisTranslatorProvider),
												new Function<LiblouisTranslator,BrailleTranslator>() {
													public BrailleTranslator _apply(LiblouisTranslator translator) {
														return __apply(logCreate(new TransformImpl(grade, translator, hyphenatorQuery.toString()))); }}); }})); }}
			return empty;
		}
		
		private final static Pattern PRINT_PAGE_NUMBER = Pattern.compile("(?<first>[0-9]+)?(?:/(?<last>[0-9]+))?");
		private final static Pattern NUMBER = Pattern.compile("[0-9]+");
		private final static String PRINT_PAGE_NUMBER_SIGN = "⠸⠼";
		private final static String NUMBER_SIGN = "⠼";
		private final static String[] UPPER_DIGIT_TABLE = new String[]{"⠚","⠁","⠃","⠉","⠙","⠑","⠋","⠛","⠓","⠊"};
		private final static String[] LOWER_DIGIT_TABLE = new String[]{"⠴","⠂","⠆","⠒","⠲","⠢","⠖","⠶","⠦","⠔"};

		private class TransformImpl extends AbstractBrailleTranslator {
			
			private final XProc xproc;
			private final int grade;
			private final FromStyledTextToBraille translator;
			
			private TransformImpl(int grade, LiblouisTranslator translator, String hyphenatorQuery) {
				Map<String,String> options = ImmutableMap.of(
					"contraction-grade", ""+grade,
					"virtual.dis-uri", virtualDisTable.toASCIIString(),
					"hyphenator", hyphenatorQuery);
				xproc = new XProc(href, null, options);
				this.grade = grade;
				this.translator = translator.fromStyledTextToBraille();
			}
			
			@Override
			public XProc asXProc() {
				return xproc;
			}
			
			@Override
			public FromStyledTextToBraille fromStyledTextToBraille() {
				return fromStyledTextToBraille;
			}

			private void failIfOtherStyleAttached(SimpleInlineStyle style, TermList values) {
				style.removeProperty("hyphens");
				if (values.size() > 1 || style.size() > 1)
					throw new RuntimeException("Translator does not support '" + style +"'");
			}

			private final FromStyledTextToBraille fromStyledTextToBraille = new FromStyledTextToBraille() {
				public java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText) {
					if (size(styledText) == 1) {
						CSSStyledText s = styledText.iterator().next();
						SimpleInlineStyle style = s.getStyle();
						if (style != null) {
							CSSProperty val = style.getProperty("text-transform");
							if (val != null) {
								if (val == TextTransform.list_values) {
									TermList values = style.getValue(TermList.class, "text-transform");
									for (Term<?> t: values) {
										String tt = ((TermIdent)t).getValue();
										if (tt.equals("print-page")) {
											failIfOtherStyleAttached(style, values);
											return Optional.of(translatePrintPageNumber(s.getText())).asSet(); }
										else if (tt.equals("toc-page")) {
											failIfOtherStyleAttached(style, values);
											return Optional.of(translateBraillePageNumberInToc(s.getText())).asSet(); }
										else if (tt.equals(" toc-print-page")) {
											failIfOtherStyleAttached(style, values);
											return Optional.of(translatePrintPageNumberInToc(s.getText())).asSet(); }
										else if (tt.equals("volume")) {
											failIfOtherStyleAttached(style, values);
											return translateVolumeNumber(s.getText()); }
										else if (tt.equals("volumes")) {
											failIfOtherStyleAttached(style, values);
											return translateVolumesCount(s.getText()); }}}}}}
					return translator.transform(styledText);
				}
			};

			private String translatePrintPageNumber(String number) {
				Matcher m = PRINT_PAGE_NUMBER.matcher(number.replaceAll("\u200B",""));
				if (!m.matches())
					throw new RuntimeException("'" + number + "' is not a valid print page number or print page number range");
				StringBuilder b = new StringBuilder();
				b.append(PRINT_PAGE_NUMBER_SIGN);
				String first = m.group("first");
				String last = m.group("last");
				// TODO: warning if first == null
				if (first != null) {
					b.append(translateNaturalNumber(Integer.parseInt(first)));
					if (last != null)
						b.append(translateNaturalNumber(Integer.parseInt(last), true)); }
				else if (last != null)
					b.append(translateNaturalNumber(Integer.parseInt(last)));
				return b.toString();
			}
			
			private String translatePrintPageNumberInToc(String number) {
				StringBuilder b = new StringBuilder();
				b.append(NUMBER_SIGN);
				b.append(translateNaturalNumber(Integer.parseInt(number), true));
				return b.toString();
			}

			private String translateBraillePageNumberInToc(String number) {
				return translateNaturalNumber(Integer.parseInt(number));
			}

			private java.lang.Iterable<String> translateVolumeNumber(String number) {
				Matcher m = NUMBER.matcher(number);
				String ret;
				if (!m.matches())
					throw new RuntimeException("'" + number + "' is not a valid volume number");
				switch (number) {
				case "1":
					ret = "Erster";
				    break;
				case "2":
				    ret = "Zweiter";
				    break;
				case "3":
				    ret = "Dritter";
				    break;
				case "4":
				    ret = "Vierter";
				    break;
				case "5":
				    ret = "Fünfter";
				    break;
				case "6":
				    ret = "Sechster";
				    break;
				case "7":
				    ret = "Siebter";
				    break;
				case "8":
				    ret = "Achter";
				    break;
				case "9":
				    ret = "Neunter";
				    break;
				case "10":
				    ret = "Zehnter";
				    break;
				case "11":
				    ret = "Elfter";
				    break;
				case "12":
				    ret = "Zwölfter";
				    break;
				default:
				    return Optional.of(translateNaturalNumber(Integer.parseInt(number))).asSet();
				}
				return translator.transform(Optional.of(new CSSStyledText(ret)).asSet());
			}
			
			private java.lang.Iterable<String> translateVolumesCount(String number) {
				Matcher m = NUMBER.matcher(number);
				if (!m.matches())
					throw new RuntimeException("'" + number + "' is not a valid number");
				String ret;
				switch (number) {
				case "1":
				    ret = "Einem";
				    break;
				case "2":
				    ret = "Zwei";
				    break;
				case "3":
				    ret = "Drei";
				    break;
				case "4":
				    ret = "Vier";
				    break;
				case "5":
				    ret = "Fünf";
				    break;
				case "6":
				    ret = "Sechs";
				    break;
				case "7":
				    ret = "Sieben";
				    break;
				case "8":
				    ret = "Acht";
				    break;
				case "9":
				    ret = "Neun";
				    break;
				case "10":
				    ret = "Zehn";
				    break;
				case "11":
				    ret = "Elf";
				    break;
				case "12":
				    ret = "Zwölf";
				    break;
				default:
				    return Optional.of(translateNaturalNumber(Integer.parseInt(number))).asSet();
				}
				return translator.transform(Optional.of(new CSSStyledText(ret)).asSet());
			}
			
			private String translateNaturalNumber(int number) {
				return translateNaturalNumber(number, false);
			}
			
			private String translateNaturalNumber(int number, boolean downshift) {
				StringBuilder b = new StringBuilder();
				String[] table = downshift ? LOWER_DIGIT_TABLE : UPPER_DIGIT_TABLE;
				if (number == 0)
					b.append(table[0]);
				while (number > 0) {
					b.insert(0, table[number % 10]);
					number = number / 10; }
				return b.toString();
			}
			
			@Override
			public String toString() {
				return Objects.toStringHelper(SBSTranslator.class.getSimpleName())
					.add("grade", grade)
					.toString();
			}
		}
		
		@Reference(
			name = "LiblouisTranslatorProvider",
			unbind = "unbindLiblouisTranslatorProvider",
			service = LiblouisTranslator.Provider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		protected void bindLiblouisTranslatorProvider(LiblouisTranslator.Provider provider) {
			liblouisTranslatorProviders.add(provider);
		}
	
		protected void unbindLiblouisTranslatorProvider(LiblouisTranslator.Provider provider) {
			liblouisTranslatorProviders.remove(provider);
			liblouisTranslatorProvider.invalidateCache();
		}
	
		private List<TransformProvider<LiblouisTranslator>> liblouisTranslatorProviders
		= new ArrayList<TransformProvider<LiblouisTranslator>>();
		private TransformProvider.util.MemoizingProvider<LiblouisTranslator> liblouisTranslatorProvider
		= memoize(dispatch(liblouisTranslatorProviders));
		
		@Reference(
			name = "LibhyphenHyphenatorProvider",
			unbind = "unbindLibhyphenHyphenatorProvider",
			service = LibhyphenHyphenator.Provider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		protected void bindLibhyphenHyphenatorProvider(LibhyphenHyphenator.Provider provider) {
			libhyphenHyphenatorProviders.add(provider);
		}
	
		protected void unbindLibhyphenHyphenatorProvider(LibhyphenHyphenator.Provider provider) {
			libhyphenHyphenatorProviders.remove(provider);
			libhyphenHyphenatorProvider.invalidateCache();
		}
	
		private List<TransformProvider<LibhyphenHyphenator>> libhyphenHyphenatorProviders
		= new ArrayList<TransformProvider<LibhyphenHyphenator>>();
		private TransformProvider.util.MemoizingProvider<LibhyphenHyphenator> libhyphenHyphenatorProvider
		= memoize(dispatch(libhyphenHyphenatorProviders));
		
		private static File makeUnpackDir(ComponentContext context) {
			File directory;
			for (int i = 0; true; i++) {
				directory = context.getBundleContext().getDataFile("resources" + i);
				if (!directory.exists()) break; }
			directory.mkdirs();
			return directory;
		}
	}
}
