<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:l10n="http://transpect.io/l10n"
  xmlns:tr="http://transpect.io"  
  xmlns="http://www.w3.org/1999/xhtml"
  version="2.0">

  <xsl:import href="svrl2xsl.xsl"/>
  
  <xsl:decimal-format decimal-separator="," grouping-separator="&#x2009;"/>
  
  <xsl:template name="l10n:fallback-for-removed-content">
    <span>Der Inhalt, auf den sich die Meldung bezieht, steht im aktuellen HTML-Rendering nicht zur Verfügung.
    	Es ist auch möglich, dass der Inhalt zwar vorhanden ist, aber zu wenig Informationen über seinen Ursprung mitführt. 
    	Es kann sich hierbei um ein Defizit des Konvertierungsprozesses handeln. 
    	Das tut uns leid. Hier ist der sogenannte <em>srcpath</em> für
      diagnostische Zwecke: </span>
  </xsl:template>

  <xsl:template name="l10n:adjusted-srcpath" xmlns="http://www.w3.org/1999/xhtml">
    <span title="srcpth {@adjusted-from} wurde entfernt">Anm.: Diese Meldung stammt von einer Stelle im Dokument, deren Ursprungsinformation im Lauf der Konvertierung entfernt wurde.
          Nun kann es sein, dass sie sich am umgebenden Absatz oder sogar an einem anderen Absatz in der Nähe befindet.</span>
  </xsl:template>
  <xsl:template name="l10n:severity-heading">
    <h3>Schweregrad</h3>
  </xsl:template>

  <xsl:template name="l10n:step-name">
    <span class="BC_step-name">
      <br/> 
      Konvertierungsschritt: <xsl:value-of select="@tr:step-name"/>
    </span>
  </xsl:template>

  <xsl:template name="l10n:message-empty" xmlns="http://www.w3.org/1999/xhtml">
    <xsl:param name="family" select="'Allgemeine Regeln'"/>
    <xsl:value-of select="$family"/>
  </xsl:template>

  <xsl:template name="l10n:timestamp" xmlns="http://www.w3.org/1999/xhtml">
    <xsl:param name="time" as="xs:dateTime"/>
    <xsl:value-of select="format-dateTime($time, '[D].[M].[Y0001], [H1]:[m01]')"/>
  </xsl:template>

  <xsl:function name="l10n:severity-role-label" as="xs:string">
    <xsl:param name="role" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="$role eq 'error'">
        <xsl:value-of select="'Fehler'"/>
      </xsl:when>
      <xsl:when test="$role eq 'warning'">
        <xsl:value-of select="'Warnungen'"/>
      </xsl:when>
      <xsl:when test="$role = ('Info', 'info')">
        <xsl:value-of select="'Informationen'"/>
      </xsl:when>
      <xsl:when test="$role eq 'fatal-error'">
        <xsl:value-of select="'Fatale Fehler'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$role"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
