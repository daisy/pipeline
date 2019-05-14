<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook Utilities</h1>
        <p px:role="desc">A collection of utilities for DTBook files.</p>
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a href="mailto:marisa.demeglio@gmail.com" px:role="contact">marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>

    <p:import href="dtbook-load.xpl"/>
    <p:import href="upgrade-dtbook/upgrade-dtbook.xpl"/>
    <p:import href="merge-dtbook/merge-dtbook.xpl"/>
    <p:import href="validate-dtbook/dtbook-validator.check-images.xpl"/>
    <p:import href="validate-dtbook/dtbook-validator.select-schema.xpl"/>
    <p:import href="dtbook-to-mods-meta.xpl"/>
    
</p:library>
