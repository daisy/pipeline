<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:template match="*" mode="serialize">
		<xsl:text>&lt;</xsl:text>
		<xsl:variable name="prefix" select="substring-before(name(),':')"/>
		<xsl:if test="$prefix!=''">
			<span class="code-xml-element-prefix">
				<xsl:value-of select="concat($prefix,':')"/>
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
						<xsl:value-of select="concat($prefix,':')"/>
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
		<xsl:variable name="prefix" select="substring-before(name(),':')"/>
		<xsl:if test="$prefix!=''">
			<span class="code-xml-attribute-prefix">
				<xsl:value-of select="concat($prefix,':')"/>
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
		<xsl:param name="indent" tunnel="yes" select="0"/>
		<xsl:for-each select="tokenize(.,'\n')">
			<xsl:choose>
				<xsl:when test="position()=1">
					<xsl:value-of select="."/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>&#x0A;</xsl:text>
					<xsl:choose>
						<xsl:when test="$indent &gt; 0">
							<xsl:value-of select="string-join((for $i in 1 to $indent return ' ',.),'')"/>
						</xsl:when>
						<xsl:when test="$indent &lt; 0">
							<xsl:variable name="leading-space" select="if (matches(.,'^\s'))
							                                             then replace(.,'^(\s+).*$','$1')
							                                             else ''"/>
							<xsl:value-of select="string-join((for $i in 1 to (string-length($leading-space) + $indent) return ' ',
							                                   replace(.,'^\s+','')),
							                                  '')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="."/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="*[@xml:space='preserve']" mode="serialize">
		<xsl:next-match>
			<xsl:with-param name="indent" tunnel="yes" select="0"/>
		</xsl:next-match>
	</xsl:template>
	
</xsl:stylesheet>
