<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="load-html"
  type="tr:load-html">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>This step implements TagSoup to load even not well-formed HTML files. To use TagSoup with 
      Calabash, you must include the TagSoup JAR file in your Java classpath and use a Calabash configuration file.</p>
    <pre>
      <code>
        &lt;cc:xproc-config xmlns:cc="http://xmlcalabash.com/ns/configuration" xmlns:tr="http://transpect.io"&gt;
          &lt;cc:html-parser value="tagsoup"/&gt;
        &lt;/cc:xproc-config&gt;
      </code>
    </pre>
  </p:documentation>

  <p:option name="href" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Option: <code>href</code></h3>
      <p>Mandatory, expects the path to the file.</p>
    </p:documentation>
  </p:option>
  
  <p:option name="fail-on-error" select="'false'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Option: <code>fail-on-error</code></h3>
      <p>Optional, if set to 'true', the pipeline terminates on a load error.</p>
    </p:documentation>
  </p:option>
  
  <p:output port="result" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Output port: <code>result</code></h3>
      <p>The output is either the loaded XML document or a c:errors document.</p>
    </p:documentation>
  </p:output>
  
  <p:import href="http://transpect.io/xproc-util/load/xpl/load.xpl"/>
  
  <!--  *
        * first use tr:load to load file 
        * -->
  
  <tr:load>
    <p:with-option name="href" select="$href"/>
    <p:with-option name="fail-on-error" select="$fail-on-error"/>
  </tr:load>
  
  <!--  *
        * if tr:load fails to load a file, it recovers from the error and 
        * generates an error document. In this case we assume that the file is not 
        * well-formed. Otherwise the input is cloned in the otherwise branch
        * -->
  <p:choose>
    <p:variable name="absolute-file-uri-href" select="/*/@local-href"/>
    <p:when test="/c:errors">
            
      <!--  * 
            * construct HTTP request 
            * -->
      <p:add-attribute attribute-name="href" match="/c:request" name="construct-http-request">
        <p:with-option name="attribute-value" select="$absolute-file-uri-href"/>
        <p:input port="source">
          <p:inline>
            <c:request method="GET" detailed="true" />
          </p:inline>
        </p:input>
      </p:add-attribute>
      
      <!--  * 
            * perform HTTP request to load file 
            * -->
      <p:http-request>
        <p:input port="source">
          <p:pipe port="result" step="construct-http-request"/>
        </p:input>
      </p:http-request>
      
      <!--  *
            * to read not well-formed HTML, the content-type of p:unescape-markup must 
            * be set to 'text/html' and TagSoup must be selected as HTML parser in the 
            * Calabash configuration (usually in a9s/common/calabash/xproc-config.xml)
            * -->
  
      <p:unescape-markup content-type="text/html"/>
      
      <p:unwrap match="/c:body"/>
      
    </p:when>
    <p:otherwise>
      
      <p:identity/>
      
    </p:otherwise>
  </p:choose>

</p:declare-step>