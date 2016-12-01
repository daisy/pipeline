package org.daisy.pipeline.gui.databridge;

import java.util.HashMap;
import java.util.List;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.UrlBasedDatatypeService;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcOptionMetadata.Output;
import org.daisy.pipeline.script.XProcPortMetadata;

public class ScriptField {
	public enum FieldType {INPUT, OUTPUT, OPTION};
	
	public static class DataType {
		private DataType() {}
		
		public static DataType FILE = new DataType();
		public static DataType DIRECTORY = new DataType();
		public static DataType STRING = new DataType();
		public static DataType BOOLEAN = new DataType();
		public static DataType INTEGER = new DataType();
		
		public static class Enumeration extends DataType {
			private final List<String> values;
			private Enumeration(List<String> values) {
				this.values = ImmutableList.copyOf(values);
			}
			public List<String> getValues() {
				return values;
			}
		}
	}
	
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
	public ScriptField(XProcOptionInfo optionInfo, XProcOptionMetadata metadata, DatatypeRegistry datatypeRegistry) {
		name = optionInfo.getName().toString();
		description = metadata.getDescription();
		niceName = metadata.getNiceName();
		isSequence = metadata.isSequence();
		dataType = getDataType(metadata.getType(), metadata.getDatatype(), datatypeRegistry);
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
	private DataType getDataType(String simpleType, String dataType, DatatypeRegistry datatypeRegistry) {
		if (dataTypeMap.containsKey(dataType)) {
			return dataTypeMap.get(dataType);
		} else if (dataType != null) {
			//System.out.println("############################DATA TYPE not found: " + dataType);
			Optional<DatatypeService> o = datatypeRegistry.getDatatype(dataType);
			if (o.isPresent()) {
				if (o.get() instanceof UrlBasedDatatypeService) {
					UrlBasedDatatypeService service = (UrlBasedDatatypeService)o.get();
					if (service.isEnumeration()) {
						return new DataType.Enumeration(service.getEnumerationValues());
					}
				}
			}
		}
		if (dataTypeMap.containsKey(simpleType)) {
			return dataTypeMap.get(simpleType);
		}
		return DataType.STRING; // default to string
	}
	
}
