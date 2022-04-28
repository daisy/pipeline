import javax.xml.transform.SourceLocator;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

import org.daisy.common.messaging.MessageAppender;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "pf:java-function",
	service = { ExtensionFunctionDefinition.class }
)
public class JavaFunction extends ExtensionFunctionDefinition {
	
	private static final StructuredQName funcname = new StructuredQName("pf",
			"http://www.daisy.org/ns/pipeline/functions", "java-function");
	
	@Override
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	@Override
	public int getMinimumNumberOfArguments() {
		return 0;
	}
	
	@Override
	public int getMaximumNumberOfArguments() {
		return 0;
	}
	
	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {};
	}
	
	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_STRING;
	}
	
	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					MessageAppender activeBlock = MessageAppender.getActiveBlock();
					if (activeBlock != null) {
						activeBlock.asLogger().info("inside pf:java-function");
						logger.info("going to throw an exception");
					}
					throw new RuntimeException("foobar");
				} catch (Throwable e) {
					XPathException xe = new XPathException("Unexpected error in " + funcname.getClarkName(), e);
					xe.setErrorCodeQName(new StructuredQName("", "", "MYERR"));
					throw xe;
				}
			}
		};
	}
	
	private static final Logger logger = LoggerFactory.getLogger(JavaFunction.class);
	
}
