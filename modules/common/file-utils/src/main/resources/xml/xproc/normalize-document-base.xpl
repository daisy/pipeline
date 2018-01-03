<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:normalize-document-base"
                version="1.0">
    
    <p:input port="source" sequence="true"/>
    <p:output port="result" sequence="true"/>
    
    <p:import href="normalize-uri.xpl"/>
    
    <p:for-each name="for-each">
        <p:variable name="has-attribute" select="if (/*/@xml:base) then 'true' else 'false'"/>
        
        <px:normalize-uri name="normalized-base-uri">
            <p:with-option name="href" select="base-uri(/*)">
                <p:pipe port="current" step="for-each"/>
            </p:with-option>
        </px:normalize-uri>
        
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="/*/text()">
                <p:pipe port="normalized" step="normalized-base-uri"/>
            </p:with-option>
        </p:add-attribute>
        
        <p:choose>
            <p:when test="$has-attribute = 'false'">
                <p:delete match="/*/@xml:base"/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    
</p:declare-step>
