<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:f="http://www.daisy.org/pipeline/modules/nordic-epub3-dtbook-migrator/dtbook-to-epub3.xsl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">

    <xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/numeral-conversion.xsl"/>
    <!--<xsl:import href="../../../../test/xspec/mock/numeral-conversion.xsl"/>-->
    <xsl:import href="epub3-vocab.xsl"/>

    <xsl:output indent="yes" exclude-result-prefixes="#all"/>

    <xsl:param name="generate-ids" select="true()"/>
    <xsl:param name="supported-list-types" select="('ol','ul','pl')"/>
    <xsl:param name="parse-list-marker" select="true()"/>
    <xsl:param name="add-tbody" select="true()"/>

    <xsl:template match="comment()">
        <xsl:copy-of select="." exclude-result-prefixes="#all"/>
    </xsl:template>

    <xsl:template match="text()">
        <xsl:copy-of select="." exclude-result-prefixes="#all"/>
    </xsl:template>

    <xsl:template match="*">
        <xsl:comment select="concat('No template for element: ',name())"/>
    </xsl:template>

    <xsl:template name="f:coreattrs">
        <xsl:param name="classes" select="()" tunnel="yes"/>
        <xsl:param name="types" select="()" tunnel="yes"/>
        <xsl:param name="except" select="()" tunnel="yes"/>
        <xsl:param name="all-ids" select="()" tunnel="yes"/>
        <xsl:variable name="is-first-level" select="boolean((self::dtbook:level or self::dtbook:level1) and (parent::dtbook:frontmatter or parent::dtbook:bodymatter or parent::dtbook:rearmatter))"/>
        <xsl:if test="$is-first-level">
            <!--
                the frontmatter/bodymatter/rearmatter does not have corresponding elements in HTML and is removed;
                try preserving the attributes on the closest sectioning element(s) when possible
            -->
            <xsl:copy-of select="parent::*/(@title|@xml:space)[not(name()=$except)]" exclude-result-prefixes="#all"/>
            <xsl:if test="not(preceding-sibling::dtbook:level or preceding-sibling::dtbook:level1)">
                <xsl:copy-of select="parent::*/@id[not(name()=$except)]" exclude-result-prefixes="#all"/>
            </xsl:if>
        </xsl:if>
        <xsl:copy-of select="(@id|@title|@xml:space)[not(name()=$except)]" exclude-result-prefixes="#all"/>
        <xsl:if
            test="$generate-ids and not(@id) and ($types[.='epigraph'] or not(local-name()=('book','span','p','div','tr','th','td','link','br','line','linenum','title','author','em','strong','dfn','kbd','code','samp','cite','abbr','acronym','sub','sup','bdo','sent','w','pagenum','docauthor','bridgehead','dd','lic','thead','tfoot','tbody','colgroup','col') and namespace-uri()='http://www.daisy.org/z3986/2005/dtbook/'))">
            <xsl:attribute name="id" select="f:generate-pretty-id(.,$all-ids)"/>
        </xsl:if>
        <xsl:call-template name="f:classes-and-types">
            <xsl:with-param name="classes" select="(if ($is-first-level) then tokenize(parent::*/@class,'\s+') else (), $classes)" tunnel="yes"/>
            <xsl:with-param name="types" select="$types" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="f:i18n">
        <xsl:param name="except" select="()" tunnel="yes"/>
        <xsl:variable name="is-first-level" select="boolean((self::dtbook:level or self::dtbook:level1) and (parent::dtbook:frontmatter or parent::dtbook:bodymatter or parent::dtbook:rearmatter))"/>
        <xsl:if test="$is-first-level">
            <!--
                the frontmatter/bodymatter/rearmatter does not have corresponding elements in HTML and is removed;
                try preserving the attributes on the closest sectioning element(s) when possible
            -->
            <xsl:if test="../@xml:lang and not($except=('lang','xml:lang'))">
                <xsl:attribute name="lang" select="../@xml:lang"/>
                <xsl:attribute name="xml:lang" select="../@xml:lang"/>
            </xsl:if>
            <xsl:copy-of select="../@dir[not(name()=$except)]" exclude-result-prefixes="#all"/>
        </xsl:if>
        <xsl:if test="@xml:lang and not($except=('lang','xml:lang'))">
            <xsl:attribute name="lang" select="@xml:lang"/>
            <xsl:attribute name="xml:lang" select="@xml:lang"/>
        </xsl:if>
        <xsl:copy-of select="@dir[not(name()=$except)]" exclude-result-prefixes="#all"/>
    </xsl:template>

    <xsl:template name="f:classes-and-types">
        <xsl:param name="classes" select="()" tunnel="yes"/>
        <xsl:param name="types" select="()" tunnel="yes"/>
        <xsl:param name="except" select="()" tunnel="yes"/>
        <xsl:param name="except-classes" select="()" tunnel="yes"/>
        <xsl:param name="except-types" select="()" tunnel="yes"/>
        <xsl:variable name="showin" select="for $s in (@showin) return concat('showin-',$s)"/>
        <xsl:variable name="old-classes" select="tokenize(@class,'\s+')"/>

        <xsl:variable name="epub-types">
            <xsl:for-each select="$old-classes">
                <xsl:choose>
                    <xsl:when test=".='jacketcopy'">
                        <xsl:sequence select="'cover'"/>
                    </xsl:when>
                    <xsl:when test=".='endnote'">
                        <xsl:sequence select="'rearnote'"/>
                    </xsl:when>
                    <xsl:when test=".=$vocab-default">
                        <xsl:sequence select="."/>
                    </xsl:when>
                    <xsl:when test=".=$vocab-z3998">
                        <xsl:sequence select="concat('z3998:',.)"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:for-each>
            <xsl:value-of select="''"/>
        </xsl:variable>
        <xsl:variable name="epub-types" select="($types, $epub-types)[not(.='') and not(.=$except-types)]"/>
        <xsl:variable name="epub-types" select="($epub-types, if ($epub-types='bodymatter' and count($epub-types)=1) then 'chapter' else ())"/>
        <xsl:variable name="epub-types"
            select="($epub-types, if ((self::dtbook:level2 or self::dtbook:level[count(ancestor::level)=1]) and (ancestor::dtbook:level1|ancestor::dtbook:level[not(ancestor::dtbook:level)])/tokenize(@class,'\s+')='part' and count($epub-types)=0) then 'chapter' else ())"/>

        <xsl:variable name="classes" select="($classes, $old-classes[not(.=($vocab-default,$vocab-z3998,'jacketcopy','endnote'))], $showin)[not(.='') and not(.=$except-classes)]"/>

        <xsl:if test="count($classes) and not('_class'=$except)">
            <xsl:attribute name="class" select="string-join(distinct-values($classes),' ')"/>
        </xsl:if>
        <xsl:if test="count($epub-types) and not('_epub:type'=$except)">
            <xsl:attribute name="epub:type" select="string-join(distinct-values($epub-types),' ')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="f:attrs">
        <xsl:call-template name="f:coreattrs"/>
        <xsl:call-template name="f:i18n"/>
        <!-- ignore @smilref -->
        <!-- @showin handled by coreattrs -->
    </xsl:template>

    <xsl:template name="f:attrsrqd">
        <xsl:call-template name="f:coreattrs"/>
        <xsl:call-template name="f:i18n"/>
        <!-- ignore @smilref -->
        <!-- @showin handled by classes-and-types -->
    </xsl:template>

    <xsl:template match="dtbook:dtbook">
        <xsl:variable name="all-ids" select=".//@id"/>
        <html>
            <xsl:namespace name="epub" select="'http://www.idpf.org/2007/ops'"/>
            <xsl:copy-of select="namespace::*[not(.='http://www.daisy.org/z3986/2005/dtbook/')]" exclude-result-prefixes="#all"/>
            <xsl:call-template name="f:attlist.dtbook">
                <xsl:with-param name="all-ids" select="$all-ids" tunnel="yes"/>
            </xsl:call-template>
            <xsl:apply-templates select="node()">
                <xsl:with-param name="all-ids" select="$all-ids" tunnel="yes"/>
            </xsl:apply-templates>
        </html>
    </xsl:template>

    <xsl:template name="f:attlist.dtbook">
        <!-- ignore @version -->
        <xsl:call-template name="f:i18n"/>
    </xsl:template>

    <xsl:template name="f:headmisc">
        <xsl:apply-templates select="node()[not((self::*, following-sibling::*[1])[1][self::dtbook:meta[starts-with(@name,'dtb:')]])]"/>
    </xsl:template>

    <xsl:template match="dtbook:head">
        <head>
            <xsl:copy-of select="namespace::*[not(.='http://www.daisy.org/z3986/2005/dtbook/')]" exclude-result-prefixes="#all"/>
            <xsl:call-template name="f:attlist.head"/>
            <meta charset="UTF-8"/>
            <title>
                <xsl:value-of select="string((dtbook:meta[matches(@name,'dc:title','i')])[1]/@content)"/>
            </title>
            <meta name="dc:identifier" content="{string((dtbook:meta[matches(@name,'dtb:uid')])[1]/@content)}"/>
            <meta name="viewport" content="width=device-width"/>
            <meta name="nordic:guidelines" content="2015-1"/>
            <xsl:for-each select="dtbook:meta[starts-with(@name,'track:') and not(@name='track:Guidelines')]">
                <meta name="nordic:{lower-case(substring-after(@name,'track:'))}" content="{@content}"/>
            </xsl:for-each>
            <xsl:if test="not(dtbook:meta[@name='dc:Source'])">
                <!-- Set ISBN to 0 to avoid validation errors -->
                <meta name="dc:source" content="urn:isbn:0"/>
            </xsl:if>
            <xsl:call-template name="f:headmisc"/>
            <style type="text/css" xml:space="preserve"><![CDATA[
                .initialism{
                    -epub-speak-as:spell-out;
                }
                .list-style-type-none{
                    list-style-type:none;
                }
                table[class ^= "table-rules-"],
                table[class *= " table-rules-"]{
                    border-width:thin;
                    border-style:hidden;
                }
                table[class ^= "table-rules-"]:not(.table-rules-none),
                table[class *= " table-rules-"]:not(.table-rules-none){
                    border-collapse:collapse;
                }
                table[class ^= "table-rules-"] td,
                table[class *= " table-rules-"] td{
                    border-width:thin;
                    border-style:none;
                }
                table[class ^= "table-rules-"] th,
                table[class *= " table-rules-"] th{
                    border-width:thin;
                    border-style:none;
                }
                table.table-rules-none td,
                table.table-rules-none th{
                    border-width:thin;
                    border-style:hidden;
                }
                table.table-rules-all td,
                table.table-rules-all th{
                    border-width:thin;
                    border-style:solid;
                }
                table.table-rules-cols td,
                table.table-rules-cols th{
                    border-left-width:thin;
                    border-right-width:thin;
                    border-left-style:solid;
                    border-right-style:solid;
                }
                table.table-rules-rows tr{
                    border-top-width:thin;
                    border-bottom-width:thin;
                    border-top-style:solid;
                    border-bottom-style:solid;
                }
                table.table-rules-groups colgroup{
                    border-left-width:thin;
                    border-right-width:thin;
                    border-left-style:solid;
                    border-right-style:solid;
                }
                table.table-rules-groups tfoot,
                table.table-rules-groups thead,
                table.table-rules-groups tbody{
                    border-top-width:thin;
                    border-bottom-width:thin;
                    border-top-style:solid;
                    border-bottom-style:solid;
                }
                table[class ^= "table-frame-"],
                table[class *= " table-frame-"]{
                    border:thin hidden;
                }
                table.table-frame-void{
                    border-style:hidden;
                }
                table.table-frame-above{
                    border-style:outset hidden hidden hidden;
                }
                table.table-frame-below{
                    border-style:hidden hidden outset hidden;
                }
                table.table-frame-lhs{
                    border-style:hidden hidden hidden outset;
                }
                table.table-frame-rhs{
                    border-style:hidden outset hidden hidden;
                }
                table.table-frame-hsides{
                    border-style:outset hidden;
                }
                table.table-frame-vsides{
                    border-style:hidden outset;
                }
                table.table-frame-box{
                    border-style:outset;
                }
                table.table-frame-border{
                    border-style:outset;
                }]]></style>
            <xsl:if test="@profile">
                <link rel="profile" href="{@profile}"/>
            </xsl:if>
        </head>
    </xsl:template>

    <xsl:template name="f:attlist.head">
        <xsl:call-template name="f:i18n"/>
        <!-- @profile handled by main head element test -->
    </xsl:template>

    <xsl:template match="dtbook:link">
        <link>
            <xsl:call-template name="f:attlist.link"/>
        </link>
    </xsl:template>

    <xsl:template name="f:attlist.link">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@href|@hreflang|@type|@rel|@media" exclude-result-prefixes="#all"/>
        <!-- @charset and @rev are dropped -->
    </xsl:template>

    <xsl:template match="dtbook:meta">
        <xsl:choose>
            <xsl:when test="starts-with(@name,'dtb:')"/>
            <xsl:when test="matches(@name,'dc:title','i') or matches(@name,'dc:identifier','i') or matches(@name,'dc:format','i') or starts-with(@name,'track:')"/>
            <xsl:otherwise>
                <meta>
                    <xsl:call-template name="f:attlist.meta"/>
                </meta>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:attlist.meta">
        <xsl:call-template name="f:i18n"/>
        <xsl:copy-of select="@content|@http-equiv" exclude-result-prefixes="#all"/>
        <xsl:choose>
            <xsl:when test="matches(@name,'dc:.*','i')">
                <xsl:attribute name="name" select="lower-case(@name)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="name" select="@name"/>
            </xsl:otherwise>
        </xsl:choose>
        <!-- @scheme is dropped -->
    </xsl:template>

    <xsl:template match="dtbook:book">
        <body>
            <xsl:copy-of select="namespace::*[not(.='http://www.daisy.org/z3986/2005/dtbook/')]" exclude-result-prefixes="#all"/>
            <xsl:call-template name="f:attlist.book"/>
            <xsl:apply-templates select="node()"/>
        </body>
    </xsl:template>

    <xsl:template name="f:attlist.book">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except" select="'id'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="f:cover">
        <section>
            <xsl:call-template name="f:attrs">
                <xsl:with-param name="types" select="'cover'" tunnel="yes"/>
            </xsl:call-template>
            <xsl:apply-templates select="node()"/>
        </section>
    </xsl:template>

    <xsl:template name="f:titlepage">
        <section>
            <xsl:call-template name="f:attlist.frontmatter">
                <xsl:with-param name="types" select="'titlepage'" tunnel="yes"/>
            </xsl:call-template>
            <xsl:apply-templates select="node()"/>
        </section>
    </xsl:template>

    <xsl:template match="dtbook:frontmatter">

        <xsl:if test="dtbook:doctitle | dtbook:covertitle | dtbook:docauthor">
            <header>
                <xsl:apply-templates select="dtbook:doctitle | dtbook:covertitle | dtbook:docauthor"/>
            </header>
        </xsl:if>

        <xsl:for-each select="dtbook:level1 | dtbook:level">
            <xsl:choose>

                <!-- cover -->
                <xsl:when test="f:classes(.)=('cover','jacketcopy')">
                    <xsl:apply-templates
                        select="if (not(preceding-sibling::*)) then preceding-sibling::comment() else (preceding-sibling::comment() intersect preceding-sibling::*[1]/following-sibling::comment())"/>

                    <xsl:call-template name="f:cover"/>
                </xsl:when>

                <!-- title page -->
                <xsl:when test="f:classes(.)='titlepage'">
                    <xsl:apply-templates
                        select="if (not(preceding-sibling::*)) then preceding-sibling::comment() else (preceding-sibling::comment() intersect preceding-sibling::*[1]/following-sibling::comment())"/>

                    <xsl:call-template name="f:titlepage"/>
                </xsl:when>

                <!-- the rest of the frontmatter -->
                <xsl:otherwise>
                    <xsl:apply-templates
                        select="if (not(preceding-sibling::*)) then preceding-sibling::comment() else (preceding-sibling::comment() intersect preceding-sibling::*[1]/following-sibling::comment())"/>
                    <xsl:apply-templates select="."/>
                </xsl:otherwise>

            </xsl:choose>
        </xsl:for-each>

        <xsl:apply-templates select="*[last()]/following-sibling::comment()"/>

    </xsl:template>

    <xsl:template name="f:attlist.frontmatter">
        <xsl:param name="types" tunnel="yes"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="('frontmatter',$types)" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:bodymatter">
        <!-- all attributes on bodymatter will be lost -->
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template name="f:attlist.bodymatter">
        <xsl:param name="types" tunnel="yes"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="('bodymatter',$types)" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:rearmatter">
        <!-- all attributes on rearmatter will be lost -->
        <xsl:apply-templates select="node()"/>
    </xsl:template>

    <xsl:template name="f:attlist.rearmatter">
        <xsl:param name="types" tunnel="yes"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="('backmatter',$types)" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:level | dtbook:level1 | dtbook:level2 | dtbook:level3 | dtbook:level4 | dtbook:level5 | dtbook:level6">
        <xsl:element name="{if (f:classes(.)='article') then 'article' else 'section'}">
            <xsl:call-template name="f:attlist.level"/>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="f:attlist.level">
        <xsl:variable name="types"
            select="if (ancestor::*[self::dtbook:level or self::dtbook:level1 or self::dtbook:level2 or self::dtbook:level3 or self::dtbook:level4 or self::dtbook:level5 or self::dtbook:level6]) then () else if (ancestor::dtbook:frontmatter) then 'frontmatter' else if (ancestor::dtbook:bodymatter) then 'bodymatter' else 'backmatter'"/>
        <xsl:variable name="types"
            select="($types, if (dtbook:note[not(//dtbook:table//dtbook:noteref/substring-after(@idref,'#')=@id)]) then if (ancestor::dtbook:bodymatter) then 'rearnotes' else if (ancestor::dtbook:rearmatter) then 'footnotes' else () else ())"/>
        <xsl:variable name="types" select="($types, if (dtbook:list[f:classes(.)='toc']) then 'toc' else ())"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="$types" tunnel="yes"/>
        </xsl:call-template>
        <!-- @depth is removed, it is implicit anyway -->
    </xsl:template>

    <xsl:template match="dtbook:br">
        <br>
            <xsl:call-template name="f:attlist.br"/>
        </br>
    </xsl:template>

    <xsl:template name="f:attlist.br">
        <xsl:call-template name="f:coreattrs"/>
    </xsl:template>

    <xsl:template match="dtbook:line">
        <p>
            <xsl:call-template name="f:attlist.line"/>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template name="f:attlist.line">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'line'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:linenum">
        <span>
            <xsl:call-template name="f:attlist.linenum"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="f:attlist.linenum">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'linenum'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:address">
        <address>
            <xsl:call-template name="f:attlist.address"/>
            <xsl:apply-templates select="node()"/>
        </address>
    </xsl:template>

    <xsl:template name="f:attlist.address">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:div">
        <div>
            <xsl:call-template name="f:attlist.div"/>
            <xsl:apply-templates select="node()"/>
        </div>
    </xsl:template>

    <xsl:template name="f:attlist.div">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:title">
        <strong>
            <xsl:call-template name="f:attlist.title"/>
            <xsl:apply-templates select="node()"/>
        </strong>
    </xsl:template>

    <xsl:template name="f:attlist.title">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'title'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:author">
        <xsl:choose>
            <xsl:when test="parent::dtbook:level1[tokenize(@class,'\s+')='titlepage']">
                <p>
                    <xsl:call-template name="f:attlist.author"/>
                    <xsl:apply-templates select="node()"/>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <span>
                    <xsl:call-template name="f:attlist.author"/>
                    <xsl:apply-templates select="node()"/>
                </span>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:attlist.author">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'z3998:author'" tunnel="yes"/>
            <xsl:with-param name="classes" select="if (parent::dtbook:level1[tokenize(@class,'\s+')='titlepage']) then 'docauthor' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:prodnote | dtbook:div[f:classes(.) = ('prodnote','production')]">
      <xsl:element name="{if (parent::*[tokenize(@class,'\s+')=('cover','jacketcopy')]) then 'section'
                          else 'aside'}">
        <xsl:call-template name="f:attlist.prodnote"/>
        <xsl:apply-templates select="node() | text()"/>
      </xsl:element>
    </xsl:template>

    <!-- Render inline prodnotes as spans -->
    <xsl:template match="dtbook:prodnote[f:is-inline(./parent::dtbook:*) and not(./parent::dtbook:imggroup)]">
      <span>
        <xsl:call-template name="f:attlist.prodnote"/>
        <xsl:apply-templates select="node() | text()"/>
      </span>
    </xsl:template>

    <xsl:template name="f:attlist.prodnote">
        <xsl:param name="all-ids" select="()" tunnel="yes"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="if (not(parent::*[tokenize(@class,'\s+')=('cover','jacketcopy')])) then 'z3998:production' else ()" tunnel="yes"/>
            <xsl:with-param name="classes" select="(if (not(parent::*/tokenize(@class,'\s+')=('cover','jacketcopy'))) then 'prodnote' else (), if (@render) then concat('render-',@render) else ())"
                tunnel="yes"/>
            <xsl:with-param name="all-ids" select="$all-ids" tunnel="yes"/>
        </xsl:call-template>
        <!-- @imgref is dropped, the relationship is preserved in the corresponding img/@longdesc -->
        <xsl:if test="not(@id) and (
                        $generate-ids or (
                        some $ref in tokenize(@imgref,'\s+')[not(.='')] satisfies //dtbook:img[@id=$ref]))">
            <xsl:attribute name="id" select="f:generate-pretty-id(.,$all-ids)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:sidebar">
        <xsl:choose>
            <xsl:when test="@render='required'">
                <figure>
                    <xsl:call-template name="f:attlist.sidebar"/>
                    <xsl:apply-templates select="node()"/>
                </figure>
            </xsl:when>
            <xsl:otherwise>
                <aside>
                    <xsl:call-template name="f:attlist.sidebar"/>
                    <xsl:apply-templates select="node()"/>
                </aside>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:attlist.sidebar">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'sidebar'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'sidebar'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:note">
        <xsl:variable name="type"
            select="if (count(parent::*[matches(local-name(),'^level\d?$')])) then if (count(ancestor::dtbook:bodymatter)) then 'rearnote' else if (count(ancestor::dtbook:rearmatter)) then 'footnote' else 'note' else 'note'"/>
        <xsl:choose>
            <xsl:when test="$type='note'">
                <aside>
                    <xsl:call-template name="f:attlist.note">
                        <xsl:with-param name="types" select="$type" tunnel="yes"/>
                    </xsl:call-template>
                    <xsl:apply-templates select="node()"/>
                </aside>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="not(preceding-sibling::*[not(self::dtbook:pagenum)][1] intersect preceding-sibling::dtbook:note)">
                    <!-- first note in sequence of notes; handle all the notes in the sequence here -->
                    <ol epub:type="{$type}s">
                        <xsl:for-each
                            select="(., if (not(count(following-sibling::*[not(self::dtbook:note) and not(self::dtbook:pagenum)]))) then following-sibling::dtbook:note else (following-sibling::*[not(self::dtbook:note) and not(self::dtbook:pagenum)][1]/preceding-sibling::dtbook:note intersect following-sibling::dtbook:note))">
                            <li>
                                <xsl:call-template name="f:attlist.note">
                                    <xsl:with-param name="types" select="$type" tunnel="yes"/>
                                </xsl:call-template>
                                <xsl:apply-templates select="node()"/>
                                <xsl:apply-templates
                                    select="if (count(following-sibling::*[not(self::dtbook:pagenum)][1] intersect following-sibling::dtbook:note)) then (following-sibling::dtbook:pagenum intersect following-sibling::dtbook:note[1]/preceding-sibling::dtbook:pagenum) else ()">
                                    <xsl:with-param name="pagenum.parent" select="." tunnel="yes"/>
                                </xsl:apply-templates>
                            </li>
                        </xsl:for-each>
                    </ol>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:attlist.note">
        <xsl:call-template name="f:attrsrqd">
            <xsl:with-param name="classes" select="'notebody'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:annotation">
        <aside>
            <xsl:call-template name="f:attlist.annotation"/>
            <xsl:apply-templates select="node()"/>
        </aside>
    </xsl:template>

    <xsl:template name="f:attlist.annotation">
        <xsl:call-template name="f:attrsrqd">
            <xsl:with-param name="types" select="'annotation'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'annotation'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:epigraph | dtbook:p[tokenize(@class,'\s+') = 'epigraph']">
        <xsl:choose>
            <xsl:when test="exists(.//dtbook:h1 | .//dtbook:h2 | .//dtbook:h3 | .//dtbook:h4 | .//dtbook:h5 | .//dtbook:h6)">
                <aside>
                    <xsl:call-template name="f:attlist.epigraph"/>
                    <xsl:apply-templates select="node()"/>
                </aside>
            </xsl:when>
            <xsl:when test="exists(ancestor::dtbook:epigraph | ancestor::dtbook:p[tokenize(@class,'\s+') = 'epigraph'])">
                <p>
                    <xsl:call-template name="f:attlist.epigraph"/>
                    <xsl:apply-templates select="node()"/>
                </p>
            </xsl:when>
            <xsl:otherwise>
                <blockquote>
                    <xsl:call-template name="f:attlist.epigraph"/>
                    <xsl:apply-templates select="node()"/>
                </blockquote>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:attlist.epigraph">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'epigraph'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'epigraph'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:byline">
        <p>
            <xsl:call-template name="f:attlist.byline"/>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template name="f:attlist.byline">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'byline'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:dateline">
        <span>
            <xsl:call-template name="f:attlist.dateline"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="f:attlist.dateline">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'dateline'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:linegroup">
        <xsl:element name="{if (dtbook:hd) then 'section' else 'div'}">
            <xsl:call-template name="f:attlist.linegroup"/>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="f:attlist.linegroup">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="if (parent::dtbook:poem) then 'z3998:verse' else ()" tunnel="yes"/>
            <xsl:with-param name="classes" select="'linegroup'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:poem">
        <xsl:element name="{if (dtbook:hd) then 'section' else 'div'}">
            <xsl:call-template name="f:attlist.poem"/>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="f:attlist.poem">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'z3998:poem'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'poem'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:a">
        <a>
            <xsl:call-template name="f:attlist.a"/>
            <xsl:apply-templates select="node()"/>
        </a>
    </xsl:template>

    <xsl:template name="f:attlist.a">
        <xsl:variable name="target" as="xs:string?">
            <xsl:choose>
                <xsl:when test="f:classes(.)[matches(.,'^target--')]">
                    <xsl:sequence select="replace((f:classes(.)[matches(.,'^target--')])[1],'^target--','_')"/>
                </xsl:when>
                <xsl:when test="f:classes(.)[matches(.,'^target-')]">
                    <xsl:sequence select="replace((f:classes(.)[matches(.,'^target-')])[1],'^target-','')"/>
                </xsl:when>
                <xsl:when test="@external='true'">
                    <xsl:sequence select="'_blank'"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="(if (@external) then concat('external-',@external)
                                                    else if ($target='_blank' or matches(@href,'^(\w+:|/)')) then 'external-false'
                                                    else (),
                                                    if (@rev) then concat('rev-',@rev) else ())" tunnel="yes"/>
            <xsl:with-param name="exclude-classes" select="for $target in (f:classes(.)[matches(.,'^target-')]) return $target" tunnel="yes"/>
        </xsl:call-template>
        <xsl:copy-of select="@type|@href|@hreflang|@rel|@accesskey|@tabindex" exclude-result-prefixes="#all"/>
        <!-- @rev is dropped since it's not supported in HTML5 -->
        <xsl:if test="$target">
            <xsl:attribute name="target" select="$target"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:em">
        <em>
            <xsl:call-template name="f:attlist.em"/>
            <xsl:apply-templates select="node()"/>
        </em>
    </xsl:template>

    <xsl:template name="f:attlist.em">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:strong">
        <strong>
            <xsl:call-template name="f:attlist.strong"/>
            <xsl:apply-templates select="node()"/>
        </strong>
    </xsl:template>

    <xsl:template name="f:attlist.strong">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <!-- TODO: allow dtbook:span[f:classes(.)='definition'] -->
    <xsl:template match="dtbook:dfn">
        <dfn>
            <xsl:call-template name="f:attlist.dfn"/>
            <xsl:apply-templates select="node()"/>
        </dfn>
    </xsl:template>

    <xsl:template name="f:attlist.dfn">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <!-- TODO: allow dtbook:span[f:classes(.)='keyboard'] -->
    <xsl:template match="dtbook:kbd">
        <kbd>
            <xsl:call-template name="f:attlist.kbd"/>
            <xsl:apply-templates select="node()"/>
        </kbd>
    </xsl:template>

    <xsl:template name="f:attlist.kbd">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:code">
        <code>
            <xsl:call-template name="f:attlist.code"/>
            <xsl:apply-templates select="node()"/>
        </code>
    </xsl:template>

    <xsl:template name="f:attlist.code">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="except" select="'xml:space'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:call-template name="f:i18n"/>
        <!-- ignore @smilref -->
        <!-- @showin handled by "attrs" -->
    </xsl:template>

    <!-- TODO: allow dtbook:span[f:classes(.)='example'] -->
    <xsl:template match="dtbook:samp">
        <samp>
            <xsl:call-template name="f:attlist.samp"/>
            <xsl:apply-templates select="node()"/>
        </samp>
    </xsl:template>

    <xsl:template name="f:attlist.samp">
        <xsl:call-template name="f:attrs"/>
        <xsl:call-template name="f:i18n"/>
        <!-- ignore @smilref -->
        <!-- @showin handled by "attrs" -->
    </xsl:template>

    <!-- TODO: allow dtbook:span[f:classes(.)='cite'] -->
    <xsl:template match="dtbook:cite">
        <cite>
            <xsl:call-template name="f:attlist.cite"/>
            <xsl:apply-templates select="node()"/>
        </cite>
    </xsl:template>

    <xsl:template name="f:attlist.cite">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:abbr | dtbook:span[@class='truncation']">
        <abbr>
            <xsl:call-template name="f:attlist.abbr"/>
            <xsl:apply-templates select="node()"/>
        </abbr>
    </xsl:template>

    <xsl:template name="f:attlist.abbr">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:acronym">
        <abbr>
            <xsl:call-template name="f:attlist.acronym"/>
            <xsl:apply-templates select="node()"/>
        </abbr>
    </xsl:template>

    <xsl:template name="f:attlist.acronym">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="if (@pronounce='no') then 'z3998:initialism' else 'z3998:acronym'" tunnel="yes"/>
            <xsl:with-param name="classes" select="if (@pronounce='no') then 'initialism' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:sub">
        <sub>
            <xsl:call-template name="f:attlist.sub"/>
            <xsl:apply-templates select="node()"/>
        </sub>
    </xsl:template>

    <xsl:template name="f:attlist.sub">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:sup">
        <sup>
            <xsl:call-template name="f:attlist.sup"/>
            <xsl:apply-templates select="node()"/>
        </sup>
    </xsl:template>

    <xsl:template name="f:attlist.sup">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:span">
        <span>
            <xsl:call-template name="f:attlist.span"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="f:attlist.span">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:bdo">
        <bdo>
            <xsl:call-template name="f:attlist.bdo"/>
            <xsl:apply-templates select="node()"/>
        </bdo>
    </xsl:template>

    <xsl:template name="f:attlist.bdo">
        <xsl:call-template name="f:coreattrs"/>
        <xsl:call-template name="f:i18n"/>
        <!-- ignore @smilref -->
        <!-- @showin handled by "coreattrs" -->
    </xsl:template>

    <xsl:template match="dtbook:sent">
        <span>
            <xsl:call-template name="f:attlist.sent"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="f:attlist.sent">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'z3998:sentence'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'sentence'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:w">
        <span>
            <xsl:call-template name="f:attlist.w"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="f:attlist.w">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'z3998:word'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'word'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:pagenum">
        <xsl:param name="pagenum.parent" tunnel="yes" select="parent::*"/>
        <xsl:variable name="pagenum.parent" select="if (count($pagenum.parent/descendant-or-self::* intersect parent::*/ancestor-or-self::*) &gt;= 3) then parent::* else $pagenum.parent"/>

        <xsl:if
            test="not(count($pagenum.parent[matches(local-name(),'^level\d?$')]) = 1 and not(ancestor::dtbook:frontmatter) and count(following-sibling::*[not(self::dtbook:pagenum)][1][self::dtbook:note[not(//dtbook:table//dtbook:noteref/substring-after(@idref,'#')=@id)]]) = 1 and count(preceding-sibling::*[not(self::dtbook:pagenum)][1][self::dtbook:note]) = 1)">
            <!-- xsl:if avoids inserting pagenum betwen li elements when rearnotes or footnotes are made into lists. -->

            <xsl:element name="{if (f:is-inline($pagenum.parent) and not(parent::dtbook:imggroup)) then 'span' else 'div'}">
                <xsl:call-template name="f:attlist.pagenum"/>
                <xsl:if test="normalize-space(.)">
                    <xsl:attribute name="title" select="normalize-space(.)"/>
                    <!--
                    NOTE: the title attribute is overwritten with the contents of the pagenum,
                    so any pre-existing @title content is lost.
                -->
                </xsl:if>
            </xsl:element>
        </xsl:if>
    </xsl:template>

    <xsl:template name="f:attlist.pagenum">
        <xsl:call-template name="f:attrsrqd">
            <xsl:with-param name="types" select="'pagebreak'" tunnel="yes"/>
            <xsl:with-param name="classes" select="if (@page) then concat('page-',@page) else if (normalize-space(.)='') then 'page-special' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:noteref">
        <a>
            <xsl:call-template name="f:attlist.noteref"/>
            <xsl:apply-templates select="node()"/>
        </a>
    </xsl:template>

    <xsl:template name="f:attlist.noteref">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'noteref'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'noteref'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:attribute name="href" select="@idref"/>
        <xsl:copy-of select="@type" exclude-result-prefixes="#all"/>
    </xsl:template>

    <xsl:template match="dtbook:annoref">
        <a>
            <xsl:call-template name="f:attlist.annoref"/>
            <xsl:apply-templates select="node()"/>
        </a>
    </xsl:template>

    <xsl:template name="f:attlist.annoref">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'annoref'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'annoref'" tunnel="yes"/>
        </xsl:call-template>
        <xsl:attribute name="href" select="@idref"/>
        <xsl:copy-of select="@type" exclude-result-prefixes="#all"/>
    </xsl:template>

    <!-- TODO: allow dtbook:span[f:classes(.)='quote'] -->
    <xsl:template match="dtbook:q">
        <q>
            <xsl:call-template name="f:attlist.q"/>
            <xsl:apply-templates select="node()"/>
        </q>
    </xsl:template>

    <xsl:template name="f:attlist.q">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@cite" exclude-result-prefixes="#all"/>
    </xsl:template>

    <xsl:template match="dtbook:img">
        <img>
            <xsl:call-template name="f:attlist.img"/>
            <xsl:apply-templates select="node()"/>
        </img>
    </xsl:template>

    <xsl:template name="f:attlist.img">
        <xsl:param name="all-ids" select="()" tunnel="yes"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="all-ids" select="$all-ids" tunnel="yes"/>
        </xsl:call-template>
        <xsl:attribute name="src" select="concat('images/',@src)"/>
        <xsl:attribute name="alt" select="if (@alt and @alt='') then '' else if (not(@alt)) then 'image' else @alt"/>
        <xsl:copy-of select="@longdesc|@height|@width" exclude-result-prefixes="#all"/>
        <xsl:if test="not(@longdesc) and @id">
            <xsl:variable name="id" select="@id"/>
            <xsl:variable name="longdesc" select="(//dtbook:prodnote|//dtbook:caption)[tokenize(@imgref,'\s+')=$id]"/>
            <xsl:if test="$longdesc">
                <xsl:attribute name="longdesc" select="concat('#',$longdesc[1]/((@id,f:generate-pretty-id(.,$all-ids))[1]))"/>
                <!-- NOTE: if the image has multiple prodnotes or captions, only the first one will be referenced. -->
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:imggroup">
        <figure>
            <xsl:call-template name="f:attlist.imggroup"/>

            <xsl:choose>
                <xsl:when test="count(dtbook:img) = 1">
                    <!-- Single image -->

                    <xsl:call-template name="f:imggroup.image">
                        <xsl:with-param name="content" select="node()"/>
                    </xsl:call-template>

                </xsl:when>
                <xsl:otherwise>
                    <!-- Image series -->

                    <xsl:variable name="image-series-captions" select="dtbook:img[1]/preceding-sibling::dtbook:caption"/>
                    <xsl:choose>
                        <xsl:when test="count($image-series-captions) = 1">
                            <figcaption>
                                <xsl:for-each select="$image-series-captions">
                                    <xsl:call-template name="f:attlist.caption"/>
                                    <xsl:apply-templates select="node()"/>
                                </xsl:for-each>
                            </figcaption>
                        </xsl:when>
                        <xsl:when test="count($image-series-captions) &gt; 1">
                            <figcaption>
                                <xsl:for-each select="$image-series-captions">
                                    <div>
                                        <xsl:call-template name="f:attlist.caption"/>
                                        <xsl:apply-templates select="node()"/>
                                    </div>
                                </xsl:for-each>
                            </figcaption>
                        </xsl:when>
                    </xsl:choose>
                    <xsl:apply-templates select="dtbook:img[1]/preceding-sibling::node()[not(self::dtbook:caption)]"/>

                    <xsl:for-each select="dtbook:img">
                        <xsl:variable name="trailing-content"
                            select="if (following-sibling::dtbook:img) then following-sibling::node() intersect following-sibling::dtbook:img[1]/preceding-sibling::node() else following-sibling::node()"/>
                        <figure class="image">
                            <xsl:call-template name="f:imggroup.image">
                                <xsl:with-param name="content" select=". | $trailing-content"/>
                            </xsl:call-template>
                        </figure>
                    </xsl:for-each>

                </xsl:otherwise>
            </xsl:choose>
        </figure>
    </xsl:template>

    <xsl:template name="f:imggroup.image">
        <xsl:param name="content" required="yes"/>

        <xsl:apply-templates select="$content[self::dtbook:img]"/>
        <xsl:apply-templates select="$content[self::node() and not(self::dtbook:img or self::dtbook:caption)]"/>

        <xsl:variable name="image-captions" select="$content[self::dtbook:caption]"/>
        <xsl:choose>
            <xsl:when test="count($image-captions) = 1">
                <figcaption>
                    <xsl:for-each select="$image-captions">
                        <xsl:call-template name="f:attlist.caption"/>
                        <xsl:apply-templates select="node()"/>
                    </xsl:for-each>
                </figcaption>
            </xsl:when>
            <xsl:when test="count($image-captions) &gt; 1">
                <figcaption>
                    <xsl:for-each select="$image-captions">
                        <div>
                            <xsl:call-template name="f:attlist.caption"/>
                            <xsl:apply-templates select="node()"/>
                        </div>
                    </xsl:for-each>
                </figcaption>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:attlist.imggroup">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="if (count(dtbook:img) &gt; 1) then 'image-series' else 'image'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:p">
        <xsl:variable name="element" select="."/>
        <xsl:variable name="has-block-elements" select="if (dtbook:list or dtbook:dl or dtbook:imggroup) then true() else false()"/>
        <xsl:variable name="contains-single-code-element" select="count(dtbook:code) = 1 and count(* | text()[normalize-space()]) = 1 and @xml:space='preserve'"/>
        <xsl:if test="f:classes($element)=('precedingemptyline','precedingseparator')">
            <hr class="{if (f:classes($element)='precedingseparator') then 'separator' else 'emptyline'}"/>
        </xsl:if>
        <xsl:element name="{if ($has-block-elements) then 'div' else if ($contains-single-code-element) then 'pre' else 'p'}" namespace="http://www.w3.org/1999/xhtml">
            <!-- div allows the same attributes as p -->
            <xsl:call-template name="f:attlist.p">
                <xsl:with-param name="except-classes" select="('precedingemptyline','precedingseparator')" tunnel="yes"/>
                <xsl:with-param name="except" select="if (not($has-block-elements) and $contains-single-code-element) then 'xml:space' else ()" tunnel="yes"/>
            </xsl:call-template>
            <xsl:for-each-group select="node()" group-adjacent="not(self::dtbook:list or self::dtbook:dl or self::dtbook:imggroup)">
                <xsl:choose>
                    <xsl:when test="current-grouping-key()">
                        <xsl:choose>
                            <xsl:when test="$has-block-elements">
                                <p>
                                    <xsl:apply-templates select="current-group()"/>
                                </p>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates select="current-group()"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- In HTML, lists and figures(imggroup) are not allowed inside p. -->
                        <xsl:apply-templates select="current-group()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each-group>
        </xsl:element>
    </xsl:template>

    <xsl:template name="f:attlist.p">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:doctitle">
        <xsl:element name="{if (parent::dtbook:frontmatter) then 'h1' else 'p'}">
            <xsl:call-template name="f:attlist.doctitle"/>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="f:attlist.doctitle">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'fulltitle'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'title'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:docauthor">
        <p>
            <xsl:call-template name="f:attlist.docauthor"/>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template name="f:attlist.docauthor">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'z3998:author'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'docauthor'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:covertitle">
        <p>
            <xsl:call-template name="f:attlist.covertitle"/>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template name="f:attlist.covertitle">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'covertitle'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'covertitle'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:h1 | dtbook:h2 | dtbook:h3 | dtbook:h4 | dtbook:h5 | dtbook:h6">
        <xsl:variable name="name">
            <xsl:choose>
                <xsl:when test="ancestor-or-self::*[self::dtbook:level1 or
                                                    self::dtbook:level2 or
                                                    self::dtbook:level3 or
                                                    self::dtbook:level4 or
                                                    self::dtbook:level5 or
                                                    self::dtbook:level6]">
                    <xsl:sequence select="concat('h',f:level(.))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="local-name(.)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:element name="{$name}">
            <xsl:call-template name="f:attlist.h"/>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="dtbook:hd">
        <xsl:element name="h{f:level(.)}">
            <xsl:call-template name="f:attlist.h"/>
            <xsl:apply-templates select="node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template name="f:attlist.h">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="if (parent::dtbook:level1[tokenize(@class,'\s+')='titlepage']) then 'fulltitle' else ()" tunnel="yes"/>
            <xsl:with-param name="classes" select="if (parent::dtbook:level1[tokenize(@class,'\s+')='titlepage']) then 'title' else ()" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:bridgehead">
        <p>
            <xsl:call-template name="f:attlist.bridgehead"/>
            <xsl:apply-templates select="node()"/>
        </p>
    </xsl:template>

    <xsl:template name="f:attlist.bridgehead">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="types" select="'bridgehead'" tunnel="yes"/>
            <xsl:with-param name="classes" select="'bridgehead'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dtbook:blockquote">
        <blockquote>
            <xsl:call-template name="f:attlist.blockquote"/>
            <xsl:apply-templates select="node()"/>
        </blockquote>
    </xsl:template>

    <xsl:template name="f:attlist.blockquote">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@cite" exclude-result-prefixes="#all"/>
    </xsl:template>

    <xsl:template match="dtbook:dl">
        <xsl:apply-templates select="node()[self::dtbook:pagenum|self::text()|self::comment()][not(preceding-sibling::*[self::dtbook:dt or self::dtbook:dd])]">
            <xsl:with-param name="pagenum.parent" tunnel="yes" select="parent::*"/>
        </xsl:apply-templates>
        <dl>
            <xsl:call-template name="f:attlist.dl"/>
            <xsl:apply-templates
                select="dtbook:dt|dtbook:dd | (comment()|text())[preceding-sibling::*[self::dtbook:dt or self::dtbook:dd] and following-sibling::*[self::dtbook:dt or self::dtbook:dd]]"/>
            <xsl:if test="(dtbook:dt|dtbook:dd)[last()][self::dtbook:dt]">
                <dd/>
            </xsl:if>
        </dl>
        <xsl:apply-templates
            select="node()[self::dtbook:pagenum|self::text()|self::comment()][preceding-sibling::*[self::dtbook:dt or self::dtbook:dd] and not(following-sibling::*[self::dtbook:dt or self::dtbook:dd])]">
            <xsl:with-param name="pagenum.parent" tunnel="yes" select="parent::*"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template name="f:attlist.dl">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:dt">
        <dt>
            <xsl:call-template name="f:attlist.dt"/>
            <xsl:apply-templates select="node()"/>
            <xsl:variable name="this" select="."/>
            <xsl:for-each
                select="following-sibling::node()[self::dtbook:pagenum|self::text()|self::comment()][preceding-sibling::*[self::dtbook:dd|self::dtbook:dt][1]=$this][following-sibling::*[self::dtbook:dt or self::dtbook:dd]]">
                <xsl:if test="position()=1">
                    <xsl:text> </xsl:text>
                </xsl:if>
                <xsl:apply-templates select=".">
                    <xsl:with-param name="pagenum.parent" tunnel="yes" select="."/>
                </xsl:apply-templates>
            </xsl:for-each>
        </dt>
    </xsl:template>

    <xsl:template name="f:attlist.dt">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:dd">
        <dd>
            <xsl:call-template name="f:attlist.dd"/>
            <xsl:apply-templates select="node()"/>
            <xsl:variable name="this" select="."/>
            <xsl:for-each
                select="following-sibling::node()[self::dtbook:pagenum|self::text()|self::comment()][preceding-sibling::*[self::dtbook:dd|self::dtbook:dt][1]=$this][following-sibling::*[self::dtbook:dt or self::dtbook:dd]]">
                <xsl:if test="position()=1">
                    <xsl:text> </xsl:text>
                </xsl:if>
                <xsl:apply-templates select=".">
                    <xsl:with-param name="pagenum.parent" tunnel="yes" select="."/>
                </xsl:apply-templates>
            </xsl:for-each>
        </dd>
    </xsl:template>

    <xsl:template name="f:attlist.dd">
        <xsl:call-template name="f:attrs"/>
    </xsl:template>

    <xsl:template match="dtbook:list">
        <xsl:param name="all-ids" select="()" tunnel="yes"/>
        <xsl:choose>
            <xsl:when test="dtbook:hd">
                <section>
                    <xsl:attribute name="id" select="f:generate-pseudorandom-id(concat(f:generate-pretty-id(.,$all-ids),'_section'),$all-ids)"/>
                    <xsl:apply-templates select="dtbook:hd"/>
                    <xsl:call-template name="f:list.content">
                        <xsl:with-param name="all-ids" select="$all-ids" tunnel="yes"/>
                    </xsl:call-template>
                </section>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="f:list.content">
                    <xsl:with-param name="all-ids" select="$all-ids" tunnel="yes"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="f:list.content">
        <xsl:if test="not(@type=$supported-list-types)">
            <xsl:message terminate="yes">Error</xsl:message>
        </xsl:if>
        <xsl:apply-templates select="dtbook:pagenum[not(preceding-sibling::dtbook:li)]">
            <xsl:with-param name="pagenum.parent" tunnel="yes" select="parent::*"/>
        </xsl:apply-templates>
        <xsl:variable name="first-marker-text" select="dtbook:li[1]/(text()[1] | dtbook:p/text()[1])[normalize-space()][1]"/>
        <xsl:variable name="first-marker"
            select="if (starts-with($first-marker-text,'•')) then '•' else if (matches($first-marker-text,'^[0-9a-zA-Z]+\.')) then substring-before($first-marker-text,'.') else ''"/>
        <xsl:variable name="first-marker-type"
            select="if (not($first-marker)) then '' else if ($first-marker='•') then '•' else if (string(number($first-marker)) != 'NaN') then '1' else if ($first-marker='i') then 'i' else if ($first-marker='I') then 'I' else if ($first-marker=lower-case($first-marker)) then 'a' else 'A'"/>
        <xsl:element name="{if (@type=('ol','ul')) then @type else if ($parse-list-marker and $first-marker-type='•') then 'ul' else 'ol'}">
            <xsl:call-template name="f:attlist.list">
                <xsl:with-param name="marker-type" select="$first-marker-type"/>
            </xsl:call-template>
            <xsl:apply-templates select="dtbook:li">
                <xsl:with-param name="marker-type" select="$first-marker-type"/>
            </xsl:apply-templates>
        </xsl:element>
        <xsl:apply-templates select="dtbook:pagenum[preceding-sibling::dtbook:li and not(following-sibling::dtbook:li)]">
            <xsl:with-param name="pagenum.parent" tunnel="yes" select="parent::*"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template name="f:attlist.list">
        <xsl:param name="marker-type" select="''"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" tunnel="yes"
                            select="if (ancestor-or-self::dtbook:list[f:classes(.)='toc'] or @type=('ol','ul')) then ()
                                    else (if (not($parse-list-marker) or $marker-type='') then ('list-style-type-none') else (),
                                          if (@type='pl') then 'preformatted' else ())" />
            <xsl:with-param name="except-types" select="if (ancestor-or-self::dtbook:list[f:classes(.)='toc']) then 'toc' else ()" tunnel="yes"/>
        </xsl:call-template>
        <!-- @depth is implicit; ignore it -->
        <!-- NOTE: type attribute is actually deprecated; list styles should be handled through CSS -->
        <xsl:choose>
            <xsl:when test="@type=('ol','ul')">
                <xsl:if test="@enum">
                    <xsl:attribute name="type" select="@enum"/>
                </xsl:if>
            </xsl:when>
            <xsl:when test="not($parse-list-marker)"/>
            <xsl:when test="$marker-type='•'">
                <!-- <xsl:attribute name="type" select="'disc'"/> -->
            </xsl:when>
            <xsl:when test="$marker-type=('a','A','i','I')">
                <xsl:attribute name="type" select="$marker-type"/>
            </xsl:when>
        </xsl:choose>
        <!-- NOTE: start attribute is actually deprecated -->
        <xsl:if test="@type='ol' or not($parse-list-marker and $marker-type='•')">
            <xsl:copy-of select="@start"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:li">
        <xsl:param name="marker-type" select="''"/>
        <xsl:apply-templates select="preceding-sibling::comment() intersect preceding-sibling::*[1]/following-sibling::comment()"/>
        <li>
            <xsl:choose>
                <xsl:when test="parent::*/@type=('ol','ul') or not($parse-list-marker)">
                    <xsl:call-template name="f:attlist.li"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="marker-text" select="(text() | dtbook:p/text())[normalize-space()][1]"/>
                    <xsl:variable name="marker"
                                  select="if (starts-with($marker-text,'•')) then '•' else if (matches($marker-text,'^[0-9a-zA-Z]+\.')) then substring-before($marker-text,'.') else ()"/>
                    <xsl:variable name="marker-type"
                                  select="if (not($marker)) then '' else if ($marker='•') then '•' else if (string(number($marker)) != 'NaN') then '1' else parent::*/dtbook:li[1]/(text()[normalize-space()]|dtbook:p/text()[normalize-space()])[1]/(if (starts-with(.,'i.')) then 'i' else if (starts-with(.,'I.')) then 'I' else if (substring(.,1,1)=lower-case(substring(.,1,1))) then 'a' else 'A')"/>
                    <!-- NOTE: list is assumed to be preformatted; a generic script would calculate implicit value based on start attribute etc. -->
                    <xsl:variable name="marker-value"
                                  select="if ($marker-type=('a','A')) then f:numeric-alpha-to-decimal(lower-case($marker)) else if ($marker-type=('i','I')) then pf:numeric-roman-to-decimal(lower-case($marker)) else $marker"/>
                    
                    <xsl:call-template name="f:attlist.li">
                        <xsl:with-param name="li-value" select="$marker-value"/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
            
            <xsl:for-each select="node()">
                <xsl:choose>
                    <xsl:when test="parent::*/@type=('ol','ul') or not($parse-list-marker)">
                        <xsl:apply-templates select="."/>
                    </xsl:when>
                    <xsl:when
                        test="self::dtbook:* and not(preceding-sibling::node()[self::* or self::text()[normalize-space()]]) and not(text()[1]/preceding-sibling::*) and matches(text()[1],'^(\w+\.|•)')">
                        <xsl:if test="* or normalize-space(replace(text()[1],'^(\w+\.|•) ','')) != ''">
                            <xsl:variable name="element">
                                <xsl:apply-templates select="."/>
                            </xsl:variable>
                            <xsl:for-each select="$element/*">
                                <xsl:copy exclude-result-prefixes="#all">
                                    <xsl:copy-of select="@*" exclude-result-prefixes="#all"/>
                                    <xsl:value-of select="replace(text()[1],'^(\w+\.|•) ','')"/>
                                    <xsl:copy-of select="node() except text()[1]" exclude-result-prefixes="#all"/>
                                </xsl:copy>
                            </xsl:for-each>
                        </xsl:if>
                    </xsl:when>
                    <xsl:when test="self::text() and not(preceding-sibling::node()[self::* or self::text()[normalize-space()]]) and matches(.,'^(\w+\.|•)')">
                        <xsl:value-of select="replace(.,'^(\w+\.|•) ','')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>

            <xsl:variable name="this" select="."/>
            <xsl:apply-templates select="following-sibling::dtbook:pagenum[preceding-sibling::dtbook:li[1]=$this][following-sibling::dtbook:li]">
                <xsl:with-param name="pagenum.parent" tunnel="yes" select="."/>
            </xsl:apply-templates>
        </li>
    </xsl:template>

    <xsl:template name="f:attlist.li">
        <xsl:param name="li-value" select="''"/>
        <xsl:call-template name="f:attrs"/>
        <xsl:if test="string(number($li-value)) != 'NaN'">
            <xsl:attribute name="value" select="$li-value"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:lic">
        <span>
            <xsl:call-template name="f:attlist.lic"/>
            <xsl:apply-templates select="node()"/>
        </span>
    </xsl:template>

    <xsl:template name="f:attlist.lic">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes" select="'lic'" tunnel="yes"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="f:cellhalign">
        <xsl:sequence select="if (@align and not(@align='char')) then concat('text-align: ',@align,';') else ()"/>
        <xsl:if test="@align='char'">
            <xsl:sequence select="'text-align: right;'"/>
            <!--
                NOTE: when align is “char”, we could set "padding" to an integer such that
                the cells align horizontally according to the “char” character. For instance,'
                if one cell contains “1.2” and the cell below it contains “1.25”, then the
                padding for the first cell would be 1 and the padding for the last cell would be 0.
                Unless someone requests this, we'll ignore this for now. The algorithm would be
                padding = {length of longest postfix in column} - {length of postfix in current cell}.
            -->
        </xsl:if>
        <!--<xsl:if test="@char">
            <xsl:attribute name="char" select="@char"/> <!-\- Character -\->
        </xsl:if>
        <xsl:if test="@charoff">
            <xsl:attribute name="charoff" select="@charoff"/> <!-\- Length -\->
        </xsl:if>-->
    </xsl:template>

    <xsl:template name="f:cellvalign">
        <xsl:sequence select="if (@valign) then concat('vertical-align: ',@valign,';') else ()"/>
    </xsl:template>

    <xsl:template match="dtbook:table">
        <xsl:apply-templates select="(.|dtbook:tbody)/dtbook:pagenum[not(preceding-sibling::dtbook:tr)]">
            <xsl:with-param name="pagenum.parent" tunnel="yes" select="parent::*"/>
        </xsl:apply-templates>

        <table>
            <xsl:call-template name="f:attlist.table"/>
            <xsl:if test="@summary and not(dtbook:caption)">
                <caption>
                    <p class="table-summary">
                        <xsl:value-of select="string(@summary)"/>
                    </p>
                </caption>
            </xsl:if>
            <xsl:apply-templates select="dtbook:caption/preceding-sibling::comment() | dtbook:caption"/>
            <xsl:if test="dtbook:col">
                <colgroup>
                    <xsl:for-each select="dtbook:col">
                        <xsl:variable name="this" select="."/>
                        <xsl:apply-templates select="preceding-sibling::comment()[following-sibling::*[1]=$this] | $this"/>
                    </xsl:for-each>
                </colgroup>
            </xsl:if>
            <xsl:for-each select="dtbook:colgroup">
                <xsl:variable name="this" select="."/>
                <xsl:apply-templates select="preceding-sibling::comment()[following-sibling::*[1]=$this] | $this"/>
            </xsl:for-each>
            <xsl:apply-templates select="dtbook:thead | dtbook:tfoot | dtbook:tbody"/>
            <xsl:if test="not(dtbook:tbody) and dtbook:tr">
                <xsl:choose>
                    <xsl:when test="$add-tbody">
                        <tbody>
                            <xsl:apply-templates select="dtbook:tr"/>
                        </tbody>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="dtbook:tr"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </table>

        <xsl:apply-templates select="(.|dtbook:tbody)/dtbook:pagenum[not(following-sibling::dtbook:tr)]">
            <xsl:with-param name="pagenum.parent" tunnel="yes" select="parent::*"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template name="f:attlist.table">
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="classes"
                select="(
                if (@rules) then concat('table-rules-',@rules) else (),
                if (@frame) then concat('table-frame-',@frame) else ()
                )" tunnel="yes"
            />
        </xsl:call-template>
        <!-- @summary handled the dtbook:table and dtbook:caption templates -->
        <xsl:variable name="style">
            <xsl:if test="@border">
                <xsl:sequence select="concat('border: ',@border,(if (not(ends-with(@border,'%'))) then 'px' else ''),' solid black;')"/>
            </xsl:if>
            <xsl:if test="@cellspacing">
                <xsl:if test="@width">
                    <xsl:sequence select="concat('width: ',@width,if (not(ends-with(@width,'%'))) then 'px;' else ';')"/>
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="@cellspacing=('','0')">
                        <xsl:sequence select="'border-collapse: collapse; border-spacing: 0;'"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="concat('border-collapse: separate; border-spacing: ',@cellspacing,'px;')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="style" select="$style[not(.='')]"/>
        <xsl:if test="count($style)">
            <xsl:attribute name="style" select="string-join($style,' ')"/>
        </xsl:if>
        <!-- @cellpadding is added to the @style attribute of descendant th and td elements -->
    </xsl:template>

    <xsl:template match="dtbook:caption[parent::dtbook:table]">
        <caption>
            <xsl:call-template name="f:attlist.caption"/>
            <xsl:if test="parent::dtbook:table[@summary]">
                <p class="table-summary">
                    <xsl:value-of select="string(parent::dtbook:table/@summary)"/>
                </p>
            </xsl:if>
            <xsl:apply-templates select="node()"/>
        </caption>
    </xsl:template>

    <xsl:template match="dtbook:caption[parent::dtbook:imggroup]">
        <xsl:apply-templates select="node()"/>
        <!--<div>
            <xsl:call-template name="f:attlist.caption"/>
            
        </div>-->
    </xsl:template>

    <xsl:template name="f:attlist.caption">
        <xsl:param name="all-ids" select="()" tunnel="yes"/>
        <xsl:call-template name="f:attrs">
            <xsl:with-param name="all-ids" select="$all-ids" tunnel="yes"/>
        </xsl:call-template>
        <!-- @imgref is dropped, the relationship is preserved in the corresponding img/@longdesc -->
        <xsl:if test="not(@id) and (
                        $generate-ids or (
                        some $ref in tokenize(@imgref,'\s+')[not(.='')] satisfies //dtbook:img[@id=$ref]))">
            <xsl:attribute name="id" select="f:generate-pretty-id(.,$all-ids)"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:thead">
        <thead>
            <xsl:call-template name="f:attlist.thead"/>
            <xsl:apply-templates select="node()"/>
        </thead>
    </xsl:template>

    <xsl:template name="f:attlist.thead">
        <xsl:call-template name="f:attrs"/>
        <xsl:variable name="style">
            <xsl:call-template name="f:cellhalign"/>
            <xsl:call-template name="f:cellvalign"/>
        </xsl:variable>
        <xsl:variable name="style" select="$style[not(.='')]"/>
        <xsl:if test="count($style)">
            <xsl:attribute name="style" select="string-join($style,' ')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:tfoot">
        <tfoot>
            <xsl:call-template name="f:attlist.tfoot"/>
            <xsl:apply-templates select="node()"/>
        </tfoot>
    </xsl:template>

    <xsl:template name="f:attlist.tfoot">
        <xsl:call-template name="f:attrs"/>
        <xsl:variable name="style">
            <xsl:call-template name="f:cellhalign"/>
            <xsl:call-template name="f:cellvalign"/>
        </xsl:variable>
        <xsl:variable name="style" select="$style[not(.='')]"/>
        <xsl:if test="count($style)">
            <xsl:attribute name="style" select="string-join($style,' ')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:tbody">
        <tbody>
            <xsl:call-template name="f:attlist.tbody"/>
            <xsl:apply-templates select="node()[not(self::dtbook:pagenum)]"/>
        </tbody>
    </xsl:template>

    <xsl:template name="f:attlist.tbody">
        <xsl:call-template name="f:attrs"/>
        <xsl:variable name="style">
            <xsl:call-template name="f:cellhalign"/>
            <xsl:call-template name="f:cellvalign"/>
        </xsl:variable>
        <xsl:variable name="style" select="$style[not(.='')]"/>
        <xsl:if test="count($style)">
            <xsl:attribute name="style" select="string-join($style,' ')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:colgroup">
        <colgroup>
            <xsl:call-template name="f:attlist.colgroup"/>
            <xsl:apply-templates select="node()"/>
        </colgroup>
    </xsl:template>

    <xsl:template name="f:attlist.colgroup">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@span" exclude-result-prefixes="#all"/>
        <xsl:variable name="style">
            <xsl:call-template name="f:cellhalign"/>
            <xsl:call-template name="f:cellvalign"/>
            <xsl:sequence select="if (@width) then concat('width: ',@width, if (not(ends-with(@width,'%'))) then 'px;' else ';') else ()"/>
        </xsl:variable>
        <xsl:variable name="style" select="$style[not(.='')]"/>
        <xsl:if test="count($style)">
            <xsl:attribute name="style" select="string-join($style,' ')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:col">
        <col>
            <xsl:call-template name="f:attlist.col"/>
            <xsl:apply-templates select="node()"/>
        </col>
    </xsl:template>

    <xsl:template name="f:attlist.col">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@span" exclude-result-prefixes="#all"/>
        <xsl:variable name="style">
            <xsl:call-template name="f:cellhalign"/>
            <xsl:call-template name="f:cellvalign"/>
            <xsl:sequence select="if (@width) then concat('width: ',@width, if (not(ends-with(@width,'%'))) then 'px;' else ';') else ()"/>
        </xsl:variable>
        <xsl:variable name="style" select="$style[not(.='')]"/>
        <xsl:if test="count($style)">
            <xsl:attribute name="style" select="string-join($style,' ')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:tr">
        <tr>
            <xsl:call-template name="f:attlist.tr"/>
            <xsl:apply-templates select="node()"/>
        </tr>
    </xsl:template>

    <xsl:template name="f:attlist.tr">
        <xsl:call-template name="f:attrs"/>
        <xsl:variable name="style">
            <xsl:call-template name="f:cellhalign"/>
            <xsl:call-template name="f:cellvalign"/>
        </xsl:variable>
        <xsl:variable name="style" select="$style[not(.='')]"/>
        <xsl:if test="count($style)">
            <xsl:attribute name="style" select="string-join($style,' ')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="dtbook:th | dtbook:td">
        <xsl:element name="{local-name()}">
            <xsl:call-template name="f:attlist.th.td"/>
            
            <xsl:apply-templates select="node()"/>

            <xsl:if test="not(following-sibling::dtbook:th or following-sibling::dtbook:td)">
                <!-- If this is the last cell in the row -->

                <xsl:if test="../following-sibling::dtbook:tr">
                    <!-- If there is a row after this row -->

                    <!-- Then move pagenums from between this and the following row into the start of this cell -->
                    <xsl:apply-templates select="../(following-sibling::dtbook:pagenum intersect following-sibling::dtbook:tr[1]/preceding-sibling::dtbook:pagenum)">
                        <xsl:with-param name="pagenum.parent" tunnel="yes" select="."/>
                    </xsl:apply-templates>
                </xsl:if>
            </xsl:if>
        </xsl:element>
    </xsl:template>

    <xsl:template name="f:attlist.th.td">
        <xsl:call-template name="f:attrs"/>
        <xsl:copy-of select="@headers|@scope|@rowspan|@colspan" exclude-result-prefixes="#all"/>
        <!-- @abbr and @axis are ignored as they have no good equivalent in HTML -->
        <xsl:variable name="style">
            <xsl:call-template name="f:cellhalign"/>
            <xsl:call-template name="f:cellvalign"/>
            <xsl:variable name="cellpadding" select="ancestor::dtbook:table[1][@cellpadding][1]/@cellpadding"/>
            <xsl:sequence select="if ($cellpadding) then concat('padding: ',$cellpadding,if (not(ends-with($cellpadding,'%'))) then 'px;' else ';') else ()"/>
        </xsl:variable>
        <xsl:variable name="style" select="$style[not(.='')]"/>
        <xsl:if test="count($style)">
            <xsl:attribute name="style" select="string-join($style,' ')"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="math:*">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="math:*/@*">
        <xsl:choose>
            <xsl:when test="local-name() = 'altimg'">
                <xsl:attribute name="altimg" select="concat('images/',.)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="." exclude-result-prefixes="#all"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:function name="f:classes" as="xs:string*">
        <xsl:param name="element" as="element()"/>
        <xsl:sequence select="tokenize($element/@class,'\s+')"/>
    </xsl:function>

    <xsl:function name="f:level" as="xs:integer">
        <xsl:param name="element" as="element()"/>
        <xsl:variable name="level"
            select="count($element/ancestor-or-self::*[self::dtbook:level or self::dtbook:level1 or self::dtbook:level2 or self::dtbook:level3 or self::dtbook:level4 or self::dtbook:level5 or self::dtbook:level6 or self::dtbook:linegroup[dtbook:hd] or self::dtbook:poem[dtbook:hd]])"/>
        <xsl:sequence select="max((1, min(($level, 6))))"/>
    </xsl:function>

    <xsl:function name="f:generate-pretty-id" as="xs:string">
        <xsl:param name="element" as="element()"/>
        <xsl:param name="all-ids"/>
        <xsl:variable name="id">
            <xsl:choose>
                <xsl:when test="$element[self::dtbook:blockquote or self::dtbook:q]">
                    <xsl:sequence select="concat('quote_',count($element/(ancestor::*|preceding::*)[self::dtbook:blockquote or self::dtbook:q])+1)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="element-name" select="local-name($element)"/>
                    <xsl:sequence select="concat($element-name,'_',count($element/(ancestor::*|preceding::*)[local-name()=$element-name])+1)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:sequence select="if ($all-ids=$id) then generate-id($element) else $id"/>
    </xsl:function>

    <xsl:function name="f:generate-pseudorandom-id" as="xs:string">
        <xsl:param name="prefix" as="xs:string"/>
        <xsl:param name="all-ids"/>
        <xsl:variable name="pseudorandom-id" select="concat($prefix,'_',replace(replace(string(current-time()),'\d+:\d+:([\d\.]+)(\+.*)?','$1'),'[^\d]',''))"/>
        <xsl:choose>
            <xsl:when test="$pseudorandom-id=$all-ids">
                <!--
                    Try again.
                    WARNING: this is a theoretically infinite recursion. In practice however, this shouldn't happen.
                -->
                <xsl:sequence select="f:generate-pseudorandom-id($prefix, $all-ids)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$pseudorandom-id"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="f:is-inline" as="xs:boolean">
        <xsl:param name="parent" as="element()?"/>
        <xsl:choose>
            <xsl:when test="$parent">
                <xsl:variable name="sibling-implies-inline"
                    select="('em','strong','dfn','code','samp','kbd','cite','abbr','acronym','a','img','br','q','sub','sup','span','bdo','sent','w','annoref','noteref','lic')"/>
                <xsl:variable name="parent-implies-inline"
                    select="($sibling-implies-inline,'imggroup','pagenum','prodnote','line','linenum','address','title','author','byline','dateline','p','doctitle','docauthor','covertitle','h1','h2','h3','h4','h5','h6','bridgehead','dt')"/>
                <xsl:sequence
                    select="if ($parent[self::dtbook:* and local-name()=$parent-implies-inline] or $parent/(text()[normalize-space()],dtbook:*[local-name()=$sibling-implies-inline])) then true() else false()"
                />
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="false()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="f:numeric-alpha-to-decimal" as="xs:integer">
        <!-- TODO: update pf:numeric-alpha-to-decimal in numeral-conversion.xsl in DP2 common-utils -->
        <xsl:param name="alpha" as="xs:string"/>
        <xsl:value-of select="xs:integer(sum(for $pos in (1 to string-length($alpha)) return ((string-to-codepoints(substring( $alpha, $pos, 1 )) - 96 ) * f:power( 26, $pos - 1 ))))"/>
    </xsl:function>

    <xsl:function name="f:power" as="xs:double">
        <!-- TODO: move to DP2 common-utils? or a math module? -->
        <!-- From http://users.atw.hu/xsltcookbook2/xsltckbk2-chp-3-sect-5.html -->
        <xsl:param name="base" as="xs:double"/>
        <xsl:param name="exp" as="xs:integer"/>
        <xsl:sequence select="if ($exp lt 0) then f:power(1.0 div $base, -$exp)
            else
            if ($exp eq 0)
            then 1e0
            else $base * f:power($base, $exp - 1)"/>
    </xsl:function>

</xsl:stylesheet>
