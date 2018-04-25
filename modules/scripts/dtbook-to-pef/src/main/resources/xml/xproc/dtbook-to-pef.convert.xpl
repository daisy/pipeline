<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:dtbook-to-pef" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:input port="source.fileset" primary="true">
        <p:documentation>DTBook fileset</p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation>DTBook files</p:documentation>
    </p:input>
    <p:output port="result" primary="true" sequence="true"> <!-- sequence=false when d:status result="ok" -->
        <p:documentation>PEF</p:documentation>
    </p:output>
    <p:output port="obfl" sequence="true"> <!-- sequence=false when include-obfl=true -->
        <p:documentation>OBFL</p:documentation>
        <p:pipe step="transform" port="obfl"/>
    </p:output>
    <p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation>Whether or not the conversion was successful. When include-obfl is true,
        the conversion may fail but still output a document on the "obfl" port.</p:documentation>
        <p:pipe step="transform" port="status"/>
    </p:output>
    
    <p:input kind="parameter" port="parameters" sequence="true">
        <p:inline>
            <c:param-set/>
        </p:inline>
    </p:input>
    
    <p:option name="default-stylesheet" select="'http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/css/default.css'"/>
    <p:option name="stylesheet" select="''"/>
    <p:option name="transform" select="'(translator:liblouis)(formatter:dotify)'"/>
    <p:option name="include-obfl" select="'false'"/>
    
    <!-- Empty temporary directory dedicated to this conversion -->
    <p:option name="temp-dir" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/xml-to-pef/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    
    <!-- Ensure that there's exactly one c:param-set -->
    <px:merge-parameters name="parameters">
        <p:input port="source">
            <p:pipe step="main" port="parameters"/>
        </p:input>
    </px:merge-parameters>
    <p:sink/>
    
    <!-- Find the first and only DTBook file -->
    <p:group name="dtbook" px:message="Loading DTBook" px:progress=".04">
        <p:output port="result"/>
        <px:fileset-load media-types="application/x-dtbook+xml">
            <p:input port="fileset">
                <p:pipe step="main" port="source.fileset"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe step="main" port="source.in-memory"/>
            </p:input>
        </px:fileset-load>
        <px:assert message="No DTBook document found." test-count-min="1" error-code="PEZE00"/>
        <px:assert message="More than one DTBook found in fileset." test-count-max="1" error-code="PEZE00"/>
    </p:group>
    
    <p:group px:message="Applying style sheets" px:progress=".08">
        <p:variable name="first-css-stylesheet"
                    select="tokenize($stylesheet,'\s+')[matches(.,'\.s?css$')][1]"/>
        <p:variable name="first-css-stylesheet-index"
                    select="(index-of(tokenize($stylesheet,'\s+')[not(.='')], $first-css-stylesheet),10000)[1]"/>
        <p:variable name="stylesheets-to-be-inlined"
                    select="string-join((
                              if (tokenize($stylesheet,'\s+') = 'http://www.daisy.org/pipeline/modules/braille/xml-to-pef/generate-toc.xsl')
                                then ()
                                else 'http://www.daisy.org/pipeline/modules/braille/xml-to-pef/generate-toc.xsl',
                              (tokenize($stylesheet,'\s+')[not(.='')])[position()&lt;$first-css-stylesheet-index],
                              $default-stylesheet,
                              resolve-uri('../../css/default.scss'),
                              (tokenize($stylesheet,'\s+')[not(.='')])[position()&gt;=$first-css-stylesheet-index]),' ')">
            <p:inline><_/></p:inline>
        </p:variable>
        <p:identity px:message="{$stylesheets-to-be-inlined}" px:message-severity="DEBUG"/>
        <px:apply-stylesheets px:progress="1">
            <p:with-option name="stylesheets" select="$stylesheets-to-be-inlined"/>
            <p:input port="parameters">
                <p:pipe port="result" step="parameters"/>
            </p:input>
        </px:apply-stylesheets>
    </p:group>
    
    <p:choose px:progress=".04">
        <p:when test="//math:math">
            <p:viewport px:message="Transforming MathML"
                        match="math:math">
                <px:transform px:progress="1">
                    <p:with-option name="query" select="concat('(input:mathml)(locale:',(/*/@xml:lang,'und')[1],')')">
                        <p:pipe step="dtbook" port="result"/>
                    </p:with-option>
                    <p:with-option name="temp-dir" select="$temp-dir"/>
                </px:transform>
            </p:viewport>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    
    <p:choose name="transform" px:progress=".83">
        <p:variable name="lang" select="(/*/@xml:lang,'und')[1]"/>
        <p:when test="$include-obfl='true'">
            <p:output port="pef" primary="true" sequence="true"/>
            <p:output port="obfl">
                <p:pipe step="obfl" port="result"/>
            </p:output>
            <p:output port="status">
                <p:pipe step="try-pef" port="status"/>
            </p:output>
            <p:group name="obfl" px:message="Transforming from DTBook XML with inline CSS to OBFL" px:progress=".95">
                <p:output port="result"/>
                <p:variable name="transform-query" select="concat('(input:css)(output:obfl)',$transform,'(locale:',$lang,')')"/>
                <p:identity px:message-severity="DEBUG" px:message="px:transform query={$transform-query}"/>
                <px:transform px:progress="1">
                    <p:with-option name="query" select="$transform-query"/>
                    <p:with-option name="temp-dir" select="$temp-dir"/>
                    <p:input port="parameters">
                        <p:pipe port="result" step="parameters"/>
                    </p:input>
                </px:transform>
            </p:group>
            <p:try name="try-pef" px:message="Transforming from OBFL to PEF" px:progress=".05">
                <p:group>
                    <p:output port="pef" primary="true"/>
                    <p:output port="status">
                        <p:inline>
                            <d:status result="ok"/>
                        </p:inline>
                    </p:output>
                    <p:variable name="transform-query" select="concat('(input:obfl)(input:text-css)(output:pef)',$transform,'(locale:',$lang,')')"/>
                    <p:identity px:message-severity="DEBUG" px:message="px:transform query={$transform-query}"/>
                    <px:transform px:progress="1">
                        <p:with-option name="query" select="$transform-query"/>
                        <p:with-option name="temp-dir" select="$temp-dir"/>
                        <p:input port="parameters">
                            <p:pipe port="result" step="parameters"/>
                        </p:input>
                    </px:transform>
                </p:group>
                <p:catch>
                    <p:output port="pef" primary="true">
                        <p:empty/>
                    </p:output>
                    <p:output port="status">
                        <p:inline>
                            <d:status result="error"/>
                        </p:inline>
                    </p:output>
                    <p:sink/>
                </p:catch>
            </p:try>
        </p:when>
        <p:otherwise px:message="Transforming from XML with inline CSS to PEF">
            <p:output port="pef" primary="true"/>
            <p:output port="obfl">
                <p:empty/>
            </p:output>
            <p:output port="status">
                <p:inline>
                    <d:status result="ok"/>
                </p:inline>
            </p:output>
            <p:variable name="transform-query" select="concat('(input:css)(output:pef)',$transform,'(locale:',$lang,')')"/>
            <p:identity px:message-severity="DEBUG" px:message="px:transform query={$transform-query}"/>
            <px:transform px:progress="1">
                <p:with-option name="query" select="$transform-query"/>
                <p:with-option name="temp-dir" select="$temp-dir"/>
                <p:input port="parameters">
                    <p:pipe port="result" step="parameters"/>
                </p:input>
            </px:transform>
        </p:otherwise>
    </p:choose>
    
    <p:choose name="add-metadata" px:progress=".01">
        <p:xpath-context>
            <p:pipe step="transform" port="status"/>
        </p:xpath-context>
        <p:when test="/*/@result='ok'">
            <p:xslt name="metadata" px:message="Extracting metadata from DTBook" px:progress="1/2">
                <p:input port="source">
                    <p:pipe step="dtbook" port="result"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../xslt/dtbook-to-metadata.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
            <pef:add-metadata px:message="Adding metadata to PEF" px:progress="1/2">
                <p:input port="source">
                    <p:pipe step="transform" port="pef"/>
                </p:input>
                <p:input port="metadata">
                    <p:pipe step="metadata" port="result"/>
                </p:input>
            </pef:add-metadata>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
