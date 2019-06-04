<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:cx="http://xmlcalabash.com/ns/extensions" 
  xmlns:tr="http://transpect.io"
  version="1.0" 
  name="get-clades-from-dirs" 
  type="tr:get-clades-from-dirs">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>This step converts a transpect clades document from the output
    of <code>tr:directory-loop</code>. The clades document can be used
    as input for the <code>tr:paths</code> step.</p>
    <p>Parameter sets can be stored as <code>params.xml</code> in the corresponding 
    directory. There are predefined parameters which are used to control how the 
    clades are generated.</p>
    <pre><code>&lt;c:param-set xmlns:c="http://www.w3.org/ns/xproc-step">
&lt;c:param name="exclude-filter" value="'(xsl|xpl|schematron)'"/>&lt;-- exclude directories from cascade -->
&lt;c:param name="clade-role" value="global"/>&lt;-- assign a specific clade role to the directory -->
&lt;/c:param-set></code></pre>
  </p:documentation>
  
  <p:input port="params">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The initial params document. It's base URI is taken to iterate over 
        the subdirectories and construct the clade document.</p>
    </p:documentation>
  </p:input>
  
  <p:output port="result" primary="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The clades document</p>
    </p:documentation>
  </p:output>
  
  <p:output port="directory-param-sets" primary="false">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Listing of directories including expanded parameter-sets.</p>
    </p:documentation>
  </p:output>
  
  <p:option name="resolve-params" select="'yes'">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Whether parameter references in parameters should be resolved, e.g.</p>
      <pre><code>&lt;c:param name="isbn" value="(97[8|9])\d{10}"/>
&lt;c:param name="book-id" value="book_{$isbn}"/>  
      </code></pre>
      <p>Resolving the parameter <code>{$isbn}</code> above, would lead to this result:</p>
      <pre><code>&lt;c:param name="isbn" value="(97[8|9])\d{10}"/>
&lt;c:param name="book-id" value="book_((97[8|9])\d{10})"/>  
      </code></pre>
    </p:documentation>
  </p:option>
  
  <p:option name="debug" select="'yes'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>  
  <p:option name="status-dir-uri" select="concat($debug-dir-uri, '/status')"/>
  
  <p:import href="directory-loop.xpl"/>
  <p:import href="http://transpect.io/xproc-util/resolve-params/xpl/resolve-params.xpl"/>
  <p:import href="http://transpect.io/xproc-util/simple-progress-msg/xpl/simple-progress-msg.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl"/>
    
  <tr:simple-progress-msg file="trdemo-paths.txt">
    <p:input port="msgs">
      <p:inline>
        <c:messages>
          <c:message xml:lang="en">Read configuration from file system</c:message>
          <c:message xml:lang="de">Lese Konfiguration vom Dateisystem</c:message>
        </c:messages>
      </p:inline>
    </p:input>
    <p:with-option name="status-dir-uri" select="$status-dir-uri"/>
  </tr:simple-progress-msg>
  
  <!--  *
        * the locaction of the parameter document is the initial point of the cascade.
        * -->
  
  <tr:directory-loop>
    <p:with-option name="path" select="replace(base-uri(/c:param-set), '^(.+)/.+$', '$1')"/>
    <p:with-option name="exclude-filter" select="/c:param-set/c:param[@name eq 'exclude-filter']/@value"/>
  </tr:directory-loop>
  
  <tr:store-debug pipeline-step="cascade/directory-loop">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <p:choose>
    <p:when test="$resolve-params = ('yes', 'true')">
      
      <!-- expand all parameter sets, e.g. add parameters from upper levels (in scope) -->
      
      <p:xslt>
        <p:input port="stylesheet">
          <p:inline>
            <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
              <xsl:template match="c:param-set">
                <xsl:variable name="param-sets-in-scope" 
                  select="parent::c:directory/ancestor::c:directory/c:param-set" as="element(c:param-set)*"/>
                <xsl:copy>
                  <xsl:apply-templates select="@*"/>
                  <xsl:sequence select="tr:expand-param-set(c:param, $param-sets-in-scope)"/>  
                </xsl:copy>
              </xsl:template>
              
              <xsl:function name="tr:expand-param-set">
                <xsl:param name="current-params" as="element(c:param)+"/>
                <xsl:param name="param-sets-in-scope" as="element(c:param-set)*"/>
                <xsl:choose>
                  <xsl:when test="count($param-sets-in-scope) gt 0">        
                    <xsl:variable name="new-params" select="$param-sets-in-scope[1]/c:param[not(@name = $current-params/@name)]"/>
                    <xsl:sequence select="tr:expand-param-set(($current-params, $new-params), $param-sets-in-scope[not(position() eq 1)])"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:sequence select="$current-params"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:function>
              
              <xsl:template match="@*|*">
                <xsl:copy>
                  <xsl:apply-templates select="@*, node()"/>
                </xsl:copy>
              </xsl:template>
              
            </xsl:stylesheet>
          </p:inline>
        </p:input>
        <p:input port="parameters">
          <p:empty/>
        </p:input>
      </p:xslt>
      
      <p:viewport match="c:param-set" name="viewport-on-params">
        
        <tr:resolve-params name="resolve-params"/>
        
      </p:viewport>
      
    </p:when>
    <p:otherwise>
      
      <p:identity/>
      
    </p:otherwise>
  </p:choose>
  
  <tr:store-debug pipeline-step="cascade/dirs-and-params">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
  <p:xslt name="dirs-to-clades-xslt">
    <p:input port="stylesheet">
      <p:document href="../xsl/dirs-to-clades.xsl"/>
    </p:input>
    <p:input port="parameters">
      <p:pipe port="params" step="get-clades-from-dirs"/>
    </p:input>
  </p:xslt>
  
  <tr:store-debug pipeline-step="cascade/clades-from-dirs">
    <p:with-option name="active" select="$debug"/>
    <p:with-option name="base-uri" select="$debug-dir-uri"/>
  </tr:store-debug>
  
</p:declare-step>
