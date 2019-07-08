<!DOCTYPE xsl:stylesheet [
<!ENTITY two-child-element "('mfrac','mroot', 'msub', 'msup', 'msubsup', 'munder',
                             'mover', 'munderover', 'mmultiscripts', 'msub')">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	 xmlns="http://www.w3.org/1998/Math/MathML" 
	 xpath-default-namespace="http://www.w3.org/1998/Math/MathML"
	 version="2.0">
  
  <xsl:import href="identity.xsl"/>
  
  <xsl:template match="msub[count(*) = 1] | msup[count(*) = 1] | msubsup[count(*) le 2]" mode="repair-subsup">
    <xsl:element name="{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
      <xsl:variable name="base" as="element(*)?" 
        select="(preceding-sibling::*[1] | parent::mrow[current() is *[1]]/preceding-sibling::*[1][not(parent::*/local-name() = &two-child-element;)])[1]"/>
      <xsl:choose>
        <xsl:when test="exists($base)">
          <xsl:apply-templates mode="#current" select="$base">
            <xsl:with-param name="keep" select="true()"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <mrow/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="#current"/>
    </xsl:element>
  </xsl:template>

  <!-- do we expect the base in front of ../mrow/mrow or even ../mrow/mrow/mrow? -->
  <xsl:template
    match="*[not(parent::*/local-name() = &two-child-element;)][following-sibling::*[1]/self::msub[count(*) = 1]] |
           *[not(parent::*/local-name() = &two-child-element;)][following-sibling::*[1]/self::mrow/*[1]/self::msub[count(*) = 1]] |
           *[not(parent::*/local-name() = &two-child-element;)][following-sibling::*[1]/self::msup[count(*) = 1]] |
           *[not(parent::*/local-name() = &two-child-element;)][following-sibling::*[1]/self::mrow/*[1]/self::msup[count(*) = 1]] |
           *[not(parent::*/local-name() = &two-child-element;)][following-sibling::*[1]/self::msubsup[count(*) le 2]] |
           *[not(parent::*/local-name() = &two-child-element;)][following-sibling::*[1]/self::mrow/*[1]/self::msubsup[count(*) le 2]]" mode="repair-subsup"  priority="1">
    <xsl:param name="keep" select="false()"/>
    <xsl:if test="$keep">
      <xsl:next-match>
        <xsl:with-param name="keep" select="$keep"/>
      </xsl:next-match>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*[preceding-sibling::*[1]/local-name() = 'mmultiscripts'][starts-with(following-sibling::*[1]/local-name(), 'msu')]" mode="repair-subsup" priority="2">
    <!-- base with preceding mmultiscripts and following msu(b|p|bsup) becomes one mmultiscripts -->
    <xsl:message select="'real mmulti!'"></xsl:message>
    <xsl:variable name="fs1" select="following-sibling::*[1]" as="element(*)"/>
    <mmultiscripts>
      <xsl:next-match>
        <xsl:with-param name="keep" select="true()"/>
      </xsl:next-match>
      <xsl:if test="local-name($fs1) = 'msup'">
        <mrow/>
      </xsl:if>
      <xsl:apply-templates select="$fs1/node()"/>
      <xsl:if test="local-name($fs1) = 'msub'">
        <mrow/>
      </xsl:if>
      <mprescripts/>
      <xsl:apply-templates select="preceding-sibling::*[1]/node()[preceding-sibling::mprescripts]"/>
    </mmultiscripts>
  </xsl:template>

  <xsl:template match="mmultiscripts[starts-with(following-sibling::*[2]/local-name(), 'msu')]" mode="repair-subsup" priority="2"/>
  <xsl:template match="*[starts-with(local-name(), 'msu')][preceding-sibling::*[2]/local-name() = 'mmultiscripts']" mode="repair-subsup" priority="2"/>

  <xsl:template match="mmultiscripts[not(count(mprescripts/preceding-sibling::*) mod 2 = 1)]" mode="repair-subsup">
    <mmultiscripts>
      <xsl:choose>
        <xsl:when test="following-sibling::*">
          <xsl:apply-templates mode="#current" select="following-sibling::*[1]">
            <xsl:with-param name="keep" select="true()"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <mrow/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates mode="#current"/>
    </mmultiscripts>
  </xsl:template>

  <xsl:template match="*[preceding-sibling::*[1]/self::mmultiscripts[not(count(mprescripts/preceding-sibling::*) mod 2 = 1)]]" mode="repair-subsup">
	 <xsl:param name="keep" select="false()"/>
	 <xsl:if test="$keep">
		<xsl:next-match/>
	 </xsl:if>
  </xsl:template>
</xsl:stylesheet>
