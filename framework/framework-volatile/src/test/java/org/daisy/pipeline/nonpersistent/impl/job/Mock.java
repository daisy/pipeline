package org.daisy.pipeline.nonpersistent.impl.job;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.script.BoundXProcScript;

public class Mock {

	static class MockedJobContext extends AbstractJobContext {

		public MockedJobContext(Client cl,JobId id, JobBatchId batchId,String niceName,
				BoundXProcScript boundScript, URIMapper mapper) {
			super(cl,id, batchId,niceName, boundScript, mapper);
			// TODO Auto-generated constructor stub
		}
	}


	public static JobContext newJobContext(Client cl){
		return new MockedJobContext(cl,JobIdFactory.newId(),null,null,null,null);
	}
	public static JobContext newJobContext(Client cl,JobBatchId batchId){
		return new MockedJobContext(cl,JobIdFactory.newId(),batchId,null,null,null);
	}
	
}
