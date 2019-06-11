<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:j="http://marklogic.com/json"
  xmlns:tr="http://transpect.io"
  version="1.0" 
  name="virustotal" 
  type="tr:virustotal">
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Send local files to Virus Total and get a Schematron report.</p>
    <p>This XProc step use the Total Virus API to upload and scan local 
      files. Scan reports are retrieved with <code>p:http-request</code> and 
      validated with Schematron.</p>
    <p>Note: requires a Virus Total account, please see option <code>api-key</code>.</p>
    <pre>$ sh calabash/calabash.sh -Xtransparent-json -Xjson-flavor=marklogic \
      -o report=svrl.xml virustotal.xpl href=test.txt api-key=myRandomKey</pre>
    <p>Get more information on Virus Total at https://www.virustotal.com</p>
  </p:documentation>
  
  <p:output port="result" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <dl><dt>result</dt><dd>This port provides a JSON XML 
        representation of the scan results.</dd></dl>
    </p:documentation>
  </p:output>
  
  <p:output port="report" primary="false" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <dl><dt>report</dt><dd>This port provides a SVRL report.</dd></dl>
    </p:documentation>
    <p:pipe port="report" step="validate-scan-results"/>
  </p:output>
  
  <p:option name="scan-url" select="'https://www.virustotal.com/vtapi/v2/file/scan'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <dl><dt>url</dt><dd>The URL of the scan request.</dd></dl>
    </p:documentation>
  </p:option>
  
  <p:option name="report-url" select="'https://www.virustotal.com/vtapi/v2/file/report'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <dl><dt>url</dt><dd>The URL of the report request.</dd></dl>
    </p:documentation>
  </p:option>
  
  <p:option name="href">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <dl><dt>href</dt><dd>The path of the file to be checked.</dd></dl>
    </p:documentation>
  </p:option>
  
  <p:option name="api-key" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <dl><dt>api-key</dt><dd>This option expects your personal virustotal API key. You can obtain 
        an API key by register an user account at https://www.virustotal.com/. Later you can receive 
        your API key here: https://www.virustotal.com/de/user/MYUSERNAME/apikey/</dd></dl>
    </p:documentation>
  </p:option>
  
  <p:choose>
    <p:when test="string-length($api-key) eq 0">
      
      <p:error code="apikey">
        <p:input port="source">
          <p:inline>
            <c:error>Please provide a Virus Total API key. Please register and obtain a key here: https://www.virustotal.com</c:error>
          </p:inline>
        </p:input>
      </p:error>
      
    </p:when>
    <p:otherwise>  
      
      <p:identity>
        <p:input port="source">
          <p:inline><c:other>OK</c:other></p:inline>
        </p:input>
      </p:identity>
      
    </p:otherwise>
  </p:choose>
  
  <!--  *
        * load local file
        * -->
  
  <p:add-attribute attribute-name="href" match="/c:request" name="construct-http-request">
    <p:with-option name="attribute-value" select="$href"/>
    <p:input port="source">
      <p:inline>
        <c:request method="GET" detailed="true"/>
      </p:inline>
    </p:input>
  </p:add-attribute>
  
  <p:http-request name="get-local-file"/>

  <!--  *
        * submit for scanning
        * -->

  <p:group>
    <p:variable name="content-type" select="/c:body/@content-type"/>
    
    <p:in-scope-names name="vars"/>
    
    <p:template>
      <p:input port="template">
        <p:inline>
          <c:request method="POST" href="{$scan-url}">
            <c:header name="api-key" value="{$api-key}"/>
            <c:multipart content-type="multipart/form-data" boundary="=-=-=-=-=">
              <c:body content-type="{$content-type}"
                disposition='form-data; name="file"; filename="{$href}"'
                >{/c:body/text()}</c:body>
            </c:multipart>
          </c:request>
        </p:inline>
      </p:input>
      <p:input port="source">
        <p:pipe port="result" step="get-local-file"/>
      </p:input>
      <p:input port="parameters">
        <p:pipe port="result" step="vars"/>
      </p:input>
    </p:template>
    
  </p:group>
  
  <p:http-request/>
  
  <!--  *
        * retrieving file scan reports
        * -->
  
  <p:template>
    <p:input port="template">
      <p:inline>
        <c:request method="POST" href="{$report-url}">
          <c:body content-type="application/x-www-form-urlencoded">apikey={$api-key}&amp;resource={/j:json/j:resource/text()}</c:body>
        </c:request>
      </p:inline>
    </p:input>
    <p:with-param name="report-url" select="$report-url"/>
    <p:with-param name="api-key" select="$api-key"/>
  </p:template>
  
  <p:http-request/>
  
  <!--  *
        * validate results with schematron
        * -->
  
  <p:validate-with-schematron assert-valid="false" name="validate-scan-results">
    <p:input port="schema">
      <p:inline>
        <sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
          
          <sch:ns uri="http://marklogic.com/json" prefix="j"/>
          
          <sch:pattern>
            <sch:rule context="/j:json/j:scans/j:*">
              <sch:let name="scanner" value="local-name()"/>
              
              <sch:assert test="j:detected eq 'false'">
                Detected a virus while scanning with '<sch:value-of select="$scanner"/>'.
              </sch:assert>
              
            </sch:rule>
            
          </sch:pattern>
          
        </sch:schema>
      </p:inline>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:validate-with-schematron>
    
</p:declare-step>