<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  version="1.0">
  
  <p:option name="base" select="'file:/C:/cygwin/home/gerrit/Dev/epubtools-xproc/tmp/guide///epub/OEBPS/text/cover.xhtml'"/>
  <p:option name="add" select="'../../testi/foo.bar'"/>
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Expected result when running without parameters:
        <code>file:/C:/cygwin/home/gerrit/Dev/epubtools-xproc/tmp/guide/epub/OEBPS/testi/foo.bar</code></p>
    <p>That is: two levels (up from a file or from a directory count as one level here) get eaten, one is added, 
      and then the filename is changed to what is in the 'add' parameter.</p>
    <p>Try other values of add – without path, without reference to '..', without file name, …</p>
    <p>Try other values of base – with or without trailing slash.</p>
  </p:documentation>
  
  <p:in-scope-names name="opts"/>
  
  <p:xslt template-name="test">
    <p:input port="stylesheet">
      <p:inline>
				<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
				  xmlns:xs="http://www.w3.org/2001/XMLSchema"
					xmlns:tr="http://transpect.io" 
					exclude-result-prefixes="#all" version="2.0">
					<xsl:import href="../xsl/resolve-uri.xsl"/>

					<xsl:param name="base" as="xs:string"/>
					<xsl:param name="add" as="xs:string"/>

					<xsl:template name="test">
						<xsl:message select="tr:uri-composer($base, $add)"/>
					</xsl:template>

				</xsl:stylesheet>
      </p:inline>
    </p:input>
    <p:input port="parameters">
      <p:pipe port="result" step="opts"/>
    </p:input>
    <p:input port="source"><p:empty/></p:input>
  </p:xslt>
  
  <p:sink/>
  
</p:declare-step>