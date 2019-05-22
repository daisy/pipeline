<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns="http://www.w3.org/1999/xhtml"
    xpath-default-namespace="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:opf="http://www.idpf.org/2007/opf" exclude-result-prefixes="#all"
    xmlns:xs="http://www.w3.org/2001/XMLSchema">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
    <!--    <xsl:import href="../../../../test/xspec/mock/uri-functions.xsl"/>-->

    <xsl:template match="node()">
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:copy-of select="(/* | opf:*)/namespace::*[not(.=('http://www.idpf.org/2007/opf','http://purl.org/dc/elements/1.1/','http://purl.org/dc/terms/'))]"/>
            <xsl:namespace name="epub" select="'http://www.idpf.org/2007/ops'"/>
            <xsl:attribute name="xml:base" select="base-uri(/*)"/>
            <xsl:if test="@prefix">
                <xsl:attribute name="epub:prefix" select="string-join((/* | opf:*)/@prefix, ' ')"/>
            </xsl:if>

            <head/>
            <body>
                <xsl:variable name="spine" select="opf:spine/*"/>
                <xsl:variable name="toc" select="//nav[matches(@epub:type,'(^|\s)toc(\s|$)')]/ol"/>
                <xsl:variable name="basedir" select="replace(base-uri(),'[^/]+$','')"/>
                <xsl:variable name="spine-annotated">
                    <xsl:for-each select="$spine">
                        <xsl:copy exclude-result-prefixes="#all">
                            <xsl:copy-of select="@*" exclude-result-prefixes="#all"/>
                            <xsl:attribute name="level" select="pf:outline-level($spine, $toc, position())"/>
                        </xsl:copy>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:call-template name="section-nesting">
                    <xsl:with-param name="spine" select="$spine-annotated/*"/>
                    <xsl:with-param name="level" select="1"/>
                </xsl:call-template>
            </body>
        </xsl:copy>
    </xsl:template>

    <xsl:template name="section-nesting">
        <xsl:param name="spine" as="element()*"/>
        <xsl:param name="level" as="xs:integer"/>

        <xsl:for-each-group select="$spine" group-starting-with=".[@level=$level]">
            <section>
                <xsl:attribute name="xml:base" select="replace(base-uri(),'.*/','')"/>
                <xsl:call-template name="section-nesting">
                    <xsl:with-param name="spine" select="current-group()[position() &gt; 1]"/>
                    <xsl:with-param name="level" select="$level + 1"/>
                </xsl:call-template>
            </section>
        </xsl:for-each-group>
    </xsl:template>

    <xsl:function name="pf:outline-level" as="xs:integer">
        <xsl:param name="spine" as="element()*"/>
        <xsl:param name="toc" as="element()"/>
        <xsl:param name="position" as="xs:integer"/>

        <xsl:variable name="href" select="tokenize($spine[$position]/base-uri(),'/')[last()]"/>
        <xsl:variable name="partition" select="$spine[$position]/(tokenize(@epub:type,'\s+')[.=('cover','frontmatter','bodymatter','backmatter')], 'bodymatter')[1]"/>
        <xsl:variable name="division" select="$spine[$position]/(tokenize(@epub:type,'\s+')[not(.=('cover','frontmatter','bodymatter','backmatter'))], 'chapter')[1]"/>
        <xsl:variable name="preceding-partition" select="$spine[$position - 1]/(tokenize(@epub:type,'\s+')[.=('cover','frontmatter','bodymatter','backmatter')], 'bodymatter')[1]"/>
        <xsl:variable name="preceding-division" select="$spine[$position - 1]/(tokenize(@epub:type,'\s+')[not(.=('cover','frontmatter','bodymatter','backmatter'))], 'chapter')[1]"/>
        <xsl:variable name="has-preceding-part" select="xs:boolean(count($spine[position() &lt; $position and tokenize(@epub:type,'\s+')=$partition and tokenize(@epub:type,'\s+')='part']))"/>
        <xsl:variable name="toc-level" select="count(($toc//a[tokenize(@href,'#')[1]=$href])[1]/ancestor::li)"/>

        <xsl:choose>
            <xsl:when test="$toc-level &gt; 0">
                <xsl:value-of select="$toc-level"/>
            </xsl:when>
            <xsl:when test="$partition != $preceding-partition">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:when test="$division = 'part'">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:when test="$division = 'rearnotes' and not($preceding-division = 'rearnotes')">
                <xsl:value-of select="pf:outline-level($spine, $toc, $position - 1) + 1"/>
            </xsl:when>
            <xsl:when test="$has-preceding-part">
                <xsl:value-of select="2"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="1"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:template match="li">
        <section>
            <xsl:apply-templates select="node()"/>
        </section>
    </xsl:template>

    <xsl:template match="span">
        <xsl:element name="{concat('h',min((count(ancestor::li),6)))}">
            <xsl:copy-of select="(@*|node())" exclude-result-prefixes="#all"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="a">
        <xsl:attribute name="xml:base" select="pf:relativize-uri(tokenize(@href,'#')[1],base-uri(.))"/>
    </xsl:template>

</xsl:stylesheet>
