<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc"
           xmlns:dotify="http://code.google.com/p/dotify/"
           version="1.0">
    
    <p:declare-step type="dotify:obfl-to-pef">
        <p:input port="source" sequence="false"/>
        <p:output port="result" sequence="false"/>
        <p:option name="locale" required="true"/>
        <p:option name="mode" required="true"/>
        <p:option name="identifier" required="false" select="''"/>
        <p:input port="parameters" kind="parameter" primary="false"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/dotify/calabash/impl/OBFLToPEFStep.java
        -->
    </p:declare-step>
    
</p:library>
