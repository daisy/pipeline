<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:my="http://my-functions"
                exclude-result-prefixes="#all">
	
	<xsl:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/transform/block-translator-template.xsl"/>
	<xsl:import href="functions.xsl"/>
	<xsl:import href="select-braille-table.xsl"/>
	<xsl:import href="handle-elements.xsl"/>
	
	<xsl:param name="virtual.dis-uri" select="resolve-uri('../liblouis/virtual.dis')"/> <!-- must be file URI -->
	<xsl:param name="hyphenator" required="yes"/>
	<xsl:param name="ascii-braille" select="'no'"/>
	
	<xsl:variable name="TABLE_BASE_URI"
	              select="concat($virtual.dis-uri,',http://www.sbs.ch/pipeline/liblouis/tables/')"/>
	
	<xsl:template match="css:block" mode="#default before after">
		<xsl:apply-templates/>
	</xsl:template>
	
	<xsl:template name="translate" as="text()">
		<xsl:param name="table" as="xs:string" required="no">
		  <xsl:call-template name="my:get-tables"/>
		</xsl:param>
		<xsl:param name="text" as="xs:string" required="no" select="string()"/>
		<xsl:param name="source-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:variable name="inline-style" as="element()*">
		  <xsl:apply-templates select="$source-style" mode="property"/>
		</xsl:variable>
		<xsl:variable name="unicode-braille"
			      select="pf:text-transform(
		                      concat('(liblouis-table:&quot;',$table,'&quot;)',$hyphenator),
		                      $text,
		                      css:serialize-declaration-list($inline-style))"/>
		<xsl:choose>
		  <xsl:when test="$ascii-braille = 'yes'">
		    <xsl:variable name="ascii-braille" as="xs:string*">
		      <xsl:analyze-string regex="[\s&#x00A0;&#x00AD;&#x200B;]+" select="$unicode-braille">
			<xsl:matching-substring>
			  <xsl:sequence select="translate(.,'&#x00AD;&#x200B;','tm')"/>
			</xsl:matching-substring>
			<xsl:non-matching-substring>
			  <xsl:sequence select="pef:encode('(liblouis-table:&quot;sbs.dis&quot;)', .)"/>
			</xsl:non-matching-substring>
		      </xsl:analyze-string>
		    </xsl:variable>
		    <xsl:value-of select="string-join($ascii-braille,'')"/>
		  </xsl:when>
		  <xsl:otherwise>
		    <xsl:value-of select="$unicode-braille"/>
		  </xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="css:property" mode="property">
		<xsl:if test="not(@value=css:initial-value(@name))">
			<xsl:sequence select="."/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="css:property[@name='word-spacing']" mode="property"/>
	
	<xsl:template match="*" priority="10">
		<xsl:param name="source-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:param name="result-style" as="element()*" tunnel="yes"/> <!-- css:property* -->
		<xsl:variable name="style" as="element()*" select="css:deep-parse-stylesheet(@style)"/> <!-- css:rule* -->
		<xsl:variable name="translated-style" as="element()*">
			<xsl:call-template name="translate-style">
				<xsl:with-param name="style" select="$style"/>
				<xsl:with-param name="context" tunnel="yes" select="."/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="source-style" as="element()*">
			<xsl:call-template name="css:computed-properties">
				<xsl:with-param name="properties" select="$text-properties"/>
				<xsl:with-param name="context" select="$dummy-element"/>
				<xsl:with-param name="cascaded-properties" tunnel="yes" select="$style/css:property"/>
				<xsl:with-param name="parent-properties" tunnel="yes" select="$source-style"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="result-style" as="element()*">
			<xsl:call-template name="css:computed-properties">
				<xsl:with-param name="properties" select="$text-properties"/>
				<xsl:with-param name="context" select="$dummy-element"/>
				<xsl:with-param name="cascaded-properties" tunnel="yes" select="$translated-style/css:property"/>
				<xsl:with-param name="parent-properties" tunnel="yes" select="$result-style"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="translated-style" as="element()*">
			<xsl:apply-templates mode="insert-style" select="$translated-style"/>
		</xsl:variable>
		<xsl:next-match>
			<xsl:with-param name="translated-style" tunnel="yes" select="css:serialize-stylesheet($translated-style)"/>
			<xsl:with-param name="source-style" tunnel="yes" select="$source-style"/>
			<xsl:with-param name="result-style" tunnel="yes" select="$result-style"/>
			<xsl:with-param name="hyphenation" tunnel="yes"
			                select="$hyphenation='true' and boolean($source-style/self::css:property[@name='hyphens' and @value='auto'])"/>
		</xsl:next-match>
	</xsl:template>
	
	<xsl:template match="@style">
		<xsl:param name="translated-style" as="xs:string" tunnel="yes"/>
		<xsl:sequence select="css:style-attribute($translated-style)"/>
	</xsl:template>
	
	<xsl:template mode="translate-style" match="css:property[@name='word-spacing']">
		<xsl:sequence select="."/>
	</xsl:template>
	
	<xsl:template mode="translate-style"
	              match="css:property[@name=('letter-spacing',
	                                         'font-style',
	                                         'font-weight',
	                                         'text-decoration',
	                                         'color')]"/>
	
	<xsl:template mode="translate-style" match="css:property[@name='hyphens' and @value='auto']">
		<css:property name="hyphens" value="manual"/>
	</xsl:template>
	
</xsl:stylesheet>
