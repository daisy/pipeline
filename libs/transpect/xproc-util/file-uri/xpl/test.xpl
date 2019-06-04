<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:tr="http://transpect.io"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  version="1.0">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <h4>Sample results for <code class="step">tr:file-uri</code></h4>
  </p:documentation>

  <p:option name="filename" required="false" select="'http://transpect.io/xproc-util/file-uri/xpl/file-uri.xpl'"/>
  
  <p:output port="result" primary="true"/>
  
  <p:import href="file-uri.xpl"/>
  

  <tr:file-uri name="current-dir" filename=".">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Current directory (resolved against the static base URI). Sample output:</p> 
<pre><code>&lt;c:result 
  local-href="file:/C:/cygwin/home/gerrit/Dev/tmp/file-uri/" 
  os-path="C:/cygwin/home/gerrit/Dev/tmp/file-uri/"/></code></pre> 
    </p:documentation>
  </tr:file-uri>

  <p:sink/>
 
  <tr:file-uri name="without-catalog">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The filename is an http: URL that may or may not be resolved by the standard XML catalog resolver.
      For reasons explained there, the <code class="step">tr:file-uri</code> step does not use the 
        standard catalog resolution. Sample output:</p> 
<pre><code>&lt;c:result 
  error-status="404" 
  href="http://transpect.io/xproc-util/file-uri/xpl/file-uri.xpl"/></code></pre> 
    </p:documentation>
    <p:with-option name="filename" select="$filename"/>
  </tr:file-uri>
  
  <p:sink/>
  
  <tr:file-uri name="with-catalog">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>XML catalog supplied to the catalog port. Sample output:</p>
      <pre><code>&lt;c:result 
  local-href="file:/C:/cygwin/home/gerrit/Dev/tmp/file-uri/xpl/file-uri.xpl"  
  os-path="C:/cygwin/home/gerrit/Dev/tmp/file-uri/xpl/file-uri.xpl"
  orig-href="http://transpect.io/xproc-util/file-uri/xpl/file-uri.xpl"/></code></pre>
    </p:documentation>
    
    <p:with-option name="filename" select="$filename"/>
    <p:input port="catalog">
      <p:document href="../xmlcatalog/catalog.xml"/>
    </p:input>
    <p:input port="resolver">
      <p:document href="http://rawgit.com/transpect/xslt-util/master/xslt-based-catalog-resolver/xsl/resolve-uri-by-catalog.xsl"/>
    </p:input>
  </tr:file-uri>
 
  <p:sink/>
 
  <tr:file-uri name="via-http-request" filename="http://rawgit.com/transpect/xproc-util/master/file-uri/xpl/file-uri.xpl">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Unresolved, retrievable HTTP URL. Sample output:</p> 
<pre><code>&lt;c:result 
  local-href="file:/C:/Users/gerrit/tmp/file-uri_3957da2c.xpl" 
  os-path="C:/Users/gerrit/tmp/file-uri_3957da2c.xpl" 
  href="https://subversion.le-tex.de/common/xproc-util/file-uri/file-uri.xpl"/></code></pre>
    </p:documentation>
  </tr:file-uri>
  
  <p:wrap-sequence wrapper="c:results">
    <p:input port="source">
      <p:pipe port="result" step="current-dir"/>
      <p:pipe port="result" step="without-catalog"/>
      <p:pipe port="result" step="with-catalog"/>
      <p:pipe port="result" step="via-http-request"/>
    </p:input>
  </p:wrap-sequence>
  
</p:declare-step>