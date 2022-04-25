package org.daisy.pipeline.braille.common.saxon.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslator.FromStyledTextToBraille;
import org.daisy.pipeline.braille.common.BrailleTranslatorRegistry;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import org.daisy.pipeline.braille.css.CSSStyledText;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "pf:text-transform",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class TextTransformDefinition extends ExtensionFunctionDefinition {
	
	private static final StructuredQName funcname = new StructuredQName("pf",
			"http://www.daisy.org/ns/pipeline/functions", "text-transform");
	
	@Reference(
		name = "BrailleTranslatorRegistry",
		unbind = "-",
		service = BrailleTranslatorRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindBrailleTranslatorRegistry(BrailleTranslatorRegistry registry) {
		translatorRegistry = registry;
		logger.debug("Binding BrailleTranslator registry: {}", registry);
	}
	
	private BrailleTranslatorRegistry translatorRegistry;
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	@Override
	public int getMinimumNumberOfArguments() {
		return 2;
	}
	
	@Override
	public int getMaximumNumberOfArguments() {
		return 4;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.SINGLE_STRING,
			SequenceType.ATOMIC_SEQUENCE,
			SequenceType.ATOMIC_SEQUENCE,
			SequenceType.ATOMIC_SEQUENCE
		};
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.ATOMIC_SEQUENCE;
	}
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					Query query = query(arguments[0].head().getStringValue());
					List<String> text = sequenceToList(arguments[1]);
					List<CSSStyledText> styledText = new ArrayList<CSSStyledText>();
					if (arguments.length > 2) {
						List<String> style = sequenceToList(arguments[2]);
						if (style.size() != text.size())
							throw new XPathException("Lengths of text and style sequences must match");
						if (arguments.length > 3) {
							List<String> lang = sequenceToList(arguments[3]);
							if (lang.size() != text.size())
								throw new XPathException("Lengths of text and lang sequences must match");
							for (int i = 0; i < text.size(); i++)
								styledText.add(new CSSStyledText(text.get(i), style.get(i), parseLocale(lang.get(i)))); }
						else
							for (int i = 0; i < text.size(); i++)
								styledText.add(new CSSStyledText(text.get(i), style.get(i))); }
					else
						for (int i = 0; i < text.size(); i++)
							styledText.add(new CSSStyledText(text.get(i)));
					for (BrailleTranslator t : translatorRegistry.get(query)) {
						FromStyledTextToBraille fsttb;
						try {
							fsttb = t.fromStyledTextToBraille(); }
						catch (UnsupportedOperationException e) {
							logger.trace("Translator does not implement the FromStyledTextToBraille interface: " + t);
							continue; }
						try {
							return iterableToSequence(fsttb.transform(styledText)); }
						catch (Exception e) {
							logger.debug("Failed to translate string with translator " + t);
							throw e; }
					}
					throw new XPathException("Could not find a BrailleTranslator for query: " + query); }
				catch (XPathException e) {
					throw e; }
				catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:text-transform", e); }
			}
		};
	}
	
	private static List<String> sequenceToList(Sequence seq) throws XPathException {
		List<String> list = new ArrayList<String>();
		SequenceIterator iterator = seq.iterate();
		for (Item item = iterator.next(); item != null; item = iterator.next())
			list.add(item.getStringValue());
		return list;
	}
	
	private static Sequence iterableToSequence(Iterable<String> iterable) {
		List<StringValue> list = new ArrayList<StringValue>();
		for (String s : iterable)
			list.add(new StringValue(s));
		return new SequenceExtent(list);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(TextTransformDefinition.class);
	
}
