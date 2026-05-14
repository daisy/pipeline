<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:normalize-document-base"
                version="1.0">
    
    <p:input port="source"/>
    <p:output port="result"/>
    
    <p:import href="normalize-uri.xpl">
        <p:documentation>
            px:normalize-uri
        </p:documentation>
    </p:import>
    <p:import href="set-base-uri.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>

    <px:normalize-uri name="normalized-base-uri">
        <p:with-option name="href" select="base-uri(/*)"/>
    </px:normalize-uri>
    
    <px:set-base-uri>
        <p:with-option name="base-uri" select="/*/text()">
            <p:pipe port="normalized" step="normalized-base-uri"/>
        </p:with-option>
    </px:set-base-uri>
    
</p:declare-step>
