<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:x="http://www.daisy.org/ns/xprocspec"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-inline-prefixes="#all"
                type="x:assert-xml-declaration"
                name="main">
    
    <p:documentation><![CDATA[
        expect example:
            <c:result version="1.0" encoding="UTF-8" standalone="true"/>
        
        expect attributes:
            - the version attribute is required
            - empty or missing attributes means that those attributes must not be present in the xml declaration
    ]]></p:documentation>

    <p:input port="context" sequence="true"/>
    <p:input port="expect" sequence="true"/>
    <p:input port="parameters" kind="parameter" primary="true"/>
    <p:output port="result" primary="true"/>

    <p:import href="xml-peek.xpl">
        <p:documentation>
            px:file-xml-peek
        </p:documentation>
    </p:import>

    <p:split-sequence initial-only="true" test="position()=1">
        <p:input port="source">
            <p:pipe port="expect" step="main"/>
        </p:input>
    </p:split-sequence>
    <p:add-attribute match="/*" attribute-name="version">
        <p:with-option name="attribute-value" select="/*/@version"/>
    </p:add-attribute>
    <p:add-attribute match="/*" attribute-name="encoding">
        <p:with-option name="attribute-value" select="/*/@encoding"/>
    </p:add-attribute>
    <p:add-attribute match="/*" attribute-name="standalone">
        <p:with-option name="attribute-value" select="/*/@standalone"/>
    </p:add-attribute>
    <p:identity name="expected-xml-declaration"/>
    <p:template name="expected">
        <p:input port="template">
            <p:inline>
                <x:expected>&lt;?xml version=&quot;{string(/*/@version)}&quot;{if (not(/*/@encoding='')) then concat(' encoding=&quot;',/*/@encoding,'&quot;') else ''}{if (not(/*/@standalone='')) then concat(' standalone=&quot;',/*/@standalone,'&quot;') else ''}?&gt;</x:expected>
            </p:inline>
        </p:input>
    </p:template>

    <p:group>
        <p:for-each name="context-iterator">
            <p:iteration-source>
                <p:pipe port="context" step="main"/>
            </p:iteration-source>
            <p:variable name="base-uri" select="base-uri(/*)"/>
            <px:file-xml-peek name="xml-peek">
                <p:with-option name="href" select="base-uri(/*)"/>
            </px:file-xml-peek>
            <p:sink/>
            <p:identity name="xml-prolog">
                <p:input port="source">
                    <p:pipe step="xml-peek" port="prolog"/>
                </p:input>
            </p:identity>
            <p:group>
                <p:variable name="version" select="/*/c:xml/@version"/>
                <p:variable name="encoding" select="/*/c:xml/@encoding"/>
                <p:variable name="standalone" select="/*/c:xml/@standalone"/>
                <p:variable name="xml-declaration" select="/*/c:xml/text()"/>
                
                <p:in-scope-names name="xml-declaration-vars"/>
                <p:template>
                    <p:input port="template">
                        <p:inline>
                            <x:was>{$xml-declaration} ({$base-uri})</x:was>
                        </p:inline>
                    </p:input>
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                    <p:input port="parameters">
                        <p:pipe step="xml-declaration-vars" port="result"/>
                    </p:input>
                </p:template>
                <p:add-attribute match="/*" attribute-name="result">
                    <p:with-option name="attribute-value" select="if ($version=/*/@version and $encoding=/*/@encoding and $standalone=/*/@standalone) then 'passed' else 'failed'">
                        <p:pipe port="result" step="expected-xml-declaration"/>
                    </p:with-option>
                </p:add-attribute>
            </p:group>
        </p:for-each>
        <p:wrap-sequence wrapper="x:test-result" name="wrapped-test-results"/>
        <p:in-scope-names name="test-results-vars"/>
        <p:template>
            <p:input port="template">
                <p:inline>
                    <x:test-result>
                        <x:was>{string-join(/*/x:was/text(),'&#xa;')}</x:was>
                    </x:test-result>
                </p:inline>
            </p:input>
            <p:input port="source">
                <p:pipe port="result" step="wrapped-test-results"/>
            </p:input>
            <p:input port="parameters">
                <p:pipe step="test-results-vars" port="result"/>
            </p:input>
        </p:template>
        <p:add-attribute match="/*" attribute-name="result">
            <p:with-option name="attribute-value" select="if (/*/x:was/@result='failed') then 'failed' else 'passed'">
                <p:pipe port="result" step="wrapped-test-results"/>
            </p:with-option>
        </p:add-attribute>
        <p:insert match="/*" position="first-child">
            <p:input port="insertion">
                <p:pipe port="result" step="expected"/>
            </p:input>
        </p:insert>

    </p:group>

</p:declare-step>
