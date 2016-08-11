<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:brl="http://www.daisy.org/z3986/2009/braille/"
    exclude-result-prefixes="xs"
    xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns="http://www.daisy.org/z3986/2005/dtbook/">

  <xsl:output indent="yes"/>

  <xsl:param name="contraction-grade" select="'0'"/>

  <xsl:variable name="series">
    <xsl:choose>
      <xsl:when test="//meta[@name='prod:series']/@content='PPP'">rucksack</xsl:when>
      <xsl:when test="//meta[@name='prod:series']/@content='SJW'">sjw</xsl:when>
      <xsl:otherwise>standard</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="series-number"
		select="//meta[@name='prod:seriesNumberprod:series']/@content"/>

  <xsl:template match="frontmatter/docauthor">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
    <xsl:call-template name="add-information-based-from-metadata"/>
  </xsl:template>

  <xsl:template match="node()" mode="#all" priority="-5">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="add-information-based-from-metadata">
    <level1 id="cover-recto">

      <!-- Authors -->
      <p id="cover-author">
        <xsl:value-of select="//docauthor"/>
      </p>

      <!-- Title -->
      <p id="cover-title">
        <xsl:value-of select="//doctitle"/>
      </p>

      <!-- Series -->
      <xsl:if test="$series = 'sjw'">
	<p class="series-sjw">SJW-Heft NR. <xsl:value-of select="$series-number"/></p>
      </xsl:if>

      <!-- Volumes -->
      <!-- How many Volumes -->
      <p class="how-many-volumes">In <span id="number-of-volumes"/> <span id="number-of-volumes-label"/></p>

      <!-- Current Volume -->
      <p class="which-volume">
	<!-- FIXME: if there are more than 12 volumes we want just the -->
	<!-- number but downshifted as with ordinals -->
	<span id="current-volume"/> Band
      </p>
      
      <!-- Series -->
      <xsl:if test="$series = 'rucksack'">
	    <p class="series-ppp">Rucksackbuch Nr. <xsl:value-of select="$series-number"/></p>
      </xsl:if>

      <!-- Publisher -->
      <xsl:choose>
	<xsl:when test="$contraction-grade = '0'">
	  <p class="publisher"><abbr>SBS</abbr> Schweiz. Bibliothek<br/> Für Blinde, Seh- und<br/> Lesebehinderte</p>
	</xsl:when>
	<xsl:when test="$contraction-grade = '1'">
	  <p class="publisher"><abbr>SBS</abbr> Schweizerische Bibliothek<br/> Für Blinde, Seh- und<br/> Lesebehinderte</p>
	</xsl:when>
	<xsl:otherwise>
	  <p class="publisher"><abbr>SBS</abbr> Schweizerische Bibliothek<br/> Für Blinde, Seh- und Lesebehinderte</p>
	</xsl:otherwise>
      </xsl:choose>

    </level1>
    <level1 id="cover-verso">
      <xsl:choose>
	<xsl:when test="$series = 'sjw'">
	  <p id="sjw-blurb">Brailleausgabe mit freundlicher Genehmigung des
	  <abbr>SJW</abbr> Schweizerischen Jugend-Schriftenwerks, Zürich. Wir
	  danken dem <abbr>SJW</abbr>-Verlag für die Bereitstellung der
	  Daten.</p>
	</xsl:when>
	<xsl:otherwise>
	  <p id="copyright-blurb">Dieses Braillebuch ist die ausschliesslich für
	  die Nutzung durch Seh- und Lesebehinderte Menschen bestimmte
	  zugängliche Version eines urheberrechtlich geschützten Werks. Sie
	  können es im Rahmen des Urheberrechts persönlich nutzen, dürfen es
	  aber nicht weiter verbreiten oder öffentlich zugänglich machen</p>
	</xsl:otherwise>
      </xsl:choose>

      <!-- Series -->
      <xsl:if test="$series = 'rucksack'">
	    <p class="series-ppp">Rucksackbuch Nr. <xsl:value-of select="$series-number"/></p>
      </xsl:if>

      <!-- Publisher long -->
      <xsl:choose>
	<xsl:when test="$contraction-grade = '0'">
	  <p id="publisher-blurb">Verlag, Satz und Druck<br/>
	  <abbr>SBS</abbr> Schweiz. Bibliothek<br/> für Blinde, Seh- und<br/> Lesebehinderte, Zürich<br/>
	  <brl:computer>www.sbs.ch</brl:computer></p>
	</xsl:when>
	<xsl:when test="$contraction-grade = '1'">
	  <p id="publisher-blurb">Verlag, Satz und Druck<br/>
	  <abbr>SBS</abbr> Schweizerische Bibliothek<br/> für Blinde, Seh- und<br/> Lesebehinderte, Zürich<br/>
	  <brl:computer>www.sbs.ch</brl:computer></p>
	</xsl:when>
	<xsl:otherwise>
	  <p id="publisher-blurb"> <br/>Verlag, Satz und Druck<br/>
	  <abbr>SBS</abbr> Schweizerische Bibliothek<br/> für Blinde, Seh- und Lesebehinderte, Zürich<br/>
	  <brl:computer>www.sbs.ch</brl:computer></p>
	</xsl:otherwise>
      </xsl:choose>

      <xsl:variable name="date" select="//meta[@name = 'dc:Date']/@content"/>
      <p id="cover-year"><abbr>SBS</abbr> <xsl:value-of select="format-date($date, '[Y]')"/>
      </p>
   </level1>
  </xsl:template>

</xsl:stylesheet>
