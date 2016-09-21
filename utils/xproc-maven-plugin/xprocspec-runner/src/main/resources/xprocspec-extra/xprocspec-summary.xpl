<?xml version='1.0' encoding='utf-8'?>
<p:declare-step version="1.0"
                type="pxi:test-report"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/"
                xmlns:x="http://www.daisy.org/ns/xprocspec"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xpath-version="2.0">
  
  <p:input port="parameters" kind="parameter"/>
  <p:output port="result" sequence="false"/>
  
  <p:xslt template-name="main">
    <p:input port="source">
      <p:empty/>
    </p:input>
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0">
          <xsl:param name="test-names" as="xs:string" required="yes"/>
          <xsl:param name="surefire-reports" as="xs:string" required="yes"/>
          <xsl:param name="reports" as="xs:string" required="yes"/>
          <xsl:template name="main">
            <xsl:variable name="tbody" as="element()">
              <tbody>
                <xsl:for-each select="tokenize($test-names, ' ')">
                  <xsl:variable name="i" as="xs:integer" select="position()"/>
                  <xsl:variable name="test-name" as="xs:string" select="."/>
                  <xsl:variable name="surefire-report-uri" as="xs:string" select="tokenize($surefire-reports, ' ')[$i]"/>
                  <xsl:variable name="report-uri" as="xs:string" select="tokenize($reports, ' ')[$i]"/>
                  <xsl:choose>
                    <xsl:when test="doc-available($surefire-report-uri)">
                      <xsl:variable name="surefire-report" select="doc($surefire-report-uri)"/>
                      <xsl:variable name="pending" select="number(($surefire-report/testsuites/@skipped, 0)[1])"/>
                      <xsl:variable name="error" select="number($surefire-report/testsuites/@errors)"/>
                      <xsl:variable name="failed" select="number($surefire-report/testsuites/@failures)"/>
                      <xsl:variable name="total" select="number($surefire-report/testsuites/@tests)"/>
                      <xsl:variable name="passed" select="$total - $failed - $error - $pending"/>
                      <tr class="{ if ($failed &gt; 0 or $error &gt; 0) then 'failed'
                                   else if ($pending &gt; 0) then 'pending'
                                   else 'successful' }">
                        <th>
                          <a href="{$report-uri}">
                            <xsl:value-of select="$test-name"/>
                          </a>
                        </th>
                        <th>
                          <span>
                            <xsl:value-of select="$passed"/>
                          </span>
                          <xsl:text>/</xsl:text>
                          <span>
                            <xsl:value-of select="$pending"/>
                          </span>
                          <xsl:text>/</xsl:text>
                          <span>
                            <xsl:value-of select="$failed"/>
                          </span>
                          <xsl:text>/</xsl:text>
                          <span>
                            <xsl:value-of select="$error"/>
                          </span>
                          <xsl:text>/</xsl:text>
                          <span>
                            <xsl:value-of select="$total"/>
                          </span>
                        </th>
                      </tr>
                    </xsl:when>
                    <xsl:otherwise>
                      <tr class="error">
                        <th>
                          <xsl:value-of select="$test-name"/>
                        </th>
                        <th>
                          <span>0</span>
                          <xsl:text>/</xsl:text>
                          <span>0</span>
                          <xsl:text>/</xsl:text>
                          <span>0</span>
                          <xsl:text>/</xsl:text>
                          <span>1</span>
                          <xsl:text>/</xsl:text>
                          <span>1</span>
                        </th>
                      </tr>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
              </tbody>
            </xsl:variable>
            <xsl:variable name="passed" select="sum($tbody/tr/th[2]/span[1]/number(.))"/>
            <xsl:variable name="pending" select="sum($tbody/tr/th[2]/span[2]/number(.))"/>
            <xsl:variable name="failed" select="sum($tbody/tr/th[2]/span[3]/number(.))"/>
            <xsl:variable name="error" select="sum($tbody/tr/th[2]/span[4]/number(.))"/>
            <xsl:variable name="total" select="sum($tbody/tr/th[2]/span[5]/number(.))"/>
            <html>
              <head>
                <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                <title>
                  Test Summary
                  <xsl:value-of select="concat('(', $passed, '/', $pending, '/', $failed, '/', $error, '/', $total, ')')"/>
                </title>
                <link rel="stylesheet" type="text/css" href="xspec.css"/>
              </head>
              <body>
                <h1>
                  Test Summary
                  <span style="position:absolute; right:15">
                    <xsl:value-of select="concat($passed, '/', $pending, '/', $failed, '/', $error, '/', $total)"/>
                  </span>
                </h1>
                <table class="xspec">
                  <colgroup>
                    <col width="85%"/>
                    <col width="15%"/>
                  </colgroup>
                  <thead>
                    <tr>
                      <th style="text-align: right; font-weight: normal;">
                        passed/pending/failed/error/total
                      </th>
                      <th>
                        <xsl:value-of select="concat($passed, '/', $pending, '/', $failed, '/', $error, '/', $total)"/>
                      </th>
                    </tr>
                  </thead>
                  <xsl:sequence select="$tbody"/>
                </table>
              </body>
            </html>
          </xsl:template>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
  </p:xslt>
  
</p:declare-step>
