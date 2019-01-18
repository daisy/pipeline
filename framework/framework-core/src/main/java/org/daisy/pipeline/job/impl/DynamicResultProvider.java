package org.daisy.pipeline.job.impl;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

import org.daisy.pipeline.job.URIMapper;

/**
 * This class is not thread-safe if several threads are generating results at the same time.
 * not that likely use case
 */
public final class DynamicResultProvider implements Supplier<Result>{

	private final List<Result> providedResults= Lists.newLinkedList();
	private final Supplier<Result> backingProvider;
	private final String portName;
	private final String portMimetype;
	private final URIMapper mapper;
	
	private String constantSystemId = null;
	private String prefix = null;
	private String suffix = null;
	
	public DynamicResultProvider(Supplier<Result> backingProvider, String portName, String portMimetype, URIMapper mapper) {
		this.backingProvider = backingProvider;
		this.portName = portName;
		this.portMimetype = portMimetype;
		this.mapper = mapper;
	};

	/**
	 * The results returned by this method will not support setting the systemId and a expcetion will be thrown
	 */
	@Override
	public Result get() {
		Result res = null;
		int count = providedResults.size();
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
				String parts[] = URITranslatorHelper.getDynamicResultProviderParts(portName, constantSystemId, portMimetype);
				prefix = mapper.mapOutput(URI.create(parts[0])).toString();
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
	
}
