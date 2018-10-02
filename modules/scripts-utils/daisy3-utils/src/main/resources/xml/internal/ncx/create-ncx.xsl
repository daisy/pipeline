<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
		xmlns="http://www.daisy.org/z3986/2005/ncx/"
		exclude-result-prefixes="#all" version="2.0">

  <!-- inputs: -->
  <!-- main: dtbook content document -->
  <!-- collection(): audio map -->

  <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
  <xsl:output indent="yes"/>

  <xsl:param name="mo-dir"/>
  <xsl:param name="audio-dir"/>
  <xsl:param name="ncx-dir"/>
  <xsl:param name="uid"/>

  <xsl:variable name="titles" select="' levelhd hd h1 h2 h3 h4 h5 h6 '"/>
  <xsl:variable name="navPoints" select="' level level1 level2 level3 level4 level5 level6 '"/>
  <xsl:variable name="pageTargets" select="' pagenum '"/>
  <xsl:variable name="audio-dir-rel" select="pf:relativize-uri($audio-dir, $ncx-dir)"/>
  <xsl:variable name="mo-dir-rel" select="pf:relativize-uri($mo-dir, $ncx-dir)"/>

  <xsl:variable name="navTargets">
    <targets>
      <target type="note"/>
    </targets>
  </xsl:variable>

  <xsl:variable name="all-but-headings"
		select="concat(' ', string-join($navTargets//@type, ' '), ' ', $pageTargets)"/>

  <xsl:variable name="pages" select="//dtbook:pagenum"/>

  <xsl:key name="clips" use="@idref" match="*[@idref]"/>
  <xsl:key name="headings" use="generate-id()"
	   match="*[contains($navPoints, concat(' ', local-name(), ' ')) and
		  *[contains($titles, concat(' ', local-name(), ' '))]]"/>


  <xsl:variable name="play-orders">
    <d:playOrders>
      <xsl:for-each select="//*[key('headings', generate-id()) or
			    contains($all-but-headings, concat(' ', local-name(), ' '))]">
	<d:o v="{position()}" id="{generate-id()}"/>
      </xsl:for-each>
    </d:playOrders>
  </xsl:variable>

  <xsl:key name="orders" use="@id" match="*[@id]"/>

  <xsl:function name="d:getText">
    <xsl:param name="container"/>
    <xsl:value-of select="normalize-space(string-join($container/descendant-or-self::text(), ''))"/>
  </xsl:function>

  <xsl:template match="/">
    <!-- TODO: set xml:lang -->
    <ncx version="2005-1">
      <head>
	<meta content="DAISY Pipeline 2" name="dtb:generator"/>
	<meta name="dtb:uid" content="{$uid}"/>
	<meta name="dtb:depth"
	      content="{concat('0', max(//*[key('headings', generate-id())]/
		       count(ancestor-or-self::*[key('headings', generate-id())])))}"/>

	<xsl:variable name="total-pages" select="count($pages)"/>
	<meta name="dtb:totalPageCount" content="{$total-pages}"/>
	<meta name="dtb:maxPageNumber" content="{$total-pages}"/>
	<smilCustomTest bookStruct="PAGE_NUMBER" defaultState="false" id="pagenum" override="visible"/>
	<smilCustomTest bookStruct="NOTE" defaultState="false" id="note" override="visible"/>
	<smilCustomTest bookStruct="NOTE_REFERENCE" defaultState="false" id="noteref" override="visible"/>
	<smilCustomTest bookStruct="ANNOTATION" defaultState="false" id="annotation" override="visible"/>
	<smilCustomTest bookStruct="LINE_NUMBER" defaultState="false" id="linenum" override="visible"/>
	<smilCustomTest bookStruct="OPTIONAL_SIDEBAR" defaultState="false" id="sidebar" override="visible"/>
	<smilCustomTest bookStruct="OPTIONAL_PRODUCER_NOTE" defaultState="false" id="prodnote" override="visible"/>
      </head>

      <docTitle>
	<xsl:apply-templates mode="add-text" select="(//dtbook:doctitle)[1]"/>
      </docTitle>

      <xsl:apply-templates select="//dtbook:docauthor" mode="author"/>

      <navMap>
	<xsl:choose>
	  <xsl:when test="not(($play-orders//*[@id])[1]) or not((//*[key('headings', generate-id())])[1])">
	    <!-- Fake navPoint: should not happen if the dtbook has been previously fixed -->
	    <navPoint playOrder="{count($play-orders//*[@id]) + 1}" id="fake-navPoint">
	      <navLabel><text>Error: no headings</text></navLabel>
	      <xsl:apply-templates mode="add-content-link"
				   select="(//*[@smilref and not(key('orders', generate-id(), $play-orders))])[1]"/>
	    </navPoint>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:apply-templates select="*" mode="navMap">
	      <xsl:with-param name="play-orders" select="$play-orders"/>
	    </xsl:apply-templates>
	  </xsl:otherwise>
	</xsl:choose>
      </navMap>

      <xsl:call-template name="pageList">
	<xsl:with-param name="play-orders" select="$play-orders"/>
      </xsl:call-template>

      <xsl:call-template name="navList">
	<xsl:with-param name="play-orders" select="$play-orders"/>
      </xsl:call-template>
    </ncx>
  </xsl:template>

  <xsl:template match="*" mode="author">
    <docAuthor>
      <xsl:apply-templates mode="add-text" select="."/>
    </docAuthor>
  </xsl:template>

  <!-- ======== navMap ======== -->
  <xsl:template match="*" mode="navMap">
    <xsl:param name="play-orders"/>
    <xsl:apply-templates select="*" mode="navMap">
      <xsl:with-param name="play-orders" select="$play-orders"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*[key('headings', generate-id())]" mode="navMap">
    <xsl:param name="play-orders"/>
    <navPoint>
      <xsl:apply-templates select="."  mode="add-content">
	<xsl:with-param name="play-orders" select="$play-orders"/>
	<xsl:with-param name="text-container" select="*[contains($titles, concat(' ', local-name(), ' '))][1]"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="*" mode="navMap">
	<xsl:with-param name="play-orders" select="$play-orders"/>
      </xsl:apply-templates>
    </navPoint>
  </xsl:template>

  <!-- ======== pageList ======== -->
  <xsl:template name="pageList">
    <xsl:param name="play-orders"/>
    <xsl:if test="$pages">
      <pageList>
	<xsl:for-each select="$pages">
	  <xsl:variable name="val" select="d:getText(.)"/>
	  <pageTarget type="{if (@page) then @page else 'normal'}" value="{position()}">
	    <xsl:apply-templates select="."  mode="add-content">
	      <xsl:with-param name="play-orders" select="$play-orders"/>
	      <xsl:with-param name="text-container" select="."/>
	    </xsl:apply-templates>
	  </pageTarget>
	</xsl:for-each>
      </pageList>
    </xsl:if>
  </xsl:template>

  <!-- ======== navList ======== -->
  <xsl:template name="navList">
    <xsl:param name="play-orders"/>
    <xsl:param name="doc-context" select="/"/>  <!-- the context will be overriden by the for-each. -->
    <xsl:for-each select="$navTargets//*[@type]">
      <xsl:variable name="type" select="@type"/>
      <xsl:variable name="targets" select="$doc-context//*[local-name() = $type]"/>
      <xsl:if test="$targets">
	<navList class="{$type}">
	  <navLabel><text><xsl:value-of select="@type"/></text></navLabel>
	  <xsl:for-each select="$targets">
	    <navTarget>
	      <xsl:apply-templates select="."  mode="add-content">
		<xsl:with-param name="play-orders" select="$play-orders"/>
		<xsl:with-param name="text-container" select="."/>
	      </xsl:apply-templates>
	    </navTarget>
	  </xsl:for-each>
	</navList>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- ======== Utils ======== -->
  <xsl:template match="*" mode="add-content">
    <xsl:param name="play-orders"/>
    <xsl:param name="text-container"/>
    <xsl:variable name="play-order" select="key('orders', generate-id(), $play-orders)/@v"/>
    <xsl:attribute name="playOrder">
      <xsl:value-of select="$play-order"/>
    </xsl:attribute>
    <xsl:attribute name="id">
      <xsl:value-of select="concat('ncx-', $play-order)"/>
    </xsl:attribute>
    <navLabel>
      <xsl:apply-templates select="$text-container" mode="add-text"/>
    </navLabel>
    <xsl:apply-templates select="$text-container" mode="add-content-link"/>
  </xsl:template>

  <xsl:template match="*" mode="add-content-link">
    <xsl:if test="@smilref">
      <content src="{concat($mo-dir-rel, tokenize(@smilref, '[/\\]')[last()])}"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*" mode="add-text">
    <text>
      <xsl:value-of select="d:getText(.)"/>
    </text> <!-- TODO: check if text != ''? -->
    <xsl:variable name="all-clips" select="descendant-or-self::*[@id and key('clips', @id, collection()[/d:audio-clips])]"/>
    <xsl:if test="count($all-clips) > 0">
      <!-- The audio information are added only if the text has been synthesized into one single sound file, -->
      <!-- because the specifications do not allow multiple <audio> nodes. -->
      <xsl:variable name="first-clip" select="key('clips', $all-clips[1]/@id, collection()[/d:audio-clips])"/>
      <xsl:variable name="last-clip" select="key('clips', $all-clips[last()]/@id, collection()[/d:audio-clips])"/>
      <xsl:if test="$first-clip/@src = $last-clip/@src">
	<audio src="{concat($audio-dir-rel, tokenize($first-clip/@src, '[/\\]')[last()])}"
	       clipBegin="{$first-clip/@clipBegin}" clipEnd="{$last-clip/@clipEnd}"/>
      </xsl:if>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
