<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/ns/SMIL"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xsl"/>

    <xsl:param name="mo-dir" select="''"/>

    <xsl:variable name="content-doc-rel" select="pf:relativize-uri(base-uri(/*),$mo-dir)"/>

    <!-- d:audio-clips document with @src attributes relativized against $mo-dir and @textref attributes normalized and resolved -->
    <xsl:variable name="audio-map" as="document-node(element(d:audio-clips))">
        <xsl:variable name="audio-map" select="collection()[/d:audio-clips]"/>
        <xsl:document>
            <xsl:for-each select="$audio-map/*">
                <xsl:variable name="audio-map-uri" select="base-uri(.)"/>
                <xsl:copy>
                    <xsl:for-each select="*">
                        <xsl:copy>
                            <xsl:sequence select="@* except (@src,@textref)"/>
                            <xsl:attribute name="src" select="pf:relativize-uri(resolve-uri(@src,$audio-map-uri),$mo-dir)"/>
                            <xsl:attribute name="textref" select="pf:normalize-uri(resolve-uri(@textref,$audio-map-uri))"/>
                        </xsl:copy>
                    </xsl:for-each>
                </xsl:copy>
            </xsl:for-each>
        </xsl:document>
    </xsl:variable>

    <xsl:output indent="yes"/>

    <xsl:key name="audio-clips" match="d:clip" use="pf:normalize-uri(resolve-uri(@textref,base-uri(.)))"/>

    <xsl:template match="/*">
        <smil version="3.0">
            <!--TODO declare non-standard @epub:type vocabularies-->
            <xsl:apply-templates/>
        </smil>
    </xsl:template>

    <xsl:template match="html:body" priority="2">
        <!-- If the body element does have an @id, it is assumed to not link to any audio clip. -->
        <body>
            <xsl:if test="@id">
                <xsl:attribute name="epub:textref" select="concat($content-doc-rel,'#',@id)"/>
            </xsl:if>
            <xsl:copy-of select="@epub:type"/>
            <xsl:apply-templates/>
        </body>
    </xsl:template>

    <xsl:template match="html:*[@id]" priority="1">
        <xsl:variable name="textref" select="concat(pf:normalize-uri(pf:html-base-uri(.)),'#',@id)"/>
        <xsl:variable name="clip" as="element(d:clip)?" select="key('audio-clips',$textref,$audio-map)"/>
        <xsl:choose>
            <xsl:when test="exists($clip)">
                <par>
                    <xsl:copy-of select="@epub:type"/>
                    <text src="{concat($content-doc-rel,'#',@id)}"/>
                    <audio>
                        <xsl:copy-of select="$clip/(@src,@clipBegin|@clipEnd)"/>
                    </audio>
                </par>
            </xsl:when>
            <xsl:otherwise>
                <seq epub:textref="{concat($content-doc-rel,'#',@id)}">
                    <xsl:copy-of select="@epub:type"/>
                    <xsl:apply-templates/>
                </seq>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="text()"/>

</xsl:stylesheet>
