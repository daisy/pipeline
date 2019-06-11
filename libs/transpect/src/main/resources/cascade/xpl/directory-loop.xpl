<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:tr="http://transpect.io"
  version="1.0" 
  name="directory-loop"
  type="tr:directory-loop">
  
  <p:documentation>
    The step performs a recursive directory listing and
    looks for parameter documents named 'params.xml'. The parameter documents
    are included in the XML directory listing.  
  </p:documentation>
  
  <p:output port="result"/>
  
  <p:option name="depth" select="-1"/>
  <p:option name="path" required="true"/>
  <p:option name="exclude-filter"/>
  
  <p:import href="http://transpect.io/xproc-util/recursive-directory-list/xpl/recursive-directory-list.xpl"/>
  
  <p:choose>
    <p:when test="p:value-available('exclude-filter')">
      
      <tr:recursive-directory-list>
        <p:with-option name="path" select="$path"/>
        <!-- eclude filter expects a regular expression -->
        <p:with-option name="exclude-filter" select="$exclude-filter"/>
      </tr:recursive-directory-list>
      
    </p:when>
    <p:otherwise>
      
      <tr:recursive-directory-list>
        <p:with-option name="path" select="$path"/>
      </tr:recursive-directory-list>
      
    </p:otherwise>
  </p:choose>
  
  <p:try>
    <p:group>
      
      <p:identity name="current-dir"/>
      
      <p:load name="load-params">
        <p:with-option name="href" select="concat(c:directory/@xml:base, 'params.xml')"/>
      </p:load>
      
      <!-- remove relaxng declaration -->
      <p:delete match="/processing-instruction()" name="remove-rng-declaration"/>
      
      <p:insert position="first-child">
        <p:input port="source">
          <p:pipe port="result" step="current-dir"/>
        </p:input>
        <p:input port="insertion">
          <p:pipe port="result" step="remove-rng-declaration"/>
        </p:input>
      </p:insert>
      
    </p:group>
    <p:catch>
      <p:identity/>
    </p:catch>
  </p:try>
  
  <p:viewport match="/c:directory/c:directory">
    <p:variable name="path" select="/c:directory/@xml:base"/>
    
    <p:choose>
      <p:when test="$depth != 0">
        
        <tr:directory-loop>
          <p:with-option name="path" select="$path"/>
          <p:with-option name="exclude-filter" select="$exclude-filter"/>
          <p:with-option name="depth" select="$depth - 1"/>
        </tr:directory-loop>
        
      </p:when>
      <p:otherwise>
        <p:identity/>
      </p:otherwise>
    </p:choose>
  </p:viewport>
  
</p:declare-step>
