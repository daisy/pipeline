<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
	version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
	>
	<!--
	<xsl:import href="dtbook_to_rtf_encode.xsl"/>
	<xsl:import href="dtbook_to_rtf_styles.xsl"/>-->
	<xsl:output method="text" indent="yes" encoding="Windows-1252"/>
	<xsl:strip-space elements="*"/>
	
	<!-- ##### notes and annotations ##### -->
	
	<!-- #### noteref ELEMENT #### -->
	<xsl:template match="noteref|dtb:noteref">
		<xsl:variable name="nested"	select="ancestor::note|ancestor::dtb:note|ancestor::annotation|ancestor::dtb:annotation"/>
		<xsl:choose>
			<xsl:when test="starts-with(@idref, '#')">
				<xsl:call-template name="FOOTNOTE">
					<xsl:with-param name="noteid" select="substring-after(@idref, '#')"/>
					<xsl:with-param name="nested" select="$nested"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="not(contains(@idref, '#'))">
				<xsl:call-template name="FOOTNOTE">
					<xsl:with-param name="noteid" select="@idref"/>
					<xsl:with-param name="nested" select="$nested"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>{\super [See: </xsl:text>
				<xsl:call-template name="rtf-encode">
					<xsl:with-param name="str" select="@idref"/>
				</xsl:call-template>
				<xsl:text>]}</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="FOOTNOTE">
		<xsl:param name="noteid"/>
		<xsl:param name="nested"/>
		<xsl:variable name="note" select="//*[@id=$noteid][self::note|self::dtb:note]"/>
		<xsl:if test="$note">
			<xsl:choose>
				<xsl:when test="$note[contains(@class, 'endnote')]">
					<xsl:call-template name="ANNOREF">
						<xsl:with-param name="href" select="concat('#', $noteid)"/>
					</xsl:call-template>
				</xsl:when>
				<xsl:when test="$nested">
					<!-- Nested notes are displayed in their full in superscript -->
					<xsl:text>{\super </xsl:text>
					<xsl:apply-templates select="//note[@id=$noteid]//text()"/>
					<xsl:apply-templates select="//dtb:note[@id=$noteid]//text()"/>
					<xsl:text>}</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:variable name="no" select="normalize-space(text())"/>
					<xsl:choose>
						<xsl:when test="$no">
							<xsl:value-of select="concat('{\super ', $no, '}')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>{\super \chftn}</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:text>{\footnote</xsl:text>
					<xsl:call-template name="NORMAL_STYLE_FONT_ONLY"/>
					<xsl:choose>
						<xsl:when test="$no">
							<xsl:value-of select="concat('{\super ', $no, '}')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>{\super \chftn}</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:for-each select="$note/node()">
						<xsl:choose>
							<xsl:when test="last()">
								<xsl:choose>
									<xsl:when test="self::p|self::dtb:p">
										<xsl:call-template name="NORMAL_STYLE_FONT_ONLY"/>
										<xsl:apply-templates/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:apply-templates select="self::node()"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:apply-templates select="self::node()"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
					<xsl:text>}</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	
	<!-- #### annoref ELEMENT #### -->
	<xsl:template match="annoref|dtb:annoref">
		<xsl:choose>
			<xsl:when test="not(contains(@idref, '#'))">
				<xsl:call-template name="ANNOREF">
					<xsl:with-param name="href" select="concat('#', @idref)"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="ANNOREF">
					<xsl:with-param name="href" select="@idref"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="ANNOREF">
		<xsl:param name="href"/>
		<xsl:choose>
			<xsl:when test="starts-with($href, '#') or @external='false'">
				<xsl:text>{\super{\field</xsl:text>
				<xsl:text>{\*\fldinst HYPERLINK \\l </xsl:text>
				<xsl:value-of select="substring-after($href, '#')"/>
				<xsl:text>}</xsl:text>
				<xsl:text>{\fldrslt </xsl:text>
				<xsl:text>\cf3\ul </xsl:text>
				<xsl:apply-templates/>
				<xsl:text>}</xsl:text>
				<xsl:text>}}</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<xsl:text>{\super{\field</xsl:text>
				<xsl:text>{\*\fldinst HYPERLINK </xsl:text>
				<xsl:value-of select="$href"/>
				<xsl:text>}</xsl:text>
				<xsl:text>{\fldrslt </xsl:text>
				<xsl:text>\cf3\ul </xsl:text>
				<xsl:apply-templates/>
				<xsl:text>}</xsl:text>
				<xsl:text>}}</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<!-- #### _note ELEMENTS (skipped when not class 'endnote') #### -->
	<xsl:template match="note|dtb:note">
		<xsl:variable name="noteid" select="@id"/>
		<xsl:if test="contains(@class,'endnote')">
			<xsl:for-each select="//*[@idref=$noteid or @idref=concat('#', $noteid)][self::noteref|self::dtb:noteref]">
				<xsl:if test=".//text()">
					<xsl:text>{\pard\super{\field</xsl:text>
					<xsl:text>{\*\fldinst HYPERLINK \\l </xsl:text>
					<xsl:value-of select="@id"/>
					<xsl:text>}</xsl:text>
					<xsl:text>{\fldrslt </xsl:text>
					<xsl:text>\cf3\ul </xsl:text>
					<xsl:apply-templates select=".//text()"/>
					<xsl:text>}</xsl:text>
					<xsl:text>}} </xsl:text>
				</xsl:if>
			</xsl:for-each>
			<xsl:apply-templates/>
		</xsl:if>
	</xsl:template>

	<!-- #### annotation ELEMENTS #### -->
	<xsl:template match="annotation|dtb:annotation">
		<xsl:apply-templates/>
	</xsl:template>



</xsl:stylesheet>
