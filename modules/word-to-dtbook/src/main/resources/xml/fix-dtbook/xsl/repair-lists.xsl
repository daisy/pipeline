<?xml version="1.0" encoding="utf-8"?>
<!--
	List fix
		Version
			2007-12-13

		Description
			List fix:
				- wraps a list in li when the parent of the list is another list
				- adds @type if missing (default value is "pl")
				- corrects @depth atribute
				- removes enum attribute if the list is not ordered
				- removes start attribute if the list is not ordered

		Nodes
			list, li

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
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>

	<xsl:template match="dtb:list[parent::dtb:list]">
		<xsl:message terminate="no">Adding li-tag around list-tag</xsl:message>
		<xsl:element name="li" namespace="http://www.daisy.org/z3986/2005/dtbook/">
			<xsl:call-template name="copyList"/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="dtb:list">
		<xsl:call-template name="copyList"/>
	</xsl:template>
	
	<xsl:template name="copyList">
		<xsl:copy>
			<xsl:copy-of select="@*[not(local-name()='depth') and 
			                        not(local-name()='enum') and 
			                        not(local-name()='start')]"/>
			<xsl:if test="not(@type)">
				<xsl:message terminate="no">Adding @type="pl" to list-tag</xsl:message>
				<xsl:attribute name="type">pl</xsl:attribute>
			</xsl:if>
			<xsl:if test="@depth">
				<xsl:if test="@depth!=count(ancestor-or-self::dtb:list)">
					<xsl:message>Correcting list depth attribute</xsl:message>
				</xsl:if>				
				<xsl:attribute name="depth">
					<xsl:value-of select="count(ancestor-or-self::dtb:list)"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="@enum">
				<xsl:choose>
					<xsl:when test="not(@type='ol')">
						<xsl:message>Removing enum attribute from non-ordered list</xsl:message>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="@enum"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
			<xsl:if test="@start">
				<xsl:choose>
					<xsl:when test="not(@type='ol')">
						<xsl:message>Removing start attribute from non-ordered list</xsl:message>
					</xsl:when>
					<xsl:otherwise>
						<xsl:copy-of select="@start"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>