package org.daisy.pipeline.gui.databridge;

import java.util.ArrayList;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.daisy.pipeline.gui.databridge.ScriptFieldAnswer;
import org.daisy.pipeline.gui.databridge.ScriptField.DataType;
import org.daisy.pipeline.gui.databridge.ScriptField.FieldType;

public class BoundScript {
	
	private Script script;
	private ObservableList<ScriptFieldAnswer> requiredOptionAnswers;
	private ObservableList<ScriptFieldAnswer> optionalOptionAnswers;
	
	public BoundScript(Script script) {
		this.script = script;
		this.requiredOptionAnswers = FXCollections.observableArrayList();
		this.optionalOptionAnswers = FXCollections.observableArrayList();
		createAnswers();
	}
	public Script getScript() {
		return script;
	}
	public Iterable<ScriptFieldAnswer> getRequiredOptionFields() {
		return requiredOptionAnswers;
	}
	public Iterable<ScriptFieldAnswer> getOptionalOptionFields() {
		return optionalOptionAnswers;
	}
	
	public ScriptFieldAnswer getInputByName(String name) {
		ScriptFieldAnswer answer = findByNameAndType(requiredOptionAnswers, FieldType.INPUT, name);
		if (answer == null) {
			return findByNameAndType(optionalOptionAnswers, FieldType.INPUT, name);
		} else {
			return answer;
		}
	}
	public ScriptFieldAnswer getOptionByName(String name) {
		ScriptFieldAnswer answer = findByNameAndType(requiredOptionAnswers, FieldType.OPTION, name);
		if (answer == null) {
			return findByNameAndType(optionalOptionAnswers, FieldType.OPTION, name);
		} else {
			return answer;
		}
	}
	
	private ScriptFieldAnswer findByNameAndType(Iterable<ScriptFieldAnswer> list, FieldType type, String name) {
		for (ScriptFieldAnswer answer : list) {
			if (answer.getField().getFieldType() == type
			    && answer.getField().getName().equals(name)) {
				return answer;
			}
		}
		return null;
	}
	
	private void createAnswers() {
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
//			SimpleBooleanProperty b = (SimpleBooleanProperty)answer.answerProperty();
//			b.set(true);
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
