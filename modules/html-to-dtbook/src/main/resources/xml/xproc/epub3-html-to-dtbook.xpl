<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                type="pxi:epub3-html-to-dtbook"
                name="main">

    <p:input port="html.fileset" primary="true"/>
    <p:input port="html.in-memory">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            Single HTML document
        </p:documentation>
    </p:input>
    <p:input port="resources.fileset"/>
    <p:input port="resources.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            Resources
        </p:documentation>
    </p:input>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            DTBook document and resources
        </p:documentation>
        <p:pipe step="add-dtbook" port="result.in-memory"/>
    </p:output>

    <p:option name="dtbook-file-name" required="true"/>
    <p:option name="imply-headings" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
            px:fileset-filter
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-outline
        </p:documentation>
    </p:import>

    <p:identity>
        <p:input port="source">
            <p:pipe step="main" port="html.in-memory"/>
        </p:input>
    </p:identity>

    <!--
        epub3-to-dtbook.xsl makes certain assumptions about the input structure
        - sectioning content elements have sectioning (root/content) element as parent
        - heading elements have sectioning element as parent
        - body becomes book
        - child 'header' element of body becomes doctitle/covertitle/docauthor
        - child sectionining elements of body become frontmatter/bodymatter/rearmatter
    -->
    <p:group px:progress="1/2" name="prepare-html">
        <p:output port="result"/>
        <!--
            Add missing sectioning elements so that there are no implied sections
        -->
        <px:html-outline fix-sectioning="no-implied" px:progress="1/4"/>
        <!--
            Move everything one level down if step above resulted in multiple body elements
        -->
        <p:wrap match="/*/html:body[preceding-sibling::html:body|following-sibling::html:body]"
                group-adjacent="true()" wrapper="html:body"/>
        <p:rename match="/*/html:body/html:body" new-name="html:section"/>
        <!--
            Move body one level down if it contains content beside sectioning and heading elements
        -->
        <p:wrap match="/*/html:body[text()[normalize-space()]
                                   |*[not(self::html:section  |self::html:h1
                                         |self::html:article  |self::html:h2
                                         |self::html:aside    |self::html:h3
                                         |self::html:nav      |self::html:h4
                                         |self::html:header   |self::html:h5
                                         |self::html:hgroup   |self::html:h6
                                         )]]"
                wrapper="html:body"/>
        <p:rename match="/*/html:body/html:body" new-name="html:section"/>
        <!--
            Add missing headings
        -->
        <p:choose px:progress="1/4">
            <p:when test="$imply-headings='true'">
                <px:html-outline fix-untitled-sections="imply-heading"/>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <!--
            Wrap body's heading element inside header
        -->
        <p:wrap match="/*/html:body/*[self::html:h1
                                     |self::html:h2
                                     |self::html:h3
                                     |self::html:h4
                                     |self::html:h5
                                     |self::html:hgroup]"
                group-adjacent="true()" wrapper="html:header"/>
        <!--
            Move sectioning and heading elements up
        -->
        <p:xslt px:progress="1/2">
            <p:input port="stylesheet">
                <p:document href="../xslt/prepare-html.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:group>

    <!--
        Convert DPUB-ARIA roles to epub:type (see https://idpf.github.io/epub-guides/epub-aria-authoring/#sec-mappings)
    -->
    <p:label-elements match="*[@role=('doc-pagebreak',
                                      'doc-noteref',
                                      'doc-footnote',
                                      'doc-endnote',
                                      'doc-endnotes',
                                      'doc-epigraph')]"
                      attribute="epub:type"
                      replace="true"
                      label="string-join(
                               distinct-values((
                                 @epub:type/tokenize(.,'\s+')[not(.='')],
                                 replace(@role,'^doc-',''))),
                               ' ')"/>


    <!--
        Convert HTML to DTBook
    -->
    <p:xslt px:progress="1/2">
        <p:input port="stylesheet">
            <p:document href="../xslt/epub3-to-dtbook.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>

    <!--
        Rename
    -->
    <px:set-base-uri name="dtbook">
        <p:with-option name="base-uri"
                       select="resolve-uri(($dtbook-file-name[.!=''],
                                            concat(replace(base-uri(/*),'^(.*)\.[^/\.]*$','$1'),'.xml'))[1],
                                           base-uri(/*))"/>
    </px:set-base-uri>
    <p:sink/>

    <!--
        Combine DTBook with resources
    -->
    <px:fileset-filter not-media-types="text/css" name="rm-css">
        <p:input port="source">
            <p:pipe step="main" port="resources.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="resources.in-memory"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-add-entry media-type="application/x-dtbook+xml" name="add-dtbook" px:progress="1/20">
        <p:input port="source.in-memory">
            <p:pipe step="rm-css" port="result.in-memory"/>
        </p:input>
        <p:input port="entry">
            <p:pipe step="dtbook" port="result"/>
        </p:input>
        <p:with-param port="file-attributes" name="omit-xml-declaration" select="'false'"/>
        <p:with-param port="file-attributes" name="version" select="'1.0'"/>
        <p:with-param port="file-attributes" name="encoding" select="'utf-8'"/>
        <p:with-param port="file-attributes" name="doctype-public" select="'-//NISO//DTD dtbook 2005-3//EN'"/>
        <p:with-param port="file-attributes" name="doctype-system" select="'http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd'"/>
    </px:fileset-add-entry>

</p:declare-step>
