<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:opf="http://www.idpf.org/2007/opf" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:d="http://www.daisy.org/ns/pipeline/data" type="pxi:daisy202-to-epub3-package"
    name="package" exclude-inline-prefixes="#all" version="1.0">

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
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-pub-utils/library.xpl"/>

    <p:variable name="result-uri" select="concat($publication-dir,'package.opf')"/>

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">Compile OPF metadata.</p:documentation>
    <p:identity>
        <p:input port="source">
            <p:pipe port="ncc" step="package"/>
        </p:input>
    </p:identity>
    <px:message message="Extracting metadata from NCC..."/>
    <p:xslt>
        <p:with-param name="pub-id" select="$pub-id"/>
        <p:input port="stylesheet">
            <p:document href="ncc-metadata-to-opf-metadata.xsl"/>
        </p:input>
    </p:xslt>
    <px:message message="Metadata successfully extracted and converted to OPF"/>
    <p:identity name="opf-metadata"/>
    <p:sink/>

    <p:group name="spine">
        <p:output port="result" sequence="true"/>
        <p:variable name="base" select="base-uri(/*)">
            <p:pipe port="spine" step="package"/>
        </p:variable>
        <p:for-each>
            <p:output port="result" sequence="true"/>
            <p:iteration-source select="/*/d:file">
                <p:pipe port="spine" step="package"/>
            </p:iteration-source>
            <p:wrap-sequence wrapper="d:fileset"/>
            <p:add-attribute match="/*" attribute-name="xml:base">
                <p:with-option name="attribute-value" select="$base"/>
            </p:add-attribute>
            <px:fileset-join/>
        </p:for-each>
        <px:message message="Created filesets for each spine item"/>
    </p:group>
    <p:sink/>

    <px:fileset-join>
        <p:input port="source">
            <p:pipe port="spine" step="package"/>
            <p:pipe port="resources" step="package"/>
        </p:input>
    </px:fileset-join>
    <px:message message="Created manifest fileset"/>
    <p:identity name="manifest"/>
    <p:sink/>

    <px:epub3-pub-create-package-doc>
        <p:with-option name="result-uri" select="$result-uri"/>
        <p:with-option name="compatibility-mode" select="$compatibility-mode"/>
        <p:with-option name="detect-properties" select="'false'"/>
        <p:input port="spine-filesets">
            <p:pipe port="result" step="spine"/>
        </p:input>
        <p:input port="publication-resources">
            <p:pipe port="result" step="manifest"/>
        </p:input>
        <p:input port="metadata">
            <p:pipe port="result" step="opf-metadata"/>
        </p:input>
        <p:input port="content-docs">
            <p:pipe port="navigation" step="package"/>
            <p:pipe port="content-docs" step="package"/>
        </p:input>
        <p:input port="mediaoverlays">
            <p:pipe port="mediaoverlay" step="package"/>
        </p:input>
    </px:epub3-pub-create-package-doc>
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
                <p:pipe port="result" step="result-fileset.with-package"/>
            </p:input>
        </px:fileset-join>
        <px:mediatype-detect/>
        <px:message message="Added package document to result fileset"/>
    </p:group>
    <p:identity name="result-fileset"/>

</p:declare-step>
