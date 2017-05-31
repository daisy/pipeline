<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:ex="http://example.net/ns" type="ex:identity" version="1.0">
    
    <p:input port="source.document.primary" primary="true">
        <p:inline>
            <source.document.primary.default/>
        </p:inline>
    </p:input>
    <p:input port="source.document.secondary.sequence" sequence="true">
        <p:inline>
            <source.document.secondary.sequence/>
        </p:inline>
    </p:input>
    <p:input port="source.document.secondary.select-children" select="/*/*" sequence="true">
        <p:inline>
            <default>
                <default/>
                <default/>
            </default>
        </p:inline>
    </p:input>
    <p:input port="source.parameter.primary.sequence" primary="true" kind="parameter" sequence="true"/>
    <p:input port="source.parameter.secondary" kind="parameter"/>
    
    <p:output port="result.document.primary" primary="true">
        <p:pipe port="source.document.primary" step="main"/>
    </p:output>
    <p:output port="result.document.secondary.sequence" sequence="true">
        <p:pipe port="source.document.secondary.sequence" step="main"/>
    </p:output>
    <p:output port="result.document.secondary.select-children" sequence="true">
        <p:pipe port="source.document.secondary.select-children" step="main"/>
    </p:output>
    <p:output port="result.parameter.primary">
        <p:pipe port="result" step="parameters"/>
    </p:output>
    <p:output port="result.parameter.secondary">
        <p:pipe port="source.parameter.secondary" step="main"/>
    </p:output>
    <p:output port="options">
        <p:pipe port="result" step="options"/>
    </p:output>
    
    <p:option name="option"/>
    <p:option name="option.required" required="true"/>
    <!--<p:option name="option.required" select="'required'"/>-->
    <p:option name="option.default" select="'default'"/>
    
    <p:identity>
        <p:input port="source">
            <p:inline>
                <c:options>
                    <c:option name="option"/>
                    <c:option name="option.required"/>
                    <c:option name="option.default"/>
                </c:options>
            </p:inline>
        </p:input>
    </p:identity>
    <p:choose>
        <p:when test="p:value-available('option',false())">
            <p:add-attribute match="/*/*[@name='option']" attribute-name="value">
                <p:with-option name="attribute-value" select="$option"/>
            </p:add-attribute>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:add-attribute match="/*/*[@name='option.required']" attribute-name="value">
        <p:with-option name="attribute-value" select="$option.required"/>
    </p:add-attribute>
    <p:add-attribute match="/*/*[@name='option.default']" attribute-name="value">
        <p:with-option name="attribute-value" select="$option.default"/>
    </p:add-attribute>
    <p:delete match="/*/*[not(.[@value])]"/>
    <p:identity name="options"/>
    
    <p:parameters name="parameters">
        <p:input port="parameters">
            <p:pipe port="source.parameter.primary.sequence" step="main"/>
        </p:input>
    </p:parameters>
    
</p:declare-step>
