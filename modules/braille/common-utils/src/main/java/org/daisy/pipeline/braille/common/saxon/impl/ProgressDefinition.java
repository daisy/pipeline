package org.daisy.pipeline.braille.common.saxon.impl;

import java.math.BigDecimal;

import static com.xmlcalabash.util.XProcMessageListenerHelper.parseNumber;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pf:progress",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class ProgressDefinition extends ExtensionFunctionDefinition {
	
	private static final StructuredQName funcname = new StructuredQName("pf",
			"http://www.daisy.org/ns/pipeline/functions", "progress");
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.SINGLE_STRING
		};
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.EMPTY_SEQUENCE;
	}
	
	private static final Sequence VOID = EmptySequence.getInstance();
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					BigDecimal portion = parseNumber(arguments[0].head().getStringValue());
					MessageAppender activeBlock = MessageAppender.getActiveBlock();
					MessageBuilder m = new MessageBuilder().withProgress(portion);
					// we need an active block otherwise we have no place to send the progress info to
					if (activeBlock != null)
						activeBlock.append(m).close();
					return VOID; }
				catch (ArithmeticException e) {
					// probably divided by null, ignore progress info
					return VOID; }
				catch (Throwable e) {
					throw new XPathException("Unexpected error in pf:progress", e); }
			}
		};
	}
}
