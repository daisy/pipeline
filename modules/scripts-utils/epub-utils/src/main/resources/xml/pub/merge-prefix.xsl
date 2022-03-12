<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

    <xsl:include href="epub3-vocab.xsl"/>

    <xsl:param name="implicit-input-prefixes" required="yes"/>
    <xsl:param name="implicit-output-prefixes" required="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@prefix|
                         @epub:prefix"/>

    <xsl:template match="/*" priority="1">
        <xsl:variable name="implicit.in" as="element(f:vocab)*">
            <xsl:for-each select="f:parse-prefix-decl($implicit-input-prefixes)">
                <xsl:copy>
                    <xsl:attribute name="implicit" select="'implicit'"/>
                    <xsl:sequence select="@*"/>
                </xsl:copy>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="all" as="element()*" select="f:all-prefix-decl(/,$implicit.in)"/>
        <!--
            #default means: if a prefix is implicit in at least one of the input attributes, it will
            be implicit in the output attribute
        -->
        <xsl:variable name="implicit.out" as="element(f:vocab)*"
                      select="if ($implicit-output-prefixes='#default')
                              then $implicit.in[some $v1 in . satisfies
                                                some $v2 in $all//f:vocab[@implicit] satisfies
                                                $v1/@prefix=$v2/@prefix and $v1/@uri=$v2/@uri]
                              else f:parse-prefix-decl($implicit-output-prefixes)"/>
        <xsl:variable name="unified" as="element(f:vocab)*" select="f:unified-prefix-decl($all//f:vocab,$implicit.in,$implicit.out)"/>
        <xsl:next-match>
            <xsl:with-param name="implicit.in" tunnel="yes" select="$implicit.in"/>
            <xsl:with-param name="implicit.out" tunnel="yes" select="$implicit.out"/>
            <xsl:with-param name="all" tunnel="yes" select="$all"/>
            <xsl:with-param name="unified" tunnel="yes" select="$unified"/>
        </xsl:next-match>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:param name="unified" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:copy>
            <xsl:if test="not(//opf:*)">
                <xsl:namespace name="epub" select="'http://www.idpf.org/2007/ops'"/>
            </xsl:if>
            <xsl:if test="exists($unified)">
                <xsl:variable name="prefix" as="xs:string"
                              select="string-join(
                                        for $vocab in $unified return concat($vocab/@prefix,': ',$vocab/@uri),
                                        ' ')"/>
                <xsl:choose>
                    <xsl:when test="//opf:*">
                        <xsl:attribute name="prefix" select="$prefix"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="epub:prefix" select="$prefix"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="meta/@property|
                         meta/@scheme|
                         link/@rel|
                         html:meta/@name">
        <xsl:param name="implicit.in" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:param name="implicit.out" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:param name="all" as="element()*" tunnel="yes" required="yes"/>
        <xsl:param name="unified" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:variable name="normalized" as="xs:string?"
                      select="f:expand-property(.,.,$implicit.in,$implicit.out,$all,$unified)/@name"/>
        <xsl:if test="exists($normalized)">
            <xsl:attribute name="{name(.)}" select="$normalized"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@epub:type">
        <xsl:param name="implicit.in" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:param name="implicit.out" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:param name="all" as="element()*" tunnel="yes" required="yes"/>
        <xsl:param name="unified" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:variable name="attr" select="."/>
        <xsl:variable name="normalized" as="xs:string*"
                      select="for $type in tokenize(., '\s+') return
                              f:expand-property($type,$attr,$implicit.in,$implicit.out,$all,$unified)/@name"/>
        <xsl:if test="exists($normalized)">
            <xsl:attribute name="{local-name(.)}" namespace="{namespace-uri(.)}"
                           select="string-join($normalized,' ')"/>
        </xsl:if>
    </xsl:template>

    <!--
        Returns a `f:property` element from a property-typeed attribute where:

        * @prefix contains the resolved, unified prefix for the property
        * @uri contains the resolved absolute URI of the property
        * @name contains the resolved name for the property, prefixed by the unified prefix
    -->
    <xsl:function name="f:expand-property" as="element(f:property)?">
        <xsl:param name="property" as="xs:string"/>
        <xsl:param name="context" as="attribute()?"/>
        <xsl:param name="implicit.in" as="element(f:vocab)*"/>
        <xsl:param name="implicit.out" as="element(f:vocab)*"/>
        <xsl:param name="all" as="element()*"/>
        <xsl:param name="unified" as="element(f:vocab)*"/>
        <xsl:variable name="prefix" select="substring-before($property,':')" as="xs:string"/>
        <xsl:variable name="reference" select="replace($property,'(.+:)','')" as="xs:string"/>
        <xsl:variable name="vocab" as="xs:string?"
                      select="($all[@id=generate-id(($context/ancestor::*[(@prefix|@epub:prefix) or not(parent::*)])[last()])]
                                   /f:vocab[@prefix=$prefix]/@uri,
                               if ($prefix='') then $vocab-package-uri else ()
                              )[1]"/>
        <xsl:if test="exists($vocab)">
            <xsl:variable name="unified-prefix" as="xs:string?"
                          select="(if ($vocab=$vocab-package-uri) then '' else (),
                                   $implicit.out[@uri=$vocab]/@prefix,
                                   $unified[@uri=$vocab]/@prefix
                                  )[1]"/>
            <f:property prefix="{$unified-prefix}"
                        uri="{concat($vocab,$reference)}"
                        name="{if ($unified-prefix) then concat($unified-prefix,':',$reference)  else $reference}"/>
        </xsl:if>
    </xsl:function>

    <!--
        Returns all the vocabs declared in the various @prefix attributes, as `f:vocab` elements
        grouped by elements having a `@id` attribute generated by `generate-id()`.

        Vocabs that are not used in `@epub:type`, `meta/@name`, `meta/@property`, `meta/@scheme` or
        `link/@rel` are discarded.
    -->
    <xsl:function name="f:all-prefix-decl" as="element()*">
        <xsl:param name="doc" as="document-node()?"/>
        <xsl:param name="implicit.in" as="element(f:vocab)*"/>
        <xsl:for-each select="$doc//*[(@prefix|@epub:prefix) or not(parent::*)]">
            <_ id="{generate-id(.)}">
                <xsl:variable name="elements-in-scope" as="element()*"
                              select="descendant-or-self::* except .//*[@prefix|@epub:prefix]/descendant-or-self::*"/>
                <xsl:variable name="used-prefixes" as="xs:string*"
                              select="distinct-values(
                                        for $prop in distinct-values(
                                          $elements-in-scope/self::meta/(@property|@scheme)|
                                          $elements-in-scope/self::link/@rel|
                                          $elements-in-scope/self::html:meta/@name|
                                          $elements-in-scope/@epub:type)
                                          [contains(.,':')]
                                        return substring-before($prop,':'))"/>
                <xsl:variable name="parsed-prefix-attr" as="element(f:vocab)*" select="f:parse-prefix-decl(@prefix|@epub:prefix)"/>
                <xsl:sequence select="for $prefix in $used-prefixes return
                                      ($parsed-prefix-attr[@prefix=$prefix],$implicit.in[@prefix=$prefix])[1]"/>
            </_>
        </xsl:for-each>
    </xsl:function>

    <!--
        Returns a sequence of `f:vocab` elements representing unified vocab declarations
        throughout the document passed as argument.

        * URIs from reserved prefixes (according to $implicit-output-prefixes) are discarded
        * vocabs are unified:
          - prefixes are unique
          - URIs are unique
          - if the list contains a reserved prefix (according to $implicit-input-prefixes), it is mapped to the right URI
          - if the list contains a URI from a reserved prefix, it is mapped to the right prefix
    -->
    <xsl:function name="f:unified-prefix-decl" as="element()*">
        <xsl:param name="all" as="element(f:vocab)*"/>
        <xsl:param name="implicit.in" as="element(f:vocab)*"/>
        <xsl:param name="implicit.out" as="element(f:vocab)*"/>
        <xsl:for-each-group select="f:merge-prefix-decl($all,$implicit.in)
                                    [not(@uri=($vocab-package-uri,
                                               $implicit.out/@uri))]"
                            group-by="@uri">
            <xsl:variable name="uri" select="current-grouping-key()"/>
            <xsl:sequence select="($implicit.in[@uri=$uri],current-group())[1]"/>
        </xsl:for-each-group>
    </xsl:function>

    <xsl:template match="/phony" xpath-default-namespace="">
        <!-- avoid SXXP0005 warning -->
        <xsl:next-match/>
    </xsl:template>

</xsl:stylesheet>
