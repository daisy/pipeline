<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dbk="http://docbook.org/ns/docbook"
  xmlns:css="http://www.w3.org/1996/css"
  xmlns:tr="http://transpect.io"
	xmlns:csstmp="http://transpect.io/csstmp"
  version="1.0"
  name="merge-hub"
  type="tr:merge-hub">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
		<p>This step expects a sequence of Hub files and merges them to one single file.</p>
	</p:documentation>
    
  <p:input port="source" primary="true" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2>Input <i>source</i></h2>
			<p>The input port expects a sequence of Hub documents.</p>
		</p:documentation>
  </p:input>
  
  <p:output port="result" primary="true">
		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
			<h2>Output <i>result</i></h2>
			<p>The result port provides a single Hub document.</p>
		</p:documentation>
	  <p:pipe port="result" step="merge"/>
	</p:output>
  
  <p:output port="report">
    <p:documentation xmlns="htttp://www.w3.org/1999/xhtml">
      <h2>Output <i>result</i></h2>
      <p>The report port provides a Schematron SVRL document.</p>
    </p:documentation>
    <p:pipe port="result" step="generate-report"/>
  </p:output>
  
  <p:option name="debug" required="false" select="'no'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Option: <code>debug</code></h3>
      <p>Used to switch debug mode on or off. Pass 'yes' to enable debug mode.</p>
    </p:documentation>
  </p:option>
  <p:option name="debug-dir-uri" select="'debug'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Option: <code>debug-dir-uri</code></h3>
      <p>Expects a file URI of the directory that should be used to store debug information.</p> 
    </p:documentation>
  </p:option>
 
  <p:option name="move-dir-components-to-srcpath" required="false" select="0">
    <p:documentation>When merging Hub documents, the info block of the first document will
      be used for the whole document. Styles that are present in the other documents but not
      in the first will be merged into the info/css:rules block of the first document.
      We don’t test for deviating style definitions yet.
      For the sake of keeping the @srcpath attributes unique, we provide an option where
      you can specify that you want to move the last X path components of 
      info/keywordset[@role = 'hub']/keyword[@role = 'source-dir-uri'] to the @srcpath attributes.
      If the source-dir-uri is 'file:/C:/cygwin/home/gerrit/CustomerA/ProjectB/tmp/fileC.docx.tmp/',
      an option of move-dir-components-to-srcpath="1" will effect that the source-dir-uri in the
      merged document is 'file:/C:/cygwin/home/gerrit/CustomerA/ProjectB/tmp/' and the @srcpath
      attributes look like 'fileC.docx.tmp/document.xml?xpath=/w:document/…'.
    </p:documentation>
  </p:option>
  
  <p:option name="space-separated-docVar-merge" required="false" select="''">
    <p:documentation>values of docVars occuring in multiple Hub documents with the same role can be merged into a 
      semicolon separated list. This option contains a list of roles that are treated this way.</p:documentation>
  </p:option>
  
  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl" />
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl" />

  <p:wrap-sequence wrapper="document" wrapper-namespace="http://xmlcalabash.com/ns/extensions" wrapper-prefix="cx"/>
	
  <tr:store-debug pipeline-step="merge-hub/pre-merge">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <p:xslt initial-mode="merge-hub" name="merge">
    <p:with-param name="move-dir-components-to-srcpath" select="$move-dir-components-to-srcpath"><p:empty/></p:with-param>
    <p:with-param name="space-separated-docVar-merge" select="$space-separated-docVar-merge"><p:empty/></p:with-param>
    <p:input port="parameters"><p:empty/></p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/merge-hub.xsl"/>
    </p:input>
  </p:xslt>
  
  <p:delete match="*[@csstmp:*]"/>
  
  <tr:store-debug pipeline-step="merge-hub/post-merge">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <p:sink/>
  
  <p:xslt initial-mode="generate-report" name="generate-report">
    <p:input port="source">
      <p:pipe port="result" step="merge"/>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:document href="../xsl/merge-hub.xsl"/>
    </p:input>
  </p:xslt>

</p:declare-step>
