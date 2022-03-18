<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                xmlns="http://www.w3.org/2001/SMIL20/"
                exclude-result-prefixes="#all">

  <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

  <xsl:param name="mo-dir"/>
  <xsl:param name="uid"/>
  <xsl:param name="audio-only"/>

  <xsl:variable name="content-doc-uri" select="base-uri(/*)"/>
  <xsl:variable name="content-doc-rel" select="pf:relativize-uri($content-doc-uri, $mo-dir)"/>
  <xsl:variable name="ref-targets" select="' note annotation '"/>

  <xsl:variable name="custom-attrs">
    <customAttributes>
      <customTest defaultState="false" id="pagenum" override="visible"/>
      <customTest defaultState="false" id="note" override="visible"/>
      <customTest defaultState="false" id="noteref" override="visible"/>
      <customTest defaultState="false" id="annotation" override="visible"/>
      <customTest defaultState="false" id="linenum" override="visible"/>
      <customTest defaultState="false" id="sidebar" override="visible"/>
      <customTest defaultState="false" id="prodnote" override="visible"/>
    </customAttributes>
  </xsl:variable>

  <!-- d:audio-clips document with @src attributes relativized against $mo-dir -->
  <xsl:variable name="audio-map">
    <xsl:variable name="audio-map" select="collection()[/d:audio-clips]"/>
    <xsl:variable name="audio-map-uri" select="base-uri($audio-map/*)"/>
    <xsl:for-each select="$audio-map/*">
      <xsl:copy>
        <xsl:for-each-group select="*" group-by="@src">
          <xsl:variable name="src" select="current-grouping-key()"/>
          <xsl:variable name="src" select="resolve-uri($src,$audio-map-uri)"/>
          <xsl:variable name="src" select="pf:relativize-uri($src,$mo-dir)"/>
          <xsl:for-each select="current-group()">
            <xsl:copy>
              <xsl:sequence select="@* except @src"/>
              <xsl:attribute name="src" select="$src"/>
            </xsl:copy>
          </xsl:for-each>
        </xsl:for-each-group>
      </xsl:copy>
    </xsl:for-each>
  </xsl:variable>

  <xsl:key name="clips" match="*[@idref]" use="@idref"/>
  <xsl:key name="targets" match="*[@id and contains($ref-targets, concat(' ', local-name(), ' '))]" use="@id"/>

  <xsl:template match="/">
    <!-- TODO: rewrite the algorithm to iterate over the document only once. -->
    <xsl:variable name="root" select="/"/>
    <!-- This could be optimized by not recursing over the nodes with a @smilref. -->
    <xsl:for-each-group select="//*[@smilref]" group-by="substring-before(@smilref, '#')">
      <xsl:result-document href="{resolve-uri(current-grouping-key(), $content-doc-uri)}">
	<smil>
	  <head>
	     <meta content="{$uid}" name="dtb:uid"/>
	     <!-- this value will be corrected in px:daisy3-smil-add-elapsed-time-->
	     <meta content="00:00:00" name="dtb:totalElapsedTime"/>
	     <meta content="DAISY Pipeline 2" name="dtb:generator"/>
	     <xsl:copy-of select="$custom-attrs"/>
	  </head>
	  <body>
	    <seq id="root-seq">
	      <xsl:apply-templates select="$root/*" mode="find-ref">
	        <xsl:with-param name="smilfile" select="current-grouping-key()"/>
	      </xsl:apply-templates>
	    </seq>
	  </body>
	</smil>
      </xsl:result-document>
    </xsl:for-each-group>
  </xsl:template>

  <!-- === FIND THE SUBTREES THAT CORRESPOND TO THE SMIL FILE WE ARE LOOKING FOR === -->

  <xsl:template match="*" mode="find-ref" priority="1">
    <xsl:param name="smilfile"/>
    <xsl:apply-templates mode="find-ref" select="*">
      <xsl:with-param name="smilfile" select="$smilfile"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*[@smilref]" mode="find-ref" priority="2">
    <xsl:param name="smilfile"/>
    <xsl:if test="substring-before(@smilref, '#') = $smilfile">
      <xsl:apply-templates select="." mode="write-smil"/>
    </xsl:if>
    <!-- No 'otherwise' needed as the children will have the same
         @smilref. Annotations and notes are the only
         exceptions. There is a special case for them .-->
  </xsl:template>

  <!-- === SMIL WRITING TEMPLATES === -->

  <xsl:template match="*[@smilref]" mode="write-smil" priority="2">
    <xsl:variable name="smil-nr" select="substring-before(@smilref, '#')"/>
    <xsl:variable name="id-in-smil" select="substring-after(@smilref, '#')"/>
    <xsl:choose>
      <xsl:when test="descendant::*[@smilref and substring-before(@smilref, '#') = $smil-nr][1]">
	<seq id="{$id-in-smil}" class="{local-name()}">
	  <xsl:if test="self::math:*">
	    <xsl:call-template name="escapable"/>
	  </xsl:if>
	  <xsl:apply-templates select="." mode="write-custom"/>
	  <xsl:apply-templates mode="write-smil" select="*"/>
	</seq>
      </xsl:when>
      <xsl:otherwise>
	<par id="{$id-in-smil}" class="{local-name()}">
	  <xsl:apply-templates select="." mode="write-custom"/>
	  <xsl:apply-templates select="." mode="add-link"/>
	</par>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="math:math[@smilref]" mode="write-smil" priority="3">
    <xsl:variable name="id-in-smil" select="substring-after(@smilref, '#')"/>
    <seq id="{$id-in-smil}" class="{local-name()}">
      <xsl:call-template name="escapable"/>
      <xsl:apply-templates select="." mode="write-custom"/>
      <par id="{concat($id-in-smil, '-par')}" class="{local-name()}">
	<xsl:apply-templates select="." mode="write-custom"/>
	<xsl:apply-templates select="." mode="add-link"/>
      </par>
    </seq>
  </xsl:template>

  <xsl:template match="*" mode="write-smil" priority="1">
    <xsl:apply-templates mode="write-smil" select="*"/>
  </xsl:template>

  <xsl:template name="escapable">
    <xsl:attribute name="end">
      <xsl:value-of select="'DTBuserEscape;'"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template name="add-audio">
    <xsl:variable name="clip" select="key('clips', @id, $audio-map)"/>
    <xsl:if test="$audio-only='false'">
      <text src="{concat($content-doc-rel, '#', @id)}">
        <xsl:if test="self::math:*">
          <xsl:attribute name="type">
            <xsl:text>http://www.w3.org/1998/Math/MathML</xsl:text>
          </xsl:attribute>
        </xsl:if>
      </text>
    </xsl:if>
    <xsl:if test="$clip">
      <audio>
        <xsl:copy-of select="$clip/(@src|@clipBegin|@clipEnd)"/>
      </audio>
    </xsl:if>
  </xsl:template>

  <xsl:variable name="custom-list" select="concat(' ', string-join($custom-attrs/*/*/@id, ' '), ' ')"/>
  <xsl:template match="*[contains($custom-list, concat(' ', local-name(), ' '))]" mode="write-custom">
    <xsl:attribute name="customTest" >
      <xsl:value-of select="local-name()"/>
    </xsl:attribute>
  </xsl:template>

  <xsl:template match="*" mode="write-custom"/>

  <xsl:template match="dtbook:noteref|dtbook:annoref" mode="add-link">
    <xsl:choose>
      <xsl:when test="$audio-only='true'">
	<xsl:call-template name="add-audio"/>
      </xsl:when>
      <xsl:otherwise>
	<a external="false" href="{tokenize(key('targets', substring-after(@idref, '#'))/@smilref, '[/\\]')[last()]}">
	  <xsl:call-template name="add-audio"/>
	</a>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*" mode="add-link">
    <xsl:call-template name="add-audio"/>
  </xsl:template>

</xsl:stylesheet>
