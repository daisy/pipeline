<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:odt="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
    xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0"
    exclude-inline-prefixes="#all"
    type="odt:store" name="store" version="1.0">
    
    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>
    
    <p:option name="href" required="true"/>
    
    <p:output port="result" primary="false">
        <p:pipe step="result" port="result"/>
    </p:output>
    
    <p:import href="utils/normalize-uri.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl"/>
    
    <p:variable name="base" select="//d:file[starts-with(@media-type, 'application/vnd.oasis.opendocument')]
                                    /resolve-uri(@href, base-uri(.))"/>
    
    <pxi:normalize-uri name="href">
        <p:with-option name="href" select="$href"/>
    </pxi:normalize-uri>
    <p:sink/>
    
    <!-- ================= -->
    <!-- Generate manifest -->
    <!-- ================= -->
    
    <p:xslt name="manifest">
        <p:input port="source">
            <p:pipe step="store" port="fileset.in"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="manifest-from-fileset.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    <p:sink/>
    
    <!-- Remove directories from fileset -->
    <p:delete match="//d:file[ends-with(resolve-uri(@href, base-uri(.)), '/')]">
        <p:input port="source">
            <p:pipe step="store" port="fileset.in"/>
        </p:input>
    </p:delete>
    
    <px:fileset-add-entry media-type="application/xml" name="fileset.with-manifest">
        <p:with-option name="href" select="resolve-uri('META-INF/manifest.xml', $base)"/>
    </px:fileset-add-entry>
    <p:sink/>
    
    <!-- ================= -->
    <!-- Generate mimetype -->
    <!-- ================= -->
    
    <p:string-replace match="/c:data/text()">
        <p:input port="source">
            <p:inline>
                <c:data content-type="text/plain">$mimetype</c:data>
            </p:inline>
        </p:input>
        <p:with-option name="replace" select="concat('&quot;', //manifest:file-entry[@manifest:full-path='/']/@manifest:media-type, '&quot;')">
            <p:pipe step="manifest" port="result"/>
        </p:with-option>
    </p:string-replace>
    <p:string-replace match="text()" replace="normalize-space()"/>
    
    <p:add-attribute match="/*" attribute-name="xml:base" name="mimetype">
        <p:with-option name="attribute-value" select="resolve-uri('mimetype', $base)"/>
    </p:add-attribute>
    <p:sink/>
    
    <px:fileset-add-entry first="true" media-type="text/plain" name="fileset.with-mimetype">
        <p:input port="source">
            <p:pipe step="fileset.with-manifest" port="result"/>
        </p:input>
        <p:with-option name="href" select="resolve-uri('mimetype', $base)"/>
    </px:fileset-add-entry>
    
    <!-- =================== -->
    <!-- Store files to disk -->
    <!-- =================== -->
    
    <px:fileset-store name="fileset-store">
        <p:input port="in-memory.in">
            <p:pipe step="store" port="in-memory.in"/>
            <p:pipe step="manifest" port="result"/>
            <p:pipe step="mimetype" port="result"/>
        </p:input>
    </px:fileset-store>
    
    <!-- ====== -->
    <!-- Zip up -->
    <!-- ====== -->
    
    <px:zip-manifest-from-fileset>
        <p:input port="source">
            <p:pipe step="fileset.with-mimetype" port="result"/>
        </p:input>
    </px:zip-manifest-from-fileset>
    
    <p:add-attribute name="zip-manifest" match="c:entry[@name='mimetype']" attribute-name="compression-method" attribute-value="stored"/>
    
    <px:zip compression-method="deflated">
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:input port="manifest">
            <p:pipe port="result" step="zip-manifest"/>
        </p:input>
        <p:with-option name="href" select="/c:result/string()">
            <p:pipe step="href" port="result"/>
        </p:with-option>
    </px:zip>
    
    <p:string-replace match="/c:result/text()" name="result">
        <p:input port="source">
            <p:inline>
                <c:result>$href</c:result>
            </p:inline>
        </p:input>
        <p:with-option name="replace" select="concat('&quot;', /c:zipfile/@href, '&quot;')"/>
    </p:string-replace>
    
</p:declare-step>
