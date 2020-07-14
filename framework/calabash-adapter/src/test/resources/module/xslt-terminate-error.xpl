<p:declare-step version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:xslt-terminate-error">
    
    <p:output port="result"/>
    
    <p:xslt template-name="start">
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
                    <xsl:template name="start">
                        <xsl:message terminate="yes">Runtime Error</xsl:message>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">xslt-terminate-error</h1>
        <p px:role="desc">xslt-terminate-error</p>
    </p:documentation>
    
</p:declare-step>
