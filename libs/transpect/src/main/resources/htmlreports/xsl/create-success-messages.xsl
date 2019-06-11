<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
  xmlns:sch="http://purl.oclc.org/dsdl/schematron"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xpath-default-namespace="http://transpect.io"
  exclude-result-prefixes="xs tr sch svrl"
  version="2.0">

  <xsl:param name="report-summary-components" as="xs:string" select="'prose'"/>

  <xsl:variable name="severity-vocabulary" as="xs:string+"
    select="('info', 'warning', 'error', 'fatal-error')"/>

  <xsl:variable name="summary-component-vocabulary" as="xs:string+"
    select="('prose', 'severity-totals', 'error-code-totals', 'rule-family-totals', $severity-vocabulary)"/>


  <xsl:variable name="strings" as="element(string)+" xmlns="http://transpect.io">
    <string xml:id="f1">
      <loc xml:lang="de">Abbruchfehler</loc>
      <loc xml:lang="en">Fatal errors</loc>
    </string>
    <string xml:id="e1">
      <loc xml:lang="de">Fehler/-arten</loc>
      <loc xml:lang="en">Errors/distinct</loc>
    </string>
    <string xml:id="w1">
      <loc xml:lang="de">Warnungen/Warnungsarten</loc>
      <loc xml:lang="en">Warnings/distinct</loc>
    </string>
    <string xml:id="s1">
      <loc xml:lang="de">Meldungen wurden in den HTML-Prüfbericht eingefügt. </loc>
      <loc xml:lang="en">Messages have been inserted into the HTML report. </loc>
    </string>
  </xsl:variable>
  
  <xsl:template match="/*">
    <xsl:variable name="summary-component-list-tokens" as="xs:string*"
      select="tokenize($report-summary-components, '\s+')"/>
    <xsl:variable name="selected-summary-components" as="xs:string*"
      select="if ($summary-component-list-tokens = '#all')
              then $summary-component-vocabulary
              else $summary-component-list-tokens[. = $summary-component-vocabulary]"/>
  
    <xsl:variable name="infos" as="element(message)*" select="/document/messages/message[@severity = 'info']"/>
    <xsl:variable name="warnings" as="element(message)*" select="/document/messages/message[@severity = 'warning']"/>
    <xsl:variable name="errors" as="element(message)*" select="/document/messages/message[@severity = 'error']"/>
    <xsl:variable name="fatal-errors" as="element(message)*" select="/document/messages/message[@severity = 'fatal-error']"/>
    <xsl:variable name="info-count" as="xs:integer" select="count($infos)"/>
    <xsl:variable name="warning-count" as="xs:integer" select="count($warnings)"/>
    <xsl:variable name="error-count" as="xs:integer" select="count($errors)"/>
    <xsl:variable name="fatal-error-count" as="xs:integer" select="count($fatal-errors)"/>
    <xsl:variable name="distinct-info-count" as="xs:integer" select="count(/document/messages[message[@severity = 'info']])"/>
    <xsl:variable name="distinct-error-count" as="xs:integer" select="count(/document/messages[message[@severity = 'error']])"/>
    <xsl:variable name="distinct-warning-count" as="xs:integer" select="count(/document/messages[message[@severity = 'warning']])"/>
    <xsl:variable name="distinct-fatal-error-count" as="xs:integer" select="count(/document/messages[message[@severity = 'fatal-error']])"/>
    <xsl:variable name="root" select="/" as="document-node(element(document))"/>
    <c:messages timestamp="{format-dateTime(current-dateTime(), '[Y]-[M02]-[D02] [H02]:[m02]:[s02][Z]')}">
      <xsl:if test="$selected-summary-components = 'prose'">
        <xsl:for-each select="('en', 'de')">
          <c:message xml:lang="{.}">
            <xsl:variable name="_lang" select="." as="xs:string"/>
            <xsl:value-of select="$strings[@xml:id = 's1']/loc[@xml:lang = $_lang]"/>
            <xsl:if test="$fatal-error-count gt 0">
              <xsl:value-of select="$strings[@xml:id = 'f1']/loc[@xml:lang = $_lang]"/>
              <xsl:text>: </xsl:text>
              <xsl:value-of select="$fatal-error-count"/>
              <xsl:text>. </xsl:text>
            </xsl:if>
            <xsl:if test="$distinct-error-count gt 0">
              <xsl:value-of select="$strings[@xml:id = 'e1']/loc[@xml:lang = $_lang]"/>
              <xsl:text>: </xsl:text>
              <xsl:value-of select="$error-count"/>
              <xsl:text>/</xsl:text>
              <xsl:value-of select="$distinct-error-count"/>
              <xsl:text>. </xsl:text>
            </xsl:if>
            <xsl:if test="$distinct-warning-count gt 0">
              <xsl:value-of select="$strings[@xml:id = 'w1']/loc[@xml:lang = $_lang]"/>
              <xsl:text>: </xsl:text>
              <xsl:value-of select="$warning-count"/>
              <xsl:text>/</xsl:text>
              <xsl:value-of select="$distinct-warning-count"/>
              <xsl:text>.</xsl:text>
            </xsl:if>
          </c:message>
        </xsl:for-each>  
      </xsl:if>
      <xsl:if test="$selected-summary-components = 'severity-totals'">
        <severity-totals>
          <xsl:attribute name="highest" select="$severity-vocabulary[for $current in . 
                                                                     return exists($root/document/messages/message[@severity = $current])]
                                                                    [last()]"/>
          <severity name="info">
            <xsl:attribute name="count" select="$info-count"/>
            <xsl:attribute name="distinct" select="$distinct-info-count"/>
          </severity>
          <severity name="warning">
            <xsl:attribute name="count" select="$warning-count"/>
            <xsl:attribute name="distinct" select="$distinct-warning-count"/>
          </severity>
          <severity name="error">
            <xsl:attribute name="count" select="$error-count"/>
            <xsl:attribute name="distinct" select="$distinct-error-count"/>
          </severity>
          <severity name="fatal-error">
            <xsl:attribute name="count" select="$fatal-error-count"/>
            <xsl:attribute name="distinct" select="$distinct-fatal-error-count"/>
          </severity>
        </severity-totals>
      </xsl:if>
      <xsl:if test="$selected-summary-components = 'error-code-totals'">
        <error-code-totals>
          <xsl:for-each-group select="/document/messages/message" group-by="(@code, 'unspecified')[1]">
            <message code="{current-grouping-key()}" count="{count(current-group())}"/>
          </xsl:for-each-group>
        </error-code-totals>
      </xsl:if>
      <xsl:if test="$selected-summary-components = 'rule-family-totals'">
        <rule-family-totals>
          <xsl:for-each-group select="/document/messages/message" group-by="(@rule-family, 'unspecified')[1]">
            <message code="{current-grouping-key()}" count="{count(current-group())}"/>
          </xsl:for-each-group>
        </rule-family-totals>
      </xsl:if>
      <xsl:for-each select="$severity-vocabulary[. = $selected-summary-components]">
        <xsl:element name="{.}">
          <xsl:apply-templates select="$root/document/messages/message[@severity = current()]"/>
        </xsl:element>
      </xsl:for-each>
    </c:messages>
  </xsl:template>
  
  <xsl:template match="message">
    <message>
      <xsl:apply-templates select="@code, @severity, @rule-family, @category, *:text"/>
    </message>
  </xsl:template>
  
  <xsl:template match="@*">
    <xsl:copy/>
  </xsl:template>
  
  <xsl:template match="sch:span[@class = ('srcpath', 'category')]"/>
  
</xsl:stylesheet>
