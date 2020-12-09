<p:declare-step type="pxi:annotate" version="1.0" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		exclude-inline-prefixes="#all">

  <p:input port="source" primary="true"/>
  <p:input port="annotations" sequence="true"/>
  <p:input port="sentence-ids"/>
  <p:output port="result" primary="true"/>

  <p:count>
    <p:input port="source">
      <p:pipe port="annotations" step="main"/>
    </p:input>
  </p:count>
  <p:choose>
    <p:when test=". = 0">
      <p:identity>
	<p:input port="source">
	  <p:pipe port="source" step="main"/>
	</p:input>
      </p:identity>
    </p:when>
    <p:otherwise>
      <p:xslt name="generate-xslt">
	<p:input port="source">
	  <p:pipe port="annotations" step="main"/>
	</p:input>
	<p:input port="stylesheet">
	  <p:document href="../xslt/generate-annotating-stylesheet.xsl"/>
	</p:input>
	<p:input port="parameters">
	  <p:empty/>
	</p:input>
      </p:xslt>
      <p:xslt name="annotate">
	<p:input port="source">
	  <p:pipe port="source" step="main"/>
	  <p:pipe port="sentence-ids" step="main"/>
	</p:input>
	<p:input port="stylesheet">
	  <p:pipe port="result" step="generate-xslt"/>
	</p:input>
	<p:input port="parameters">
	  <p:empty/>
	</p:input>
      </p:xslt>
    </p:otherwise>
  </p:choose>


</p:declare-step>
