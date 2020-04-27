<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

	<xsl:template name="f:generate-ids" as="xs:string*">
		<xsl:param name="amount" as="xs:integer" required="yes"/>
		<xsl:param name="prefix" as="xs:string" required="yes"/>
		<xsl:param name="in-use" as="xs:string*" select="()"/>
		<xsl:param name="_feed" as="xs:integer" select="1"/>
		<xsl:variable name="ids" as="xs:string*"
		              select="for $i in 1 to $amount return concat($prefix,$_feed + $i - 1)"/>
		<xsl:variable name="ids" as="xs:string*" select="$ids[not(.=$in-use)]"/>
		<xsl:sequence select="$ids"/>
		<xsl:if test="count($ids) &lt; $amount">
			<xsl:call-template name="f:generate-ids">
				<xsl:with-param name="amount" select="$amount - count($ids)"/>
				<xsl:with-param name="prefix" select="$prefix"/>
				<xsl:with-param name="in-use" select="$in-use"/>
				<xsl:with-param name="_feed" select="$_feed + $amount"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template name="pf:next-match-with-generated-ids">
		<xsl:param name="prefix" as="xs:string" required="yes"/>
		<xsl:param name="for-elements" as="element()*" select=".//*"/>
		<xsl:param name="in-use" as="xs:string*" select="root()//@id"/>
		<xsl:variable name="ids" as="xs:string*">
			<xsl:call-template name="f:generate-ids">
				<xsl:with-param name="amount" select="count($for-elements)"/>
				<xsl:with-param name="prefix" select="$prefix"/>
				<xsl:with-param name="in-use" select="$in-use"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="idmap">
			<xsl:document>
				<d:idmap>
					<xsl:for-each select="$for-elements">
						<xsl:variable name="i" select="position()"/>
						<d:id element="{generate-id(.)}" id="{$ids[$i]}"/>
					</xsl:for-each>
				</d:idmap>
			</xsl:document>
		</xsl:variable>
		<xsl:next-match>
			<xsl:with-param name="f:generated-ids" tunnel="yes" select="$idmap"/>
		</xsl:next-match>
	</xsl:template>

	<xsl:key name="f:element" match="d:id" use="string(@element)"/>

	<xsl:template name="pf:generate-id" as="attribute(id)">
		<xsl:param name="f:generated-ids" tunnel="yes" select="()"/>
		<xsl:if test="not(exists($f:generated-ids))">
			<xsl:message terminate="yes" select="'pf:generate-id must be called from within pf:next-match-with-generated-ids.'"/>
		</xsl:if>
		<xsl:variable name="element-id" as="xs:string" select="generate-id(.)"/>
		<xsl:variable name="id" as="attribute()?" select="key('f:element',$element-id,$f:generated-ids)[1]/@id"/>
		<xsl:if test="not(exists($id))">
			<xsl:message terminate="yes">
				<xsl:text>No id generated for </xsl:text>
				<xsl:copy-of select="."/>
				<xsl:text>: element not included in "for-elements" parameter.</xsl:text>
			</xsl:message>
		</xsl:if>
		<xsl:sequence select="$id"/>
	</xsl:template>

</xsl:stylesheet>
