package org.daisy.pipeline.job;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import org.daisy.pipeline.job.impl.JobURIUtils;

import com.google.common.io.Files;

public final class JobSize {

        private Job job;
        private long contextSize;
        private long outputSize;
        private long logSize;

        /**
         * @param job
         * @param contextSize
         * @param outputSize
         * @param logSize
         */
        protected JobSize(Job job, long contextSize, long outputSize,
                        long logSize) {
                this.job = job;
                this.contextSize = contextSize;
                this.outputSize = outputSize;
                this.logSize = logSize;
        }

        /**
         * @return the contextSize
         */
        public long getContextSize() {
                return contextSize;
        }

        /**
         * @return the outputSize
         */
        public long getOutputSize() {
                return outputSize;
        }

        /**
         * @return the logSize
         */
        public long getLogSize() {
                return logSize;
        }

        /**
         * @return the job
         */
        public Job getJob() {
                return job;
        }
        /** Return the sum of all three sizes
         */
        public long getSum(){
                return this.logSize+this.outputSize+this.contextSize;
        }
        /**
         * Returns the job sizes for a collection of jobs
         */
        public static Iterable<JobSize> getSizes(Iterable<Job> jobs) {
                LinkedList<JobSize> sizes= new LinkedList<JobSize>(); 
                for (Job job:jobs){
                        sizes.add(JobSize.getSize(job));
                }
                return sizes;
        }

        public static long getTotal(Iterable<JobSize> sizes) {
                long total=0;
                for (JobSize size:sizes){
                        total+=size.getSum();
                }
                return total;
        }
        public static JobSize getSize(Job job){
                JobSize size=null;
                try {
                        size = new JobSize(job,
                                        JobSize.getContextSize(job.getId()),
                                        JobSize.getOutputSize(job.getId()), 
                                        JobSize.getLogSize(job
                                                .getId()));
                } catch (IOException e) {
                        throw new RuntimeException(String.format("Error calculating the size for job %s",job.getId()),e);
                }

                return size;
        }
        protected static long getLogSize(JobId id) {
                File f = JobURIUtils.getLogFile(id.toString());
                return f.length();
        }

        protected static long getContextSize(JobId id) throws IOException {
                return JobSize.getDirSize(JobURIUtils.getJobContextDir(id.toString()));
        }

        protected static long getOutputSize(JobId id) throws IOException {
                return JobSize.getDirSize(JobURIUtils.getJobOutputDir(id.toString()));
        }

        /**Computes the size of the directory by recursively walking through 
         * and summing up the file and folder sizes 
         */
        protected static long getDirSize(File dir){
                long size=0;
                for (File f: Files.fileTreeTraverser().postOrderTraversal(dir)){
                        //ignore the size of dirs due to x-platform interoperability
                        if (!f.isDirectory()){
                                size+=f.length();
                        }
                }
                return size;
        }

}
