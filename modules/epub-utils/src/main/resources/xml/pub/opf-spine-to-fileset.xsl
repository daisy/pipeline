<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

	<xsl:param name="ignore-missing" required="yes"/>

	<xsl:template match="/">
		<xsl:variable name="fileset" as="element(d:fileset)" select="collection()[2]/*"/>
		<xsl:for-each select="$fileset">
			<xsl:copy>
				<xsl:sequence select="@*"/>
				<xsl:for-each select="collection()[1]">
					<xsl:variable name="opf-base" select="base-uri(/*)"/>
					<xsl:for-each select="/*/spine/itemref">
						<xsl:variable name="idref" select="@idref"/>
						<xsl:variable name="item" as="element(opf:item)*" select="/*/manifest/item[@id=$idref]"/>
						<xsl:if test="not(count($item)=1)">
							<xsl:message terminate="yes" select="concat('itemref must point at exactly one item: ', $idref)"/>
						</xsl:if>
						<xsl:variable name="href" select="$item/resolve-uri(@href,$opf-base)"/>
						<xsl:variable name="file" as="element(d:file)?" select="$fileset/d:file[resolve-uri(@href,base-uri(.))=$href]"/>
						<xsl:if test="not($ignore-missing='true' or exists($file))">
							<xsl:message terminate="yes" select="concat('Spine item not found: ', $href)"/>
						</xsl:if>
						<xsl:for-each select="$file">
							<xsl:copy>
								<xsl:sequence select="@* except @media-type"/>
								<xsl:sequence select="$item/@media-type"/>
								<xsl:sequence select="node()"/>
							</xsl:copy>
						</xsl:for-each>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:copy>
		</xsl:for-each>
	</xsl:template>

</xsl:stylesheet>
