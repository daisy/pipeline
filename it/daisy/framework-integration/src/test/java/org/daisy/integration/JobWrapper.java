package org.daisy.integration;

import java.util.List;

import org.daisy.pipeline.webservice.jabx.job.Job;
import org.daisy.pipeline.webservice.jabx.job.Results;

public class JobWrapper {


        private Job job;

        /**
         * @param job
         */
        public JobWrapper(Job job) {
                this.job = job;
        }

        public Results getResults(){
                List<Object> elements=this.job.getNicenameOrScriptOrMessages();
                return (Results) elements.get(elements.size()-1);
        }
        
}
