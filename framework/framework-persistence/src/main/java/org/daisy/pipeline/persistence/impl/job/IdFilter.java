package org.daisy.pipeline.persistence.impl.job;

import javax.persistence.EntityManager;
import javax.persistence.criteria.Predicate;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.IdentifiableType;
import javax.persistence.metamodel.SingularAttribute;

import org.daisy.pipeline.job.JobId;

public class IdFilter extends QueryDecorator<PersistentJob> {

        private JobId id;  
        private SingularAttribute<? super PersistentJob,String> idAttribute;

        public IdFilter(EntityManager em,JobId id) {
                super(em);
                this.id=id;
                //get the PersistentJob id attribute from the metamodel
                EntityType<PersistentJob> entity= this.em.getMetamodel().entity(PersistentJob.class);
                this.idAttribute=((IdentifiableType<PersistentJob>)entity).getId(String.class);
        }

        @Override
        Predicate getPredicate(QueryDecorator<PersistentJob>.QueryHolder holder) {
                //check that id is equal
                Predicate idFilter=holder.cb.equal(holder.root.get(this.idAttribute),id.toString());
                return idFilter;
        }
        
}
