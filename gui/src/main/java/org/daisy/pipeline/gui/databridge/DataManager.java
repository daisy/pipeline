package org.daisy.pipeline.gui.databridge;

import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.gui.MainWindow;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

// communicate with the gui-friendly list of ObservableJob objects
// represent the scripts in a gui-friendly way
public class DataManager {
	
	MainWindow main;
	
	public DataManager(MainWindow main) {
		this.main = main;
		initData();
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
	
	public ObservableJob addJob(Job job) {
		ObservableJob objob = new ObservableJob(job);
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
	
	private void addScript(XProcScript xprocScript) {
		Script script = new Script(xprocScript);
		main.getScriptData().add(script);
	}
	
	// called once at startup
	// read the list of scripts
	private void initData() {
		for (XProcScriptService scriptService : main.getScriptRegistry().getScripts()) {
			XProcScript xprocScript = scriptService.load();
			addScript(xprocScript);
		}
		
	}
	
	public BoundScript cloneBoundScript(BoundScript boundScript) {
		BoundScript newBoundScript = new BoundScript(boundScript.getScript());
		
		for (ScriptFieldAnswer answer : boundScript.getInputFields()) {
			ScriptFieldAnswer newAnswer = newBoundScript.getInputByName(answer.getField().getName());
			copyAnswer(newAnswer, answer);
		}
		for (ScriptFieldAnswer answer : boundScript.getRequiredOptionFields()) {
			ScriptFieldAnswer newAnswer = newBoundScript.getOptionByName(answer.getField().getName());
			copyAnswer(newAnswer, answer);
		}
		for (ScriptFieldAnswer answer : boundScript.getOptionalOptionFields()) {
			ScriptFieldAnswer newAnswer = newBoundScript.getOptionByName(answer.getField().getName());
			copyAnswer(newAnswer, answer);
		}
		
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
