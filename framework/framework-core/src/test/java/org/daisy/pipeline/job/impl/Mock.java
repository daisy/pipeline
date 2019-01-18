package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.impl.XProcDecorator;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;

import com.google.common.base.Supplier;

class Mock   {
        public static JobContext mockContext(JobId id){
                return new AbstractJobContext(null,id,null,"",null,null){


                };
        }

        public static class MockSource implements Source,Supplier<Source>{
                String sId;

                /**
                 * Constructs a new instance.
                 *
                 * @param sId The sId for this instance.
                 */
                public MockSource(String sId) {
                        this.sId = sId;
                }

                @Override
                public String getSystemId() {
                        return sId;
                }

                @Override
                public void setSystemId(String systemId) {
                        sId=systemId;
                        
                }

                @Override
                public Source get() {
                        return this;
                }
        }

        public static class MockResult implements Result,Supplier<Result>{
                String sId;

                /**
                 * Constructs a new instance.
                 *
                 * @param sId The sId for this instance.
                 */
                public MockResult(String sId) {
                        this.sId = sId;
                }

                @Override
                public String getSystemId() {
                        return sId;
                }

                @Override
                public void setSystemId(String systemId) {
                        sId=systemId;
                        
                }

                @Override
                public Result get() {
                        return this;
                }
        }
        public static Supplier<Source> getSourceProvider(String systemId){
                return new MockSource(systemId);

        }
        public static Supplier<Result> getResultProvider(String systemId){
                return new MockResult(systemId);

        }
        //generates a script with the asked fetures
        //the port/option names follow the convection
        //Input: input-x
        //options:
        // output file: option-output-file-x
        // output dir: option-output-dir-x
        // input file: option-input-file-x
        // input dir: option-input-x
        // other types: option-x
        // 
        public static class ScriptGenerator{
                public static String INPUT="input";
                public static String OUTPUT="output";
                public static String VALUE="value";
                public static String OPTION="option";
                public static String FILE="file";
                public static String DIR="dir";

                public static class Builder{
                        int inputs;
                        int optionOutputsFile;
                        int optionOutputsDir;
                        int optionInputs;
                        int optionOther;
                        int optionOutputsNA;
                        int outputPorts;
                        Set<XProcPortInfo> fixedOutputPorts = new LinkedHashSet<XProcPortInfo>();
                        Map<String,String> fixedOutputPortMediaTypes = new HashMap<String,String>();
                        private int optionTemp;

                        /**
                         * Sets the inputs for this instance.
                         *
                         * @param inputs The inputs.
                         */
                        public Builder withInputs(int inputs) {
                                this.inputs = inputs;
                                return this;
                        }

                        /**
                         * Sets the optionOutputsFile for this instance.
                         *
                         * @param optionOutputsFile The optionOutputsFile.
                         */
                        public Builder withOptionOutputsFile(int optionOutputsFile) {
                                this.optionOutputsFile = optionOutputsFile;
                                return this;
                        }

                        /**
                         * Sets the optionOutputsDir for this instance.
                         *
                         * @param optionOutputsDir The optionOutputsDir.
                         */
                        public Builder withOptionOutputsDir(int optionOutputsDir) {
                                this.optionOutputsDir = optionOutputsDir;
                                return this;
                        }

                        /**
                         * Sets the optionInputsFile for this instance.
                         *
                         * @param optionInputsFile The optionInputsFile.
                         */
                        public Builder withOptionInputs(int optionInputs) {
                                this.optionInputs = optionInputs;
                                return this;
                        }

                        /**
                         * Sets the optionOther for this instance.
                         *
                         * @param optionOther The optionOther.
                         */
                        public Builder withOptionOther(int optionOther) {
                                this.optionOther = optionOther;
                                return this;
                        }

                        /**
                         * Sets the optionOther for this instance.
                         *
                         * @param optionOther The optionOther.
                         */
                        public Builder withOptionTemp(int optionTemp) {
                                this.optionTemp = optionTemp;
                                return this;
                        }


                        public ScriptGenerator build(){
                                return new ScriptGenerator( inputs, optionOutputsFile, optionOutputsDir, optionInputs, optionOther,optionOutputsNA,outputPorts, fixedOutputPorts, fixedOutputPortMediaTypes, optionTemp);
                        }

                        /**
                         * Sets the optionOutputsNA for this instance.
                         *
                         * @param optionOutputsNA The optionOutputsNA.
                         */
                        public Builder withOptionOutputsNA(int optionOutputsNA) {
                                this.optionOutputsNA = optionOutputsNA;
                                return this;
                        }

                        public Builder withOutputPorts(int outputPorts) {
                                this.outputPorts= outputPorts;
                                return this;
                        }

                        public Builder withOutputPort(String portName, String mediaType, boolean isSequence, boolean isPrimary) {
                                fixedOutputPorts.add(XProcPortInfo.newOutputPort(portName, isSequence, isPrimary));
                                fixedOutputPortMediaTypes.put(portName, mediaType);
                                return this;
                        }
                }

                int inputs;
                int optionOutputsFile;
                int optionOutputsDir;
                int optionInputs;
                int optionOther;
                int optionOutputsNA;
                int outputPorts;
                Set<XProcPortInfo> fixedOutputPorts;
                Map<String,String> fixedOutputPortMediaTypes;
                int optionTemp;

                /**
                 * Constructs a new instance.
                 *
                 * @param inputs The inputs for this instance.
                 * @param optionOutputsFile The optionOutputsFile for this instance.
                 * @param optionOutputsDir The optionOutputsDir for this instance.
                 * @param optionInputsFile The optionInputsFile for this instance.
                 * @param optionInputsDir The optionInputsDir for this instance.
                 * @param optionOther The optionOther for this instance.
                 */
                public ScriptGenerator(int inputs, int optionOutputsFile,
                                int optionOutputsDir, int optionInputs,
                                int optionOther,int optionsOutputNA,int outputPorts,
                                Set<XProcPortInfo> fixedOutputPorts,
                                Map<String,String> fixedOutputPortMediaTypes, int optionTemp) {
                        this.inputs = inputs;
                        this.optionOutputsFile = optionOutputsFile;
                        this.optionOutputsDir = optionOutputsDir;
                        this.optionInputs= optionInputs;
                        this.optionOther = optionOther;
                        this.optionOutputsNA= optionsOutputNA;
                        this.outputPorts=outputPorts;
                        this.fixedOutputPorts = fixedOutputPorts;
                        this.fixedOutputPortMediaTypes = fixedOutputPortMediaTypes;
                        this.optionTemp=optionTemp;
                }


                public XProcScript generate(){
                        Set<XProcPortInfo> inputSet= new LinkedHashSet<XProcPortInfo>(); 
                        Set<XProcPortInfo> outputSet= new LinkedHashSet<XProcPortInfo>(); 
                        Set<XProcOptionInfo> optionsSet= new LinkedHashSet<XProcOptionInfo>(); 
                        HashMap<QName,XProcOptionMetadata> optionMetadatas= new LinkedHashMap<QName,XProcOptionMetadata>(); 
                        HashMap<String,XProcPortMetadata> portMetadatas= new LinkedHashMap<String,XProcPortMetadata>(); 
                        //inputs
                        for (int i=0;i<this.inputs;i++){
                                inputSet.add(XProcPortInfo.newInputPort(getInputName(i),false, true));
                        }
                        //outputs
                        if (fixedOutputPorts != null) {
                                outputSet.addAll(fixedOutputPorts);
                        }
                        for (int i=0;i<this.outputPorts;i++){
                                outputSet.add(XProcPortInfo.newOutputPort(getOutputName(i),false, true));
                        }
                        //options inputs
                        for (int i=0;i<this.optionInputs;i++){
                                QName name=getOptionInputName(i);
                                optionsSet.add( XProcOptionInfo.newOption(name, false, ""));
                                optionMetadatas.put(name,new XProcOptionMetadata.Builder()
                                .withType(XProcDecorator.TranslatableOption.ANY_FILE_URI.toString()).build());
                        }
                        //options output file
                        for (int i=0;i<this.optionOutputsFile;i++){
                                QName name= getOptionOutputFileName(i);
                                optionsSet.add( XProcOptionInfo.newOption(name, false, ""));
                                optionMetadatas.put(name,new XProcOptionMetadata.Builder()
                                .withType(XProcDecorator.TranslatableOption.ANY_FILE_URI.toString()).withOutput("result").build());
                        }

                        //options output file
                        for (int i=0;i<this.optionOutputsDir;i++){
                                QName name= getOptionOutputDirName(i);
                                optionsSet.add( XProcOptionInfo.newOption(name, false, ""));
                                optionMetadatas.put(name,new XProcOptionMetadata.Builder()
                                .withType(XProcDecorator.TranslatableOption.ANY_DIR_URI.toString()).withOutput("result").build());
                        }

                        //options output file
                        for (int i=0;i<this.optionTemp;i++){
                                QName name = getOptionTempName(i);
                                optionsSet.add(XProcOptionInfo.newOption(name, false, ""));
                                optionMetadatas
                                                .put(name,
                                                                new XProcOptionMetadata.Builder()
                                                                                .withType(
                                                                                                XProcDecorator.TranslatableOption.ANY_DIR_URI
                                                                                                                .toString())
                                                                                .withOutput("temp").build());
                        }

                        //options output file
                        for (int i = 0; i < this.optionOutputsNA; i++) {
                                QName name = getOptionOutputNAName(i);
                                optionsSet.add(XProcOptionInfo.newOption(name, false, ""));
                                optionMetadatas
                                                .put(name,
                                                                new XProcOptionMetadata.Builder()
                                                                                .withType(
                                                                                                XProcDecorator.TranslatableOption.ANY_DIR_URI
                                                                                                                .toString())
                                                                                .withOutput("NA").build());
                        }
                        //regular options
                        for (int i = 0; i < this.optionOther; i++) {
                                QName name = getRegularOptionName(i);
                                optionsSet.add(XProcOptionInfo.newOption(name, false, ""));
                                optionMetadatas.put(name,
                                                new XProcOptionMetadata.Builder().build());
                        }
                        XProcPipelineInfo.Builder pipelineBuilder = new XProcPipelineInfo.Builder();
                        for (XProcOptionInfo oInf : optionsSet) {
                                pipelineBuilder.withOption(oInf);
                        }
                        for (XProcPortInfo port : inputSet) {
                                pipelineBuilder.withPort(port);
                        }
                        for (XProcPortInfo port : outputSet) {
                                pipelineBuilder.withPort(port);
                                XProcPortMetadata.Builder meta = new XProcPortMetadata.Builder();
                                if (fixedOutputPortMediaTypes != null) {
                                        meta.withMediaType(fixedOutputPortMediaTypes.get(port.getName()));
                                }
                                portMetadatas.put(port.getName(), meta.build());
                        }
                        List<String> inputMedias = Collections.emptyList();
                        return new XProcScript(pipelineBuilder.build(), null, null, null,
                                        portMetadatas, optionMetadatas, null, inputMedias,
                                        inputMedias);

                }

                public static QName getOptionTempName(int num) {
                        return new QName(String.format("TEMP-%s-%s-%s-%d",OPTION,OUTPUT,DIR,num));
                }

                public static String getInputName(int num){
                                return (String.format("%s-%d",INPUT,num));
                }
                public static String getOutputName(int num){
                                return (String.format("%s-%d",OUTPUT,num));
                }
                public static QName getOptionInputName(int num){
                                return new QName(String.format("%s-%s-%d",OPTION,INPUT,num));
                }


                public static QName getOptionOutputFileName(int num){
                                return new QName(String.format("%s-%s-%s-%d",OPTION,OUTPUT,FILE,num));
                }
                public static QName getOptionOutputDirName(int num){
                                return new QName(String.format("%s-%s-%s-%d",OPTION,OUTPUT,DIR,num));
                }
                public static QName getRegularOptionName(int num){
                                return new QName(String.format("%s-%d",OPTION,num));
                }

                public static QName getOptionOutputNAName(int num){
                                return new QName(String.format("%s-na-%d",OPTION,num));
                }
        }

        public static void populateDir(String dir) throws IOException{
                File fdir= new File(URI.create(dir));
                fdir.mkdirs();
                assert(fdir.isDirectory());
                (new File(fdir,"uno.xml")).createNewFile();
                (new File(fdir,"dos.xml")).createNewFile();
                (new File(fdir,"tres.xml")).createNewFile();
        }


}
