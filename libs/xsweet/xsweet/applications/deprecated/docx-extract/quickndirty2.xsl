<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
  xmlns:xsw="http://coko.foundation/xsweet"
  exclude-result-prefixes="#all">
  
<!-- XSweet: one of the earliest docx extraction XSLTs, kept here for historical reasons. It's standalone! -->
  
  <!-- Indent should really be no, but for testing. -->
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
  
  <xsl:param as="xs:string" name="show-css">yes</xsl:param>

  <!-- Turn $show-css to 'yes' to switch on $css-reflect. -->
  <!-- $show-css supplements the traversal with @style markers wherever certain
       kinds of formatting (e.g. font shift indicators including the spurious font shifts
       left in by word processors) are indicated in the text; it can be very noisy. -->
  <xsl:variable as="xs:boolean" name="css-reflect" select="$show-css='yes'"/>
  
  <!-- Run on 'document.xml' inside a .docx -->
  
  <xsl:template match="/w:document">
    <html>
      <head>
        <meta charset="UTF-8"/> 
      </head>
      <xsl:apply-templates select="w:body"/>
    </html>
  </xsl:template>
  
  <xsl:template match="w:body">
    <body>
      <xsl:apply-templates select="w:p"/>
    </body>
  </xsl:template>

  <!-- //w:p/w:pPr/w:pStyle -->
  <xsl:template match="w:p">
    <p>
      <xsl:for-each select="w:pPr/w:pStyle">
        <xsl:attribute name="class" select="@w:val"/>
      </xsl:for-each>
      
      <xsl:apply-templates select="w:r"/>
    </p>
  </xsl:template>
  
  <xsl:function name="xsw:css-literal" as="xs:string?">
    <xsl:param name="run" as="element(w:r)"/>
    <xsl:if test="$css-reflect">
      <xsl:apply-templates select="$run/w:rPr" mode="render-css"/>
    </xsl:if>
  </xsl:function>
  
  <xsl:template match="w:r[matches(xsw:css-literal(.), '\S')]">
    <span style="{normalize-space(xsw:css-literal(.))}">
      <xsl:call-template name="format-components"/>
    </span>
  </xsl:template>
  
  <xsl:template match="w:r">
    <xsl:call-template name="format-components"/>
  </xsl:template>
  
  <xsl:template name="format-components">
    <xsl:for-each-group select="* except w:rPr" group-adjacent="xsw:has-format(.)">
      <!--  current-grouping-key() is always true for some elements, and true for all when
              there is no w:rPr. The effect of the group-adjacent is to "bundle" elements
              to be wrapped in formatting, or not, depending on the element type. For example,
              footnote callouts that are expanded to footnotes are not wrapped, lest formatting
              for the callout be wrapped around the footnote in the result. -->
      <xsl:choose>
        <xsl:when test="current-grouping-key()">
          <!-- when the stuff is to be formatted, traverse to w:rPr carrying the group through. -->
          <xsl:apply-templates select="../w:rPr">
            <xsl:with-param name="contents" tunnel="yes">
              <xsl:apply-templates select="current-group()"/>
            </xsl:with-param>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="current-group()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>
  
  <xsl:variable name="endnotes-doc"  select="document('endnotes.xml',/)"/>
  <xsl:variable name="footnotes-doc" select="document('footnotes.xml',/)"/>
  
  <xsl:key name="endnotes-by-id"  match="w:endnote"  use="@w:id"/>
  <xsl:key name="footnotes-by-id" match="w:footnote" use="@w:id"/>
  
  <xsl:template match="w:endnoteReference" priority="3">
    <div class="endnote_fetched">
      <xsl:apply-templates select="key('endnotes-by-id',@w:id,$endnotes-doc)"/>
    </div>
  </xsl:template>

  <!-- Again overriding the default behavior for w:r/*, to the same effect. -->
  <xsl:template match="w:footnoteReference" priority="3">
    <div class="footnote_fetched">
      <xsl:apply-templates select="key('footnotes-by-id',@w:id,$footnotes-doc)"/>
    </div>
  </xsl:template>
  
  <!-- w:rPr works by pushing its contents through its children one at a time
       in sibling succession, given them each an opportunity to wrap the results. -->
  <!-- Individual templates matching w:rPr/* provide for the particular mappings into HTML. -->
  <xsl:template match="w:rPr">
    <xsl:param name="contents" select="()" tunnel="yes"/>
    <xsl:apply-templates select="*[1]">
      <!-- Tunneling <xsl:with-param name="contents" select="$contents"/>-->
    </xsl:apply-templates>
    <xsl:if test="empty(*)">
      <xsl:sequence select="$contents"/>
    </xsl:if>
  </xsl:template>

  
  <!-- Look ma! no modes! children of w:rPr perform a *sibling traversal*
       in order to wrap themselves sequentially in HTML (inline) wrappers. -->
  <!-- xsl:template/@priority must be used to assure a better match than the default. -->
  
  <!-- By default we name an element after its tag in Word ML (w: namespace). -->
  <xsl:template match="w:rPr/*">
    <xsl:element name="{local-name()}">
      <xsl:call-template name="tuck-next"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template priority="5" match="w:rPr/w:bCs">
    <!-- https://msdn.microsoft.com/en-us/library/documentformat.openxml.wordprocessing.boldcomplexscript(v=office.14).aspx -->
    <b class="bCs">
      <xsl:call-template name="tuck-next"/>
    </b>
  </xsl:template>

  <!-- When there's an inline style, announce it. -->
  <xsl:template priority="5" match="w:rPr/w:rStyle">
    <span class="{@w:val}">
      <xsl:call-template name="tuck-next"/>
    </span>
  </xsl:template>
  
  <!-- This should match any formatting we don't wish to see among wrapped inline elements;
       note that the same formatting properties may be detected in/by CSS reflection instead. -->
  <xsl:template priority="5" match="w:rPr/w:sz | w:rPr/w:szCs | w:rPr/w:rFonts | w:wPr/w:color">
    <!-- Just do the next one. -->
    <xsl:call-template name="tuck-next"/>
  </xsl:template>

  <!-- Called to effect the sibling traversal among w:rPrr/* elements. -->
  <xsl:template name="tuck-next">
    <xsl:param name="contents" select="()" tunnel="yes"/>
    <!-- If there's more format, keep going. -->
    <xsl:apply-templates select="following-sibling::*[1]"/>
    <!-- If not go back to get the text. -->
    <xsl:if test="empty(following-sibling::*)">
      <xsl:sequence select="$contents"/>
    </xsl:if>
  </xsl:template>

  <xsl:function name="xsw:has-format" as="xs:boolean">
    <xsl:param name="n" as="node()"/>
    <xsl:variable name="n-is-callout" as="xs:boolean">
      <xsl:apply-templates select="$n" mode="is-callout"/>
    </xsl:variable>
    <xsl:sequence select="exists($n/../w:rPr) and not($n-is-callout)"/>
  </xsl:function>
  
  <!-- Since we don't want to see these wrapped in formatting ...  -->
  <xsl:template match="w:footnoteReference | w:endnoteReference" mode="is-callout" as="xs:boolean">
    <xsl:sequence select="true()"/>
  </xsl:template>
  
  <xsl:template match="*" mode="is-callout" as="xs:boolean">
    <xsl:sequence select="false()"/>
  </xsl:template>
  

  <xsl:template match="*" mode="render-css"/>
  
  <xsl:template mode="render-css" match="w:rPr">
   <xsl:value-of separator="; ">
     <xsl:apply-templates mode="#current"/>
   </xsl:value-of><!---->
  </xsl:template>
  
  <xsl:template mode="render-css" as="xs:string" match="w:rFonts">
    <xsl:value-of>
      <xsl:text>font-family: </xsl:text>
      <xsl:value-of select="@w:ascii"/>
    </xsl:value-of>
  </xsl:template>
  
  <xsl:template mode="render-css" as="xs:string" match="w:sz | w:szCs">
    <xsl:value-of>
      <xsl:text>font-size: </xsl:text>
      <xsl:value-of select="@w:val"/>
    </xsl:value-of>
  </xsl:template>
  
  <xsl:template mode="render-css" as="xs:string" match="w:color">
    <xsl:value-of>
      <xsl:text>color: </xsl:text>
      <xsl:value-of select="@w:val/replace(.,'^\d','#$0')"/>
    </xsl:value-of>
  </xsl:template>
  
</xsl:stylesheet>