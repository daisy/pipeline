<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:odt="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
                xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-inline-prefixes="#all"
                type="px:dtbook-to-odt" name="main">
    
    <!-- WARNING! This converter doesn't produce valid OpenDocument Text as such.
       It relies on LibreOffice to do some fixing. Opening the ODT file in MS Word
       directly might fail. One way of dealing with this is to add an `odt:save-as`
       post-step, opening the ODT file in LibreOffice and saving it again as ODT.
    -->
    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>
    <p:input port="meta" sequence="true"/>
    <p:input port="parameters" kind="parameter"/>
    
    <p:input port="content.xsl">
        <p:documentation> The main XSLT </p:documentation>
    </p:input>
    
    <p:output port="fileset.out" primary="true"/>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe step="separate-mathml" port="in-memory.out"/>
    </p:output>
    
    <p:option name="template" required="true">
        <p:documentation>
            OpenOffice template file (.ott) that contains the style definitions.
        </p:documentation>
    </p:option>
    <p:option name="asciimath" required="true">
        <p:documentation>
            How to render ASCIIMath-encoded formulas? `ASCIIMATH` or `MATHML`.
        </p:documentation>
    </p:option>
    <p:option name="images" required="true">
        <p:documentation>
            How to render images? `EMBED` or `LINK`.
        </p:documentation>
    </p:option>
    
    <p:option name="temp-dir" required="true">
        <!-- empty temporary directory dedicated to this conversion -->
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:mkdir
            px:copy-resource
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-join
            px:fileset-update
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/odf-utils/library.xpl">
        <p:documentation>
            odt:load
            odt:get-file
            odt:embed-images
            odt:separate-mathml
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/asciimath-utils/library.xpl"/>
    
    <!-- =========== -->
    <!-- LOAD DTBOOK -->
    <!-- =========== -->
    
    <px:fileset-load media-types="application/x-dtbook+xml" name="dtbook">
        <p:input port="in-memory">
            <p:pipe step="main" port="in-memory.in"/>
        </p:input>
    </px:fileset-load>
    <p:sink/>
    
    <!-- ============= -->
    <!-- LOAD TEMPLATE -->
    <!-- ============= -->
    
    <px:mkdir name="mkdir">
        <!-- because copy-resource does not create parent directory -->
        <p:with-option name="href" select="$temp-dir"/>
    </px:mkdir>
    <p:group name="template">
        <p:output port="fileset.out" primary="true"/>
        <p:output port="in-memory.out" sequence="true">
            <p:pipe step="load-template" port="in-memory.out"/>
        </p:output>
        <p:variable name="dtbook-name" select="replace(p:base-uri(/),'^.*/([^/]*)\.[^/\.]*$','$1')">
            <p:pipe step="dtbook" port="result"/>
        </p:variable>
        <p:variable name="save-dir"
                    select="resolve-uri(concat($dtbook-name,'.odt/'),string(/c:result))">
            <p:pipe step="mkdir" port="result"/>
        </p:variable>
        <p:variable name="template-copy" select="resolve-uri(replace($template,'^.*/([^/]+)$','$1'),string(/c:result))">
            <p:pipe step="mkdir" port="result"/>
        </p:variable>
        <px:copy-resource>
            <p:with-option name="href" select="$template"/>
            <p:with-option name="target" select="$template-copy"/>
        </px:copy-resource>
        <odt:load name="load-template">
            <p:with-option name="href" select="string(/c:result)"/>
            <p:with-option name="target" select="$save-dir"/>
        </odt:load>
    </p:group>
    <p:sink/>
    
    <odt:get-file href="content.xml" name="template-content">
        <p:input port="fileset.in">
            <p:pipe step="template" port="fileset.out"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe step="template" port="in-memory.out"/>
        </p:input>
    </odt:get-file>
    <p:sink/>
    
    <odt:get-file href="styles.xml" name="template-styles">
        <p:input port="fileset.in">
            <p:pipe step="template" port="fileset.out"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe step="template" port="in-memory.out"/>
        </p:input>
    </odt:get-file>
    <p:sink/>
    
    <odt:get-file href="meta.xml" name="template-meta">
        <p:input port="fileset.in">
            <p:pipe step="template" port="fileset.out"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe step="template" port="in-memory.out"/>
        </p:input>
    </odt:get-file>
    <p:sink/>
    
    <px:fileset-join name="template-content-styles-meta-fileset">
        <p:input port="source">
            <p:pipe step="template-content" port="result.fileset"/>
            <p:pipe step="template-styles" port="result.fileset"/>
            <p:pipe step="template-meta" port="result.fileset"/>
        </p:input>
    </px:fileset-join>
    <p:sink/>
    
    <!-- =================== -->
    <!-- ASCIIMATH TO MATHML -->
    <!-- =================== -->
    
    <p:identity>
        <p:input port="source">
            <p:pipe step="dtbook" port="result"/>
        </p:input>
    </p:identity>
    <p:choose>
        <p:when test="$asciimath=('MATHML', 'BOTH')">
            <p:viewport match="dtb:span[@class='asciimath']">
                <px:asciimath-to-mathml>
                    <p:with-option name="asciimath" select="string(.)"/>
                </px:asciimath-to-mathml>
            </p:viewport>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:identity name="maybe-convert-asciimath-to-mathml"/>
    
    <!-- ============================= -->
    <!-- MODIFY CONTENT, STYLES & META -->
    <!-- ============================= -->
    
    <p:xslt name="content.temp">
        <p:input port="source">
            <p:pipe step="template-content" port="result"/>
            <p:pipe step="maybe-convert-asciimath-to-mathml" port="result"/>
        </p:input>
        <p:input port="stylesheet">
            <p:pipe step="main" port="content.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:pipe step="main" port="parameters"/>
        </p:input>
        <p:with-param port="parameters" name="asciimath" select="$asciimath"/>
        <p:with-param port="parameters" name="images" select="$images"/>
    </p:xslt>
    
    <p:xslt name="content">
        <p:input port="stylesheet">
            <p:document href="automatic-styles.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <p:xslt name="styles">
        <p:input port="source">
            <p:pipe step="template-styles" port="result"/>
            <p:pipe step="content.temp" port="result"/>
            <p:pipe step="dtbook" port="result"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="styles.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    <p:sink/>
    
    <p:insert name="meta.temp" match="/office:document-meta/office:meta" position="first-child">
        <p:input port="source">
            <p:pipe step="template-meta" port="result"/>
        </p:input>
        <p:input port="insertion">
            <p:pipe step="main" port="meta"/>
        </p:input>
    </p:insert>
    
    <p:xslt name="meta">
        <p:input port="source">
            <p:pipe step="meta.temp" port="result"/>
            <p:pipe step="dtbook" port="result"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="meta.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <px:fileset-update name="update-files">
        <p:input port="update.fileset">
            <p:pipe step="template-content-styles-meta-fileset" port="result"/>
        </p:input>
        <p:input port="update.in-memory">
            <p:pipe step="content" port="result"/>
            <p:pipe step="styles" port="result"/>
            <p:pipe step="meta" port="result"/>
        </p:input>
        <p:input port="source.fileset">
            <p:pipe step="template" port="fileset.out"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="template" port="in-memory.out"/>
        </p:input>
    </px:fileset-update>
    
    <!-- ============ -->
    <!-- EMBED IMAGES -->
    <!-- ============ -->
    
    <p:choose name="maybe-embed-images">
        <p:when test="$images='EMBED'">
            <p:output port="fileset.out" primary="true"/>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe step="embed-images" port="in-memory.out"/>
            </p:output>
            <odt:embed-images name="embed-images">
                <p:input port="fileset.in"/>
                <p:input port="in-memory.in">
                    <p:pipe step="update-files" port="result.in-memory"/>
                </p:input>
                <p:input port="original-fileset">
                    <p:pipe step="main" port="fileset.in"/>
                </p:input>
            </odt:embed-images>
        </p:when>
        <p:otherwise>
            <p:output port="fileset.out" primary="true">
                <p:pipe step="update-files" port="result.fileset"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe step="update-files" port="result.in-memory"/>
            </p:output>
            <p:sink>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:sink>
        </p:otherwise>
    </p:choose>
    
    <!-- =============== -->
    <!-- SEPARATE MATHML -->
    <!-- =============== -->
    
    <odt:separate-mathml name="separate-mathml">
        <p:input port="fileset.in"/>
        <p:input port="in-memory.in">
            <p:pipe step="maybe-embed-images" port="in-memory.out"/>
        </p:input>
    </odt:separate-mathml>
    
</p:declare-step>
