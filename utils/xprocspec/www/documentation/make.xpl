<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0">

    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>
    
    <p:variable name="cwd" select="replace(base-uri(/*),'^file:/*(/.*/)[^/]*$','$1')">
        <p:inline>
            <doc/>
        </p:inline>
    </p:variable>
    
    <p:identity>
        <p:input port="source">
            <p:inline exclude-inline-prefixes="#all">
                <c:request method="GET" override-content-type="text/plain; charset=utf-8"/>
            </p:inline>
        </p:input>
    </p:identity>
    <p:add-attribute match="c:request" attribute-name="href">
        <p:with-option name="attribute-value" select="'template.html'"/>
    </p:add-attribute>
    <p:http-request/>
    <!--  remove doctypes etc (<!DOCTYPE html> doesn't work with p:unescape-markup)  -->
    <p:string-replace match="/*/text()[1]" replace="replace(/*/text()[1],'^&lt;[!\?].*?(&lt;[^!\?])','$1','s')"> </p:string-replace>
    <p:unescape-markup content-type="text/html"/>
    <p:unwrap match="c:body"/>
    <p:identity name="template"/>
    <p:sink/>
    
    <p:load href="../../xprocspec/src/main/resources/content/xml/schema/xprocspec.rng"/>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="simplify-rng.xsl"/>
        </p:input>
    </p:xslt>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="simplify-rng-2.xsl"/>
        </p:input>
    </p:xslt>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="rng-to-documentation.xsl"/>
        </p:input>
    </p:xslt>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="create-element-links.xsl"/>
        </p:input>
    </p:xslt>
    <p:delete match="//*[preceding::*/@id=@id]"/>
    <p:identity name="generated"/>
    
    <p:replace match="//*[@id='replaceme']">
        <p:input port="source">
            <p:pipe port="result" step="template"/>
        </p:input>
        <p:input port="replacement">
            <p:pipe port="result" step="generated"/>
        </p:input>
    </p:replace>
    <p:store href="generated.html" name="store"/>
    
    <p:exec source-is-xml="false" result-is-xml="false" errors-is-xml="false" name="exec">
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:with-option name="cwd" select="$cwd">
            <p:pipe port="result" step="store"/>
        </p:with-option>
        <p:with-option name="command" select="'phantomjs'"/>
        <p:with-option name="args" select="concat(normalize-space(.),'/respec/tools/respec2html.js generated.html index.html')">
            <p:pipe port="result" step="pwd"/>
        </p:with-option>
    </p:exec>
    
    <p:identity name="result"/>
    <p:sink/>
    
    <p:exec source-is-xml="false" result-is-xml="false" errors-is-xml="false" command="pwd" name="pwd">
        <p:input port="source">
            <p:empty/>
        </p:input>
    </p:exec>
    <p:sink/>
    
</p:declare-step>
