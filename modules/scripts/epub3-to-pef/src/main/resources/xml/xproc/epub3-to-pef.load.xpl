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
    <p:output port="opf">
        <p:pipe step="opf" port="result"/>
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
    <p:choose name="result" px:progress="1">
        <p:when test="ends-with(lower-case($epub),'.epub')"
                px:message="EPUB is in a ZIP container; unzipping">
            <p:output port="fileset.out" primary="true">
                <p:pipe step="mediatype" port="result"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe step="load" port="result"/>
            </p:output>
            
            <px:fileset-unzip store-to-disk="true" name="unzip">
                <p:with-option name="href" select="$epub"/>
                <p:with-option name="unzipped-basedir" select="concat($temp-dir,'epub/')"/>
            </px:fileset-unzip>
            <p:sink/>
            <px:mediatype-detect name="mediatype">
                <p:input port="source">
                    <p:pipe step="unzip" port="fileset"/>
                </p:input>
            </px:mediatype-detect>
            <px:fileset-load name="load">
                <p:input port="in-memory">
                    <p:empty/>
                </p:input>
            </px:fileset-load>
        </p:when>
        <p:otherwise px:message="EPUB is not in a container">
            <p:output port="fileset.out" primary="true">
                <p:pipe port="result" step="load.fileset"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="result" step="load.in-memory"/>
            </p:output>
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
    
    <!-- Get the OPF so that we can use the metadata in options -->
    <px:message message="Getting the OPF"/>
    <px:fileset-load media-types="application/oebps-package+xml">
        <p:input port="in-memory">
            <p:pipe step="result" port="in-memory.out"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="opf"/>
    
</p:declare-step>
