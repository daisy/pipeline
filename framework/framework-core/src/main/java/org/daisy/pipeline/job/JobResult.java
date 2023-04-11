package org.daisy.pipeline.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * The Class JobResult.
 */
public class JobResult {

        @Override
        public int hashCode() {
                return this.idx.toString().hashCode()+this.path.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
                if(obj==null || !(obj instanceof JobResult))
                        return false;
                JobResult other=(JobResult) obj;
                return this.idx.toString().equals(other.idx.toString())&&this.path.equals(other.path);

        }

        @Override
        public String toString() {
                return String.format("JobResult [#=%s path='%s']", this.idx,this.path);
        }

        //short index for the result 
        private String idx;

        // path to the file
        private File path;

        //media type
        private String mediaType;

        /**
         * Constructs a new instance.
         *
         * @param idx The idx for this instance.
         * @param path The path for this instance.
         * @param mediaType The mediaType for this instance.
         */
        protected JobResult(String idx, File path, String mediaType) {
                if (path == null)
                        throw new NullPointerException();
                if (!path.exists())
                        throw new IllegalArgumentException("the document was not stored to disk: " + path);
                this.path = path;
                this.idx = idx;
                this.mediaType = mediaType;
        }

        /**
         * Gets a result without the first index level 
         *
         *
         */
        public JobResult strip(){
                return new JobResult(stripPrefix(idx), path, mediaType);
        }

        private static String stripPrefix(String index) {
                int idx = index.indexOf('/');
                if (idx != 0)
                        return index.substring(idx + 1);
                else
                        return index;
        }

        /**
         * Gets the idx for this instance.
         *
         * @return The idx.
         */
        public String getIdx() {
                return idx;
        }

        /**
         * Gets the path for this instance.
         *
         * @return The path.
         */
        public File getPath() {
                return path;
        }

        /**
         * Gets the mediaType for this instance.
         *
         * @return The mediaType.
         */
        public String getMediaType() {
                return this.mediaType;
        }

        /**
         * Get the contents of the resource as an {@link InputStream}.
         */
        public InputStream asStream() throws IOException {
                return new FileInputStream(path);
        }

        /**
         * Returns the size of the file pointed by path in bytes
         * @return the size
         */
        public long getSize() {
                try{
                        if (!path.exists()) {
                                throw new IOException(String.format("File not found: ", path.getAbsolutePath()));
                        }
                        return path.length();
                }catch (Exception e){
                        throw new RuntimeException("Error calculating result size",e);
                }
        }
}
