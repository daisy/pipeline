<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">

    <!-- <xsl:use-package name="http://www.daisy.org/pipeline/modules/css-utils/library.xsl"/> -->
    <xsl:import href="library.xsl"/>

    <xsl:param name="source" as="xs:string"/>
    <xsl:param name="context.fileset" as="document-node(element(d:fileset))?"/>
    <xsl:param name="context.in-memory" as="document-node()*"/>

    <xsl:template name="main">
        <xsl:sequence select="pf:css-to-fileset($source,$context.fileset,$context.in-memory)"/>
    </xsl:template>

</xsl:stylesheet>
