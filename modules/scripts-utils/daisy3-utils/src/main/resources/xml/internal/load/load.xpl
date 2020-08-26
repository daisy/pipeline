<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                name="main" type="px:daisy3-load">

    <p:documentation>
        <p px:role="desc">Creates a fileset document based on a DAISY 3 package file.</p>
        <p>The fileset entries are ordered as the appear in the <code>manifest</code> element of the
            input OPF document, except for SMIL documents which are listed in the <code>spine</code>
            order. If more than one DTBook document is found in the fileset, SMIL documents are
            loaded in memory to sort the DTBook documents in the spine order in the resulting
            fileset. Otherwise, only the OPF itself is loaded in memory.</p>
        <p>Note: In the resulting fileset, the media type of SMIL documents will be
                <code>application/smil+xml</code> (as opposed to <code>application/smil</code> in
            DAISY 3) and the media type of the OPF document will be
                <code>application/oebps-package+xml</code> (as opposed to <code>text/xml</code> in
            DAISY 3).</p>
    </p:documentation>

    <p:input port="source" primary="true" sequence="false">
        <p:documentation>
            <h2 px:role="name">Input OPF</h2>
            <p px:role="desc">The package file of the input DTB.</p>
        </p:documentation>
    </p:input>

    <p:output port="fileset.out" primary="true" sequence="false">
        <p:pipe port="fileset" step="fileset-ordered"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="source" step="main"/>
        <p:pipe port="docs" step="fileset-ordered"/>
    </p:output>

    <p:serialization port="fileset.out" indent="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">For manipulating
            filesets.</p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:smil-to-text-fileset
        </p:documentation>
    </p:import>

    <p:xslt name="fileset">
        <p:input port="stylesheet">
            <p:document href="opf-to-fileset.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>

    <p:group name="fileset-ordered">
        <p:output port="fileset" primary="true"/>
        <p:output port="docs" sequence="true">
            <p:pipe port="docs" step="choose"/>
        </p:output>
        <!--Re-order the DTBook entries in the file set-->
        <p:choose name="choose">
            <p:when test="count(//d:file[@media-type='application/x-dtbook+xml']) > 1">
                <p:output port="fileset" primary="true"/>
                <p:output port="docs" sequence="true">
                    <p:pipe port="result" step="load-smils"/>
                </p:output>
                <!--when there is more than one DTBook, delete all entries
                and re-compute them by parsing each SMIL file-->
                <p:delete name="fileset-no-dtbooks"
                    match="d:file[@media-type='application/x-dtbook+xml']"/>
                <p:for-each name="load-smils">
                    <p:output port="result"/>
                    <p:iteration-source select="//d:file[@media-type='application/smil']"/>
                    <p:load>
                        <p:with-option name="href" select="/*/resolve-uri(@href,base-uri(.))"/>
                    </p:load>
                </p:for-each>
                <p:for-each name="fileset-dtbooks">
                    <p:output port="result"/>
                    <p:iteration-source>
                        <p:pipe port="result" step="load-smils"/>
                    </p:iteration-source>
                    <px:smil-to-text-fileset/>
                    <p:add-attribute attribute-name="media-type"
                        attribute-value="application/x-dtbook+xml" match="d:file"/>
                </p:for-each>
                <px:fileset-join>
                    <p:input port="source">
                        <p:pipe port="result" step="fileset-no-dtbooks"/>
                        <p:pipe port="result" step="fileset-dtbooks"/>
                    </p:input>
                </px:fileset-join>
            </p:when>
            <p:otherwise>
                <p:output port="fileset" primary="true"/>
                <p:output port="docs" sequence="true">
                    <p:empty/>
                </p:output>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:group>

</p:declare-step>
