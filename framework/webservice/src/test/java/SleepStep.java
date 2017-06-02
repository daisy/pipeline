import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.library.Identity;
import com.xmlcalabash.runtime.XAtomicStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SleepStep extends Identity {
	
	private static final Logger logger = LoggerFactory.getLogger(SleepStep.class);
	private static final QName _milliseconds = new QName("milliseconds");
		
	private SleepStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}

	@Override
	public void run() throws SaxonApiException {
		Long ms = Long.parseLong(getOption(_milliseconds).getString());
		logger.info("{}: going to sleep for {}ms...", this, ms);
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			throw new XProcException(step.getNode(), e);
		}
		logger.info("{}: woke up", this);
		super.run();
	}
	
	public static class Provider implements XProcStepProvider {
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new SleepStep(runtime, step);
		}
	}
}
