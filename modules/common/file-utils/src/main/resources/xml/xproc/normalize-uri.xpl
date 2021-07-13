<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="#all"
                type="px:normalize-uri"
                name="main">
    
    <!-- step behaves similar to p:identity -->
    <p:input port="source" primary="true" sequence="true">
        <p:empty/>
    </p:input>
    <p:output port="result" primary="true" sequence="true">
        <p:pipe port="source" step="main"/>
    </p:output>
    
    <!-- the normalized URI is made available on a secondary port -->
    <p:output port="normalized" primary="false">
        <p:pipe port="result" step="normalized"/>
    </p:output>
    
    <!-- the href to normalize -->
    <p:option name="href" required="true"/>
    
    <cx:import href="../xslt/uri-functions.xsl" type="application/xslt+xml">
        <p:documentation>
            pf:normalize-uri
        </p:documentation>
    </cx:import>

    <p:sink/>
    <p:string-replace match="/*/text()">
        <p:input port="source">
            <p:inline>
                <c:result>[href]</c:result>
            </p:inline>
        </p:input>
        <p:with-option name="replace" select="concat('&quot;',pf:normalize-uri($href),'&quot;')"/>
    </p:string-replace>
    <p:identity name="normalized"/>
    
</p:declare-step>
