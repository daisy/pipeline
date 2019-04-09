package org.daisy.pipeline.persistence.impl.job;

import java.util.Iterator;

import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.JobBuilder;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobStorage;
import org.daisy.pipeline.job.RuntimeConfigurator;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.script.ScriptRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Collections2;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
    name = "persistent-job-storage",
    immediate = true,
    service = { JobStorage.class }
)
public class PersistentJobStorage implements JobStorage {
        private final static String STORE_MODE="javax.persistence.cache.storeMode";
        private static final Logger logger = LoggerFactory
                        .getLogger(PersistentJobStorage.class);

        private Database db;

        private RuntimeConfigurator configurator;

        private QueryDecorator<PersistentJob> filter;

        public PersistentJobStorage(){}

        PersistentJobStorage(Database db,QueryDecorator<PersistentJob> filter,
                        RuntimeConfigurator configurator){
                this.db=db;
                this.filter=filter;
                this.configurator=configurator;
        }

        @Reference(
           name = "entity-manager-factory",
           unbind = "-",
           service = EntityManagerFactory.class,
           target = "(osgi.unit.name=pipeline-pu)",
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setEntityManagerFactory(EntityManagerFactory emf) {
                this.db = new Database(emf);
                this.filter=QueryDecorator.empty(db.getEntityManager());
        }

        @Reference(
           name = "script-registry",
           unbind = "-",
           service = ScriptRegistry.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setRegistry(ScriptRegistry scriptRegistry) {
                PersistentJobContext.setScriptRegistry(scriptRegistry);
        }


        private void checkDatabase() {
                if (db == null) {
                        logger.warn("Database is null in persistent job storage");
                        throw new IllegalStateException("db is null");
                }
        }

        @Override
        public Iterator<Job> iterator() {
                checkDatabase();
                TypedQuery<PersistentJob> query=this.filter.getQuery(PersistentJob.class);
                //make sure that we have the data from the db, 
                query.setHint(STORE_MODE, CacheStoreMode.REFRESH);

                return Collections2.transform(query.getResultList(),
                                new Function<Job, Job>() {
                                        @Override
                                        public Job apply(Job job) {
                                                // set event bus and monitor
                                                PersistentJobStorage.this.configurator.configure(job);
                                                return job;
                                        }
                                }).iterator();
        }


        @Override
        public Optional<Job> add(Priority priority,JobContext ctxt) {
                checkDatabase();
                logger.debug("Adding job to db:" + ctxt.getId());
                JobBuilder builder = new PersistentJob.PersistentJobBuilder(db).withPriority(priority)
                                .withContext(ctxt);
                Job pjob = builder.build();
                // set event bus and monitor
                this.configurator.configure(pjob);
                return Optional.of(pjob);
        }

        @Override
        public Optional<Job> remove(JobId jobId) {
                checkDatabase();
                Optional<Job> stored=this.get(jobId);
                if (stored.isPresent()) {
                        db.deleteObject(stored.get());
                        logger.debug(String.format("Job with id %s deleted", jobId));
                }
                return stored;
        }

        @Override
        public Optional<Job> get(JobId id) {
                checkDatabase();
                IdFilter idFilter=new IdFilter(this.db.getEntityManager(),id);
                idFilter.setNext(this.filter);

                PersistentJob job = null;
                try{
                       job= idFilter.getQuery(PersistentJob.class).getSingleResult();
                }catch(NoResultException nre){
                        return Optional.absent();
                }

                if (job != null) {
                        job.setDatabase(db);
                        // set event bus and monitor
                        this.configurator.configure(job);
                }
                return Optional.fromNullable((Job)job);
        }

        @Override
        public JobStorage filterBy(Client client) {
                if (client.getRole()==Role.ADMIN){
                        return this;
                }else{
                        QueryDecorator<PersistentJob> byClient= new ClientFilter(this.db.getEntityManager(),client);
                        byClient.setNext(filter);
                        return new PersistentJobStorage(this.db,byClient,this.configurator);
                }
        }

        /**
         * @param configurator the configurator to set
         */
        @Reference(
           name = "runtime-configurator",
           unbind = "-",
           service = RuntimeConfigurator.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setConfigurator(RuntimeConfigurator configurator) {
                this.configurator = configurator;
        }

        @Override
        public JobStorage filterBy(JobBatchId id) {
                QueryDecorator<PersistentJob> byBatchId= new BatchFilter(this.db.getEntityManager(),id);
                byBatchId.setNext(filter);
                return new PersistentJobStorage(this.db,byBatchId,this.configurator);
        }
        
}
