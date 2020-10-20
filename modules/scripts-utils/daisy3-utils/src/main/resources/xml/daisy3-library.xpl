<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DAISY 3 Utilities</h1>
        <p px:role="desc">A collection of utilities for DAISY 3 filesets.</p>
        <div px:role="author maintainer">
            <p px:role="name">Romain Deltour</p>
            <a href="mailto:rdeltour@gmail.com" px:role="contact">rdeltour@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>

    <p:import href="internal/load/load.xpl"/>
    <p:import href="internal/smils/create-daisy3-smils.xpl"/>
    <p:import href="internal/smils/add-smilrefs.xpl"/>
    <p:import href="internal/ncx/create-ncx.xpl"/>
    <p:import href="internal/ncx/update-links.xpl"/>
    <p:import href="internal/opf/create-daisy3-opf.xpl"/>
    <p:import href="internal/dtbook/prepare-dtbook.xpl"/>
    <p:import href="internal/resources/create-res-file.xpl"/>
</p:library>
