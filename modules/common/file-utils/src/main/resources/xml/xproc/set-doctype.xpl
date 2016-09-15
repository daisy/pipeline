<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" type="px:set-doctype" version="1.0">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Sets, or deletes, the doctype of a file. If `doctype` is the empty string, the doctype will be deleted. It is an error if `doctype` does not match the regex
            `^&lt;!DOCTYPE\s+\w+\s*(|SYSTEM\s+("[^"]*"|'[^']*')|PUBLIC\s+("[\s\w\-'()+,\./:=?;!*#@$_%]*"|'[\s\w\-()+,\./:=?;!*#@$_%]*')\s+("[^"]*"|'[^']*'))(\s*\[[^\]]+\])?>$`. The result port will contain a
            `c:result` document with the URI to the file as its text node.</p>
    </p:documentation>

    <p:output port="result">
        <p:pipe port="result" step="store"/>
    </p:output>
    <p:option name="href" required="true"/>
    <p:option name="doctype" required="true"/>
    <p:option name="encoding" select="'utf-8'"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:variable name="doctype-regex" select="'^&lt;!DOCTYPE\s+\w+\s*(|SYSTEM\s+(&quot;[^&quot;]*&quot;|''[^'']*'')|PUBLIC\s+(&quot;[\s\w\-''()+,\./:=?;!*#@$_%]*&quot;|''[\s\w\-()+,\./:=?;!*#@$_%]*'')\s+(&quot;[^&quot;]*&quot;|''[^'']*''))(\s*\[[^\]]+\])?>$'"/>

    <p:add-attribute match="/*" attribute-name="href">
        <p:with-option name="attribute-value" select="$href"/>
        <p:input port="source">
            <p:inline exclude-inline-prefixes="#all">
                <c:request method="GET"/>
            </p:inline>
        </p:input>
    </p:add-attribute>

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
    <p:string-replace match="/*/text()">
      <p:with-option name="replace" select="concat('replace(.,''^(&lt;\?[^&gt;]*&gt;\s*)*\s*?(&lt;!DOCTYPE\s[^&gt;]*&gt;\n?)?(.)'',''',concat('$1', replace($doctype, '''', ''''''),'&#10;$3'),''')')"/>
    </p:string-replace>
    <p:store method="text" name="store">
        <p:with-option name="href" select="$href"/>
        <p:with-option name="encoding" select="$encoding"/>
    </p:store>

</p:declare-step>
