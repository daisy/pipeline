<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" type="px:epub3-nav-create-page-list" xmlns:h="http://www.w3.org/1999/xhtml" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" version="1.0"
    xmlns:epub="http://www.idpf.org/2007/ops" name="main">


    <p:input port="source" sequence="true"/>
    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>
    <p:option name="hidden" select="'true'"/>
    <p:option name="base-dir" select="''"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <p:for-each name="page-lists">
        <p:output port="result"/>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="../xslt/html5-to-page-list.xsl"/>
            </p:input>
            <p:with-param name="base-uri" select="if ($base-dir='') then base-uri(/*) else $base-dir"/>
        </p:xslt>
        <p:filter select="(//h:ol)[1]"/>
    </p:for-each>

    <p:insert match="/h:nav/h:ol" position="first-child">
        <p:input port="source">
            <p:inline exclude-inline-prefixes="#all">
                <nav epub:type="page-list" xmlns="http://www.w3.org/1999/xhtml">
                    <h1>List of pages</h1>
                    <ol/>
                </nav>
            </p:inline>
        </p:input>
        <p:input port="insertion">
            <p:pipe port="result" step="page-lists"/>
        </p:input>
    </p:insert>

    <!-- Translate "List of pages" -->
    <p:replace match="//h:nav[@epub:type='page-list']/h:h1/text()">
        <p:input port="replacement">
            <p:pipe port="result" step="lop-string"/>
        </p:input>
    </p:replace>
    <p:unwrap match="//h:nav[@epub:type='page-list']/h:h1/*"/>

    <p:unwrap match="/h:nav/h:ol/h:ol"/>

    <!--TODO better handling of duplicate IDs-->
    <p:delete match="@xml:id|@id"/>

    <p:choose>
        <p:when test="$hidden='true'">
            <p:add-attribute attribute-name="hidden" attribute-value="" match="/h:nav"/>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:identity name="result"/>
    <p:sink/>

    <p:wrap-sequence wrapper="wrapper">
        <p:input port="source">
            <p:pipe port="source" step="main"/>
        </p:input>
    </p:wrap-sequence>
    <px:i18n-translate name="lop-string" string="List of pages">
        <p:with-option name="language" select="( /*/h:html/@lang , /*/h:html/@xml:lang , /*/h:html/h:head/h:meta[matches(lower-case(@name),'^(.*[:\.])?language$')]/@content , 'en' )[1]"/>
        <p:input port="maps">
            <p:document href="../i18n.xml"/>
        </p:input>
    </px:i18n-translate>
    <p:sink/>

</p:declare-step>
