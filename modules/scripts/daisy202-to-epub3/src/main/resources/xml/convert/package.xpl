<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-inline-prefixes="#all"
                type="pxi:daisy202-to-epub3-package" name="package">

    <p:documentation>Compile the OPF.</p:documentation>

    <p:input port="spine" primary="false" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="desc">A ordered fileset of Content Documents sorted in reading order.</p>
            <p>See also: <a class="see" href="http://idpf.org/epub/30/spec/epub30-overview.html#sec-nav-order">http://idpf.org/epub/30/spec/epub30-overview.html#sec-nav-order</a></p>
        </p:documentation>
    </p:input>
    <p:input port="ncc" primary="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The DAISY 2.02 NCC</p:documentation>
    </p:input>
    <p:input port="navigation" primary="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The EPUB3 Navigation Document.</p:documentation>
    </p:input>
    <p:input port="content-docs" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The EPUB3 Content Documents.</p:documentation>
    </p:input>
    <p:input port="mediaoverlay" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The EPUB3 Media Overlays.</p:documentation>
    </p:input>
    <p:input port="resources" primary="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">Files other than the Content Documents in the spine and the Media Overlays (i.e. the Navigation Document, the NCX, the audio, images,
            etc.).</p:documentation>
    </p:input>

    <p:output port="opf-package" sequence="true" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The package document.</p:documentation>
        <p:pipe port="result" step="opf-package"/>
    </p:output>
    <p:output port="fileset" primary="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">A fileset of all the files in the EPUB3 publication, including the package file itself.</p:documentation>
        <p:pipe port="result" step="result-fileset"/>
    </p:output>

    <p:option name="pub-id" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p px:role="desc">The publication identifier.</p>
            <p>See also: <a class="see" href="http://idpf.org/epub/30/spec/epub30-publications.html#sec-opf-dcidentifier"
                >http://idpf.org/epub/30/spec/epub30-publications.html#sec-opf-dcidentifier</a></p>
        </p:documentation>
    </p:option>
    <p:option name="compatibility-mode" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">Whether or not to make the package document backwards-compatible. Can be either 'true' (default) or 'false'.</p:documentation>
    </p:option>
    <p:option name="publication-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">URI to the EPUB3 Publication directory.</p:documentation>
    </p:option>
    <p:option name="epub-dir" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">URI to the base directory where the EPUB3-files should eventually be stored.</p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-create
            px:fileset-add-entry
            px:fileset-add-entries
            px:fileset-join
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl">
        <p:documentation>
            px:mediatype-detect
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub3-create-package-doc
        </p:documentation>
    </p:import>
    <p:import href="ncc-to-opf-metadata.xpl">
        <p:documentation>
            px:ncc-to-opf-metadata
        </p:documentation>
    </p:import>

    <p:variable name="result-uri" select="concat($publication-dir,'package.opf')"/>

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">Compile OPF metadata.</p:documentation>
    <px:ncc-to-opf-metadata px:message="Extracting metadata from NCC...">
        <p:input port="source">
            <p:pipe step="package" port="ncc"/>
        </p:input>
        <p:with-option name="pub-id" select="$pub-id"/>
    </px:ncc-to-opf-metadata>
    <p:identity name="opf-metadata" px:message="Metadata successfully extracted and converted to OPF"/>
    <p:sink/>

    <p:group name="manifest">
        <p:output port="result"/>
        <px:fileset-create/>
        <px:fileset-add-entries>
            <p:input port="entries">
                <p:pipe step="package" port="navigation"/>
                <p:pipe step="package" port="content-docs"/>
                <p:pipe step="package" port="mediaoverlay"/>
            </p:input>
        </px:fileset-add-entries>
        <p:identity name="manifest-without-resources"/>
        <p:sink/>
        <px:fileset-join>
            <p:input port="source">
                <p:pipe step="manifest-without-resources" port="result"/>
                <p:pipe step="package" port="resources"/>
            </p:input>
        </px:fileset-join>
        <px:message message="Created manifest fileset"/>
    </p:group>
    <p:sink/>

    <px:epub3-create-package-doc>
        <p:with-option name="output-base-uri" select="$result-uri"/>
        <p:with-option name="compatibility-mode" select="$compatibility-mode"/>
        <p:with-option name="detect-properties" select="'false'"/>
        <p:input port="source.fileset">
            <p:pipe step="manifest" port="result"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="package" port="navigation"/>
            <p:pipe step="package" port="content-docs"/>
            <p:pipe step="package" port="mediaoverlay"/>
        </p:input>
        <p:input port="spine">
            <p:pipe step="package" port="spine"/>
        </p:input>
        <p:input port="metadata">
            <p:pipe step="opf-metadata" port="result"/>
        </p:input>
    </px:epub3-create-package-doc>
    <px:message message="Package document created successfully"/>
    <p:identity name="opf-package"/>

    <p:group>
        <p:xslt>
            <p:with-param name="base" select="replace($result-uri,'[^/]+$','')"/>
            <p:input port="stylesheet">
                <p:document href="package.manifest-to-fileset.xsl"/>
            </p:input>
        </p:xslt>
        <px:fileset-add-entry name="result-fileset.with-package">
            <p:with-option name="href" select="$result-uri"/>
            <p:with-option name="media-type" select="'application/oebps-package+xml'"/>
        </px:fileset-add-entry>
        <px:fileset-create name="result-fileset.with-epub-base">
            <p:with-option name="base" select="$epub-dir"/>
        </px:fileset-create>
        <px:fileset-join>
            <p:input port="source">
                <p:pipe port="result" step="result-fileset.with-epub-base"/>
                <p:pipe port="result.fileset" step="result-fileset.with-package"/>
            </p:input>
        </px:fileset-join>
        <px:mediatype-detect/>
        <px:message message="Added package document to result fileset"/>
    </p:group>
    <p:identity name="result-fileset"/>

</p:declare-step>
