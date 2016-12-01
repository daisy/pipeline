package org.daisy.pipeline.gui.databridge;

import java.util.HashMap;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcOptionMetadata.Output;
import org.daisy.pipeline.script.XProcPortMetadata;

public class ScriptField {
	public enum FieldType {INPUT, OUTPUT, OPTION};
	public enum DataType {FILE, DIRECTORY, STRING, BOOLEAN, INTEGER}; /* @px:type attribute of script XML */
	
	
	public static HashMap<String, DataType> dataTypeMap;
	static {
        dataTypeMap = new HashMap<String, DataType>();
        dataTypeMap.put("anyFileURI", DataType.FILE);
        dataTypeMap.put("anyDirURI", DataType.DIRECTORY);
        dataTypeMap.put("boolean", DataType.BOOLEAN);
        dataTypeMap.put("string", DataType.STRING);
        dataTypeMap.put("integer", DataType.INTEGER);
        dataTypeMap.put("xs:boolean", DataType.BOOLEAN);
        dataTypeMap.put("xs:string", DataType.STRING);
        dataTypeMap.put("xs:integer", DataType.INTEGER);
    }
	
	private String name;
	private String niceName;
	private String description;
	private FieldType fieldType; /* input, output, or option */
	private String mediaType; /* @media-type attribute of script XML; e.g. "application/xhtml+xml" */
	private boolean isRequired;
	private boolean isSequence;
	private DataType dataType; /* @type attribute of script XML; e.g. anyFileURI */
	private boolean isOrdered; /* ONLY for options */
	private boolean isPrimary; /* ONLY for input/output */
	private boolean isTemp;		/* ONLY for options */
	private boolean isResult;	/* ONLY for options */
	private String defaultValue; /*ONLY for options */
	
	
	
	public ScriptField(XProcPortInfo portInfo, XProcPortMetadata metadata, FieldType fieldType) {
		name = portInfo.getName();
		description = metadata.getDescription();
		niceName = metadata.getNiceName();
		isSequence = portInfo.isSequence();
		mediaType = metadata.getMediaType();
		this.fieldType = fieldType;
		isRequired = metadata.isRequired();
		isOrdered = false;
		dataType = DataType.FILE;
		isPrimary = portInfo.isPrimary();
		isTemp = false;
		isResult = false;
		defaultValue = "";
	}
	public ScriptField(XProcOptionInfo optionInfo, XProcOptionMetadata metadata) {
		name = optionInfo.getName().toString();
		description = metadata.getDescription();
		niceName = metadata.getNiceName();
		isSequence = metadata.isSequence();
		dataType = getDataType(metadata.getType());
		mediaType = metadata.getMediaType();
		fieldType = FieldType.OPTION;
		isRequired = optionInfo.isRequired();
		isOrdered = metadata.isOrdered();
		isPrimary = false;
		isResult = metadata.getOutput() == Output.RESULT;
		isTemp = metadata.getOutput() == Output.TEMP;
		defaultValue = optionInfo.getSelect();
		// trim single quotes from start/end of string
		if (defaultValue != null && defaultValue.isEmpty() == false) {
			defaultValue = defaultValue.replaceAll("^'|'$", "");
		}

	}
	public String getName() {
		return name;
	}
	public String getNiceName() {
		return niceName;
	}
	public String getDescription() {
		return description;
	}
	public FieldType getFieldType() {
		return fieldType;
	}
	public String getMediaType() {
		return mediaType;
	}
	public boolean isRequired() {
		return isRequired;
	}
	public boolean isSequence() {
		return isSequence;
	}
	public DataType getDataType() {
		return dataType;
	}
	public boolean isOrdered() {
		return isOrdered;
	}
	public boolean isPrimary() {
		return isPrimary;
	}
	public boolean isTemp() {
		return isTemp;
	}
	public boolean isResult() {
		return isResult;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	private DataType getDataType(String dataType) {
		if (dataTypeMap.containsKey(dataType)) {
			return dataTypeMap.get(dataType);
		}
		else {
			//System.out.println("############################DATA TYPE not found: " + dataType);
		}
		return DataType.STRING; // default to string
	}
	
}
