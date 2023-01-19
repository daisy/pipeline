package org.daisy.pipeline.persistence.impl.job;

import java.util.Iterator;

import javax.persistence.CacheStoreMode;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.daisy.common.priority.Priority;
import org.daisy.common.properties.Properties;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.event.MessageStorage;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobMonitorFactory;
import org.daisy.pipeline.job.JobStorage;
import org.daisy.pipeline.job.JobURIUtils;
import org.daisy.pipeline.persistence.impl.Database;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClientStorage;
import org.daisy.pipeline.persistence.impl.messaging.PersistentMessageStorage;
import org.daisy.pipeline.persistence.impl.webservice.PersistentWebserviceStorage;
import org.daisy.pipeline.script.ScriptRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.Collections2;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
    name = "persistent-job-storage",
    service = { JobStorage.class }
)
public class PersistentJobStorage implements JobStorage {

        private static final boolean PERSISTENCE_DISABLED = "false".equalsIgnoreCase(
                    Properties.getProperty("org.daisy.pipeline.persistence"));
        private final static String STORE_MODE="javax.persistence.cache.storeMode";
        private static final Logger logger = LoggerFactory
                        .getLogger(PersistentJobStorage.class);

        private Database db;
        private ScriptRegistry scriptRegistry;
        private PersistentClientStorage clientStorage;
        private final PersistentMessageStorage messageStorage;
        private boolean messageStorageAccessed = false;

        /**
         * The {@link JobMonitorFactory} that will be used to recreate {@link AbstractJob} objects
         * if needed (in the {@link #get()} and {@link #iterator()} methods).
         */
        private JobMonitorFactory jobMonitorFactory;

        private QueryDecorator<PersistentJob> filter;

        public PersistentJobStorage() {
                messageStorage = new PersistentMessageStorage();
                // make sure that the org.daisy.pipeline.data property is set
                try {
                        JobURIUtils.assertFrameworkDataDirPersisted();
                } catch (Exception e) {
                        throw new IllegalStateException("'org.daisy.pipeline.data' property is not set, can not persist jobs", e);
                }
        }

        private PersistentJobStorage(PersistentJobStorage template) {
                this.db = template.db;
                this.filter = template.filter;
                this.messageStorage = template.messageStorage;
                this.messageStorageAccessed = template.messageStorageAccessed;
                this.jobMonitorFactory = template.jobMonitorFactory;
        }

        @Reference(
           name = "persistent-client-storage",
           unbind = "-",
           service = PersistentWebserviceStorage.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setClientStorage(PersistentWebserviceStorage storage) {
                this.clientStorage = (PersistentClientStorage)storage.getClientStorage();
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
                db = new Database(emf);
                filter = QueryDecorator.empty(db.getEntityManager());
                messageStorage.setEntityManagerFactory(emf);
        }

        @Reference(
           name = "script-registry",
           unbind = "-",
           service = ScriptRegistry.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setRegistry(ScriptRegistry scriptRegistry) {
                this.scriptRegistry = scriptRegistry;
        }

        /**
         * @throws RuntimeException if persistent storage is disabled through the org.daisy.pipeline.persistence system property.
         */
        @Activate
        protected void activate() throws RuntimeException {
                if (PERSISTENCE_DISABLED)
                        throw new RuntimeException("Persistent storage is disabled");
                jobMonitorFactory = new JobMonitorFactory(this);
        }

        private void checkDatabase() {
                if (db == null) {
                        logger.warn("Database is null in persistent job storage");
                        throw new IllegalStateException("db is null");
                }
        }

        @Override
        public Iterator<AbstractJob> iterator() {
                checkDatabase();
                TypedQuery<PersistentJob> query=this.filter.getQuery(PersistentJob.class);
                //make sure that we have the data from the db, 
                query.setHint(STORE_MODE, CacheStoreMode.REFRESH);
                return Collections2.transform(
                    query.getResultList(),
                    job -> {
                        // finalize job context (set JobMonitor and XProcScript)
                        job.getContext().finalize(scriptRegistry, jobMonitorFactory);
                        return (AbstractJob)job;
                    }
                ).iterator();
        }


        @Override
        public Optional<AbstractJob> add(AbstractJob job) {
                checkDatabase();
                logger.debug("Adding job to db:" + job.getId());
                return Optional.of(new PersistentJob(db, job, clientStorage));
        }

        @Override
        public Optional<AbstractJob> remove(JobId jobId) {
                checkDatabase();
                Optional<AbstractJob> stored=this.get(jobId);
                if (stored.isPresent()) {
                        db.deleteObject(stored.get());
                        logger.debug(String.format("Job with id %s deleted", jobId));
                }
                if (messageStorageAccessed)
                        messageStorage.remove(jobId.toString());
                return stored;
        }

        @Override
        public Optional<AbstractJob> get(JobId id) {
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
                        // finalize job context (set JobMonitor and XProcScript)
                        job.getContext().finalize(scriptRegistry, jobMonitorFactory);
                }
                return Optional.fromNullable(job);
        }

        @Override
        public JobStorage filterBy(Client client) {
                if (client.getRole()==Role.ADMIN) {
                        return this;
                } else {
                        PersistentJobStorage copy = new PersistentJobStorage(this);
                        QueryDecorator<PersistentJob> byClient = new ClientFilter(this.db.getEntityManager(), client);
                        byClient.setNext(filter);
                        copy.filter = byClient;
                        return copy;
                }
        }

        @Override
        public JobStorage filterBy(JobBatchId id) {
                PersistentJobStorage copy = new PersistentJobStorage(this);
                QueryDecorator<PersistentJob> byBatchId = new BatchFilter(this.db.getEntityManager(), id);
                byBatchId.setNext(filter);
                copy.filter = byBatchId;
                return copy;
        }

        @Override
        public MessageStorage getMessageStorage() {
                messageStorageAccessed = true;
                return messageStorage;
        }
}
