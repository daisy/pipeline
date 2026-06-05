<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:tokenize" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    version="1.0">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>This step invokes the XPath <code>fn:tokenize</code> function with its options as
            arguments and returns the result as a sequence of <code>c:result</code> documents.</p>
    </p:documentation>

    <p:option name="string" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input string. If a zero-length string is supplied, the step returns an empty
                sequence.</p>
        </p:documentation>
    </p:option>
    <p:option name="regex" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The regular expression used to match separators.</p>
        </p:documentation>
    </p:option>
    <p:option name="flags" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>One or more letters indicating options on how the matching is to be performed. If
                this argument is omitted, the effect is the same as supplying a zero-length string,
                which defaults all the option settings.</p>
        </p:documentation>
    </p:option>

    <p:output port="result" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A sequence of <code>c:result</code> documents whose content are substrings of the
                input string.</p>
        </p:documentation>
        <p:pipe port="secondary" step="tokenize-xslt"/>
    </p:output>

    <p:xslt template-name="tokenize" name="tokenize-xslt">
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:with-param name="string" select="$string"/>
        <p:with-param name="regex" select="$regex"/>
        <p:with-param name="flags" select="$flags"/>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0"
                    exclude-result-prefixes="#all">
                    <xsl:param name="string" as="xs:string"/>
                    <xsl:param name="regex" as="xs:string"/>
                    <xsl:param name="flags" as="xs:string"/>
                    <xsl:template name="tokenize">
                        <xsl:for-each select="tokenize($string,$regex,$flags)">
                            <xsl:result-document href="{resolve-uri(concat('result-',position()))}">
                                <c:result><xsl:value-of select="."/></c:result>
                            </xsl:result-document>
                        </xsl:for-each>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:xslt>
    <p:sink/>
    
</p:declare-step>
