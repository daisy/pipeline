<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
                xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
                xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
                xmlns:dcterms="http://purl.org/dc/terms/"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
                xmlns:v="urn:schemas-microsoft-com:vml"
                xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
                xmlns:dcmitype="http://purl.org/dc/dcmitype/"
                xmlns:o="urn:schemas-microsoft-com:office:office"
                xmlns:d="org.daisy.pipeline.word_to_dtbook.impl.DaisyClass"
                xmlns="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="w pic wp dcterms xsi cp dc a r v dcmitype d xsl m o xs">
  <!--Parameter citation-->
  <xsl:param name="Cite_style" select="d:Citation($myObj)"/>

  <!--template for frontmatter elements-->
  <xsl:template name="TableOfContents">
    <xsl:param name="pagination" as="xs:string"/>
    <!-- <xsl:message terminate="no">progress:Handling table of content</xsl:message> -->

      <!--checking for Table of content Element-->
      <xsl:for-each select="$documentXml//w:document/w:body/node()">
        <!--Checking for w:p element-->
        <xsl:if test="self::w:p">
          <!-- Checking for TOC style-->
          <xsl:if test="w:pPr/w:pStyle[substring(@w:val,1,3)='TOC']">
            <!--Checking for w:hyperlink Element-->
            <xsl:if test="w:hyperlink">
              <xsl:variable name="set_list" as="xs:integer" select="d:Set_Toc($myObj)"/>
              <xsl:if test="$set_list=1">
                <!--Opening level1 and list Tag-->
                <xsl:value-of disable-output-escaping="yes" select="'&lt;level1&gt;'"/>
                <xsl:if test="w:pPr/w:pStyle[@w:val='TOCHeading']">
                  <h1>
                    <xsl:value-of select="w:r/w:t"/>
                  </h1>
                </xsl:if>
                <xsl:value-of disable-output-escaping="yes" select="'&lt;list type=&quot;pl&quot;&gt;'"/>
              </xsl:if>
              <!--Checking for w:hyperlink Element-->
              <xsl:if test="w:hyperlink and not(w:pPr/w:pStyle[@w:val='TOCHeading'])">
                <xsl:value-of disable-output-escaping="yes" select="'&lt;li&gt;'"/>
                <xsl:for-each select="w:hyperlink/w:r/w:rPr/w:rStyle[@w:val='Hyperlink']">
                  <xsl:variable name="club" as="xs:string" select="../../w:t"/>
                  <xsl:value-of select="d:SetTOCMessage($myObj,$club)"/>
                </xsl:for-each>
                <lic>
                  <xsl:value-of select="d:GetTOCMessage($myObj)"/>
                </lic>
                <xsl:value-of select="d:NullMsg($myObj)"/>
                <xsl:for-each select="w:hyperlink/w:r">
                  <xsl:if test="not(w:rPr/w:rStyle[@w:val='Hyperlink'])and w:t">
                    <lic>
                      <xsl:attribute name="class">pagenum</xsl:attribute>
                      <xsl:text>  </xsl:text>
                      <xsl:value-of select="w:t"/>
                    </lic>
                  </xsl:if>
                </xsl:for-each>
                <!--Closing li Tag-->
                <xsl:value-of disable-output-escaping="yes" select="'&lt;/li&gt;'"/>
              </xsl:if>
            </xsl:if>
          </xsl:if>
        </xsl:if>
      </xsl:for-each>
      <xsl:if test="d:Set_Toc($myObj)&gt;1">
        <!--Closing list tag-->
        <xsl:value-of disable-output-escaping="yes" select="'&lt;/list&gt;'"/>
        <!--Closing level1-->
        <xsl:value-of disable-output-escaping="yes" select="'&lt;/level1&gt;'"/>
      </xsl:if>
      <!-- Calling function to Reset the counter value -->
      <xsl:sequence select="d:sink(d:Get_Toc($myObj))"/> <!-- empty -->

      <!--checking for Table of content-->
      <xsl:for-each select="$documentXml//w:document/w:body/node()">
        <!--Checking for w:p Tag-->
        <xsl:if test="self::w:p">
          <!--Checking for TOC Style-->
          <xsl:if test="w:pPr/w:pStyle[substring(@w:val,1,3)='TOC']">
            <xsl:if test ="not(w:hyperlink)">
              <xsl:variable name="set_list" as="xs:integer" select="d:Set_Toc($myObj)"/>
              <xsl:if test="$set_list=1">
                <!--opening level1 and list Tag-->
                <xsl:value-of disable-output-escaping="yes" select="'&lt;level1&gt;'"/>
                <xsl:if test="w:pPr/w:pStyle[@w:val='TOCHeading']">
                  <h1>
                    <xsl:value-of select="w:r/w:t"/>
                  </h1>
                </xsl:if>
                <xsl:value-of disable-output-escaping="yes" select="'&lt;list type=&quot;pl&quot;&gt;'"/>
              </xsl:if>
              <xsl:for-each select=".">
                <xsl:value-of disable-output-escaping="yes" select="'&lt;li&gt;'"/>
                <xsl:for-each select="w:r">
                  <xsl:if test="w:t">
                    <xsl:variable name="setToc" as="xs:integer" select="d:Set_tabToc($myObj)"/>
                    <xsl:choose>
                      <xsl:when test="$setToc&gt;=2">
                        <lic>
                          <xsl:attribute name="class">
                            <xsl:value-of select="'pagenum'"/>
                          </xsl:attribute>
                          <xsl:text> </xsl:text>
                          <xsl:value-of select="w:t"/>
                        </lic>
                      </xsl:when>
                      <xsl:otherwise>
                        <lic>
                          <xsl:value-of select="w:t"/>
                        </lic>
                      </xsl:otherwise>
                    </xsl:choose>
                  </xsl:if>
                </xsl:for-each>
                <xsl:sequence select="d:sink(d:Get_tabToc($myObj))"/> <!-- empty -->
                <!--Closing the li Tag-->
                <xsl:value-of disable-output-escaping="yes" select="'&lt;/li&gt;'"/>
              </xsl:for-each>
            </xsl:if>
          </xsl:if>
        </xsl:if>
      </xsl:for-each>
      <xsl:if test="d:Set_Toc($myObj)&gt;1">
        <!--Closing list Tag-->
        <xsl:value-of disable-output-escaping="yes" select="'&lt;/list&gt;'"/>
        <!--Closing level1 Tag-->
        <xsl:value-of disable-output-escaping="yes" select="'&lt;/level1&gt;'"/>
      </xsl:if>
      <!--Calling function which resets the counter value for TOC-->
      <xsl:sequence select="d:sink(d:Get_Toc($myObj))"/> <!-- empty -->

      <!--Checking for Table of content-->
      <xsl:for-each select="$documentXml//w:body/w:sdt">
        <!--Checking for Table of content-->
        <xsl:if test="w:sdtPr/w:docPartObj/w:docPartGallery/@w:val='Table of Contents'">
          <level1>
            <!--Creating class attribute-->
            <xsl:attribute name="class">
              <xsl:value-of select="'print_toc'"/>
            </xsl:attribute>
            <xsl:sequence select="d:sink(d:CheckTocOccur($myObj))"/> <!-- empty -->
            <xsl:if test="$pagination='automatic'">
              <!--Calling countpageTOC template to check number of pages before TOC-->
              <!--<xsl:call-template name="countpageTOC"/>-->
            </xsl:if>
            <h1>
              <xsl:value-of select="w:sdtContent/w:p/w:r/w:t"/>
            </h1>
            <xsl:value-of disable-output-escaping="yes" select="'&lt;list type=&quot;pl&quot;&gt;'"/>
            <!-- if Automatic TOC -->
            <xsl:if test="w:sdtContent/w:p/w:hyperlink">
              <xsl:for-each select="w:sdtContent/w:p/w:hyperlink">
                <xsl:value-of disable-output-escaping="yes" select="'&lt;li&gt;'"/>
                <xsl:for-each select="w:r/w:rPr/w:rStyle[@w:val='Hyperlink']">
                  <xsl:variable name="club" as="xs:string" select="../../w:t"/>
                  <xsl:value-of select="d:SetTOCMessage($myObj,$club)"/>
                </xsl:for-each>
                <lic>
                  <xsl:value-of select="d:GetTOCMessage($myObj)"/>
                </lic>
                <xsl:value-of select="d:NullMsg($myObj)"/>
                <xsl:for-each select="w:r">
                  <xsl:if test="not(w:rPr/w:rStyle[@w:val='Hyperlink']) and w:t">
                    <lic>
                      <xsl:attribute name="class">pagenum</xsl:attribute>
                      <xsl:text>  </xsl:text>
                      <xsl:value-of select="w:t"/>
                    </lic>
                  </xsl:if>
                </xsl:for-each>
                <!--Closing li Tag-->
                <xsl:value-of disable-output-escaping="yes" select="'&lt;/li&gt;'"/>
              </xsl:for-each>
            </xsl:if>
            <!-- if Manual TOC -->
            <xsl:if test="not(w:sdtContent/w:p/w:hyperlink)">
              <xsl:for-each select="w:sdtContent/w:p">
                <xsl:if test="not(w:pPr/w:pStyle[@w:val='TOCHeading'])">
                  <xsl:value-of disable-output-escaping="yes" select="'&lt;li&gt;'"/>
                  <xsl:for-each select="w:r/w:t">
                    <xsl:variable name="club" as="xs:string" select="."/>
                    <xsl:value-of select="d:SetTOCMessage($myObj,$club)"/>
                  </xsl:for-each>
                  <lic>
                    <xsl:value-of select="d:GetTOCMessage($myObj)"/>
                  </lic>
                  <xsl:value-of select="d:NullMsg($myObj)"/>
                  <!--Closing li Tag-->
                  <xsl:value-of disable-output-escaping="yes" select="'&lt;/li&gt;'"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:if>
            <xsl:if test="(not(following-sibling::node()[1][w:r/w:rPr/w:rStyle[substring(@w:val,1,15)='PageNumberDAISY']]) and ($pagination='custom')) or (not($pagination='custom'))">
              <xsl:value-of disable-output-escaping="yes" select="'&lt;/list&gt;'"/>
            </xsl:if>
          </level1>
        </xsl:if>
      </xsl:for-each>
    
  </xsl:template>

</xsl:stylesheet>
  
