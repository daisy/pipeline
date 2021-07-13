import java.math.BigDecimal;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.library.Identity;
import com.xmlcalabash.runtime.XAtomicStep;

import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaStep extends Identity implements XProcStep {
	
	private static final Logger logger = LoggerFactory.getLogger(JavaStep.class);
	private static final QName _throw_error = new QName("throw-error");
	private static final QName _show_progress = new QName("show-progress");
	
	private JavaStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}

	@Override
	public void run() throws SaxonApiException {
		if (getOption(_show_progress, "false").equals("true")) {
			MessageAppender activeBlock = MessageAppender.getActiveBlock();
			activeBlock.append(
				new MessageBuilder().withLevel(Level.INFO)
				                    .withText("px:java-step (1)")
				                    .withProgress(new BigDecimal(.5))
			).close();
			
			// sleep so that there is enough time to get an intermediate state
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			activeBlock.append(
				new MessageBuilder().withLevel(Level.INFO)
				                    .withText("px:java-step (2)")
				                    .withProgress(new BigDecimal(.5))
			).close();
		}
		if (getOption(_throw_error, "false").equals("true")) {
			logger.info("going to throw an exception");
			throw XProcException.fromException(new RuntimeException("foobar"))
			                    .rebase(step.getLocation(),
			                            new RuntimeException().getStackTrace());
		}
		super.run();
	}
	
	@Component(
		name = "px:java-step",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}java-step" }
	)
	public static class Provider implements XProcStepProvider {
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new JavaStep(runtime, step);
		}
	}
}
