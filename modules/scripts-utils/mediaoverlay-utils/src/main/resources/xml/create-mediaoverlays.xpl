<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="px:create-mediaoverlays" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    version="1.0">

    <p:input port="content-docs" primary="true" sequence="true"/>
    <p:input port="audio-map"/>
    <p:option name="content-dir"/>

    <p:output port="fileset.out" primary="true"/>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="result" step="media-overlays"/>
    </p:output>

    <p:variable name="audio-dir" select="concat($content-dir,'audio/')">
        <p:empty/>
    </p:variable>
    <p:variable name="mo-dir" select="concat($content-dir,'mo/')">
        <p:empty/>
    </p:variable>

    <p:for-each name="media-overlays">
        <p:output port="result"/>
        <p:variable name="mo-uri"
            select="concat($mo-dir,replace(base-uri(/*),'.*?([^/]*)\.x?html$','$1.smil'))"/>
        <p:identity name="content-doc"/>

        <p:xslt>
            <p:with-option name="output-base-uri" select="$mo-uri"/>
            <p:input port="source">
                <p:pipe port="result" step="content-doc"/>
                <p:pipe port="audio-map" step="main"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="create-mediaoverlay.xsl"/>
            </p:input>
            <p:with-param name="mo-dir" select="$mo-dir"/>
            <p:with-param name="audio-dir" select="$audio-dir"/>
        </p:xslt>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="clean-mediaoverlay.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <!--Hack to set the base URI-->
        <p:add-attribute attribute-name="xml:base" match="/*">
            <p:with-option name="attribute-value" select="$mo-uri"/>
        </p:add-attribute>
        <p:delete match="/*/@xml:base"/>
    </p:for-each>

    <p:group name="fileset">
        <p:for-each>
            <p:output port="result" sequence="true"/>
            <p:variable name="mo-uri" select="base-uri(/*)"/>
            <px:fileset-create>
                <p:with-option name="base" select="$mo-dir"/>
            </px:fileset-create>
            <px:fileset-add-entry media-type="application/smil+xml">
                <p:with-option name="href" select="$mo-uri"/>
            </px:fileset-add-entry>
        </p:for-each>
        <px:fileset-join name="manifest"/>
    </p:group>

</p:declare-step>
