<p:declare-step type="px:mathml-to-ssml" version="1.0" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		xmlns:m="http://www.w3.org/1998/Math/MathML"
		exclude-inline-prefixes="#all">

  <p:input port="source" sequence="true" primary="true"/>
  <p:output port="result" sequence="true" primary="true"/>

  <!-- Convert Content into Presentation MathML if any -->
  <!-- TODO: improve Content MathML detection -->
  <p:for-each>
    <p:choose>
      <p:when test="count(//m:apply)+count(//m:set)+count(//m:list)+count(//m:matrix)+count(//m:vector)=0">
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
      <!-- <p:identity/> -->
      </p:otherwise>
    </p:choose>
  </p:for-each>
  <p:wrap-sequence wrapper="m:wrapper"/>

  <!-- Main conversion (Presentation Mathml to SSML) -->
  <p:xslt>
    <p:input port="stylesheet">
      <p:document href="pres-to-ssml/pres-mathml-to-ssml.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
