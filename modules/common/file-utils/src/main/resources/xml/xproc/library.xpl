<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <div>
            <h1>File Utilities</h1>
            <p>The steps defined in this library provide information about files and the ability to
                manipulate them. All implementations are required to support <code>file:</code>
                scheme URIs. Support for other schemes is implementation-defined. </p>
            <p>All <code>href</code> attributes are made absolute with respect to the element on
                which they are specified.</p>
        </div>
    </p:documentation>

    <p:import href="recursive-directory-list.xpl"/>
    <p:import href="tempdir.xpl"/>
    <p:import href="copy-resource.xpl"/>
    <p:import href="set-doctype.xpl"/>
    <p:import href="peek.xpl"/>
    <p:import href="xml-peek.xpl"/>
    <p:import href="set-xml-declaration.xpl"/>
    <p:import href="normalize-uri.xpl"/>
    <p:import href="normalize-document-base.xpl"/>
    
    <p:import href="java-library.xpl"/>

</p:library>
