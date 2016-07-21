<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:extract"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:louis="http://liblouis.org/liblouis"
    exclude-inline-prefixes="#all"
    version="1.0">
    
    <p:input port="source" primary="true"/>
    <p:option name="match" required="true"/>
    <p:option name="label" required="true"/>
    <p:output port="result" primary="true"/>
    <p:output port="extracted" sequence="true">
        <p:pipe step="filter" port="result"/>
    </p:output>
    
    <p:variable name="base" select="base-uri(/*)"/>
    
    <p:label-elements attribute="xml:base">
        <p:with-option name="match" select="$match"/>
        <p:with-option name="label" select="concat('concat(&quot;',replace($base,'.xml$',''),'/&quot;,',$label,',&quot;.xml&quot;)')"/>
    </p:label-elements>
    
    <p:wrap wrapper="louis:_include_">
        <p:with-option name="match" select="$match"/>
    </p:wrap>
    
    <p:viewport match="louis:_include_" name="include">
        <p:output port="result" primary="true"/>
        <p:rename match="/*" new-name="louis:include"/>
        <p:add-attribute match="/*" attribute-name="href">
            <p:with-option name="attribute-value" select="base-uri(/*/*)"/>
        </p:add-attribute>
    </p:viewport>
    
    <p:filter name="filter">
        <p:with-option name="select" select="concat('//', $match)"/>
    </p:filter>
    <p:sink/>
    
    <p:delete>
        <p:input port="source">
            <p:pipe step="include" port="result"/>
        </p:input>
        <p:with-option name="match" select="$match"/>
    </p:delete>
    
</p:declare-step>
