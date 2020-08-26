<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:odt="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
    xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0"
    exclude-inline-prefixes="#all"
    type="odt:load" version="1.0">
    
    <!-- The .odt or .ott file. Should be a file: URI -->
    <p:option name="href" required="true"/>
    
    <!-- Directory where the fileset will ultimately be stored -->
    <p:option name="target" required="false" select="''"/>
    
    <p:output port="fileset.out" primary="true">
        <p:pipe step="fileset" port="result"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:empty/>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl"/>
    <p:import href="manifest-to-fileset.xpl">
        <p:documentation>
            px:odf-manifest-to-fileset
        </p:documentation>
    </p:import>
    
    <!-- ==================== -->
    <!-- Extract ODT manifest -->
    <!-- ==================== -->
    
    <px:unzip file="META-INF/manifest.xml" content-type="application/xml" name="manifest">
        <p:with-option name="href" select="$href"/>
    </px:unzip>
    
    <!-- =============== -->
    <!-- Change mimetype -->
    <!-- =============== -->
    
    <p:add-attribute match="//manifest:file-entry[@manifest:full-path='/']"
                     attribute-name="manifest:media-type" attribute-value="application/vnd.oasis.opendocument.text"/>
    
    <!-- =========================== -->
    <!-- Convert manifest to fileset -->
    <!-- =========================== -->
    
    <px:odf-manifest-to-fileset name="fileset">
        <p:with-option name="base" select="$target"/>
        <p:with-option name="original-base" select="concat($href, '!/')"/>
    </px:odf-manifest-to-fileset>
    
</p:declare-step>
