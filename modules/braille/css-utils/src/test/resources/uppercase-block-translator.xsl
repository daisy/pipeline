<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all">
	
	<xsl:import href="../../main/resources/xml/transform/abstract-block-translator.xsl"/>
	
	<xsl:template match="css:block">
		<xsl:variable name="uppercase-text" as="text()*">
			<xsl:apply-templates mode="translate"/>
		</xsl:variable>
		<xsl:apply-templates select="node()[1]" mode="treewalk">
			<xsl:with-param name="new-text-nodes" select="$uppercase-text"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="*" mode="translate">
		<xsl:param name="source-style" as="element()*" tunnel="yes"/>
		<xsl:variable name="source-style" as="element()*">
			<xsl:variable name="stylesheet" as="element()*">
				<xsl:call-template name="css:deep-parse-stylesheet">
					<xsl:with-param name="stylesheet" select="@style"/>
				</xsl:call-template>
			</xsl:variable>
			<xsl:call-template name="css:computed-properties">
				<xsl:with-param name="properties" select="$text-properties"/>
				<xsl:with-param name="context" select="$dummy-element"/>
				<xsl:with-param name="cascaded-properties" tunnel="yes" select="$stylesheet[not(@selector)]/css:property"/>
				<xsl:with-param name="parent-properties" tunnel="yes" select="$source-style"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:apply-templates mode="#current">
			<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="text()" mode="translate">
		<xsl:param name="source-style" as="element()*" tunnel="yes"/>
		<xsl:variable name="uppercase" as="xs:string" select="upper-case(.)"/>
		<xsl:variable name="normalised" as="xs:string"
		              select="if ($source-style[@name='white-space' and not(@value='normal')])
		                      then $uppercase
		                      else normalize-space($uppercase)"/>
		<xsl:variable name="hyphenated" as="xs:string"
		              select="if ($source-style[@name='hyphens' and @value='auto'])
		                      then replace($normalised, 'FOOBAR', 'FOO=BAR')
		                      else $normalised"/>
		<xsl:value-of select="$hyphenated"/>
	</xsl:template>
	
	<xsl:template match="css:property[@name='hyphens' and @value='auto']" mode="translate-style">
		<css:property name="hyphens" value="manual"/>
	</xsl:template>
	
</xsl:stylesheet>
