<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/"
                xpath-default-namespace="http://www.w3.org/2001/SMIL20/"
                exclude-result-prefixes="#all">

    <xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

    <xsl:variable name="test-note-ids"
        select="collection()/ncx:ncx/ncx:head/ncx:smilCustomTest[@bookStruct='NOTE']/string(@id)" as="xs:string*"/>
    <xsl:variable name="test-noteref-ids"
        select="collection()/ncx:ncx/ncx:head/ncx:smilCustomTest[@bookStruct='NOTE_REFERENCE']/string(@id)"
        as="xs:string*"/>
    <xsl:variable name="test-page-ids"
        select="collection()/ncx:ncx/ncx:head/ncx:smilCustomTest[@bookStruct='PAGE_NUMBER']/string(@id)"
        as="xs:string*"/>
    <xsl:variable name="test-prodnote-ids"
        select="collection()/ncx:ncx/ncx:head/ncx:smilCustomTest[@bookStruct='OPTIONAL_PRODUCER_NOTE']/string(@id)"
        as="xs:string*"/>
    <xsl:variable name="test-sidebar-ids"
        select="collection()/ncx:ncx/ncx:head/ncx:smilCustomTest[@bookStruct='OPTIONAL_SIDEBAR']/string(@id)"
        as="xs:string*"/>
    <xsl:variable name="ncx-idrefs" as="document-node()">
        <!--
            Get the ID references to a SMIL document from the NCX. The result has the form:

            <d:idrefs xmlns:d="http://www.daisy.org/ns/pipeline/data">
               <d:idref id="navPoint_001" idref="par_0001" navmap="true"/>
               <d:idref id="pageTarget_001" idref="par_0003"/>
               <d:idref id="navTarget_001" idref="par_0002"/>
               ...
            </d:idrefs>

            These are used as fallback if a text elements are missing in the input (if part of audio-only DAISY 3)
        -->
        <xsl:document>
            <d:idrefs>
                <xsl:if test="collection()/ncx:ncx">
                    <xsl:variable name="smil-relative-uri"
                                  select="pf:relativize-uri(base-uri(collection()[1]/*), base-uri(collection()/ncx:ncx))"/>
                    <xsl:for-each select="collection()/ncx:ncx//ncx:content[substring-before(@src,'#')=$smil-relative-uri]">
                        <d:idref idref="{substring-after(@src,'#')}"
                                 id="{../@id}">
                            <xsl:if test="exists(ancestor::ncx:navMap)">
                                <xsl:attribute name="navmap" select="'true'"/>
                            </xsl:if>
                        </d:idref>
                    </xsl:for-each>
                </xsl:if>
            </d:idrefs>
        </xsl:document>
    </xsl:variable>
    <xsl:variable name="ncx-relative-uri" as="xs:string?"
                  select="collection()/ncx:ncx/pf:relativize-uri(base-uri(.), base-uri(collection()[1]/*))"/>

    <xsl:key name="ncx-idrefs" match="d:idref" use="@idref"/>
    <xsl:key name="ids" match="*" use="@id"/>

    <xsl:template match="body" priority="1">
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'id_'"/>
            <xsl:with-param name="for-elements" select="//audio[not(@id)]|
                                                        //par[empty(text)]|
                                                        //seq[par]"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="text()"/>

    <xsl:template match="/*">
        <smil>
            <head>
                <meta name="dc:format" content="Daisy 2.02"/>
                <meta name="ncc:generator" content="DAISY Pipeline 2"/>
                <xsl:apply-templates select="/smil/head/meta"/>
                <!--ncc:timeInThisSmil
                content: Sum of SMIL time in current SMIL document
                scheme: SMIL clock; recommended syntax: "hh:mm:ss"
                occurrence: optional - recommended-->
                <meta name="ncc:timeInThisSmil" content="{//body[1]/seq[1]/@dur}"/>
                <layout>
                    <region id="txtView"/>
                </layout>
            </head>
            <body>
                <!--FIXME calculate the effective total time-->
                <!--FIXME support other SMIL clock values -->
                <seq dur="{//body[1]/seq[1]/@dur}">
                    <xsl:apply-templates select="//body"/>
                </seq>
            </body>
        </smil>
    </xsl:template>
    
    <xsl:template match="meta[@name='dtb:totalElapsedTime']">
        <meta name="ncc:totalElapsedTime" content="{@content}"/>
    </xsl:template>
    <xsl:template match="meta[@name='dtb:uid']">
        <meta name="dc:identifier" content="{@content}"/>
    </xsl:template>

    <xsl:template name="add-text-ref">
        <xsl:if test="exists($ncx-relative-uri)">
            <xsl:variable name="ncc-ref" as="xs:string?" select="f:get-ncc-ref(.)"/>
            <xsl:if test="exists($ncc-ref)">
                <text src="{concat($ncx-relative-uri,'#',$ncc-ref)}">
                    <xsl:call-template name="pf:generate-id"/>
                </text>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    <xsl:template match="par[empty(text)]" mode="media">
        <xsl:call-template name="add-text-ref"/>
        <xsl:next-match/>
    </xsl:template>
    <xsl:template match="par" mode="media">
        <xsl:apply-templates mode="#current"/>
    </xsl:template>

    <xsl:template match="audio" mode="media">
        <audio src="{@src}" clip-begin="{@clipBegin}" clip-end="{@clipEnd}">
            <xsl:sequence select="@id"/>
            <xsl:if test="not(exists(@id))">
                <xsl:call-template name="pf:generate-id"/>
            </xsl:if>
        </audio>
    </xsl:template>

    <xsl:template match="seq[count(audio)>1]" mode="media">
        <seq>
            <xsl:copy-of select="@dur|@id"/>
            <xsl:apply-templates mode="#current"/>
        </seq>
    </xsl:template>
    <xsl:template match="seq[par]" mode="media">
        <xsl:call-template name="add-text-ref"/>
        <seq>
            <xsl:copy-of select="@dur|@id"/>
            <xsl:apply-templates select=".//audio" mode="#current"/>
        </seq>
    </xsl:template>

    <!--TODO uncomment when we add support for audio+text DAISY 3-->
    <!--<xsl:template match="s:text" mode="media">
        <text id="{if(@id) then @id else generate-id()}" src="{@src}"/>
    </xsl:template>-->


    <xsl:template match="par[f:system-required(.)]">
        <par endsync="last" system-required="{f:system-required(.)}">
            <xsl:copy-of select="@id"/>
            <xsl:apply-templates select="." mode="media"/>
        </par>
    </xsl:template>
    
    <xsl:template match="par[f:is-noteref(.) and f:is-note(following::*[1])]">
        <seq>
            <!-- if the customTest is on the parent seq, copy this parent ID -->
            <xsl:if test="not(@customTest)">
                <xsl:copy-of select="ancestor::seq[1][@customTest]/@id"/>
            </xsl:if>
            <xsl:comment>Note reference</xsl:comment>
            <xsl:next-match/>
            <xsl:comment>Note body</xsl:comment>
            <par endsync="last" system-required="footnote-on">
                <xsl:copy-of select="following::par[1]/@id"/>
                <xsl:apply-templates select="following::*[1]" mode="media"/>
            </par>
        </seq>
    </xsl:template>

    <xsl:template match="par">
        <par endsync="last">
            <xsl:copy-of select="@id"/>
            <xsl:apply-templates select="." mode="media"/>
        </par>
    </xsl:template>

    <xsl:template
        match="seq[f:is-note(.) and f:is-noteref(preceding::par[not(f:is-note(.))][1])]
              |par[f:is-note(.) and f:is-noteref(preceding::par[not(f:is-note(.))][1])]"/>

    <xsl:template
        match="seq[not(parent::body)][count(*)=2 and not(f:is-note(*[1])) and f:is-note(*[2])]">
        <seq>
            <xsl:copy-of select="@id|@dur"/>
            <xsl:apply-templates select="*[1]"/>
            <xsl:apply-templates select="*[2]"/>
        </seq>
    </xsl:template>

    <xsl:function name="f:get-customtest" as="attribute()?">
        <xsl:param name="elem" as="element()?"/>
        <xsl:sequence
            select="
            $elem/(@customTest,
            self::seq[count(*)=1]/par/@customTest,
            self::par/ancestor::seq[1]/@customTest)[1]"
        />
    </xsl:function>

    <xsl:function name="f:is-note" as="xs:boolean">
        <xsl:param name="elem" as="node()?"/>
        <xsl:sequence select="f:get-customtest($elem) = $test-note-ids"/>
    </xsl:function>

    <xsl:function name="f:is-noteref" as="xs:boolean">
        <xsl:param name="elem" as="element()?"/>
        <xsl:sequence select="f:get-customtest($elem) = $test-noteref-ids"/>
    </xsl:function>

    <xsl:function name="f:system-required" as="xs:string?">
        <xsl:param name="elem" as="element()?"/>
        <xsl:variable name="customTest" select="f:get-customtest($elem)"/>
        <xsl:sequence
            select="
            if ($test-page-ids=$customTest) then 'pagenumber-on'
            else if ($test-note-ids=$customTest) then 'footnote-on'
            else if ($test-prodnote-ids=$customTest) then 'prodnote-on'
            else if ($test-sidebar-ids=$customTest) then 'sidebar-on'
            else ()
            "
        />
    </xsl:function>

    <xsl:function name="f:get-ncc-ref" as="xs:string?">
        <xsl:param name="par" as="node()?"/>
        <!--
        ## text backlinks
        
        when creating a `par` in the output tree:
          - if the `@id` is referenced in the NCX, add the back-link
          - if a parent's `@id` is referenced in the NCX, add the back-link
          - if no ref is found in the NCX, back-link to the current item from the `navMap`.
          
        how to look-up back-link IDs:
          - smil@id -> ncx@id map
          - find 1st @id used in navMap whose carrier precedes this
          
        Note: back-link IDs are copied from NCX to the NCC
        -->
        <xsl:variable name="this-or-parent-id"
            select="
            $par/ancestor-or-self::*[@id and exists(key('ncx-idrefs',@id,$ncx-idrefs))][1]/@id"/>

        <xsl:sequence
            select="(
            if ($this-or-parent-id) then key('ncx-idrefs',$this-or-parent-id,$ncx-idrefs)/@id
            else $ncx-idrefs//d:idref[@navmap][for $idref in @idref return $par/(.>>./key('ids',$idref))]/@id
            )[1]"
        />
    </xsl:function>

</xsl:stylesheet>
