<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-daisy202"
    xmlns:c="http://www.w3.org/ns/xproc-step" type="pxi:daisy3-to-daisy202-smils" name="main"
    version="1.0">

    <p:input port="smils" sequence="true" primary="true"/>
    <p:input port="ncx"/>

    <p:output port="fileset" primary="true"/>
    <p:output port="docs" sequence="true">
        <p:pipe port="docs" step="iter-smils"/>
    </p:output>

    <p:option name="input-dir" required="true"/>
    <p:option name="output-dir" required="true"/>


    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>


    <!--NCX smilCustomTest elements used for SMIL conversion-->
    <p:filter name="custom-tests" select="/ncx:ncx/ncx:head/ncx:smilCustomTest"
        xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/">
        <p:input port="source">
            <p:pipe port="ncx" step="main"/>
        </p:input>
    </p:filter>
    <p:sink/>
    <!--NCX references to SMIL docs, grouped by doc URIs.-->
    <p:xslt name="ncx-idrefs">
        <p:input port="source">
            <p:pipe port="ncx" step="main"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="ncx-to-smil-id-map.xsl"/>
        </p:input>
        <p:input port="parameters">
            <!--FIXME pass the relative URI to the NCC-->
            <p:empty/>
        </p:input>
    </p:xslt>
    <p:sink/>

    <!--Main iteration over input SMIL docs-->
    <p:for-each name="iter-smils">
        <p:iteration-source>
            <p:pipe port="smils" step="main"/>
        </p:iteration-source>
        <p:output port="docs">
            <p:pipe port="result" step="result-smil"/>
        </p:output>
        <p:output port="filesets" primary="true"/>
        <p:variable name="daisy3-smil-uri" select="base-uri(/)"/>

        <!--FIXME there must be a better way than an inline XSLT-->
        <!--<p:variable name="daisy202-smil-uri" select="replace(base-uri(/),$input-dir,$output-dir)"/>-->
        <p:xslt name="daisy202-smil-uri">
            <p:input port="stylesheet">
                <p:inline>
                    <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                        xmlns:pf="http://www.daisy.org/ns/pipeline/functions" version="2.0">
                        <xsl:import
                            href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl"/>
                        <xsl:param name="input-dir"/>
                        <xsl:param name="output-dir"/>
                        <xsl:template match="/">
                            <doc
                                href="{replace(pf:normalize-uri(base-uri(/)),pf:normalize-uri($input-dir),pf:normalize-uri($output-dir))}"
                            />
                        </xsl:template>
                    </xsl:stylesheet>
                </p:inline>
            </p:input>
            <p:with-param name="input-dir" select="$input-dir"/>
            <p:with-param name="output-dir" select="$output-dir"/>
        </p:xslt>
        <p:sink/>

        <p:identity>
            <p:input port="source">
                <p:inline>
                    <doc/>
                </p:inline>
            </p:input>
        </p:identity>

        <!--Get the list of NCX ID references to this SMIL-->
        <p:filter name="idrefs">
            <p:input port="source">
                <p:pipe port="result" step="ncx-idrefs"/>
            </p:input>
            <p:with-option name="select" select="concat('/*/d:doc[@href=''',base-uri(/),''']')"/>
        </p:filter>

        <!--Convert DAISY 2.02 SMIL to DAISY 3 SMIL-->
        <p:xslt name="smil-to-smil">
            <p:input port="source">
                <p:pipe port="current" step="iter-smils"/>
                <p:pipe port="result" step="custom-tests"/>
                <p:pipe port="result" step="idrefs"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="smil-to-smil.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="//@href">
                <p:pipe port="result" step="daisy202-smil-uri"/>
            </p:with-option>
        </p:add-attribute>
        <p:delete match="/*/@xml:base" name="result-smil"/>

        <!--Fileset of the audio files used in this SMIL-->
        <p:group>
            <p:output port="result" primary="true"/>
            <p:xslt>
                <p:input port="source">
                    <p:pipe port="result" step="smil-to-smil"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document
                        href="http://www.daisy.org/pipeline/modules/mediaoverlay-utils/smil-to-audio-fileset.xsl"
                    />
                </p:input>
            </p:xslt>
            <px:mediatype-detect/>
            <!--TODO use px:fileset-move instead of the following-->
            <p:viewport match="//d:file">
                <p:add-attribute match="/*" attribute-name="original-href">
                    <p:with-option name="attribute-value"
                        select="resolve-uri(/*/@href,base-uri(/*))"/>
                </p:add-attribute>
            </p:viewport>
            <p:add-attribute match="/*" attribute-name="xml:base">
                <p:with-option name="attribute-value" select="$output-dir"/>
            </p:add-attribute>
        </p:group>

        <!--Add the URI of the converted SMIL to the audio resource fileset-->
        <px:fileset-add-entry media-type="application/smil+xml">
            <p:with-option name="href" select="//@href">
                <p:pipe port="result" step="daisy202-smil-uri"/>
            </p:with-option>
        </px:fileset-add-entry>
    </p:for-each>
    <px:fileset-join/>

</p:declare-step>
