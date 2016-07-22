package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClient;

public class ClientFilter extends QueryDecorator<PersistentJob> {
        private Client client;
        private EntityManager em;

        public ClientFilter(EntityManager em,Client client) {
                super(em);
                this.client=client;

        }

        @Override
        Predicate getPredicate( QueryDecorator<PersistentJob>.QueryHolder holder) {
                Join<Job,JobContext> joinContext=holder.root.join(PersistentJob.MODEL_JOB_CONTEXT);
                Join<JobContext,Client> joinClient=joinContext.join(PersistentJobContext.MODEL_CLIENT);
                Predicate pred=holder.cb.equal(joinClient.get(PersistentClient.MODEL_CLIENT_ID),this.client.getId());

                return pred;
        }
        
}
