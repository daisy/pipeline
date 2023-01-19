package org.daisy.pipeline.job;

import java.io.File;
import java.io.IOException;
import java.net.URI;

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

        // path to the actual file
        private URI path;

        //media type
        private String mediaType;

        /**
         * Constructs a new instance.
         *
         * @param idx The idx for this instance.
         * @param path The path for this instance.
         * @param mediaType The mediaType for this instance.
         */
        protected JobResult(String idx, URI path, String mediaType) {
                this.idx = idx;
                this.path = path;
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
                if (path == null || path.toString().isEmpty())
                        throw new RuntimeException("the document was not stored to disk");
                return idx;
        }

        /**
         * Gets the path for this instance.
         *
         * @return The path.
         */
        public URI getPath() {
                if (path == null || path.toString().isEmpty())
                        throw new RuntimeException("the document was not stored to disk");
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
         * Returns the size of the file pointed by path in bytes
         * @return the size
         */
        public long getSize() {
                try{
                        File f= new File(path);
                        if( ! f.exists()){
                                throw new IOException(String.format("File not found :",f.getAbsolutePath()));
                        }
                        return f.length();
                }catch (Exception e){
                        throw new RuntimeException("Error calculating result size",e);
                }
        }
}
