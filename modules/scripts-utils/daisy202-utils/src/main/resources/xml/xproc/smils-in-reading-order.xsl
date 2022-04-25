<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:template match="/">
		<xsl:variable name="smils" as="xs:anyURI*"
		              select="for $href in //body//a/@href return
		                      resolve-uri(
		                        pf:recompose-uri(pf:tokenize-uri(pf:normalize-uri($href))[position()&lt;5]),
		                        pf:base-uri($href/..))"/>
		<d:fileset>
			<xsl:for-each-group select="$smils" group-by="string(.)">
				<d:file href="{current()}"/>
			</xsl:for-each-group>
		</d:fileset>
	</xsl:template>

</xsl:stylesheet>
