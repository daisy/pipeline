<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:template name="main">
		<d:fileset>
			<xsl:for-each select="collection()[1]/nav">
				<xsl:variable name="base" select="base-uri(.)"/>
				<xsl:variable name="anchors" as="element(html:a)*">
					<xsl:for-each select="ol/li/a">
						<a href="{pf:normalize-uri(resolve-uri(@href,$base))}">
							<xsl:sequence select="(@* except @href)|node()"/>
						</a>
					</xsl:for-each>
				</xsl:variable>
				<xsl:for-each-group select="$anchors[contains(@href,'#')]" group-by="substring-before(@href,'#')">
					<d:file href="{substring-before(@href,'#')}">
						<xsl:for-each select="current-group()">
							<d:anchor id="{substring-after(@href,'#')}" title="{string(.)}"/>
						</xsl:for-each>
					</d:file>
				</xsl:for-each-group>
			</xsl:for-each>
		</d:fileset>
	</xsl:template>

</xsl:stylesheet>
