<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>

	<xsl:param name="dtbook-metadata" as="element(dtb:head)*"/>
	<xsl:param name="brailleCellType" as="xs:string"/>
	<xsl:param name="brailleSystem" as="xs:string*"/>

	<xsl:template match="/metadata">
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<!--== Required metadata
			    - dc:identifier, dc:title and dc:language are also required by EPUB, so should already be present in the
			      input
			      - dc:identifier is generated
			      - dc:title is taken from the required DTBook field
			      - dc:language is taken from xml:lang if not present in DTBook
			    - we also know that dcterms:modified is already present in the input
			    - as well as dc:date, which we know has been generated and not adopted from the DTBook, because we have
			      removed it from the DTBook before passing to dtbook-to-epub3
			    ==-->
			<dc:format>eBraille 1.0</dc:format>
			<!-- dc:creator is adopted from DTBook, but unlike EPUB it is not required by DAISY 3 because "not all
			     documents have known creators" -->
			<xsl:if test="not(dc:creator)">
				<xsl:call-template name="pf:warn">
					<xsl:with-param name="msg" select="'dc:creator unknown'"/>
				</xsl:call-template>
				<dc:creator>Unknown</dc:creator>
			</xsl:if>
			<xsl:if test="not(meta[@property='dcterms:dateCopyrighted'])">
				<meta property="dcterms:dateCopyrighted">
					<xsl:choose>
						<!-- if a dcterms:date refinement on dc:source is present, take that value -->
						<xsl:when test="some $src in dc:source satisfies meta[@property='dcterms:date']
						                                                     [@refines=concat('#',$src/@id)]">
							<xsl:value-of select="(for $src in dc:source return
							                       meta[@property='dcterms:date']
							                           [@refines=concat('#',$src/@id)]
							                       )[1]/string(.)"/>
						</xsl:when>
						<!-- otherwise, if dtb:sourceDate is present in the DTBook, take that value -->
						<xsl:when test="$dtbook-metadata//dtb:meta[lower-case(@name)='dtb:sourceDate']/string(@content)">
							<xsl:value-of select="$dtbook-metadata//dtb:meta[lower-case(@name)='dc:sourceDate']
							                      /string(@content)[1]"/>
						</xsl:when>
						<!-- otherwise if dc:source is unknown and dc:date is present in the DTBook, take that value -->
						<xsl:when test="not(dc:source) and
						                $dtbook-metadata//dtb:meta[lower-case(@name)='dtb:uid']/string(@content) and
						                $dtbook-metadata//dtb:meta[lower-case(@name)='dc:date']/string(@content)">
							<xsl:call-template name="pf:warn">
								<xsl:with-param name="msg"
								                select="'dcterms:dateCopyrighted taken from DTBook''s dc:date'"/>
							</xsl:call-template>
							<xsl:value-of select="$dtbook-metadata//dtb:meta[lower-case(@name)='dc:date']
							                      /string(@content)[1]"/>
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
			<meta property="a11y:brailleCellType">
				<xsl:value-of select="$brailleCellType"/>
			</meta>
			<xsl:for-each select="$brailleSystem">
				<meta property="a11y:brailleSystem">
					<!-- note that there are currently no registered braille codes (see
					     https://daisy.github.io/ebraille/published/registries/codes/), but since a11y:brailleSystem is
					     required, we need to put some value -->
					<xsl:value-of select="."/>
				</meta>
			</xsl:for-each>
			<xsl:if test="not(meta[@property='a11y:completeTranscription'])">
				<!-- assume that DTBook is complete -->
				<xsl:call-template name="pf:warn">
					<xsl:with-param name="msg" select="'a11y:completeTranscription unknown, assuming true'"/>
				</xsl:call-template>
				<meta property="a11y:completeTranscription">true</meta>
			</xsl:if>
			<xsl:if test="not(meta[@property='a11y:producer'])">
				<xsl:call-template name="pf:warn">
					<xsl:with-param name="msg" select="'a11y:producer unknown'"/>
				</xsl:call-template>
				<meta property="a11y:producer">Unknown</meta>
			</xsl:if>
			<xsl:if test="not(meta[@property='a11y:tactileGraphics'])">
				<!-- this can only be true if the DTBook contains tactile graphics -->
				<meta property="a11y:tactileGraphics">false</meta>
			</xsl:if>
			<!--== Recommended metadata ==-->
			<xsl:if test="not(dc:source)">
				<!-- use dtb:uid as dc:source of eBraille publication if dc:source of DTBook is missing -->
				<xsl:variable name="uid" as="xs:string?"
				              select="$dtbook-metadata//dtb:meta[lower-case(@name)='dtb:uid']/string(@content)"/>
				<xsl:if test="$uid">
					<dc:source id="dtb"><xsl:value-of select="$uid"/></dc:source>
					<!-- use DTBook's dc:date as dcterms:date refinement of dc:source -->
					<xsl:variable name="date" as="xs:string?"
					              select="$dtbook-metadata//dtb:meta[lower-case(@name)='dc:date']/string(@content)"/>
					<xsl:if test="$date">
						<meta property="dcterms:date" refines="#dtb">
							<xsl:value-of select="$date"/>
						</meta>
					</xsl:if>
					<!-- use DTBook's dc:publisher as dcterms:publisher refinement of dc:source -->
					<xsl:variable name="publisher" as="xs:string?"
					              select="$dtbook-metadata//dtb:meta[lower-case(@name)='dc:publisher']/string(@content)"/>
					<xsl:if test="$publisher">
						<meta property="dcterms:publisher" refines="#dtb">
							<xsl:value-of select="$publisher"/>
						</meta>
					</xsl:if>
					<!-- use DTBook's dc:rights as dcterms:rights refinement of dc:source -->
					<xsl:variable name="rights" as="xs:string?"
					              select="$dtbook-metadata//dtb:meta[lower-case(@name)='dc:rights']/string(@content)"/>
					<xsl:if test="$rights">
						<meta property="dcterms:rights" refines="#dtb">
							<xsl:value-of select="$rights"/>
						</meta>
					</xsl:if>
				</xsl:if>
			</xsl:if>
			<!--== Additional metadata
			    - we know that schema:accessMode is already present but with the value textual: we will replace that
			      with tactile
			    ==-->
			<meta property="schema:accessMode">tactile</meta>
			<xsl:if test="dc:source">
				<xsl:variable name="id" select="(dc:source[1]/@id,'src')[1]"/>
				<xsl:variable name="sourceDate" as="xs:string?"
				              select="$dtbook-metadata//dtb:meta[@name='dtb:sourceDate']/string(@content)"/>
				<xsl:if test="$sourceDate">
					<meta property="dcterms:date" refines="#{$id}">
						<xsl:value-of select="$sourceDate"/>
					</meta>
				</xsl:if>
				<xsl:variable name="sourcePublisher" as="xs:string?"
				              select="$dtbook-metadata//dtb:meta[@name='dtb:sourcePublisher']/string(@content)"/>
				<xsl:if test="$sourcePublisher">
					<meta property="dcterms:publisher" refines="#{$id}">
						<xsl:value-of select="$sourcePublisher"/>
					</meta>
				</xsl:if>
				<xsl:variable name="sourceRights" as="xs:string?"
				              select="$dtbook-metadata//dtb:meta[@name='dtb:sourceRights']/string(@content)"/>
				<xsl:if test="$sourceRights">
					<meta property="dcterms:rights" refines="#{$id}">
						<xsl:value-of select="$sourceRights"/>
					</meta>
				</xsl:if>
			</xsl:if>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<!--
	    Drop any dc:title without refinement: we know the first dc:title will be the primary one
	    (title-type="main"), whereas subsequent dc:title, coming from doctitle and covertitle, will
	    not have a title-type. Any but the first dc:title may be ignored by reading systems anyway.
	-->
	<xsl:template match="dc:title">
		<xsl:if test="some $id in @id satisfies parent::*/opf:meta[@refines=concat('#',$id)]">
			<xsl:next-match/>
		</xsl:if>
	</xsl:template>

	<!-- Give the first dc:source an ID so that it can be referenced by dcterms:publisher -->
	<xsl:template match="dc:source[not(@id)][not(preceding-sibling::dc:source)]">
		<xsl:copy>
			<xsl:attribute name="id" select="'src'"/>
			<xsl:sequence select="@*|node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="dc:format"/>

	<xsl:template match="meta[@property='schema:accessMode'][normalize-space(string(.))='textual']"/>

	<xsl:template match="node()">
		<xsl:sequence select="."/>
	</xsl:template>

</xsl:stylesheet>
