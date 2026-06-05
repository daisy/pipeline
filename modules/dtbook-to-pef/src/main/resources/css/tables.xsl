<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all"
                version="2.0">

	<!-- Duplicate tables -->

	<xsl:param name="duplicate-tables-with-class" as="xs:string?" required="yes"/>
	<xsl:param name="classes-for-table-duplicates" as="xs:string*" required="yes"/>

	<xsl:template match="/*">
		<xsl:choose>
			<xsl:when test="count($classes-for-table-duplicates)&gt;1">
				<xsl:next-match/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:sequence select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="dtb:table">
		<xsl:variable name="table" select="."/>
		<xsl:choose>
			<xsl:when test="(empty($duplicate-tables-with-class) and not(@class)) or
			                (exists($duplicate-tables-with-class) and @class and tokenize(@class,'\s+')=$duplicate-tables-with-class)">
				<xsl:for-each select="$classes-for-table-duplicates">
					<xsl:variable name="class" select="."/>
					<xsl:variable name="first" select="position()=1"/>
					<xsl:for-each select="$table">
						<xsl:copy>
							<xsl:attribute name="class"
							               select="string-join((
							                         replace($class,'\.',' '),
							                         tokenize(@class,'\s+')[not(.=('',$duplicate-tables-with-class))]),
							                       ' ')"/>
							<xsl:apply-templates select="(@* except @class)|node()">
								<xsl:with-param name="first" tunnel="yes" select="first"/>
							</xsl:apply-templates>
						</xsl:copy>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="@id|@xml:id">
		<xsl:param name="first" tunnel="yes" select="true()"/>
		<xsl:if test="$first">
			<xsl:sequence select="."/>
		</xsl:if>
	</xsl:template>

	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
