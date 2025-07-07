<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:zedai-to-pef" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:empty/>
    </p:input>
    <p:output port="result" px:media-type="application/x-pef+xml"/>
    
    <p:option name="default-stylesheet" required="false" select="'#default'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The user agent's <a href="https://www.w3.org/TR/CSS2/cascade.html#cascade">default
            style sheet</a>.</p>
            <p>The idea of this option is that a custom "ZedAI to PEF" script could be written with
            its own default style sheet.</p>
        </p:documentation>
    </p:option>
    <p:option name="stylesheet" required="false" select="''"/>
    <p:option name="transform" required="false" select="''"/>
    
    <p:option name="temp-dir" required="true">
        <p:documentation>
            Empty temporary directory dedicated to this conversion
        </p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/zedai-to-epub3/library.xpl">
        <p:documentation>
            px:zedai-to-opf-metadata
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
        <p:documentation>
            px:transform
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
        <p:documentation>
            px:css-cascade
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl">
        <p:documentation>
            px:pef-add-metadata
        </p:documentation>
    </p:import>
    
    <px:fileset-load media-types="application/z3998-auth+xml">
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <px:assert message="No ZedAI document found in the fileset." test-count-min="1" error-code="PEZE00"/>
    <px:assert message="More than one ZedAI document found in the fileset." test-count-max="1" error-code="PEZE00"/>
    <p:identity name="zedai"/>
    
    <px:css-cascade media="embossed">
        <p:with-option name="user-stylesheet"
                       select="concat(
                                 if ($default-stylesheet!='#default')
                                   then $default-stylesheet
                                   else resolve-uri('../css/default.css'),
                                 ' ',
                                 $stylesheet)"/>
    </px:css-cascade>
    
    <p:viewport match="math:math">
        <px:transform>
            <p:with-option name="query" select="concat('(input:mathml)(locale:',(/*/@xml:lang,'und')[1],')')">
                <p:pipe step="zedai" port="result"/>
            </p:with-option>
            <p:with-param port="parameters" name="temp-dir" select="$temp-dir"/>
        </px:transform>
    </p:viewport>
    
    <px:transform name="pef">
        <p:with-option name="query" select="concat('(input:css)(output:pef)',$transform,'(document-locale:',(/*/@xml:lang,'und')[1],')')"/>
        <p:with-param port="parameters" name="temp-dir" select="$temp-dir"/>
    </px:transform>
    <p:sink/>

    <px:zedai-to-opf-metadata name="metadata">
        <p:input port="source">
            <p:pipe step="zedai" port="result"/>
        </p:input>
    </px:zedai-to-opf-metadata>
    <p:sink/>
    
    <px:pef-add-metadata>
        <p:input port="source">
            <p:pipe step="pef" port="result"/>
        </p:input>
        <p:input port="metadata">
            <p:pipe step="metadata" port="result"/>
        </p:input>
    </px:pef-add-metadata>
    
</p:declare-step>
