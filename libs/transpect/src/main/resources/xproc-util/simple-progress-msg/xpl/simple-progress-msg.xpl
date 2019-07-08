<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:pkg="http://expath.org/ns/pkg" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:epub="http://transpect.io/epubtools" 
  xmlns:idml2xml="http://transpect.io/idml2xml"
  xmlns:tr="http://transpect.io" 
  version="1.0">

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>

  <p:declare-step type="tr:simple-progress-msg" name="progress-msg">
  	
  	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
  		<p>This step stores status messages as plain text files and prints them to the standard output (GI 2018-09-13: The 
  		  latter doesn’t seem to be true). The step can be used everywhere in your pipeline. The input will be simply forwarded 
  		  to the output without any transformations.</p>
  		<p>The input port entitled "msgs" expects a <code>c:message</code> XML document. The status messages must be wrapped 
  		  in <code>c:message</code> elements.</p>
  		<p>For localized messages, you can use multiple <code>c:message</code> elements each including a <code>xml:lang</code> 
  		  attribute. The attribute value must be a language code according to ISO 639-1.</p>
  		<pre><code>&lt;tr:simple-progress-msg file="trdemo-paths.txt">
  &lt;p:input port="msgs">
    &lt;p:inline>
      &lt;c:messages>
        &lt;c:message xml:lang="en">Generating File Paths&lt;/c:message>
        &lt;c:message xml:lang="de">Generiere Dateisystempfade&lt;/c:message>
      &lt;/c:messages>
    &lt;/p:inline>
  &lt;/p:input>
  &lt;p:with-option name="status-dir-uri" select="$status-dir-uri"/>
&lt;/tr:simple-progress-msg></code></pre>
  	  <p>Sometimes you might want to switch off storing of messages altogether. You can do this by appending
  	  '?enabled=false' to the URI.</p>
  	</p:documentation>

    <p:input port="source" primary="true" sequence="true">
    	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
    		<h3>Input port: <code>source</code></h3>
    		<p>Input will simply be passed thru. This allows insertion of this step into current pipelines with minimal
    			effort.</p>
    	</p:documentation>
      <p:empty/>
    </p:input>
    <p:output port="result" primary="true" sequence="true">
    	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
    		<h3>Output port: <code>result</code></h3>
    		<p>Output is reproduced from the input port without any transformations.</p>
    	</p:documentation>
      <p:pipe port="source" step="progress-msg"/>
    </p:output>
  	<p:input port="msgs" xmlns="http://www.w3.org/1999/xhtml">
      <p:documentation>
      	<h3>Input port: <code>msgs</code></h3>
      	<p>A <code>c:messages</code> document with <c:body>c:message</c:body> elements. Each element may have a <code>xml:lang</code> and a name attribute.</p>
			</p:documentation>
    </p:input>

    <p:option name="file">
    	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
    		<h3>Option: <code>file</code></h3>
    		<p>This option sets the filename of the text file containing the status message.</p>
    	</p:documentation>
    </p:option>
  	
    <p:option name="status-dir-uri" select="'status?enabled=false'">
    	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
    		<h3>Option: <code>status-dir-uri</code></h3>
    		<p>This option expects a file: URI. The localized message is saved to this URI as text.</p>
    	  <p>Optionally add '?enabled=false' to the URI in order to deactivate this storage. It can be useful
    	  for situations in which a writable directory cannot be taken for granted.</p>
    	</p:documentation>
    </p:option>
    
    <p:choose>
      <p:xpath-context>
        <p:empty/>
      </p:xpath-context>
      <p:when test="matches($status-dir-uri, '\?.*enabled=false')">
        <p:sink/>
      </p:when>
      <p:otherwise>
        <p:variable name="lang" select="replace(p:system-property('p:language'), '[-].+$', '')">
          <p:empty/>
        </p:variable>
        <p:variable name="status-dir-uri-without-query-string" select="replace($status-dir-uri, '\?.*$', '')">
          <p:empty/>
        </p:variable>
    
        <p:xslt>
          <p:input port="source">
            <p:pipe port="msgs" step="progress-msg"/>
          </p:input>
          <p:input port="parameters">
            <p:empty/>
          </p:input>
          <p:with-param name="lang" select="$lang">
            <p:empty/>
          </p:with-param>
          <p:input port="stylesheet">
            <p:inline>
  	            <xsl:stylesheet version="2.0">
          					<xsl:param name="lang" as="xs:string"/>
          					<xsl:param name="basename" select="''"/>
          					<xsl:template match="/">
          					  <c:message>
          					    <xsl:value-of
          					      select="replace((//c:message[@xml:lang eq $lang], //c:message)[1], '(.)[&#xa;&#xd;]*$', '$1&#xa;')"/>
          					  </c:message>
          					</xsl:template>
          			</xsl:stylesheet>
      			</p:inline>
          </p:input>
        </p:xslt>
        <p:store method="text">
          <p:with-option name="href" 
            select="concat($status-dir-uri-without-query-string, '/', concat(p:system-property('p:episode'), '_', $file))"/>
        </p:store>
        
      </p:otherwise>
    </p:choose>

  </p:declare-step>

  <p:declare-step type="tr:propagate-caught-error" name="propagate-caught-error">
  	
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>This step redirects an error to a status text file and prints a <code>cx:message</code>. If option
          <code>fail-on-error</code> is set to <code>true</code>, the error is reproduced with an attached error code.</p>
    </p:documentation>
  	
    <p:input port="source" primary="true">
    	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
      	<h3>Input port: <code>source</code></h3>
      	<p>A <code>c:errors</code> document that is on the error port of a p:catch step</p>
      </p:documentation>
    </p:input>
  	
    <p:output port="result" primary="true">
    	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
    		<h3>Output port: <code>result</code></h3>
    		<p>If option <code>fail-on-error</code> is not set to <code>true</code>, then the output is a <code>c:errors</code> document. Additionally, two attributes are attached:</p>
    		<ul>
    			<li><code>role</code>: severity of the error</li>
    			<li><code>code</code>: error code</li>
    		</ul>
    	</p:documentation>
    </p:output>

  	<p:option name="fail-on-error" required="false" select="'false'">
  		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
  			<h3>Option: <code>fail-on-error</code></h3>
  			<p>If this option is set to <code>true</code>, the pipeline fails with a <code>p:error</code>.</p>
  		</p:documentation>
  	</p:option>
    
    <p:option name="rule-family" required="false" select="'internal'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h3>Option: <code>rule-family</code></h3>
        <p>Specifies the category of the errors.</p>
      </p:documentation>
    </p:option>

    <p:option name="code" required="false" select="'tr:UNSP01'">
    	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
    		<h3>Option: <code>code</code></h3>
    		<p>Sets the error code, that is attached as code attribute to the <code>c:errors</code> document.</p>
    	</p:documentation>
    </p:option>
    
    <p:option name="step-type" required="false">
  		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
  			<h3>Option: <code>step-type</code></h3>
  			<p>The name of the step in which the error occurred. More diagnostic information may be provided 
  			in the message itself (that may contain almost arbitrary elements).</p>
  		</p:documentation>
  	</p:option>
  	
    <p:option name="severity" required="false" select="'fatal-error'">
      <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h3>Option: <code>severity</code></h3>
        <p>Sets the severity, that is attached as role attribute to the <code>c:errors</code> document.</p>
      </p:documentation>
    </p:option>
  	
    <p:option name="msg-file" required="false" select="'unspecified-error.txt'">
    	<p:documentation xmlns="http://www.w3.org/1999/xhtml">
    		<h3>Option: <code>msg-file</code></h3>
    		<p>Filename of the plain text file, that contains the error message.</p>
    	</p:documentation>
    </p:option>
  	
  	<p:option name="status-dir-uri" required="false" select="'debug/status?enabled=false'">
  		<p:documentation xmlns="http://www.w3.org/1999/xhtml">
  			<h3>Option: <code>status-dir-uri</code></h3>
  			<p>URI of the directory, where the status message file is stored.</p>
  		</p:documentation>
  	</p:option>

    <p:xslt name="error-msg">
      <p:input port="source">
        <p:pipe port="source" step="propagate-caught-error"/>
      </p:input>
      <p:input port="parameters">
        <p:empty/>
      </p:input>
      <p:input port="stylesheet">
        <p:document href="../xsl/error-msg.xsl"/>
      </p:input>
    </p:xslt>
    
    <tr:simple-progress-msg name="write-progress-msg">
      <p:with-option name="file" select="$msg-file"/>
      <p:input port="msgs">
        <p:pipe port="result" step="error-msg"/>
      </p:input>
      <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
    </tr:simple-progress-msg>
    
    <cx:message name="output-error-msg" cx:depends-on="write-progress-msg">
      <p:with-option name="message" select="/c:errors">
        <p:pipe port="source" step="propagate-caught-error"/>
      </p:with-option>
    </cx:message>
    
    <p:sink/>
    
    <p:add-attribute name="add-family" attribute-name="tr:rule-family" match="/c:errors">
      <p:with-option name="attribute-value" select="$rule-family"/>
      <p:input port="source">
        <p:pipe port="source" step="propagate-caught-error"/>
      </p:input>
    </p:add-attribute>
    
    <p:add-attribute name="add-code" attribute-name="code" match="/c:errors/c:error[last()]">
      <p:with-option name="attribute-value" select="$code"/>
    </p:add-attribute>
    
    <p:choose>
      <p:when test="p:value-available('step-type')">
        <p:add-attribute name="add-step-type" attribute-name="type" match="/c:errors/c:error[last()]">
          <p:with-option name="attribute-value" select="$step-type"/>
        </p:add-attribute>    
      </p:when>
      <p:otherwise>
        <p:identity/>
      </p:otherwise>
    </p:choose>
    
    <p:add-attribute name="add-severity" attribute-name="role" match="/c:errors/c:error[last()]">
      <p:with-option name="attribute-value" select="$severity"/>
    </p:add-attribute>
    
    <p:choose>
      <p:when test="$fail-on-error = 'true'">
        <p:error cx:depends-on="output-error-msg">
          <p:documentation>If you use a prefix in the error code, you might have to declare it in this file.</p:documentation>
          <p:with-option name="code" select="$code"/>
          <p:input port="source">
            <p:pipe port="result" step="add-severity"/>
          </p:input>
        </p:error>
      </p:when>
      <p:otherwise>
        <p:identity>
          <p:input port="source">
            <p:pipe port="result" step="add-severity"/>  
          </p:input>
        </p:identity>
      </p:otherwise>
    </p:choose>
  </p:declare-step>
  
</p:library>
