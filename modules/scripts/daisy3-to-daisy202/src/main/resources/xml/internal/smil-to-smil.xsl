<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/"
    xpath-default-namespace="http://www.w3.org/2001/SMIL20/" exclude-result-prefixes="#all"
    version="2.0">


    <xsl:output indent="yes"/>
    <xsl:param name="ncc-uri" select="'ncc.html'"/>

    <!--<xsl:variable name="test-note-ids"
        select="collection()/ncx:smilCustomTest[@bookStruct='NOTE']/string(@id)" as="xs:string*"/>
    <xsl:variable name="test-noteref-ids"
        select="collection()/ncx:smilCustomTest[@bookStruct='NOTE_REFERENCE']/string(@id)"
        as="xs:string*"/>
    <xsl:variable name="test-page-ids"
        select="collection()/ncx:smilCustomTest[@bookStruct='PAGE_NUMBER']/string(@id)"
        as="xs:string*"/>
    <xsl:variable name="test-prodnote-ids"
        select="collection()/ncx:smilCustomTest[@bookStruct='OPTIONAL_PRODUCER_NOTE']/string(@id)"
        as="xs:string*"/>
    <xsl:variable name="test-sidebar-ids"
        select="collection()/ncx:smilCustomTest[@bookStruct='OPTIONAL_SIDEBAR']/string(@id)"
        as="xs:string*"/>
    <xsl:variable name="ncx-idrefs" as="document-node()" select="collection()[d:doc]"/>-->
    <xsl:variable name="ncx-idrefs" as="document-node()">
        <xsl:document>
            <d:doc>
                <d:idref idref="bagw_0015" id="bagw_0015" navmap="true"/>
                <d:idref idref="bagw_0017" id="bagw_0017"/>
                <d:idref idref="bagw_0016" id="noteref-1"/>
            </d:doc>
        </xsl:document>
    </xsl:variable>
    <xsl:variable name="test-note-ids" select="('note','footnote')" as="xs:string*"/>
    <xsl:variable name="test-noteref-ids" select="'noteref'" as="xs:string*"/>
    <xsl:variable name="test-page-ids" select="'pagenumber'" as="xs:string*"/>
    <xsl:variable name="test-prodnote-ids" select="'prodnote'" as="xs:string*"/>
    <xsl:variable name="test-sidebar-ids" select="'sidebar'" as="xs:string*"/>

    <xsl:key name="ncx-idrefs" match="d:idref" use="@idref"/>
    <xsl:key name="ids" match="*" use="@id"/>

    <xsl:template match="text()"/>

    <xsl:template match="/">
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
        <text id="{generate-id()}" src="{concat($ncc-uri,'#',f:get-ncc-ref(.))}"/>
    </xsl:template>
    <xsl:template match="par[empty(text)]" mode="media">
        <xsl:call-template name="add-text-ref"/>
        <xsl:next-match/>
    </xsl:template>
    <xsl:template match="par" mode="media">
        <xsl:apply-templates mode="#current"/>
    </xsl:template>

    <xsl:template match="audio" mode="media">
        <audio id="{if(@id) then @id else generate-id()}" src="{@src}" clip-begin="{@clipBegin}"
            clip-end="{@clipEnd}"/>
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
            select="
            if ($this-or-parent-id) then key('ncx-idrefs',$this-or-parent-id,$ncx-idrefs)/@id
            else $ncx-idrefs//d:idref[@navmap][for $idref in @idref return $par/(.>>./key('ids',$idref))]/@id
            "
        />
    </xsl:function>

</xsl:stylesheet>
