<?xml version="1.0" encoding="UTF-8"?>
<!--
	Add author and title
		Version
			2008-09-25

		Description
			Inserts docauthor and doctitle

		Nodes
			docauthor
			doctitle

		Namespaces
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Author
			Joel Håkansson, TPB
			Linus Ericson, TPB
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="dtb">

	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>

	<xsl:template name="insertDoctitle">
		<xsl:choose>
			<xsl:when test="not(//dtb:frontmatter/dtb:doctitle)">
				<xsl:choose>
					<xsl:when test="//dtb:meta[@name='dc:Title'][1]/@content!=''">
						<xsl:element name="doctitle">
							<xsl:value-of select="//dtb:meta[@name='dc:Title'][1]/@content"/>
						</xsl:element>
					</xsl:when>
					<xsl:when test="//dtb:doctitle">
						<xsl:copy-of select="(//dtb:doctitle)[1]"/>
					</xsl:when>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="(//dtb:frontmatter/dtb:doctitle)[1]"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="insertDocauthor">
		<xsl:choose>
			<xsl:when test="not(//dtb:frontmatter/dtb:docauthor)">
				<xsl:for-each select="//dtb:meta[@name='dc:Creator']">
					<xsl:if test="@content!=''">
						<xsl:element name="docauthor">
							<xsl:value-of select="@content"/>
						</xsl:element>
					</xsl:if>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy-of select="//dtb:frontmatter/dtb:docauthor"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="dtb:frontmatter">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:call-template name="insertDoctitle"/>
			<!-- We apply the covertitle before the docauthor to respect the DTD order -->
			<xsl:apply-templates select="dtb:covertitle"/>
			<xsl:call-template name="insertDocauthor"/>
			<xsl:apply-templates select="*[not(local-name()='covertitle' and namespace-uri()='http://www.daisy.org/z3986/2005/dtbook/')]"/>
		</xsl:copy>
	</xsl:template>
	
	<!-- These elements are already inserted by the insertDoctitle and insertDocauthor functions -->
	<xsl:template match="dtb:frontmatter/dtb:doctitle">
		<xsl:choose>
			<xsl:when test="not(preceding::dtb:doctitle)"><!-- Nothing --></xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:copy-of select="@*"/>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template match="dtb:frontmatter/dtb:docauthor">
	</xsl:template>
	
	<!--
	RD-20110302: no longer systematically create a frontmatter
	<xsl:template match="dtb:book">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:if test="not(dtb:frontmatter) and count(//dtb:meta[@name='dc:Creator' or @name='dc:Title'])&gt;0">
				<xsl:element name="frontmatter">
					<xsl:call-template name="insertDoctitle"/>
					<xsl:call-template name="insertDocauthor"/>
				</xsl:element>
			</xsl:if>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>-->
	
</xsl:stylesheet>
