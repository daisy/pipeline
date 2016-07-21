package org.daisy.pipeline.braille.liblouis.saxon.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.AtomicSequence;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.Provider;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import org.daisy.pipeline.braille.liblouis.LiblouisHyphenator;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "louis:hyphenate",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class HyphenateDefinition extends ExtensionFunctionDefinition {
	
	private static final StructuredQName funcname = new StructuredQName("louis",
			"http://liblouis.org/liblouis", "hyphenate");
	
	@Reference(
		name = "LiblouisHyphenatorProvider",
		unbind = "unbindLiblouisHyphenatorProvider",
		service = LiblouisHyphenator.Provider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void bindLiblouisHyphenatorProvider(LiblouisHyphenator.Provider provider) {
		providers.add(provider);
		logger.debug("Adding LiblouisHyphenator provider: {}", provider);
	}
	
	protected void unbindLiblouisHyphenatorProvider(LiblouisHyphenator.Provider provider) {
		providers.remove(provider);
		hyphenators.invalidateCache();
		logger.debug("Removing LiblouisHyphenator provider: {}", provider);
	}
	
	private List<TransformProvider<LiblouisHyphenator>> providers
	= new ArrayList<TransformProvider<LiblouisHyphenator>>();
	private Provider.util.MemoizingProvider<Query,LiblouisHyphenator> hyphenators
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
		return 2;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
				SequenceType.SINGLE_STRING,
				SequenceType.SINGLE_STRING};
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_STRING;
	}
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					Query query = query(((AtomicSequence)arguments[0]).getStringValue());
					String[] text = sequenceToArray(arguments[1]);
					Hyphenator hyphenator;
					try {
						hyphenator = hyphenators.get(query).iterator().next(); }
					catch (NoSuchElementException e) {
						throw new RuntimeException("Could not find a hyphenator for query: " + query); }
					return arrayToSequence(hyphenator.asFullHyphenator().transform(text));}
				catch (Exception e) {
					logger.error("louis:hyphenate failed", e);
					throw new XPathException("louis:hyphenate failed"); }
			}
		};
	}
	
	private static String[] sequenceToArray(Sequence seq) throws XPathException {
		List<String> list = new ArrayList<String>();
		for (SequenceIterator<?> i = seq.iterate(); i.next() != null;)
			list.add(i.current().getStringValue());
		return list.toArray(new String[list.size()]);
	}
	
	private static Sequence arrayToSequence(String[] array) {
		List<StringValue> list = new ArrayList<StringValue>();
		for (String s : array)
			list.add(new StringValue(s));
		return new SequenceExtent<StringValue>(list);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(HyphenateDefinition.class);
}
