package org.daisy.pipeline.braille.common.saxon.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Provider;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;

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
		name = "BrailleTranslatorProvider",
		unbind = "unbindBrailleTranslatorProvider",
		service = BrailleTranslatorProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	@SuppressWarnings(
		"unchecked" // safe cast to BrailleTranslatorProvider<BrailleTranslator>
	)
	protected void bindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
		providers.add((BrailleTranslatorProvider<BrailleTranslator>)provider);
		logger.debug("Adding BrailleTranslator provider: {}", provider);
	}
	
	protected void unbindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
		providers.remove(provider);
		translators.invalidateCache();
		logger.debug("Removing BrailleTranslator provider: {}", provider);
	}
	
	private List<BrailleTranslatorProvider<BrailleTranslator>> providers
	= new ArrayList<BrailleTranslatorProvider<BrailleTranslator>>();
	
	private Provider.util.MemoizingProvider<Query,BrailleTranslator> translators
	= memoize(dispatch(providers));
	
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
							throw new RuntimeException("Lengths of text and style sequences must match");
						if (arguments.length > 3) {
							List<String> lang = sequenceToList(arguments[3]);
							if (lang.size() != text.size())
								throw new RuntimeException("Lengths of text and lang sequences must match");
							for (int i = 0; i < text.size(); i++) {
								Map<String,String> attrs = new HashMap<String,String>();
								attrs.put("lang", lang.get(i));
								styledText.add(new CSSStyledText(text.get(i), style.get(i), attrs)); }}
						else
							for (int i = 0; i < text.size(); i++)
								styledText.add(new CSSStyledText(text.get(i), style.get(i))); }
					else
						for (int i = 0; i < text.size(); i++)
							styledText.add(new CSSStyledText(text.get(i)));
					for (BrailleTranslator t : translators.get(query))
						try {
							return iterableToSequence(t.fromStyledTextToBraille().transform(styledText)); }
						catch (UnsupportedOperationException e) {}
					throw new RuntimeException("Could not find a BrailleTranslator for query: " + query); }
				catch (Exception e) {
					logger.error("pf:text-transform failed", e);
					throw new XPathException("pf:text-transform failed"); }
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
