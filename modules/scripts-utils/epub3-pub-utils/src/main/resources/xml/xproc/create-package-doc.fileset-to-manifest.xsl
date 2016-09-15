<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns="http://www.idpf.org/2007/opf" xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions" exclude-result-prefixes="#all">
    <xsl:output indent="yes"/>
    <xsl:param name="result-uri"/>

    <xsl:variable name="result-uri-head-with-slashes" select="if (starts-with($result-uri,'file:/')) then replace($result-uri,'^([^/]+/+).*$','$1') else replace($result-uri,'^([^/]+/+[^/]+).*$','$1')"/>
    <xsl:variable name="result-uri-head" select="replace(replace($result-uri-head-with-slashes,'/+','/'),'/$','')"/>
    <xsl:variable name="result-uri-tail"
        select="replace(replace(if (starts-with($result-uri,'file:/')) then replace($result-uri,'^file:/+','') else replace($result-uri,'^[^/]+/+[^/]+/+',''),'[^/]+$',''),'/+','/')"/>

    <xsl:template match="/*">
        <manifest>
            <xsl:for-each select="*">
                <item>
                    <xsl:copy-of select="@href|@media-type|@id"/>
                    <xsl:variable name="item-uri-head"
                        select="if (starts-with(resolve-uri(@href,base-uri(.)),'file:/')) then 'file:' else replace(replace(resolve-uri(@href,base-uri(.)),'^([^/]+/+[^/]+)/.*$','$1'),'/+','/')"/>
                    <xsl:variable name="item-uri-tail"
                        select="replace(if (starts-with(resolve-uri(@href,base-uri(.)),'file:/')) then replace(resolve-uri(@href,base-uri(.)),'^file:/+','') else replace(resolve-uri(@href,base-uri(.)),'^[^/]+/+[^/]+/+',''),'/+','/')"/>
                    <xsl:if test="$item-uri-head=$result-uri-head">
                        <xsl:attribute name="href" select="f:relative-to(tokenize(concat($result-uri-head,'/',$result-uri-tail),'/+'),tokenize(concat($item-uri-head,'/',$item-uri-tail),'/+'),'')"/>
                    </xsl:if>
                    <xsl:variable name="cover-image" select="if (@cover-image='true') then 'cover-image' else ''"/>
                    <xsl:variable name="mathml" select="if (@mathml='true') then concat($cover-image,' mathml') else $cover-image"/>
                    <xsl:variable name="nav" select="if (@nav='true') then concat($mathml,' nav') else $mathml"/>
                    <xsl:variable name="remote-resources" select="if (@remote-resources='true') then concat($nav,' remote-resources') else $nav"/>
                    <xsl:variable name="scripted" select="if (@scripted='true') then concat($remote-resources,' scripted') else $remote-resources"/>
                    <xsl:variable name="svg" select="if (@svg='true') then concat($scripted,' svg') else $scripted"/>
                    <xsl:variable name="switch" select="if (@switch='true') then concat($svg,' switch') else $svg"/>
                    <xsl:if test="string-length($switch) &gt; 0">
                        <xsl:attribute name="properties" select="normalize-space($switch)"/>
                    </xsl:if>
                </item>
            </xsl:for-each>
        </manifest>
    </xsl:template>
    <xsl:function name="f:relative-to">
        <xsl:param name="from"/>
        <xsl:param name="to"/>
        <xsl:param name="relation"/>

        <xsl:choose>
            <xsl:when test="count($to) &lt;= 1 and count($from) = 0">
                <xsl:value-of select="concat($relation,$to)"/>
            </xsl:when>
            <xsl:when test="string-length($relation) &gt; 0">
                <xsl:value-of
                    select="f:relative-to(subsequence($from,2), subsequence($to,min((count($to),2))), concat(
                        if (count($from) and string-length($from[1])) then '../' else '',
                        $relation,
                        if (count($to) &gt; 1 and string-length($to[1])) then concat($to[1],'/') else ''
                    ))"
                />
            </xsl:when>
            <xsl:when test="count($to) &gt; 1 and $to[1]=$from[1]">
                <xsl:value-of select="f:relative-to(subsequence($from,2), subsequence($to,min((count($to),2))), '')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of
                    select="f:relative-to(subsequence($from,2), subsequence($to,min((count($to),2))), concat(
                        if (count($from) and string-length($from[1])) then '../' else '',
                        if (count($to) &gt; 1 and string-length($to[1])) then concat($to[1],'/') else ''
                    ))"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
</xsl:stylesheet>
