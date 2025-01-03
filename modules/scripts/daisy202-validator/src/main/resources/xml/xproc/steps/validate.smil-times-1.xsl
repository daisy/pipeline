<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns="" xpath-default-namespace=""
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    <xsl:import href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>

    <xsl:param name="context" as="document-node()*"/>

    <xsl:template match="/*">
        <c:result>
            <xsl:apply-templates select="*"/>
        </c:result>
    </xsl:template>

    <!-- matching *:smil instead of smil to avoid namespace warning -->
    <xsl:template match="*:smil">
        <smil>
            <xsl:variable name="base" select="base-uri(.)"/> <!-- can not use base-uri(/) because of the p:wrap-sequence -->
            <xsl:attribute name="xml:base" select="$base"/>
            <xsl:variable name="meta-duration" select="head/meta[@name='ncc:timeInThisSmil']/string(@content)"/>
            <xsl:variable name="meta-duration" select="if ($meta-duration)
                                                       then pf:smil-clock-value-to-seconds($meta-duration)
                                                       else 0"/>
            <!-- NOTE: this assumes that if there are no clip-end attribute, only a clip-begin
                 attribute, then the declared SMIL duration is the total duration of the audio file
                 pointed to by that audio element -->
            <xsl:choose>
                <xsl:when test=".//ref"> <!-- replace($base,'^.*/([^/]*)$','$1')=('master.smil','MASTER.SMIL') -->
                    <!-- master SMIL -->
                    <xsl:attribute name="is-master" select="'true'"/>
                    <xsl:variable name="duration" as="xs:decimal*">
                        <xsl:for-each select=".//ref">
                            <xsl:variable name="src" as="xs:string" select="pf:normalize-uri(@src)"/>
                            <xsl:variable name="src" as="xs:string*" select="pf:tokenize-uri($src)"/>
                            <xsl:variable name="fragment" as="xs:string?" select="$src[5]"/>
                            <xsl:variable name="file" as="xs:string" select="pf:recompose-uri($src[position()&lt;5])"/>
                            <!-- base URIs where normalized by px:fileset-load -->
                            <xsl:variable name="file" as="xs:anyURI" select="resolve-uri($file,$base)"/>
                            <xsl:variable name="file" as="document-node()?" select="$context[base-uri(/)=$file]"/>
                            <xsl:choose>
                                <xsl:when test="exists($file)">
                                    <xsl:choose>
                                        <xsl:when test="exists($fragment)">
                                            <xsl:variable name="fragment" as="element()" select="$file//*[@id=$fragment]"/>
                                            <xsl:choose>
                                                <xsl:when test="exists($fragment)">
                                                    <!--
                                                        assuming that author intended to reference the whole SMIL
                                                        (FIXME: check whether we need to raise a warning/error when a fragment is specified)
                                                    --><!--
                                                    <xsl:variable name="fragment" as="element()" select="if ($fragment/self::text)
                                                                                                         then $fragment/..
                                                                                                         else $fragment"/>
                                                    <xsl:sequence select="pf:smil-total-seconds($fragment)"/>-->
                                                    <xsl:sequence select="pf:smil-total-seconds($file/*)"/>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:message select="concat('ref points to a non-existing SMIL element: ',@src)"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:sequence select="pf:smil-total-seconds($file/*)"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:message select="concat('ref points to a non-existing SMIL file: ',@src)"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:attribute name="calculated-duration" select="sum($duration)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="calculated-duration" select="pf:smil-total-seconds(.)"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:for-each select="body//audio[(@clip-begin and not(@clip-end)) or
                                              (not(@clip-begin) and @clip-end)]">
                <xsl:message select="concat('audio clip is missing a ',
                                            (if (@clip-begin) then 'clip-end' else 'clip-begin'),
                                            ' attribute (par id: ',
                                            parent::par/@id,
                                            ')')"/>
            </xsl:for-each>
            <xsl:attribute name="meta-duration" select="$meta-duration"/>
            <xsl:variable name="meta-totalTime" select="head/meta[@name='ncc:totalElapsedTime']
                                                            /string(@content)"/>
            <xsl:attribute name="meta-totalTime" select="if ($meta-totalTime)
                                                         then pf:smil-clock-value-to-seconds($meta-totalTime)
                                                         else 0"/>
        </smil>
    </xsl:template>

</xsl:stylesheet>
