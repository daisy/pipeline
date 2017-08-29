<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="xml" indent="yes"/>

    <xsl:template match="@* | node()">
        <xsl:copy>
			<xsl:if test="self::* and ancestor::*[local-name()='book'] and not(@id)">
				<xsl:attribute name="id">
					<xsl:value-of select="generate-id()"/>
				</xsl:attribute>
			</xsl:if>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>