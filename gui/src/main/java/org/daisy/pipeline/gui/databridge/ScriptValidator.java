package org.daisy.pipeline.gui.databridge;

import java.io.File;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.daisy.pipeline.gui.databridge.ScriptField.DataType;
import org.daisy.pipeline.gui.databridge.ScriptField.FieldType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ScriptValidator {
	
        private static final Logger logger = LoggerFactory.getLogger(ScriptValidator.class);
	private BoundScript boundScript;
	private ObservableList<String> messages;
	
	public ScriptValidator(BoundScript boundScript) {
		this.boundScript = boundScript;
		messages = FXCollections.observableArrayList();
	}
	public boolean validate() {
		boolean inputsAreValid = checkFields(boundScript.getInputFields());
		boolean reqOptionsAreValid = checkFields(boundScript.getRequiredOptionFields());
		// validate the optional options just to get any messages about their values
		// for example, a file path might be expected
		checkFields(boundScript.getOptionalOptionFields());
                
		
                logger.debug("Inputs "+inputsAreValid);
                logger.debug("reqOptionsAreValid "+reqOptionsAreValid);
		return inputsAreValid && reqOptionsAreValid;
		
	}
	public ObservableList<String> getMessages() {
		return messages;
	}
	private boolean checkFields(Iterable<ScriptFieldAnswer> fields) {
		boolean isValid = true;
		ScriptFieldValidator validator = new ScriptFieldValidator();
		for (ScriptFieldAnswer answer : fields) {
			if (!validator.validate(answer)) {
				messages.add(validator.getMessage());
				isValid = false;
			}
		}
		return isValid;
	}
	public class ScriptFieldValidator {
		
		private String EMPTYSTRING = "ERROR: Value is empty for ";
		private String BADPATH = "ERROR: File not found: ";
		private String NOTANUM = "ERROR: Numeric value required for ";
		private String CANTCREATEDIR = "ERROR: Could not create directory ";
		private String NOTADIR = "ERROR: Not a directory: ";
		private String NOTINENUM = "ERROR: Value is not in the list of accepted values: ";
		
	
		String message;
		public boolean validate(ScriptFieldAnswer answer) {
			DataType dataType = answer.getField().getDataType();
			
			if (dataType == DataType.BOOLEAN) {
				return validateBoolean(answer);
			}
			if (dataType == DataType.DIRECTORY) {
				return validateDirectory(answer);
			}
			if (dataType == DataType.FILE) {
				return validateFile(answer);
			}
			if (dataType == DataType.INTEGER) {
				return validateInteger(answer);
			}
			if (dataType == DataType.STRING) {
				return validateString(answer);
			}
			if (dataType instanceof DataType.Enumeration) {
				return validateEnumeration(answer, (DataType.Enumeration)dataType);
			}
			return validateString(answer); // default to string
		}
		
		public String getMessage() {
			return this.message;
		}
		private boolean validateBoolean(ScriptFieldAnswer answer) {
			return true;
		}
		
		private boolean validateString(ScriptFieldAnswer answer) {
			if (! (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerString) ) {
				return false;
			}
			ScriptFieldAnswer.ScriptFieldAnswerString answer_ = (ScriptFieldAnswer.ScriptFieldAnswerString)answer;
			
			String answerString = answer_.answerProperty().get();
			if (answer.getField().isRequired() && (answerString == null || answerString.isEmpty())) {
				message = EMPTYSTRING + answer.getField().getNiceName();
				return false;
			}
			return true;
		}
		
		// validate file paths
		private boolean validateFile(ScriptFieldAnswer answer) {
			boolean valid=true;
			if (! (answer instanceof ScriptFieldAnswer.ScriptFieldAnswerList)) {
				valid= validateString(answer) && validateFile(((ScriptFieldAnswer.ScriptFieldAnswerString)answer).answerProperty().get(),answer.getField());
			}else{
				for (String s:((ScriptFieldAnswer.ScriptFieldAnswerList)answer).answerProperty()){
					if (! validateFile(s,answer.getField())){
						valid=false;
						break;
					}
					
				}
			}
			return valid;
		}
		private boolean validateFile(String answerString,ScriptField field) {
			
			
			
			
			// optional fields can have empty values; but if it's not empty, proceed to make sure it's valid
			if (field.isRequired() == false && answerString.isEmpty()) {
				return true;
			}
			
			File file = new File(answerString);
			// for input files: check that the file exists
			if (field.getFieldType() == FieldType.INPUT || 
					field.getFieldType() == FieldType.OPTION) {
				if (!file.exists()) {
					message = BADPATH + field.getNiceName();
					return false;
				}
			}
			return true;
		}
		
		// validate directory paths
		// attempt to create directories for result or temp options
		private boolean validateDirectory(ScriptFieldAnswer answer) {
			
			if (!validateString(answer)) {
				return false;
			}
			ScriptFieldAnswer.ScriptFieldAnswerString answer_ = (ScriptFieldAnswer.ScriptFieldAnswerString)answer;
			String answerString = answer_.answerProperty().get();
			
			// optional fields can have empty values; but if it's not empty, proceed to make sure it's valid
			if (answer.getField().isRequired() == false && answerString.isEmpty()) {
				return true;
			}
			
			File file = new File(answerString);
			
			
			if (answer.getField().isResult() || answer.getField().isTemp()) {
				// try to create if it doesn't exist
				if (!file.exists()) {
					boolean couldCreateDir = file.mkdirs();
					if (!couldCreateDir) {
						message = CANTCREATEDIR + answerString;
						return false;
					}
				}
			}
			else {
				if (!file.exists()) {
					message = BADPATH + answer.getField().getNiceName();
					return false;
				}
				if (!file.isDirectory()) {
					message = NOTADIR + answer.getField().getNiceName();
					return false;
				}
			}
			return true;
			
		}
		// we'll treat ints like strings with special rules
		private boolean validateInteger(ScriptFieldAnswer answer) {
			if (!validateString(answer)) {
				return false;
			}
			ScriptFieldAnswer.ScriptFieldAnswerString answer_ = (ScriptFieldAnswer.ScriptFieldAnswerString)answer;
			String answerString = answer_.answerProperty().get();
			
			// optional fields can have empty values; but if it's not empty, proceed to make sure it's valid
			if (answer.getField().isRequired() == false && answerString.isEmpty()) {
				return true;
			}
			
			try {
				Integer.parseInt(answerString);
			}
			catch (NumberFormatException e) {
				message = NOTANUM + answer.getField().getNiceName();
				return false;
			}
			return true;
		}
		
		private boolean validateEnumeration(ScriptFieldAnswer answer, DataType.Enumeration enumeration) {
			if (!validateString(answer)) {
				return false;
			}
			ScriptFieldAnswer.ScriptFieldAnswerString answer_ = (ScriptFieldAnswer.ScriptFieldAnswerString)answer;
			String answerString = answer_.answerProperty().get();
			if (!enumeration.getValues().contains(answerString)) {
				message = NOTINENUM + answer.getField().getNiceName();
				return false;
			}
			return true;
		}
	}
}
