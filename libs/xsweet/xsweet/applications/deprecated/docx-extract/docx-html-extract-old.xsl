<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
  xmlns="http://www.w3.org/1999/xhtml" xmlns:xsw="http://coko.foundation/xsweet"
  exclude-result-prefixes="#all" xmlns:fn="http://www.example.com/fn">

<!-- For docs on WordML, see (at least):
  
  http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/index.html
  
  -->


  <!-- Indent should really be no, but for testing. -->
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

  <xsl:variable name="endnotes-file"  select="resolve-uri('endnotes.xml',  document-uri(/))"/>
  <xsl:variable name="footnotes-file" select="resolve-uri('footnotes.xml', document-uri(/))"/>
  <xsl:variable name="styles-file"    select="resolve-uri('styles.xml',    document-uri(/))"/>
  <!-- We have no interest in stylesWithEffects.xml. -->

  <!--<xsl:variable name="endnotes-file" select="'x'"/>
  <xsl:variable name="styles-file"   select="'x'"/>-->

  <xsl:variable name="endnotes-doc">
    <xsl:if test="doc-available($endnotes-file)">
      <xsl:sequence select="doc($endnotes-file)"/>
    </xsl:if>
  </xsl:variable>
  
  <xsl:variable name="footnotes-doc">
    <xsl:if test="doc-available($footnotes-file)">
      <xsl:sequence select="doc($footnotes-file)"/>
    </xsl:if>
  </xsl:variable>
  
  <!-- If no styles are found we get a root (temporary tree) w/ no branches. -->
  <xsl:variable name="styles">
    <xsl:if test="doc-available($styles-file)">
      <xsl:sequence select="doc($styles-file)"/>
    </xsl:if>
  </xsl:variable>
  
  
  <xsl:key name="styles-by-id" match="w:style" use="@w:styleId"/>

  <!-- Reinstate footnotes handling when we have some. -->
  <!-- <xsl:variable name="footnotes-doc" select="document('footnotes.xml',/)"/>
       <xsl:key name="footnotes-by-id" match="w:footnote" use="@w:id"/> -->


  
 <!-- Run on 'document.xml' inside a .docx -->

  <!-- Note that unprefixed elements are in namespace http://www.w3.org/1999/xhtml -->
  <xsl:template match="/w:document">
    <html>
      <head>
        <meta charset="UTF-8"/>
        <style type="text/css">
          <!-- Retrieving and writing only those styles actually used.
               #Todo: traverse style derivation tree to pick up defaults. -->
          <xsl:apply-templates select="//(w:pStyle|w:rStyle)/key('styles-by-id',@w:val, $styles)"/>
        </style>
      </head>
      <xsl:apply-templates select="w:body"/>
    </html>
  </xsl:template>

  <!-- DrawingML - we traverse in case there's content buried therein, but we do not pursue. -->
  <xsl:template match="wp:*" xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing">
    <!-- Nor do we pick up text unless we kick back into the w: namespace. -->
    <xsl:apply-templates select="*"/>
  </xsl:template>

  <xsl:template match="w:body">
    <body>
      <div class="docx-body">
        <xsl:apply-templates select="w:p | w:tbl"/>
      </div>
      <div class="docx-endnotes">
        <xsl:apply-templates select="$endnotes-doc/*/w:endnote"/>
      </div>
      <div class="docx-footnotes">
        <xsl:apply-templates select="$footnotes-doc/*/w:footnote"/>
      </div>
    </body>
  </xsl:template>

  <xsl:template match="w:drawing">
    <div class="drawing">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="w:endnote">
    <div class="docx-endnote" id="en{@w:id}">
      <xsl:apply-templates select="w:p"/>
    </div>
  </xsl:template>
  
  <xsl:template match="w:endnoteRef | w:footnoteRef">
    <span class="{local-name()}">
      <xsl:comment> value to be generated </xsl:comment>
    </span>
  </xsl:template>

  <xsl:template match="w:footnote">
    <div class="docx-footnote" id="fn{@w:id}">
      <xsl:apply-templates select="w:p"/>
    </div>
  </xsl:template>
  
  
  <!-- //w:p/w:pPr/w:pStyle -->
  <xsl:function name="xsw:safeClass" as="xs:string">
    <xsl:param name="val" as="attribute()"/>
    <xsl:variable name="safer" select="replace($val, '[\.:\C]', '')"/>
    <xsl:value-of>
      <!-- Drop in a _ if the first character is not an initial name char in XML (or HTML NMTOKEN) -->
      <xsl:if test="matches($safer, '^\I')">_</xsl:if>
      <xsl:value-of select="$safer"/>
    </xsl:value-of>
  </xsl:function>

  <xsl:template match="w:p">
    <p>
      <!-- Copying any style declaration -->
      <xsl:for-each select="w:pPr/w:pStyle">
        <!-- strip periods, colons and non-XML name chars (this includes spaces) -->
        <xsl:attribute name="class">
          <xsl:value-of select="xsw:safeClass(@w:val)"/>
        </xsl:attribute>
      </xsl:for-each>

      <!-- Also promoting (some) properties to CSS @style -->
      <xsl:variable name="style">
        <xsl:apply-templates mode="render-css" select="w:pPr"/>
      </xsl:variable>
      <!-- Adding the attribute only when there is a value. -->
      <xsl:if test="matches($style, '\S')">
        <xsl:attribute name="style" select="$style"/>
      </xsl:if>
      <xsl:apply-templates select="*"/>
    </p>
  </xsl:template>

  <xsl:template match="w:tbl">
    <table>
      <xsl:apply-templates select="w:tr"/>
    </table>
  </xsl:template>
  
  <xsl:template match="w:tr">
    <tr>
      <xsl:apply-templates select="w:tc"/>
    </tr>
  </xsl:template>
  
  <xsl:template match="w:tc">
    <td>
      <xsl:apply-templates select="w:p"/>
    </td>
  </xsl:template>
  
  <!-- Drop in default traversal -->
  <xsl:template match="w:pPr"/>

  <!-- Nothing to see here :-( keep going. -->
  <xsl:template match="w:hyperlink">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="w:r">
    <span>
      <xsl:variable name="literal-css">
        <xsl:apply-templates select="w:rPr" mode="render-css"/>
      </xsl:variable>
      <xsl:if test="matches($literal-css,'\S')">
        <xsl:attribute name="style" select="$literal-css"/>
      </xsl:if>
      <xsl:call-template name="format-components"/>
    </span>
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
          <!-- to get whitespace, for example -->
          <xsl:apply-templates select="current-group()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each-group>
  </xsl:template>

  <!-- Nothing interesting here; keep moving. (Revise this if it ever changes.) -->
  <xsl:template match="w:instrText"/>
  
  <xsl:template match="w:endnoteReference" priority="3">
    <!-- All we have to go on is the @w:id ... so we take it -->
    <a class="endnoteReference" href="#en{@w:id}">
      <xsl:apply-templates/>
      <xsl:if test="empty(node())">
        <xsl:comment> (generated) </xsl:comment>
      </xsl:if>
    </a>
  </xsl:template>
  
  <xsl:template match="w:footnoteReference" priority="3">
    <!-- Just like the endnoteReference -->
    <a class="footnoteReference" href="#fn{@w:id}">
      <xsl:apply-templates/>
      <xsl:if test="empty(node())">
        <xsl:comment> (generated) </xsl:comment>
      </xsl:if>
    </a>
  </xsl:template>
  
  <!-- Again overriding the default behavior for w:r/*, to the same effect.
       Check and switch on when we do footnotes. 
       See line 50 or so (template @match='w:body') -->
  <!--<xsl:template match="w:footnoteReference" priority="3">
    <div class="footnote_fetched">
      <xsl:apply-templates select="key('footnotes-by-id',@w:id,$footnotes-doc)"/>
    </div>
  </xsl:template>-->

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
  
  <xsl:template priority="10" match="w:rPr/w:b[@w:val='0'] | w:rPr/w:i[@w:val='0'] | w:rPr/w:u[@w:val=('0','none')] | 
    w:rPr/w:smallCaps[@w:val='0'] | w:rPr/w:color[@w:val='000000']">
    <xsl:call-template name="tuck-next"/>
  </xsl:template>
  
  
  <!-- http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/ST_VerticalAlignRun.html -->
  <!--<w:vertAlign w:val="superscript"/>-->
  <xsl:template priority="4" match="w:rPr/w:vertAlign[@w:val='superscript']">
    <!-- https://msdn.microsoft.com/en-us/library/documentformat.openxml.wordprocessing.boldcomplexscript(v=office.14).aspx -->
    <!-- But note this template is overridden below for most w:bCs as we consider it redundant when already bold. -->
    <sup>
      <xsl:call-template name="tuck-next"/>
    </sup>
  </xsl:template>
  
  <xsl:template priority="4" match="w:rPr/w:vertAlign[@w:val='subscript']">
    <!-- https://msdn.microsoft.com/en-us/library/documentformat.openxml.wordprocessing.boldcomplexscript(v=office.14).aspx -->
    <!-- But note this template is overridden below for most w:bCs as we consider it redundant when already bold. -->
    <sub>
      <xsl:call-template name="tuck-next"/>
    </sub>
  </xsl:template>
  
  <xsl:template priority="3" match="w:rPr/w:bCs">
    <!-- https://msdn.microsoft.com/en-us/library/documentformat.openxml.wordprocessing.boldcomplexscript(v=office.14).aspx -->
    <xsl:call-template name="tuck-next"/>
  </xsl:template>
  
  <xsl:template priority="3" match="w:rPr/w:rtl">
    <!-- https://msdn.microsoft.com/en-us/library/office/aa173442(v=office.11).aspx -->
    <xsl:call-template name="tuck-next"/>
  </xsl:template>
  
  <xsl:template priority="5" match="w:u[not(matches(@w:val, '\S'))]">
    <xsl:call-template name="tuck-next"/>
  </xsl:template>

  <!-- When there's an inline style, announce it. -->
  <xsl:template priority="5" match="w:rPr/w:rStyle">
    <span class="{@w:val}">
      <xsl:call-template name="tuck-next"/>
    </span>
  </xsl:template>
  
  <xsl:template match="w:tab">
    <!-- Not html, but we'll survive -->
    <tab/>
  </xsl:template>
  
  <!-- This should match any formatting we don't wish to see among wrapped inline elements;
       note that the same formatting properties may be detected in/by CSS reflection instead. -->
  <xsl:template priority="5"
    match="w:rPr/w:sz | w:rPr/w:szCs | w:rPr/w:rFonts | w:rPr/w:color | w:rPr/w:shd | w:rPr/w:smallCaps">
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
    <xsl:sequence select="($n is $n/self::w:tab) or exists($n/../w:rPr) and not($n-is-callout)"/>
  </xsl:function>

  <!-- If we didn't want to see these wrapped in formatting ...
  <xsl:template match="w:footnoteReference | w:endnoteReference" mode="is-callout" as="xs:boolean">
    <xsl:sequence select="true()"/>
  </xsl:template>  -->

  <xsl:template match="*" mode="is-callout" as="xs:boolean">
    <xsl:sequence select="false()"/>
  </xsl:template>


  <xsl:template match="*" mode="render-css"/>
  
  <xsl:template mode="render-css transcribe-css" match="w:pPr | w:rPr" as="xs:string?">
    <xsl:value-of separator="; ">
      <xsl:apply-templates mode="#current"/>
    </xsl:value-of>
    <!---->
  </xsl:template>

  <!-- Inside w:pPr -->
  <xsl:template mode="render-css" match="w:ind" as="xs:string*">
    <xsl:apply-templates mode="#current" select="@w:left | @w:right | @w:firstLine | @w:hanging"/>
  </xsl:template>

  <xsl:template mode="render-css" match="w:spacing" as="xs:string*">
    <xsl:apply-templates mode="#current" select="@w:before | @w:after"/>
  </xsl:template>

  <xsl:template mode="render-css" match="w:ind/@* | w:spacing/@*" as="xs:string">
    <xsl:value-of>
      <xsl:apply-templates mode="css-property" select="."/>
      <xsl:text>: </xsl:text>
      <xsl:value-of select=". div 20"/>
      <xsl:text>pt</xsl:text>
    </xsl:value-of>
  </xsl:template>

  <!-- <w:jc w:val="center"/> doing center, left, right, justified (ha) -->
  <!--http://webapp.docx4java.org/OnlineDemo/ecma376/WordML/ST_Jc.html-->
  
  <!-- With apologies, not supporting other values of text alignment in Word. -->
  <xsl:template priority="2" mode="render-css" match="w:jc[@w:val=('left','right','center','both')]" as="xs:string">
    <xsl:value-of>
      <xsl:text>text-align: </xsl:text>
      <xsl:value-of select="if (@w:val = 'both') then 'justify' else @w:val"/>
    </xsl:value-of>
  </xsl:template>
  
  <xsl:template priority="2" mode="render-css" match="w:ind/@w:hanging" as="xs:string">
    <xsl:value-of>
      <xsl:text>text-indent: -</xsl:text>
      <xsl:value-of select=". div 20"/>
      <xsl:text>pt; padding-left: </xsl:text>
      <xsl:value-of select=". div 20"/>
      <xsl:text>pt</xsl:text>
    </xsl:value-of>
  </xsl:template>

  <xsl:template mode="css-property" match="w:spacing/@w:before">margin-top</xsl:template>
  <xsl:template mode="css-property" match="w:spacing/@w:after">margin-bottom</xsl:template>
  <xsl:template mode="css-property" match="w:ind/@w:left">margin-left</xsl:template>
  <xsl:template mode="css-property" match="w:ind/@w:right">margin-right</xsl:template>
  <xsl:template mode="css-property" match="w:ind/@w:firstLine">text-indent</xsl:template>

  <xsl:template mode="render-css" as="xs:string"
    match="w:rFonts[exists(@w:ascii | @w:cs | @w:hAnsi | @w:eastAsia)]">
    <xsl:value-of>
      <xsl:text>font-family: </xsl:text>
      <xsl:value-of select="(@w:ascii, @w:cs, @w:hAnsi, @w:eastAsia)[1]"/>
    </xsl:value-of>
  </xsl:template>

  <xsl:template mode="render-css" as="xs:string?" match="w:szCs[. = (../w:sz)]"/>

  <!-- Font size for complex scripts (szCs) is just noise. -->
  <xsl:template mode="render-css" as="xs:string?" match="w:szCs"/>

  <xsl:template mode="render-css" as="xs:string" match="w:sz">
    <xsl:value-of>
      <xsl:text>font-size: </xsl:text>
      <xsl:value-of select="@w:val div 2"/>
      <xsl:text>pt</xsl:text>
    </xsl:value-of>
  </xsl:template>

  <xsl:template mode="render-css" as="xs:string" match="w:smallCaps[not(@w:val='0')]">
    <xsl:text>font-variant: small-caps</xsl:text>
  </xsl:template>

  <xsl:template mode="render-css" as="xs:string?" match="w:color">
    <xsl:if test="not(@w:val='000000')">
    <xsl:value-of>
      <xsl:text>color: </xsl:text>
      <xsl:value-of select="@w:val/replace(., '^\d', '#$0')"/>
    </xsl:value-of>
    </xsl:if>
  </xsl:template>

  <!-- Wrapper mode 'transcribe-css' enables us to call render-css but also
       override it in cases. -->
  <xsl:template match="*" mode="transcribe-css" as="xs:string*">
    <xsl:apply-templates select="." mode="render-css"/>
  </xsl:template>
  
  <xsl:template match="w:b[not(@val=0)]" mode="transcribe-css" as="xs:string">
    <xsl:text>font-weight: bold</xsl:text>
  </xsl:template>
  
  <xsl:template match="w:i[not(@val=0)]" mode="transcribe-css" as="xs:string">
    <xsl:text>font-style: italic</xsl:text>
  </xsl:template>
  
  
  <!-- Generating CSS from Word (paragraph and text) styles. -->

  <xsl:template match="w:styles">
    <style type="text/css">
      <xsl:apply-templates/>
    </style>
  </xsl:template>

  <xsl:template match="w:styles/*">
    <xsl:text>&#xA;.</xsl:text>
    <xsl:value-of select="xsw:safeClass(@w:styleId)"/>
    <xsl:text> { </xsl:text>
      <xsl:apply-templates select="." mode="writeCSS"/>
    <xsl:text> }</xsl:text>
  </xsl:template>
  
  <xsl:template mode="writeCSS" match="w:styles/*">

    <!-- To traverse to linked styles ... -->
    <xsl:param name="visited" select="()"/>
    
    <xsl:apply-templates mode="writeCSS"
      select="key('styles-by-id', (w:link/@w:val[false()] | w:basedOn/@w:val)[empty(. intersect $visited)])">
      <xsl:with-param name="visited" select="$visited, ."/>
    </xsl:apply-templates>
    
    <xsl:text>/* </xsl:text>
    <xsl:value-of select="@w:styleId"/>
    <xsl:text>*/ </xsl:text>
    
    <xsl:variable name="css-produced" as="xs:string*">
      <xsl:apply-templates select="w:pPr | w:rPr" mode="transcribe-css"/>
    </xsl:variable>
    
    <xsl:if test="exists($visited) and exists($css-produced[matches(.,'\S')])">; </xsl:if>
    <xsl:value-of select="$css-produced[matches(.,'\S')]" separator="; "/>
    
    
  </xsl:template>

</xsl:stylesheet>