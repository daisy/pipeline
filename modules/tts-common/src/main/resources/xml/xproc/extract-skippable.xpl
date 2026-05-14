<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-inline-prefixes="#all"
		type="pxi:extract-skippable" name="main">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Extract skippable elements.</p>
  </p:documentation>

  <p:input port="source" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The input document should have been previously processed by
      <code>px:isolate-skippable</code>, which means that elements containing skippable elements
      have been broken up and wrapper <code>span</code> elements with unique <code>id</code>
      attributes have been inserted.</p>
    </p:documentation>
  </p:input>
  <p:input port="sentence-ids">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>List of the sentence IDs, as a document with <code>id</code> attributes.</p>
    </p:documentation>
  </p:input>
  <p:input port="skippable-ids">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The list of skippable element IDs, as a document with <code>id</code> attributes (coming
      from the <code>px:isolate-skippable</code> step).</p>
    </p:documentation>
  </p:input>
  <p:output port="skippable-free" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Skippable elements are extracted from the normal flow and SSML <code>mark</code> elements
      are inserted at their original position.</p>
      <p>The mark names are of the form "X___Y", where "X" refers to the part of the sentence before
      the mark (and after the preceding mark), and "Y" refers to the part of the sentence after the
      mark (and before the following mark).</p>
    </p:documentation>
  </p:output>
  <p:output port="skippable-only">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>A flat list of extracted elements containing the skippable elements, wrapped in a SSML
      <code>speak</code> document. The context of the extracted elements outside of their containing
      sentence is lost. Any information (such as attributes) that needs to be preserved should be
      transferred to the sentence elements before this step.</p>
    </p:documentation>
    <p:pipe port="secondary" step="separate"/>
  </p:output>

  <p:xslt name="separate">
    <p:input port="stylesheet">
      <p:document href="extract-skippable.xsl"/>
    </p:input>
    <p:with-param port="parameters" name="sentence-ids" select="/">
      <p:pipe step="main" port="sentence-ids"/>
    </p:with-param>
    <p:with-param port="parameters" name="skippable-ids" select="/">
      <p:pipe step="main" port="skippable-ids"/>
    </p:with-param>
  </p:xslt>

</p:declare-step>
