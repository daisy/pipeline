<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:assert" name="main"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Example usage:</p>
        <pre xml:space="preserve">
            &lt;px:assert message="All d:file elements must have a media-type attribute. $1 and $2 more d:file elements is missing their media-type attribute." severity="ERROR" error-code="pxe:PEOU0002"&gt;
               &lt;p:with-param name="param1" select="(/d:fileset/d:file[not(@media-type)])[1]/@href"/&gt;
               &lt;p:with-param name="param2" select="count(/d:fileset/d:file[not(@media-type)])-1"/&gt;
           &lt;/px:assert&gt;
        </pre>
    </p:documentation>
    
    <p:input port="source" primary="true" sequence="true">
        <p:empty/>
    </p:input>
    <p:output port="result" sequence="true">
        <p:pipe port="result" step="result"/>
    </p:output>
    
    <p:option name="test" select="''" cx:as="xs:string"/>                           <!-- boolean -->
    <p:option name="test-count-min" select="''"/>                                   <!-- positive integer -->
    <p:option name="test-count-max" select="''"/>                                   <!-- positive integer -->
    <p:option name="error-code" select="''"/>                                       <!-- QName - if not given, only a warning will be displayed. -->
    <p:option name="error-code-prefix" select="''"/>                                <!-- NCName -->
    <p:option name="error-code-namespace" select="''"/>                             <!-- anyURI -->
    <p:option name="message" required="true" cx:as="xs:string"/>                    <!-- description of what you are asserting. $1, $2 etc will be replaced with the contents of param1, param2 etc. -->
    <p:option name="param1" select="''" cx:as="xs:string"/>
    <p:option name="param2" select="''" cx:as="xs:string"/>
    <p:option name="param3" select="''" cx:as="xs:string"/>
    <p:option name="param4" select="''" cx:as="xs:string"/>
    <p:option name="param5" select="''" cx:as="xs:string"/>
    <p:option name="param6" select="''" cx:as="xs:string"/>
    <p:option name="param7" select="''" cx:as="xs:string"/>
    <p:option name="param8" select="''" cx:as="xs:string"/>
    <p:option name="param9" select="''" cx:as="xs:string"/>
    <!-- in the unlikely event that you need more parameters you'll have to format the message string yourself -->
    
    <p:import href="error.xpl"/>
    
    <p:add-attribute match="/*" attribute-name="message" name="message">
        <p:input port="source">
            <p:inline>
                <c:result/>
            </p:inline>
        </p:input>
        <p:with-option name="attribute-value"
                       select="replace(replace(replace(replace(replace(replace(replace(replace(replace(
                                 $message,
                                 '\$1',replace($param1,'\$','\\\$')),
                                 '\$2',replace($param2,'\$','\\\$')),
                                 '\$3',replace($param3,'\$','\\\$')),
                                 '\$4',replace($param4,'\$','\\\$')),
                                 '\$5',replace($param5,'\$','\\\$')),
                                 '\$6',replace($param6,'\$','\\\$')),
                                 '\$7',replace($param7,'\$','\\\$')),
                                 '\$8',replace($param8,'\$','\\\$')),
                                 '\$9',replace($param9,'\$','\\\$'))">
            <p:inline>
                <irrelevant/>
            </p:inline>
        </p:with-option>
    </p:add-attribute>
    
    <p:choose>
        <p:when test="$test">
            <p:choose>
                <p:when test="$test-count-min or $test-count-max">
                    <p:identity px:message-severity="WARN" px:message="the 'test' option and the 'test-count-*' options cannot be specified at the same time; only 'test' will be evaluated"/>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
            <p:choose>
                <p:when test="$test='true'">
                    <!-- assertion passed; do nothing -->
                    <p:add-attribute match="/*" attribute-name="result" attribute-value="true"/>
                </p:when>
                <p:otherwise>
                    <!-- assertion failed -->
                    <p:add-attribute match="/*" attribute-name="result" attribute-value="false"/>
                </p:otherwise>
            </p:choose>
        </p:when>
        
        <p:when test="$test-count-min or $test-count-max">
            <p:identity name="test-count.input"/>
            <p:count>
                <p:input port="source">
                    <p:pipe port="source" step="main"/>
                </p:input>
            </p:count>
            <p:choose>
                <p:when test="(not($test-count-min) or number($test-count-min) &lt;= number(/*)) and (not($test-count-max) or number($test-count-max) &gt;= number(/*))">
                    <p:add-attribute match="/*" attribute-name="result" attribute-value="true">
                        <p:input port="source">
                            <p:pipe port="result" step="test-count.input"/>
                        </p:input>
                    </p:add-attribute>
                </p:when>
                <p:otherwise>
                    <p:variable name="was" select="/*/text()"/>
                    <p:add-attribute match="/*" attribute-name="result" attribute-value="false">
                        <p:input port="source">
                            <p:pipe port="result" step="test-count.input"/>
                        </p:input>
                    </p:add-attribute>
                    <p:add-attribute match="/*" attribute-name="message">
                        <p:with-option name="attribute-value" select="concat(/*/@message,' (was: ',$was,')')"/>
                    </p:add-attribute>
                </p:otherwise>
            </p:choose>
        </p:when>
        
        <p:otherwise>
            <p:identity px:message-severity="WARN" px:message="either the 'test' option or at least one of the 'test-count-*' options must be specified; assertion failed"/>
            <p:add-attribute match="/*" attribute-name="result" attribute-value="false"/>
        </p:otherwise>
    </p:choose>
    
    <p:choose>
        <p:when test="/*/@result='true'">
            <!-- assertion passed; do nothing -->
            <p:identity>
                <p:input port="source">
                    <p:pipe port="source" step="main"/>
                </p:input>
            </p:identity>
        </p:when>
        
        <p:when test="not($error-code='')">
            <!-- assertion failed; throw error -->
            <px:error>
                <p:with-option name="message" select="/*/@message"/>
                <p:with-option name="code" select="$error-code"/>
                <p:with-option name="code-namespace" select="$error-code-namespace"/>
                <p:with-option name="code-prefix" select="$error-code-prefix"/>
            </px:error>
        </p:when>
        
        <p:otherwise>
            <!-- assertion failed; display warning -->
            <p:variable name="msg" select="/*/@message"/>
            <p:identity px:message-severity="WARN" px:message="{$msg}">
                <p:input port="source">
                    <p:pipe port="source" step="main"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    
    <p:identity name="result"/>
    
</p:declare-step>