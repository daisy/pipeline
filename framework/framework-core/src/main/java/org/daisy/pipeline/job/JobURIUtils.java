package org.daisy.pipeline.job;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.daisy.pipeline.job.impl.IOHelper;
import org.daisy.pipeline.properties.Properties;

public class JobURIUtils   {

        public final static String ORG_DAISY_PIPELINE_IOBASE = "org.daisy.pipeline.iobase";

        final static String IO_DATA_SUBDIR = "context";
        final static String IO_OUTPUT_SUBDIR = "output";

        /**
         * Returns an idle uri mapping, in case we are expecting absolute uris 
         * all the time
         */
        static URIMapper newOutputURIMapper(String jobId) throws IOException {
                File outputDir = IOHelper.makeDirs(getJobOutputDir(jobId));
                return new URIMapper(URI.create(""),outputDir.toURI());
        }

        /**
         * Returns a URI mapper which builds a directory extructure 
         * based on the jobid
         */
        static URIMapper newURIMapper(String jobId) throws IOException {
                //based on the the id
                File contextDir = IOHelper.makeDirs(getJobContextDir(jobId));
                File outputDir = IOHelper.makeDirs(getJobOutputDir(jobId));
                return new URIMapper(contextDir.toURI(),outputDir.toURI());

        }

        public static File getLogFile(String jobId) {
                File logFile;
                try {
                        logFile = new File(getJobBaseFile(jobId), String.format("%s.log", jobId));
                        logFile.createNewFile();
                        return logFile;
                } catch (IOException e) {
                        throw new RuntimeException(String.format("Error creating the log file for %s", jobId), e);
                }
        }

        static File getJobBaseFile(String jobId) throws IOException {
                return IOHelper.makeDirs(new File(new File(frameworkBase()), jobId));
        }

        /**
         * Returns the job's context directory
         * @throws IOException
         */
        static File getJobContextDir(String jobId) throws IOException {
                return new File(getJobBaseFile(jobId), IO_DATA_SUBDIR);
        }

        /**
         * Returns the job's output directory
         * @throws IOException
         */
        static File getJobOutputDir(String jobId) throws IOException {
                return new File(getJobBaseFile(jobId), IO_OUTPUT_SUBDIR);
        }

        static URI getJobBase(String jobId) throws IOException {
                return getJobBaseFile(jobId).toURI();
        }

        static boolean cleanJobBase(String jobId) {
                try {
                        return IOHelper.delete(getJobBase(jobId));
                } catch (IOException e) {
                        throw new RuntimeException(String.format("Error cleaning Job id:%s", jobId), e);
                }
        }

        private static String frameworkBase(){
                if (Properties.getProperty(ORG_DAISY_PIPELINE_IOBASE) == null) {
                        throw new IllegalStateException(String.format("The property %s is not set",ORG_DAISY_PIPELINE_IOBASE ));
                }
                return Properties.getProperty(ORG_DAISY_PIPELINE_IOBASE);
        }
}
