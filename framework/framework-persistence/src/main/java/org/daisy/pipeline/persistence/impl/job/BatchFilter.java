package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.SingularAttribute;

import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobBatchId;

public class BatchFilter extends QueryDecorator<PersistentJob> {

        private JobBatchId id;  
        private SingularAttribute<? super PersistentJob,String> idAttribute;

        public BatchFilter(EntityManager em,JobBatchId id) {
                super(em);
                this.id=id;
                //get the PersistentJob id attribute from the metamodel
                EntityType<PersistentJob> entity= this.em.getMetamodel().entity(PersistentJob.class);
                this.idAttribute=((IdentifiableType<PersistentJob>)entity).getId(String.class);
        }

        @Override
        Predicate getPredicate(QueryDecorator<PersistentJob>.QueryHolder holder) {
                Join<AbstractJob,AbstractJobContext> joinContext = holder.root.join(PersistentJob.MODEL_JOB_CONTEXT);
                Predicate pred=holder.cb.equal(joinContext.get("stringBatchId"),this.id.toString());

                return pred;
        }
        
}
