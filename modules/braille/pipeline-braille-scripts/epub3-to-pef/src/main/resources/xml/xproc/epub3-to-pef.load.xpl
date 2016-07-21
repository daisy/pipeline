<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-pef.load" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:output port="fileset.out" primary="true">
        <p:pipe port="fileset.out" step="result"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="in-memory.out" step="result"/>
    </p:output>
    
    <p:option name="epub" required="true" px:media-type="application/epub+zip application/oebps-package+xml"/>
    <!-- Empty temporary directory dedicated to this conversion -->
    <p:option name="temp-dir" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>
    
    <!--
        Until v1.10 of DP2 is released, we cannot point into ZIP files using URIs.
        So for now we unzip the entire EPUB before continuing.
        See: https://github.com/daisy/pipeline-modules-common/pull/73
    -->
    <p:choose name="result">
        <p:when test="ends-with(lower-case($epub),'.epub')">
            <p:output port="fileset.out" primary="true"/>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="in-memory.out" step="unzip"/>
            </p:output>
            
            <px:message severity="DEBUG" message="EPUB is in a ZIP container; unzipping"/>
            <px:unzip-fileset name="unzip">
                <p:with-option name="href" select="$epub"/>
                <p:with-option name="unzipped-basedir" select="concat($temp-dir,'epub/')"/>
            </px:unzip-fileset>
            <px:fileset-store name="load.stored">
                <p:input port="fileset.in">
                    <p:pipe port="fileset.out" step="unzip"/>
                </p:input>
                <p:input port="in-memory.in">
                    <p:pipe port="in-memory.out" step="unzip"/>
                </p:input>
            </px:fileset-store>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="fileset.out" step="load.stored"/>
                </p:input>
            </p:identity>
            <p:viewport match="/*/d:file">
                <p:add-attribute match="/*" attribute-name="original-href">
                    <p:with-option name="attribute-value" select="resolve-uri(/*/@href,base-uri())"/>
                </p:add-attribute>
            </p:viewport>
            <px:mediatype-detect/>
            
        </p:when>
        <p:otherwise>
            <p:output port="fileset.out" primary="true">
                <p:pipe port="result" step="load.fileset"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="result" step="load.in-memory"/>
            </p:output>
            
            <px:message message="EPUB is not in a container"/>
            <px:fileset-create>
                <p:with-option name="base" select="replace($epub,'(.*/)([^/]*)','$1')"/>
            </px:fileset-create>
            <px:fileset-add-entry media-type="application/oebps-package+xml">
                <p:with-option name="href" select="replace($epub,'(.*/)([^/]*)','$2')"/>
                <p:with-option name="original-href" select="$epub"/>
            </px:fileset-add-entry>
            <px:mediatype-detect/>
            <p:identity name="load.fileset"/>
            
            <px:fileset-load>
                <p:input port="in-memory">
                    <p:empty/>
                </p:input>
            </px:fileset-load>
            <p:identity name="load.in-memory"/>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
