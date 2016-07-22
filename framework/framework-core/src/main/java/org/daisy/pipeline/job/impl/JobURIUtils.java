package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.URIMapper;

public class JobURIUtils   {
        /** The Constant ORG_DAISY_PIPELINE_IOBASE. */
        public final static String ORG_DAISY_PIPELINE_IOBASE = "org.daisy.pipeline.iobase";

        public final static String IO_DATA_SUBDIR = "context";
        /** The I o_ outpu t_ subdir. */
        public final static String IO_OUTPUT_SUBDIR = "output";
        /**
         * Returns an idle uri mapping, in case we are expecting absolute uris 
         * all the time
         */
        public static URIMapper newOutputURIMapper(JobId id) throws IOException{
                File outputDir = IOHelper.makeDirs(JobURIUtils.getJobOutputDir(id));
                return new URIMapper(URI.create(""),outputDir.toURI());
        }


        /**
         * Returns a URI mapper which builds a directory extructure 
         * based on the jobid
         */
        public static URIMapper newURIMapper(JobId id) throws IOException{
                //based on the the id
                File contextDir = IOHelper.makeDirs(JobURIUtils.getJobContextDir(id));
                File outputDir = IOHelper.makeDirs(JobURIUtils.getJobOutputDir(id));
                return new URIMapper(contextDir.toURI(),outputDir.toURI());

        }

        public static URI getLogFile(JobId id) {
                //this has to be done according to the logback configuration file
                
                File logFile;
                try {
                        logFile = new File(getJobBaseFile(id),String.format("%s.log",id.toString()));
                        logFile.createNewFile();
                        return logFile.toURI();
                } catch (IOException e) {
                        throw new RuntimeException(String.format("Error creating the log file for %s",id.toString()),e);
                }
        }
        
        protected static File getJobBaseFile(JobId id) throws IOException{
                return IOHelper.makeDirs(new File(new File(frameworkBase()), id.toString()));
        }
        
        /**
         * Returns the job's context directory
         * @throws IOException
         */
        public static File getJobContextDir(JobId id) throws IOException {
                return new File(JobURIUtils.getJobBaseFile(id),JobURIUtils.IO_DATA_SUBDIR);
        }

        /**
         * Returns the job's output directory
         * @throws IOException
         */
        public static File getJobOutputDir(JobId id) throws IOException {
                return new File(JobURIUtils.getJobBaseFile(id),JobURIUtils.IO_OUTPUT_SUBDIR);
        }
        public static URI getJobBase(JobId id) throws IOException{
                return getJobBaseFile(id).toURI();
        }

        public static boolean cleanJobBase(JobId id){
                try {
                        return IOHelper.delete(getJobBase(id));
                } catch (IOException e) {
                        throw new RuntimeException(String.format("Error cleaning Job id:%s",id),e);
                }
        }
        private static String frameworkBase(){
                if (System.getProperty(ORG_DAISY_PIPELINE_IOBASE) == null) {
                        throw new IllegalStateException(String.format("The property %s is not set",ORG_DAISY_PIPELINE_IOBASE ));
                }
                return System.getProperty(ORG_DAISY_PIPELINE_IOBASE);
        }
}
