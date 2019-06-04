<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:css="http://www.w3.org/1996/css"
  xmlns:functx="http://www.functx.com" 
  xmlns:hub2htm="http://transpect.io/hub2htm" 
  exclude-result-prefixes="xs functx css hub2htm"
  version="2.0">
  
  <xsl:import href="http://www.functx.com/XML_Elements_and_Attributes/XML_Document_Structure/path-to-node-with-pos.xsl"/>

  <xsl:key name="css:rule" match="css:rule" use="@name"/>
  
  <!-- Whether css:font-style="italic" in a named rule should wrap content in  <italic> 
    or whatever is configured as css:italic-elt-name. Same for bold and underline, if the
    corresponding css:…-elt-name variables are non-empty.
    Don’t change the default value, true(), here because other pipelines may depend on this 
    default value.
  -->
  <xsl:variable name="css:wrap-content-with-elements-from-mappable-style-attributes" as="xs:boolean"
    select="true()"/>

  <xsl:variable name="css:rule-selection-attribute-names" 
    select="if (/*/@css:rule-selection-attribute)
            then tokenize(/*/@css:rule-selection-attribute, '\s+')
            else ()"
    as="xs:string*"/>

  <!-- css:content is a default template for content elements (paras, spans, maybe 
       also cells and titles). It processes CSS attributes in the current mode,
       wraps content into new elements generated from certain CSS attributes 
       (e.g. italic, bold ). You can control what is wrapped by overwriting either the 
       whole function css:map-att-to-elt or by assigning custom wrapper element names 
       to the variables css:italic-elt-name, css:bold-elt-name, and 
       css:underline-elt-name. After the wrappers are in place, the content will
       be processed in the current mode.
       Usage example: 
       <xsl:template match="para" mode="mymode">
         <p>
           <xsl:call-template name="css:content"/>
         <p>
       </xsl:template>
  -->
  <xsl:template name="css:content">
    <xsl:param name="also-consider-rule-atts" as="xs:boolean"
      select="$css:wrap-content-with-elements-from-mappable-style-attributes"/>
    <xsl:param name="css:rule-selection-attribute-names" as="xs:string*"
      select="$css:rule-selection-attribute-names"/>
    <xsl:param name="css:apply-other-atts" as="xs:boolean"
      select="true()"/>
    <xsl:param name="root" as="document-node()" select="root(.)" tunnel="yes"/>
    <xsl:variable name="other-atts" as="attribute(*)*" select="css:other-atts(.)"/>
    <xsl:variable name="atts" as="attribute(*)*"
                  select="(
                    @*
                    union
                    key('css:rule', 
                        for $a in @*[name() = $css:rule-selection-attribute-names]
                        return distinct-values(($a, tokenize($a, '\s+'))), (: we don’t tokenize at _-_ :) 
                        $root)
                      [$css:wrap-content-with-elements-from-mappable-style-attributes]
                      /@*
                  )[css:map-att-to-elt(., current())]"/>
    <xsl:apply-templates select="." mode="class-att"/>
    <xsl:call-template name="css:remaining-atts">
      <xsl:with-param name="remaining-atts" select="$other-atts[not(name() = $atts/name())]"/>
    </xsl:call-template>
    <xsl:call-template name="css:wrap">
      <xsl:with-param name="atts" select="$atts"/>
      <xsl:with-param name="other-atts" as="attribute(*)*" select="$other-atts[name() = $atts/name()]" tunnel="yes"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:function name="css:other-atts" as="attribute(*)*">
    <xsl:param name="context" as="element(*)"/>
    <xsl:sequence select="$context/@*[not(css:map-att-to-elt(., ..))]
                                     [not(name() = $css:rule-selection-attribute-names)]"/>
    <!-- may be overridden with
      <xsl:sequence select="$context/@*[not(css:map-att-to-elt(., ..))]"/> 
    for other vocabularies (e.g., hub2bits) -->
  </xsl:function>
  
  <!-- This should be overridden, for example with the following:
  <xsl:template name="css:other-atts">
    <xsl:apply-templates select="." mode="class-att"/>
    <xsl:call-template name="css:remaining-atts">
      <xsl:with-param name="remaining-atts" 
        select="@*[not(css:map-att-to-elt(., ..))]"/>
    </xsl:call-template>
  </xsl:template>
  -->    
  <xsl:template name="css:other-atts">
    <xsl:apply-templates select="@*[not(css:map-att-to-elt(., ..))]" mode="#current"/>
  </xsl:template>
  
  <xsl:param name="css:wrap-namespace" as="xs:string" select="'http://www.w3.org/1999/xhtml'"/> 
  
  <xsl:template name="css:wrap">
    <xsl:param name="atts" as="attribute(*)*"/>
    <xsl:param name="other-atts" as="attribute(*)*" tunnel="yes"/>
    <xsl:choose>
      <xsl:when test="$atts">
        <xsl:element name="{css:map-att-to-elt($atts[1], .)}" namespace="{$css:wrap-namespace}">
          <xsl:call-template name="css:remaining-atts">
            <xsl:with-param name="remaining-atts" select="$other-atts[name() = $atts[1]/name()]"/>
          </xsl:call-template>
          <xsl:call-template name="css:wrap">
            <xsl:with-param name="atts" select="subsequence($atts, 2)"/>
            <xsl:with-param name="other-atts" select="$other-atts[not(name() = $atts[1]/name())]" tunnel="yes"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="css:remaining-atts">
          <xsl:with-param name="remaining-atts" select="$other-atts"/>
        </xsl:call-template>
        <xsl:apply-templates mode="#current"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- If you don’t want a style attribute, just do an 
    <xsl:apply-templates select="$remaining-atts" mode="hub2htm:css-style-overrides"/>
  in your overrides.  
  -->
  <xsl:template name="css:remaining-atts">
    <xsl:param name="remaining-atts" as="attribute(*)*"/>
    <xsl:variable name="atts" as="attribute(*)*">
      <xsl:apply-templates select="$remaining-atts[not(namespace-uri() = 'http://www.w3.org/1996/css')]" mode="#current"/>
    </xsl:variable>
    <xsl:variable name="css-atts" as="attribute(*)*">
      <!-- This mode enables selective property filtering in your overriding template -->
      <xsl:apply-templates select="$remaining-atts[namespace-uri() = 'http://www.w3.org/1996/css']" mode="hub2htm:css-style-overrides"/>
    </xsl:variable>
    <xsl:if test="exists($css-atts)">
      <xsl:attribute name="style"
        select="string-join(
                  for $a in $css-atts[not(starts-with(name(), 'pseudo'))] 
                  return concat(local-name($a), ': ', $a),
                  '; '
                )" />
    </xsl:if>
    <xsl:sequence select="$atts"/>
  </xsl:template>
  
  <xsl:variable name="css:italic-elt-name" as="xs:string?" select="'i'"/>
  <xsl:variable name="css:bold-elt-name" as="xs:string?" select="'b'"/>
  <xsl:variable name="css:subscript-elt-name" as="xs:string?" select="'sub'"/>
  <xsl:variable name="css:superscript-elt-name" as="xs:string?" select="'sup'"/>
  <!--<xsl:variable name="css:underline-elt-name" as="xs:string?" select="'u'"/>--><!-- not permitted in XHTML -->
  
  <xsl:function name="css:map-att-to-elt" as="xs:string?">
    <xsl:param name="prop" as="attribute(*)"/>
    <xsl:param name="context" as="element(*)"/>
    <xsl:apply-templates select="$prop" mode="css:map-att-to-elt">
      <xsl:with-param name="context" select="$context"/>
    </xsl:apply-templates>
  </xsl:function>
  
  <!-- Default: don’t return anything: -->
  <xsl:template match="@*" mode="css:map-att-to-elt"/>
  
  <xsl:template match="@css:font-style[. = ('italic', 'oblique')]" mode="css:map-att-to-elt" as="xs:string?">
    <xsl:param name="context" as="element(*)?"/>
    <xsl:sequence select="$css:italic-elt-name"/>
  </xsl:template>
  
  <xsl:template match="@remap[. = 'superscript']" mode="css:map-att-to-elt" as="xs:string?">
    <xsl:param name="context" as="element(*)?"/>
    <xsl:sequence select="$css:superscript-elt-name"/>
  </xsl:template>
  
  <xsl:template match="@remap[. = 'subscript']" mode="css:map-att-to-elt" as="xs:string?">
    <xsl:param name="context" as="element(*)?"/>
    <xsl:sequence select="$css:subscript-elt-name"/>
  </xsl:template>

  <xsl:template match="@css:font-weight[matches(., '^bold|[6-9]00$')]" 
    mode="css:map-att-to-elt" as="xs:string?">
    <xsl:param name="context" as="element(*)?"/>
    <xsl:sequence select="$css:bold-elt-name"/>
  </xsl:template>
  
  <!--<xsl:template match="@css:text-decoration-line[. = ('underline')]" mode="css:map-att-to-elt" as="xs:string?">
    <xsl:param name="context" as="element(*)?"/>
    <xsl:sequence select="$css:underline-elt-name"/>
  </xsl:template>--><!-- not permitted in xhtml -->
  

  <xsl:template match="*" mode="css:unhandled" >
    <xsl:message select="string-join((name(), concat('/', functx:path-to-node-with-pos(.)), substring(normalize-space(.),1,40)), '  ')" /> 
    <xsl:apply-templates select="@*" mode="#current" />
  </xsl:template>
  
  <xsl:template match="@*" mode="css:unhandled" >
    <xsl:message select="concat('  ', name(), '=', ., '  ', concat('/', functx:path-to-node-with-pos(.)))" /> 
  </xsl:template>
  
  <!-- This template should be called as in:
    <xsl:template match="css:rule" mode="mymode">
      <xsl:call-template name="css:move-to-attic">
        <xsl:with-param name="atts" select="@*[css:map-att-to-elt(.)]"/>
      </xsl:call-template>
    </xsl:template>
  -->
  <xsl:template name="css:move-to-attic" as="element(css:rule)">
    <xsl:param name="atts" as="attribute(*)*"/>
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@* except $atts" mode="#current"/>
      <xsl:if test="exists($atts union css:attic/(@*, *))">
        <css:attic>
          <xsl:apply-templates select="$atts union css:attic/@*" mode="#current"/>
        </css:attic>
      </xsl:if>
      <xsl:apply-templates select="* except css:attic" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- Like hub:equiv-props, but also considering the props as equivalent that will 
       be resolved to the same wrapping markup. -->
  <xsl:function name="css:equiv-props" as="attribute(*)*">
    <xsl:param name="prop" as="attribute(*)"/>
    <xsl:param name="context" as="element(*)?"/>
    <xsl:param name="props" as="attribute(*)*"/>
    <xsl:sequence select="$props[css:map-att-to-elt(., $context) = css:map-att-to-elt($prop, $context)]
                          union
                          $props[name() = name($prop) and . = $prop]"/>
  </xsl:function>
  
  <!-- Note that this key is for Hub input -->
  <xsl:key name="css:styled-content" match="*[@role]" use="@role"/>
  
</xsl:stylesheet>
