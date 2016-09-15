<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="px:fix-dtbook-structure" version="1.0"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:input port="source" primary="true"/>
    <p:output port="result" primary="true"/>
    <p:option name="mathml-formulae-img" select="''"/>

    <p:xslt>
      <p:input port="stylesheet">
	<p:document href="fix-dtbook-structure.xsl"/>
      </p:input>
      <p:with-param name="mathml-formulae-img" select="$mathml-formulae-img"/>
    </p:xslt>

</p:declare-step>
