<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:odt="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
                xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0"
                exclude-inline-prefixes="#all"
                type="odt:store" name="store">
    
    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>
    
    <p:option name="href" required="true"/>
    
    <p:output port="result" primary="false">
        <p:pipe step="result" port="result"/>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:normalize-uri
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:zip-manifest-from-fileset
            px:fileset-add-entry
            px:fileset-store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl">
        <p:documentation>
            px:zip
        </p:documentation>
    </p:import>
    
    <p:variable name="base" select="//d:file[starts-with(@media-type, 'application/vnd.oasis.opendocument')]
                                    /resolve-uri(@href, base-uri(.))"/>
    
    <px:normalize-uri name="href">
        <p:with-option name="href" select="$href"/>
    </px:normalize-uri>
    
    <!-- ================= -->
    <!-- Generate manifest -->
    <!-- ================= -->
    
    <p:xslt name="manifest">
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
    
    <px:set-base-uri>
        <p:with-option name="base-uri" select="resolve-uri('mimetype', $base)"/>
    </px:set-base-uri>
    <p:add-xml-base name="mimetype"/>
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
    
    <px:zip compression-method="deflated" cx:depends-on="fileset-store">
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:input port="manifest">
            <p:pipe port="result" step="zip-manifest"/>
        </p:input>
        <p:with-option name="href" select="/c:result/string()">
            <p:pipe step="href" port="normalized"/>
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
