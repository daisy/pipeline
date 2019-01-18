<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:import href="fileset-add-entry.xpl"/>
    <p:import href="fileset-copy.xpl"/>
    <p:import href="fileset-create.xpl"/>
    <p:import href="fileset-diff.xpl"/>
    <p:import href="fileset-from-dir.xpl"/>
    <p:import href="fileset-from-dir-list.xpl"/>
    <p:import href="fileset-intersect.xpl"/>
    <p:import href="fileset-join.xpl"/>
    <p:import href="fileset-filter.xpl"/>
    <p:import href="fileset-load.xpl"/>
    <p:import href="fileset-store.xpl"/>
    <p:import href="fileset-add-ref.xpl"/>
    <p:import href="fileset-rebase.xpl"/>
    <p:import href="fileset-move.xpl"/>
    <p:import href="fileset-unzip.xpl"/>
    <p:import href="fileset-update.xpl"/>

    <p:declare-step type="px:zip-manifest-from-fileset">
        <p:input port="source"/>
        <p:output port="result"/>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="../xslt/fileset-to-zip-manifest.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:declare-step>

    <p:import href="xprocspec-fileset-compare.xpl"/>

</p:library>
