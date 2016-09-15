<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:catalog="urn:oasis:names:tc:entity:xmlns:xml:catalog" exclude-inline-prefixes="#all">

    <p:output port="result" sequence="true">
        <p:pipe port="result" step="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>

    <p:import href="test-mediatype-detect.xpl"/>

    <pxi:test-mediatype-detect name="test-mediatype-detect"/>

    <p:for-each name="test">
        <p:output port="result">
            <p:pipe port="result" step="test.result"/>
        </p:output>
        <p:iteration-source>
            <p:pipe step="test-mediatype-detect" port="result"/>
        </p:iteration-source>
        <p:variable name="script-uri" select="/*/@script-uri"/>
        <p:variable name="test-group-name" select="/*/@name"/>
        <p:delete match="/*/c:result[@result='true']"/>
        <p:identity>
            <p:log port="result"/>
        </p:identity>
        <p:viewport match="/*/c:result" name="test.viewport">
            <p:in-scope-names name="test.viewport.vars"/>
            <p:template>
                <p:input port="source">
                    <p:pipe step="test.viewport" port="current"/>
                </p:input>
                <p:input port="template">
                    <p:inline>
                        <d:error>
                            <d:desc>{string(//@name)} {if (@result='false') then 'failed' else @result}</d:desc>
                            <!--<d:file>{$script-uri}</d:file>-->
                            <d:location href="{$script-uri}#{/*/@name}"/>
                            <d:expected/>
                            <d:was/>
                        </d:error>
                    </p:inline>
                </p:input>
                <p:input port="parameters">
                    <p:pipe step="test.viewport.vars" port="result"/>
                </p:input>
            </p:template>
            <p:insert match="d:expected" position="last-child">
                <p:input port="insertion" select=".//c:expected/*">
                    <p:pipe step="test.viewport" port="current"/>
                </p:input>
            </p:insert>
            <p:insert match="d:was" position="last-child">
                <p:input port="insertion" select=".//c:was/*">
                    <p:pipe step="test.viewport" port="current"/>
                </p:input>
            </p:insert>
            <p:choose>
                <p:when test="count(//d:expected/node())=0 and count(//d:was/node())=0">
                    <p:delete match="d:expected | d:was"/>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
            <p:wrap match="/*" wrapper="d:errors"/>
            <p:wrap match="/*" wrapper="d:report"/>
            <p:add-attribute match="/*" attribute-name="type" attribute-value="filecheck"/>
        </p:viewport>
        <p:rename match="/*" new-name="d:reports"/>
        <p:wrap match="/*" wrapper="d:document-validation-report"/>
        <p:insert match="/*" position="first-child">
            <p:input port="insertion">
                <p:pipe step="test.document-info" port="result"/>
            </p:input>
        </p:insert>
        <p:identity name="test.result"/>

        <p:in-scope-names name="test.vars"/>
        <p:template name="test.document-info">
            <p:input port="source">
                <p:pipe port="current" step="test"/>
            </p:input>
            <p:input port="template">
                <p:inline>
                    <d:document-info>
                        <d:document-name>{string(/*/@name)}</d:document-name>
                        <d:document-type>XProc Unit Test</d:document-type>
                        <d:document-path>{$script-uri}</d:document-path>
                        <d:error-count>{string(count(/*/*[@result='false']))}</d:error-count>
                    </d:document-info>
                </p:inline>
            </p:input>
            <p:input port="parameters">
                <p:pipe step="test.vars" port="result"/>
            </p:input>
        </p:template>
    </p:for-each>
    
    <px:validation-report-to-html name="result"/>
    <p:store href="file:/tmp/report.html"/>

</p:declare-step>
