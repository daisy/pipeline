<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>DAISY 3 Utilities</h1>
        <p>A collection of utilities for DAISY 3 filesets.</p>
        <dl px:role="author">
            <dt>Name:</dt>
            <dd px:role="name">Romain Deltour</dd>
            <dt>E-mail:</dt>
            <dd><a px:role="contact" href="mailto:rdeltour@gmail.com">rdeltour@gmail.com</a></dd>
            <dt>Organization:</dt>
            <dd px:role="organization">DAISY Consortium</dd>
        </dl>
        <dl px:role="author">
            <dt>Name:</dt>
            <dd px:role="name">Bert Frees</dd>
            <dt>E-mail:</dt>
            <dd><a px:role="contact" href="mailto:bertfrees@gmail.com">bertfrees@gmail.com</a></dd>
        </dl>
    </p:documentation>

    <p:import href="internal/load/load.xpl"/>
    <p:import href="internal/smils/create-daisy3-smils.xpl"/>
    <p:import href="internal/smils/add-smilrefs.xpl"/>
    <p:import href="internal/smils/add-elapsed-time.xpl"/>
    <p:import href="internal/ncx/create-ncx.xpl"/>
    <p:import href="internal/ncx/update-links.xpl"/>
    <p:import href="internal/opf/create-daisy3-opf.xpl"/>
    <p:import href="internal/dtbook/prepare-dtbook.xpl"/>
    <p:import href="internal/resources/create-res-file.xpl"/>
    <p:import href="internal/audio-transcode.xpl"/>
    <p:import href="internal/update-links.xpl"/>
    <p:import href="internal/upgrade.xpl"/>

</p:library>
