<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  version="1.0">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>This step splits an URI into its components. Thus it 
    provides additional information about the URI, e.g.</p>
    <p><code>https://mike@www.le-tex.de:8080/public/manuals/appliances?disp=mobile#section-2</code></p>
    <p>converts to:</p>
    <pre>
      <c:param-set xmlns:c="http://www.w3.org/ns/xproc-step"
        href="https://mike@www.le-tex.de:8080/public/manuals/appliances?disp=mobile#section-2" 
        scheme="https" 
        user="mike" 
        host="www.le-tex.de" 
        port="8080" 
        path="/public/manuals/appliances" 
        query="disp=mobile" 
        fragment="section-2">
        <c:param name="disp" value="mobile"/>
      </c:param-set>
    </pre>
  </p:documentation>

  <p:output port="result">
    <p:documentation>A c:param-set including URI parts and query parameters.</p:documentation>
  </p:output>

  <p:option name="href">
    <p:documentation>Any URI</p:documentation>
  </p:option>

  <p:xslt template-name="main">
    <p:with-param name="href" select="$href"/>
    <p:input port="stylesheet">
      <p:document href="../xsl/decompose-uri.xsl"/>
    </p:input>
    <p:input port="source">
      <p:empty/>
    </p:input>
  </p:xslt>

</p:declare-step>
