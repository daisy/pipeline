package org.daisy.pipeline.css.sass;

import java.io.StringReader;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.transform.stream.StreamSource;

import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.css.sass.SassAnalyzer.SassVariable;
import org.daisy.pipeline.datatypes.DatatypeService;

import org.junit.Assert;
import org.junit.Test;

public class SassAnalyzerTest {

	private final static Medium SCREEN = Medium.parse("screen");
	@Test
	public void testVariableNameHyphen() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   $my-var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("My var", v.getNiceName());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testVariableNameUnderscore() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   $my_var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("My var", v.getNiceName());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testVariableNameCamelCase() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   $myVar: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("My var", v.getNiceName());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testVariableNameUpperCamelCase() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   $MyVar: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("My var", v.getNiceName());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testDefaultValue() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   $my-var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("true", v.getValue());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testFixedVariable() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   $my-var: true;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("true", v.getValue());
		Assert.assertFalse(v.isDefault());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testMapVariable() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   $my-var: (foo: 1, bar: 2);"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("(foo: 1, bar: 2)", v.getValue());
		Assert.assertEquals("string", v.getType().getId());
		Assert.assertFalse(v.isDefault());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testRegularComment() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   /*"                    + "\n" +
			                                                            "    * a comment"          + "\n" +
			                                                            "    */"                   + "\n" +
			                                                            "   $my-var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("a comment", v.getDescription());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testDoxygenComment() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   /**"                   + "\n" +
			                                                            "    * a comment"          + "\n" +
			                                                            "    */"                   + "\n" +
			                                                            "   $my-var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("a comment", v.getDescription());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testDoxygenLongDescription() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   /**"                      + "\n" +
			                                                            "    * a comment"             + "\n" +
			                                                            "    *"                       + "\n" +
			                                                            "    * a long description in" + "\n" +
			                                                            "    * markdown format"       + "\n" +
			                                                            "    *"                       + "\n" +
			                                                            "    *     indented block"    + "\n" +
			                                                            "    *"                       + "\n" +
			                                                            "    */"                      + "\n" +
			                                                            "   $my-var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("a comment\n\na long description in\nmarkdown format\n\n    indented block", v.getDescription());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testDoxygenFencedCodeBlock() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("/**"                      + "\n" +
			                                                            " * a comment"             + "\n" +
			                                                            " *"                       + "\n" +
			                                                            " * ~~~sass"               + "\n" +
			                                                            " * @if $my-var {"         + "\n" +
			                                                            " *   ..."                 + "\n" +
			                                                            " * }"                     + "\n" +
			                                                            " * ~~~"                   + "\n" +
			                                                            " */"                      + "\n" +
			                                                            " $my-var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("a comment\n\n~~~sass\n@if $my-var {\n  ...\n}\n~~~", v.getDescription());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testDoxygenBrief() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   /**"                   + "\n" +
			                                                            "    * @brief My variable" + "\n" +
			                                                            "    *"                    + "\n" +
			                                                            "    * a comment"          + "\n" +
			                                                            "    */"                   + "\n" +
			                                                            "   $my-var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("My variable", v.getNiceName());
		Assert.assertEquals("a comment", v.getDescription());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testDoxygenVar() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   /**"                   + "\n" +
			                                                            "    * @var $my-var"       + "\n" +
			                                                            "    *"                    + "\n" +
			                                                            "    * a comment"          + "\n" +
			                                                            "    */"                   + "\n" +
			                                                            "   $my-var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("my-var", v.getName());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testDoxygenVarType() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   /**"                      + "\n" +
			                                                            "    * @var string $my-var"   + "\n" +
			                                                            "    *"                       + "\n" +
			                                                            "    * a comment"             + "\n" +
			                                                            "    */"                      + "\n" +
			                                                            "   $my-var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("string", v.getType().getId());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testDoxygenVarTypeInferred() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   /**"                      + "\n" +
			                                                            "    * a comment"             + "\n" +
			                                                            "    */"                      + "\n" +
			                                                            "   $my-var: true !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("boolean", v.getType().getId());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testDoxygenXMLCommand() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("   /**"                                                              + "\n" +
			                                                            "    * a comment"                                                     + "\n" +
			                                                            "    *"                                                               + "\n" +
			                                                            "    * <px:type xmlns:px=\"http://www.daisy.org/ns/pipeline/xproc\">" + "\n" +
			                                                            "    *   <choice>"                                                    + "\n" +
			                                                            "    *     <value>1</value>"                                          + "\n" +
			                                                            "    *     <value>2</value>"                                          + "\n" +
			                                                            "    *   </choice>"                                                   + "\n" +
			                                                            "    * </px:type>"                                                    + "\n" +
			                                                            "    */"                                                              + "\n" +
			                                                            "   $my-var: 1 !default;"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("a comment", v.getDescription());
		DatatypeService type = v.getType();
		Assert.assertTrue(type.validate("1").isValid());
		Assert.assertTrue(type.validate("2").isValid());
		Assert.assertFalse(type.validate("3").isValid());
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testVariableInExpression() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(SCREEN, null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("@if $my-var {" + "\n" +
			                                                            "	..."        + "\n" +
			                                                            "}"))),
			null
		).getVariables().iterator();
		Assert.assertFalse(variables.hasNext());
	}

	@Test
	public void testVariableInsideMediaRule() throws Exception {
		Iterator<SassVariable> variables = new SassAnalyzer(Medium.parse("embossed AND (duplex:1)"), null, null).analyze(
			Collections.singletonList(new StreamSource(new StringReader("@media embossed {"                   + "\n" +
			                                                            "    @media (duplex:1) {"             + "\n" +
			                                                            "        $my-var: true !default;"     + "\n" +
			                                                            "    }"                               + "\n" +
			                                                            "    @media (duplex:0) {"             + "\n" +
			                                                            "        $other-var: true !default;"  + "\n" +
			                                                            "    }"                               + "\n" +
			                                                            "}"                                   + "\n" +
			                                                            "@media print {"                      + "\n" +
			                                                            "	$third-var: true !default;"       + "\n" +
			                                                            "}"))),
			null
		).getVariables().iterator();
		SassVariable v = variables.next();
		Assert.assertEquals("my-var", v.getName());
		Assert.assertFalse(variables.hasNext());
	}
}
