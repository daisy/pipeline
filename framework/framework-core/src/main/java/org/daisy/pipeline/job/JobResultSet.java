package org.daisy.pipeline.job;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.namespace.QName;

import org.daisy.pipeline.job.impl.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public final class JobResultSet {
        private static final Logger logger = LoggerFactory.getLogger(JobResultSet.class);
        public static class Builder{
                private final Multimap<String,JobResult> outputPorts=LinkedListMultimap.create();
                private final Multimap<QName,JobResult> options=LinkedListMultimap.create();

                /**
                 * Constructs a new instance.
                 */
                public Builder() {
                }

                public Builder addResult(String port,JobResult result){
                        outputPorts.put(port,result);   
                        return this;
                }

                public Builder addResults(QName option,Collection<JobResult> results){
                        options.putAll(option,results); 
                        return this;
                }
                public Builder addResults(String port,Collection<JobResult> results){
                        outputPorts.putAll(port,results);       
                        return this;
                }

                public Builder addResult(QName option,JobResult result){
                        logger.debug(String.format("Adding result %s",result));
                        options.put(option,result);     
                        return this;
                }
                public JobResultSet build(){
                        return new JobResultSet(outputPorts,options);
                }
        
        }

        private final Multimap<String,JobResult> outputPorts;
        private final Multimap<QName,JobResult> options;

        /**
         * Constructs a new instance.
         *
         * @param outputPorts The outputPorts for this instance.
         * @param options The options for this instance.
         */
        public JobResultSet(Multimap<String, JobResult> outputPorts,
                        Multimap<QName, JobResult> options) {
                this.outputPorts = Multimaps.unmodifiableMultimap(outputPorts);
                this.options = Multimaps.unmodifiableMultimap(options);
        }

        public static InputStream asZippedInputStream(Collection<JobResult> results) throws IOException{
                return new ByteArrayInputStream(asZippedByteArray(results));
        }

        public static byte[] asZippedByteArray(Collection<JobResult> results) throws IOException{
                ByteArrayOutputStream buf=new ByteArrayOutputStream();
                ZipOutputStream zout= new ZipOutputStream(buf);
                for(JobResult res: results){
                        ZipEntry entry=new ZipEntry(res.getIdx().toString());      
                        zout.putNextEntry(entry);
                        InputStream is= res.getPath().toURL().openStream();
                        IOHelper.dump(is,zout);
                        is.close();
                }
                zout.close();
                buf.close();
                return buf.toByteArray();
        }
        public Collection<String> getPorts(){
                return outputPorts.keySet();
        }
        public Collection<QName> getOptions(){
                return options.keySet();
        }

        public Collection<JobResult> getResults(String port){
                return outputPorts.get(port);
        }
        public Collection<JobResult> getResults(QName option){
                return options.get(option);
        }
        public Collection<JobResult> getResults(){
                List<JobResult> results= Lists.newLinkedList(outputPorts.values());
                results.addAll(options.values());
                return Collections.unmodifiableList(results);
        }
}
