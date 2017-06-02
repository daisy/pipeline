package org.daisy.integration;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.daisy.pipeline.client.PipelineClient;
import org.daisy.pipeline.webservice.jaxb.job.Job;
import org.daisy.pipeline.webservice.jaxb.request.Input;
import org.daisy.pipeline.webservice.jaxb.request.Item;
import org.daisy.pipeline.webservice.jaxb.request.JobRequest;
import org.daisy.pipeline.webservice.jaxb.request.ObjectFactory;
import org.daisy.pipeline.webservice.jaxb.request.Priority;
import org.daisy.pipeline.webservice.jaxb.request.Script;
import org.daisy.pipeline.webservice.jaxb.script.Scripts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

public class Utils {
        public final static String SCRIPT="dtbook-to-zedai";
        public final static String SOURCE="hauy_valid.xml";
        public final static String NICE_NAME="NICE_NAME";
        private static final Logger logger = LoggerFactory.getLogger(Utils.class);

        public static PipelineLauncher startPipeline(PipelineClient client) throws IOException {
        
                //Just for testing this will disappear soon
                File path=new File(new File(System.getProperty("pipeline.path")),"bin");
                PipelineLauncher launcher=PipelineLauncher.newLauncher(path,client)
                        .setProperty("gosh.args","--noi");
                return launcher;
        }


        public static Optional<JobRequest> getJobRequest(PipelineClient client)
                        throws URISyntaxException {
              return getJobRequest(client,Priority.LOW);
        }
        public static Optional<JobRequest> getJobRequest(PipelineClient client,Priority priority)
                        throws URISyntaxException {
                File hauy=new File(Utils.class.getClassLoader().getResource("dtbook/hauy_valid.xml").toURI());
                return Utils.getJobRequest(client,priority,hauy.toURI().toString());
        }
        public static Optional<JobRequest> getJobRequest(PipelineClient client,Priority priority,String path)
                        throws URISyntaxException {
                ObjectFactory reqFactory= new ObjectFactory();
                JobRequest req=reqFactory.createJobRequest();
                Script script=reqFactory.createScript();

                Optional<String> href=Utils.getScriptHref(SCRIPT,client);
                if (!href.isPresent()){
                        return Optional.absent();
                }
                script.setHref(href.get());
                //set the source
                Item source = reqFactory.createItem();
                source.setValue(path);
                Input input = reqFactory.createInput();
                input.getItem().add(source);
                input.setName("source");
                //Nice name
                req.getScriptOrNicenameOrPriority().add(script);
                req.getScriptOrNicenameOrPriority()
                        .add(NICE_NAME);
                req.getScriptOrNicenameOrPriority().add(input);
                req.getScriptOrNicenameOrPriority().add(priority);
                return Optional.of(req);
        }

        public static Optional<String> getScriptHref(String name,PipelineClient client){
                Scripts scripts=client.scripts();
                for(org.daisy.pipeline.webservice.jaxb.script.Script s : scripts.getScript()){
                        if (s.getId().equals(name)){
                                return Optional.of(s.getHref());
                        }
                }
                return Optional.absent();
        }

        public static Job waitForStatusChange(String status, Job in, long timeout,
                        PipelineClient client) throws Exception {
                long waited=0L;
                Job job=in;
                logger.info(String.format("Waiting for status %s",status));
                while(job.getStatus().value()!=status){
                        if (job.getStatus().value()=="ERROR"){
                                throw new RuntimeException("Job errored while waiting for another status");
                        }
                
                        job=client.job(job.getId());
                        try {
                                Thread.sleep(500);                 
                                waited+=500;
                        } catch(InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                        }
                        if(waited>timeout){
                                throw new RuntimeException("waitForStatusChange timed out");
                        }
                }

                logger.info(String.format("After status %s",job.getStatus().value()));
                return job;


        }
        public static String logPath(String id){
                String path[]=new String[]{PipelineLauncher.getDataPath(),
                        "jobs",id,id+".log"};
                return Joiner.on(File.separator).join(path);
        }

        public static String jobPath(String id){
                String path[]=new String[]{PipelineLauncher.getDataPath(),
                       "jobs",id};
                return Joiner.on(File.separator).join(path);
        }
        
        public static PipelineClient getClient(){
                return new PipelineClient("http://localhost:8181/ws");
        }
        public static PipelineClient getClient(String id,String secret){
                return new PipelineClient("http://localhost:8181/ws",id,secret);
        }
        //use only when extrictly necessary
        public static void cleanUpDb() throws IOException {
           FileUtils.deleteDirectory(new File(new File(PipelineLauncher.getDataPath()),"db")); 
        }
}


