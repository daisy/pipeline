package org.daisy.pipeline.gui.databridge;

import java.util.ArrayList;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.daisy.pipeline.gui.databridge.ScriptFieldAnswer;
import org.daisy.pipeline.gui.databridge.ScriptField.DataType;

public class BoundScript {
	
	private Script script;
	private ObservableList<ScriptFieldAnswer> inputAnswers;
	private ObservableList<ScriptFieldAnswer> requiredOptionAnswers;
	private ObservableList<ScriptFieldAnswer> optionalOptionAnswers;
	
	public BoundScript(Script script) {
		this.script = script;
		this.inputAnswers = FXCollections.observableArrayList();
		this.requiredOptionAnswers = FXCollections.observableArrayList();
		this.optionalOptionAnswers = FXCollections.observableArrayList();
		createAnswers();
	}
	public Script getScript() {
		return script;
	}
	public Iterable<ScriptFieldAnswer> getInputFields() {
		return inputAnswers;
	}
	public Iterable<ScriptFieldAnswer> getRequiredOptionFields() {
		return requiredOptionAnswers;
	}
	public Iterable<ScriptFieldAnswer> getOptionalOptionFields() {
		return optionalOptionAnswers;
	}
	
	public ScriptFieldAnswer getInputByName(String name) {
		return findByName(inputAnswers, name);
	}
	public ScriptFieldAnswer getOptionByName(String name) {
		// look in both lists
		ScriptFieldAnswer answer = findByName(requiredOptionAnswers, name);
		if (answer == null) {
			return findByName(optionalOptionAnswers, name);
		}
		else {
			return answer;
		}
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

		for (ScriptField field : script.getRequiredOptionFields()) {
			ScriptFieldAnswer answer = createAnswer(field);
			requiredOptionAnswers.add(answer);
		}
		for (ScriptField field : script.getOptionalOptionFields()) {
			ScriptFieldAnswer answer = createAnswer(field);
			optionalOptionAnswers.add(answer);
		}
	}
	
	private ScriptFieldAnswer createAnswer(ScriptField field) {
		ScriptFieldAnswer answer;
		if (field.getDataType() == DataType.BOOLEAN) {
			answer = new ScriptFieldAnswer.ScriptFieldAnswerBoolean(field);
			// default to true for bool fields
			SimpleBooleanProperty b = (SimpleBooleanProperty)answer.answerProperty();
			b.set(true);
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
