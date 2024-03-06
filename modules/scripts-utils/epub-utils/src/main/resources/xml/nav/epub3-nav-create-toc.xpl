<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                type="px:epub3-create-toc" name="main">

    <p:input port="source" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The content documents</p>
        </p:documentation>
    </p:input>
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The generated table of contents as a <code>nav</code> document with
            <code>epub:type="toc"</code> and <code>role="doc-toc"</code>.</p>
        </p:documentation>
        <p:pipe port="result" step="result"/>
    </p:output>
    <p:output port="content-docs" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The modified content documents. All <code>body</code>, <code>article</code>,
            <code>aside</code>, <code>nav</code>, <code>section</code>, <code>h1</code>,
            <code>h2</code>, <code>h3</code>, <code>h4</code>, <code>h5</code>, <code>h6</code> and
            <code>hgroup</code> elements have an <code>id</code> attribute.</p>
        </p:documentation>
        <p:pipe step="tocs" port="content-doc"/>
    </p:output>
    <p:option name="output-base-uri">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The base URI of the resulting document.</p>
        </p:documentation>
    </p:option>

    <!--TODO honnor the 'untitled' option-->
    <p:option name="untitled" select="'unwrap'">
        <!-- "unwrap" (default) | "include" | "exclude" | "hide" -->
    </p:option>
    <!--TODO honnor the 'sidebar' option-->
    <p:option name="sidebars" select="'exclude'">
        <!-- "include" | "exclude" (default) | "hide" -->
    </p:option>
    <!--TODO honnor the 'visible-depth' option-->
    <p:option name="visible-depth" select="-1">
        <!-- integer -->
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:i18n-translate
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-outline
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    
    <p:for-each name="tocs">
        <p:output port="result" sequence="true" primary="true"/>
        <p:output port="content-doc" sequence="true">
            <p:pipe step="outline" port="result"/>
        </p:output>
        <!-- create an ordered list (ol) from each xhtml document -->
        <px:html-outline name="outline">
            <p:with-option name="toc-output-base-uri" select="$output-base-uri">
                <p:empty/>
            </p:with-option>
            <p:with-option name="fix-untitled-sections-in-outline"
                           select="if ($untitled='unwrap') then 'unwrap' else 'imply-heading'">
                <p:empty/>
            </p:with-option>
        </px:html-outline>
        <p:sink/>
        <p:filter select="/h:ol/h:li">
            <p:input port="source">
                <p:pipe step="outline" port="toc"/>
            </p:input>
        </p:filter>
    </p:for-each>
    <p:sink/>

    <p:insert match="/h:nav/h:ol" position="first-child">
        <!-- Prepare the table of content -->
        <p:input port="source">
            <p:inline exclude-inline-prefixes="#all">
                <nav epub:type="toc" role="doc-toc" xmlns="http://www.w3.org/1999/xhtml">
                    <h1>Table of contents</h1>
                    <ol/>
                </nav>
            </p:inline>
        </p:input>
        <!-- Insert the result of previous step in the inlined "ol" -->
        <p:input port="insertion">
            <p:pipe step="tocs" port="result"/>
        </p:input>
    </p:insert>

    <!-- Translate "Table of contents" -->
    <p:replace match="//h:nav[@epub:type='toc']/h:h1/text()">
        <p:input port="replacement">
            <p:pipe port="result" step="toc-string"/>
        </p:input>
    </p:replace>

    <!-- unwrap the tocs titles subnodes (replace by their children) -->
    <p:unwrap match="//h:nav[@epub:type='toc']/h:h1/*"/>

    <!--TODO better handling of duplicate IDs-->
    <p:delete match="@xml:id|@id"/>
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
    <px:i18n-translate name="toc-string" string="Table of contents">
        <p:with-option name="language" select="( /*/h:html/@lang , /*/h:html/@xml:lang , /*/h:html/h:head/h:meta[matches(lower-case(@name),'^(.*[:\.])?language$')]/@content , 'en' )[1]"/>
        <p:input port="maps">
            <p:document href="i18n.xml"/>
        </p:input>
    </px:i18n-translate>
    <p:sink/>

</p:declare-step>
