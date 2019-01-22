<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-pef" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:option name="epub" required="true"/>
    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>
    <p:output port="fileset.out" primary="true">
        <p:pipe port="result" step="fileset.out"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="result" step="in-memory.out"/>
    </p:output>
    <p:output port="obfl" sequence="true"> <!-- sequence=false when include-obfl=true -->
        <p:pipe step="transform" port="obfl"/>
    </p:output>
    
    <p:input kind="parameter" port="parameters" sequence="true">
        <p:inline>
            <c:param-set/>
        </p:inline>
    </p:input>
    
    <p:option name="default-stylesheet" select="'http://www.daisy.org/pipeline/modules/braille/epub3-to-pef/css/default.css'"/>
    <p:option name="stylesheet" select="''"/>
    <p:option name="apply-document-specific-stylesheets" select="'false'"/>
    <p:option name="transform" select="'(translator:liblouis)(formatter:dotify)'"/>
    <p:option name="include-obfl" select="'false'"/>
    
    <!-- Empty temporary directory dedicated to this conversion -->
    <p:option name="temp-dir" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/xml-to-pef/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    
    <!-- Ensure that there's exactly one c:param-set -->
    <px:merge-parameters name="parameters" px:progress=".01">
        <p:input port="source">
            <p:pipe step="main" port="parameters"/>
        </p:input>
    </px:merge-parameters>
    
    <!-- Load XHTML documents in spine order. -->
    <px:fileset-load px:message="Load XHTML documents in spine order" px:progress=".04"
                     media-types="application/oebps-package+xml application/xhtml+xml">
        <p:input port="fileset">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <p:for-each>
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="base-uri(/*)"/>
        </p:add-attribute>
    </p:for-each>
    <p:wrap-sequence wrapper="wrapper"/>
    <p:xslt px:progress=".01">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="../xslt/get-epub3-spine.xsl"/>
        </p:input>
    </p:xslt>
    <p:filter select="/*/*"/>
    
    <!-- In case there exists any CSS in the EPUB already, and $apply-document-specific-stylesheets = 'true',  then inline that CSS. -->
    <p:for-each px:message="Processing CSS that is already present in the EPUB" px:progress=".09">
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="base-uri(/*)"/>
        </p:add-attribute>
        
        <p:choose px:progress="1/2">
            <p:when test="$apply-document-specific-stylesheets='true'">
                <px:message>
                    <p:with-option name="message" select="concat('Inlining document-specific CSS for ',replace(base-uri(/*),'.*/',''),'')"/>
                </px:message>
                <px:apply-stylesheets px:progress="1">
                    <p:input port="parameters">
                        <p:pipe step="parameters" port="result"/>
                    </p:input>
                </px:apply-stylesheets>
            </p:when>
            <p:otherwise>
                <p:delete match="@style"/>
            </p:otherwise>
        </p:choose>
        <css:delete-stylesheets px:progress="1/2"/>
        <p:filter select="/*/html:body"/>
        
        <!-- xml:base attribute is required for resolving cross-references between different bodies -->
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="base-uri(/*)"/>
        </p:add-attribute>
    </p:for-each>
    <p:identity name="spine-bodies"/>
    
    <!-- Convert OPF metadata to HTML metadata. -->
    <px:fileset-load media-types="application/oebps-package+xml">
        <p:input port="fileset">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="opf"/>
    <p:xslt px:message="Convert OPF metadata to HTML metadata" px:progress=".01">
        <p:input port="stylesheet">
            <p:document href="../xslt/opf-to-html-head.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    <p:identity name="opf-as-head"/>
    
    <!-- Create a new HTML document with <head> based on the OPF and all <body> elements from the input HTML documents -->
    <p:wrap-sequence wrapper="html" wrapper-namespace="http://www.w3.org/1999/xhtml">
        <p:input port="source">
            <p:pipe port="result" step="opf-as-head"/>
            <p:pipe port="result" step="spine-bodies"/>
        </p:input>
    </p:wrap-sequence>
    <p:add-attribute match="/*" attribute-name="xml:lang">
        <p:with-option name="attribute-value" select="(/*/opf:metadata/dc:language[not(@refines)])[1]/text()">
            <p:pipe port="result" step="opf"/>
        </p:with-option>
    </p:add-attribute>
    
    <p:xslt px:message="Generating table of contents" px:progress=".01">
        <p:input port="stylesheet">
            <p:document href="http://www.daisy.org/pipeline/modules/braille/xml-to-pef/generate-toc.xsl"/>
        </p:input>
        <p:with-param name="depth" select="/*/*[@name='toc-depth']/@value">
            <p:pipe step="parameters" port="result"/>
        </p:with-param>
    </p:xslt>
    
    <p:group px:message="Inlining global CSS" px:progress=".10">
        <p:variable name="abs-stylesheet"
                    select="string-join(for $s in tokenize($stylesheet,'\s+')[not(.='')]
                                        return resolve-uri($s,$epub),' ')"/>
        <p:variable name="first-css-stylesheet"
                    select="tokenize($abs-stylesheet,'\s+')[matches(.,'\.s?css$')][1]"/>
        <p:variable name="first-css-stylesheet-index"
                    select="(index-of(tokenize($abs-stylesheet,'\s+')[not(.='')], $first-css-stylesheet),10000)[1]"/>
        <p:variable name="stylesheets-to-be-inlined"
                    select="string-join((
                              (tokenize($abs-stylesheet,'\s+')[not(.='')])[position()&lt;$first-css-stylesheet-index],
                              $default-stylesheet,
                              resolve-uri('../../css/default.scss'),
                              (tokenize($abs-stylesheet,'\s+')[not(.='')])[position()&gt;=$first-css-stylesheet-index]),' ')">
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
    
    <p:group px:message="Transforming MathML" px:progress=".10">
        <p:variable name="lang" select="(/*/opf:metadata/dc:language[not(@refines)])[1]/text()">
            <p:pipe port="result" step="opf"/>
        </p:variable>
        <p:viewport px:progress="1"
                    match="math:math">
            <px:transform>
                <p:with-option name="query" select="concat('(input:mathml)(locale:',$lang,')')"/>
                <p:with-option name="temp-dir" select="$temp-dir"/>
            </px:transform>
        </p:viewport>
    </p:group>
    
    <p:choose name="transform" px:progress=".61">
        <p:variable name="lang" select="(/*/opf:metadata/dc:language[not(@refines)])[1]/text()">
            <p:pipe port="result" step="opf"/>
        </p:variable>
        <p:when test="$include-obfl='true'">
            <p:output port="pef" primary="true"/>
            <p:output port="obfl">
                <p:pipe step="obfl" port="result"/>
            </p:output>
            <p:group name="obfl" px:message="Transforming from XML with inline CSS to OBFL" px:progress=".40">
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
            <p:group px:message="Transforming from OBFL to PEF" px:progress=".60">
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
        </p:when>
        <p:otherwise px:message="Transforming from XML with inline CSS to PEF" >
            <p:output port="pef" primary="true"/>
            <p:output port="obfl">
                <p:empty/>
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
    
    <p:identity name="pef"/>

    <p:identity>
        <p:input port="source">
            <p:pipe step="pef" port="result"/>
            <p:pipe step="opf" port="result"/>
        </p:input>
    </p:identity>
    <p:xslt px:message="Adding metadata to PEF based on EPUB 3 package document metadata" px:progress=".01">
        <p:input port="stylesheet">
            <p:document href="http://www.daisy.org/pipeline/modules/braille/pef-utils/add-opf-metadata-to-pef.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <px:set-base-uri>
        <p:with-option name="base-uri" select="replace(base-uri(/*),'[^/]+$',concat(((/*/opf:metadata/dc:identifier[not(@refines)]/text()), 'pef')[1],'.pef'))">
            <p:pipe port="result" step="opf"/>
        </p:with-option>
    </px:set-base-uri>
    <p:identity name="in-memory.out"/>
    
    <px:fileset-create>
        <p:with-option name="base" select="replace(base-uri(/*),'[^/]+$','')"/>
    </px:fileset-create>
    <px:fileset-add-entry px:progress=".01"
                          media-type="application/x-pef+xml">
        <p:with-option name="href" select="base-uri(/*)">
            <p:pipe port="result" step="in-memory.out"/>
        </p:with-option>
    </px:fileset-add-entry>
    <p:identity name="fileset.out"/>
    
</p:declare-step>
