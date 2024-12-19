package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobMonitor;
import org.daisy.pipeline.job.StatusNotifier;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;

import com.google.common.base.Supplier;

public class Mock   {
        public static AbstractJobContext mockContext(JobId jobId) {
                return new AbstractJobContext() {{
                        this.id = jobId;
                        this.niceName = "";
                }};
        }

        static class MockedJobContext extends AbstractJobContext {

                public MockedJobContext(Client client) {
                        this(client, null);
                }

                public MockedJobContext(Client client, JobBatchId batchId) {
                        super();
                        this.client = client;
                        this.id = JobIdFactory.newId();
                        this.batchId = batchId;
                        this.niceName = "";
                        this.monitor = new JobMonitor() {
                                        @Override
                                        public MessageAccessor getMessageAccessor() {
                                                return null;
                                        }
                                        @Override
                                        public StatusNotifier getStatusUpdates() {
                                                return null;
                                        }
                                };
                }
        }

        public static class MockSource implements Source {
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
        public static Source getSource(String systemId){
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


                public XProcScript generate() {
                        XProcScript.Builder builder = new XProcScript.Builder("", "", null, null, null, null);

                        // inputs
                        for (int i = 0; i < inputs; i++) {
                                XProcPortInfo info = XProcPortInfo.newInputPort(getInputName(i), false, true, true);
                                builder.withInputPort(info, new XProcPortMetadata("", "", ""));
                        }

                        // outputs
                        Set<XProcPortInfo> outputPortSet = new LinkedHashSet<XProcPortInfo>();
                        if (fixedOutputPorts != null) {
                                outputPortSet.addAll(fixedOutputPorts);
                        }
                        for (int i = 0; i < outputPorts; i++) {
                                outputPortSet.add(XProcPortInfo.newOutputPort(getOutputName(i),false, true));
                        }
                        for (XProcPortInfo port : outputPortSet) {
                                builder.withOutputPort(port,
                                                       new XProcPortMetadata("", "", fixedOutputPortMediaTypes != null
                                                                                         ? fixedOutputPortMediaTypes.get(port.getName())
                                                                                         : null));
                        }

                        // options inputs
                        for (int i = 0; i < optionInputs; i++) {
                                builder.withOption(
                                        XProcOptionInfo.newOption(getOptionInputName(i), false, ""),
                                        new XProcOptionMetadata(
                                                null, null, XProcOptionMetadata.ANY_FILE_URI, null));
                        }

                        // options output file
                        for (int i = 0; i < optionOutputsFile; i++) {
                                builder.withOption(
                                        XProcOptionInfo.newOption(getOptionOutputFileName(i), false, ""),
                                        new XProcOptionMetadata(
                                                null, null, XProcOptionMetadata.ANY_FILE_URI, null,
                                                XProcOptionMetadata.Output.RESULT, true));
                        }

                        // options output file
                        for (int i=0; i < optionOutputsDir; i++) {
                                builder.withOption(
                                        XProcOptionInfo.newOption(getOptionOutputDirName(i), false, ""),
                                        new XProcOptionMetadata(
                                                null, null, XProcOptionMetadata.ANY_DIR_URI, null,
                                                XProcOptionMetadata.Output.RESULT, true));
                        }

                        // options output file
                        for (int i=0;i<this.optionTemp;i++){
                                builder.withOption(
                                        XProcOptionInfo.newOption(getOptionTempName(i), false, ""),
                                        new XProcOptionMetadata(
                                                null, null, XProcOptionMetadata.ANY_DIR_URI, null,
                                                XProcOptionMetadata.Output.TEMP, true));
                        }

                        // options output file
                        for (int i = 0; i < this.optionOutputsNA; i++) {
                                builder.withOption(
                                        XProcOptionInfo.newOption(getOptionOutputNAName(i), false, ""),
                                        new XProcOptionMetadata(
                                                null, null, XProcOptionMetadata.ANY_DIR_URI, null,
                                                XProcOptionMetadata.Output.NA, false));
                        }
                        // regular options
                        for (int i = 0; i < this.optionOther; i++) {
                                builder.withOption(
                                        XProcOptionInfo.newOption(getRegularOptionName(i), false, ""),
                                        new XProcOptionMetadata(null, null, null, null));
                        }

                        return builder.build();
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
