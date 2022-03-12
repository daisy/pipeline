<?xml version="1.0" encoding="UTF-8"?>
<!-- =============================================================== -->
<!-- There are 2 copies of this file:                                -->
<!-- * scripts/dtbook-to-pef/src/main/resources/css/generate-toc.xsl -->
<!-- * scripts/html-to-pef/src/main/resources/css/generate-toc.xsl   -->
<!-- Whenever you update this file, also update the other copies.    -->
<!-- =============================================================== -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                version="2.0"
                exclude-result-prefixes="#all">

  <!-- stylesheet defaults to names used in HTML if no other grammar is detected -->
  
  <xsl:param name="depth" as="xs:integer" select="0"/>
  <xsl:param name="exclude-class" as="xs:string" select="''"/>
  <xsl:param name="document-toc-id" as="xs:string" select="''"/>
  <xsl:param name="volume-toc-id" as="xs:string" select="''"/>
  <xsl:param name="heading-names" select="''"/>
  
  <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>
  <xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
  
  <xsl:variable name="root-base-uri" as="xs:anyURI" select="base-uri(/*)"/>
  
  <xsl:variable name="included-heading-names" as="xs:string*"
                select="if ($heading-names) then tokenize($heading-names,' ') else f:html-headers()"/>
  <xsl:variable name="included-headings" as="element()*"
                select="//*[local-name()=$included-heading-names]
                           [$exclude-class='' or not(@class[tokenize(.,'\s+')[not(.='')]=$exclude-class])]"/>
  
  <xsl:template match="/*" priority="1">
    <xsl:call-template name="pf:next-match-with-generated-ids">
      <xsl:with-param name="prefix" select="'h_'"/>
      <xsl:with-param name="for-elements" select="$included-headings
                                                     [self::*:h1|self::*:h2|self::*:h3|self::*:h4|self::*:h5|self::*:h6]
                                                     [not(@id|@xml:id)]"/>
      <xsl:with-param name="in-use" select="//@id|//@xml:id"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="/*">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="$depth &gt; 0 and ($document-toc-id!='' or $volume-toc-id!='')">
        <xsl:variable name="list" as="element()*">
          <xsl:for-each-group select="$included-headings" group-starting-with="*:h1">
            <xsl:variable name="list-item" as="element()*">
              <xsl:if test="current-group()/self::*:h1">
                <xsl:element name="{f:link-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                  <xsl:call-template name="link-attributes">
                    <xsl:with-param name="header-element" select="current-group()/self::*:h1"/>
                  </xsl:call-template>
                  <xsl:apply-templates select="current-group()/self::*:h1/child::node()"/>
                </xsl:element>
              </xsl:if>
              <xsl:if test="$depth &gt; 1">
                <xsl:variable name="list" as="element()*">
                  <xsl:for-each-group select="current-group()[not(self::*:h1)]" group-starting-with="*:h2">
                    <xsl:variable name="list-item" as="element()*">
                      <xsl:if test="current-group()/self::*:h2">
                        <xsl:element name="{f:link-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                          <xsl:call-template name="link-attributes">
                            <xsl:with-param name="header-element" select="current-group()/self::*:h2"/>
                          </xsl:call-template>
                          <xsl:apply-templates select="current-group()/self::*:h2/child::node()"/>
                        </xsl:element>
                      </xsl:if>
                      <xsl:if test="$depth &gt; 2">
                        <xsl:variable name="list" as="element()*">
                          <xsl:for-each-group select="current-group()[not(self::*:h2)]" group-starting-with="*:h3">
                            <xsl:variable name="list-item" as="element()*">
                              <xsl:if test="current-group()/self::*:h3">
                                <xsl:element name="{f:link-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                  <xsl:call-template name="link-attributes">
                                    <xsl:with-param name="header-element" select="current-group()/self::*:h3"/>
                                  </xsl:call-template>
                                  <xsl:apply-templates select="current-group()/self::*:h3/child::node()"/>
                                </xsl:element>
                              </xsl:if>
                              <xsl:if test="$depth &gt; 3">
                                <xsl:variable name="list" as="element()*">
                                  <xsl:for-each-group select="current-group()[not(self::*:h3)]" group-starting-with="*:h4">
                                    <xsl:variable name="list-item" as="element()*">
                                      <xsl:if test="current-group()/self::*:h4">
                                        <xsl:element name="{f:link-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                          <xsl:call-template name="link-attributes">
                                            <xsl:with-param name="header-element" select="current-group()/self::*:h4"/>
                                          </xsl:call-template>
                                          <xsl:apply-templates select="current-group()/self::*:h4/child::node()"/>
                                        </xsl:element>
                                      </xsl:if>
                                      <xsl:if test="$depth &gt; 4">
                                        <xsl:variable name="list" as="element()*">
                                          <xsl:for-each-group select="current-group()[not(self::*:h4)]" group-starting-with="*:h5">
                                            <xsl:variable name="list-item" as="element()*">
                                              <xsl:if test="current-group()/self::*:h5">
                                                <xsl:element name="{f:link-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                                  <xsl:call-template name="link-attributes">
                                                    <xsl:with-param name="header-element" select="current-group()/self::*:h5"/>
                                                  </xsl:call-template>
                                                  <xsl:apply-templates select="current-group()/self::*:h5/child::node()"/>
                                                </xsl:element>
                                              </xsl:if>
                                              <xsl:if test="$depth &gt; 5">
                                                <xsl:variable name="list" as="element()*">
                                                  <xsl:for-each select="current-group()/self::*:h6">
                                                    <xsl:element name="{f:list-item-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                                      <xsl:call-template name="list-item-attributes"/>
                                                      <xsl:element name="{f:link-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                                        <xsl:call-template name="link-attributes">
                                                          <xsl:with-param name="header-element" select="."/>
                                                        </xsl:call-template>
                                                        <xsl:apply-templates select="node()"/>
                                                      </xsl:element>
                                                    </xsl:element>
                                                  </xsl:for-each>
                                                </xsl:variable>
                                                <xsl:if test="exists($list)">
                                                  <xsl:element name="{f:list-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                                    <xsl:call-template name="list-attributes"/>
                                                    <xsl:sequence select="$list"/>
                                                  </xsl:element>
                                                </xsl:if>
                                              </xsl:if>
                                            </xsl:variable>
                                            <xsl:if test="exists($list-item)">
                                              <xsl:element name="{f:list-item-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                                <xsl:call-template name="list-item-attributes"/>
                                                <xsl:sequence select="$list-item"/>
                                              </xsl:element>
                                            </xsl:if>
                                          </xsl:for-each-group>
                                        </xsl:variable>
                                        <xsl:if test="exists($list)">
                                          <xsl:element name="{f:list-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                            <xsl:call-template name="list-attributes"/>
                                            <xsl:sequence select="$list"/>
                                          </xsl:element>
                                        </xsl:if>
                                      </xsl:if>
                                    </xsl:variable>
                                    <xsl:if test="exists($list-item)">
                                      <xsl:element name="{f:list-item-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                        <xsl:call-template name="list-item-attributes"/>
                                        <xsl:sequence select="$list-item"/>
                                      </xsl:element>
                                    </xsl:if>
                                  </xsl:for-each-group>
                                </xsl:variable>
                                <xsl:if test="exists($list)">
                                  <xsl:element name="{f:list-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                    <xsl:call-template name="list-attributes"/>
                                    <xsl:sequence select="$list"/>
                                  </xsl:element>
                                </xsl:if>
                              </xsl:if>
                            </xsl:variable>
                            <xsl:if test="exists($list-item)">
                              <xsl:element name="{f:list-item-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                                <xsl:call-template name="list-item-attributes"/>
                                <xsl:sequence select="$list-item"/>
                              </xsl:element>
                            </xsl:if>
                          </xsl:for-each-group>
                        </xsl:variable>
                        <xsl:if test="exists($list)">
                          <xsl:element name="{f:list-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                            <xsl:call-template name="list-attributes"/>
                            <xsl:sequence select="$list"/>
                          </xsl:element>
                        </xsl:if>
                      </xsl:if>
                    </xsl:variable>
                    <xsl:if test="exists($list-item)">
                      <xsl:element name="{f:list-item-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                        <xsl:call-template name="list-item-attributes"/>
                        <xsl:sequence select="$list-item"/>
                      </xsl:element>
                    </xsl:if>
                  </xsl:for-each-group>
                </xsl:variable>
                <xsl:if test="exists($list)">
                  <xsl:element name="{f:list-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                    <xsl:call-template name="list-attributes"/>
                    <xsl:sequence select="$list"/>
                  </xsl:element>
                </xsl:if>
              </xsl:if>
            </xsl:variable>
            <xsl:if test="exists($list-item)">
              <xsl:element name="{f:list-item-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
                <xsl:call-template name="list-item-attributes"/>
                <xsl:sequence select="$list-item"/>
              </xsl:element>
            </xsl:if>
          </xsl:for-each-group>
        </xsl:variable>
        <xsl:if test="exists($list)">
          <xsl:if test="$document-toc-id!=''">
            <xsl:element name="{f:list-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
              <xsl:call-template name="list-attributes"/>
              <xsl:attribute name="id" select="$document-toc-id"/>
              <xsl:sequence select="$list"/>
            </xsl:element>
          </xsl:if>
          <xsl:if test="$volume-toc-id!=''">
            <xsl:element name="{f:list-name(namespace-uri(/*))}" namespace="{namespace-uri(/*)}">
              <xsl:call-template name="list-attributes"/>
              <xsl:attribute name="id" select="$volume-toc-id"/>
              <xsl:sequence select="$list"/>
            </xsl:element>
          </xsl:if>
        </xsl:if>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*:h1[not(@id|@xml:id)]|
                       *:h2[not(@id|@xml:id)]|
                       *:h3[not(@id|@xml:id)]|
                       *:h4[not(@id|@xml:id)]|
                       *:h5[not(@id|@xml:id)]|
                       *:h6[not(@id|@xml:id)]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:if test="$depth &gt; 0 and . intersect $included-headings">
        <xsl:variable name="generated-id" as="xs:string">
          <xsl:call-template name="pf:generate-id"/>
        </xsl:variable>
        <xsl:attribute name="xml:id" select="$generated-id"/>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- ========================================= -->
  <!-- Format-specific tag names and attributes: -->
  <!-- ========================================= -->
  
  <xsl:function name="f:html-headers">
    <xsl:sequence select="for $i in 1 to 6 return concat('h',$i)"/>
  </xsl:function>
  
  <xsl:function name="f:list-name">
    <xsl:param name="namespace-uri"/>
    <xsl:choose>
      <xsl:when test="$namespace-uri = 'http://www.daisy.org/z3986/2005/dtbook/'">
        <xsl:value-of select="'list'"/>
      </xsl:when>
      <xsl:when test="$namespace-uri = 'http://www.daisy.org/ns/z3998/authoring/'">
        <xsl:value-of select="'list'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'ol'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:template name="list-attributes">
    <xsl:choose>
      <xsl:when test="namespace-uri(/*) = 'http://www.daisy.org/z3986/2005/dtbook/'">
        <xsl:attribute name="type" select="'ol'"/>
      </xsl:when>
      <xsl:when test="namespace-uri(/*) = 'http://www.daisy.org/ns/z3998/authoring/'">
        <xsl:attribute name="type" select="'ordered'"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:template>
  
  <xsl:function name="f:list-item-name">
    <xsl:param name="namespace-uri"/>
    <xsl:value-of select="'li'"/>
  </xsl:function>
  
  <xsl:template name="list-item-attributes"/>
  
  <xsl:function name="f:link-name">
    <xsl:param name="namespace-uri"/>
    <xsl:value-of select="'a'"/>
  </xsl:function>
  
  <xsl:template name="link-attributes">
    <xsl:param name="header-element" as="element()" required="yes"/>
    <xsl:apply-templates mode="link-attributes" select="$header-element"/>
  </xsl:template>
  
  <xsl:template mode="link-attributes"
                match="*:h1|*:h2|*:h3|*:h4|*:h5|*:h6">
    <xsl:variable name="id" as="xs:string">
      <xsl:choose>
        <xsl:when test="@id|@xml:id">
          <xsl:sequence select="@id|@xml:id"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="pf:generate-id"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:attribute name="href" select="concat('#',encode-for-uri($id))"/>
    <xsl:if test="not(@xml:base)">
      <xsl:variable name="header-base-uri" as="xs:anyURI" select="base-uri(.)"/>
      <xsl:if test="not($header-base-uri=$root-base-uri)">
        <xsl:attribute name="xml:base" select="$header-base-uri"/>
      </xsl:if>
    </xsl:if>
    <xsl:attribute name="title" select="normalize-space(string(.))"/>
  </xsl:template>
  
</xsl:stylesheet>
