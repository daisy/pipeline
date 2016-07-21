<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:louis="http://liblouis.org/liblouis"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                exclude-result-prefixes="#all"
                version="2.0">
   
   <!--
       css-utils [2.0.0,3.0.0)
   -->
   <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
   
   <xsl:variable name="page-width" as="xs:integer"
                 select="xs:integer(number((/louis:box/@width,/*/louis:page-layout//c:param[@name='louis:page-width']/@value)[1]))"/>
   
   <xsl:template match="@*|node()">
      <xsl:copy>
         <xsl:apply-templates select="@*|node()"/>
      </xsl:copy>
   </xsl:template>
   
   <xsl:template match="*[child::louis:toc-item]">
      <xsl:copy>
         <xsl:apply-templates select="@*"/>
         <xsl:for-each-group select="*|text()[normalize-space(.)!='']"
                             group-adjacent="boolean(self::louis:toc-item)">
            <xsl:choose>
               <xsl:when test="current-grouping-key()">
                  <xsl:for-each-group select="current-group()"
                                      group-adjacent="for $ref in (@ref) return
                                                      base-uri(collection()/*[descendant::*[@xml:id=$ref]])">
                     <xsl:variable name="href" as="xs:string" select="current-grouping-key()"/>
                     
                     <xsl:for-each-group select="current-group()"
                                         group-adjacent="pxi:concretize-auto(css:specified-properties(
                                                           'right', true(), true(), true(), .), .)/@value">
                       
                        <xsl:variable name="right" as="xs:integer" select="xs:integer(number(current-grouping-key()))"/>
                        <xsl:for-each-group select="current-group()" group-adjacent="string(@print-page)">
                           <xsl:variable name="print-page" as="xs:string" select="current-grouping-key()"/>
                           <xsl:for-each-group select="current-group()" group-adjacent="string(@braille-page)">
                              <xsl:variable name="braille-page" as="xs:string" select="current-grouping-key()"/>
                              <xsl:for-each-group select="current-group()" group-adjacent="string(@leader)">
                                 <xsl:variable name="leader" as="xs:string" select="current-grouping-key()"/>
                                 <xsl:element name="louis:div">
                                    <xsl:attribute name="css:display" select="'block'"/>
                                    <xsl:sequence select="css:style-attribute(css:serialize-declaration-list(
                                                            (css:property('left', 0), css:property('right', $right))))"/>
                                    <xsl:element name="louis:toc">
                                       <xsl:attribute name="href" select="$href"/>
                                       <xsl:attribute name="width" select="$page-width - $right"/>
                                       <xsl:if test="$print-page!=''">
                                          <xsl:attribute name="print-pages" select="$print-page"/>
                                       </xsl:if>
                                       <xsl:if test="$braille-page!=''">
                                          <xsl:attribute name="braille-pages" select="$braille-page"/>
                                       </xsl:if>
                                       <xsl:if test="$leader!=''">
                                          <xsl:attribute name="leader" select="$leader"/>
                                       </xsl:if>
                                       <xsl:for-each select="current-group()">
                                          <xsl:copy>
                                             <xsl:sequence select="@ref"/>
                                             <xsl:sequence select="css:style-attribute(css:serialize-declaration-list(
                                                                     pxi:concretize-auto(css:specified-properties(
                                                                       'left text-indent', true(), true(), true(), .), .)))"/>
                                          </xsl:copy>
                                       </xsl:for-each>
                                    </xsl:element>
                                 </xsl:element>
                              </xsl:for-each-group>
                           </xsl:for-each-group>
                        </xsl:for-each-group>
                     </xsl:for-each-group>
                  </xsl:for-each-group>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:for-each select="current-group()">
                     <xsl:apply-templates select="."/>
                  </xsl:for-each>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:for-each-group>
      </xsl:copy>
   </xsl:template>
   
   <xsl:function name="pxi:concretize-auto">
     <xsl:param name="properties" as="element()*"/>
     <xsl:param name="context" as="element()"/>
     <xsl:apply-templates select="$properties" mode="concretize-auto">
       <xsl:with-param name="context" select="$context"/>
     </xsl:apply-templates>
   </xsl:function>
   
   <!--
       assuming that margins, borders and paddings have been handled previously (handle-css-margin.xsl)
   -->
   <xsl:template match="css:property[@name=('left','right') and @value=('auto','inherit')]"
                 mode="concretize-auto">
     <xsl:param name="context" as="element()"/>
     <xsl:sequence select="if ($context/parent::*)
                           then pxi:concretize-auto(
                                  css:specified-properties(@name, false(), true(), true(), $context/parent::*), $context/parent::*)
                           else css:property(@name, 0)"/>
   </xsl:template>
   
   <xsl:template match="css:property" mode="concretize-auto">
     <xsl:sequence select="."/>
   </xsl:template>
   
</xsl:stylesheet>
