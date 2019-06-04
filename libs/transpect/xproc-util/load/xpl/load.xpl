<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="load"
  type="tr:load">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Replacement for <c:body>p:load</c:body>. Uses the file-uri util to load any file
      without using resolve-uri or other inconveniend ways. A relative
      file (param href) will be loaded relative to the current working directory. Please
      note, there is no input port. The document is loaded via <code>href</code> option.</p>
  </p:documentation>

  <p:option name="href" required="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Option: <code>href</code></h3>
      <p>Mandatory, expects the path to the file.</p>
    </p:documentation>
  </p:option>
  
  <p:option name="dtd-validate" select="'false'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Option: <code>dtd-validate</code></h3>
      <p>Validate with the declared DTD. Please ensure that the XML DTD reference 
        can be properly resolved. Use your XML catalog to rewrite either the system id 
        or the public id of the DTD to a URI.</p>
    </p:documentation>
  </p:option>
  
  <p:option name="fail-on-error" select="'yes'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Option: <code>fail-on-error</code></h3>
      <p>Optional, if set to 'yes', the pipeline terminates on a load error.</p>
    </p:documentation>
  </p:option>
  
  <p:output port="result" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <h3>Output port: <code>result</code></h3>
      <p>The output is either the loaded XML document or a c:errors document.</p>
    </p:documentation>
  </p:output>
  
  <p:import href="http://transpect.io/xproc-util/file-uri/xpl/file-uri.xpl"/>
  
  <!--  *
        * retrieve the absolute URI from the file path
        * -->
  
  <tr:file-uri name="retrieve-absolute-file-uri-href">
    <p:with-option name="filename" select="$href"/>
  </tr:file-uri>
  
  <p:group>
    <p:variable name="absolute-file-uri-href" select="/c:result/@local-href">
      <p:pipe port="result" step="retrieve-absolute-file-uri-href"/>
    </p:variable>
    
  <p:try>
    <p:group>
      
      <p:load>
        <p:with-option name="href" select="$absolute-file-uri-href">
          <p:pipe port="result" step="retrieve-absolute-file-uri-href"/>
        </p:with-option>
        <p:with-option name="dtd-validate" select="$dtd-validate"/>
      </p:load>
      
      
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
        <p:when test="$fail-on-error eq 'yes'">
          
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
  
  <!--  *
        * attach the file URI as attribute to ease postprocessing.
        * -->
  
  <p:add-attribute attribute-name="xml:base" match="/*">
    <p:with-option name="attribute-value" select="$absolute-file-uri-href"/>
  </p:add-attribute>
  
  </p:group>
  
</p:declare-step>
