<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:svg="http://www.w3.org/2000/svg"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    <!-- <xsl:use-package name="http://www.daisy.org/pipeline/modules/css-utils/library.xsl"/> -->
    <xsl:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xsl"/>

    <xsl:strip-space elements="*"/>
    <xsl:output indent="yes"/>

    <xsl:param name="context.fileset" as="document-node(element(d:fileset))?"/>
    <xsl:param name="context.in-memory" as="document-node()*"/>

    <xsl:template match="text()|@*"/>

    <xsl:variable name="doc-base"
        select="if (/html/head/base[@href][1]) then resolve-uri(normalize-space(/html/head/base[@href][1]/@href),base-uri(/*)) else base-uri(/*)"/>
    <xsl:variable name="include-input-doc" select="true()" as="xs:boolean"/>

    <xsl:template match="/*">
        <!--
        Builds a file set with all the resources referenced from the HTML.
        Some media types are inferred – users may have to apply additional type detection.
        A @kind attribute is used to annotate the kind of resource:
          - stylesheet
          - media
          - image
          - video
          - audio
          - script
          - content
          - description
          - text-track
          - animation
          - font
        -->
        <d:fileset>
            <xsl:attribute name="xml:base" select="replace($doc-base,'^(.+/)[^/]*','$1')"/>
            <xsl:if test="$include-input-doc">
                <d:file href="{replace($doc-base,'.*/([^/]+)$','$1')}" media-type="application/xhtml+xml"/>
            </xsl:if>
            <xsl:apply-templates/>
        </d:fileset>
    </xsl:template>

    <xsl:template match="*">
        <xsl:apply-templates select="node()|@*"/>
    </xsl:template>

    <xsl:template match="/processing-instruction('xml-stylesheet')">
        <xsl:variable name="href" select="replace(.,'^.*href=(&amp;apos;|&quot;)(.*?)\1.*$','$2')"/>
        <xsl:variable name="type" select="replace(.,'^.*type=(&amp;apos;|&quot;)(.*?)\1.*$','$2')"/>
        <xsl:sequence
            select="f:fileset-entry($href,
            if ($type) then $type
            else if (pf:get-extension($href)='css') then 'text/css'
            else if (pf:get-extension($href)=('xsl','xslt')) then 'application/xslt+xml'
            else '','stylesheet')"
        />
    </xsl:template>

    <xsl:template match="@aria-describedat[not(starts-with(.,'#'))]">
        <xsl:sequence select="f:fileset-entry(.,(),'description')"/>
    </xsl:template>

    <xsl:template match="link">
        <!--
            External resources: icon, prefetch, stylesheet + pronunciation
            Hyperlinks:  alternate, author, help, license, next, prev, search
        -->
        <!--Note: outbound hyperlinks that resolve outside the EPUB Container are not Publication Resources-->
        <xsl:variable name="rel" as="xs:string*" select="tokenize(@rel,'\s+')"/>
        <xsl:if test="$rel=('stylesheet','pronunciation')">
            <xsl:sequence
                select="f:fileset-entry(@href,
                if (@type) then @type
                else if (pf:get-extension(@href)='css') then 'text/css'
                else if (pf:get-extension(@href)=('xsl','xslt')) then 'application/xslt+xml'
                else if (pf:get-extension(@href)='pls') then 'application/pls+xml'
                else '',$rel)"
            />
        </xsl:if>
        <xsl:if test="$rel='stylesheet' and (@type='text/css' or pf:get-extension(@href)='css')">
            <xsl:variable name="href" select="resolve-uri(@href,base-uri(.))"/>
            <xsl:if test="unparsed-text-available($href)">
                <xsl:for-each select="pf:css-to-fileset(unparsed-text($href),$href,$context.fileset,$context.in-memory)">
                    <xsl:sequence select="f:fileset-entry(.,(),'')"/>
                </xsl:for-each>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template match="style">
        <xsl:for-each select="pf:css-to-fileset(.,$doc-base,$context.fileset,$context.in-memory)">
            <xsl:sequence select="f:fileset-entry(.,(),'')"/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="script/@src">
        <xsl:sequence
            select="f:fileset-entry(.,if (../@type) then ../@type else 'text/javascript','script')"/>
    </xsl:template>

    <xsl:template match="a/@href">
        <!--FIXME add out-of-spine local XHTML resources-->
    </xsl:template>


    <xsl:template match="img/@src">
        <xsl:if test="not(pf:get-scheme(.)='data')">
            <xsl:sequence select="f:fileset-entry(., f:image-type-from-filename(.), 'image')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="img/@longdesc[not(starts-with(.,'#'))]">
        <xsl:sequence select="f:fileset-entry(.,(),'description')"/>
    </xsl:template>

    <xsl:template match="iframe/@src">
        <!--TODO support iframe/@srcdoc -->
        <xsl:sequence select="f:fileset-entry(.,'application/xhtml+xml','content')"/>
    </xsl:template>

    <xsl:template match="embed/@src">
        <xsl:sequence select="f:fileset-entry(., ../@type, '')"/>
    </xsl:template>

    <xsl:template match="object/@data">
        <xsl:sequence
            select="f:fileset-entry(., ../@type, if (f:is-audio(., ../@type)) then 'audio'
                                                 else if (f:is-image(., ../@type)) then 'image'
                                                 else if (f:is-video(., ../@type)) then 'video'
                                                 else '')"
        />
    </xsl:template>


    <xsl:template match="audio/@src">
        <xsl:sequence select="f:fileset-entry(.,(),'audio')"/>
    </xsl:template>

    <xsl:template match="video/@src">
        <xsl:sequence select="f:fileset-entry(.,(),'video')"/>
    </xsl:template>

    <xsl:template match="source/@src">
        <xsl:sequence
            select="f:fileset-entry(., ../@type, if (../../audio) then 'audio'
                                                 else if (../../video) then 'video'
                                                 else '')"
        />
    </xsl:template>

    <xsl:template match="track/@src">
        <xsl:sequence select="f:fileset-entry(.,(),'text-track')"/>
    </xsl:template>


    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  SVG                                                                        |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->

    <!--See http://www.w3.org/TR/SVGTiny12/linking.html-->

    <xsl:template match="svg:animation/@xlink:href">
        <xsl:sequence select="f:fileset-entry(.,'application/svg+xml','image animation')"
        />
    </xsl:template>

    <xsl:template match="svg:audio/@xlink:href">
        <xsl:sequence select="f:fileset-entry(., ../@type, 'audio')"/>
    </xsl:template>

    <xsl:template match="svg:foreignObject/@xlink:href">
        <xsl:sequence select="f:fileset-entry(.,(),'')"/>
    </xsl:template>

    <xsl:template match="svg:font-face-uri/@xlink:href">
        <xsl:sequence select="f:fileset-entry(.,(),'font')"/>
    </xsl:template>

    <xsl:template match="svg:handler/@xlink:href">
        <xsl:sequence select="f:fileset-entry(., ../@type, '')"/>
    </xsl:template>

    <xsl:template match="svg:image/@xlink:href">
        <xsl:sequence select="f:fileset-entry(., ../@type, 'image')"/>
    </xsl:template>

    <xsl:template match="svg:script/@xlink:href">
        <xsl:sequence select="f:fileset-entry(., ../@type, 'script')"/>
    </xsl:template>

    <xsl:template match="svg:video/@xlink:href">
        <xsl:sequence select="f:fileset-entry(., ../@type, 'video')"/>
    </xsl:template>

    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  MathML                                                                     |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->

    <xsl:template match="m:annotation/@src">
        <xsl:sequence select="f:fileset-entry(., ../@encoding, 'description')"/>
    </xsl:template>

    <xsl:template match="m:math/@altimg">
        <xsl:sequence select="f:fileset-entry(., f:image-type-from-filename(.), 'image')"/>
    </xsl:template>

    <xsl:template match="m:mglyph/@src">
        <xsl:sequence select="f:fileset-entry(.,(),'image')"/>
    </xsl:template>

    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  Constructs a single fileset entry                                          |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->
    <xsl:function name="f:fileset-entry" as="element()?">
        <xsl:param name="uri" as="item()"/>
        <xsl:param name="type" as="xs:string?"/>
        <xsl:param name="kind" as="xs:string?"/>
        <xsl:variable name="href" select="pf:normalize-uri($uri,false())"/>
        <xsl:if test="$href and (pf:get-scheme($href)='file' or pf:is-relative($href))">
            <xsl:variable name="resolved"
                select="
                if ($uri instance of attribute()) then
                    resolve-uri($href,base-uri($uri))
                else
                    $href
                "/>
            <d:file
                href="{if (starts-with($href,'file:')) then
                           pf:relativize-uri($href,$doc-base)
                       else if (pf:is-relative($href) and  $uri instance of attribute() and base-uri($uri) ne $doc-base) then
                           pf:relativize-uri($resolved,$doc-base)
                       else
                           $href}">
                <xsl:if test="not($context.fileset//d:file[resolve-uri(@href,base-uri(.))=$resolved]
                                                          [1][not(@original-href)])">
                    <xsl:attribute name="original-href" select="$resolved"/>
                </xsl:if>
                <xsl:if test="$type">
                    <xsl:attribute name="media-type" select="$type"/>
                </xsl:if>
                <xsl:if test="$kind">
                    <xsl:attribute name="kind" select="$kind"/>
                </xsl:if>
            </d:file>
        </xsl:if>
    </xsl:function>


    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  Media Type Utils                                                           |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->
    <xsl:function name="f:is-audio" as="xs:boolean">
        <xsl:param name="uri" as="item()?"/>
        <xsl:param name="type" as="item()?"/>
        <xsl:sequence
            select="starts-with($type,'audio/')
            or pf:get-extension($uri)=('m4a','mp3','aac','aiff','ogg','wav','wvma','flac','mpa','spx','oga','3gp')"
        />
    </xsl:function>
    <xsl:function name="f:is-image" as="xs:boolean">
        <xsl:param name="uri" as="item()?"/>
        <xsl:param name="type" as="item()?"/>
        <xsl:sequence
            select="starts-with($type,'image/')
            or pf:get-extension($uri)=('jpg','jpeg','png','gif','svg')"
        />
    </xsl:function>
    <xsl:function name="f:is-video" as="xs:boolean">
        <xsl:param name="uri" as="item()?"/>
        <xsl:param name="type" as="item()?"/>
        <xsl:sequence
            select="starts-with($type,'video/')
            or pf:get-extension($uri)=('mp4','mpeg','m4v','mp4','flv','wmv','mov','ogv')"
        />
    </xsl:function>
    <xsl:function name="f:image-type-from-filename" as="xs:string?">
        <xsl:param name="src" as="item()?"/>
        <xsl:sequence select="if (matches($src,'.*\.png$','i')) then 'image/png'
                              else if (matches($src,'.*\.jpe?g$','i')) then 'image/jpeg'
                              else if (matches($src,'.*\.gif$','i')) then 'image/gif'
                              else if (matches($src,'.*\.svg$','i')) then 'image/svg+xml'
                              else ()"/>
    </xsl:function>

</xsl:stylesheet>
