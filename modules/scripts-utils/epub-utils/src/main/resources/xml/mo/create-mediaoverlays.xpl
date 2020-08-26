<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:epub3-create-mediaoverlays" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Create SMIL documents from a set of HTML documents and audio clips.</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The source fileset with HTML documents.</p>
        </p:documentation>
    </p:input>
    <p:input port="audio-map">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The d:audio-clips document from the TTS step</p>
        </p:documentation>
    </p:input>
    <p:option name="audio-dir">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Directory for the audio files.</p>
        </p:documentation>
    </p:option>
    <p:option name="flatten" select="'true'"/>
    <p:option name="anti-conflict-prefix" select="''"/>
    <p:option name="mediaoverlay-dir">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Directory for the SMIL files.</p>
        </p:documentation>
    </p:option>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The result fileset with the SMIL and audio files.</p>
        </p:documentation>
        <p:pipe step="skip-if-no-audio" port="in-memory"/>
    </p:output>
    <p:output port="original-audio.fileset">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The original audio files</p>
            <p>Copies of the audio files are added to "fileset.out". Later on, when the EPUB is
            stored, the document on this port can be passed to px:fileset-delete to clean up the
            original files (or skip it to keep the original files).</p>
        </p:documentation>
        <p:pipe step="skip-if-no-audio" port="mapping"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
            px:add-xml-base
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:audio-clips-to-fileset
            px:audio-clips-update-files
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-move
            px:fileset-load
            px:fileset-create
            px:fileset-add-entry
            px:fileset-join
        </p:documentation>
    </p:import>

    <p:choose name="skip-if-no-audio">
        <p:xpath-context>
            <p:pipe step="main" port="audio-map"/>
        </p:xpath-context>
        <p:when test="count(/d:audio-clips/*)=0">
            <p:output port="fileset" primary="true">
                <p:inline><d:fileset/></p:inline>
            </p:output>
            <p:output port="in-memory" sequence="true">
                <p:empty/>
            </p:output>
            <p:output port="mapping">
                <p:inline><d:fileset/></p:inline>
            </p:output>
            <p:sink/>
        </p:when>
        <p:otherwise>
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="smil.in-memory" port="result"/>
            </p:output>
            <p:output port="mapping">
                <p:pipe step="audio" port="mapping"/>
            </p:output>

            <!--
                FIXME: add warning if audio is not an EPUB 3 core media type
                FIXME: or better, add support for audio transcoding
            -->
            <p:documentation>Copy the audio files</p:documentation>
            <px:audio-clips-to-fileset>
                <p:input port="source">
                    <p:pipe step="main" port="audio-map"/>
                </p:input>
            </px:audio-clips-to-fileset>
            <!-- <px:fileset-filter not-media-types="audio/mpeg audio/mp4"/> -->
            <px:fileset-move name="audio">
                <p:with-option name="target" select="$audio-dir">
                    <p:empty/>
                </p:with-option>
                <p:with-option name="flatten" select="$flatten"/>
                <p:with-option name="prefix" select="$anti-conflict-prefix"/>
            </px:fileset-move>
            <p:sink/>
            <px:audio-clips-update-files name="audio-map">
                <p:input port="source">
                    <p:pipe step="main" port="audio-map"/>
                </p:input>
                <p:input port="mapping">
                    <p:pipe step="audio" port="mapping"/>
                </p:input>
            </px:audio-clips-update-files>
            <p:sink/>

            <p:documentation>Generate the SMIL files</p:documentation>
            <px:fileset-load media-types="application/xhtml+xml">
                <p:input port="fileset">
                    <p:pipe step="main" port="source.fileset"/>
                </p:input>
                <p:input port="in-memory">
                    <p:pipe step="main" port="source.in-memory"/>
                </p:input>
            </px:fileset-load>
            <p:for-each name="smil.in-memory">
                <p:output port="result" sequence="true"/>
                <p:variable name="mo-uri"
                            select="concat($mediaoverlay-dir,replace(base-uri(/*),'.*?([^/]*)\.x?html$','$1.smil'))"/>
                <p:identity name="content-doc"/>
                <p:sink/>
                <p:xslt>
                    <p:input port="source">
                        <p:pipe step="content-doc" port="result"/>
                        <p:pipe step="audio-map" port="result"/>
                    </p:input>
                    <p:input port="stylesheet">
                        <p:document href="create-mediaoverlay.xsl"/>
                    </p:input>
                    <p:with-param name="mo-dir" select="$mediaoverlay-dir"/>
                </p:xslt>
                <p:xslt>
                    <p:input port="stylesheet">
                        <p:document href="clean-mediaoverlay.xsl"/>
                    </p:input>
                    <p:input port="parameters">
                        <p:empty/>
                    </p:input>
                </p:xslt>
                <px:set-base-uri>
                    <p:with-option name="base-uri" select="$mo-uri"/>
                </px:set-base-uri>
                <px:add-xml-base root="false">
                    <!--
                        otherwise the base URI of some elements would be empty (Calabash bug?)
                    -->
                </px:add-xml-base>
            </p:for-each>

            <p:for-each>
                <p:variable name="mo-uri" select="base-uri(/*)"/>
                <px:fileset-create>
                    <p:with-option name="base" select="$mediaoverlay-dir"/>
                </px:fileset-create>
                <px:fileset-add-entry media-type="application/smil+xml">
                    <p:with-option name="href" select="$mo-uri"/>
                </px:fileset-add-entry>
            </p:for-each>
            <px:fileset-join/>
            <p:identity name="smil.fileset"/>
            <p:sink/>

            <p:documentation>Put the SMIL and audio files in a fileset</p:documentation>
            <px:fileset-join>
                <p:input port="source">
                    <p:pipe step="smil.fileset" port="result"/>
                    <p:pipe step="audio" port="result.fileset"/>
                </p:input>
            </px:fileset-join>
        </p:otherwise>
    </p:choose>

</p:declare-step>
