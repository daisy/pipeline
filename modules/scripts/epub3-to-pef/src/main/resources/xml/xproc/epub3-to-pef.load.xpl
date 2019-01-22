<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-pef.load" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:output port="fileset.out" primary="true"/>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe step="load" port="result.in-memory"/>
    </p:output>
    
    <p:option name="epub" required="true" px:media-type="application/epub+zip application/oebps-package+xml"/>
    <p:option name="temp-dir" required="true">
        <p:documentation>Empty temporary directory dedicated to this step.</p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl"/>
    
    <px:epub3-load name="load" px:progress="1">
        <p:with-option name="href" select="$epub"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
    </px:epub3-load>
    
</p:declare-step>
