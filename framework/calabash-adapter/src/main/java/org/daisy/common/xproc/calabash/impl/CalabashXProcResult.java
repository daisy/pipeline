package org.daisy.common.xproc.calabash.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;

import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcResult;

import com.google.common.base.Supplier;
import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.runtime.XPipeline;

/**
 * Implementation of the XProcResult interface
 */
public final class CalabashXProcResult implements XProcResult {

	/** The xpipeline. */
	private final XPipeline xpipeline;

	/** The configuration. */
	private final XProcConfiguration configuration;

	/** The message accessor. */


	/**
	 * creates a new XProcResult instance
	 *
	 * @param xpipeline the xpipeline whose results where be processed by this object
	 * @param configuration the pipeline config object
	 * @param accessor Allows to access the messages produced during the pipeline execution;
	 * @return the x proc result
	 */
	static XProcResult newInstance(XPipeline xpipeline,XProcConfiguration configuration) {
		return new CalabashXProcResult(xpipeline,configuration);
	}

	/**
	 * Instantiates a new calabash x proc result.
	 *
	 * @param xpipeline the xpipeline
	 * @param configuration the configuration
	 * @param accessor the accessor
	 */
	private CalabashXProcResult(XPipeline xpipeline,XProcConfiguration configuration) {
		this.xpipeline = xpipeline;
		this.configuration = configuration;

	}

	/* (non-Javadoc)
	 * @see org.daisy.common.xproc.XProcResult#writeTo(org.daisy.common.xproc.XProcOutput)
	 */
	@Override
	public void writeTo(XProcOutput output) {
		if(xpipeline.getOutputs() != null)
			for (String port : xpipeline.getOutputs()) {

				Supplier<Result> resultProvider = output.getResultProvider(port);

				ReadablePipe rpipe = xpipeline.readFrom(port);
				while (rpipe.moreDocuments()) {
					Serializer serializer = SerializationUtils.newSerializer(
							xpipeline.getSerialization(port), configuration);
					Result result = resultProvider.get();
					if (result instanceof StreamResult) {
						StreamResult streamResult = (StreamResult) result;
						serializer.setOutputStream(streamResult.getOutputStream());
					} else {
						URI uri = null;
						try {
							uri = new URI(result.getSystemId());
						} catch (URISyntaxException e) {
							throw new RuntimeException(String.format("Malformed uri while writing results: %s",result.getSystemId()),e);
						}
						if ("file".equals(uri.getScheme())) {
							serializer.setOutputFile(new File(uri));
						} else {
							URL url;
							try {
								url = uri.toURL();
								final URLConnection conn = url.openConnection();
								conn.setDoOutput(true);
								serializer.setOutputStream(conn.getOutputStream());
							} catch (MalformedURLException e) {
								throw new RuntimeException(String.format("Malformed url while writing results: %s",uri),e);
							} catch (IOException e) {
								throw new RuntimeException("IOError caught when writing results",e);
							}
						}
					}
					try {
						serializer.serializeNode(rpipe.read());
					} catch (SaxonApiException e) {
						throw new RuntimeException("Error caught when writing results",e);
					}
				}
			}
	}



}
