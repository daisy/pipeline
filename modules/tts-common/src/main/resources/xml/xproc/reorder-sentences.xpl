<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:reorder-sentences" version="1.0" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		xmlns:cx="http://xmlcalabash.com/ns/extensions"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		exclude-inline-prefixes="#all">

  <p:input port="source" sequence="true"/>
  <p:output port="result"/>

  <p:option name="ids-in-order" cx:as="xs:string*" required="true">
    <p:documentation>The sentence IDs in the desired order. The resulting SSML will start with these
    sentences. Unmatched sentences are added at the end (in the order in which they are provided).</p:documentation>
  </p:option>

  <p:xslt>
    <p:input port="stylesheet">
      <p:document href="reorder-sentences.xsl"/>
    </p:input>
    <p:with-param port="parameters" name="ids-in-order" select="$ids-in-order"/>
  </p:xslt>

</p:declare-step>
