package org.daisy.pipeline.gui.databridge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public interface ScriptFieldAnswer<T> {
	
	public T answerProperty();
	public ScriptField getField();
	
	public class ScriptFieldAnswerBase {
		private ScriptField field;
		public ScriptFieldAnswerBase(ScriptField field) {
			this.field = field;
		}
		public ScriptField getField() {
			return field;
		}
		
	}
	
	// the getField method is implemented by the base class; the other(s) are implemented for each class
	
	// note that there is no Integer variant; we're just using Strings with different validation rules
	// (the validation rules come from the field's DataType, not the class type below
	
	public class ScriptFieldAnswerString extends ScriptFieldAnswerBase implements ScriptFieldAnswer<SimpleStringProperty> {
		private SimpleStringProperty answer;
		
		public ScriptFieldAnswerString(ScriptField field) {
			super(field);
			answer = new SimpleStringProperty(field.getDefaultValue());
		}
		
		public SimpleStringProperty answerProperty() {		
			return answer;
		}
	}

	
	public class ScriptFieldAnswerBoolean extends ScriptFieldAnswerBase implements ScriptFieldAnswer<SimpleBooleanProperty> {
		private SimpleBooleanProperty answer;
		
		public ScriptFieldAnswerBoolean(ScriptField field) {
			super(field);
			answer = new SimpleBooleanProperty();
			if (field.getDefaultValue().equals("true")) {
				answer.set(true);
			}
			else {
				answer.set(false);
			}
		}
		public SimpleBooleanProperty answerProperty() {
			return answer;
		}
		public String answerAsString() {
			if (answer.get() == true) {
				return "true";
			}
			else {
				return "false";
			}
		}
	}
	
	public class ScriptFieldAnswerList extends ScriptFieldAnswerBase implements ScriptFieldAnswer<ObservableList<String>> {
		private ObservableList<String> answer;
		
		public ScriptFieldAnswerList(ScriptField field) {
			super(field);
			answer = FXCollections.observableArrayList();
		}
		public ObservableList<String> answerProperty() {
			return answer;
		}
	}
	
	public class ScriptFieldAnswerTempDir extends ScriptFieldAnswerString {
		private SimpleStringProperty answer;
		
		public ScriptFieldAnswerTempDir(ScriptField field) {
			super(field);
			try {
				File tempDir = Files.createTempDirectory(null).toFile();
				tempDir.delete();
				answer = new SimpleStringProperty(tempDir.getAbsolutePath());
			} catch(IOException e) {
				answer = new SimpleStringProperty("");
			}
		}
		
		public SimpleStringProperty answerProperty() {		
			return answer;
		}
	}
}
