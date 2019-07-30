package org.daisy.pipeline.file.calabash.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.file.URIs;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

import org.osgi.service.component.annotations.Component;

/**
 * This steps allows to copy any resource from a URI to a file in the file system usign a file scheme URI 
 */
@Component(
	name = "pxi:copy-resource",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}copy-resource" }
)
public class CopyResourceProvider implements XProcStepProvider {

	/** The logger. */
	Logger logger = LoggerFactory.getLogger(this.getClass());

	/* (non-Javadoc)
	 * @see org.daisy.common.xproc.calabash.XProcStepProvider#newStep(com.xmlcalabash.core.XProcRuntime, com.xmlcalabash.runtime.XAtomicStep)
	 */
	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new CopyResource(runtime, step);
	}

	/**
	 * Activate (OSGI)
	 */
	public void activate() {
		logger.trace("Activating px:copy-resource provider");
	}
	
	/**
	 * Actual implementation of the CopyResource step
	 *
	 *
	 */
	public static class CopyResource extends DefaultStep {
		private static final QName _href = new QName("href");
		private static final QName _target = new QName("target");
		private static final QName _fail_on_error = new QName("fail-on-error");
		private static final int bufsize = 8192;

		private WritablePipe result = null;

		/**
		 * Creates a new instance of CopyResource 
		 */
		public CopyResource(XProcRuntime runtime, XAtomicStep step) {
			super(runtime,step);
		}

		public void setOutput(String port, WritablePipe pipe) {
			result = pipe;
		}

		public void reset() {
			result.resetWriter();
		}

		public void run() throws SaxonApiException {
			super.run();

			if (runtime.getSafeMode()) {
				throw XProcException.dynamicError(21);
			}

			boolean failOnError = getOption(_fail_on_error, true);

			// Note that href.getBaseURI() always returns the absolute path of copy-resource.xpl
			RuntimeValue href = getOption(_href);
			URI sourceUri= URIs.resolve(href.getBaseURI(), href.getString());

			href = getOption(_target);
			URI destUri = URIs.resolve(href.getBaseURI(), href.getString());

			File target=this.getFile(destUri,sourceUri);

			TreeWriter tree = new TreeWriter(runtime);
			tree.startDocument(step.getNode().getBaseURI());
			tree.addStartElement(XProcConstants.c_result);
			tree.startContent();

			tree.addText(target.toURI().toASCIIString());

			this.copy(sourceUri,target);

			tree.addEndElement();
			tree.endDocument();

			result.write(tree.getResult());
		}

		File getFile(URI destUri,URI sourceUri){
			File target=null;
			if (!"file".equals(destUri.getScheme())) {
				throw new XProcException(step.getNode(), "Only file: scheme URIs are supported by taget.");
			} else {
				target = new File(destUri.getPath());
			}

			if (target.isDirectory()) {
				int idx=sourceUri.getPath().lastIndexOf('/');
				if(idx>-1){
					target = new File(target, sourceUri.getPath().substring(idx));
				}else{
					target = new File(target, sourceUri.getPath());
				}
				if (target.isDirectory()) {
					throw new XProcException(step.getNode(), "Cannot copy: target is a directory: " + target.getAbsolutePath());
				}
			}
			return target;

		}

		void copy(URI sourceUri,File target){

			try {
				InputStream src = sourceUri.toURL().openStream();
				FileOutputStream dst = new FileOutputStream(target);
				ByteStreams.copy(src,dst);
				src.close();
				dst.close();
			} catch (FileNotFoundException fnfe) {
				throw new XProcException(fnfe);
			} catch (IOException ioe) {
				throw new XProcException(ioe);
			}
		}
	}

}

