<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="pxi:daisy202-to-epub3-resources"
                name="resources">

    <p:documentation>
        <h1 px:role="desc">Copy the auxiliary resources from the DAISY 2.02 fileset to the EPUB 3 fileset, and return a manifest of all the resulting files.</h1>
    </p:documentation>

    <p:input port="daisy-smil" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The DAISY 2.02 SMIL-files.</p:documentation>
    </p:input>
    <p:input port="daisy-content" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The EPUB3 Content Documents with @original-href annotated to reference the original DAISY 2.02 content files.</p:documentation>
    </p:input>

    <p:output port="fileset" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">A fileset with references to all the resources (audio, images, etc.).</p:documentation>
    </p:output>

    <p:option name="content-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">URI to the EPUB3 Content directory.</p:documentation>
    </p:option>
    <p:option name="include-mediaoverlay-resources" required="true" cx:as="xs:string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">Whether or not to include audio files associated with media overlays. Can be either 'true' or 'false'.</p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-join
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl">
        <p:documentation>
            px:mediatype-detect
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:smil-to-audio-fileset
        </p:documentation>
    </p:import>

    <p:for-each name="content-resources">
        <p:output port="result" sequence="true"/>
        <p:iteration-source>
            <p:pipe port="daisy-content" step="resources"/>
        </p:iteration-source>
        <p:variable name="original-href" select="/*/@original-href"/>
        <px:set-base-uri>
            <p:with-option name="base-uri" select="$original-href"/>
        </px:set-base-uri>
        <p:add-xml-base name="html"/>
        <p:sink/>
        <px:fileset-add-entry media-type="application/xhtml+xml">
            <p:input port="source">
                <p:inline><d:fileset/></p:inline>
            </p:input>
            <p:input port="entry">
                <p:pipe step="html" port="result"/>
            </p:input>
        </px:fileset-add-entry>
        <px:html-load>
            <p:input port="source.in-memory">
                <p:pipe step="html" port="result"/>
            </p:input>
        </px:html-load>
        <!-- not using fileset-filter because we don't want to delete files references from iframes -->
        <p:delete match="d:file[@media-type='application/xhtml+xml' and not(@kind='content')]"/>
        <px:message message="extracted list of resources from $1">
            <p:with-option name="param1" select="$original-href"/>
        </px:message>
    </p:for-each>
    <p:sink/>
    <p:for-each name="smil-resources">
        <p:output port="result" sequence="true"/>
        <p:iteration-source>
            <p:pipe port="daisy-smil" step="resources"/>
        </p:iteration-source>
        <p:variable name="original-href" select="base-uri(/*)"/>
        <p:choose>
            <p:when test="$include-mediaoverlay-resources='true'">
                <px:smil-to-audio-fileset/>
                <px:message message="extracted list of audio files from $1">
                    <p:with-option name="param1" select="$original-href"/>
                </px:message>
            </p:when>
            <p:otherwise>
                <p:identity>
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <p:sink/>
    <px:fileset-join>
        <p:input port="source">
            <p:pipe port="result" step="content-resources"/>
            <p:pipe port="result" step="smil-resources"/>
        </p:input>
    </px:fileset-join>
    <px:mediatype-detect name="iterate.mediatype"/>
    <p:viewport match="//d:file">
        <p:add-attribute match="/*" attribute-name="original-href">
            <p:with-option name="attribute-value" select="resolve-uri(/*/@href,base-uri(/*))"/>
        </p:add-attribute>
    </p:viewport>
    <p:add-attribute match="/*" attribute-name="xml:base">
        <p:with-option name="attribute-value" select="$content-dir"/>
    </p:add-attribute>
    <px:message message="determined media type for all auxiliary resources"/>

</p:declare-step>
