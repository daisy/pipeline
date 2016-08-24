<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0" xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml">

    <xsl:template match="/*">
        <html>
            <head/>
            <body>
                <xsl:for-each select="//nav[@epub:type='toc']/ol/li">
                    <h1>
                        <xsl:copy-of select="@id | @class | text()"/>
                    </h1>
                    <xsl:for-each select="ol/li">
                        <h2>
                            <xsl:copy-of select="@id | @class | text()"/>
                        </h2>
                        <xsl:for-each select="ol/li">
                            <h3>
                                <xsl:copy-of select="@id | @class | text()"/>
                            </h3>
                            <xsl:for-each select="ol/li">
                                <h4>
                                    <xsl:copy-of select="@id | @class | text()"/>
                                </h4>
                                <xsl:for-each select="ol/li">
                                    <h5>
                                        <xsl:copy-of select="@id | @class | text()"/>
                                    </h5>
                                    <xsl:for-each select="ol/li">
                                        <h6>
                                            <xsl:copy-of select="@id | @class | text()"/>
                                        </h6>
                                    </xsl:for-each>
                                </xsl:for-each>
                            </xsl:for-each>
                        </xsl:for-each>
                    </xsl:for-each>
                </xsl:for-each>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>
