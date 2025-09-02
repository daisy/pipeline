<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:m="http://www.w3.org/1998/Math/MathML"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-result-prefixes="#all">

  <xsl:import href="mathml-serialization.xsl"/>
  <xsl:import href="mathml-normalization.xsl"/>

  <xsl:param name="language" as="xs:string" required="true"/>

  <!-- @simple is added during the compilation of the rules -->
  <xsl:key name="simple-rules" match="d:rule[@constant]" use="@constant"/>

  <xsl:variable name="rules" select="document('mathml-rules.xml')"/>

  <!-- =========================================================== -->
  <!-- == COMPILE THE CONVERSION RULES FOR THE CURRENT LANGUAGE == -->
  <!-- =========================================================== -->
  <xsl:variable name="compiled-rules" as="document-node(element(d:rules))">
    <xsl:variable name="lang" select="substring-before(concat($language,'-'),'-')"/>
    <xsl:document>
      <d:rules>
        <xsl:for-each select="$rules//d:rule[d:trans[not(@xml:lang) or @xml:lang = $lang][1]]">
          <xsl:sort data-type="number" order="descending"
                    select="if (current()/@priority) then current()/@priority else 0"/>
          <xsl:copy>
            <xsl:variable name="pattern" select="node() except d:trans"/>
            <xsl:copy-of select="@* except regex except constant"/>
            <xsl:choose>
              <xsl:when test="count($pattern//d:group) + count($pattern//d:text) + count($pattern//d:optional-group) = 0">
                <xsl:attribute name="constant">
                  <xsl:value-of select="string-join(d:serialize($pattern, 'false'), '')"/>
                </xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="regex">
                  <xsl:value-of select="string-join(d:serialize($pattern, 'true'), '')"/>
                </xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:sequence select="d:trans[@xml:lang = $lang][1]"/>
          </xsl:copy>
        </xsl:for-each>
      </d:rules>
    </xsl:document>
  </xsl:variable>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- process MathML -->
  <xsl:template match="m:math">

    <!-- =========================================================== -->
    <!-- ==================== APPLY THE RULES ====================== -->
    <!-- =========================================================== -->
    <xsl:variable name="lang" select="substring-before(concat($language,'-'),'-')"/>
    <xsl:variable name="normalized">
      <xsl:apply-templates select="." mode="normalize"/>
    </xsl:variable>
    <ssml:speak version="1.1">
      <xsl:copy-of select="@*"/> <!-- id, xml:lang and CSS -->

      <!-- likely to come from left annotations: -->
      <xsl:for-each select="m:*[local-name()!='text'][1]/preceding-sibling::m:text">
	<xsl:value-of select="text()"/>
      </xsl:for-each>

      <!-- inner m:text will be lost -->
      <!-- text only MathML will be lost -->

      <xsl:call-template name="iterate-over-rules">
	<!-- the MathML is serialized the same way as the matching rules have been serialized to regex. -->
	<xsl:with-param name="serialized" select="string-join(d:serialize($normalized/node(), 'false'), '')"/>
	<xsl:with-param name="compiled-rules" select="$compiled-rules"/>
      </xsl:call-template>

      <!-- likely to come from right annotations: -->
      <xsl:for-each select="m:*[local-name()!='text'][1]/following-sibling::m:text">
	<xsl:value-of select="text()"/>
      </xsl:for-each>
    </ssml:speak>
  </xsl:template>

  <xsl:template name="iterate-over-rules">
    <xsl:param name="serialized"/>
    <xsl:param name="compiled-rules"/>
    <xsl:param name="emphasize" select="true()"/>

    <!-- the depth levels in the serialization are decremented so they
         can match those of the rules which start with level 1. -->
    <xsl:variable name="decrement">
      <xsl:analyze-string select="$serialized" regex="^:([0-9]+):">
	<xsl:matching-substring>
	  <xsl:value-of select="regex-group(1)"/> <!-- = the lowest depth level (most likely 1 or 2) -->
	</xsl:matching-substring>
	<xsl:non-matching-substring/>
      </xsl:analyze-string>
    </xsl:variable>
    <xsl:variable name="shifted">
      <xsl:analyze-string select="$serialized" regex="([:@])([0-9]+)">
	<xsl:matching-substring>
	  <xsl:value-of
	      select="concat(regex-group(1), 1 + number(regex-group(2)) - number($decrement))"/>
	</xsl:matching-substring>
	<xsl:non-matching-substring>
	  <xsl:copy/> <!-- the rest is left unchanged -->
	</xsl:non-matching-substring>
      </xsl:analyze-string>
    </xsl:variable>

    <!-- try matching the simple rules in priority (since they usually
         are specializations) -->
    <xsl:variable name="simple-rule"
		  select="key('simple-rules', $shifted, $compiled-rules)"/>
    <xsl:choose>
      <xsl:when test="$simple-rule">
	<xsl:copy-of select="$simple-rule/d:trans/node()"/>
      </xsl:when>
      <xsl:when test="not(contains($serialized, ':') and contains($serialized, '@'))">
	<!-- the MathML contains no nodes, only text -->
	<xsl:value-of select="concat(' ', $serialized, ' ')"/>
      </xsl:when>
      <xsl:otherwise>
	<!-- Now we can try to match the regexs with the updated,
	     serialized MathML: -->
	<xsl:variable name="first-rule" select="$compiled-rules//d:rule[@regex][1]"/>
	<xsl:if test="$first-rule">
	  <xsl:call-template name="one-iteration">
	    <xsl:with-param name="serialized" select="$shifted"/>
	    <xsl:with-param name="rule" select="$first-rule"/>
	    <xsl:with-param name="compiled-rules" select="$compiled-rules"/>
	    <xsl:with-param name="emphasize" select="$emphasize"/>
	  </xsl:call-template>
	</xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="one-iteration">
    <xsl:param name="serialized"/>
    <xsl:param name="rule"/>
    <xsl:param name="compiled-rules"/>
    <xsl:param name="emphasize"/>
    <xsl:variable name="next" select="$rule/following-sibling::d:rule[@regex][1]"/>
    <xsl:analyze-string select="$serialized" regex="{concat('^', $rule/@regex, '$')}">
      <xsl:matching-substring>
	<xsl:variable name="regex-groups">
	  <d:regex-groups>
	    <d:regex-group n="1"><xsl:value-of select="regex-group(1)"/></d:regex-group>
	    <d:regex-group n="2"><xsl:value-of select="regex-group(2)"/></d:regex-group>
	    <d:regex-group n="3"><xsl:value-of select="regex-group(3)"/></d:regex-group>
	    <d:regex-group n="4"><xsl:value-of select="regex-group(4)"/></d:regex-group>
	    <d:regex-group n="5"><xsl:value-of select="regex-group(5)"/></d:regex-group>
	    <!-- yes, it won't work with more than 5 groups. -->
	    <!-- It can easily be replaced with a for-each though. -->
	  </d:regex-groups>
	</xsl:variable>
	<xsl:apply-templates mode="replace" select="$rule/d:trans/node()">
	  <xsl:with-param name="regex-groups" select="$regex-groups"/>
	  <xsl:with-param name="compiled-rules" select="$compiled-rules"/>
	  <xsl:with-param name="emphasize" select="$emphasize"/>
	</xsl:apply-templates>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
	<xsl:variable name="expected-ending" select="'@1@[^ ]+ $'"/>
	<xsl:variable name="without-ending"
		      select="replace($serialized, $expected-ending, '')"/>
	<xsl:choose>
	  <xsl:when test="$next">
	    <!-- Try with the next rule -->
	    <xsl:call-template name="one-iteration">
	      <xsl:with-param name="serialized" select="$serialized"/>
	      <xsl:with-param name="rule" select="$next"/>
	      <xsl:with-param name="compiled-rules" select="$compiled-rules"/>
	      <xsl:with-param name="emphasize" select="$emphasize"/>
	    </xsl:call-template>
	  </xsl:when>
	  <!-- No rule has matched. To deal with this case, we
	       recurse on children if we are trying to match a
	       single node, or we recurse on the nodes of the list
	       if we are trying to match a list of more than one
		 node. -->
	  <xsl:when test="not(contains($without-ending, '@1@'))">
	    <!-- This case should not happen if the list of rules
		 is exhaustive, though exhaustiveness is not
		 mandatory. -->
	    <xsl:call-template name="iterate-over-rules">
	      <!-- Recursive call on the list of children (by
		   deconstructing the serialized parent): -->
	      <xsl:with-param name="serialized" select="replace($without-ending, '^:1:[^ ]+ ', '')"/>
	      <xsl:with-param name="compiled-rules" select="$compiled-rules"/>
	      <xsl:with-param name="emphasize" select="$emphasize"/>
	    </xsl:call-template>
	  </xsl:when>
	  <xsl:when test="not(matches($serialized, $expected-ending))">
	    <!-- may happend if the MathML is wrongly formatted,
	         e.g. if there are isolated texts nodes. -->
	    <xsl:value-of select="' ERROR '"/>
	  </xsl:when>
	  <xsl:otherwise>
	    <!-- This case WILL occur because there can't be rules
		 that deconstruct an undeterminate number of
		 nodes, such as the "A B C..." in <mrow> A B C D E
		 F G </mrow> -->
	    <xsl:analyze-string select="$serialized" regex=":1:"> <!-- split the list -->
	      <xsl:matching-substring/>
	      <xsl:non-matching-substring>
		<xsl:call-template name="iterate-over-rules">
		  <!-- Recursive call on the nodes of the list -->
		  <xsl:with-param name="serialized" select="concat(':1:', .)"/>
		  <xsl:with-param name="compiled-rules" select="$compiled-rules"/>
		  <xsl:with-param name="emphasize" select="$emphasize"/>
		</xsl:call-template>
	      </xsl:non-matching-substring>
	    </xsl:analyze-string>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>

  <xsl:template match="d:group" mode="replace" priority="2">
    <xsl:param name="regex-groups"/>
    <xsl:param name="compiled-rules"/>
    <xsl:param name="emphasize"/>
    <xsl:variable name="n" select="@n"/>

    <xsl:variable name="play-emphasis" select="$emphasize and @emph='true'"/>
    <xsl:if test="$play-emphasis">
      <ssml:break time="300ms"/>
    </xsl:if>
    <xsl:call-template name="iterate-over-rules">
      <!-- Transformation of the subtree that corresponds to the group -->
      <xsl:with-param name="serialized" select="$regex-groups//d:regex-group[@n=$n]"/>
      <xsl:with-param name="compiled-rules" select="$compiled-rules"/>
      <xsl:with-param name="emphasize" select="@emph-children='true'"/>
    </xsl:call-template>
    <xsl:if test="$play-emphasis">
      <ssml:break time="300ms"/>
    </xsl:if>
  </xsl:template>


  <xsl:template match="node()" mode="replace" priority="1">
    <xsl:param name="regex-groups"/>
    <xsl:param name="compiled-rules"/>
    <xsl:param name="emphasize"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="node()" mode="replace">
	<xsl:with-param name="regex-groups" select="$regex-groups"/>
	<xsl:with-param name="compiled-rules" select="$compiled-rules"/>
	<xsl:with-param name="depth" select="$emphasize"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
