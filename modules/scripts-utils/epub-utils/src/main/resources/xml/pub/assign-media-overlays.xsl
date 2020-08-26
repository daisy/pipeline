<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:s="http://www.w3.org/ns/SMIL"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

    <xsl:key name="text-refs" match="s:text/@src|@epub:textref"
             use="resolve-uri(substring-before(.,'#'),pf:base-uri(..))"/>

    <xsl:template match="opf:manifest">
        <xsl:variable name="manifest-base" select="pf:base-uri(.)"/>
        <xsl:variable name="manifest-with-smil">
            <xsl:copy>
                <xsl:sequence select="@*|opf:item"/>
                <xsl:variable name="smil-uris" select="collection()/s:smil/base-uri()"/>
                <xsl:variable name="existing-item-uris" select="opf:item/resolve-uri(@href,base-uri(.))"/>
                <xsl:variable name="new-smil-uris" select="$smil-uris[not(.=$existing-item-uris)]"/>
                <xsl:for-each select="$new-smil-uris">
                    <item href="{pf:relativize-uri(.,$manifest-base)}" media-type="application/smil+xml"/>
                </xsl:for-each>
                <xsl:variable name="audio-uris" as="xs:string*">
                    <xsl:variable name="audio-uris" as="xs:string*">
                        <xsl:for-each select="collection()/s:smil">
                            <xsl:variable name="smil-base" select="base-uri(.)"/>
                            <xsl:sequence select="distinct-values(//s:audio/resolve-uri(@src,$smil-base))"/>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:sequence select="distinct-values($audio-uris)"/>
                </xsl:variable>
                <xsl:variable name="new-audio-uris" select="$audio-uris[not(.=$existing-item-uris)]"/>
                <xsl:variable name="mo-fileset" as="element(d:fileset)" select="collection()/d:fileset"/>
                <xsl:for-each select="$new-audio-uris">
                    <xsl:variable name="audio-uri" select="."/>
                    <xsl:variable name="audio-file" as="element(d:file)?"
                                  select="$mo-fileset/d:file[resolve-uri(@href,base-uri())=$audio-uri]"/>
                    <item href="{pf:relativize-uri($audio-uri,$manifest-base)}" media-type="{$audio-file/@media-type}"/>
                </xsl:for-each>
            </xsl:copy>
        </xsl:variable>
        <xsl:variable name="manifest-with-smil">
            <xsl:apply-templates mode="add-ids" select="$manifest-with-smil"/>
        </xsl:variable>
        <xsl:apply-templates mode="assign-media-overlays" select="$manifest-with-smil">
            <xsl:with-param name="manifest-base" tunnel="yes" select="$manifest-base"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="add-ids" match="opf:manifest">
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'item_'"/>
            <xsl:with-param name="for-elements" select="opf:item[not(@id)]"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="add-ids" match="opf:item[not(@id)]">
        <xsl:copy>
            <xsl:call-template name="pf:generate-id"/>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="assign-media-overlays" match="opf:item[@media-type='application/xhtml+xml']">
        <xsl:param name="manifest-base" tunnel="yes" required="yes"/>
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:variable name="mo"
                          select="collection()[/s:smil]
                                              [exists(key('text-refs',current()/resolve-uri(@href,$manifest-base),.))]"/>
            <xsl:if test="exists($mo)">
                <xsl:variable name="mo-base" select="base-uri($mo/*)"/>
                <xsl:attribute name="media-overlay" select="../opf:item[resolve-uri(@href,$manifest-base)=$mo-base]/@id"/>
            </xsl:if>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="#default add-ids assign-media-overlays" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
