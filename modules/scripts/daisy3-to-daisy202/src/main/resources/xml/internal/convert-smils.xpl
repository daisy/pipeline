<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-daisy202"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/"
                type="pxi:daisy3-to-daisy202-smils" name="main">

    <p:input port="source.fileset" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Source fileset with DAISY 3 SMIL documents and a NCX document.</p>
        </p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true"/>

    <p:output port="result.fileset" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Result fileset with the DAISY 2.02 SMIL documents and the audio files referenced from
            the SMILs.</p>
        </p:documentation>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="moved-fileset" port="result.in-memory"/>
    </p:output>

    <p:option name="input-dir" required="true"/>
    <p:option name="output-dir" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-rebase
            px:fileset-move
            px:fileset-join
            px:fileset-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl">
        <p:documentation>
            px:mediatype-detect
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:smil-to-audio-fileset
            px:smil-downgrade
            px:smil-update-links
        </p:documentation>
    </p:import>

    <px:fileset-load media-types="application/x-dtbncx+xml" name="ncx">
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:identity>
        <p:input port="source">
            <p:pipe step="ncx" port="result.fileset"/>
        </p:input>
    </p:identity>
    <px:assert error-code="XXXX"
               message="The input DTB must contain exactly one NCX file (media-type 'application/x-dtbncx+xml')">
        <p:with-option name="test" select="count(//d:file)=1"/>
    </px:assert>
    <p:label-elements match="d:file" attribute="original-href" label="resolve-uri(@href,base-uri(.))"/>
    <p:add-attribute match="d:file" attribute-name="href" name="ncx-to-ncc-mapping">
        <p:with-option name="attribute-value" select="resolve-uri('ncc.html',$input-dir)"/>
    </p:add-attribute>
    <p:sink/>

    <px:fileset-load media-types="application/smil+xml" name="input-smils">
        <p:input port="fileset">
            <p:pipe step="main" port="source.fileset"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>

    <!--Main iteration over input SMIL docs-->
    <p:for-each name="iter-smils">
        <p:output port="audio.fileset" primary="true"/>
        <p:output port="smil.in-memory">
            <p:pipe step="smil-downgrade" port="result"/>
        </p:output>

        <!--Convert DAISY 3 SMIL to DAISY 2.02 SMIL-->
        <px:smil-downgrade version="1.0">
            <p:input port="ncx">
                <p:pipe step="ncx" port="result"/>
            </p:input>
        </px:smil-downgrade>

        <!--Replace NCX with NCC-->
        <px:smil-update-links name="smil-downgrade">
            <p:input port="mapping">
                <p:pipe step="ncx-to-ncc-mapping" port="result"/>
            </p:input>
        </px:smil-update-links>

        <!--Fileset of the audio files used in this SMIL-->
        <px:smil-to-audio-fileset/>
        <px:mediatype-detect/>
    </p:for-each>
    <px:fileset-join name="audio-fileset"/>
    <p:sink/>
    <p:add-attribute match="d:file" attribute-name="media-version" attribute-value="1.0">
        <p:input port="source">
            <p:pipe step="input-smils" port="result.fileset"/>
        </p:input>
    </p:add-attribute>
    <p:add-attribute match="d:file"
                     attribute-name="doctype-public"
                     attribute-value="-//W3C//DTD SMIL 1.0//EN"/>
    <p:add-attribute match="d:file"
                     attribute-name="doctype-system"
                     attribute-value="http://www.w3.org/TR/REC-smil/SMIL10.dtd"/>
    <p:identity name="smil-fileset"/>
    <p:sink/>
    <px:fileset-join>
        <p:input port="source">
            <p:pipe step="smil-fileset" port="result"/>
            <p:pipe step="audio-fileset" port="result"/>
        </p:input>
    </px:fileset-join>
    <px:fileset-rebase>
        <p:with-option name="new-base" select="$input-dir"/>
    </px:fileset-rebase>
    <px:fileset-move name="moved-fileset">
        <p:input port="source.in-memory">
            <p:pipe step="iter-smils" port="smil.in-memory"/>
        </p:input>
        <p:with-option name="target" select="$output-dir"></p:with-option>
    </px:fileset-move>

</p:declare-step>
