<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>DTBook Utilities</h1>
        <p>A collection of utilities for DTBook files.</p>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Marisa DeMeglio</dd>
                <dt>E-mail:</dt>
                <dd><a href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization">DAISY Consortium</dd>
            </dl>
        </address>
    </p:documentation>

    <p:import href="dtbook-load.xpl"/>
    <p:import href="upgrade-dtbook/upgrade-dtbook.xpl"/>
    <p:import href="merge-dtbook/merge-dtbook.xpl"/>
    <p:import href="validate-dtbook/validate-dtbook.xpl"/>
    <p:import href="dtbook-to-mods-meta.xpl"/>
    <p:import href="dtbook-update-links.xpl"/>
    <p:import href="break-detect/library.xpl"/>

</p:library>
