package org.daisy.pipeline.gui.databridge;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.gui.databridge.ScriptField.DataType;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;

// representation of a pipeline script in a GUI-friendly way
public class Script {
        private String name;
        private String description;
        private ArrayList<ScriptField> inputFields;
        private ArrayList<ScriptField> optionFields;
        private XProcScript xprocScript;
        private boolean hasResultOptions; //indicates that there are non-temp results specified by options
        
        public Script(XProcScript script, DatatypeRegistry datatypeRegistry) {
            inputFields = new ArrayList<ScriptField>();
            optionFields = new ArrayList<ScriptField>();
            xprocScript = script;
            
            name = script.getName();
            description = script.getDescription();
            
            hasResultOptions = false;
            
            XProcPipelineInfo scriptInfo = script.getXProcPipelineInfo();
            for (XProcPortInfo portInfo : scriptInfo.getInputPorts()) {
                XProcPortMetadata metadata = script.getPortMetadata(portInfo.getName());
                ScriptField field = new ScriptField(portInfo, metadata, ScriptField.FieldType.INPUT);
                inputFields.add(field);
            }
            
            for (XProcOptionInfo optionInfo : scriptInfo.getOptions()) {
                XProcOptionMetadata metadata = script.getOptionMetadata(optionInfo.getName());
                ScriptField field = new ScriptField(optionInfo, metadata, datatypeRegistry);
                optionFields.add(field);
                
                if (field.isResult() == true && field.isTemp() == false && field.getDataType() == DataType.DIRECTORY) {
                	hasResultOptions = true;
                }
            }
        }
        
        public String getName() {
                return name;
        }
        public String getDescription() {
                return description;
        }
        public Iterable<ScriptField> getInputFields() {
                return inputFields;
        }
        public Iterable<ScriptField> getOptionFields() {
                return optionFields;
        }
        public XProcScript getXProcScript() {
                return xprocScript;
        }
        public boolean hasResultOptions() {
        	return hasResultOptions;
        }
        // this function is useful because the bound script might not have all the script options (it just contains what we want to represent on screen)
        public ScriptField getOptionFieldByName(String name) {
        	for (ScriptField field : optionFields) {
    			if (field.getName().equals(name)) {
    				return field;
    			}
    		}
    		return null;
        }
        public static class ScriptComparator implements Comparator<Script> {

                @Override
                public int compare(Script o1, Script o2) {
                        return Collator.getInstance().compare(o1.getName(),o2.getName());
                }
        }
        
}
