package org.daisy.pipeline.common.calabash.impl;

import java.util.Map;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.model.Step;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkerStep extends DefaultStep implements XProcStep {
	
	@Component(
		name = "px:chunker",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}xml-chunker" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new ChunkerStep(runtime, step);
		}
	}
	
	private ReadablePipe sourcePipe = null;
	private WritablePipe resultPipe = null;
	private WritablePipe mappingPipe = null;
	
	private static final QName ALLOW_BREAK_BEFORE = new QName("allow-break-before");
	private static final QName ALLOW_BREAK_AFTER = new QName("allow-break-after");
	private static final QName PREFER_BREAK_BEFORE = new QName("prefer-break-before");
	private static final QName PREFER_BREAK_AFTER = new QName("prefer-break-after");
	private static final QName ALWAYS_BREAK_BEFORE = new QName("always-break-before");
	private static final QName ALWAYS_BREAK_AFTER = new QName("always-break-after");
	
	private static final QName PART_ATTRIBUTE = new QName("part-attribute");
	private static final QName PROPAGATE = new QName("propagate");
	private static final QName MAX_CHUNK_SIZE = new QName("max-chunk-size");
	
	private ChunkerStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		sourcePipe = pipe;
	}
	
	@Override
	public void setOutput(String port, WritablePipe pipe) {
		if ("result".equals(port)) {
			resultPipe = pipe;
		} else { // "mapping"
			mappingPipe = pipe;
		}
	}
	
	@Override
	public void reset() {
		sourcePipe.resetReader();
		resultPipe.resetWriter();
		mappingPipe.resetWriter();
	}
	
	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			RuntimeValue partAttribute = getOption(PART_ATTRIBUTE);
			new Chunker(
				getOption(ALLOW_BREAK_BEFORE),
				getOption(ALLOW_BREAK_AFTER),
				getOption(PREFER_BREAK_BEFORE),
				getOption(PREFER_BREAK_AFTER),
				getOption(ALWAYS_BREAK_BEFORE),
				getOption(ALWAYS_BREAK_AFTER),
				partAttribute != null ? SaxonHelper.jaxpQName(partAttribute.getQName()) : null,
				getOption(PROPAGATE, true),
				getOption(MAX_CHUNK_SIZE, -1),
				runtime.getProcessor().getUnderlyingConfiguration())
			.transform(
				new XMLCalabashInputValue(sourcePipe),
				new XMLCalabashOutputValue(
					new WritablePipe() {
						private int count = 0;
						public void write(XdmNode doc) {
							(count++ == 0 ? mappingPipe : resultPipe).write(doc);
						}
						public void canWriteSequence(boolean sequence) { throw new UnsupportedOperationException(); }
						public boolean writeSequence() { throw new UnsupportedOperationException(); }
						public void setWriter(Step step) { throw new UnsupportedOperationException(); }
						public void resetWriter() { throw new UnsupportedOperationException(); }
						public void close() { throw new UnsupportedOperationException(); }
					},
					runtime))
			.run();
		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ChunkerStep.class);
	
}
