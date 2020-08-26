<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-inline-prefixes="#all"
                type="pxi:load-html">

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
        <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
            <p:documentation>
                px:data
            </p:documentation>
        </p:import>
        <px:data>
            <p:with-option name="href" select="$href"/>
            <p:with-option name="content-type" select="concat('text/plain; charset=',$encoding)"/>
        </px:data>
        <!--  remove doctypes etc (<!DOCTYPE html> doesn't work with p:unescape-markup)  -->
        <p:string-replace match="/*/text()[1]"
            replace="replace(/*/text()[1],'^&lt;[!\?].*?(&lt;[^!\?])','$1','s')"/>
        <p:unescape-markup content-type="text/html" namespace="http://www.w3.org/1999/xhtml"/>
        <p:unwrap match="c:data"/>
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
            <p:document href="../xslt/namespace-fixup.xsl"/>
        </p:input>
    </p:xslt>
</p:declare-step>
