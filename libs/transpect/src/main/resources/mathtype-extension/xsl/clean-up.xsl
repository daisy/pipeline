<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
  <!ENTITY functions "('ln','max','min','cos','sin')">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	 xmlns="http://www.w3.org/1998/Math/MathML"
	 xpath-default-namespace="http://www.w3.org/1998/Math/MathML"
	 version="2.0">
  
  <xsl:import href="identity.xsl"/>

  <xsl:template match="/math" mode="clean-up" as="element(math)">
    <xsl:variable name="single-mrow">
      <xsl:apply-templates select="." mode="single-mrow"/>
    </xsl:variable>
    <math>
      <xsl:apply-templates select="@*" mode="#current"/>
      <xsl:apply-templates select="$single-mrow/math/node()" mode="#current"/>
    </math>
  </xsl:template>

  <xsl:template match="mrow[count(node()) = 1]/mrow" mode="single-mrow">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>

  <xsl:template match="mrow[count(*) = 1]" mode="clean-up">
    <xsl:apply-templates mode="#current"/>
  </xsl:template>
  
  <xsl:template match="*[not(self::mrow)][mrow][count(*) = 1]/mrow" mode="clean-up" priority="1.1">
    <!-- dissolve mstyle/mrow if mrow is the only element -->
    <xsl:apply-templates mode="#current"/>
  </xsl:template>

  <xsl:template match="@mathsize[. = '100%']" mode="clean-up"/>
  
  <xsl:template match="@columnalign[. = 'center']" mode="clean-up"/>

  <xsl:template match="*[local-name() = ('mtext', 'mo', 'mn')]/@mathvariant[. = 'normal']" mode="clean-up"/>

  <xsl:template match="mi[string-length(.) = 1]/@mathvariant[. = 'italic']" mode="clean-up"/>

  <xsl:template match="mi[string-length(.) gt 1]/@mathvariant[. = 'normal']" mode="clean-up"/>

  <xsl:template match="munderover[count(child::*[position() gt 1]/node()) = 0]" mode="clean-up" priority="2">
    <xsl:apply-templates select="node()[1]" mode="clean-up"/>
  </xsl:template>
  
  <xsl:template match="munderover[count(child::*[2]/node()) = 0]" mode="clean-up">
    <mover>
      <xsl:apply-templates select="@*" mode="clean-up"/>
      <xsl:apply-templates select="node()[1]" mode="clean-up"/>
      <xsl:apply-templates select="node()[3]" mode="clean-up"/>
    </mover>
  </xsl:template>

  <xsl:template match="munderover[count(child::*[3]/node()) = 0]" mode="clean-up">
    <munder>
      <xsl:apply-templates select="@*" mode="clean-up"/>
      <xsl:apply-templates select="node()[1]" mode="clean-up"/>
      <xsl:apply-templates select="node()[2]" mode="clean-up"/>
    </munder>
  </xsl:template>
  
  <xsl:template match="msubsup[count(child::*[position() gt 1]/node()) = 0]" mode="clean-up" priority="2">
    <xsl:apply-templates select="node()[1]" mode="clean-up"/>
  </xsl:template>
  
  <xsl:template match="msubsup[count(child::*[2]/node()) = 0]" mode="clean-up">
    <msup>
      <xsl:apply-templates select="@*" mode="clean-up"/>
      <xsl:apply-templates select="node()[1]" mode="clean-up"/>
      <xsl:apply-templates select="node()[3]" mode="clean-up"/>
    </msup>
  </xsl:template>

  <xsl:template match="msubsup[count(child::*[3]/node()) = 0]" mode="clean-up">
    <msub>
      <xsl:apply-templates select="@*" mode="clean-up"/>
      <xsl:apply-templates select="node()[1]" mode="clean-up"/>
      <xsl:apply-templates select="node()[2]" mode="clean-up"/>
    </msub>
  </xsl:template>
  
  <!-- move malignmarks before the element which should be the reference for alignment -->
  
  <xsl:template match="*[following-sibling::*[1][self::malignmark]]" mode="clean-up">
    <xsl:variable name="malignmark" select="following-sibling::*[1][self::malignmark]" as="element(malignmark)"/>
    <xsl:copy-of select="$malignmark"/>
    <xsl:copy>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="malignmark[preceding-sibling::*[1]]" mode="clean-up"/>

</xsl:stylesheet>
