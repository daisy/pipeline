<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:p="http://www.w3.org/ns/xproc" version="2.0">
    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <p:output port="result"/>
            <xsl:copy-of select="p:variable"/>
            <p:identity>
                <p:input port="source">
                    <p:inline>
                        <c:result>
                            <c:options/>
                            <c:params/>
                        </c:result>
                    </p:inline>
                </p:input>
            </p:identity>
            <xsl:for-each select="(//*[@name='test'])[1]/p:with-option">
                <p:add-attribute match="/*/c:options" attribute-name="{@name}">
                    <xsl:copy-of select="namespace::*"/>
                    <p:with-option name="attribute-value" select="{@select}">
                        <xsl:copy-of select="./*"/>
                    </p:with-option>
                </p:add-attribute>
            </xsl:for-each>
            <xsl:for-each select="(//*[@name='test'])[1]/p:with-param">
                <p:add-attribute match="/*/c:params" attribute-name="{@name}">
                    <xsl:copy-of select="namespace::*"/>
                    <p:with-option name="attribute-value" select="{@select}">
                        <xsl:copy-of select="./*"/>
                    </p:with-option>
                </p:add-attribute>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
