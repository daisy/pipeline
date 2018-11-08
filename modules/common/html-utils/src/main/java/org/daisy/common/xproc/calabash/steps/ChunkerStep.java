package org.daisy.common.xproc.calabash.steps;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.calabash.XMLCalabashHelper;
import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkerStep extends DefaultStep {
	
	@Component(
		name = "px:chunker",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}chunker" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new ChunkerStep(runtime, step);
		}
	}
	
	private ReadablePipe sourcePipe = null;
	private WritablePipe resultPipe = null;
	
	private static final QName ALLOW_BREAK_BEFORE = new QName("allow-break-before");
	private static final QName ALLOW_BREAK_AFTER = new QName("allow-break-after");
	private static final QName PREFER_BREAK_BEFORE = new QName("prefer-break-before");
	private static final QName PREFER_BREAK_AFTER = new QName("prefer-break-after");
	private static final QName ALWAYS_BREAK_BEFORE = new QName("always-break-before");
	private static final QName ALWAYS_BREAK_AFTER = new QName("always-break-after");
	
	private static final QName MAX_CHUNK_SIZE = new QName("max-chunk-size");
	private static final QName LINK_ATTRIBUTE_NAME = new QName("link-attribute-name");
	private static final QName DEFAULT_LINK_ATTRIBUTE_NAME = new QName("href");
	
	private ChunkerStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		sourcePipe = pipe;
	}
	
	@Override
	public void setOutput(String port, WritablePipe pipe) {
		resultPipe = pipe;
	}
	
	@Override
	public void reset() {
		sourcePipe.resetReader();
		resultPipe.resetWriter();
	}
	
	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			Configuration configuration = runtime.getProcessor().getUnderlyingConfiguration();
			XMLCalabashHelper.transform(
				new Chunker(getOption(ALLOW_BREAK_BEFORE),
				            getOption(ALLOW_BREAK_AFTER),
				            getOption(PREFER_BREAK_BEFORE),
				            getOption(PREFER_BREAK_AFTER),
				            getOption(ALWAYS_BREAK_BEFORE),
				            getOption(ALWAYS_BREAK_AFTER),
				            getOption(MAX_CHUNK_SIZE, -1),
				            SaxonHelper.jaxpQName(getOption(LINK_ATTRIBUTE_NAME, DEFAULT_LINK_ATTRIBUTE_NAME)),
				            configuration),
				sourcePipe,
				resultPipe,
				runtime);
		} catch (Exception e) {
			logger.error("px:chunker failed", e);
			throw new XProcException(step.getNode(), e);
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ChunkerStep.class);
	
}
