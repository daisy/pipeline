package org.daisy.pipeline.gui.databridge;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

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
        
        // job output goes in directories like this:
        //     path/to/boundScript.getOutputDir/jobDir/optionDir1..n/
        // where the optionDirs are the options' names
        Path jobDir = createJobOutputDirectory(boundScript.getOutputDir().get(), boundScript.getScript().getName(), main);
        if (jobDir == null) { // this means the specified directory doesn't exist, or a subdir for this specific job couldn't be created
        	return null;
        }
        
        
        XProcScript xprocScript = boundScript.getScript().getXProcScript();
        XProcInput.Builder inBuilder = new XProcInput.Builder();
        XProcOutput.Builder outBuilder = new XProcOutput.Builder();
        XProcPipelineInfo scriptInfo = xprocScript.getXProcPipelineInfo();
        
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
                ScriptFieldAnswer answer = boundScript.getOptionByName(optionName);
                if (answer != null) {
                	addToBuilder(boundScript.getOptionByName(optionName), inBuilder);
                }
                
                // if there was no answer given in the bound script, this is probably one of the options we didn't expose
                // check if it's a result directory, and if so, generate a value for it
                else {
                	ScriptField optionField = boundScript.getScript().getOptionFieldByName(optionName);
                	
                	if (optionField.isResult() == true && optionField.isTemp() == false && optionField.getDataType() == DataType.DIRECTORY) {
                		Path optionPath = jobDir.resolve(optionName);
                		try {
							Files.createDirectory(optionPath);
						} catch (IOException e) {
							logger.error("Could not create job option directory: " + optionPath.toString());
						}
                		// not quitting here, see what the framework does with this path
                		inBuilder.withOption(option.getName(), optionPath.toUri().toString());
                	}
                }
        }
              
        BoundXProcScript bound = BoundXProcScript.from(xprocScript, inBuilder.build(), outBuilder.build());

        Optional<Job> newJob = pipelineServices.getJobManager().newJob(bound).isMapping(true).withNiceName("TODO").build();
        
        return newJob.get();
    }

	private static void addToBuilder(ScriptFieldAnswer answer, Builder builder) throws MalformedURLException {

		String name = answer.getField().getName();
		FieldType type = answer.getField().getFieldType();
		DataType dataType = answer.getField().getDataType();

		if (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerString) {
			ScriptFieldAnswer.ScriptFieldAnswerString answer_ = (ScriptFieldAnswer.ScriptFieldAnswerString) answer;
			String value = answer_.answerProperty().get();
			if (type == FieldType.INPUT) {
				LazySaxSourceProvider prov = new LazySaxSourceProvider(new File(value).toURI().toString());
				builder.withInput(name, prov);
			} else {
				if (!"".equals(value) && (dataType == DataType.DIRECTORY || dataType == DataType.FILE)) {
					value = new File(value).toURI().toString();
				}
				builder.withOption(new QName(name), value);
			}

		}

		else if (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerList) {
			ScriptFieldAnswer.ScriptFieldAnswerList answer_ = (ScriptFieldAnswer.ScriptFieldAnswerList) answer;
			for (String value : answer_.answerProperty()) {
				if (type == FieldType.INPUT) {
					LazySaxSourceProvider prov = new LazySaxSourceProvider(new File(value).toURI().toString());
					builder.withInput(name, prov);
				} else {

					if (dataType == DataType.DIRECTORY || dataType == DataType.FILE) {
						value = new File(value).toURI().toString();
					}
					builder.withOption(new QName(name), value);
				}
			}
		}

		else if (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerBoolean) {
			ScriptFieldAnswer.ScriptFieldAnswerBoolean answer_ = (ScriptFieldAnswer.ScriptFieldAnswerBoolean) answer;
			String value = answer_.answerAsString();
			// booleans are only possibly ever options
			builder.withOption(new QName(name), value);
		}
	}

	// create a folder for this job in the given directory, and return a path to this new folder
	private static Path createJobOutputDirectory(String parentDir, String scriptName, MainWindow main) {
		// the script validator already ensures that this parentDir exists
		Path path = Paths.get(parentDir);
		
		// create a directory for the job, based on the script name
		String timestamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		String jobDir = timestamp + "_" + scriptName;
		Path jobDirPath = path.resolve(jobDir);
		// there is an extremely small chance that this folder would
		// actually already exist .. but just to be safe
		if (Files.exists(jobDirPath)) {
			jobDirPath = path.resolve(jobDir + "_" + UUID.randomUUID().toString());
		}
		try {
			Files.createDirectory(jobDirPath);
		} catch (IOException e) {
			logger.error("Could not create job directory: " + jobDirPath.toString());
			main.getMessagesPane().addMessage("ERROR: Could not create job directory: " + jobDirPath.toString());
			return null;
		}
		return jobDirPath;
		
	}
}
