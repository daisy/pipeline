package org.daisy.pipeline.persistence.impl.job;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobMonitorFactory;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClient;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClientStorage;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class persists job contexts.
 * The general idea is to write getters and setters (Access property)  when possible
 * to make proxification as transparent as possible.
 * Complex depenedencies of the context like ports, options, mapper
 * and results are wrapped in their own persistent objects so in this
 * case they're are persisted as fields. 
 * @author Javier Asensio Cubero capitan.cambio@gmail.com
 */
@Entity
@Table(name="job_contexts")
@Access(AccessType.FIELD)
public final class PersistentJobContext extends AbstractJobContext {
        public static final long serialVersionUID=1L;
        private static final Logger logger = LoggerFactory.getLogger(PersistentJobContext.class);

        private String scriptId = null;

        //embedded mapper
        @Embedded
        private PersistentMapper pMapper;
        @ManyToOne
        private PersistentClient pClient;
        public static final String MODEL_CLIENT = "pClient";

        @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
        @MapsId("job_id")
        private List<PersistentInputPort> inputPorts= new ArrayList<PersistentInputPort>();

        @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
        //@JoinColumn(name="job_id",referencedColumnName="job_id")
        @MapsId("job_id")
        private List<PersistentOption> options= new ArrayList<PersistentOption>();

        /**
         * Not used (only kept for backward compatibility).
         */
        @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
        @MapsId("job_id")
        //@JoinColumn(name="job_id",referencedColumnName="job_id")
        private List<PersistentParameter> parameters= new ArrayList<PersistentParameter>();

        @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
        @MapsId("job_id")
        //@JoinColumn(name="job_id",referencedColumnName="job_id")
        private List<PersistentPortResult> portResults= new ArrayList<PersistentPortResult>();

        /**
         * Not used (only kept for backward compatibility).
         */
        @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.EAGER)
        @MapsId("job_id")
        //@JoinColumn(name="job_id",referencedColumnName="job_id")
        private List<PersistentOptionResult> optionResults= new ArrayList<PersistentOptionResult>();

        PersistentJobContext(AbstractJobContext ctxt, PersistentClientStorage clientStorage) {
                super(ctxt);
                // Map complex objects to their Persistent representation
                logger.debug("coping the objects to the model ");
                this.pMapper = new PersistentMapper(this.uriMapper);
                this.inputPorts = ContextHydrator.dehydrateInputPorts(this.getId(), this.getScript(), this.input);
                this.options = ContextHydrator.dehydrateOptions(this.getId(), this.getScript(), this.input);
                if (this.getClient() instanceof PersistentClient)
                        this.pClient = (PersistentClient)this.getClient();
                else if (clientStorage == null)
                        throw new IllegalArgumentException("expected client to be of type PersistenceClient");
                else if (this.getClient() == Client.DEFAULT_ADMIN)
                        this.pClient = (PersistentClient)clientStorage.defaultClient();
                else
                        try {
                                this.pClient = (PersistentClient)clientStorage.get(this.getClient().getId()).get();
                        } catch (NoSuchElementException e) {
                                throw new IllegalArgumentException("client does not exist");
                        }
                //everything is inmutable but this
                this.updateResults();
        }

        @SuppressWarnings("unused") // used by jpa
        private PersistentJobContext() {
                super();
        }

        /**
         * Although we could delegate the actual hydration
         * to setters (i.e. getInput) the performance would be affected.
         * Therefore we prefer doing hydration on the PostLoad event.
         */
        @PostLoad
        @SuppressWarnings("unused")//jpa only
        private void postLoad(){
                logger.debug("Post loading jobcontext");
                //we have all the model but we have to hidrate the actual objects
                this.uriMapper = this.pMapper.getMapper();
                File contextDir = null; {
                        URI u = uriMapper.getInputBase();
                        if (u != null && !"".equals(u.toString())) {
                                try {
                                        contextDir = new File(u);
                                } catch (IllegalArgumentException e) {
                                        throw new IllegalStateException("Not a directory: " + u, e);
                                }
                        }
                }
                ScriptInput.Builder builder = contextDir != null ? new ScriptInput.Builder(contextDir)
                                                                 : new ScriptInput.Builder();
                try {
                        ContextHydrator.hydrateInputPorts(builder,inputPorts);
                } catch (FileNotFoundException e) {
                        throw new IllegalStateException("Input files missing", e);
                }
                ContextHydrator.hydrateOptions(builder,options);
                this.input = builder.build();

                this.client = this.pClient;

                JobResultSet.Builder rBuilder=new JobResultSet.Builder();
                ContextHydrator.hydrateResultPorts(rBuilder,portResults);
                this.results = rBuilder.build();
        }

        void updateResults() {
                if(this.portResults.size()==0)
                        this.portResults=ContextHydrator.dehydratePortResults(this);
        }

        @Column(name="job_id")
        @Id
        @Access(AccessType.PROPERTY)
        private String getStringId() {
                return this.getId().toString();
        }

        @SuppressWarnings("unused") //used by jpa
        private void setStringId(String sId) {
                this.id = JobIdFactory.newIdFromString(sId);
        }

        public static final String MODEL_BATCH_ID = "batch_id";
        @Column(name=MODEL_BATCH_ID)
        @Access(AccessType.PROPERTY)
        private String getStringBatchId() {
                return batchId != null ? batchId.toString() : null;
        }

        @SuppressWarnings("unused") //used by jpa
        private void setStringBatchId(String batchId) {
                if(batchId!=null){
                        this.batchId = JobIdFactory.newBatchIdFromString(batchId);
                }
        }

        @Column(name="log_file")
        @Access(AccessType.PROPERTY)
        private String getStringLogFile() {
                if(super.getLogFile()==null)
                        return "";
                return super.getLogFile().toString();
        }

        @SuppressWarnings("unused") //used by jpa
        private void setStringLogFile(String logFile) {
                this.logFile = URI.create(logFile);
        }

        @Column(name="script_id")
        @Access(AccessType.PROPERTY)
        private String getScriptId() {
                if (scriptId != null) {
                        return scriptId;
                } else if (this.getScript() != null) {
                        return this.getScript().getId();
                } else {
                        throw new IllegalStateException("Script is null");
                }
        }

        @SuppressWarnings("unused") //used by jpa
        private void setScriptId(String id) {
                scriptId = id;
        }

        @Column(name="nice_name")
        @Access(AccessType.PROPERTY)
        @Override
        public String getName() {
                return super.getName();
        }

        @SuppressWarnings("unused") // used by jpa
        private void setName(String Name) {
                this.niceName = Name;
        }

        void finalize(ScriptRegistry registry, JobMonitorFactory monitorFactory) {
                if (registry != null) {
                        ScriptService<?> service = registry.getScript(scriptId);
                        if (service == null)
                                throw new IllegalStateException(
                                        String.format("Illegal state for recovering XProcScript %s: registry %s",
                                                      scriptId, registry));
                        script = service.load();
                        logger.debug(String.format("load script %s", script));
                }
                if (monitorFactory != null)
                        monitor = monitorFactory.newJobMonitor(id);
        }

        // for unit tests

        ScriptInput getInput() {
                return input;
        }

        URIMapper getResultMapper() {
                return uriMapper;
        }
}
