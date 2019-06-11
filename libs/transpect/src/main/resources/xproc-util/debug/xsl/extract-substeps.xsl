<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:c="http://www.w3.org/ns/xproc-step" 
  xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:cx="http://xmlcalabash.com/ns/extensions">

  <xsl:param name="output-port"/>

  <xsl:template name="main">
    <xsl:message select="'&#xa;Looking for port', $output-port, 'in', base-uri()"/>
    <xsl:choose>
      <xsl:when test="not(/*/*:output[@port eq $output-port]/*:pipe/@step)">
        <xsl:message terminate="yes"
          select="'&#xa;&#xa;&#x9;&#xa;ERROR: xpl does not contain output port', $output-port, 'or construct unexpected.&#xa;&#xa;'"/>
        <c:result>
          <xsl:value-of select="'ERROR: xpl does not contain output port', $output-port, 'or construct unexpected.'"/>
        </c:result>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="/"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="  /*/*:option
                       | /*/*:input
                       | /*/*:output
                       | /*/*:serialization
                       | /*/*:import
                       | /*/*" priority="5">
    <xsl:variable name="resulting-step" select="xs:string(/*/*:output[@port eq $output-port]/*:pipe/@step)"/>
    <xsl:choose>
      <!-- remove pipe reference of each p:output, whose step executed after debug output port -->
      <xsl:when test="name() eq 'p:output' and 
                      @port ne $output-port and 
                      not(/*/*[@name eq current()/*:pipe/@step]
                              [following::*[@name eq $resulting-step]]
                         )">
        <xsl:copy>
          <xsl:apply-templates select="@*" />
          <p:inline>
            <c:result/>
          </p:inline>
        </xsl:copy>
      </xsl:when>
      <!-- step of the debug output -->
      <xsl:when test="@name eq $resulting-step">
        <xsl:copy>
          <xsl:apply-templates select="@*, node()" />
        </xsl:copy>
        <p:sink/>
      </xsl:when>
      <xsl:when test="following::*[@name eq $resulting-step]">
        <xsl:copy-of select="." />
      </xsl:when>
      <xsl:otherwise />
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="@* | node()" priority="-1">
    <xsl:copy>
      <xsl:apply-templates select="@*, node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
