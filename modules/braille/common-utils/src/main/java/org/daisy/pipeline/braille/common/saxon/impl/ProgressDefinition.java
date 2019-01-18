package org.daisy.pipeline.braille.common.saxon.impl;

import java.math.BigDecimal;

import com.xmlcalabash.core.XProcException;
import static com.xmlcalabash.util.XProcMessageListenerHelper.parseNumber;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.EmptySequence;
import net.sf.saxon.value.SequenceType;

import org.daisy.pipeline.event.EventBusProvider;
import org.daisy.pipeline.event.ProgressMessage;
import org.daisy.pipeline.event.ProgressMessageBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Component(
	name = "pf:progress",
	service = { ExtensionFunctionDefinition.class }
)
@SuppressWarnings("serial")
public class ProgressDefinition extends ExtensionFunctionDefinition {
	
	private EventBusProvider eventBus;
	
	@Reference(
		name = "EventBusProvider",
		unbind = "-",
		service = EventBusProvider.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setEventBusProvider(EventBusProvider provider) {
		eventBus = provider;
	}
	
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
					ProgressMessage activeBlock = ProgressMessage.getActiveBlock();
					ProgressMessageBuilder m = new ProgressMessageBuilder().withProgress(portion);
					if (activeBlock != null)
						activeBlock.post(m).close();
					else {
						String jobId = MDC.get("jobid");
						if (jobId != null && !"default".equals(jobId)) {
							m.withJobId(jobId);
							eventBus.post(m).close();
						} else {
							// not in job context
							logger.debug("progress: " + portion);
						}
					}
					return VOID; }
				catch (ArithmeticException e) {
					// probably divided by null, ignore progress info
					return VOID; }
				catch (Exception e) {
					throw new XPathException("pf:progress failed", XProcException.javaError(e, 0)); }
			}
		};
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ProgressDefinition.class);
	
}
