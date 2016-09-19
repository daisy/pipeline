<meta:stylesheet xmlns:meta="http://www.w3.org/1999/XSL/Transform"
		 xmlns:xsl="anything-but-the-xsl-namespace"
		 xmlns:tts="http://www.daisy.org/ns/pipeline/tts"
		 xmlns:m="http://www.w3.org/1998/Math/MathML"
		 xmlns:d="http://www.daisy.org/ns/pipeline/data"
		 version="2.0">

  <meta:namespace-alias stylesheet-prefix="xsl" result-prefix="meta"/>

  <!-- Known limitations:

   - Aural CSS is not properly moved when annotations are moved to other regions. Perhaps
   we could move them inside a <span> holding the CSS properties

   - If a sentence is bound to multiple annotations, resulting order may not be what the
   user expects, though this is unlikely to happen. See flatten-structure.xsl for a more
   accurate way of ordering annotations.

  -->
  <meta:template match="/">
    <xsl:stylesheet xmlns:tts="http://www.daisy.org/ns/pipeline/tts" version="2.0">

      <!-- Map the before/after nodes to existing nodes inside sentences, or to the
           sentences themselves-->
      <xsl:key name="before" match="tts:before" use="@id"/>
      <xsl:key name="after" match="tts:after" use="@id"/>
      <xsl:key name="clips" match="*[@id]" use="@id"/>
      <xsl:key name="sentences" match="*[@id]" use="@id"/>

      <xsl:variable name="sentence-ids" select="collection()[2]"/>

      <xsl:variable name="mapping">
	<tts:map>
	  <xsl:for-each select="//*">
	    <xsl:variable name="lang" select="ancestor-or-self::*[@xml:lang][1]/@xml:lang"/>
	    <xsl:variable name="short-lang" select="tokenize($lang, '-')[1]"/>
	    <xsl:variable name="annotations">
	      <xsl:apply-templates select="current()"/>
	    </xsl:variable>

	    <xsl:if test="$annotations/*">
	      <xsl:variable name="translated-annot"
			    select="$annotations/descendant-or-self::*[@xml:lang=$lang or @xml:lang=$short-lang][1]"/>
	      <xsl:variable name="parent-sent" select="ancestor-or-self::*[@id and key('sentences', @id, $sentence-ids)][1]"/>
	      <xsl:variable name="translated-before" select="$translated-annot/*[local-name()='before']/node()"/>
	      <xsl:variable name="translated-after" select="$translated-annot/*[local-name()='after']/node()"/>
	      <xsl:choose>
		<xsl:when test="$parent-sent">
		  <tts:before>
		    <xsl:attribute name="id">
		      <xsl:value-of select="tts:mapping-id(current())"/>
		    </xsl:attribute>
		    <xsl:sequence select="$translated-before"/>
		  </tts:before>
		  <tts:after>
		    <xsl:attribute name="id">
		      <xsl:value-of select="tts:mapping-id(current())"/>
		    </xsl:attribute>
		    <xsl:sequence select="$translated-after"/>
		  </tts:after>
		</xsl:when>
		<xsl:otherwise>
		  <xsl:variable name="children-sents" select="descendant::*[@id and key('sentences', @id, $sentence-ids)]"/>
		  <xsl:choose>
		    <xsl:when test="$children-sents">
		      <tts:before>
			<xsl:attribute name="id">
			  <xsl:value-of select="$children-sents[1]/@id"/>
			</xsl:attribute>
			<xsl:sequence select="$translated-before"/>
		      </tts:before>
		      <tts:after>
			<xsl:attribute name="id">
			  <xsl:value-of select="$children-sents[last()]/@id"/>
			</xsl:attribute>
			<xsl:sequence select="$translated-after"/>
		      </tts:after>
		    </xsl:when>
		    <xsl:otherwise>
		      <xsl:variable name="prev-sent" select="preceding::*[@id and key('sentences', @id, $sentence-ids)][1]"/>
		      <xsl:variable name="next-sent" select="following::*[@id and key('sentences', @id, $sentence-ids)][1]"/>
		      <xsl:if test="$prev-sent">
			<!-- Here we encounter an important limitation: if there is no
			     sentence before, the current annotation will never be
			     inserted. It occurs at the beginning of the document and when
			     skippable elements follow each other without text in
			     between.  -->
			<tts:after>
			  <xsl:attribute name="id">
			    <xsl:value-of select="$prev-sent/@id"/>
			  </xsl:attribute>
			  <xsl:sequence select="$translated-before"/>
			</tts:after>
		      </xsl:if>
		      <xsl:if test="$next-sent">
			<tts:before>
			  <xsl:attribute name="id">
			    <xsl:value-of select="$next-sent/@id"/>
			  </xsl:attribute>
			  <xsl:sequence select="$translated-after"/>
			</tts:before>
		      </xsl:if>
		    </xsl:otherwise>
		  </xsl:choose>
		</xsl:otherwise>
	      </xsl:choose>
	    </xsl:if>
	  </xsl:for-each>
	</tts:map>
      </xsl:variable>

      <!-- Insert the text thanks to the mapping -->
      <xsl:template match="/">
	<xsl:copy>
	  <xsl:apply-templates select="node()" mode="insert-text"/>
	</xsl:copy>
      </xsl:template>

      <xsl:template match="m:*" mode="insert-text" priority="3">
	<xsl:variable name="id" select="tts:mapping-id(.)"/>
	<xsl:variable name="before" select="key('before', $id, $mapping)"/>
	<xsl:variable name="after" select="key('after', $id, $mapping)"/>
	<xsl:copy>
	  <xsl:copy-of select="@*"/>
	  <xsl:if test="$before">
	    <m:text><xsl:copy-of select="string-join($before//text(), ' ')"/></m:text>
	  </xsl:if>
	  <xsl:apply-templates select="node()" mode="insert-text"/>
	  <xsl:if test="$after">
	    <m:text><xsl:copy-of select="string-join($after//text(), ' ')"/></m:text>
	  </xsl:if>
	</xsl:copy>
      </xsl:template>

      <xsl:template match="*" mode="insert-text" priority="2">
	<xsl:variable name="id" select="tts:mapping-id(.)"/>
	<xsl:variable name="before" select="key('before', $id, $mapping)"/>
	<xsl:variable name="after" select="key('after', $id, $mapping)"/>
	<xsl:copy>
	  <xsl:copy-of select="@*"/>
	  <xsl:if test="$before">
	    <xsl:value-of select="' '"/>
	    <xsl:copy-of select="$before/node()"/>
	    <xsl:value-of select="' '"/>
	  </xsl:if>
	  <xsl:apply-templates select="node()" mode="insert-text"/>
	  <xsl:if test="$after">
	    <xsl:value-of select="' '"/>
	    <xsl:copy-of select="$after/node()"/>
	    <xsl:value-of select="' '"/>
	  </xsl:if>
	</xsl:copy>
      </xsl:template>

      <xsl:template match="node()" mode="insert-text" priority="1">
	<xsl:copy>
	  <xsl:copy-of select="@*"/>
	  <xsl:apply-templates select="node()" mode="insert-text"/>
	</xsl:copy>
      </xsl:template>

      <xsl:function name="tts:mapping-id">
	<xsl:param name="node"/>
	<xsl:value-of select="if ($node/@id) then $node/@id else concat('mapid-', generate-id($node))"/>
      </xsl:function>

      <!-- Copy the user's rules -->
      <meta:copy-of select="collection()[/meta:stylesheet]/meta:stylesheet/meta:variable"/>
      <meta:copy-of select="collection()[/meta:stylesheet]/meta:stylesheet/meta:template"/>
      <meta:copy-of select="collection()[/meta:stylesheet]/meta:stylesheet/meta:function"/>
      <xsl:template match="node()">
	<!-- no annotation -->
      </xsl:template>

    </xsl:stylesheet>
  </meta:template>

</meta:stylesheet>
