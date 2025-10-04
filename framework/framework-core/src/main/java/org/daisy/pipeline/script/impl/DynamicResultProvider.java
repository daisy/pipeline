package org.daisy.pipeline.script.impl;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import org.daisy.pipeline.script.ScriptPort;

/**
 * This class is not thread-safe if several threads are generating results at the same time.
 * not that likely use case
 */
public final class DynamicResultProvider implements Supplier<Result>{

	private final List<Result> providedResults= Lists.newLinkedList();
	private final Supplier<Result> backingProvider;
	private final String portName;
	private final String portMimetype;
	private final URI resultDir;
	
	private String constantSystemId = null;
	private String prefix = null;
	private String suffix = null;
	
	public DynamicResultProvider(Supplier<Result> backingProvider, String portName, String portMimetype, File resultDir) {
		this.backingProvider = backingProvider;
		this.portName = portName;
		this.portMimetype = portMimetype;
		this.resultDir = resultDir.toURI();
	};

	/**
	 * The results returned by this method will not support setting the systemId and a expcetion will be thrown
	 */
	@Override
	public Result get() {
		Result res = null;
		int count = providedResults.size();
		/* Note that in practice backingProvider will always be null because the {@link
		 * BoundXProcScript} API doesn't allow specifying outputs anymore.
		 */
		if (backingProvider != null) {
			res = backingProvider.get();
			if (!(res instanceof StreamResult)) {
				String sysId = res.getSystemId();
				if (sysId == null || sysId.isEmpty())
					throw new IllegalArgumentException("Provided a result that is no StreamResult and has no systemId");
				if (constantSystemId != null && !constantSystemId.equals(sysId))
					throw new IllegalArgumentException("Provided a result with systemId ending in '/' " +
					                                   "after which a result with a different systemId was provided");
				if (count == 0) {
					constantSystemId = sysId;
				} else if (count == 1) {
					Result first = providedResults.get(0);
					if (!(first instanceof StreamResult) && sysId.equals(first.getSystemId()))
						constantSystemId = sysId;
				}
				if (constantSystemId != null)
					res = null;
			}
		}
		if (res == null) {
			if (prefix == null) {
				String parts[] = getDynamicResultProviderParts(portName, constantSystemId, portMimetype);
				prefix = resultDir.resolve(URI.create(parts[0])).toString();
				suffix = parts[1];
			}
			String sysId = count == 0 ?
				String.format("%s%s", prefix, suffix) :
				String.format("%s-%d%s", prefix, count, suffix);
			res = new DynamicResult(sysId);
		}
		if (!(res instanceof StreamResult)) {
			String s = res.getSystemId();
			for (Result r : providedResults)
				if (!(r instanceof StreamResult))
					if (s.equals(r.getSystemId()))
						throw new IllegalArgumentException("Provided a result with the same systemId as a previous result");
		}
		providedResults.add(res);
		return res;
	}

	public Collection<Result> providedResults(){
		return Collections.unmodifiableCollection(this.providedResults);	
	}

	private static class DynamicResult implements Result{
		private final String systemId;

		/**
		 * Constructs a new instance.
		 *
		 * @param systemId The systemId for this instance.
		 */
		public DynamicResult(String systemId) {
			this.systemId = systemId;
		}

		@Override
		public String getSystemId() {
			return systemId;
		}

		@Override
		public void setSystemId(String arg0) {
			throw new UnsupportedOperationException(String.format("%s does not support modifying the systemId",DynamicResult.class));
		}
	}

	/**
	 * Return the prefix (unmapped) at index 0 and suffix at index 1 for the a dynamic result
	 * provider based on the provider and the port info.
	 */
	static final String[] getDynamicResultProviderParts(String name, String systemId, String mimetype){
		String parts[] = null;
		// on the result/result.xml way
		if (systemId == null || systemId.isEmpty()) {
			parts = new String[]{String.format("%s/%s", name, name), ScriptPort.getFileExtension(mimetype)};
			// directory -> dir/name, .xml
			// the first part is the last char of the sysId
		} else if (systemId.charAt(systemId.length() - 1) == '/') {
			parts= new String[]{String.format("%s%s", systemId, name), ScriptPort.getFileExtension(mimetype)};
			// file name/name, (".???"|"")
		} else {
			String ext = "";
			String path = systemId;
			int idx;
			// get the extension if there is one
			if ((idx = path.lastIndexOf('.')) > -1)
				ext = path.substring(idx);
			// the path had a dot in the middle, it's not an extension
			if (ext.indexOf('/') > 0)
				ext = "";
			// there's extension so we divide
			// lastIndexOf(.) will never be -1
			if (!ext.isEmpty())
				path = path.substring(0, path.lastIndexOf('.'));
			parts = new String[]{path, ext};
		}
		return parts;
	}
}
