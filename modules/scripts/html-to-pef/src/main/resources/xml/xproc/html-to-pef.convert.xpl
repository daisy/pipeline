<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-inline-prefixes="#all"
                type="px:html-to-pef" name="main">
    
    <p:input port="source.fileset" primary="true">
        <p:documentation>HTML fileset</p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true">
        <p:empty/>
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
    
    <p:option name="default-stylesheet" required="false" select="'#default'"/>
    <p:option name="stylesheet" select="''"/>
    <p:option name="transform" select="'(translator:liblouis)(formatter:dotify)'"/>
    <p:option name="include-obfl" select="'false'" cx:as="xs:string"/>
    
    <!-- Empty temporary directory dedicated to this conversion -->
    <p:option name="temp-dir" required="true"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
        <p:documentation>
            px:parse-query
            px:transform
            px:merge-parameters
            px:apply-stylesheets
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl">
        <p:documentation>
            pef:add-metadata
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-to-epub3/library.xpl">
        <p:documentation>
            px:html-to-opf-metadata
        </p:documentation>
    </p:import>
    
    <!-- Ensure that there's exactly one c:param-set -->
    <px:merge-parameters name="parameters" px:progress=".01">
        <p:input port="source">
            <p:pipe step="main" port="parameters"/>
        </p:input>
    </px:merge-parameters>
    <p:sink/>
    
    <!-- Parse transform query to a c:param-set -->
    <px:parse-query name="parsed-transform-query">
        <p:with-option name="query" select="$transform"/>
    </px:parse-query>
    <p:sink/>

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
    
    <p:group px:message="Applying style sheets" px:progress=".11">
        <p:variable name="first-css-stylesheet"
                    select="tokenize($stylesheet,'\s+')[matches(.,'\.s?css$')][1]"/>
        <p:variable name="first-css-stylesheet-index"
                    select="(if (exists($first-css-stylesheet))
                               then index-of(tokenize($stylesheet,'\s+')[not(.='')], $first-css-stylesheet)
                               else (),
                             10000)[1]"/>
        <p:variable name="stylesheets-to-be-inlined"
                    select="string-join((
                              (tokenize($stylesheet,'\s+')[not(.='')])[position()&lt;$first-css-stylesheet-index],
                              if ($default-stylesheet!='#default')
                                then $default-stylesheet
                                else resolve-uri('../../css/default.css'),
                              resolve-uri('../../css/default.scss'),
                              (tokenize($stylesheet,'\s+')[not(.='')])[position()&gt;=$first-css-stylesheet-index]),' ')">
            <p:inline><_/></p:inline>
        </p:variable>
        <px:apply-stylesheets px:progress="1" px:message="stylesheets: {$stylesheets-to-be-inlined}" px:message-severity="DEBUG">
            <p:with-option name="stylesheets" select="$stylesheets-to-be-inlined"/>
            <p:with-option name="media"
                           select="concat(
                                     'embossed AND (width: ',
                                     (//c:param[@name='page-width' and not(@namespace[not(.='')])]/@value,40)[1],
                                     ') AND (height: ',
                                     (//c:param[@name='page-height' and not(@namespace[not(.='')])]/@value,25)[1],
                                     ')')">
                <p:pipe port="result" step="parameters"/>
            </p:with-option>
            <p:input port="parameters">
                <p:pipe port="result" step="parameters"/>
            </p:input>
        </px:apply-stylesheets>
    </p:group>
    
    <p:viewport match="math:math" px:progress=".10" px:message="Transforming MathML">
        <p:variable name="lang" select="(/*/@xml:lang,/*/@lang,'und')[1]">
            <p:pipe step="html" port="result"/>
        </p:variable>
        <p:variable name="locale-query" select="concat('(locale:',(//c:param[@name='locale']/@value,$lang)[1],')')">
            <p:pipe step="parsed-transform-query" port="result"/>
        </p:variable>
        <px:transform px:progress="1">
            <p:with-option name="query" select="concat('(input:mathml)',$locale-query)"/>
            <p:with-param port="parameters" name="temp-dir" select="$temp-dir"/>
            <p:input port="parameters">
                <p:pipe port="result" step="parameters"/>
            </p:input>
        </px:transform>
    </p:viewport>
    
    <p:choose name="transform" px:progress=".76">
        <p:variable name="lang" select="(/*/@xml:lang,/*/@lang,'und')[1]"/>
        <p:variable name="locale-query" select="concat('(document-locale:',$lang,')')"/>
        <p:when test="$include-obfl='true'">
            <p:output port="pef" primary="true" sequence="true"/>
            <p:output port="obfl">
                <p:pipe step="obfl" port="result"/>
            </p:output>
            <p:output port="status">
                <p:pipe step="try-pef" port="status"/>
            </p:output>
            <p:group name="obfl" px:message="Transforming from XML with CSS to OBFL" px:progress=".5">
                <p:output port="result"/>
                <p:variable name="transform-query" select="concat('(input:css)(output:obfl)',$transform,$locale-query)"/>
                <px:transform px:progress="1" px:message-severity="DEBUG" px:message="px:transform query={$transform-query}">
                    <p:with-option name="query" select="$transform-query"/>
                    <p:with-param port="parameters" name="temp-dir" select="$temp-dir"/>
                    <p:input port="parameters">
                        <p:pipe port="result" step="parameters"/>
                    </p:input>
                </px:transform>
            </p:group>
            <p:try name="try-pef" px:message="Transforming from OBFL to PEF" px:progress=".5">
                <p:group>
                    <p:output port="pef" primary="true"/>
                    <p:output port="status">
                        <p:inline>
                            <d:status result="ok"/>
                        </p:inline>
                    </p:output>
                    <p:variable name="transform-query" select="'(input:obfl)(input:text-css)(output:pef)'"/>
                    <px:transform px:progress="1" px:message-severity="DEBUG" px:message="px:transform query={$transform-query}">
                        <p:with-option name="query" select="$transform-query"/>
                        <p:with-param port="parameters" name="temp-dir" select="$temp-dir"/>
                        <p:input port="parameters">
                            <p:pipe port="result" step="parameters"/>
                        </p:input>
                    </px:transform>
                </p:group>
                <p:catch name="catch">
                    <p:output port="pef" primary="true">
                        <p:empty/>
                    </p:output>
                    <p:output port="status">
                        <p:pipe step="status" port="result"/>
                    </p:output>
                    <p:identity>
                        <p:input port="source">
                            <p:inline>
                                <d:status result="error"/>
                            </p:inline>
                        </p:input>
                    </p:identity>
                    <p:choose>
                        <p:xpath-context>
                            <p:pipe step="catch" port="error"/>
                        </p:xpath-context>
                        <p:when test="//c:error[@code='DOTIFY_FAILURE']">
                            <p:variable name="message" select="//c:error[@code='DOTIFY_FAILURE'][1]/string(.)">
                                <p:pipe step="catch" port="error"/>
                            </p:variable>
                            <p:identity px:message-severity="ERROR" px:message="{$message}"/>
                            <p:identity px:message-severity="ERROR"
                                        px:message="OBFL could not be formatted. Please style the HTML in a way it can be formatted."/>
                        </p:when>
                        <p:otherwise>
                            <px:message severity="ERROR">
                                <p:input port="error">
                                    <p:pipe step="catch" port="error"/>
                                </p:input>
                            </px:message>
                        </p:otherwise>
                    </p:choose>
                    <p:identity px:message-severity="ERROR" px:message="Failed to convert OBFL to PEF"/>
                    <p:identity name="status"/>
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
                <p:pipe step="try-pef" port="status"/>
            </p:output>
            <p:variable name="transform-query" select="concat('(input:css)(output:pef)',$transform,$locale-query)"/>
            <p:try name="try-pef" px:progress="1" px:message-severity="DEBUG" px:message="px:transform query={$transform-query}">
                <p:group>
                    <p:output port="pef" primary="true"/>
                    <p:output port="status">
                        <p:inline>
                            <d:status result="ok"/>
                        </p:inline>
                    </p:output>
                    <px:transform px:progress="1">
                        <p:with-option name="query" select="$transform-query"/>
                        <p:with-param port="parameters" name="temp-dir" select="$temp-dir"/>
                        <p:input port="parameters">
                            <p:pipe port="result" step="parameters"/>
                        </p:input>
                    </px:transform>
                </p:group>
                <p:catch name="catch">
                    <p:output port="pef" primary="true">
                        <p:empty/>
                    </p:output>
                    <p:output port="status">
                        <p:pipe step="status" port="result"/>
                    </p:output>
                    <p:identity>
                        <p:input port="source">
                            <p:inline>
                                <d:status result="error"/>
                            </p:inline>
                        </p:input>
                    </p:identity>
                    <p:choose>
                        <p:xpath-context>
                            <p:pipe step="catch" port="error"/>
                        </p:xpath-context>
                        <p:when test="//c:error[@code='DOTIFY_FAILURE']">
                            <p:variable name="message" select="//c:error[@code='DOTIFY_FAILURE'][1]/string(.)">
                                <p:pipe step="catch" port="error"/>
                            </p:variable>
                            <p:identity px:message-severity="ERROR" px:message="{$message}"/>
                            <p:identity px:message-severity="ERROR"
                                        px:message="OBFL could not be formatted. Please style the HTML in a way it can be formatted."/>
                            <p:identity px:message-severity="ERROR" px:message="Failed to convert OBFL to PEF"/>
                        </p:when>
                        <p:otherwise>
                            <px:message severity="ERROR">
                                <p:input port="error">
                                    <p:pipe step="catch" port="error"/>
                                </p:input>
                            </px:message>
                            <p:identity px:message-severity="ERROR" px:message="Failed to convert XML with inline CSS to PEF"/>
                        </p:otherwise>
                    </p:choose>
                    <p:identity name="status"/>
                    <p:sink/>
                </p:catch>
            </p:try>
        </p:otherwise>
    </p:choose>
    
    <p:choose px:progress=".02">
        <p:xpath-context>
            <p:pipe step="transform" port="status"/>
        </p:xpath-context>
        <p:when test="/*/@result='ok'">
            <p:identity name="pef"/>
            <p:sink/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="html" port="result"/>
                </p:input>
            </p:identity>
            <px:html-to-opf-metadata name="metadata" px:message="Extracting metadata from HTML" px:progress="1/2"/>
            <pef:add-metadata px:message="Adding metadata to PEF" px:progress="1/2">
                <p:input port="source">
                    <p:pipe step="pef" port="result"/>
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
