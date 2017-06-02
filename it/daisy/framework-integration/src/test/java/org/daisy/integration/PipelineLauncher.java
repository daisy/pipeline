package org.daisy.integration;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.daisy.pipeline.client.PipelineClient;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.io.Files;


public class PipelineLauncher {
        public static final String NIX_LAUNCHER = "pipeline2";
        public static final String WIN_LAUNCHER = "pipeline2.bat";
        private HashMap<String, String> env = new HashMap<String, String>();
        private List<String> opts= Lists.newLinkedList();
        private File path;
        private PipelineClient client;
        private Process process;
        private static final Logger logger = LoggerFactory.getLogger(PipelineLauncher.class);


        public static String getDataPath(){
                return System.getProperty("pipeline.path")+File.separator+"data";
        }
        /**
         * @param path
         */
        private PipelineLauncher(File path, PipelineClient client) {
                this.path = path;
                this.client = client;
        }

        /**
         * Sets an evironment property
         */
        public PipelineLauncher setEnv(String name, String value) {
                this.env.put(name, value);
                return this;
        }

        //Sets a property to be included or overwritten in the system.properties file
        public PipelineLauncher setProperty(String name, String value) {
                this.opts.add(String.format("%s=%s",name,value));
                return this;
        }

        //creates a new launcher using the pipeline2 script residing on the path and a client
        public static PipelineLauncher newLauncher(File path, PipelineClient client) {
                return new PipelineLauncher(path, client);
        }


        //joins the custom env with the javaops provided
        private HashMap<String,String> loadOps(){
                HashMap<String,String> env=new HashMap<String,String>(this.env);
                String javaOps="-D"+Joiner.on(" -D").join(this.opts);
                env.put("JAVA_OPTS",javaOps);
                logger.info("OPTS: "+javaOps);
                return env;
        }

        //launches the pipeline and waits it to be up
        public boolean launch() throws IOException {
                

                String script=NIX_LAUNCHER;
                if (System.getProperty("os.name").contains("Windows")){
                        script=WIN_LAUNCHER;
                }

                ProcessBuilder pb = new ProcessBuilder(
                                new File(this.path, script).toString());
                HashMap<String,String> env=this.loadOps();
                //add the data path
                pb.environment().put("PIPELINE2_DATA",PipelineLauncher.getDataPath());
                pb.environment().putAll(env);
                //redirect to tmp files
                File err=File.createTempFile("pipelineErr",".txt");
                File out=File.createTempFile("pipelineOut",".txt");
                pb.redirectError(err);
                pb.redirectOutput(out);
                //start the process
                this.process=pb.start();
                logger.info("The process has been launched "
                                + new File(this.path, NIX_LAUNCHER).toString());
                //wait until process is up
                Callable<Boolean> c = new Callable<Boolean>() {
                        //avoid compiler optimisation that
                        //may cause the while run foreva
                        volatile boolean done = false;

                        
                        public Boolean call() {
              int times=1;
                                while (!done) {
                                        logger.info(String.format("Waiting for the ws(%d)...",times++));
                                        //wait for the scripts to be registered
                                        Optional<String>href= Optional.absent();
                                        try {
                                                href= Utils.getScriptHref("dtbook-to-zedai",client);
                                        } catch (Exception e) {}

                                        if (href.isPresent()) {
                                                logger.info("Seems to be up!");
                                                done = true;
                                        }
                                        try {
                                                Thread.sleep(1000);
                                        } catch (InterruptedException e) {
                                                done=true;
                                        }
                                }
                                return done;
                        }

                };
                final FutureTask<Boolean> t = new FutureTask<Boolean>(c);
                new Thread(){
                        public void run(){
                                t.run();
                        }
                }.start();
                boolean result = false;

                try {
                        //Godel may disagree with this
                        result = t.get(60, TimeUnit.SECONDS);
                } catch (Exception e) {
                        logger.error("Timed out waiting for the pipeline "+e.getMessage());
                        t.cancel(true);
                }
                
                
                return result;
        }
        //stops the running pipeline
        public void halt() throws IOException{
                File keyFile=new File(new File(System.getProperty("java.io.tmpdir")),"dp2key.txt");
                String key=Files.readFirstLine(keyFile,Charset.defaultCharset());
                this.client.halt(key);
                try {
                        this.process.waitFor();
                } catch (InterruptedException e) {
                        logger.info("Killing the pipeline");
                        this.process.destroy();
                }

        }
        //cleans all the db and data from the pipeline residing in the given path
        public void clean(){
        }
}
