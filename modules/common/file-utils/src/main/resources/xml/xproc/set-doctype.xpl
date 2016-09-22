<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" type="px:set-doctype" version="1.0"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Sets, or deletes, the doctype of a file. If `doctype` is the empty string, the doctype will be deleted. It is an error if `doctype` does not match the regex
            `^&lt;!DOCTYPE\s+\w+\s*(|SYSTEM\s+("[^"]*"|'[^']*')|PUBLIC\s+("[\s\w\-'()+,\./:=?;!*#@$_%]*"|'[\s\w\-()+,\./:=?;!*#@$_%]*')\s+("[^"]*"|'[^']*'))(\s*\[[^\]]+\])?>$`. The result port will contain a
            `c:result` document with the URI to the file as its text node.</p>
    </p:documentation>

    <p:output port="result">
        <p:documentation>A document containing the URI to the file, same as the output of a `p:store` operation.</p:documentation>
    </p:output>
    <p:option name="href" required="true">
        <p:documentation>URI to the file you want to set the doctype of.</p:documentation>
    </p:option>
    <p:option name="doctype" required="true">
        <p:documentation>The doctype.</p:documentation>
    </p:option>
    <p:option name="encoding" select="'utf-8'">
        <p:documentation>The encoding to use when reading and writing from and to the file (default: 'utf-8').</p:documentation>
    </p:option>
    <p:option name="use-java-implementation" select="'true'">
        <p:documentation>If the Java implementation of this step is available but you don't want to use it; set this to false (default 'true').</p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:declare-step type="pxi:set-doctype">
        <p:option name="href" required="true"/>
        <p:option name="doctype" required="true"/>
        <p:option name="encoding"/>
        <p:output port="result"/>
    </p:declare-step>
    
    <p:choose>
        <p:when test="p:step-available('pxi:set-doctype') and $use-java-implementation = 'true'">
            <pxi:set-doctype>
                <p:with-option name="href" select="$href"/>
                <p:with-option name="doctype" select="$doctype"/>
                <p:with-option name="encoding" select="$encoding"/>
            </pxi:set-doctype>
            
        </p:when>
        <p:otherwise>
            <p:variable name="doctype-regex" select="'^&lt;!DOCTYPE\s+\w+\s*(|SYSTEM\s+(&quot;[^&quot;]*&quot;|''[^'']*'')|PUBLIC\s+(&quot;[\s\w\-''()+,\./:=?;!*#@$_%]*&quot;|''[\s\w\-()+,\./:=?;!*#@$_%]*'')\s+(&quot;[^&quot;]*&quot;|''[^'']*''))(\s*\[[^\]]+\])?>$'"/>
        
            <p:add-attribute match="/*" attribute-name="href">
                <p:with-option name="attribute-value" select="$href"/>
                <p:input port="source">
                    <p:inline exclude-inline-prefixes="#all">
                        <c:request method="GET"/>
                    </p:inline>
                </p:input>
            </p:add-attribute>
            <px:message severity="WARN" message="pxi:set-doctype is not available; will read and parse entire file using XProc which might cause performance issues for large files: $1">
                <p:with-option name="param1" select="/*/@href"/>
            </px:message>
        
            <p:add-attribute match="/*" attribute-name="override-content-type">
                <p:with-option name="attribute-value" select="concat('text/plain; charset=',$encoding)"/>
            </p:add-attribute>
        
            <px:assert message="The href must be an absolute file URI: $1" error-code="DPSD01">
                <p:with-option name="param1" select="$href"/>
                <p:with-option name="test" select="matches($href,'^file:/.*[^/]$')"/>
            </px:assert>
        
            <px:assert message="The doctype must either be empty, or be a valid DOCTYPE declaration: $1" error-code="DPSD02">
                <p:with-option name="param1" select="$doctype"/>
                <p:with-option name="test" select="$doctype='' or matches($doctype,$doctype-regex)"/>
            </px:assert>
        
            <p:http-request/>
            <p:xslt>
                <p:with-param name="doctype" select="$doctype"/>
                <p:input port="stylesheet">
                    <p:document href="../xslt/set-doctype.xsl"/>
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
