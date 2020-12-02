<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
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
		<xsl:variable as="namespace-node()*" name="parent-ns" select="parent::*/namespace::*[not(local-name()='xml')]"/>
		<xsl:for-each select="namespace::*[not(local-name()='xml')]">
			<xsl:variable name="name" as="xs:string" select="local-name(.)"/>
			<xsl:variable name="value" as="xs:string" select="string(.)"/>
			<xsl:if test="not(exists($parent-ns[name(.)=$name and string(.)=$value]))">
				<xsl:apply-templates mode="#current" select="."/>
			</xsl:if>
		</xsl:for-each>
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
	
	<xsl:template match="namespace-node()" mode="serialize">
		<xsl:text> </xsl:text>
		<xsl:choose>
			<xsl:when test="local-name()=''">
				<span class="code-xml-attribute-prefix">
					<xsl:text>xmlns</xsl:text>
				</span>
			</xsl:when>
			<xsl:otherwise>
				<span class="code-xml-attribute-prefix">
					<xsl:text>xmlns:</xsl:text>
				</span>
				<span class="code-xml-attribute-local-name">
					<xsl:value-of select="local-name()"/>
				</span>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>=</xsl:text>
		<span class="code-xml-attribute-value">
			<xsl:text>"</xsl:text>
			<xsl:apply-templates select="." mode="attribute-value"/>
			<xsl:text>"</xsl:text>
		</span>
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
	
	<xsl:template match="@*|namespace-node()" mode="attribute-value">
		<xsl:value-of select="."/>
	</xsl:template>
	
	<xsl:template match="text()" mode="serialize">
		<xsl:apply-templates select="."/>
	</xsl:template>
	
</xsl:stylesheet>
