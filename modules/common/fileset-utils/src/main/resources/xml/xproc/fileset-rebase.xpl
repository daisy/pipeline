<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:fileset-rebase" name="main"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">
    
    <p:documentation>Changes the xml:base of the fileset, and updates the relative hrefs in the fileset accordingly.</p:documentation>
    
    <p:input port="source"/>
    <p:output port="result"/>
    
    <p:option name="new-base" required="true"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>

    <px:assert message="new-base must not be empty" error-code="XXX">
        <p:with-option name="test" select="not($new-base='')"/>
    </px:assert>

    <p:xslt>
        <p:with-param name="new-base" select="$new-base"/>
        <p:input port="stylesheet">
            <p:document href="../xslt/fileset-rebase.xsl"/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
