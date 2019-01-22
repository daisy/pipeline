<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:html-to-pef" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:input port="source.fileset" primary="true">
        <p:documentation>HTML fileset</p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true">
        <p:empty/>
    </p:input>
    <p:output port="result" primary="true">
        <p:documentation>PEF</p:documentation>
    </p:output>
    <p:output port="obfl" sequence="true"> <!-- sequence=false when include-obfl=true -->
        <p:documentation>OBFL</p:documentation>
        <p:pipe step="transform" port="obfl"/>
    </p:output>
    
    <p:input kind="parameter" port="parameters" sequence="true">
        <p:inline>
            <c:param-set/>
        </p:inline>
    </p:input>
    
    <p:option name="default-stylesheet" select="'http://www.daisy.org/pipeline/modules/braille/html-to-pef/css/default.css'"/>
    <p:option name="stylesheet" select="''"/>
    <p:option name="transform" select="'(translator:liblouis)(formatter:dotify)'"/>
    <p:option name="include-obfl" select="'false'"/>
    
    <!-- Empty temporary directory dedicated to this conversion -->
    <p:option name="temp-dir" required="true"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
        <p:documentation>
            px:transform
            px:merge-parameters
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/xml-to-pef/library.xpl">
        <p:documentation>
            px:apply-stylesheets
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl">
        <p:documentation>
            pef:add-metadata
        </p:documentation>
    </p:import>
    
    <!-- Ensure that there's exactly one c:param-set -->
    <px:merge-parameters name="parameters" px:progress=".01">
        <p:input port="source">
            <p:pipe step="main" port="parameters"/>
        </p:input>
    </px:merge-parameters>
    
    <!-- Load HTML -->
    <px:fileset-load media-types="application/xhtml+xml">
        <p:input port="fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <px:assert message="No XHTML documents found." test-count-min="1" error-code="PEZE00"/>
    <px:assert message="More than one XHTML documents found." test-count-max="1" error-code="PEZE00"/>
    <p:identity name="html"/>
    
    <p:xslt px:message="Generating table of contents" px:progress=".01">
        <p:input port="stylesheet">
            <p:document href="http://www.daisy.org/pipeline/modules/braille/xml-to-pef/generate-toc.xsl"/>
        </p:input>
        <p:with-param name="depth" select="/*/*[@name='toc-depth']/@value">
            <p:pipe step="parameters" port="result"/>
        </p:with-param>
    </p:xslt>
    
    <p:group px:message="Inlining CSS" px:progress=".10">
        <p:variable name="stylesheets-to-be-inlined" select="string-join((
                                                               $default-stylesheet,
                                                               resolve-uri('../../css/default.scss'),
                                                               $stylesheet),' ')">
            <p:inline><_/></p:inline>
        </p:variable>
        <p:identity px:message="stylesheets: {$stylesheets-to-be-inlined}"/>
        <px:apply-stylesheets px:progress="1">
            <p:with-option name="stylesheets" select="$stylesheets-to-be-inlined"/>
            <p:input port="parameters">
                <p:pipe port="result" step="parameters"/>
            </p:input>
        </px:apply-stylesheets>
    </p:group>
    
    <p:viewport px:message="Transforming MathML" px:progress=".10"
                match="math:math">
        <px:transform>
            <p:with-option name="query" select="concat('(input:mathml)(locale:',(/*/@xml:lang,'und')[1],')')">
                <p:pipe step="html" port="result"/>
            </p:with-option>
            <p:with-option name="temp-dir" select="$temp-dir"/>
            <p:input port="parameters">
                <!-- px:transform uses the 'duplex' parameter -->
                <p:pipe port="result" step="parameters"/>
            </p:input>
        </px:transform>
    </p:viewport>
    
    <p:choose name="transform" px:message="" px:progress=".76">
        <p:variable name="lang" select="(/*/@xml:lang,'und')[1]"/>
        <p:when test="$include-obfl='true'">
            <p:output port="pef" primary="true"/>
            <p:output port="obfl">
                <p:pipe step="obfl" port="result"/>
            </p:output>
            <px:transform name="obfl" px:message="Transforming from XML with CSS to OBFL" px:progress=".5">
                <p:with-option name="query" select="concat('(input:css)(output:obfl)',$transform,'(locale:',$lang,')')"/>
                <p:with-option name="temp-dir" select="$temp-dir"/>
                <p:input port="parameters">
                    <p:pipe port="result" step="parameters"/>
                </p:input>
            </px:transform>
            <px:transform px:message="Transforming from OBFL to PEF" px:progress=".5">
                <p:with-option name="query" select="concat('(input:obfl)(input:text-css)(output:pef)',$transform,'(locale:',$lang,')')"/>
                <p:with-option name="temp-dir" select="$temp-dir"/>
                <p:input port="parameters">
                    <p:pipe port="result" step="parameters"/>
                </p:input>
            </px:transform>
        </p:when>
        <p:otherwise>
            <p:output port="pef" primary="true"/>
            <p:output port="obfl">
                <p:empty/>
            </p:output>
            <px:transform px:message="Transforming from XML with inline CSS to PEF" px:progress="1">
                <p:with-option name="query" select="concat('(input:css)(output:pef)',$transform,'(locale:',$lang,')')"/>
                <p:with-option name="temp-dir" select="$temp-dir"/>
                <p:input port="parameters">
                    <p:pipe port="result" step="parameters"/>
                </p:input>
            </px:transform>
        </p:otherwise>
    </p:choose>
    
    <p:identity name="pef"/>
    
    <p:identity>
        <p:input port="source">
            <p:pipe step="html" port="result"/>
        </p:input>
    </p:identity>
    <p:xslt name="metadata" px:message="Extracting metadata from HTML" px:progress=".01">
        <p:input port="stylesheet">
            <p:document href="../xslt/html-to-opf-metadata.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    <pef:add-metadata px:message="Adding metadata to PEF" px:progress=".01">
        <p:input port="source">
            <p:pipe step="pef" port="result"/>
        </p:input>
        <p:input port="metadata">
            <p:pipe step="metadata" port="result"/>
        </p:input>
    </pef:add-metadata>
    
</p:declare-step>
