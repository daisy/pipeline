<p:declare-step version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                type="px:java-function-runtime-error">
    
    <p:output port="result"/>
    
    <p:xslt template-name="start">
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
                    <xsl:template name="start">
                        <xsl:call-template name="a"/>
                    </xsl:template>
                    <xsl:template name="a">
                        <xsl:call-template name="b"/>
                    </xsl:template>
                    <xsl:template name="b">
                        <xsl:value-of select="pf:user-function()"/>
                    </xsl:template>
                    <xsl:function name="pf:user-function">
                        <xsl:value-of select="pf:java-function()"/>
                    </xsl:function>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
