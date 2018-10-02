<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:html-load" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/"
    xmlns:cx="http://xmlcalabash.com/ns/extensions" exclude-inline-prefixes="#all" version="1.0">
    <p:documentation>Tries first to p:load the HTML-file. An exception will be thrown if the file is
        not well formed XML, in which case the file will be loaded using p:http-request and
        p:unescape-markup. As the xproc-step ('c:') namespace is still attached to the elements
        after unwrapping, an XSLT is applied that removes all other namespaces than the XHTML
        namespace.</p:documentation>
    <p:output port="result"/>
    <p:option name="href" required="true"/>

    <p:declare-step type="pxi:http-load">
        <p:output port="result"/>
        <p:option name="href"/>
        <p:option name="encoding" select="'utf-8'"/>
        <p:identity>
            <p:input port="source">
                <p:inline exclude-inline-prefixes="#all">
                    <c:request method="GET"/>
                </p:inline>
            </p:input>
        </p:identity>
        <p:add-attribute match="c:request" attribute-name="href">
            <p:with-option name="attribute-value" select="$href"/>
        </p:add-attribute>
        <p:add-attribute match="c:request" attribute-name="override-content-type">
            <p:with-option name="attribute-value" select="concat('text/plain; charset=',$encoding)"
            />
        </p:add-attribute>
        <p:http-request/>
        <!--  remove doctypes etc (<!DOCTYPE html> doesn't work with p:unescape-markup)  -->
        <p:string-replace match="/*/text()[1]"
            replace="replace(/*/text()[1],'^&lt;[!\?].*?(&lt;[^!\?])','$1','s')"/>
        <p:unescape-markup content-type="text/html" namespace="http://www.w3.org/1999/xhtml"/>
        <p:unwrap match="c:body"/>
    </p:declare-step>

    <p:try>
        <p:group>
            <p:load>
                <p:with-option name="href" select="$href"/>
            </p:load>
        </p:group>
        <p:catch>
            <p:try>
                <p:group>
                    <pxi:http-load>
                        <p:with-option name="href" select="$href"/>
                    </pxi:http-load>
                    <p:choose>
                        <p:variable name="encoding"
                            select="//h:meta[@http-equiv='Content-Type']/replace(@content,'.*?charset\s*=\s*(''(.*?)''|&quot;(.*?)&quot;|([^''&quot;\s][^\s;]*)|.).*','$2$3$4')"
                            xmlns:h="http://www.w3.org/1999/xhtml"/>
                        <p:when test="lower-case($encoding) ne 'utf-8'">
                            <pxi:http-load>
                                <p:with-option name="href" select="$href"/>
                                <p:with-option name="encoding" select="$encoding"/>
                            </pxi:http-load>
                        </p:when>
                        <p:otherwise>
                            <p:identity/>
                        </p:otherwise>
                    </p:choose>
                </p:group>
                <p:catch>
                    <p:in-scope-names name="vars"/>
                    <p:template name="error-message">
                        <p:input port="template">
                            <p:inline>
                                <c:message><![CDATA[]]>Could not load
                                    {$href}<![CDATA[]]></c:message>
                            </p:inline>
                        </p:input>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                        <p:input port="parameters">
                            <p:pipe step="vars" port="result"/>
                        </p:input>
                    </p:template>
                    <p:error code="XC0011">
                        <p:input port="source">
                            <p:pipe port="result" step="error-message"/>
                        </p:input>
                    </p:error>
                </p:catch>
            </p:try>
        </p:catch>
    </p:try>
    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="namespace-fixup.xsl"/>
        </p:input>
    </p:xslt>
</p:declare-step>
