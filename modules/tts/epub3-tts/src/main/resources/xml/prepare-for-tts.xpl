<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:prepare-for-tts"
                exclude-inline-prefixes="#all">

    <p:input port="source" primary="true"/>
    <p:output port="result" primary="true"/>
    <p:documentation>
        Prepare HTML document for TTS:
        - Add numbers to ordered lists.
    </p:documentation>
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="prepare-for-tts.xsl"></p:document>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>

</p:declare-step>

