<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:smil="http://www.w3.org/2001/SMIL20/"
                type="px:daisy3-add-smilrefs" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Add <code>smilref</code> attributes to a DTBook based on a set of SMIL documents that
      reference the DTBook.</p>
      <p>Apart from <code>src</code> attributes of <code>text</code> elements,
      <code>epub:textref</code> attributes on <code>seq</code> elements are also regarded as
      references.</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Fileset with a set of SMILs and a DTBook without smilref attributes</p>
      </p:documentation>
    </p:input>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Copy of the input fileset with the modified DTBook.</p>
      </p:documentation>
      <p:pipe step="update" port="result.in-memory"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
      <p:documentation>
        px:fileset-load
        px:fileset-filter
        px:fileset-update
      </p:documentation>
    </p:import>

    <px:fileset-load name="load"
                     media-types="application/x-dtbook+xml
                                  application/smil">
      <p:input port="in-memory">
        <p:pipe step="main" port="source.in-memory"/>
      </p:input>
    </px:fileset-load>
    <p:sink/>
    <px:fileset-filter name="filter" media-types="application/x-dtbook+xml">
      <p:input port="source">
        <p:pipe step="load" port="result.fileset"/>
      </p:input>
      <p:input port="source.in-memory">
        <p:pipe step="load" port="result"/>
      </p:input>
    </px:fileset-filter>
    <p:sink/>

    <p:xslt name="xslt">
      <p:input port="source">
        <p:pipe step="filter" port="result.in-memory"/>
        <p:pipe step="filter" port="not-matched.in-memory"/>
      </p:input>
      <p:input port="stylesheet">
        <p:document href="add-smilrefs-from-smils.xsl"/>
      </p:input>
      <p:input port="parameters">
        <p:empty/>
      </p:input>
    </p:xslt>

    <px:fileset-update name="update">
      <p:input port="source.fileset">
        <p:pipe step="main" port="source.fileset"/>
      </p:input>
      <p:input port="source.in-memory">
        <p:pipe step="main" port="source.in-memory"/>
      </p:input>
      <p:input port="update.fileset">
        <p:pipe step="load" port="result.fileset"/>
      </p:input>
      <p:input port="update.in-memory">
        <p:pipe step="xslt" port="result"/>
        <p:pipe step="filter" port="not-matched.in-memory"/>
      </p:input>
    </px:fileset-update>

</p:declare-step>
