package io.transpect.calabash.extensions;

import org.jruby.embed.ScriptingContainer;
import org.jruby.embed.LocalVariableBehavior;

public class Ole2XmlConverter {
	 ScriptingContainer container;
	 String formula;
	 public Ole2XmlConverter(){
		  container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
		  container.runScriptlet("require 'mathtype'");
		  formula = "";
	 }
	 public String convertFormula(String filename) {
		  container.runScriptlet("xml = Mathtype::Converter.new(\""
										 + filename + "\").to_xml"
										 );
		  this.setFormula(container.get("xml").toString());
		  return this.getFormula();
	 }

	 private void setFormula(String formula){
		  this.formula = formula;
	 }

	 public String getFormula(){
		  return formula;
	 }
}
