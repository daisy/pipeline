<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>

	<xsl:param name="brailleCellType" as="xs:string"/>
	<xsl:param name="brailleSystem" as="xs:string*"/>
	<xsl:param name="ebraille-compatibility" as="xs:string?"/> <!-- soft|strict -->

	<!-- from ebraille-metadata input port (currently only for tests) -->
	<xsl:variable name="metadata-input" as="document-node()?" select="collection()[2]"/>

	<xsl:template match="/">
		<metadata>
			<!-- ==========================
			     required eBraille metadata
			     ========================== -->
			<!-- dc:format also required by EPUB: override for strict compatibility -->
			<xsl:if test="$ebraille-compatibility='strict'">
				<dc:format>eBraille 1.0</dc:format>
			</xsl:if>
			<xsl:if test="$ebraille-compatibility">
				<!-- dc:identifier also required by EPUB but generate new (which will be used as new unique-identifier) -->
				<xsl:if test="not($metadata-input//dc:identifier[not(@refines)])">
					<!-- dummy token will be replaced by p:uuid step -->
					<dc:identifier id="pub-id">@@@</dc:identifier>
				</xsl:if>
				<!-- dc:date also required by EPUB but generate new -->
				<xsl:if test="not($metadata-input//dc:date[not(@refines)])">
					<dc:date>
						<xsl:value-of select="format-dateTime(
						                        adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H')),
						                        '[Y0001]-[M01]-[D01]')"/>
					</dc:date>
				</xsl:if>
			</xsl:if>
			<!-- dc:title also required by EPUB -->
			<!-- generate dc:creator if not present yet -->
			<xsl:if test="$ebraille-compatibility">
				<xsl:if test="not(collection()//dc:creator[not(@refines)])">
					<xsl:call-template name="pf:warn">
						<xsl:with-param name="msg" select="'dc:creator unknown'"/>
					</xsl:call-template>
					<dc:creator>Unknown</dc:creator>
				</xsl:if>
			</xsl:if>
			<!-- dc:language also required by EPUB and updated in braille-rendition.package-document.xsl -->
			<!-- dcterms:modified will be generated/updated by px:epub3-add-metadata -->
			<!-- generate dcterms:dateCopyrighted if not present -->
			<xsl:if test="$ebraille-compatibility">
				<xsl:if test="not(collection()//meta[@property='dcterms:dateCopyrighted'][not(@refines)])">
					<meta property="dcterms:dateCopyrighted">
						<xsl:choose>
							<!-- if a dcterms:date refinement on dc:source is present, take that value -->
							<xsl:when test="some $src in collection()//dc:source[not(@refines)]
							                satisfies $src/../meta[@property='dcterms:date']
							                                      [@refines=concat('#',$src/@id)]">
								<xsl:value-of select="(for $src in collection()//dc:source[not(@refines)] return
								                       $src/../meta[@property='dcterms:date']
								                                   [@refines=concat('#',$src/@id)]
								                       )[1]/string(.)"/>
							</xsl:when>
							<!-- otherwise if dc:source is unknown, take dc:date -->
							<xsl:when test="not(collection()//dc:source[not(@refines)]) and
							                //dc:date[not(@refines)]">
								<xsl:call-template name="pf:warn">
									<xsl:with-param name="msg"
									                select="'dcterms:dateCopyrighted taken from original rendition''s dc:date'"/>
								</xsl:call-template>
								<xsl:value-of select="//dc:date[not(@refines)]/string(.)[1]"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:call-template name="pf:warn">
									<xsl:with-param name="msg" select="'dcterms:dateCopyrighted unknown'"/>
								</xsl:call-template>
								<xsl:text>Unknown</xsl:text>
							</xsl:otherwise>
						</xsl:choose>
					</meta>
				</xsl:if>
			</xsl:if>
			<!-- generate braille code metadata -->
			<xsl:if test="$ebraille-compatibility">
				<meta property="a11y:brailleCellType">
					<xsl:value-of select="$brailleCellType"/>
				</meta>
				<xsl:for-each select="$brailleSystem">
					<meta property="a11y:brailleSystem">
						<!-- note that there are currently no registered braille codes (see
						     https://daisy.github.io/ebraille/published/registries/codes/), but since
						     a11y:brailleSystem is required, we need to put some value -->
						<xsl:value-of select="."/>
					</meta>
				</xsl:for-each>
			</xsl:if>
			<!-- generate a11y:completeTranscription if not present -->
			<xsl:if test="$ebraille-compatibility">
				<xsl:if test="not(collection()//meta[@property='a11y:completeTranscription'])">
					<!-- assume that EPUB is complete -->
					<xsl:call-template name="pf:warn">
						<xsl:with-param name="msg" select="'a11y:completeTranscription unknown, assuming true'"/>
					</xsl:call-template>
					<meta property="a11y:completeTranscription">true</meta>
				</xsl:if>
			</xsl:if>
			<!-- generate a11y:producer if not present -->
			<xsl:if test="$ebraille-compatibility">
				<xsl:choose>
					<xsl:when test="collection()//meta[@property='a11y:producer'][not(@refines)]">
						<xsl:call-template name="pf:warn">
							<xsl:with-param name="msg" select="'a11y:producer taken from original''s a11y:producer'"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="pf:warn">
							<xsl:with-param name="msg" select="'a11y:producer unknown'"/>
						</xsl:call-template>
						<meta property="a11y:producer">Unknown</meta>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
			<xsl:if test="$ebraille-compatibility">
				<xsl:if test="not(collection()//meta[@property='a11y:tactileGraphics'][not(@refines)])">
					<!-- this can only be true if the original contains tactile graphics -->
					<meta property="a11y:tactileGraphics">false</meta>
				</xsl:if>
			</xsl:if>
			<!-- ========================
			     recommended for eBraille
			     ======================== -->
			<xsl:if test="$ebraille-compatibility">
				<!-- in absence of a dc:source in the original or the metadate input, use dc:identifier
				     of the original -->
				<xsl:if test="not(collection()//dc:source[not(@refines)])">
					<xsl:variable name="epub-id" as="xs:string?"
					              select="//dc:identifier[@id=/package/@unique-identifier]/string()"/>
					<xsl:if test="$epub-id">
						<dc:source id="epub">
							<xsl:value-of select="string($epub-id)"/>
						</dc:source>
						<!-- use original dc:date as dcterms:date refinement of dc:source -->
						<xsl:variable name="date" as="xs:string?" select="//dc:date/string()"/>
						<xsl:if test="$date">
							<meta property="dcterms:date" refines="#epub">
								<xsl:value-of select="$date"/>
							</meta>
						</xsl:if>
						<!-- use original dc:publisher as dcterms:publisher refinement of dc:source -->
						<xsl:variable name="publisher" as="xs:string?" select="//dc:publisher/string()"/>
						<xsl:if test="$publisher">
							<meta property="dcterms:publisher" refines="#epub">
								<xsl:value-of select="$publisher"/>
							</meta>
						</xsl:if>
						<!-- use original dc:rights as dcterms:rights refinement of dc:source -->
						<xsl:variable name="rights" as="xs:string?" select="//dc:rights/string()"/>
						<xsl:if test="$rights">
							<meta property="dcterms:rights" refines="#epub">
								<xsl:value-of select="$rights"/>
							</meta>
						</xsl:if>
					</xsl:if>
				</xsl:if>
			</xsl:if>
		</metadata>
	</xsl:template>

</xsl:stylesheet>
