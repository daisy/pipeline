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
    
    <p:declare-step type="dotify:file-to-obfl">
        <p:option name="source" required="true"/> <!-- must be absolute URI -->
        <p:output port="result"/>
        <p:option name="locale" required="true"/>
        <p:option name="format" required="false" select="'obfl'"/>
        
        <!--
            Configuration can currently be done in 3 different ways: options, parameters and query
            syntax.
            
            FIXME:
            - Use of query syntax should be limited to the selection of converters and should
              represent a list of features. The purpose of the query syntax is to have something
              universal and textual. In the context of an XProc step however it might make more sense
              to use options (for general features, like "locale" and "format") and parameters (for
              more specific features).
            - Options should be used for general settings not specific to a converter.
            - Parameters should be used for settings specific to a converter.
        -->
        
        <!-- Parameters -->
        <p:input port="parameters" kind="parameter" primary="false"/>
        
        <!-- Query syntax -->
        <p:option name="dotify-options" required="false"/>
        
        <!-- Options -->
        <p:option name="template" required="false" select="'default'"/>
        <p:option name="rows" required="false" select="29"/>
        <p:option name="cols" required="false" select="28"/>
        <p:option name="inner-margin" required="false" select="2"/>
        <p:option name="outer-margin" required="false" select="2"/>
        <p:option name="rowgap" required="false" select="0"/>
        <p:option name="splitterMax" required="false" select="50"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/dotify/calabash/impl/FileToOBFLStep.java
        -->
    </p:declare-step>
    
</p:library>
