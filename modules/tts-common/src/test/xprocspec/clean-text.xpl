<p:declare-step version="1.0" type="pxi:clean-text"  xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:p="http://www.w3.org/ns/xproc" exclude-inline-prefixes="#all">

  <p:input port="source" primary="true"/>
  <p:output port="result" primary="true"/>

  <p:xslt>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:inline>
	<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
			version="2.0">
	  <xsl:template match="text()" priority="2">
	    <xsl:value-of select="replace(., '[^a-zA-Z0-9]', '')"/>
	  </xsl:template>
	  <xsl:template match="@*|node()" priority="1">
	    <xsl:copy>
	      <xsl:apply-templates select="@*|node()"/>
	    </xsl:copy>
	  </xsl:template>
	</xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt>

</p:declare-step>
