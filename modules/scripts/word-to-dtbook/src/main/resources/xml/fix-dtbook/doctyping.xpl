<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="pxi:dtbook-doctyping" name="main">

    <p:input port="source" px:media-type="application/x-dtbook+xml" sequence="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A single DTBook document</p>
        </p:documentation>
    </p:input>
    <p:output port="result" sequence="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The result DTBook document as serialized content</p>
        </p:documentation>
        <p:pipe port="result" step="add-doctype" />
    </p:output>
    <p:option name="css" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2>CSS</h2>
            <p>CSS stylesheet path relative to the document</p>
        </p:documentation>
    </p:option>

    <p:xslt name="add-doctype" cx:serialize="true">
        <p:input port="stylesheet">
            <p:document href="xsl/export-doctype.xsl"/>
        </p:input>
        <p:with-param name="css" select="$css"/>
    </p:xslt>

</p:declare-step>