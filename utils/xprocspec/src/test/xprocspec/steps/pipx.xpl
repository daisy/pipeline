<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:pkg="http://expath.org/ns/pkg"
    xmlns:pipx="http://pipx.org/ns/pipx" xmlns:impl="http://pipx.org/ns/pipx/impl" exclude-inline-prefixes="xs pkg pipx impl" version="1.0" pkg:import-uri="http://pipx.org/ns/pipx.xpl">

    <!--
        Used for regression tests for issue #9
        Step copied from https://github.com/josteinaj/pipx/blob/6bd484e88f017e1435e50ff64a83194db7736aa9/src/pipx.xpl
    -->

    <p:declare-step type="pipx:error" name="this">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Throws an error, with a proper title, message, and other infos.</p>
            <p>This is the central piece in the error handling machanism, still to define precisely.</p>
            <p><b>TODO</b>: Error handling machanism, still to define precisely!</p>
            <p><b>TODO</b>: Enable the ability to give an input to p:error, through an input to this step. Maybe we should have either the option <code>message</code> or the port <code>message</code>
                (a template in both cases).</p>
        </p:documentation>
        <p:option name="code" required="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The error code, following the same requirements as the option <code>code</code> of <code>p:error</code>.</p>
            </p:documentation>
        </p:option>
        <p:option name="code-prefix" required="false">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The error code prefix, following the same requirements as the option <code>code-prefix</code> of <code>p:error</code>.</p>
            </p:documentation>
        </p:option>
        <p:option name="code-namespace" required="false">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The error code namespace, following the same requirements as the option <code>code-namespace</code> of <code>p:error</code>.</p>
            </p:documentation>
        </p:option>
        <p:option name="title" required="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The error title, can contain '{' and '}' as in p:template.</p>
            </p:documentation>
        </p:option>
        <p:option name="message" required="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The error message, can contain '{' and '}' as in p:template.</p>
            </p:documentation>
        </p:option>
        <p:input port="source" primary="true" sequence="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>Additional input to add in the error document.</p>
            </p:documentation>
        </p:input>
        <p:input port="parameters" kind="parameter" primary="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The parameters used in '{...}' replacements in $title and $message.</p>
            </p:documentation>
        </p:input>
        <p:output port="result" primary="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The <code>output</code> of the resulting p:error.</p>
            </p:documentation>
        </p:output>
        <!-- create the error title template -->
        <p:template name="title-tpl">
            <p:input port="source">
                <p:empty/>
            </p:input>
            <p:input port="template">
                <p:inline>
                    <pipx:title>{ $title }</pipx:title>
                </p:inline>
            </p:input>
            <p:with-param name="title" select="$title"/>
        </p:template>
        <!-- format the error title -->
        <p:template name="title">
            <p:input port="parameters">
                <p:pipe step="this" port="parameters"/>
            </p:input>
            <p:input port="source">
                <p:empty/>
            </p:input>
            <p:input port="template">
                <p:pipe step="title-tpl" port="result"/>
            </p:input>
        </p:template>
        <!-- create the error message template -->
        <p:template name="message-tpl">
            <p:input port="source">
                <p:empty/>
            </p:input>
            <p:input port="template">
                <p:inline>
                    <pipx:message>{ $message }</pipx:message>
                </p:inline>
            </p:input>
            <p:with-param name="message" select="$message"/>
        </p:template>
        <!-- format the error message -->
        <p:template name="message">
            <p:input port="parameters">
                <p:pipe step="this" port="parameters"/>
            </p:input>
            <p:input port="source">
                <p:empty/>
            </p:input>
            <p:input port="template">
                <p:pipe step="message-tpl" port="result"/>
            </p:input>
        </p:template>
        <!-- wrap the title, the message, and the additional documents into app:error -->
        <p:wrap-sequence wrapper="pipx:error">
            <p:input port="source">
                <p:pipe step="title" port="result"/>
                <p:pipe step="message" port="result"/>
                <p:pipe step="this" port="source"/>
            </p:input>
        </p:wrap-sequence>
        <!-- add @code to app:error -->
        <p:add-attribute match="/*" attribute-name="code" name="desc">
            <p:with-option name="attribute-value" select="$code"/>
        </p:add-attribute>
        <!-- actually throw the error -->
        <!-- TODO: Is it OK to call p:error with optional options, or choose I use
         a p:choose with combinatory explosion? -->
        <p:choose>
            <p:when test="p:value-available('code-prefix') and p:value-available('code-namespace')">
                <p:error>
                    <p:with-option name="code" select="$code"/>
                    <p:with-option name="code-prefix" select="$code-prefix"/>
                    <p:with-option name="code-namespace" select="$code-namespace"/>
                    <p:input port="source">
                        <p:pipe step="desc" port="result"/>
                    </p:input>
                </p:error>
            </p:when>
            <p:when test="p:value-available('code-prefix')">
                <p:error>
                    <p:with-option name="code" select="$code"/>
                    <p:with-option name="code-prefix" select="$code-prefix"/>
                    <p:input port="source">
                        <p:pipe step="desc" port="result"/>
                    </p:input>
                </p:error>
            </p:when>
            <p:when test="p:value-available('code-namespace')">
                <p:error>
                    <p:with-option name="code" select="$code"/>
                    <p:with-option name="code-namespace" select="$code-namespace"/>
                    <p:input port="source">
                        <p:pipe step="desc" port="result"/>
                    </p:input>
                </p:error>
            </p:when>
            <p:otherwise>
                <p:error>
                    <p:with-option name="code" select="$code"/>
                    <p:input port="source">
                        <p:pipe step="desc" port="result"/>
                    </p:input>
                </p:error>
            </p:otherwise>
        </p:choose>
    </p:declare-step>

    <p:declare-step type="pipx:parameter" name="this">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Extract a parameter from the parameter port.</p>
            <p>The parameters are passed on the primary port <code>parameters</code>, of kind <code>parameters</code>. The name of the parameter to extract is in the option <code>name</code>. If the
                option <code>required</code> is <code>true</code> (default is <code>false</code>), the step throws the error <code>pipx:no-parameter</code> if there is no such parameter or if its
                value is an empty string.</p>
            <p>The returned document, on the port <code>result</code>, contains a root element <code>param</code>, with an attribute <code>name</code>. The attribute value is the name of the parameyer
                (the value of the option <code>name</code>). The value of the element, is the value of the parameter. If the parameter does not exist (and the value of the option <code>required</code>
                is <code>false</code>), the element value can be empty.</p>
            <p>If the value of the parameter <code>required</code> is not a valid lexical <code>xs:boolean</code>, the step throws the error <code>pipx:invalid-option</code>. The valid lexical values
                are <code>false</code>, <code>true</code>, <code>0</code> and <code>1</code>, with optional heading and trailing whitespaces.</p>
            <p><b>TODO</b>: How to handle namespaces, for the parameter names?</p>
            <p><b>TODO</b>: Is it possible there are several parameters with the same name?</p>
        </p:documentation>
        <p:input port="parameters" primary="true" kind="parameter"/>
        <p:output port="result" primary="true"/>
        <p:option name="param-name" required="true"/>
        <p:option name="required" required="false" select="'0'"/>
        <p:parameters name="parameters">
            <p:input port="parameters">
                <p:pipe port="parameters" step="this"/>
            </p:input>
        </p:parameters>
        <p:template name="title-tpl">
            <p:input port="source">
                <p:pipe step="parameters" port="result"/>
            </p:input>
            <p:input port="template">
                <p:inline>
                    <param xmlns="">{ string((/c:param|/c:param-set/c:param)[@name eq $name]/@value) }</param>
                </p:inline>
            </p:input>
            <p:with-param name="name" select="$param-name"/>
        </p:template>
        <p:choose>
            <p:when test="not($required castable as xs:boolean)">
                <impl:error code="invalid-option" title="Invalid option to pipx:parameter." message="The option $required as an invalid value: '{ $value }'.">
                    <p:with-param name="value" select="$required"/>
                </impl:error>
            </p:when>
            <p:when test="not(xs:boolean($required)) or boolean(string(/*)[.])">
                <p:add-attribute match="/param" attribute-name="name">
                    <p:with-option name="attribute-value" select="$param-name"/>
                </p:add-attribute>
            </p:when>
            <p:otherwise>
                <impl:error code="no-parameter" title="Required parameter does not exist." message="The parameter '{ $name }' is required and has no value.">
                    <p:with-param name="name" select="$param-name"/>
                </impl:error>
            </p:otherwise>
        </p:choose>
    </p:declare-step>

    <!--
      Private implementation steps.
   -->

    <p:declare-step type="impl:error" name="error">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Throws an error, with a proper title, message, and other infos, in the PipX namespace.</p>
        </p:documentation>
        <p:option name="code" required="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The error code, an NCName (used to build a QName in the PipX namespace).</p>
            </p:documentation>
        </p:option>
        <p:option name="title" required="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The error title, can contain '{' and '}' as in p:template.</p>
            </p:documentation>
        </p:option>
        <p:option name="message" required="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The error message, can contain '{' and '}' as in p:template.</p>
            </p:documentation>
        </p:option>
        <p:input port="source" primary="true" sequence="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>Additional input to add in the error document.</p>
            </p:documentation>
        </p:input>
        <p:input port="parameters" primary="true" kind="parameter">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The parameters used in '{...}' replacements in $title and $message.</p>
            </p:documentation>
        </p:input>
        <p:output port="result" primary="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The <code>output</code> of the resulting p:error.</p>
            </p:documentation>
        </p:output>
        <pipx:error code-prefix="pipx" code-namespace="http://pipx.org/ns/pipx">
            <p:with-option name="code" select="$code"/>
            <p:with-option name="title" select="$title"/>
            <p:with-option name="message" select="$message"/>
        </pipx:error>
    </p:declare-step>

</p:library>
