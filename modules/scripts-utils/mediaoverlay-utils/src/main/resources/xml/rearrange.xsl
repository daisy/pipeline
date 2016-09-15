<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:di="http://www.daisy.org/ns/pipeline/tmp" xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns="http://www.w3.org/ns/SMIL" xpath-default-namespace="http://www.w3.org/ns/SMIL"
    exclude-result-prefixes="#all" version="2.0">

    <xsl:output indent="yes"/>

    <xsl:variable name="smils" select="/*/*[2]"/>

    <xsl:template match="/*">
        <smil version="3.0">
            <xsl:copy-of select="*[1]/@original-href"/>
            <body>
                <xsl:apply-templates select="*[1]"/>
            </body>
        </smil>
    </xsl:template>
    
    <xsl:template match="di:*">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*[@id]">
        <xsl:variable name="smil-elements"
            select="$smils/*[@src=base-uri(current())]/di:text[@src-fragment=current()/@id]"/>
        <xsl:choose>
            <xsl:when test="exists($smil-elements)">
                <par>
                    <xsl:copy-of select="@epub:type"/>
                    <text src="{concat(base-uri(.),'#',@id)}" id="{$smil-elements[1]/@smil-id}"/>
                    <xsl:variable name="audios" select="$smil-elements/audio"/>
                    <xsl:if test="count(distinct-values($audios/@src))>1">
                        <!--TODO support audio merge-->
                        <xsl:message>WARNING: the audio for the fragment <xsl:sequence
                                select="concat(base-uri(.),'#',@id)"/> spans over multiple
                            files.</xsl:message>
                    </xsl:if>
                    <xsl:if test="count($audios)>0">
                        <audio src="{$audios[1]/@src}" clipBegin="{$audios[1]/@clipBegin}"
                            clipEnd="{$audios[@src=$audios[1]/@src][last()]/@clipEnd}"/>
                    </xsl:if>
                </par>
            </xsl:when>
            <xsl:otherwise>
                <seq src="{base-uri(.)}" fragment="{@id}">
                    <xsl:copy-of select="@epub:type"/>
                    <xsl:apply-templates/>
                </seq>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="text()|comment()|processing-instruction()"/>

</xsl:stylesheet>
