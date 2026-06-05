<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:read-xml-declaration" name="main" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:d="http://www.daisy.org/ns/pipeline/data" exclude-inline-prefixes="#all" version="1.0"
    xmlns:cx="http://xmlcalabash.com/ns/extensions">

    <!-- TODO: move this to pipeline 2 common-utils before the v1.8 release -->

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Example usage:</p>
        <pre xml:space="preserve">
            &lt;!-- provide a single document on the primary input port --&gt;
            &lt;px:read-xml-declaration/&gt;
        </pre>
        <p>Example output:</p>
        <pre xml:space="preserve">
            &lt;c:result xmlns:c="http://www.w3.org/ns/xproc-step" standalone="yes" encoding="UTF-8" has-xml-declaration="true" version="1.0"&gt;&amp;lt;?xml version="1.0" encoding="UTF-8" standalone="yes" ?&amp;gt;&lt;/c:result&gt;
        </pre>
    </p:documentation>

    <p:option name="href" required="true"/>

    <p:output port="result"/>

    <p:identity>
        <p:input port="source">
            <p:inline exclude-inline-prefixes="#all">
                <c:request method="GET" override-content-type="text/plain; charset=UTF-8"/>
            </p:inline>
        </p:input>
    </p:identity>
    <p:add-attribute match="/*" attribute-name="href">
        <p:with-option name="attribute-value" select="$href"/>
    </p:add-attribute>
    <p:http-request/>

    <p:group>
        <p:variable name="content" select="/*/text()"/>
        <p:choose>
            <p:when test="matches($content,'^&lt;\?xml ')">
                <p:variable name="xml-declaration" select="replace($content,'^(&lt;\?xml\s.*?\?&gt;).*$','$1','s')"/>
                <p:add-attribute match="/*" attribute-name="xml-declaration">
                    <p:with-option name="attribute-value" select="$xml-declaration"/>
                    <p:input port="source">
                        <p:inline exclude-inline-prefixes="#all">
                            <c:result has-xml-declaration="true"/>
                        </p:inline>
                    </p:input>
                </p:add-attribute>
                <p:choose>
                    <p:when test="matches($xml-declaration,'^.*version\s*=\s*[&quot;&amp;apos;][\d\.]+[&quot;&amp;apos;].*$')">
                        <p:add-attribute match="/*" attribute-name="version">
                            <p:with-option name="attribute-value" select="replace($xml-declaration,'^.*version\s*=\s*[&quot;&amp;apos;]([\d\.]+)[&quot;&amp;apos;].*$','$1')"/>
                        </p:add-attribute>
                    </p:when>
                    <p:otherwise>
                        <p:identity/>
                    </p:otherwise>
                </p:choose>
                <p:choose>
                    <p:when test="matches($xml-declaration,'^.*encoding\s*=\s*[&quot;&amp;apos;][A-Za-z0-9\._\-]+[&quot;&amp;apos;].*$')">
                        <p:add-attribute match="/*" attribute-name="encoding">
                            <p:with-option name="attribute-value" select="replace($xml-declaration,'^.*encoding\s*=\s*[&quot;&amp;apos;]([A-Za-z0-9\._\-]+)[&quot;&amp;apos;].*$','$1')"/>
                        </p:add-attribute>
                    </p:when>
                    <p:otherwise>
                        <p:identity/>
                    </p:otherwise>
                </p:choose>
                <p:choose>
                    <p:when test="matches($xml-declaration,'^.*standalone\s*=\s*[&quot;&amp;apos;](yes|no)[&quot;&amp;apos;].*$')">
                        <p:add-attribute match="/*" attribute-name="standalone">
                            <p:with-option name="attribute-value" select="replace($xml-declaration,'^.*standalone\s*=\s*[&quot;&amp;apos;](yes|no)[&quot;&amp;apos;].*$','$1')"/>
                        </p:add-attribute>
                    </p:when>
                    <p:otherwise>
                        <p:identity/>
                    </p:otherwise>
                </p:choose>
            </p:when>
            <p:otherwise>
                <p:identity>
                    <p:input port="source">
                        <p:inline exclude-inline-prefixes="#all">
                            <c:result has-xml-declaration="false"/>
                        </p:inline>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
    </p:group>

</p:declare-step>
