<?xml version="1.0" encoding="utf-8"?>
<!--
	List fix
		Version
			2004-04-10

		Description
			Prepare a dtbook to the Narrator schematron rules:
			- Rule 07: No <list> or <dl> inside <p>

		Features
			- Breaks the parent paragraph into a sequence of paragraphs, list and dl
			- Each newly created paragraph has the same attributes as the original one
			- New paragraph IDs are created if necessary
			- The original paragraph ID is conserved for the first paragraph created

		Nodes
			p with a list or dl child

		Namespaces
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Author
			Romain Deltour, DAISY
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns="http://www.daisy.org/z3986/2005/dtbook/">

	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>

	<xsl:template match="dtb:p[dtb:list or dtb:dl]">
		<xsl:apply-templates select="node()" mode="fixList">
			<xsl:with-param name="pattr" select="@*[name()!='id']"/>
			<xsl:with-param name="oldId" select="@id"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="text()" mode="fixList"/>

	<xsl:template match="dtb:list|dtb:dl" mode="fixList">
		<xsl:param name="pattr" />
		<xsl:param name="oldId" />

		<xsl:message>fixing <xsl:value-of select="name()" /></xsl:message>

		<!-- The previous list or dl sibling -->
		<xsl:variable name="prev" select="(preceding-sibling::dtb:list|preceding-sibling::dtb:dl)[last()]"/>	

		<!-- The number of nodes between the previous list or dl and this one -->
		<xsl:variable name="pnumber">
			<xsl:choose>
				<!-- If there is a preceding list or dl, select preceding siblings greater than this one -->
				<xsl:when test="$prev">
					<xsl:value-of select="count(preceding-sibling::node()[
			(preceding-sibling::dtb:list|preceding-sibling::dtb:dl)[last()]=$prev])"/>
				</xsl:when>
				<!-- Otherwise select all the previous siblings -->
				<xsl:otherwise>
					<xsl:value-of select="count(preceding-sibling::node())"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<!-- The position of this list or dl -->
		<xsl:variable name="curPos" select="position()" />
		<!-- The position of the previous list or dl sibling -->
		<xsl:variable name="prevPos" select="$curPos - $pnumber - 1" />
		<!-- The siblings between the previous list and dl and this one -->
		<xsl:variable name="pcontent" select="../node()[position() &lt; $curPos and position() &gt; $prevPos]" />

		<!-- <xsl:message>fixing <xsl:value-of select="@id" />[<xsl:value-of select="$curPos" />] prev <xsl:value-of select="$prev/@id" />[<xsl:value-of select="$prevPos" />]</xsl:message>		 -->

		<!--  Insert the previous content if it's not empty -->
		<xsl:if test="normalize-space($pcontent) != ''">
			<xsl:element name="p">
				<xsl:call-template name="idGen">
					<xsl:with-param name="oldId" select="$oldId"/>
					<xsl:with-param name="genNew" select="count(../node()[position() &lt; $prevPos and not(self::dtb:list or self::dtb:dl) and normalize-space(.)!=''])!=0"/>
				</xsl:call-template>
				<xsl:copy-of select="$pattr"/>
				<xsl:copy-of select="$pcontent"/>
			</xsl:element>
		</xsl:if>

		<!-- Copy this list or dl -->
		<xsl:copy-of select="."/>

		<!-- the following siblings if there are no list or dl following -->
		<xsl:if test="not(following-sibling::dtb:list or following-sibling::dtb:dl)">
			<xsl:element name="p">
				<xsl:call-template name="idGen">
					<xsl:with-param name="oldId" select="$oldId"/>
					<xsl:with-param name="genNew" select="count(preceding-sibling::node()[not(dtb:list|dtb:dl) and normalize-space(.)!=''])>0"/>
				</xsl:call-template>
				<xsl:copy-of select="$pattr"/>
				<xsl:copy-of select="following-sibling::node()" />
			</xsl:element>
		</xsl:if>
	</xsl:template>

	<!-- Generates an ID if there was one originally and if there is some previous content -->
	<xsl:template name="idGen">
		<xsl:param name="oldId" />
		<xsl:param name="genNew" />
		<xsl:choose>
			<!-- Do nothing if the original par didn't have an ID -->
			<xsl:when test="not($oldId)">
			</xsl:when>
			<!-- Generate a new ID  -->
			<xsl:when test="$genNew">
				<xsl:attribute name="id"><xsl:value-of select="generate-id()" /></xsl:attribute>
			</xsl:when>
			<!-- Copy the old ID -->
			<xsl:otherwise>
				<xsl:copy-of select="$oldId"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>