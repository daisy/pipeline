package org.daisy.pipeline.job.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.xml.transform.Result;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

/**
 * This class is not thread-safe if several threads are generating results at the same time.
 * not that likely use case
 */
public final class DynamicResultProvider implements Supplier<Result>{

	private final String prefix;
	private final String suffix;
	private int count=0;
	private final List<Result> providedResults= Lists.newLinkedList();

	public DynamicResultProvider(String prefix,String suffix){
		this.prefix=prefix;
		this.suffix=suffix;
	};

	/**
	 * The results returned by this method will not support setting the systemId and a expcetion will be thrown
	 */
	@Override
	public Result get() {
		String sysId=null;
		if ( count==0){
			sysId=String.format("%s%s",prefix,suffix);
		}else{
			sysId=String.format("%s-%d%s",prefix,count,suffix);
		}
		count++;
		DynamicResult res= new DynamicResult(sysId);
		this.providedResults.add(res);
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
