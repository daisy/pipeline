<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		type="px:css-parse-param-set">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Parse a style sheet parameter set string and return it as a c:param-set document.</p>
    </p:documentation>

    <p:option name="parameters" required="true"/>

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A c:param-set document.</p>
        </p:documentation>
    </p:output>

    <!--
        Implemented in ../../java/org/daisy/pipeline/css/calabash/impl/CssParseParamSetStep.java
    -->

</p:declare-step>
