<?xml version="1.0" encoding="UTF-8"?>
<!--
    Set the dc:Title and frontmatter/doctitle content as required 
    by the Narrator Script
    (these elements are not required by the core DTBook grammar).
    
    Version
    2008-04-03
    
    Description
    - Adds the dc:Title meta element, if not present in input,
    or given but with null/whitespace only content values.
    - Adds the doctitle element in the frontmatter, if not 
    present in input, or given but with null/whitespace only 
    content values.

    Title value is taken:
    - from the 'dc:Title' metadata is present
    - or else from the first 'doctitle' element in the bodymatter
    - or else from the first heading 1.

    Nodes
    dtbook/head
    
    Namespaces
    (x) "http://www.daisy.org/z3986/2005/dtbook/"
    
    Doctype
    (x) DTBook
    
    Author
    Romain Deltour, DAISY
	
-->
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns="http://www.daisy.org/z3986/2005/dtbook/" version="2.0" exclude-result-prefixes="dtb">
	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>
	
	<!-- The title that will be used -->
	<xsl:param name="titleValue">
		<xsl:choose>
			<!-- If it exists, takes the value from the first 'dc:Title' metadata -->
			<xsl:when test="string-length(normalize-space(//dtb:head/dtb:meta[@name='dc:Title']/@content))>0">
				<xsl:value-of select="normalize-space(//dtb:head/dtb:meta[@name='dc:Title']/@content)"/>
			</xsl:when>
			<!-- If it exists, takes the value from the first 'doctitle' in the bodymatter -->
			<xsl:when test="string-length(normalize-space(//dtb:bodymatter//dtb:doctitle[1]))>0">
				<xsl:value-of select="normalize-space(//dtb:bodymatter//dtb:doctitle[1])"/>
			</xsl:when>
			<!-- Otherwise, takes the value from the first h1 or hd (which exists: Rule 100) -->
			<xsl:when test="//dtb:h1">
				<xsl:value-of select="normalize-space(//dtb:h1[1])"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="normalize-space(//dtb:hd[1])"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:param>

	<!-- Add the dc:Title metadata if it's not already there -->
	<xsl:template match="dtb:head">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:if test="count(dtb:meta[@name='dc:Title'])=0">
				<xsl:message terminate="no">Adding dc:Title metadata element</xsl:message>
				<xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Title</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:value-of select="$titleValue"/>
					</xsl:attribute>
				</xsl:element>
			</xsl:if>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<!-- Add the doctitle elment in the frontmatter if it's not already there -->
	<xsl:template match="dtb:frontmatter">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:if test="count(dtb:doctitle)=0">
				<xsl:message terminate="no">Adding doctitle element in the frontmatter</xsl:message>
				<xsl:element name="doctitle" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:value-of select="$titleValue"/>
				</xsl:element>
			</xsl:if>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<!-- Set the 'dc:Title' metadata if it's empty -->
	<xsl:template match="dtb:meta[@name='dc:Title' and normalize-space(@content)='']">
		<xsl:message terminate="no">Adding value to empty dc:Title metadata element</xsl:message>
		<xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
			<xsl:attribute name="name">
				<xsl:text>dc:Title</xsl:text>
			</xsl:attribute>
			<xsl:attribute name="content">
				<xsl:value-of select="$titleValue"/>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>
	
	<!-- Set the front matter 'doctitle' element if it's empty -->
	<xsl:template match="dtb:frontmatter/dtb:doctitle[normalize-space(self::*)='']">
		<xsl:message terminate="no">Adding value to empty doctitle element in the frontmatter</xsl:message>
		<xsl:element name="doctitle" namespace="http://www.daisy.org/z3986/2005/dtbook/">
			<xsl:value-of select="$titleValue"/>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
