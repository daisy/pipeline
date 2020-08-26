<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:smil-to-audio-clips">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Create a d:audio-clips document from a set of SMIL documents.</p>
    </p:documentation>

    <p:input port="source" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>SMIL documents (all versions)</p>
            <p>It is assumed that SMIL and content documents have been prepared so that all ID
            attributes are unique in the whole publication.</p>
        </p:documentation>
    </p:input>

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The d:audio-clips document</p>
        </p:documentation>
    </p:output>

    <p:option name="output-base-uri" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The base URI of the d:audio-clips document</p>
        </p:documentation>
    </p:option>

    <p:xslt template-name="create-map">
        <p:input port="stylesheet">
            <p:document href="smil-to-audio-clips.xsl"/>
        </p:input>
        <p:with-param name="output-base-uri" select="$output-base-uri">
            <p:empty/>
        </p:with-param>
        <p:with-option name="output-base-uri" select="$output-base-uri">
            <p:empty/>
        </p:with-option>
    </p:xslt>

</p:declare-step>
