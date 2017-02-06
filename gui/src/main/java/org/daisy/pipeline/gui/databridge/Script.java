package org.daisy.pipeline.gui.databridge;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;

// representation of a pipeline script in a GUI-friendly way
public class Script {
        private String name;
        private String description;
        private ArrayList<ScriptField> inputFields;
        private ArrayList<ScriptField> requiredOptionFields;
        private ArrayList<ScriptField> optionalOptionFields;
        private XProcScript xprocScript;
        
        public Script(XProcScript script, DatatypeRegistry datatypeRegistry) {
                inputFields = new ArrayList<ScriptField>();
                requiredOptionFields = new ArrayList<ScriptField>();
                optionalOptionFields = new ArrayList<ScriptField>();
                xprocScript = script;
                
                name = script.getName();
                description = script.getDescription();
                
                XProcPipelineInfo scriptInfo = script.getXProcPipelineInfo();
                for (XProcPortInfo portInfo : scriptInfo.getInputPorts()) {
                        XProcPortMetadata metadata = script.getPortMetadata(portInfo.getName());
                        ScriptField field = new ScriptField(portInfo, metadata, ScriptField.FieldType.INPUT);
                        inputFields.add(field);
                }
                
                for (XProcOptionInfo optionInfo : scriptInfo.getOptions()) {
                        XProcOptionMetadata metadata = script.getOptionMetadata(optionInfo.getName());
                        ScriptField field = new ScriptField(optionInfo, metadata, datatypeRegistry);
                        if (field.isRequired()) {
                                requiredOptionFields.add(field);
                        }
                        else {
                                optionalOptionFields.add(field);
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
        public Iterable<ScriptField> getRequiredOptionFields() {
                return requiredOptionFields;
        }
        public Iterable<ScriptField> getOptionalOptionFields() {
                return optionalOptionFields;
        }
        public XProcScript getXProcScript() {
                return xprocScript;
        }

        public static class ScriptComparator implements Comparator<Script> {

                @Override
                public int compare(Script o1, Script o2) {
                        return Collator.getInstance().compare(o1.getName(),o2.getName());
                }
        }
        
}
