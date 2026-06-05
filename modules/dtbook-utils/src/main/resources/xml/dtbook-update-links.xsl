<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                xmlns="http://www.daisy.org/z3986/2005/dtbook/"
                xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:param name="source-renamed" select="'false'"/>

    <xsl:key name="original-href" match="d:file[@original-href]" use="@original-href"/>
    <xsl:key name="original-id" match="d:anchor" use="(@original-id,@id)[1]"/>
    <xsl:key name="href" match="d:file[not(@original-href)]" use="@href"/>

    <!--
        A fileset defines the relocation of resources.
        We know that it has been previously normalized.
    -->
    <xsl:variable name="mapping" as="element(d:fileset)">
        <xsl:variable name="mapping" as="document-node(element(d:fileset))">
            <xsl:document>
                <xsl:apply-templates mode="absolute-hrefs" select="collection()[/d:fileset][1]"/>
            </xsl:document>
        </xsl:variable>
        <xsl:sequence select="$mapping/*"/>
    </xsl:variable>

    <xsl:variable name="original-doc-base" as="xs:string"
                  select="if (not($source-renamed='true'))
                          then base-uri(/)
                          else ($mapping/d:file[resolve-uri(@href,base-uri(.))=base-uri(current())][1]/@original-href,
                                base-uri(/))[1]"/>
    <xsl:variable name="doc-base" as="xs:string"
                  select="if ($source-renamed='true')
                          then base-uri(/)
                          else ($mapping/d:file[@original-href]
                                               [resolve-uri(@original-href,base-uri(.))=base-uri(current())][1]/@href,
                                base-uri(/))[1]"/>

    <xsl:template match="a/@href           |
                         img/@src          |
                         m:math/@altimg    ">
        <xsl:variable name="uri" as="xs:string" select="pf:normalize-uri(.)"/>
        <xsl:variable name="uri" as="xs:string*" select="pf:tokenize-uri($uri)"/>
        <xsl:variable name="fragment" as="xs:string?" select="$uri[5]"/>
        <xsl:variable name="file" as="xs:string" select="pf:recompose-uri($uri[position()&lt;5])"/>
        <xsl:variable name="resolved-file" as="xs:anyURI" select="resolve-uri($file,$original-doc-base)"/>
        <xsl:variable name="new-file" as="element(d:file)*" select="key('original-href',$resolved-file,$mapping)"/>
        <xsl:variable name="new-file" as="element(d:file)?" select="(if (exists($fragment))
                                                                       then $new-file[exists(key('original-id',$fragment,.))]
                                                                       else (),
                                                                     $new-file)[1]"/>
        <xsl:variable name="new-fragment" as="xs:string?" select="if (exists($fragment))
                                                                  then if (exists($new-file))
                                                                       then key('original-id',$fragment,$new-file)/@id
                                                                       else for $f in key('href',$resolved-file,$mapping)[1]
                                                                            return key('original-id',$fragment,$f)/@id
                                                                  else ()"/>
        <xsl:variable name="new-file" as="xs:string?" select="$new-file/@href"/>
        <xsl:choose>
            <xsl:when test="exists($new-file)">
                <xsl:variable name="new-uri" select="string-join(($new-file,($new-fragment,$fragment)[1]),'#')"/>
                <xsl:attribute name="{local-name(.)}" namespace="{namespace-uri(.)}"
                               select="if (starts-with($new-uri,concat($doc-base,'#')))
                                       then concat('#',substring-after($new-uri,'#'))
                                       else pf:relativize-uri($new-uri,$doc-base)"/>
            </xsl:when>
            <xsl:when test="$doc-base!=$original-doc-base and pf:is-relative(.)">
                <xsl:variable name="new-uri" select="string-join(($resolved-file,($new-fragment,$fragment)[1]),'#')"/>
                <xsl:attribute name="{local-name(.)}" namespace="{namespace-uri(.)}"
                               select="if (starts-with($new-uri,concat($doc-base,'#')))
                                       then concat('#',substring-after($new-uri,'#'))
                                       else pf:relativize-uri($new-uri,$doc-base)"/>
            </xsl:when>
            <xsl:when test="exists($new-fragment)">
                <xsl:attribute name="{local-name(.)}" namespace="{namespace-uri(.)}"
                               select="string-join(($file,$new-fragment),'#')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="absolute-hrefs"
                  match="d:file/@href|
                         d:file/@original-href">
        <xsl:attribute name="{name()}" select="resolve-uri(.,base-uri(..))"/>
    </xsl:template>

    <xsl:template mode="#default absolute-hrefs" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
