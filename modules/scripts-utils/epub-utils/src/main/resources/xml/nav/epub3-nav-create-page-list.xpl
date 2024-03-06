<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:epub="http://www.idpf.org/2007/ops"
                type="px:epub3-create-page-list" name="main">

    <p:input port="source" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The content documents</p>
        </p:documentation>
    </p:input>
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The generated page list as a <code>nav</code> document with
            <code>epub:type="page-list"</code> and <code>role="doc-pagelist"</code>.</p>
        </p:documentation>
        <p:pipe port="result" step="result"/>
    </p:output>
    <p:output port="content-docs" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The modified content documents. All <code>epub:type='pagebreak'</code> and
            <code>role='doc-pagebreak'</code> elements have an <code>id</code> attribute.</p>
        </p:documentation>
        <p:pipe step="page-lists" port="content-docs"/>
    </p:output>
    <p:option name="hidden" select="'true'"/>
    <p:option name="output-base-uri" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The base URI of the resulting document.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:i18n-translate
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-add-ids
        </p:documentation>
    </p:import>
    
    <p:for-each name="page-lists">
        <p:output port="result" primary="true"/>
        <p:output port="content-docs">
            <p:pipe step="add-ids" port="result"/>
        </p:output>
        <p:documentation>Add ID attributes</p:documentation>
        <px:html-add-ids match="*[tokenize(@epub:type,'\s+')='pagebreak' or @role='doc-pagebreak']" name="add-ids"/>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="html5-to-page-list.xsl"/>
            </p:input>
            <p:with-param name="output-base-uri" select="$output-base-uri"/>
            <p:with-option name="output-base-uri" select="$output-base-uri"/>
        </p:xslt>
        <p:filter select="(//h:ol)[1]"/>
    </p:for-each>

    <p:insert match="/h:nav/h:ol" position="first-child">
        <p:input port="source">
            <p:inline exclude-inline-prefixes="#all">
                <nav epub:type="page-list" role="doc-pagelist" xmlns="http://www.w3.org/1999/xhtml">
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
    <px:set-base-uri>
        <p:with-option name="base-uri" select="$output-base-uri"/>
    </px:set-base-uri>
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
            <p:document href="i18n.xml"/>
        </p:input>
    </px:i18n-translate>
    <p:sink/>

</p:declare-step>
