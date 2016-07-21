<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/">
    
    <xsl:variable name="metadata" select="collection()[2]//opf:metadata"/>
    
    <xsl:template match="*[local-name()='meta']">
        <xsl:variable name="this" select="."/>
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:for-each select="(
                'title',
                'creator',
                'subject',
                'description',
                'language',
                'publisher',
                'contributor',
                'date')">
                <xsl:variable name="name" select="."/>
                <xsl:if test="not($this/dc:*[local-name()=$name])">
                    <xsl:variable name="value" select="(
                        $metadata/dc:*[local-name()=$name]/string(),
                        $metadata/opf:meta[(string(@name),string(@property))=($name,concat('dc:',$name),concat('dcterms:',$name))]/string(),
                        $metadata/opf:meta[(string(@name),string(@property))=($name,concat('dc:',$name),concat('dcterms:',$name))]/@content/string())[1]"/>
                    <xsl:if test="$value != ''">
                        <xsl:element name="dc:{$name}">
                            <xsl:sequence select="$value"/>
                        </xsl:element>
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
