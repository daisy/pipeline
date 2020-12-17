package org.daisy.pipeline.job;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.job.impl.IOHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobURIUtils {

        final static String IO_DATA_SUBDIR = "context";
        final static String IO_OUTPUT_SUBDIR = "output";

        private final static Logger logger = LoggerFactory.getLogger(JobURIUtils.class);

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
                        logFile = new File(getJobBaseDir(jobId), String.format("%s.log", jobId));
                        logFile.createNewFile();
                        return logFile;
                } catch (IOException e) {
                        throw new RuntimeException(String.format("Error creating the log file for %s", jobId), e);
                }
        }

        // Base directory for a job's input, output and temp files
        static File getJobBaseDir(String jobId) throws IOException {
                File jobsBaseDir; {
                        String prop = "org.daisy.pipeline.iobase";
                        String val = Properties.getProperty(prop);
                        if (val != null) {
                                logger.warn("The '" + prop + "' property is deprecated.");
                                jobsBaseDir = new File(val);
                        } else
                                jobsBaseDir = new File(frameworkDataDir(), "jobs");
                }
                return IOHelper.makeDirs(new File(jobsBaseDir, jobId));
        }

        /**
         * Returns the job's context directory
         * @throws IOException
         */
        static File getJobContextDir(String jobId) throws IOException {
                return new File(getJobBaseDir(jobId), IO_DATA_SUBDIR);
        }

        /**
         * Returns the job's output directory
         * @throws IOException
         */
        static File getJobOutputDir(String jobId) throws IOException {
                return new File(getJobBaseDir(jobId), IO_OUTPUT_SUBDIR);
        }

        static URI getJobBase(String jobId) throws IOException {
                return getJobBaseDir(jobId).toURI();
        }

        static boolean cleanJobBase(String jobId) {
                try {
                        return IOHelper.delete(getJobBase(jobId));
                } catch (IOException e) {
                        throw new RuntimeException(String.format("Error cleaning Job id:%s", jobId), e);
                }
        }

        private static File frameworkDataDir() {
                String prop = "org.daisy.pipeline.data";
                String val = Properties.getProperty(prop);
                if (val == null) throw new IllegalStateException(String.format("The property '%s' is not set", prop));
                return new File(val);
        }
}
