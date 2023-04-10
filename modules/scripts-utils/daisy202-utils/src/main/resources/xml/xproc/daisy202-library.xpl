<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>DAISY 2.02 Utilities</h1>
        <p>A collection of utilities for DAISY 2 filesets.</p>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Jostein Austvik Jacobsen</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:josteinaj@gmail.com">josteinaj@gmail.com</a></dd>
                <dt>Organisation:</dt>
                <dd px:role="organization">NLB</dd>
            </dl>
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Bert Frees</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:bertfrees@gmail.com">bertfrees@gmail.com</a></dd>
            </dl>
        </address>
    </p:documentation>

    <p:import href="load/load.xpl"/>
    <p:import href="rename-files.xpl"/>
    <p:import href="update-links.xpl"/>
    <p:import href="fix-audio-file-order.xpl"/>
    <p:import href="audio-transcode.xpl"/>

</p:library>
