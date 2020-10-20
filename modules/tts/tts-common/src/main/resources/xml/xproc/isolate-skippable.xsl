<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		exclude-result-prefixes="#all"
		version="2.0">

  <!-- Note1: Unfortunately, we can't iterate over all the elements by recursing on
       following::*[1] and stopping when we find a skippable structure, because we risk
       recursing too many times when sentences are long. Instead, we use preceding::* to
       look for one skippable element at a time, though it is quite CPU-intensive. -->

  <!-- Note2: This script expects a list of sentence ids. If you can't provide one, it
       should work fine with a list of nodes containing text as direct child. -->

  <xsl:param name="id-prefix" />

  <xsl:variable name="no-skippable-marker" select="'no-skippable'"/>
  <xsl:variable name="sentence-ids" select="collection()[2]"/>
  <xsl:variable name="namespace" select="namespace-uri(/*)"/>

  <xsl:key name="in-scope" match="*[@skippable]" use="concat(@skippable, @n)"/>
  <xsl:key name="sentence" match="*[@id]" use="@id"/>
  <xsl:key name="skippables" match="*" use="@id"/>

  <xsl:template match="@*|node()" mode="#default copy">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="@skippable" mode="#default copy"/>

  <xsl:template match="*[@id and key('sentence', @id, $sentence-ids)]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:variable name="sentence">
	<xsl:copy-of select="."/>
      </xsl:variable>
      <xsl:variable name="scope">
	<d:scope>
	  <xsl:apply-templates select="$sentence/*" mode="find-scopes"/>
	</d:scope>
      </xsl:variable>

      <xsl:choose>
	<xsl:when test="@skippable">
	  <!-- The sentence is itself a skippable structure -->
	  <xsl:copy-of select="node()"/>
	</xsl:when>
	<xsl:when test="not($scope/*/*[@skippable != $no-skippable-marker])">
	  <!-- There are no skippable elements in this sentence -->
	  <xsl:copy-of select="node()"/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:variable name="sent-id" select="concat($id-prefix, @id, '-')"/>
	  <xsl:for-each-group select="$scope/*/*" group-adjacent="@skippable">
	    <xsl:if test="position() &gt; 1">
	      <xsl:apply-templates select="$sentence/*/*" mode="copy-skippable">
		<xsl:with-param name="scope" select="$scope"/>
		<xsl:with-param name="skippable" select="current-grouping-key()"/>
		<xsl:with-param name="skippable-ancestors"
				select="key('skippables', current-grouping-key(), $sentence)/ancestor::*"/>
	      </xsl:apply-templates>
	    </xsl:if>
	    <!-- TODO: not create an empty span when we're dealing with adjacent skippable elts -->
	    <xsl:element name="span" namespace="{$namespace}">
	      <xsl:attribute name="id" select="concat($sent-id, current-grouping-key())"/>
	      <xsl:apply-templates select="$sentence/*/node()" mode="copy-sentence">
		<xsl:with-param name="scope" select="$scope"/>
		<xsl:with-param name="skippable" select="current-grouping-key()"/>
	      </xsl:apply-templates>
	    </xsl:element>
	  </xsl:for-each-group>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()" mode="find-scopes">
    <xsl:call-template name="add-scope-entry">
      <xsl:with-param name="prev-skippable" select="preceding::*[@skippable][1]"/>
    </xsl:call-template>
    <xsl:apply-templates mode="#current"/>
  </xsl:template>

  <xsl:template match="*[@skippable]" mode="find-scopes">
    <xsl:call-template name="add-scope-entry">
      <xsl:with-param name="prev-skippable" select="."/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="add-scope-entry">
    <xsl:param name="prev-skippable" as="element()?"/>
    <xsl:variable name="prev-skippable-id"
		  select="if ($prev-skippable) then $prev-skippable/@id
			  else $no-skippable-marker"/> <!-- we know @id is present because of px:add-id step -->
    <d:a skippable="{$prev-skippable-id}" n="{generate-id()}"/>
    <xsl:for-each select="ancestor::*">
      <d:a skippable="{$prev-skippable-id}" n="{generate-id(current())}"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="node()" mode="copy-skippable">
    <xsl:param name="skippable-ancestors"/>
    <xsl:param name="scope"/>
    <xsl:param name="skippable"/>
    <xsl:choose>
      <!-- if this is a skippable we know @id is present because of px:add-id step -->
      <xsl:when test="@id[.=$skippable]">
	<xsl:copy>
	  <xsl:apply-templates select="@*|node()" mode="copy"/>
	</xsl:copy>
      </xsl:when>
      <xsl:when test="count($skippable-ancestors intersect .) = 1">
	<xsl:variable name="id" select="generate-id()"/>
	<xsl:variable name="scope-entry" select="key('in-scope', concat($skippable, $id), $scope)[1]"/>
	<xsl:copy>
	  <xsl:apply-templates select="@* except @id"/>
	  <xsl:if test="@id and not($scope-entry/preceding-sibling::*[@n = $id][1])">
	    <xsl:copy-of select="@id"/> <!-- there must not be duplicated @ids -->
	  </xsl:if>
	  <xsl:apply-templates select="*" mode="#current">
	    <xsl:with-param name="skippable-ancestors" select="$skippable-ancestors"/>
	    <xsl:with-param name="scope" select="$scope"/>
	    <xsl:with-param name="skippable" select="$skippable"/>
	  </xsl:apply-templates>
	</xsl:copy>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="node()" mode="copy-sentence">
    <xsl:param name="scope"/>
    <xsl:param name="skippable"/>
    <xsl:if test="not(@skippable)">
      <xsl:variable name="id" select="generate-id()"/>
      <xsl:variable name="scope-entry" select="key('in-scope', concat($skippable, $id), $scope)[1]"/>
      <xsl:if test="$scope-entry">
	<xsl:copy>
	  <xsl:apply-templates select="@* except @id"/>
	  <xsl:if test="@id and not($scope-entry/preceding-sibling::*[@n = $id][1])">
	    <xsl:copy-of select="@id"/> <!-- there must not be duplicated @ids -->
	  </xsl:if>
	<xsl:apply-templates mode="#current">
	  <xsl:with-param name="scope" select="$scope"/>
	  <xsl:with-param name="skippable" select="$skippable"/>
	</xsl:apply-templates>
	</xsl:copy>
      </xsl:if>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
