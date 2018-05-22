package org.daisy.pipeline.gui.databridge;

import java.util.ArrayList;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;


import org.daisy.pipeline.gui.databridge.ScriptFieldAnswer;
import org.daisy.pipeline.gui.databridge.ScriptField.DataType;

@SuppressWarnings("restriction")
public class BoundScript {
	
	private Script script;
	private ObservableList<ScriptFieldAnswer> inputAnswers;
	private ObservableList<ScriptFieldAnswer> optionAnswers;
	// this field doesn't get bound to a script field. this is a directory the user can specify for their output.
	// the gui will make subdirs for each result option, and set the script options accordingly
	private SimpleStringProperty outputDir;  
	
	public BoundScript(Script script) {
		this.script = script;
		this.outputDir = new SimpleStringProperty();
		this.inputAnswers = FXCollections.observableArrayList();
		this.optionAnswers = FXCollections.observableArrayList(); // we're only putting non-result options here
		createAnswers();
	}
	public Script getScript() {
		return script;
	}
	public Iterable<ScriptFieldAnswer> getInputFields() {
		return inputAnswers;
	}
	public Iterable<ScriptFieldAnswer> getOptionFields() {
		return optionAnswers;
	}
	
	
	public Iterable<ScriptFieldAnswer> getOptionFields(boolean isRequired) {
		return optionAnswers.filtered(x -> x.getField().isRequired() == isRequired);
	}
	public ScriptFieldAnswer getInputByName(String name) {
		return findByName(inputAnswers, name);
	}
	public ScriptFieldAnswer getOptionByName(String name) {
		return findByName(optionAnswers, name);
	}
	public SimpleStringProperty getOutputDir() {
		return outputDir;
	}
	
	private ScriptFieldAnswer findByName(Iterable<ScriptFieldAnswer> list, String name) {
		for (ScriptFieldAnswer answer : list) {
			if (answer.getField().getName().equals(name)) {
				return answer;
			}
		}
		return null;
	}
	
	private void createAnswers() {
		for (ScriptField field : script.getInputFields()) {
			ScriptFieldAnswer answer = createAnswer(field);
			inputAnswers.add(answer);
		}

		for (ScriptField field : script.getOptionFields()) {
			// we're not exposing result option directories in the GUI (Their directories will get generated automatically)
			// we're also not dealing with temp options, as the framework handles them automatically
			if (field.isResult() == false && field.isTemp() == false) {
				ScriptFieldAnswer answer = createAnswer(field);
				optionAnswers.add(answer);
			}
		}
	}
	
	private ScriptFieldAnswer createAnswer(ScriptField field) {
		ScriptFieldAnswer answer;
		if (field.getDataType() == DataType.BOOLEAN) {
			answer = new ScriptFieldAnswer.ScriptFieldAnswerBoolean(field);
		}
		// this clause can probably go as we aren't dealing with temp dirs (the framework handles it)
		else if (field.getDataType() == DataType.DIRECTORY && field.isTemp()) {
			answer = new ScriptFieldAnswer.ScriptFieldAnswerTempDir(field);
		}
		else {
			if (field.isSequence() == true) {
				answer = new ScriptFieldAnswer.ScriptFieldAnswerList(field);
			}
			else {
				answer = new ScriptFieldAnswer.ScriptFieldAnswerString(field);
			}
		}
		return answer;
	}
}
