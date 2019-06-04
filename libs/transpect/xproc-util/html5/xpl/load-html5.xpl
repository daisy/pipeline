<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step 
	xmlns:p="http://www.w3.org/ns/xproc"
	xmlns:c="http://www.w3.org/ns/xproc-step" 
	xmlns:cx="http://xmlcalabash.com/ns/extensions"
	xmlns:tr="http://transpect.io"
	xmlns:html="http://www.w3.org/1999/xhtml"
	version="1.0"
	name="load-html5"
	type="tr:load-html5">
	
	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>Uses validator.nu that is bundled with Calabash to parse HTML5 (both HTML and XML serializations) files.
		The files must have a single top-level element. They don’t need to have <code>html</code> as their top-level
		element though. <code>body</code>, <code>section</code> etc. are also acceptable.</p>
	</p:documentation>
  	
	<p:output port="result" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>An XML document in the XHTML namespace, <code>http://www.w3.org/1999/xhtml</code>.</p>
		</p:documentation>
	</p:output>
  <p:serialization port="result" omit-xml-declaration="false" method="xhtml"/>

  <p:option name="file" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<p>URI or file system path, may also be relative (will then be resolved to the folder 
			  above the calabash front end script’s folder).</p>
		</p:documentation>
  </p:option>

  <p:option name="debug" select="'no'"/>

  <p:option name="debug-dir-uri" select="'debug'"/>
  
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl" />
  <p:import href="http://transpect.io/xproc-util/file-uri/xpl/file-uri.xpl" />
  
  <tr:file-uri name="file-uri">
    <p:with-option name="filename" select="$file"/>
  </tr:file-uri>
  
  <p:sink/>

  <p:add-attribute attribute-name="href" match="/*">
    <p:input port="source">
      <p:inline>
        <c:request method="GET"/>
      </p:inline>
    </p:input>
    <p:with-option name="attribute-value" select="/*/@local-href">
      <p:pipe port="result" step="file-uri"/>
    </p:with-option>
  </p:add-attribute>
  
  <p:http-request/>
  
  <p:unescape-markup content-type="text/html"/>
  
  <p:filter select="/c:body/html:*"/>

  <p:xslt name="strip-ns-decl">
    <p:input port="parameters"><p:empty/></p:input>
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
          <xsl:template match="/*">
            <xsl:copy-of select="." copy-namespaces="no"/>
          </xsl:template>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt>

  <p:add-attribute match="/*" attribute-name="xml:base">
    <p:with-option name="attribute-value" select="/*/@local-href">
      <p:pipe port="result" step="file-uri"/>
    </p:with-option>
  </p:add-attribute>

  <tr:store-debug>
    <p:with-option name="pipeline-step" select="concat('html5/load/', /*/@lastpath)">
      <p:pipe port="result" step="file-uri"/>
    </p:with-option>
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>

</p:declare-step>