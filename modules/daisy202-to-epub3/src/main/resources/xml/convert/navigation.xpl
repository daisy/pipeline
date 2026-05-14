<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="pxi:daisy202-to-epub3-navigation" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>Make a EPUB3 Navigation Document based on the Content Documents.</h1>
    </p:documentation>

    <p:input port="ncc-navigation" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">An EPUB3 Navigation Document with contents based purely on the DAISY 2.02 NCC.</p:documentation>
    </p:input>
    <p:input port="content" primary="false" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The EPUB3 Content Documents.</p:documentation>
    </p:input>

    <p:output port="navigation" primary="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The complete EPUB3 Navigation Document.</p:documentation>
        <p:pipe port="result" step="result.navigation"/>
    </p:output>
    <p:output port="ncx" primary="false" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">A NCX document generated based on the Navigation Document.</p:documentation>
        <p:pipe port="result" step="result.ncx"/>
    </p:output>
    <p:output port="content-navfix" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The same sequence of EPUB3 Content Documents as arrived on the "content" port, but with the old Navigation Document replaced by the new one (if it's in the spine).</p>
            <pre><code class="example">
                <html xmlns="http://www.w3.org/1999/xhtml" xml:base="file:/home/user/epub3/epub/Publication/Content/a.xhtml" original-href="file:/home/user/daisy202/a.html">...</html>
                <html xmlns="http://www.w3.org/1999/xhtml" xml:base="file:/home/user/epub3/epub/Publication/Content/ncc.xhtml" original-href="file:/home/user/daisy202/ncc.html">...</html>
                <html xmlns="http://www.w3.org/1999/xhtml" xml:base="file:/home/user/epub3/epub/Publication/Content/b.xhtml" original-href="file:/home/user/daisy202/b.html">...</html>
                <html xmlns="http://www.w3.org/1999/xhtml" xml:base="file:/home/user/epub3/epub/Publication/Content/c.xhtml" original-href="file:/home/user/daisy202/c.html">...</html>
            </code></pre>
        </p:documentation>
        <p:pipe port="result" step="result.content"/>
    </p:output>
    <p:output port="fileset">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">A fileset with references to the Navigation Document and the NCX.</p:documentation>
        <p:pipe port="result" step="result.fileset"/>
    </p:output>

    <p:option name="publication-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>URI to the EPUB3 Publication directory.</p>
            <pre><code class="example">file:/home/user/epub3/epub/Publication/</code></pre>
        </p:documentation>
    </p:option>
    <p:option name="content-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>URI to the EPUB3 Content directory.</p>
            <pre><code class="example">file:/home/user/epub3/epub/Publication/Content/</code></pre>
        </p:documentation>
    </p:option>
    <p:option name="compatibility-mode" required="true" cx:as="xs:string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Whether or not to include NCX-file. Can be either 'true' (default) or 'false'.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-create
            px:fileset-add-entry
            px:fileset-add-entries
            px:fileset-join
            px:fileset-rebase
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub3-add-navigation-doc
            px:epub3-nav-to-ncx
        </p:documentation>
    </p:import>
    <cx:import href="http://www.daisy.org/pipeline/modules/daisy202-utils/library.xsl" type="application/xslt+xml">
        <p:documentation>
            pf:daisy202-identifier
        </p:documentation>
    </cx:import>


    <p:variable name="original-href" select="/*/@original-href"/>

    <!--<px:epub3-nav-create-toc name="content-nav-toc">
        <p:input port="source">
            <p:pipe port="content" step="main"/>
        </p:input>
        <p:with-option name="output-base-uri" select="...">
            <p:empty/>
        </p:with-option>
    </px:epub3-nav-create-toc>
    <p:sink/>-->

    <!-- TODO: create nav with html-lot-annotator.xsl here when it's done -->
    <!-- TODO: create nav with html-loi-annotator.xsl here when it's done -->

    <p:identity name="ncc-nav-toc">
        <p:input port="source" select="//html:nav[@*[name()='epub:type']='toc']">
            <p:pipe port="ncc-navigation" step="main"/>
        </p:input>
    </p:identity>
    <p:sink/>
    <p:identity name="ncc-nav-page-list">
        <p:input port="source" select="//html:nav[@*[name()='epub:type']='page-list']">
            <p:pipe port="ncc-navigation" step="main"/>
        </p:input>
    </p:identity>
    <p:sink/>
    <p:identity name="ncc-nav-landmarks">
        <p:input port="source" select="//html:nav[@*[name()='epub:type']='landmarks']">
            <p:pipe port="ncc-navigation" step="main"/>
        </p:input>
    </p:identity>
    <p:sink/>

    <!--<px:epub3-nav-annotate-hidden name="toc">
        <p:input port="source">
            <p:pipe port="result" step="content-nav-toc"/>
        </p:input>
        <p:input port="visible">
            <p:pipe port="result" step="ncc-toc"/>
        </p:input>
    </px:epub3-nav-annotate-hidden>-->
    <p:identity name="toc">
        <!-- TODO: replace with px:epub3-nav-annotate-hidden when that step has been implemented -->
        <p:input port="source">
            <p:pipe port="result" step="ncc-nav-toc"/>
        </p:input>
    </p:identity>
    <p:sink/>

    <px:epub3-add-navigation-doc name="nav-doc">
        <p:input port="toc">
            <p:pipe step="toc" port="result"/>
        </p:input>
        <p:input port="page-list">
            <p:pipe step="ncc-nav-page-list" port="result"/>
        </p:input>
        <p:input port="landmarks">
            <p:pipe step="ncc-nav-landmarks" port="result"/>
        </p:input>
        <p:with-option name="title" select="/*/html:head/html:title">
            <p:pipe port="ncc-navigation" step="main"/>
        </p:with-option>
        <p:with-option name="language" select="(/*/@lang,
                                                /*/@xml:lang,
                                                /*/html:head/html:meta[lower-case(@name)='dc:language']/@content)[1]">
            <p:pipe port="ncc-navigation" step="main"/>
        </p:with-option>
        <p:with-option name="output-base-uri" select="concat($content-dir,'ncc.xhtml')"/>
    </px:epub3-add-navigation-doc>
    <p:sink/>
    <p:xslt>
        <p:input port="source">
            <p:pipe step="nav-doc" port="nav"/>
        </p:input>
        <p:with-param name="identifier" select="pf:daisy202-identifier(/)">
            <p:pipe port="ncc-navigation" step="main"/>
        </p:with-param>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
                    <xsl:param name="identifier" required="yes"/>
                    <xsl:template match="@*|node()">
                        <xsl:copy>
                            <xsl:apply-templates select="@*|node()"/>
                        </xsl:copy>
                    </xsl:template>
                    <xsl:template match="html:head">
                        <xsl:copy>
                            <xsl:apply-templates select="@*"/>
                            <xsl:apply-templates select="html:meta[@charset]"/>
                            <xsl:element name="meta" namespace="http://www.w3.org/1999/xhtml">
                                <xsl:attribute name="name" select="'dc:identifier'"/>
                                <xsl:attribute name="content" select="$identifier"/>
                            </xsl:element>
                            <xsl:apply-templates select="node()[not(self::html:meta[@charset or lower-case(@name)='dc:identifier'])]"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:xslt>
    <p:delete match="/html:html/html:head/html:meta[@http-equiv]"/>
    <p:delete match="html:a[normalize-space(string-join(self::*//text(),''))='']"/>
    <p:delete match="html:span[normalize-space(string-join(self::*//text(),''))='']"/>
    <p:delete match="html:li[normalize-space(string-join(self::*//text(),''))='' or not(html:a or html:span)]"/>
    <p:delete match="html:ol[normalize-space(string-join(self::*//text(),''))='' or not(html:li)]"/>
    <p:delete match="html:nav[not(@epub:type='toc') and normalize-space(string-join(self::*//text(),''))='']"/>

    <p:add-attribute match="/*" attribute-name="original-href">
        <p:with-option name="attribute-value" select="/*/@original-href">
            <p:pipe port="ncc-navigation" step="main"/>
        </p:with-option>
    </p:add-attribute>
    <px:message message="Successfully created navigation document (ncc.xhtml)"/>
    <p:identity name="result.navigation"/>
    <p:sink/>

    <p:choose>
        <p:when test="$compatibility-mode='true'">
            <px:fileset-create>
                <p:with-option name="base" select="$content-dir"/>
            </px:fileset-create>
            <px:fileset-add-entries>
                <p:input port="entries">
                    <p:pipe step="main" port="content"/>
                </p:input>
            </px:fileset-add-entries>
            <px:fileset-join name="ncx.spine"/>
            <p:sink/>

            <p:insert match="/*" position="first-child">
                <p:input port="source">
                    <p:pipe port="result" step="result.navigation"/>
                </p:input>
                <p:input port="insertion">
                    <p:pipe port="result" step="ncx.spine"/>
                </p:input>
            </p:insert>
            <px:message message="Creating NCX..."/>
            <px:epub3-nav-to-ncx name="ncx.ncx-without-docauthors"/>
            <p:for-each>
                <p:iteration-source select="//html:meta[@name='dc:creator']">
                    <p:pipe port="ncc-navigation" step="main"/>
                </p:iteration-source>
                <p:template>
                    <p:input port="template">
                        <p:inline xmlns="http://www.daisy.org/z3986/2005/ncx/" exclude-inline-prefixes="#all">
                            <docAuthor>
                                <text>{string(/*/@content)}</text>
                            </docAuthor>
                        </p:inline>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:template>
            </p:for-each>
            <p:identity name="ncx.docauthors"/>
            <p:insert xmlns="http://www.daisy.org/z3986/2005/ncx/" match="/*/*[2]" position="after">
                <p:input port="source">
                    <p:pipe port="result" step="ncx.ncx-without-docauthors"/>
                </p:input>
                <p:input port="insertion">
                    <p:pipe port="result" step="ncx.docauthors"/>
                </p:input>
            </p:insert>
            <px:set-base-uri>
                <p:with-option name="base-uri" select="concat($content-dir,'ncx.xml')"/>
            </px:set-base-uri>
            <px:message message="Successfully created NCX"/>
        </p:when>
        <p:otherwise>
            <p:identity>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <p:identity name="result.ncx"/>

    <px:fileset-rebase>
        <p:input port="source">
            <p:pipe step="nav-doc" port="nav.fileset"/>
        </p:input>
        <p:with-option name="new-base" select="$content-dir"/>
    </px:fileset-rebase>
    <p:choose>
        <p:when test="$compatibility-mode='true'">
            <px:fileset-add-entry>
                <p:with-option name="href" select="base-uri(/*)">
                    <p:pipe port="result" step="result.ncx"/>
                </p:with-option>
                <p:with-option name="media-type" select="'application/x-dtbncx+xml'"/>
            </px:fileset-add-entry>
            <px:message message="NCX added to the fileset"/>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <px:message message="Navigation Document added to the fileset"/>
    <p:identity name="result.fileset"/>
    <p:sink/>

    <p:for-each>
        <p:iteration-source>
            <p:pipe port="content" step="main"/>
        </p:iteration-source>
        <p:variable name="nav-base" select="base-uri(/*)">
            <p:pipe port="result" step="result.navigation"/>
        </p:variable>
        <p:choose>
            <p:when test="base-uri(/*)=$nav-base">
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="result" step="result.navigation"/>
                    </p:input>
                </p:identity>
                <px:message message="The navigation document is in the spine; replaced it with the updated version"/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <p:identity name="result.content"/>

</p:declare-step>
