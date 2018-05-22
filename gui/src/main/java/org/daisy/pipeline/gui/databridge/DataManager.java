package org.daisy.pipeline.gui.databridge;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.gui.MainWindow;
import org.daisy.pipeline.gui.ServiceRegistry;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;

import javafx.collections.ObservableList;

// communicate with the gui-friendly list of ObservableJob objects
// represent the scripts in a gui-friendly way
public class DataManager {
	
	MainWindow main;
	ServiceRegistry pipelineServices;
	ObservableList<Script> scriptData;
	private int jobCount = 0;
	
	public DataManager(MainWindow main, ObservableList<Script> scriptData, ServiceRegistry pipelineServices) {
		this.main = main;
		this.scriptData = scriptData;
		this.pipelineServices = pipelineServices;
		readScripts();
	}
	
	
	public void updateStatus(Job job, Status status) {
		int i = findJob(job);
		if (i == -1) {
			return;
		}
		main.getJobData().get(i).setStatus(status);
	}
	
	public void addMessage(Job job, String message, Level level) {
		int i = findJob(job);
		if (i == -1) {
			return;
		}
		main.getJobData().get(i).addMessage(message, level);
	}
	
	public ObservableJob addJob(Job job, BoundScript boundScript) {
		ObservableJob objob = new ObservableJob(job, boundScript, ++jobCount);
		main.getJobData().add(objob);
		return objob;
	}
	
	public void removeJob(Job job) {
		int i = findJob(job);
		if (i == -1) {
			return;
		}
		main.getJobData().remove(i);
	}
	
	public int findJob(Job job) {
		ObservableList<ObservableJob> jobData = main.getJobData();
		for (ObservableJob objob : jobData) {
			// for some reason, comparing the job objects directly doesn't work
			if (objob.getJob().getId().toString().equals(job.getId().toString())) {
				return jobData.indexOf(objob);
			}
		}
		return -1;
	}
	
	// read the list of scripts
	// called every time a new scripts combo box is created
	public void readScripts() {
		Set<String> ids = scriptData.stream().map(s -> s.getId()).collect(Collectors.toCollection(() -> new HashSet<String>()));
		for (XProcScriptService scriptService : pipelineServices.getScriptRegistry().getScripts()) {
			String id = scriptService.getId();
			if (!ids.contains(id)) {
				XProcScript xprocScript = scriptService.load();
				Script script = new Script(id, xprocScript, pipelineServices.getDatatypeRegistry());
				ids.add(id);
				scriptData.add(script);
			}
		}
		
	}
	
	public BoundScript cloneBoundScript(BoundScript boundScript) {
		BoundScript newBoundScript = new BoundScript(boundScript.getScript());
		
		for (ScriptFieldAnswer answer : boundScript.getInputFields()) {
			ScriptFieldAnswer newAnswer = newBoundScript.getInputByName(answer.getField().getName());
			copyAnswer(newAnswer, answer);
		}
		for (ScriptFieldAnswer answer : boundScript.getOptionFields()) {
			ScriptFieldAnswer newAnswer = newBoundScript.getOptionByName(answer.getField().getName());
			copyAnswer(newAnswer, answer);
		}
		// TODO copy output dir property
		
		return newBoundScript;
	}
	
	// copy from old to new
	private void copyAnswer(ScriptFieldAnswer newAnswer, ScriptFieldAnswer oldAnswer) {
		if (newAnswer instanceof ScriptFieldAnswer.ScriptFieldAnswerBoolean) {
			ScriptFieldAnswer.ScriptFieldAnswerBoolean oldAnswer_ = (ScriptFieldAnswer.ScriptFieldAnswerBoolean)oldAnswer;
			ScriptFieldAnswer.ScriptFieldAnswerBoolean newAnswer_ = (ScriptFieldAnswer.ScriptFieldAnswerBoolean)newAnswer;
			newAnswer_.answerProperty().set(oldAnswer_.answerProperty().get());
		}
		else if (newAnswer instanceof ScriptFieldAnswer.ScriptFieldAnswerString) {
			ScriptFieldAnswer.ScriptFieldAnswerString oldAnswer_ = (ScriptFieldAnswer.ScriptFieldAnswerString)oldAnswer;
			ScriptFieldAnswer.ScriptFieldAnswerString newAnswer_ = (ScriptFieldAnswer.ScriptFieldAnswerString)newAnswer;
			newAnswer_.answerProperty().set(oldAnswer_.answerProperty().get());
		}
		else if (newAnswer instanceof ScriptFieldAnswer.ScriptFieldAnswerList) {
			ScriptFieldAnswer.ScriptFieldAnswerList oldAnswer_ = (ScriptFieldAnswer.ScriptFieldAnswerList)oldAnswer;
			ScriptFieldAnswer.ScriptFieldAnswerList newAnswer_ = (ScriptFieldAnswer.ScriptFieldAnswerList)newAnswer;
			int sz = oldAnswer_.answerProperty().size();
			for (int i = 0; i<sz; i++) {
				newAnswer_.answerProperty().add(i, oldAnswer_.answerProperty().get(i));
			}
		}
	}
	
}
