package org.daisy.pipeline.job;

import java.io.Serializable;

public class JobBatchId {


        private String id;
        /**
         *
         */
        public JobBatchId(String id) {
                this.id=id;
        }

        /**
         * @return the id
         */
        public String toString() {
                return id;
        }
        
}
