package org.daisy.pipeline.webservice.restlet.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.daisy.common.priority.Priority;
import org.daisy.common.spi.CreateOnStart;
import org.daisy.common.spi.ServiceLoader;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.WebserviceStorage;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobManagerFactory;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.webservice.CallbackHandler;
import org.daisy.pipeline.webservice.PipelineWebServiceConfiguration;
import org.daisy.pipeline.webservice.Properties;
import org.daisy.pipeline.webservice.restlet.WebServiceExtension;
import org.daisy.pipeline.webservice.Routes;
import org.daisy.pipeline.webservice.impl.PushNotifier;
import org.daisy.pipeline.webservice.impl.VolatileWebserviceStorage;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.Variable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.launch.Framework;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

// FIXME: For some reason, without OSGi, this class does not support stopping an instance and then
// starting another.

/**
 * The Class PipelineWebService.
 */
@org.osgi.service.component.annotations.Component(
    name = "org.daisy.pipeline.webservice",
    immediate = true,
    service = { PipelineWebService.class } // this is to ensure object created by SPIHelper.createWebService()
                                           // is an instance of PipelineWebService
)
public class PipelineWebService extends Application {

        /** The logger. */
        private static Logger logger = LoggerFactory.getLogger(PipelineWebService.class.getName());
        
        public static final String KEY_FILE_NAME="dp2key.txt";
        PipelineWebServiceConfiguration conf = new PipelineWebServiceConfiguration();
        
        /** The job manager. */
        private JobManagerFactory jobManagerFactory;

        /** The script registry. */
        private ScriptRegistry scriptRegistry;

        private WebserviceStorage webserviceStorage = null;
        private PushNotifier pushNotifier = null;

        private long shutDownKey=0L;

        private Component component;

        private DatatypeRegistry datatypeRegistry;

        private List<WebServiceExtension> extensions = null;

        /* (non-Javadoc)
         * @see org.restlet.Application#createInboundRoot()
         */
        @Override
        public Restlet createInboundRoot() {
                Router router = new Router(getContext());
                router.attach(Routes.SCRIPTS_ROUTE, ScriptsResource.class);
                router.attach(Routes.SCRIPT_ROUTE, ScriptResource.class);
                router.attach(Routes.JOBS_ROUTE, JobsResource.class);
                router.attach(Routes.JOB_ROUTE, JobResource.class);
                router.attach(Routes.BATCH_ROUTE, JobBatchResource.class);
                router.attach(Routes.JOB_CONF_ROUTE, JobConfigurationResource.class);
                router.attach(Routes.LOG_ROUTE, LogResource.class);
                router.attach(Routes.RESULT_ROUTE, ResultResource.class);
                router.attach(Routes.RESULT_OPTION_ROUTE     , OptionResultResource.class);                       // kept for backward compatibility (but will result in error)
                //:comment This allows to have url-like elements in the idx part of the query   
                TemplateRoute route= router.attach(Routes.RESULT_OPTION_ROUTE_IDX , OptionResultResource.class);  // kept for backward compatibility (but will result in error)
                Map<String, Variable> routeVariables = route.getTemplate().getVariables();
                routeVariables.put("idx", new Variable(Variable.TYPE_URI_ALL));

                
                router.attach(Routes.RESULT_PORT_ROUTE       , PortResultResource.class);
                //goto :comment
                route= router.attach(Routes.RESULT_PORT_ROUTE_IDX , PortResultResource.class);
                routeVariables = route.getTemplate().getVariables();
                routeVariables.put("idx", new Variable(Variable.TYPE_URI_ALL));
                router.attach(Routes.ALIVE_ROUTE,AliveResource.class);

                // init the administrative paths
                router.attach(Routes.CLIENTS_ROUTE, ClientsResource.class);
                router.attach(Routes.CLIENT_ROUTE, ClientResource.class);
                router.attach(Routes.PROPERTIES_ROUTE, PropertiesResource.class);
                router.attach(Routes.PROPERTY_ROUTE, PropertyResource.class);
                router.attach(Routes.HALT_ROUTE, HaltResource.class);
                router.attach(Routes.SIZES_ROUTE, SizesResource.class  );
                router.attach(Routes.QUEUE_ROUTE, QueueResource.class  );
                router.attach(Routes.QUEUE_UP_ROUTE, QueueUpResource.class  );
                router.attach(Routes.QUEUE_DOWN_ROUTE, QueueDownResource.class  );
                router.attach(Routes.DATATYPE_ROUTE, DatatypeResource.class);
                router.attach(Routes.DATATYPES_ROUTE, DatatypesResource.class);

                // attach extensions
                if (extensions != null)
                        for (WebServiceExtension extension : extensions)
                                extension.attachTo(router);

                return router;
        }

        /**
         * Inits the WS.
         */
        @Activate
        public void init() {
                if (webserviceStorage == null)
                        webserviceStorage = new VolatileWebserviceStorage();
                if (!checkAuthenticationSanity()){
                        try {
                                close();
                        } catch (Exception e) {
                                logger.error("Error shutting down:"+e.getMessage());
                        }
                        return;
                }
                //get rid of stale jobs
                this.cleanUp();
                Routes routes = new Routes();
                logger.info(String.format("Starting webservice on port %d",
                                routes.getPort()));
                component = new Component();
                if (!conf.isSsl()){
                        component.getServers().add(Protocol.HTTP, routes.getHost(),routes.getPort());
                        logger.debug("Using HTTP");
                }else{
                        Server server = component.getServers().add(Protocol.HTTPS, routes.getHost(),routes.getPort());
                        server.getContext().getParameters().add("keystorePath",conf.getSslKeystore()); 
                        server.getContext().getParameters().add("keystorePassword",conf.getSslKeystorePassword());
                        server.getContext().getParameters().add("keyPassword",conf.getSslKeyPassword());
                        logger.debug("Using HTTPS");
                }
                component.getDefaultHost().attach(routes.getPath(), this);
                this.setStatusService(new PipelineStatusService());
                try {
                        component.start();
                        logger.debug("component started");
                        generateStopKey();
                } catch (Exception e) {
                        logger.error("Shutting down the framework because of:"+e.getMessage());
                        try{
                                close();
                        }catch (Exception innerException) {
                                logger.error("Error shutting down:"+e.getMessage());
                        }
                }
                pushNotifier = new PushNotifier(jobManagerFactory);
        }

        private void cleanUp() {
                if(this.conf.getCleanUpOnStartUp()){
                        final JobManager manager = this.getJobManager(webserviceStorage.getClientStorage().defaultClient());
                        Iterable<Job> toClean = Iterables.filter(manager.getJobs(), new Predicate<Job>() {
                                @Override
                                public boolean apply(Job j) {
                                        return j.getStatus().equals(Job.Status.RUNNING) || 
                                j.getStatus().equals(Job.Status.IDLE);
                                }

                        });
                        for (Job j:toClean){
                                logger.info("Cleaning unfinished job "+j);
                                manager.deleteJob(j.getId());

                        }
                }
        }

        private boolean checkAuthenticationSanity() {
                if (this.conf.isAuthenticationEnabled()) {
                        //if the clientStore is empty close the 
                        //WS
                        if (webserviceStorage.getClientStorage().getAll().size()==0){
                                //no properties supplied
                                if (conf.getClientKey()==null || conf.getClientSecret()==null || conf.getClientKey().isEmpty()|| conf.getClientSecret().isEmpty()){
                                        //Making the error more eye catchy      
                                        logger.error("\n"
                                        +"\n"
                                        +"************************************************************\n"
                                        +"WS mode authenticated but the client store is empty, exiting\n"
                                        +"please provide values for the following properties:\n"
                                        +"-org.daisy.pipeline.ws.authentication.key    \n"
                                        +"-org.daisy.pipeline.ws.authentication.secret \n"
                                        +"************************************************************\n"
                                        +"\n"
                                        +"\n");
                                                return false;
                                }else{
                                        //new admin client via configuration properties
                                        logger.debug("Inserting new client: "+conf.getClientKey());
                                        this.webserviceStorage.getClientStorage().addClient(conf.getClientKey(),conf.getClientSecret(),Client.Role.ADMIN,"from configuration",Priority.HIGH);

                                }

                        }
                }
                return true;

        }

        private void generateStopKey() throws IOException {
                shutDownKey = new Random().nextLong();
                File fout = new File(System.getProperty("java.io.tmpdir")+File.separator+KEY_FILE_NAME);
                FileOutputStream fos= new FileOutputStream(fout);
                fos.write((shutDownKey+"").getBytes());
                fos.close();
                logger.info("Shutdown key stored to: "+System.getProperty("java.io.tmpdir")+File.separator+KEY_FILE_NAME);
        }

        public boolean shutDown(long key) {
                if(key==shutDownKey){
                        halt();
                        return true;
                }
                return false;
        }

        /* FIXME: depending on how the application is invoked, this may not be the desired effect of
         * calling halt */
        private void halt() {
                if (OSGiHelper.inOSGiContext())
                        OSGiHelper.stopFramework();
                else {
                        close();
                        System.exit(0);
                }
        }

        /**
         * Close.
         * @throws Exception
         * @throws Throwable
         */
        @Deactivate
        public void close() {
                try {
                        if (pushNotifier != null)
                                pushNotifier.close();
                        if (component != null)
                                component.stop();
                        stop();
                        logger.info("Webservice stopped.");
                } catch (Exception e) {
                        logger.error("Error stopping the web service:" + e.getMessage());
                }
        }

        /**
         * Add an extension
         */
        @Reference(
           name = "web-service-extension",
           unbind = "-",
           service = WebServiceExtension.class,
           cardinality = ReferenceCardinality.MULTIPLE,
           policy = ReferencePolicy.STATIC
        )
        public void addWebServiceExtension(WebServiceExtension extension) {
                if (extensions == null)
                        extensions = new ArrayList<>();
                extensions.add(extension);
        }

        /**
         * Gets the job manager.
         *
         * @return the job manager
         */
        public JobManager getJobManager(Client client) {
                return jobManagerFactory.createFor(client);
        }
        public JobManager getJobManager(Client client,JobBatchId batchId) {
                return jobManagerFactory.createFor(client,batchId);
        }

        /**
         * Sets the job manager.
         *
         * @param jobManager the new job manager
         */
        @Reference(
           name = "job-manager-factory",
           unbind = "-",
           service = JobManagerFactory.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setJobManagerFactory(JobManagerFactory jobManagerFactory) {
                this.jobManagerFactory = jobManagerFactory;
        }
        
        public PipelineWebServiceConfiguration getConfiguration(){
                return this.conf;       
        }

        /**
         * Gets the script registry.
         *
         * @return the script registry
         */
        public ScriptRegistry getScriptRegistry() {
                return scriptRegistry;
        }

        /**
         * Sets the script registry.
         *
         * @param scriptRegistry the new script registry
         */
        @Reference(
           name = "script-registry",
           unbind = "-",
           service = ScriptRegistry.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setScriptRegistry(ScriptRegistry scriptRegistry) {
                this.scriptRegistry = scriptRegistry;
        }

        /**
         * @return the webserviceStorage
         */
        public WebserviceStorage getStorage() {
                return webserviceStorage;
        }

        /**
         * @param webserviceStorage the webserviceStorage to set
         */
        @Reference(
           name = "webservice-storage",
           unbind = "-",
           service = WebserviceStorage.class,
           cardinality = ReferenceCardinality.OPTIONAL,
           policy = ReferencePolicy.STATIC
        )
        public void setWebserviceStorage(WebserviceStorage webserviceStorage) {
                this.webserviceStorage = webserviceStorage;
        }

        public CallbackHandler getCallbackHandler() {
                return pushNotifier;
        }

        @Reference(
           name = "datatype-registry",
           unbind = "-",
           service = DatatypeRegistry.class,
           cardinality = ReferenceCardinality.MANDATORY,
           policy = ReferencePolicy.STATIC
        )
        public void setDatatypeRegistry(DatatypeRegistry datatypeRegistry){
                this.datatypeRegistry=datatypeRegistry;
        }
        public DatatypeRegistry getDatatypeRegistry(){
                return this.datatypeRegistry;
        }

        /**
         * Main method to launch the web service.
         */
        public static void main(String[] args) {
                if (args.length > 0) {
                        logger.error("No arguments expected (got '" + String.join(" ", args) + "')");
                        System.exit(1);
                }
                PipelineWebService webservice = SPIHelper.createWebService();
                if (webservice == null)
                        System.exit(1);
                // Add shutdown hook to gracefully stop webservice because webservice.finalize()
                // is not automatically called (object is not garbage collected before shutdown).
                // Note that only the webservice is stopped gracefully, all the bound services
                // are terminated hard.
                Runtime.getRuntime().addShutdownHook(
                        new Thread() {
                                public void run() {
                                        webservice.close(); }} );
                System.err.println("Press Ctrl-C to exit");
                // program does not exit until last thread has finished
        }

        // static nested class in order to delay class loading
        private static abstract class OSGiHelper {

                static boolean inOSGiContext() {
                        try {
                                return FrameworkUtil.getBundle(OSGiHelper.class) != null;
                        } catch (NoClassDefFoundError e) {
                                return false;
                        }
                }

                /* Stop Felix */
                static void stopFramework() {
                        try {
                                ((Framework)FrameworkUtil.getBundle(OSGiHelper.class).getBundleContext().getBundle(0)).stop();
                        } catch (BundleException e) {
                                throw new RuntimeException(e);
                        }
                }
        }

        // static nested class in order to delay class loading
        private static abstract class SPIHelper {

                static PipelineWebService createWebService() {
                        PipelineWebService webservice = null;
                        for (CreateOnStart o : ServiceLoader.load(CreateOnStart.class))
                                if (webservice == null && o instanceof PipelineWebService)
                                        webservice = (PipelineWebService)o;
                        return webservice;
                }
        }
}
