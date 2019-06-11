<?xml version="1.0" encoding="utf-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" 
  xmlns:c="http://www.w3.org/ns/xproc-step"  
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:tr="http://transpect.io"
  version="1.0"
  name="text2params"
  type="tr:text2params"
  >
  
  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Converts text-based configuration files (yaml, in particular) to &lt;c:param-set&gt;s.</p>
    <p>It does not yet cope with hashes or arrays as found in yaml files. This will be adjourned
    to a time when params will be expressed as maps.</p>
  </p:documentation>

  <p:option name="file" required="true"/>
  
  <p:option name="debug" required="false" select="'no'"/>
  <p:option name="debug-dir-uri" select="'debug'"/>
  <p:output port="result" primary="true" />
  <p:serialization port="result" indent="true" omit-xml-declaration="false"/>

  <p:import href="http://xmlcalabash.com/extension/steps/library-1.0.xpl"/>
  <p:import href="http://transpect.io/xproc-util/store-debug/xpl/store-debug.xpl" />
  <p:import href="http://transpect.io/xproc-util/file-uri/xpl/file-uri.xpl" />
  
  <tr:file-uri name="file-uri">
    <p:with-option name="filename" select="$file"/>
  </tr:file-uri>
  
  <p:xslt name="xslt">
    <p:input port="parameters">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0">
          <xsl:template match="/*">
            <c:param-set>
              <xsl:choose>
                <xsl:when test="unparsed-text-available(@local-href, 'UTF-8')">
                  <xsl:sequence select="tr:parse-textfile(@local-href, 'UTF-8')"/>
                </xsl:when>
                <xsl:when test="unparsed-text-available(@local-href, 'ISO-8859-1')">
                  <xsl:sequence select="tr:parse-textfile(@local-href, 'ISO-8859-1')"/>
                </xsl:when>
                <xsl:otherwise>
                  <c:param name="could-not-load-text-file" value="{@local-href}"/>
                </xsl:otherwise>
              </xsl:choose>
            </c:param-set>
          </xsl:template>
          <xsl:function name="tr:parse-textfile" as="element(c:param)*">
            <xsl:param name="href" as="xs:string"/>
            <xsl:param name="charset" as="xs:string"/>
            <xsl:variable name="lines" as="xs:string*" 
              select="tokenize(unparsed-text($href, $charset), '(&#xd;&#xa;|&#xa;|&#xd;)')"/>
            <xsl:variable name="equals" as="xs:integer*" 
              select="for $l in $lines return count(string-to-codepoints($l)[. = 61])"/>
            <xsl:variable name="colons" as="xs:integer*" 
              select="for $l in $lines return count(string-to-codepoints($l)[. = 58])"/>
            <xsl:variable name="equals-lines" as="xs:integer" select="count($equals[. = 1])"/>
            <xsl:variable name="colon-lines" as="xs:integer" select="count($colons[. = 1])"/>
            <xsl:choose>
              <xsl:when test="$colon-lines gt $equals-lines">
                <xsl:sequence select="tr:parse-lines($lines, ':')"/>
              </xsl:when>
              <xsl:when test="$equals-lines gt 0">
                <xsl:sequence select="tr:parse-lines($lines, '=')"/>
              </xsl:when>
            </xsl:choose>
          </xsl:function>
          <xsl:function name="tr:parse-lines" as="element(c:param)*">
            <xsl:param name="lines" as="xs:string*"/>
            <xsl:param name="sep" as="xs:string"/>
            <xsl:for-each select="$lines">
              <xsl:analyze-string select="." regex="^\s*(.+)\s*{$sep}\s*(.*)\s*$">
                <xsl:matching-substring>
                  <c:param name="{replace(replace(regex-group(1), '\C', '_'), '^(\I)', '_$1')}" 
                           value="{replace(regex-group(2), '&quot;', '_')}"/>
                </xsl:matching-substring>
              </xsl:analyze-string>
            </xsl:for-each>
          </xsl:function>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt>
  
  <tr:store-debug>
    <p:with-option name="pipeline-step" select="concat('text2params/', /*/@lastpath)">
      <p:pipe port="result" step="file-uri"/>
    </p:with-option>
    <p:with-option name="active" select="$debug" />
    <p:with-option name="base-uri" select="$debug-dir-uri" />
  </tr:store-debug>
  
</p:declare-step>
