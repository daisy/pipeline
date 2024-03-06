<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                exclude-result-prefixes="#all">

	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>

	<!-- https://www.oreilly.com/library/view/regular-expressions-cookbook/9781449327453/ch04s13.html -->
	<xsl:variable name="isbn-regex" as="xs:string" select=
	              "'^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$'"/>

	<xsl:function name="pf:dtbook-source-of-pagination" as="xs:string?">
		<xsl:param name="dtbook" as="document-node(element(dtb:dtbook))"/>
		<xsl:choose>
			<xsl:when test="$dtbook//dtb:head/dtb:meta[@name='pageBreakSource']">
				<xsl:sequence select="$dtbook//dtb:head/dtb:meta[@name='pageBreakSource'][1]"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="source" as="element()*"
				              select="$dtbook//dtb:head/dtb:meta[lower-case(@name)='dc:source']"/>
				<xsl:variable name="uid" as="element()?"
				              select="$dtbook//dtb:head/dtb:meta[lower-case(@name)='dtb:uid'][1]"/>
				<xsl:variable name="isbn" as="element()*"
				              select="($source|$uid)[lower-case(@scheme)='isbn'
				                                     or starts-with(normalize-space(@content),'urn:isbn:')
				                                     or matches(normalize-space(@content),$isbn-regex,';j')]"/>
				<xsl:choose>
					<xsl:when test="count($isbn)=1">
						<xsl:sequence select="$isbn/normalize-space(@content)"/>
					</xsl:when>
					<xsl:when test="count($isbn)&gt;1">
						<xsl:call-template name="pf:warn">
							<xsl:with-param name="msg"
							                select="concat('More than one ISBN found in the DTBook metadata. ',
							                               'Selected the first one as the source of pagination.')"/>
						</xsl:call-template>
						<xsl:sequence select="$isbn[1]/normalize-space(@content)"/>
					</xsl:when>
					<xsl:when test="count($source)=1">
						<xsl:call-template name="pf:warn">
							<xsl:with-param name="msg"
							                select="concat('No ISBN found in the DTBook metadata. ',
							                               'Selected the dc:source as the source of pagination.')"/>
						</xsl:call-template>
						<xsl:sequence select="$source/normalize-space(@content)"/>
					</xsl:when>
					<xsl:when test="count($source)&gt;1">
						<xsl:call-template name="pf:warn">
							<xsl:with-param name="msg"
							                select="concat('No ISBN found in the DTBook metadata. ',
							                               'Selected the first dc:source as the source of pagination.')"/>
						</xsl:call-template>
						<xsl:sequence select="$source[1]/normalize-space(@content)"/>
					</xsl:when>
					<xsl:when test="exists($uid)">
						<xsl:call-template name="pf:warn">
							<xsl:with-param name="msg"
							                select="concat('No ISBN found in the DTBook metadata. ',
							                               'Selected the dtb:uid as the source of pagination.')"/>
						</xsl:call-template>
						<xsl:sequence select="$uid/normalize-space(@content)"/>
					</xsl:when>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:function>

</xsl:stylesheet>
