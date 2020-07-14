<p:declare-step version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:xslt-warning">
    
    <p:output port="result"/>
    
    <p:xslt>
        <p:input port="source">
            <p:inline>
                <hello/>
            </p:inline>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
                    <xsl:template match="hello">
                        <xsl:copy/>
                    </xsl:template>
                    <!-- causes ambiguous rule match warning -->
                    <xsl:template match="hello">
                        <xsl:copy>
                            <xsl:attribute name="world" select="''"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">xslt-warning</h1>
        <p px:role="desc">xslt-warning</p>
    </p:documentation>
    
</p:declare-step>
