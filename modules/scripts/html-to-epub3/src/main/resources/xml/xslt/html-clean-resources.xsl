<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns:svg="http://www.w3.org/2000/svg"
    xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:m="http://www.w3.org/1998/Math/MathML"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0" exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    <!--    <xsl:import href="../../../../test/xspec/mock-functions.xsl"/>-->

    <xsl:output indent="yes"/>


    <!--TODO implement a custom HTML-compliant base-uri() function ?-->
    <xsl:variable name="doc-base"
        select="if (/html/head/base[@href][1]) then resolve-uri(normalize-space(/html/head/base[@href][1]/@href),base-uri(/)) else base-uri(/)"/>

    <!--A fileset is available in the default collection, to check if rsesources exist or are renamed-->
    <xsl:variable name="fileset" select="collection()[/d:fileset][1]" as="document-node()?"/>
    <xsl:key name="resources" match="/d:fileset/d:file" use="@original-href"/>


    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>


    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  External Descriptions                                                      |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->
    <xsl:template match="head">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
            <xsl:if
                test="(//@aria-describedat|//@longdesc)
                       [pf:is-relative(.) and not(starts-with(.,'#'))
                        and pf:get-extension(f:clean-uri(.,false()))=('xhtml','html','htm')]">
                <!-- http://snook.ca/archives/html_and_css/hiding-content-for-accessibility -->
                <style>
                    <xsl:sequence select="concat(
                        '&#xa;.dp2-invisible {&#xa;',
                        '  position: absolute !important;&#xa;',
                        '  height: 1px; width: 1px; &#xa;',
                        '  overflow: hidden;&#xa;',
                        '  clip: rect(1px 1px 1px 1px); /* IE6, IE7 */&#xa;',
                        '  clip: rect(1px, 1px, 1px, 1px);&#xa;',
                        '}&#xa;'
                        )"/>
                </style>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="body">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
            <!--External descriptions are copied as hidden iframes at the end of the document-->
            <xsl:apply-templates select="//@aria-describedat|//@longdesc" mode="iframe"/>
        </xsl:copy>
    </xsl:template>

    <!--copy descriptions pointing to local elements as aria-describedby-->
    <xsl:template
        match="@longdesc[starts-with(.,'#')]
              |@aria-describedat[starts-with(.,'#')]"
        priority="2">
        <xsl:attribute name="aria-describedby" select="substring-after(.,'#')"/>
    </xsl:template>

    <!--A key to the external description attributes-->
    <xsl:key name="desc" match="@aria-describedat|@longdesc" use="f:clean-uri(.,false())"/>

    <xsl:template
        match="@longdesc[pf:is-relative(.)][../empty(@aria-describedat|@aria-describedby)]">
        <xsl:variable name="desc-uri" select="f:clean-uri(.)"/>
        <xsl:variable name="desc-path" select="replace($desc-uri,'([^#]+)(#.*)?','$1')"/>
        <!--keep the longdesc if the resource exists-->
        <xsl:if test="$desc-uri">
            <xsl:attribute name="longdesc" select="f:clean-uri(.)"/>
        </xsl:if>
        <!--add an aria-describedby if the resource is HTML-->
        <xsl:if test="pf:get-extension($desc-path)=('xhtml','html','htm')">
            <xsl:attribute name="aria-describedby" select="generate-id(key('desc',$desc-path)[1])"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@aria-describedat[pf:is-absolute(.)]">
        <xsl:attribute name="longdesc" select="."/>
        <xsl:attribute name="aria-describedby" select="generate-id(.)"/>
    </xsl:template>

    <xsl:template match="@aria-describedat">
        <xsl:variable name="desc-path" select="f:clean-uri(.,false())"/>
        <xsl:choose>
            <xsl:when test="not($desc-path)"/>
            <xsl:when test="pf:get-extension($desc-path)=('xhtml','html','htm')">
                <!--connvert as aria-describedby (with hidden iframe) if the resource is HTML-->
                <xsl:attribute name="aria-describedby"
                    select="generate-id(key('desc',$desc-path)[1])"/>
            </xsl:when>
            <xsl:otherwise>
                <!--keep as-is-->
                <xsl:attribute name="aria-describedat" select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@aria-describedat[pf:is-absolute(.)]" mode="iframe">
        <aside class="dp2-invisible">
            <iframe id="{generate-id(.)}" src="{.}"/>
        </aside>
    </xsl:template>
    <xsl:template
        match="@aria-describedat[pf:is-relative(.) and not(starts-with(.,'#'))]
              |@longdesc[not(starts-with(.,'#')) and not(../(@aria-describedat|@aria-describedby))]"
        mode="iframe">
        <xsl:variable name="desc-path" select="f:clean-uri(.,false())"/>
        <xsl:variable name="is-html" as="xs:boolean"
            select="pf:get-extension($desc-path)=('xhtml','html','htm')"/>
        <xsl:if test="$is-html and key('desc',$desc-path)[1] is .">
            <aside class="dp2-invisible">
                <iframe id="{generate-id(.)}" src="{f:clean-uri(.)}"/>
            </aside>
        </xsl:if>
    </xsl:template>
    <xsl:template match="@longdesc|@aria-describedat" mode="iframe"/>



    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  Other Resources                                                            |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->


    <!--<xsl:template match="/processing-instruction('xml-stylesheet')">
        <xsl:variable name="href" select="replace(.,'^.*href=(&amp;apos;|&quot;)(.*?)\1.*$','$2')"/>
        <xsl:variable name="type" select="replace(.,'^.*type=(&amp;apos;|&quot;)(.*?)\1.*$','$2')"/>
        <!-\-TODO-\->
        <xsl:copy-of select="."/>
    </xsl:template>-->

    <xsl:template match="link">
        <!--
            External resources: icon, prefetch, stylesheet + pronunciation
            Hyperlinks:  alternate, author, help, license, next, prev, search
        -->
        <!--Note: outbound hyperlinks that resolve outside the EPUB Container are not Publication Resources-->
        <!--TODO warning for remote external resources, ignore remote hyperlinks -->
        <xsl:variable name="rel" as="xs:string*" select="tokenize(@rel,'\s+')"/>
        <xsl:choose>
            <xsl:when
                test="$rel='stylesheet' and not(@type='text/css' or pf:get-extension(@href)='css')">
                <xsl:message
                    select="concat('[WARNING] Discarding stylesheet ''',@href,''' of non-core type.')"
                />
            </xsl:when>
            <xsl:when
                test="$rel='pronunciation' and not(@type='application/pls+xml' or pf:get-extension(@href)='pls')">
                <xsl:message
                    select="concat('[WARNING] Discarding pronunciation lexicon ''',@href,''' of non-core type.')"
                />
            </xsl:when>
            <xsl:when test="pf:is-relative(@href) and not($rel=('stylesheet','pronunciation'))">
                <xsl:message
                    select="concat('[WARNING] Discarding local link ''',@href,''' of unsupported relation type ''',@rel,'''.')"
                />
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="f:copy-if-clean(@href)"/>
            </xsl:otherwise>
            <!--FIXME parse CSS-->
        </xsl:choose>
    </xsl:template>

    <!--<xsl:template match="style">
        <!-\-TODO parse refs in inlined CSS-\->
    </xsl:template>-->

    <xsl:template match="script">
        <xsl:choose>
            <xsl:when
                test="@src and not(normalize-space(@type)=('','text/javascript','text/ecmascript',
                'text/javascript1.0','text/javascript1.1','text/javascript1.2','text/javascript1.3',
                'text/javascript1.4','text/javascript1.5','text/jscript','text/livescript',
                'text/x-javascript','text/x-ecmascript','application/x-javascript',
                'application/x-ecmascript','application/javascript','application/ecmascript'))">
                <xsl:message select="'[WARNING] Discarding script of non-core type.'"/>
            </xsl:when>
            <xsl:when test="@src">
                <xsl:sequence select="f:copy-if-clean(@src)"/>
            </xsl:when>
            <xsl:otherwise>
                <script type="{if (normalize-space(@type)=('','text/javascript','text/ecmascript',
                    'text/javascript1.0','text/javascript1.1','text/javascript1.2','text/javascript1.3',
                    'text/javascript1.4','text/javascript1.5','text/jscript','text/livescript',
                    'text/x-javascript','text/x-ecmascript','application/x-javascript',
                    'application/x-ecmascript','application/javascript','application/ecmascript')) then 'text/javascript' else @type}">
                    <xsl:copy-of select="@* except @type | node()"/>
                </script>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="a[@href]">
        <xsl:choose>
            <xsl:when
                test="pf:is-relative(@href) and pf:get-path(@href) and not(pf:file-exists(pf:unescape-uri(pf:get-path(@href))))"
                use-when="function-available('pf:file-exists')">
                <xsl:message
                    select="concat('[WARNING] Discarding link to non-existing resource ''',@href,'''.')"/>
                <span>
                    <xsl:copy-of select="@* except (@href|@target|@rel|@media|@targetlang|@type)"/>
                    <xsl:apply-templates select="node()"/>
                </span>
            </xsl:when>
            <xsl:when test="false()"> </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@* | node()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="img[@src]">
        <xsl:choose>
            <xsl:when test="pf:get-scheme(@src)='data'">
                <xsl:copy-of select="."/>
            </xsl:when>
            <xsl:when test="pf:is-absolute(@src)">
                <xsl:message
                    select="concat('[WARNING] Replacing remote image ''',@src,''' by alternative text.')"/>
                <span>
                    <xsl:copy-of
                        select="@* except (@alt|@src|@crossorigin|@usemap|@ismap|@width|@height)"/>
                    <xsl:value-of select="@alt"/>
                </span>
            </xsl:when>
            <xsl:when test="not(pf:get-extension(@src)=('png','jpeg','jpg','gif','svg'))">
                <xsl:message
                    select="concat('[WARNING] The type of image ''',@src,''' is not a core EPUB media type. Replacing by alternative text.')"/>
                <span>
                    <xsl:copy-of
                        select="@* except (@alt|@src|@crossorigin|@usemap|@ismap|@width|@height)"/>
                    <xsl:value-of select="@alt"/>
                </span>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="f:copy-if-clean(@src)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="iframe[@src]">
        <xsl:choose>
            <xsl:when test="pf:is-absolute(@src)">
                <xsl:message select="concat('[WARNING] Discarding remote iframe ''',@src,'''.')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="f:copy-if-clean(@src)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="embed[@src]">
        <xsl:choose>
            <xsl:when test="pf:is-absolute(@src)">
                <xsl:message
                    select="concat('[WARNING] Discarding remote embedded resource ''',@src,'''.')"/>
            </xsl:when>
            <xsl:when test="f:is-core-audio(@src,@type)">
                <xsl:sequence select="f:copy-if-clean(@src,'audio',(@type,@width,@height))"/>
            </xsl:when>
            <xsl:when test="f:is-core-image(@src,@type)">
                <xsl:sequence select="f:copy-if-clean(@src,'img',(@type))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message
                    select="concat('[WARNING] Discarding embedded resource of non-core type ''',@src,'''.')"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="object[@data]">
        <xsl:choose>
            <xsl:when test="pf:is-absolute(@data)">
                <xsl:message select="concat('[WARNING] Discarding remote object ''',@data,'''.')"/>
            </xsl:when>
            <xsl:when
                test="f:is-core-audio(@data,@type) or f:is-core-image(@data,@type) or exists(* except param)">
                <xsl:sequence select="f:copy-if-clean(@data)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message
                    select="concat('[WARNING] Discarding object ''',@data,''' of non-core type with no fallback.')"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="audio[@src]">
        <xsl:choose>
            <xsl:when
                test="f:is-core-audio(@src,()) or exists(* except track) or normalize-space(.)">
                <xsl:sequence select="f:copy-if-clean(@src)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message
                    select="concat('[WARNING] Discarding audio resource ''',@src,''' of non-core type with no fallback.')"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="audio[source]">
        <xsl:choose>
            <xsl:when
                test="exists(source[f:is-core-audio(@src,@type)]) or exists(* except (source,track)) or normalize-space(.)">
                <xsl:copy>
                    <xsl:apply-templates select="@* | node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="'[WARNING] Discarding audio resource with no fallback.'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="video[@src]">
        <xsl:choose>
            <xsl:when test="exists(* except track) or normalize-space(.)">
                <xsl:sequence select="f:copy-if-clean(@src)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message
                    select="concat('[WARNING] Discarding video resource ''',@src,''' of non-core type with no fallback.')"
                />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="video[source]">
        <xsl:choose>
            <xsl:when test="exists(* except (source,track)) or normalize-space(.)">
                <xsl:copy>
                    <xsl:apply-templates select="@* | node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message select="'[WARNING] Discarding video resource with no fallback.'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="source">
        <xsl:sequence select="f:copy-if-clean(@src)"/>
    </xsl:template>

    <xsl:template match="track">
        <xsl:sequence select="f:copy-if-clean(@src)"/>
    </xsl:template>

    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  SVG                                                                        |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->

    <!--See http://www.idpf.org/epub/30/spec/epub30-contentdocs.html#sec-svg-restrictions-->

    <xsl:template match="svg:animate|svg:set|svg:animateMotion|svg:animatecolor">
        <xsl:message select="'[WARNING] Discarding SVG animation element.'"/>
    </xsl:template>

    <xsl:template match="svg:audio">
        <xsl:message select="'[WARNING] Discarding SVG ''audio'' element, not part of SVG 1.1'"/>
    </xsl:template>

    <xsl:template match="svg:foreignObject[@xlink:href]">
        <xsl:message
            select="'[WARNING] Discarding SVG ''foreignObject'' element with external reference, not part of SVG 1.1'"
        />
    </xsl:template>

    <xsl:template match="svg:foreignObject/@requiredExtensions">
        <xsl:attribute name="requiredExtensions" select="'http://www.idpf.org/2007/ops'"/>
    </xsl:template>

    <xsl:template match="svg:font-face-uri">

        <xsl:sequence select="f:copy-if-clean(@xlink:href)"/>

    </xsl:template>

    <xsl:template match="svg:handler">
        <xsl:message select="'[WARNING] Discarding SVG ''handler'' element, not part of SVG 1.1'"/>
    </xsl:template>

    <xsl:template match="svg:image">
        <xsl:sequence select="f:copy-if-clean(@xlink:href)"/>
    </xsl:template>

    <xsl:template match="svg:script[@xlink:href]"/>

    <xsl:template match="svg:video">
        <xsl:message select="'[WARNING] Discarding SVG ''video'' element, not part of SVG 1.1'"/>
    </xsl:template>


    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  MathML                                                                     |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->


    <xsl:template match="m:math[@altimg]">
        <xsl:variable name="clean-uri" select="f:clean-uri(@altimg)"/>
        <xsl:choose>
            <xsl:when test="$clean-uri">
                <m:math altimg="{f:clean-uri(@altimg)}">
                    <xsl:apply-templates select="@* except @altimg | node()"/>
                </m:math>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message
                    select="concat('[WARNING] Discarding missing math/@altimg ''',@altimg,'''.')"/>
                <m:math>
                    <xsl:apply-templates select="@* except @altimg | node()"/>
                </m:math>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template match="m:mglyph[@src]">
        <xsl:sequence select="f:copy-if-clean(@src)"/>
    </xsl:template>

    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  Media Type Utils                                                           |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->
    <xsl:function name="f:is-core-image" as="xs:boolean">
        <xsl:param name="uri" as="item()?"/>
        <xsl:param name="type" as="item()?"/>
        <xsl:sequence
            select="$type=('image/gif','image/jpeg','image/png','image/svg+xml') 
            or pf:get-extension($uri)=('gif','jpeg','jpg','png','svg')"
        />
    </xsl:function>
    <xsl:function name="f:is-core-audio" as="xs:boolean">
        <xsl:param name="uri" as="item()?"/>
        <xsl:param name="type" as="item()?"/>
        <xsl:sequence
            select="$type=('audio/mpeg','audio/mp4') 
            or pf:get-extension($uri)=('mp3','m4a','aac')"
        />
    </xsl:function>


    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  Resource Resolver                                                          |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->
    <xsl:function name="f:clean-uri" as="xs:string?">
        <xsl:param name="uri" as="attribute()?"/>
        <xsl:sequence select="f:clean-uri($uri,true())"/>
    </xsl:function>
    <xsl:function name="f:clean-uri" as="xs:string?">
        <xsl:param name="uri" as="attribute()?"/>
        <xsl:param name="fragment" as="xs:boolean?"/>
        <!--FIXME test with fragments, e.g. frag-only, no frag, etc-->
        <xsl:variable name="resolved"
            select="resolve-uri(pf:normalize-uri($uri,false()),base-uri($uri))"/>
        <xsl:variable name="clean-path" select="key('resources',$resolved,$fileset)/@href"/>
        <!--<xsl:message select="'=================='"/>
        <xsl:message select="concat('uri: ',$uri)"/>
        <xsl:message select="concat('resolved: ',resolve-uri(pf:normalize-uri($uri,false()),base-uri($uri)))"/>
        <xsl:message select="concat('cleanpath: ',key('resources',$resolved,$fileset)/@href)"/>
        <xsl:message select="concat('return: ',if ($clean-path) then pf:replace-path($uri,$clean-path) else ())"/>-->
        <xsl:sequence
            select="if ($clean-path) then 
                        if ($fragment) then
                            pf:replace-path($uri,$clean-path)
                        else
                            $clean-path
                        else ()"
        />
    </xsl:function>

    <!--–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––>
     |  Util Templates                                                             |
    <|–––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––––-->

    <xsl:function name="f:copy-if-clean">
        <xsl:param name="att" as="attribute()?"/>
        <xsl:sequence select="f:copy-if-clean($att,(),())"/>
    </xsl:function>
    <xsl:function name="f:copy-if-clean">
        <xsl:param name="att" as="attribute()?"/>
        <xsl:param name="elm-name" as="xs:string?"/>
        <xsl:param name="skip-att" as="attribute()*"/>

        <xsl:variable name="elm" as="element()?" select="$att/.."/>
        <xsl:variable name="clean-uri" as="xs:string?" select="f:clean-uri($att)"/>
        <xsl:choose>
            <xsl:when test="$clean-uri">
                <xsl:element name="{if($elm-name) then $elm-name else $elm/name()}">
                    <xsl:attribute name="{$att/name()}" select="$clean-uri"/>
                    <xsl:apply-templates select="$elm/@* except ($att,$skip-att) | $elm/node()"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message
                    select="concat('[WARNING] Discarding missing ',$elm/name(),' ''',$att,'''.')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>
</xsl:stylesheet>
