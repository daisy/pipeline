<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    exclude-inline-prefixes="#all"
    type="px:validate-braille" version="1.0">
    
    <p:input port="source"/>
    <p:output port="result"/>
    
    <p:option name="assert-valid" select="'true'"/>
    
    <p:try>
        <p:group>
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="validate-braille.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
            <p:sink/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="main" port="source"/>
                </p:input>
            </p:identity>
        </p:group>
        <p:catch name="catch-error">
            <p:variable name="cause" select="string(/*/c:error[1])">
                <p:pipe step="catch-error" port="error"/>
            </p:variable>
            <p:string-replace match="/message/text()" name="error-message">
                <p:input port="source">
                    <p:inline><message>$message</message></p:inline>
                </p:input>
                <p:with-option name="replace" select="concat('&quot;Document is invalid. ', $cause, '&quot;')"/>
            </p:string-replace>
            <p:choose>
                <p:when test="$assert-valid='true'">
                    <p:error code="px:brl04">
                        <p:input port="source">
                            <p:pipe step="error-message" port="result"/>
                        </p:input>
                    </p:error>
                </p:when>
                <p:otherwise>
                    <p:identity>
                        <p:input port="source">
                            <p:pipe step="main" port="source"/>
                        </p:input>
                    </p:identity>
                </p:otherwise>
            </p:choose>
        </p:catch>
    </p:try>
    
</p:declare-step>
