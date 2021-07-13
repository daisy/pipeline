package org.daisy.pipeline.braille.common.saxon.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.CharMatcher;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "pf:message",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class MessageDefinition extends ExtensionFunctionDefinition {
	
	private static final StructuredQName funcname = new StructuredQName("pf",
			"http://www.daisy.org/ns/pipeline/functions", "message");
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	@Override
	public int getMinimumNumberOfArguments() {
		return 2;
	}
	
	@Override
	public int getMaximumNumberOfArguments() {
		return 3;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.SINGLE_STRING,
			SequenceType.SINGLE_STRING,
			SequenceType.ATOMIC_SEQUENCE
		};
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.EMPTY_SEQUENCE;
	}
	
	private enum Level {
		ERROR,
		WARN,
		INFO,
		DEBUG,
		TRACE
	}
	
	private static final Sequence VOID = EmptySequence.getInstance();
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					Level level = Level.valueOf(arguments[0].head().getStringValue().toUpperCase());
					String msg = CharMatcher.WHITESPACE.trimAndCollapseFrom(
						arguments[1].head().getStringValue(), ' ');
					String[] args;
					if (arguments.length > 2)
						args = sequenceToArray(arguments[2]);
					else
						args = new String[]{};
					switch (level) {
					case ERROR:
						logger.error(msg, (Object[])args);
						break;
					case WARN:
						logger.warn(msg, (Object[])args);
						break;
					case INFO:
						logger.info(msg, (Object[])args);
						break;
					case DEBUG:
						logger.debug(msg, (Object[])args);
						break;
					case TRACE:
						logger.trace(msg, (Object[])args);
						break; }
					return VOID; }
				catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:message", e); }
			}
		};
	}
	
	private static String[] sequenceToArray(Sequence seq) throws XPathException {
		List<String> list = new ArrayList<String>();
		SequenceIterator iterator = seq.iterate();
		for (Item item = iterator.next(); item != null; item = iterator.next())
			list.add(item.getStringValue());
		return list.toArray(new String[list.size()]);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(MessageDefinition.class);
	
}
