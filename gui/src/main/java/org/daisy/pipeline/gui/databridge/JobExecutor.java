package org.daisy.pipeline.gui.databridge;


import java.io.File;
import java.net.MalformedURLException;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.daisy.common.transform.LazySaxSourceProvider;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.common.xproc.XProcInput.Builder;
import org.daisy.pipeline.gui.MainWindow;
import org.daisy.pipeline.gui.NewJobPane;
import org.daisy.pipeline.gui.ServiceRegistry;
import org.daisy.pipeline.gui.databridge.ScriptField.DataType;
import org.daisy.pipeline.gui.databridge.ScriptField.FieldType;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.script.BoundXProcScript;
import org.daisy.pipeline.script.XProcScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class JobExecutor {

        private static final Logger logger = LoggerFactory.getLogger(JobExecutor.class);
        
        public static Job runJob(MainWindow main, ServiceRegistry pipelineServices, BoundScript boundScript)
                        throws MalformedURLException {
               
                NewJobPane newJobPane = main.getNewJobPane();
                if (newJobPane == null) {
                        logger.error("Job could not be created: new job view panel is null");
                return null;
                }
        if (boundScript == null) {
                logger.error("Job could not be created: script is null");
                return null;
        }
        
        XProcScript script = boundScript.getScript().getXProcScript();
        XProcInput.Builder inBuilder = new XProcInput.Builder();
        XProcOutput.Builder outBuilder = new XProcOutput.Builder();
        XProcPipelineInfo scriptInfo = script.getXProcPipelineInfo();
        
        // add inputs
        Iterator<XProcPortInfo> itInput = scriptInfo.getInputPorts().iterator();
        while (itInput.hasNext()) {
            XProcPortInfo input = itInput.next();
            String inputName = input.getName();
            addToBuilder(boundScript.getInputByName(inputName), inBuilder);
        }
        
        //add options
        Iterator<XProcOptionInfo> itOption = scriptInfo.getOptions().iterator();
        while(itOption.hasNext()) {
                XProcOptionInfo option = itOption.next();
                String optionName = option.getName().toString();
                addToBuilder(boundScript.getOptionByName(optionName), inBuilder);
        }
        
        BoundXProcScript bound = BoundXProcScript.from(script, inBuilder.build(), outBuilder.build());

        Optional<Job> newJob = pipelineServices.getJobManager().newJob(bound).isMapping(true).withNiceName("TODO").build();
        
        // TODO what does isPresent() do?
//        if(!newJob.isPresent()){
//              return Optional.absent();
//        }
                
        return newJob.get();
    }
        
        private static void addToBuilder(ScriptFieldAnswer answer, Builder builder)
                        throws MalformedURLException {
                
                String name = answer.getField().getName();
                FieldType type = answer.getField().getFieldType();
                DataType dataType=answer.getField().getDataType();
                
                if (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerString) {
                        ScriptFieldAnswer.ScriptFieldAnswerString answer_ = 
                                        (ScriptFieldAnswer.ScriptFieldAnswerString)answer;
                        String value = answer_.answerProperty().get();
                        if (type == FieldType.INPUT) {
                                LazySaxSourceProvider prov = new LazySaxSourceProvider(new File(value).toURI().toString());
                    builder.withInput(name, prov);
                        }
                        else {
                                if (!"".equals(value) && (dataType==DataType.DIRECTORY || dataType == DataType.FILE)){
                                        value=new File(value).toURI().toString();
                                }
                                builder.withOption(new QName(name), value);
                        }
                
                }
                
                else if (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerList) {
                        ScriptFieldAnswer.ScriptFieldAnswerList answer_ = 
                                (ScriptFieldAnswer.ScriptFieldAnswerList)answer;
                        for (String value : answer_.answerProperty()) {
                                if (type == FieldType.INPUT) {
                                        LazySaxSourceProvider prov = new LazySaxSourceProvider(new File(value).toURI().toString());
                                        builder.withInput(name, prov);
                                }
                                else {

                                        if (dataType==DataType.DIRECTORY || dataType == DataType.FILE){
                                                value=new File(value).toURI().toString();
                                        }
                                        builder.withOption(new QName(name), value);
                                }
                        }
                }
                
                else if (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerBoolean) {
                        ScriptFieldAnswer.ScriptFieldAnswerBoolean answer_ = 
                                        (ScriptFieldAnswer.ScriptFieldAnswerBoolean)answer;
                        String value = answer_.answerAsString();
                        // booleans are only possibly ever options
                        builder.withOption(new QName(name), value);
                }
        }
        
}
