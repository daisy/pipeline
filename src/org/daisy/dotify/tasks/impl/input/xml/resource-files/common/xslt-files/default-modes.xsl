<?xml version="1.0" encoding="utf-8"?>
<?xslt-doc-file doc-files/dtb2obfl.html?>

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.daisy.org/ns/2011/obfl">

	<xsl:output method="xml" encoding="utf-8" indent="no"/>

	<xsl:template match="*" mode="sequence-mode">
		<sequence>
			<xsl:apply-templates select="." mode="apply-sequence-attributes"/>
			<xsl:apply-templates/>
		</sequence>
	</xsl:template>

	<xsl:template match="*" mode="block-mode">
		<block>
			<xsl:apply-templates select="." mode="apply-block-attributes"/>
			<xsl:apply-templates/>
		</block>
	</xsl:template>

	<xsl:template match="*" mode="inline-mode">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="*" mode="apply-sequence-attributes"/>
	<xsl:template match="*" mode="apply-block-attributes"/>

</xsl:stylesheet>
