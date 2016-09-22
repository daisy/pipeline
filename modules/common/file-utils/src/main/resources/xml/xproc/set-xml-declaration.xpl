<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" type="px:set-xml-declaration" version="1.0"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Sets, or deletes, the xml declaration of a file. If `xml-declaration` is the empty string, the xml declaration will be deleted. It is an error if `xml-declaration` does not match the regex
            `^&lt;\?xml[ \t\r\n]+version[ \t\r\n]*=[ \t\r\n]*(''1\.[0-9]+''|&quot;1.[0-9]+&quot;)([ \t\r\n]+encoding[ \t\r\n]*=[ \t\r\n]*(''[A-Za-z][A-Za-z0-9._-]*''|&quot;[A-Za-z][A-Za-z0-9._-]*&quot;))?([ \t\r\n]+standalone[ \t\r\n]*=[ \t\r\n]*(''(yes|no)''|&quot;(yes|no)&quot;))?[ \t\r\n]*\?&gt;$`.
            The result port will contain a `c:result` document with the URI to the file as its text node.</p>
    </p:documentation>
    
    <p:output port="result">
        <p:documentation>A document containing the URI to the file, same as the output of a `p:store` operation.</p:documentation>
    </p:output>
    <p:option name="href" required="true">
        <p:documentation>URI to the file you want to set the xml declaration of.</p:documentation>
    </p:option>
    <p:option name="xml-declaration" required="true">
        <p:documentation>The xml declaration.</p:documentation>
    </p:option>
    <p:option name="encoding" select="'utf-8'">
        <p:documentation>The encoding to use when reading and writing from and to the file (default: 'utf-8').</p:documentation>
    </p:option>
    <p:option name="use-java-implementation" select="'true'">
        <p:documentation>If the Java implementation of this step is available but you don't want to use it; set this to false (default 'true').</p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <p:declare-step type="pxi:set-xml-declaration">
        <p:option name="href" required="true"/>
        <p:option name="xml-declaration" required="true"/>
        <p:option name="encoding"/>
        <p:output port="result"/>
    </p:declare-step>
    
    <p:choose>
        <p:when test="p:step-available('pxi:set-xml-declaration') and $use-java-implementation = 'true'">
            <pxi:set-xml-declaration>
                <p:with-option name="href" select="$href"/>
                <p:with-option name="xml-declaration" select="$xml-declaration"/>
                <p:with-option name="encoding" select="$encoding"/>
            </pxi:set-xml-declaration>
            
        </p:when>
        <p:otherwise>
            
            <p:variable name="xml-declaration-regex" select="'^&lt;\?xml[ \t\r\n]+version[ \t\r\n]*=[ \t\r\n]*(''1\.[0-9]+''|&quot;1.[0-9]+&quot;)([ \t\r\n]+encoding[ \t\r\n]*=[ \t\r\n]*(''[A-Za-z][A-Za-z0-9._-]*''|&quot;[A-Za-z][A-Za-z0-9._-]*&quot;))?([ \t\r\n]+standalone[ \t\r\n]*=[ \t\r\n]*(''(yes|no)''|&quot;(yes|no)&quot;))?[ \t\r\n]*\?&gt;$'"/>
            
            <p:add-attribute match="/*" attribute-name="href">
                <p:with-option name="attribute-value" select="$href"/>
                <p:input port="source">
                    <p:inline exclude-inline-prefixes="#all">
                        <c:request method="GET"/>
                    </p:inline>
                </p:input>
            </p:add-attribute>
            <px:message severity="WARN" message="pxi:set-xml-declaration is not available; will read and parse entire file using XProc which might cause performance issues for large files: $1">
                <p:with-option name="param1" select="/*/@href"/>
            </px:message>
            
            <p:add-attribute match="/*" attribute-name="override-content-type">
                <p:with-option name="attribute-value" select="concat('text/plain; charset=',$encoding)"/>
            </p:add-attribute>
            
            <px:assert message="The href must be an absolute file URI: $1" error-code="DPSD01">
                <p:with-option name="param1" select="$href"/>
                <p:with-option name="test" select="matches($href,'^file:/.*[^/]$')"/>
            </px:assert>
            
            <px:assert message="The xml-declaration must either be empty, or be a valid xml declaration: $1" error-code="DPSD02">
                <p:with-option name="param1" select="$xml-declaration"/>
                <p:with-option name="test" select="$xml-declaration='' or matches($xml-declaration,$xml-declaration-regex)"/>
            </px:assert>
            
            <p:http-request/>
            <p:xslt>
                <p:with-param name="xml-declaration" select="$xml-declaration"/>
                <p:input port="stylesheet">
                    <p:document href="../xslt/set-xml-declaration.xsl"/>
                </p:input>
            </p:xslt>
            <p:store method="text" name="store">
                <p:with-option name="href" select="$href"/>
                <p:with-option name="encoding" select="$encoding"/>
            </p:store>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="result" step="store"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
