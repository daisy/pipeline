<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns="urn:oasis:names:tc:opendocument:xmlns:container"
    exclude-result-prefixes="#all" version="2.0">
    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    <xsl:param name="result-base" required="yes"/>
    <xsl:template match="wrapper">
        <container version="1.0">
            <rootfiles>
                <xsl:apply-templates/>
            </rootfiles>
        </container>
    </xsl:template>
    <xsl:template match="d:file">
        <rootfile full-path="{pf:relativize-uri(resolve-uri(@href,base-uri(.)),$result-base)}" media-type="application/oebps-package+xml"/>
    </xsl:template>
</xsl:stylesheet>
