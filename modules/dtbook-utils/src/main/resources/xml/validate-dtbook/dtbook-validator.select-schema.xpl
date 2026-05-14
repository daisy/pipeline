<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:dtbook-validator.select-schema"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>Helper step for DTBook Validator</h1>
        <p>Select the correct RNG schema for the given DTBook and MathML versions.</p>
    </p:documentation>
    
    <!-- ***************************************************** -->
    <!-- OUTPUT and OPTIONS -->
    <!-- Note that there is NO INPUT required for this step -->
    <!-- and that it is ignored if specified -->
    <!-- ***************************************************** -->
    
    <p:input port="source" primary="true" sequence="true">
        <p:empty/>
    </p:input>
    
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The RNG schema that the document should be validated against.</p>
        </p:documentation>
    </p:output>
    
    <p:option name="dtbook-version" required="true" cx:as="xs:string" cx:type="2005-3|2005-2|2005-1|1.1.0">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>DTBook version.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="mathml-version" required="false" cx:as="xs:string" cx:type="3.0|2.0">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Version of MathML in the DTBook file.</p>
        </p:documentation>
    </p:option>
    
    <!-- Based on the DTBook and MathML version, provide the correct RelaxNG schema on the output port -->
    <p:sink/>
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
        <p:otherwise>
            <!-- default to dtbook 2005-3 with mathml 3.0 (can not happen) -->
            <p:identity>
                <p:input port="source">
                    <p:document href="./schema/rng/dtbook-2005-3.mathml-3.integration.rng"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
