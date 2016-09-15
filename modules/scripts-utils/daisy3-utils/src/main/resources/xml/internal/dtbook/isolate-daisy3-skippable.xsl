<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns="http://www.daisy.org/z3986/2005/dtbook/"
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

  <xsl:variable name="skippable-elements" select="('pagenum', 'noteref', 'annoref', 'linenum', 'math')"/>
  <xsl:variable name="no-skippable-marker" select="'no-skippable'"/>
  <xsl:variable name="sentence-ids" select="collection()[2]"/>

  <xsl:key name="in-scope" match="*[@skippable]" use="concat(@skippable, @n)"/>
  <xsl:key name="sentence" match="*[@id]" use="@id"/>
  <xsl:key name="skippables" match="*" use="generate-id()"/>

  <xsl:template match="node()">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[@id and key('sentence', @id, $sentence-ids)]">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:variable name="sentence">
	<xsl:copy-of select="."/>
      </xsl:variable>
      <xsl:variable name="scope">
	<d:scope>
	  <xsl:apply-templates select="$sentence/*" mode="find-scopes"/>
	</d:scope>
      </xsl:variable>

      <xsl:choose>
	<xsl:when test="local-name() = $skippable-elements">
	  <!-- The sentence is itself a skippable structure -->
	  <skippable id="{@id}"/> <!-- in DTBook namespace so we won't leave any unsolicited namespaces behind -->
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
	    <span id="{concat($sent-id, current-grouping-key())}">
	      <xsl:apply-templates select="$sentence/*/node()" mode="copy-sentence">
		<xsl:with-param name="scope" select="$scope"/>
		<xsl:with-param name="skippable" select="current-grouping-key()"/>
	      </xsl:apply-templates>
	    </span>
	  </xsl:for-each-group>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()" mode="find-scopes">
    <xsl:variable name="prev-skippable" select="preceding::*[local-name() = $skippable-elements][1]"/>
    <xsl:variable name="prev-skippable-id"
		  select="if ($prev-skippable) then generate-id($prev-skippable)
			  else $no-skippable-marker"/>
    <xsl:call-template name="add-scope-entry">
      <xsl:with-param name="prev-skippable-id" select="$prev-skippable-id"/>
    </xsl:call-template>
    <xsl:apply-templates select="node()" mode="find-scopes"/>
  </xsl:template>

  <xsl:template match="*[local-name() = $skippable-elements]" mode="find-scopes">
    <xsl:call-template name="add-scope-entry">
      <xsl:with-param name="prev-skippable-id" select="generate-id()"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="add-scope-entry">
    <xsl:param name="prev-skippable-id"/>
    <xsl:variable name="id" select="generate-id()"/>
    <d:a skippable="{$prev-skippable-id}" n="{$id}"/>
    <xsl:for-each select="ancestor::*">
      <d:a skippable="{$prev-skippable-id}" n="{generate-id(current())}"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="node()" mode="copy-skippable">
    <xsl:param name="skippable-ancestors"/>
    <xsl:param name="scope"/>
    <xsl:param name="skippable"/>
    <xsl:choose>
      <xsl:when test="generate-id() = $skippable">
	<xsl:variable name="skippable-id"
		      select="if (@id) then @id else concat($id-prefix, generate-id())"/>
	<xsl:copy>
	  <xsl:copy-of select="@*"/>
	  <xsl:if test="not(@id)">
	    <xsl:attribute name="id">
	      <xsl:value-of select="$skippable-id"/>
	    </xsl:attribute>
	  </xsl:if>
	  <skippable id="{$skippable-id}"/>
	  <xsl:copy-of select="node()"/>
	</xsl:copy>
      </xsl:when>
      <xsl:when test="count($skippable-ancestors intersect .) = 1">
	<xsl:variable name="id" select="generate-id()"/>
	<xsl:variable name="scope-entry" select="key('in-scope', concat($skippable, $id), $scope)[1]"/>
	<xsl:copy>
	  <xsl:copy-of select="@* except @id"/>
	  <xsl:if test="@id and not($scope-entry/preceding-sibling::*[@n = $id][1])">
	    <xsl:copy-of select="@id"/> <!-- there must not be duplicated @ids -->
	  </xsl:if>
	  <xsl:apply-templates select="*" mode="copy-skippable">
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
    <xsl:if test="not(local-name() = $skippable-elements)">
      <xsl:variable name="id" select="generate-id()"/>
      <xsl:variable name="scope-entry" select="key('in-scope', concat($skippable, $id), $scope)[1]"/>
      <xsl:if test="$scope-entry">
	<xsl:copy>
	  <xsl:copy-of select="@* except @id"/>
	  <xsl:if test="@id and not($scope-entry/preceding-sibling::*[@n = $id][1])">
	    <xsl:copy-of select="@id"/> <!-- there must not be duplicated @ids -->
	  </xsl:if>
	<xsl:apply-templates select="node()" mode="copy-sentence">
	  <xsl:with-param name="scope" select="$scope"/>
	  <xsl:with-param name="skippable" select="$skippable"/>
	</xsl:apply-templates>
	</xsl:copy>
      </xsl:if>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
