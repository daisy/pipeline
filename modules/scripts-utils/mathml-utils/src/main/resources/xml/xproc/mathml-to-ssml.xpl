<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                exclude-inline-prefixes="#all"
                type="px:mathml-to-ssml">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Transform MathML to SSML</p>
  </p:documentation>

  <p:input port="source" px:media-type="application/mathml+xml">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Either a standalone MathML document, or a document containing zero or more MathML
      elements.</p>
    </p:documentation>
  </p:input>

  <p:output port="result" px:media-type="application/mathml+xml">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Copy of the source document with MathML elements replaced by SSML (<code>speak</code>
      elements). <code>id</code> and other attributes that may be present on the MathML elements are
      preserved. Elements without an <code>id</code> attributes are given one.</p>
    </p:documentation>
  </p:output>

  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
    <p:documentation>
      px:add-ids
    </p:documentation>
  </p:import>

  <!-- make sure MathML elements have an id attribute -->
  <px:add-ids match="m:math"/>

  <p:xslt>
    <p:input port="stylesheet">
      <p:document href="mathml-to-ssml.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
