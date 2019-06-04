<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="load-data"
  type="tr:load-data">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>This step loads a file via http-request</p>
  </p:documentation>

  <p:option name="href" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Option: <code>href</code></h3>
      <p>Mandatory, expects the path to the file.</p>
    </p:documentation>
  </p:option>
  
  <p:option name="content-type-override" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Option: <code>content-type-override</code></h3>
      <p>You can use this option to submit a content-type override.</p>
    </p:documentation>
  </p:option>
  
  <p:option name="encoding" select="'base64'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Option: <code>encoding</code></h3>
      <p>Encoding of the file to be loaded.</p>
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
      <p>The output is either the loaded text wrapped in c:data document or a c:errors document.</p>
    </p:documentation>
  </p:output>

  <p:import href="http://transpect.io/xproc-util/file-uri/xpl/file-uri.xpl"/>
  
  <p:try>
    <p:group>
      
      
      <!--  *
            * retrieve the absolute URI from the file path
            * -->
      <tr:file-uri name="retrieve-absolute-file-uri-href">
        <p:with-option name="filename" select="$href"/>
      </tr:file-uri>
      
      <!--  * 
            * construct HTTP request 
            * -->
      <p:add-attribute attribute-name="href" match="/c:request" name="construct-http-request">
        <p:with-option name="attribute-value" select="/*/@local-href"/>
        <p:input port="source">
          <p:inline>
            <c:request method="GET" detailed="true"/>
          </p:inline>
        </p:input>
      </p:add-attribute>

      <!--  * 
            * conditionally force content-type
            * -->
      <p:choose>
        <p:when test="string-length($content-type-override) gt 0">
          <p:add-attribute attribute-name="override-content-type" match="/c:request">
            <p:with-option name="attribute-value" select="$content-type-override"/>
          </p:add-attribute>
        </p:when>
        <p:otherwise>
          <p:identity/>
        </p:otherwise>
      </p:choose>
      
      <p:identity name="http-request"/>
      
      <!--  * 
            * perform HTTP request to load file 
            * -->
      <p:http-request>
        <p:with-option name="encoding" select="$encoding"/>
        <p:input port="source">
          <p:pipe port="result" step="http-request"/>
        </p:input>
      </p:http-request>
      
    </p:group>
    <!--  *
          * recover from loading errors
          * -->
    <p:catch name="catch">
      
      <!--  *
            * if fail-on-error is set to 'true', this pipeline terminates 
            * with an error message. Otherwise a c:errors document is generated.
            * -->
      <p:choose name="choose">
        <p:when test="$fail-on-error eq 'true'">
          
          <p:error code="load-error">
            <p:input port="source">
              <p:pipe step="catch" port="error"/>
            </p:input>
          </p:error>
          
        </p:when>
        <p:otherwise>
          
          <p:identity name="copy-errors">
            <p:input port="source">
              <p:pipe step="catch" port="error"/>
            </p:input>
          </p:identity>
          
        </p:otherwise>
      </p:choose>
      
    </p:catch>
    
  </p:try>

</p:declare-step>