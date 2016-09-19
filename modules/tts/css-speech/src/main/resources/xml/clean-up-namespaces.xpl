<p:declare-step type="px:clean-up-namespaces" version="1.0" name="main"
		xmlns:p="http://www.w3.org/ns/xproc"
		xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
		exclude-inline-prefixes="#all">

  <p:input port="source" primary="true"/>
  <p:output port="result" primary="true"/>

  <p:xslt>
    <p:input port="stylesheet">
      <p:inline>
	<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
			exclude-result-prefixes="#all"
			version="2.0">
	  <xsl:template match="node()">
	    <xsl:copy copy-namespaces="no">
	      <xsl:copy-of select="@*"/>
	      <xsl:apply-templates/>
	    </xsl:copy>
	  </xsl:template>
	</xsl:stylesheet>
      </p:inline>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
