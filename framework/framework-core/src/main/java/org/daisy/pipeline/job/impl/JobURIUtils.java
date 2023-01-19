package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.job.URIMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobURIUtils {

        final static String IO_DATA_SUBDIR = "context";
        final static String IO_OUTPUT_SUBDIR = "output";

        private final static Logger logger = LoggerFactory.getLogger(JobURIUtils.class);

        /**
         * Returns an idle URI mapping, in case we are expecting absolute URIs all the time.
         */
        static URIMapper newOutputURIMapper(String jobId) throws IOException {
                File outputDir = IOHelper.makeDirs(getJobOutputDir(jobId));
                return new URIMapper(URI.create(""),outputDir.toURI());
        }

        /**
         * Returns a URI mapper which builds a directory structure based on the job ID.
         */
        static URIMapper newURIMapper(String jobId) throws IOException {
                //based on the the id
                File contextDir = IOHelper.makeDirs(getJobContextDir(jobId));
                File outputDir = IOHelper.makeDirs(getJobOutputDir(jobId));
                return new URIMapper(contextDir.toURI(),outputDir.toURI());
        }

        public static File getLogFile(String jobId) {
                return getLogFile(jobId, true);
        }

        private static File getLogFile(String jobId, boolean create) {
                try {
                        File logFile = new File(getJobBaseDir(jobId, create), String.format("%s.log", jobId));
                        if (create)
                                logFile.createNewFile();
                        return logFile;
                } catch (IOException e) {
                        throw new RuntimeException(String.format("Error creating the log file for %s", jobId), e);
                }
        }

        private static File jobsBaseDir = null;

        /**
         * @return the base directory for a job's input, output and temp files
         * @throws IOException if <code>create</code> is true and the directory could not be created.
         */
        static File getJobBaseDir(String jobId) throws IOException {
                return getJobBaseDir(jobId, true);
        }

        private static File getJobBaseDir(String jobId, boolean create) throws IOException {
                if (jobsBaseDir == null) {
                        String prop = "org.daisy.pipeline.iobase";
                        String val = Properties.getProperty(prop);
                        if (val != null)
                                logger.warn("The '" + prop + "' property is deprecated. Ignoring.");
                        jobsBaseDir = new File(frameworkDataDir(), "jobs");
                }
                File f = new File(jobsBaseDir, jobId);
                if (create)
                        IOHelper.makeDirs(f);
                return f;
        }

        /**
         * @return the job's context directory
         * @throws IOException if <code>create</code> is true and the directory could not be created.
         */
        public static File getJobContextDir(String jobId) throws IOException {
                return getJobContextDir(jobId, true);
        }

        private static File getJobContextDir(String jobId, boolean create) throws IOException {
                return new File(getJobBaseDir(jobId, create), IO_DATA_SUBDIR);
        }

        /**
         * @return the job's output directory
         * @throws IOException if <code>create</code> is true and the directory could not be created.
         */
        public static File getJobOutputDir(String jobId) throws IOException {
                return getJobOutputDir(jobId, true);
        }

        private static File getJobOutputDir(String jobId, boolean create) throws IOException {
                return new File(getJobBaseDir(jobId, create), IO_OUTPUT_SUBDIR);
        }

        public static boolean deleteJobBaseDir(String jobId) {
                try {
                        return deleteDir(getJobBaseDir(jobId, false));
                } catch (IOException e) {
                        throw new RuntimeException(); // coding error
                }
        }

        private static boolean deleteJobBaseDirIfEmpty(String jobId) {
                try {
                        return getJobBaseDir(jobId, false).delete();
                } catch (IOException e) {
                        throw new RuntimeException(); // coding error
                }
        }

        /**
         * Delete the job' log file, and also the parent directory if it contained only the log
         * file.
         */
        public static boolean deleteLogFile(String jobId) {
                File f = getLogFile(jobId, false);
                logger.debug("Deleting file: " + f);
                boolean logFileDeleted = f.delete();
                deleteJobBaseDirIfEmpty(jobId);
                return logFileDeleted;
        }

        /**
         * Delete the job' context directory, and also the parent directory if it contained only the
         * context directory.
         */
        public static boolean deleteJobContextDir(String jobId) {
                try {
                        boolean contextDirDeleted = deleteDir(getJobContextDir(jobId, false));
                        deleteJobBaseDirIfEmpty(jobId);
                        return contextDirDeleted;
                } catch (IOException e) {
                        throw new RuntimeException(); // coding error
                }
        }

        /**
         * Delete the job' output directory (results and temporary files), and also the parent
         * directory if it contained only the output directory.
         */
        public static boolean deleteJobOutputDir(String jobId) {
                try {
                        boolean outputDirDeleted = deleteDir(getJobOutputDir(jobId, false));
                        deleteJobBaseDirIfEmpty(jobId);
                        return outputDirDeleted;
                } catch (IOException e) {
                        throw new RuntimeException(); // coding error
                }
        }

        // see org.daisy.pipeline.job.JobURIUtils
        public static void assertFrameworkDataDirPersisted() throws IllegalStateException {
                String prop = "org.daisy.pipeline.data";
                if (Properties.getProperty(prop) == null)
                        throw new IllegalStateException(String.format("The property '%s' is not set", prop));
        }

        private static File frameworkDataDir = null;

        private static File frameworkDataDir() throws IOException {
                if (frameworkDataDir == null) {
                        String prop = "org.daisy.pipeline.data";
                        String val = Properties.getProperty(prop);
                        if (val != null)
                                frameworkDataDir = new File(val);
                        else {
                                frameworkDataDir = Files.createTempDirectory("pipeline-jobs-").toFile();
                                // delete on exit
                                Runtime.getRuntime().addShutdownHook(
                                        new Thread() {
                                                public void run() {
                                                        deleteDir(frameworkDataDir); }});
                        }
                }
                return frameworkDataDir;
        }

        private static boolean deleteDir(File dir) {
                logger.debug("Deleting directory: " + dir);
                File[] files = dir.listFiles();
                if (files != null)
                        for (File f : files) {
                                if (f.isDirectory()) {
                                        deleteDir(f);
                                }
                                f.delete();
                        }
                return dir.delete();
        }
}
