import java.math.BigDecimal;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XProcStepProvider;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.library.Identity;
import com.xmlcalabash.runtime.XAtomicStep;

import org.daisy.common.messaging.Message.Level;

import org.daisy.pipeline.event.ProgressMessage;
import org.daisy.pipeline.event.ProgressMessageBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaStep extends Identity {
	
	private static final Logger logger = LoggerFactory.getLogger(JavaStep.class);
	private static final QName _throw_error = new QName("throw-error");
	private static final QName _show_progress = new QName("show-progress");
	
	private JavaStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}

	@Override
	public void run() throws SaxonApiException {
		if (getOption(_show_progress, "false").equals("true")) {
			ProgressMessage activeBlock = ProgressMessage.getActiveBlock();
			activeBlock.post(
				new ProgressMessageBuilder().withLevel(Level.INFO)
				                            .withText("px:java-step (1)")
				                            .withProgress(new BigDecimal(.5))
			).close();
			
			// sleep so that there is enough time to get an intermediate state
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			activeBlock.post(
				new ProgressMessageBuilder().withLevel(Level.INFO)
				                            .withText("px:java-step (2)")
				                            .withProgress(new BigDecimal(.5))
			).close();
		}
		if (getOption(_throw_error, "false").equals("true")) {
			logger.info("going to throw an exception");
			throw new RuntimeException("foobar");
		}
		super.run();
	}
	
	public static class Provider implements XProcStepProvider {
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new JavaStep(runtime, step);
		}
	}
}
