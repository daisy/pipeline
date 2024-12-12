<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                exclude-inline-prefixes="#all"
                name="main">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Transform a single MathML document to SSML</p>
  </p:documentation>

  <p:input port="source" px:media-type="application/mathml+xml">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>A MathML document (either content or presentation)</p>
    </p:documentation>
  </p:input>
  <p:input kind="parameter" port="parameters">
     <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Should contain the 'language' parameter, which determineds the input and output language.</p>
    </p:documentation>
  </p:input>
  <p:output port="result" px:media-type="application/ssml+xml">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>An SSML document</p>
    </p:documentation>
  </p:output>

  <!-- convert content into presentation MathML -->
  <p:choose>
    <p:when test="empty(//m:apply|//m:set|//m:list|//m:matrix|//m:vector)">
      <p:identity/>
    </p:when>
    <p:otherwise>
      <p:xslt>
        <p:input port="stylesheet">
          <p:document href="content-to-pres/mathmlc2p.xsl"/>
        </p:input>
        <p:input port="parameters">
          <p:empty/>
        </p:input>
      </p:xslt>
    </p:otherwise>
  </p:choose>

  <!-- convert presentation MathML to SSML -->
  <p:xslt>
    <p:input port="stylesheet">
      <p:document href="pres-to-ssml/pres-mathml-to-ssml.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:pipe step="main" port="parameters"/>
    </p:input>
  </p:xslt>

</p:declare-step>
