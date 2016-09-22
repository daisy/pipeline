<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="dtbook-validator.select-schema" type="px:dtbook-validator.select-schema"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Helper step for DTBook Validator</h1>
        <p px:role="desc">Select the correct RNG schema for the given DTBook and MathML versions.</p>
    </p:documentation>
    
    <!-- ***************************************************** -->
    <!-- OUTPUT and OPTIONS -->
    <!-- Note that there is NO INPUT required for this step -->
    <!-- ***************************************************** -->
    
    <p:input port="source" primary="true">
        <p:empty/>
    </p:input>
    
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">result</h1>
            <p px:role="desc">The RNG schema that the document should be validated against.</p>
        </p:documentation>
    </p:output>
    
    <p:option name="dtbook-version" required="true" px:type="string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">mathml-version</h2>
            <p px:role="desc">Version of MathML in the DTBook file.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="mathml-version" required="false" px:type="string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">mathml-version</h2>
            <p px:role="desc">Version of MathML in the DTBook file.</p>
        </p:documentation>
    </p:option>
    
    <!-- Based on the DTBook and MathML version, provide the correct RelaxNG schema on the output port -->
    <p:choose name="choose-schema">
        <p:when test="$dtbook-version = '2005-3' and $mathml-version = '3.0'">
            <p:identity>
                <p:input port="source">
                    <p:document href="./schema/rng/dtbook-2005-3.mathml-3.integration.rng"/>
                </p:input>
            </p:identity>
        </p:when>
        <p:when test="$dtbook-version = '2005-3' and $mathml-version = '2.0'">
            <p:identity>
                <p:input port="source">
                    <p:document href="./schema/rng/dtbook-2005-3.mathml-2.integration.rng"/>
                </p:input>
            </p:identity>
        </p:when>
        <p:when test="$dtbook-version = '2005-2' and $mathml-version = '3.0'">
            <p:identity>
                <p:input port="source">
                    <p:document href="./schema/rng/dtbook-2005-2.mathml-3.integration.rng"/>
                </p:input>
            </p:identity>
        </p:when>
        <p:when test="$dtbook-version = '2005-2' and $mathml-version = '2.0'">
            <p:identity>
                <p:input port="source">
                    <p:document href="./schema/rng/dtbook-2005-2.mathml-2.integration.rng"/>
                </p:input>
            </p:identity>
        </p:when>
        <!-- we aren't supporting mathml on versions of dtbook older than 2005-2 -->
        <p:when test="$dtbook-version = '2005-1'">
            <p:identity>
                <p:input port="source">
                    <p:document href="./schema/rng/dtbook/dtbook-2005-1.rng"/>
                </p:input>
            </p:identity>
        </p:when>
        <p:when test="$dtbook-version = '1.1.0'">
            <p:identity>
                <p:input port="source">
                    <p:document href="./schema/rng/dtbook/dtbook-1.1.0.rng"/>
                </p:input>
            </p:identity>
        </p:when>
        <!-- default to dtbook 2005-3 -->
        <!-- We could also consider generating an error that the version was not detectable. -->
        <p:otherwise>
            <p:identity>
                <p:input port="source">
                    <p:document href="./schema/rng/dtbook-2005-3.mathml-3.integration.rng"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
