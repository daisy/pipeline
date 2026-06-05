<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:html="http://www.w3.org/1999/xhtml">

	<xsl:function name="pf:daisy202-identifier" as="xs:string?">
		<xsl:param name="ncc" as="document-node()"/>
		<xsl:sequence select="($ncc/html:html/html:head/html:meta[lower-case(@name)='dc:identifier']/@content)[1]"/>
	</xsl:function>

	<xsl:function name="pf:daisy202-title" as="xs:string?">
		<xsl:param name="ncc" as="document-node()"/>
		<xsl:sequence select="($ncc/html:html/html:head/html:meta[lower-case(@name)='dc:title']/@content)[1]"/>
	</xsl:function>

</xsl:stylesheet>
