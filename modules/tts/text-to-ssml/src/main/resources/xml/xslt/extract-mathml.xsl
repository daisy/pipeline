<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		xmlns:m="http://www.w3.org/1998/Math/MathML"
		version="2.0">

  <xsl:variable name="doc-with-math-elts" select="collection()[2]"/>
  <xsl:key name="math-ids" match="m:math[@id]" use="@id"/>

  <xsl:template match="/">
    <ssml:speak version="1.1">
      <xsl:for-each select="/*/ssml:s">
	<xsl:if test="not(m:get-math-element(current()))">
	  <xsl:copy-of select="current()"/>
	</xsl:if>
      </xsl:for-each>
    </ssml:speak>
    <!-- Secondary output port: -->
    <xsl:result-document href="math-only.xml">
      <ssml:speak version="1.1">
	<xsl:for-each select="/*/ssml:s">
	  <xsl:variable name="math-elt" select="m:get-math-element(current())"/>
	  <xsl:if test="$math-elt">
	    <!-- @id is taken from the elt if it was a skippable, and from the sentence otherwise. -->
	    <xsl:variable name="math-id" select="if ($math-elt/@id) then $math-elt/@id else current()/@id"/>
	    <m:math id="{$math-id}">
	      <xsl:copy-of select="$math-elt/ancestor-or-self::*/@*[local-name() != 'id']"/> <!-- copy CSS if any -->

	      <!-- move left text to inside the math element, otherwise it will be lost -->
	      <!-- <m:text> -->
	      <!-- 	<xsl:apply-templates mode="copy-text-until" -->
	      <!-- 	  select="(current()/text()|current()/*[1][local-name()!='math']/following::text()[1])[1]"> -->
	      <!-- 	  <xsl:with-param name="till" select="($math-elt//text())[1]"/> -->
	      <!-- 	</xsl:apply-templates> -->
	      <!-- </m:text> -->
	      <!-- ^^^^^^^^^^^^^^^^^^^^ -->
	      <!-- So far, this is not needed because px:annotate already adds the m:text -->

	      <!-- math content -->
	      <xsl:copy-of select="$math-elt/node()"/>

	      <!-- move right text to inside the math element -->
	      <!-- <m:text> -->
	      <!-- 	<xsl:apply-templates select="$math-elt/following::text()[1]" mode="copy-text-until"> -->
	      <!-- 	  <xsl:with-param name="till" select="current()/following::text()[1]"/> -->
	      <!-- 	</xsl:apply-templates> -->
	      <!-- </m:text> -->

	    </m:math>
	  </xsl:if>
	</xsl:for-each>
      </ssml:speak>
    </xsl:result-document>
  </xsl:template>

  <!-- We are assuming that the annotations are not big enough to be a problem for the stack -->
  <xsl:template match="text()" mode="copy-text-until">
    <xsl:param name="till" as="node()"/>
    <xsl:if test="not(. is $till)">
      <xsl:copy/>
      <xsl:apply-templates select="following::text()[1]" mode="copy-text-until">
	<xsl:with-param name="till" select="$till"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

  <xsl:function name="m:get-math-element" as="node()*">
    <xsl:param name="n"/>
    <xsl:variable name="skippable" select="$n/descendant::m:math"/>
    <xsl:choose>
      <xsl:when test="$skippable">
	<xsl:sequence select="$skippable"/>
      </xsl:when>
      <xsl:when test="key('math-ids', $n/@id, $doc-with-math-elts)">
	<xsl:sequence select="$n"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:sequence select="()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
