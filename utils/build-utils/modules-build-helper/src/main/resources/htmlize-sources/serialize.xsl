<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:template match="*" mode="serialize">
		<xsl:text>&lt;</xsl:text>
		<xsl:variable name="prefix" select="substring-before(name(),local-name())"/>
		<xsl:if test="$prefix!=''">
			<span class="code-xml-element-prefix">
				<xsl:value-of select="$prefix"/>
			</span>
		</xsl:if>
		<span class="code-xml-element-local-name">
			<xsl:value-of select="local-name()"/>
		</span>
		<xsl:apply-templates select="@*" mode="#current"/>
		<xsl:choose>
			<xsl:when test="node()">
				<xsl:text>&gt;</xsl:text>
				<xsl:apply-templates mode="#current"/>
				<xsl:text>&lt;/</xsl:text>
				<xsl:if test="$prefix!=''">
					<span class="code-xml-element-prefix">
						<xsl:value-of select="$prefix"/>
					</span>
				</xsl:if>
				<span class="code-xml-element-local-name">
					<xsl:value-of select="local-name()"/>
				</span>
				<xsl:text>&gt;</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>/&gt;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="@*" mode="serialize">
		<xsl:text> </xsl:text>
		<xsl:variable name="prefix" select="substring-before(name(),local-name())"/>
		<xsl:if test="$prefix!=''">
			<span class="code-xml-attribute-prefix">
				<xsl:value-of select="$prefix"/>
			</span>
		</xsl:if>
		<span class="code-xml-attribute-local-name">
			<xsl:value-of select="local-name()"/>
		</span>
		<xsl:text>=</xsl:text>
		<span class="code-xml-attribute-value">
			<xsl:text>"</xsl:text>
			<xsl:apply-templates select="." mode="attribute-value"/>
			<xsl:text>"</xsl:text>
		</span>
	</xsl:template>
	
	<xsl:template match="@*" mode="attribute-value">
		<xsl:value-of select="."/>
	</xsl:template>
	
	<xsl:template match="text()" mode="serialize">
		<xsl:apply-templates select="."/>
	</xsl:template>
	
</xsl:stylesheet>
