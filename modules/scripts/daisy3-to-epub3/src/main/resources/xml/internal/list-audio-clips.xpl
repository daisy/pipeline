<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="pxi:list-audio-clips" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-epub3" version="1.0">

    <p:input port="fileset.in" primary="true"/>
    <p:input port="dtbooks" sequence="true"/>
    <p:input port="smils" sequence="true"/>

    <p:option name="content-dir" required="true"/>

    <p:output port="fileset.out" primary="true"/>
    <p:output port="audio-clips" sequence="true">
        <p:pipe port="result" step="audio-clips"/>
    </p:output>


    <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>

    <!--TODO add support for audio transcoding-->
    <!--TODO filter unused audio files-->
    <!--FIXME add warning if audio is not an EPUB 3 core media type-->
    <p:group name="audio-fileset">
        <p:output port="result"/>
        <!--filter the non-audio files-->
        <p:delete match="d:file[not(@media-type=('audio/mpeg','audio/mp4'))]"/>
        <!--normalizes the file set-->
        <px:fileset-join name="test"/>
        <!--keep the original base URI (used later ot relativize the audio refs)-->
        <p:add-attribute attribute-name="original-base" match="d:fileset">
            <p:with-option name="attribute-value" select="base-uri(/*)"/>
        </p:add-attribute>
        <!--keep the original URI-->
        <p:viewport match="d:file">
            <p:add-attribute attribute-name="original-href" match="d:file">
                <p:with-option name="attribute-value" select="resolve-uri(/*/@href,base-uri(/*))"/>
            </p:add-attribute>
        </p:viewport>
        <!--re-base the file set-->
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="concat($content-dir,'audio/')"/>
        </p:add-attribute>
    </p:group>

    <!--create clip list-->
    <p:xslt template-name="create-map" name="audio-clips">
        <p:input port="source">
            <p:pipe port="dtbooks" step="main"/>
            <p:pipe port="smils" step="main"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="list-audio-clips.xsl"/>
        </p:input>
        <p:with-param name="audio-base" select="/d:fileset/@original-base"/>
    </p:xslt>

    <p:delete match="/d:fileset/@riginal-base">
        <p:input port="source">
            <p:pipe port="result" step="audio-fileset"/>
        </p:input>
    </p:delete>

</p:declare-step>
