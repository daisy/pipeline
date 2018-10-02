<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
    xmlns:math="http://www.w3.org/1998/Math/MathML"
    xmlns="http://www.w3.org/2001/SMIL20/" exclude-result-prefixes="#all" version="2.0">

  <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

  <xsl:param name="mo-dir"/>
  <xsl:param name="audio-dir"/>
  <xsl:param name="content-dir"/>
  <xsl:param name="content-uri"/>
  <xsl:param name="uid"/>
  <xsl:param name="audio-only"/>

  <xsl:function name="d:smil">
    <xsl:param name="smilref"/>
    <xsl:value-of select="substring-before($smilref, '#')"/>
  </xsl:function>

  <xsl:variable name="audio-dir-rel" select="pf:relativize-uri($audio-dir, $mo-dir)"/>
  <xsl:variable name="content-doc-rel" select="pf:relativize-uri($content-uri, $mo-dir)"/>
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

  <xsl:key name="clips" match="*[@idref]" use="@idref"/>
  <xsl:key name="targets" match="*[@id and contains($ref-targets, concat(' ', local-name(), ' '))]" use="@id"/>

  <xsl:template match="/">
    <!-- TODO: rewrite the algorithm to iterate over the document only once. -->
    <xsl:variable name="root" select="/"/>
    <!-- This could be optimized by not recursing over the nodes with a @smilref. -->
    <xsl:for-each-group select="//*[@smilref]" group-by="d:smil(@smilref)">
      <xsl:result-document href="{resolve-uri(current-grouping-key(), $content-dir)}">
	<smil>
	  <head>
	     <meta content="{$uid}" name="dtb:uid"/>
	     <meta content="00:00:00" name="dtb:totalElapsedTime"/>
	     <meta content="DAISY Pipeline 2" name="dtb:generator"/>
	     <xsl:copy-of select="$custom-attrs"/>
	  </head>
	  <body>
	    <xsl:apply-templates select="$root/*" mode="find-ref">
	      <xsl:with-param name="smilfile" select="current-grouping-key()"/>
	    </xsl:apply-templates>
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
    <xsl:if test="d:smil(@smilref) = $smilfile">
      <xsl:apply-templates select="." mode="write-smil"/>
    </xsl:if>
    <!-- No 'otherwise' needed as the children will have the same
         @smilref. Annotations and notes are the only
         exceptions. There is a special case for them .-->
  </xsl:template>

  <!-- === SMIL WRITING TEMPLATES === -->

  <xsl:template match="*[@smilref]" mode="write-smil" priority="2">
    <xsl:variable name="smil-nr" select="d:smil(@smilref)"/>
    <xsl:variable name="id-in-smil" select="substring-after(@smilref, '#')"/>
    <xsl:choose>
      <xsl:when test="descendant::*[@smilref and d:smil(@smilref) = $smil-nr][1]">
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
    <xsl:variable name="clip" select="key('clips', @id, collection()[/d:audio-clips])"/>
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
      <audio src="{concat($audio-dir-rel, tokenize($clip/@src, '[/\\]')[last()])}">
	<xsl:copy-of select="$clip/(@clipBegin|@clipEnd)"/>
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
