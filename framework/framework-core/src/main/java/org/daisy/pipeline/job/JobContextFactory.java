package org.daisy.pipeline.job;

import java.io.IOException;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.job.impl.XProcDecorator;
import org.daisy.pipeline.script.BoundXProcScript;
import org.daisy.pipeline.script.XProcScript;

public class JobContextFactory {

        private final Client client;
        private final JobMonitorFactory monitorFactory;

        public JobContextFactory(Client client, JobMonitorFactory monitorFactory) {
                this.client = client;
                this.monitorFactory = monitorFactory;
        }

        public AbstractJobContext newJobContext(boolean mapping, String niceName, JobBatchId batchId, BoundXProcScript boundScript, JobResources collection) {
                try {
                        JobId id = JobIdFactory.newId();
                        if (niceName == null ||
                            boundScript == null ||
                            monitorFactory == null)
                                throw new IllegalArgumentException();
                        XProcScript script = boundScript.getScript();
                        XProcInput input = boundScript.getInput();
                        XProcOutput output = boundScript.getOutput();
                        URIMapper mapper;
                        XProcDecorator decorator;
                        if (mapping) {
                                mapper = JobURIUtils.newURIMapper(id.toString());
                                decorator = XProcDecorator.from(script, mapper, collection);
                        } else {
                                mapper = JobURIUtils.newOutputURIMapper(id.toString());
                                decorator = XProcDecorator.from(script, mapper);
                        }
                        input = decorator.decorate(input);
                        output = decorator.decorate(output);
                        return new AbstractJobContext(client, id, batchId, niceName, script, input, output, mapper, monitorFactory) {};
                } catch (IOException e){
                        throw new RuntimeException("Error while creating job context",e);
                }
        }
}
