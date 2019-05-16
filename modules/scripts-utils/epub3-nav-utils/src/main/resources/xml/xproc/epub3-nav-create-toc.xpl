<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-nav-create-toc" name="main" xmlns:h="http://www.w3.org/1999/xhtml" xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" version="1.0"
    xmlns:epub="http://www.idpf.org/2007/ops">

    <p:input port="source" sequence="true"/>
    <p:output port="result">
        <p:pipe port="result" step="result"/>
    </p:output>
    <p:option name="base-dir" select="''"/>

    <!--TODO honnor the 'untitled' option-->
    <p:option name="untitled" select="'unwrap'"/>
    <!-- "unwrap" (default) | "include" | "exclude" | "hide" -->
    <!--TODO honnor the 'sidebar' option-->
    <p:option name="sidebars" select="'exclude'"/>
    <!-- "include" | "exclude" (default) | "hide" -->
    <!--TODO honnor the 'visible-depth' option-->
    <p:option name="visible-depth" select="-1"/>
    <!-- integer -->

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl"/>

    
    <!-- create an ordered list (ol) from an xhtml document -->
    <p:for-each name="tocs">
        <p:output port="result" sequence="true"/>
        <p:variable name="base-uri" select="p:base-uri(/*)"/>
        <p:variable name="base-ref" select="if (starts-with($base-uri,$base-dir)) 
                    then substring-after($base-uri,$base-dir) 
                    else $base-uri"/>
        
        <px:html-outline>
            <p:with-option name="file-uri" select="$base-ref"/>
        </px:html-outline>
        
        <p:filter select="/h:ol/h:li"/>
    </p:for-each>

    <p:insert match="/h:nav/h:ol" position="first-child">
        <!-- Prepare the table of content -->
        <p:input port="source">
            <p:inline exclude-inline-prefixes="#all">
                <nav epub:type="toc" xmlns="http://www.w3.org/1999/xhtml">
                    <h1>Table of contents</h1>
                    <ol/>
                </nav>
            </p:inline>
        </p:input>
        <!-- Insert the result of previous step in the inlined "ol" -->
        <p:input port="insertion">
            <p:pipe port="result" step="tocs"/>
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

    <!-- Navigation correction -->
    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="../xslt/nav-fixer.xsl"/>
        </p:input>
        <p:with-param name="untitled" select="$untitled"/>
    </p:xslt>

    <!--TODO better handling of duplicate IDs-->
    <p:delete match="@xml:id|@id"/>
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
            <p:document href="../i18n.xml"/>
        </p:input>
    </px:i18n-translate>
    <p:sink/>

</p:declare-step>
