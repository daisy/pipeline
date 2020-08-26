<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">
    
    <xsl:param name="output-base-uri"/>

    <xsl:variable name="output-base-uri-head-with-slashes"
                  select="if (starts-with($output-base-uri,'file:/'))
                          then replace($output-base-uri,'^([^/]+/+).*$','$1')
                          else replace($output-base-uri,'^([^/]+/+[^/]+).*$','$1')"/>
    <xsl:variable name="output-base-uri-head"
                  select="replace(replace($output-base-uri-head-with-slashes,'/+','/'),'/$','')"/>
    <xsl:variable name="output-base-uri-tail"
                  select="replace(
                            replace(
                              if (starts-with($output-base-uri,'file:/'))
                              then replace($output-base-uri,'^file:/+','')
                              else replace($output-base-uri,'^[^/]+/+[^/]+/+',''),
                            '[^/]+$',''),
                          '/+','/')"/>

    <xsl:template match="/*">
        <manifest>
            <xsl:for-each select="*">
                <item>
                    <xsl:copy-of select="@href|@media-type|@id"/>
                    <xsl:variable name="item-uri-head"
                                  select="if (starts-with(resolve-uri(@href,base-uri(.)),'file:/'))
                                          then 'file:'
                                          else replace(replace(resolve-uri(@href,base-uri(.)),'^([^/]+/+[^/]+)/.*$','$1'),'/+','/')"/>
                    <xsl:variable name="item-uri-tail"
                                  select="replace(
                                            if (starts-with(resolve-uri(@href,base-uri(.)),'file:/'))
                                            then replace(resolve-uri(@href,base-uri(.)),'^file:/+','')
                                            else replace(resolve-uri(@href,base-uri(.)),'^[^/]+/+[^/]+/+',''),
                                          '/+','/')"/>
                    <xsl:if test="$item-uri-head=$output-base-uri-head">
                        <xsl:attribute name="href"
                                       select="f:relative-to(tokenize(concat($output-base-uri-head,'/',$output-base-uri-tail),'/+'),
                                                             tokenize(concat($item-uri-head,'/',$item-uri-tail),'/+'),'')"/>
                    </xsl:if>
                    <xsl:variable name="properties" as="xs:string*"
                                  select="(if (@role='cover-image')      then 'cover-image'      else (),
                                           if (@mathml='true')           then 'mathml'           else (),
                                           if (@remote-resources='true') then 'remote-resources' else (),
                                           if (@scripted='true')         then 'scripted'         else (),
                                           if (@svg='true')              then 'svg'              else (),
                                           if (@switch='true')           then 'switch'           else ()
                                           )"/>
                    <xsl:if test="exists($properties)">
                        <xsl:attribute name="properties" select="string-join($properties,' ')"/>
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
                    select="f:relative-to(
                              subsequence($from,2),
                              subsequence($to,min((count($to),2))),
                              concat(
                                if (count($from) and string-length($from[1])) then '../' else '',
                                $relation,
                                if (count($to) &gt; 1 and string-length($to[1])) then concat($to[1],'/') else ''))"/>
            </xsl:when>
            <xsl:when test="count($to) &gt; 1 and $to[1]=$from[1]">
                <xsl:value-of select="f:relative-to(subsequence($from,2), subsequence($to,min((count($to),2))), '')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of
                    select="f:relative-to(
                              subsequence($from,2),
                              subsequence($to,min((count($to),2))),
                              concat(
                                if (count($from) and string-length($from[1])) then '../' else '',
                                if (count($to) &gt; 1 and string-length($to[1])) then concat($to[1],'/') else ''))"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

</xsl:stylesheet>
