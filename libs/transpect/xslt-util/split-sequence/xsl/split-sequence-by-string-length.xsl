<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="xs"
  version="2.0">  
  
  <!--  tr:holzfaellen( node()*, xs:integer, xs:QName )
    
        This function is named after the famous book 'Holzfällen' of 
        the austrian author Thomas Bernhard. The entire book consists only 
        of one paragraph. Ironically, this peculiarity cause some ebook readers
        to crash since they run into memory issues when a paragraph exceeds a 
        certain string-length.
        
        We don't know whether Bernhard would appreciate the special capability 
        of his book. But to circumvent this behaviour, you can use this function 
        to split the markup in several chunks. Alternatively you can just call 
        the function tr:split-sequence-by-string-length() directly.
        
        As arguments, both functions take an arbitrary sequence of nodes, a 
        maximum string-length and a qname (to create the wrapper elements) 
        as arguments.
        
        Here is an example how to call this function from an XSLT template:
        
        <xsl:template match="p[string-length(.) gt $para-max-strlength]">
          <xsl:sequence select="tr:holzfaellen(., 
                                               para-max-strlength, 
                                               QName('http://www.w3.org/1999/xhtml', 'p'))"/>
        </xsl:template>
  -->
  
  <xsl:function name="tr:holzfaellen">
    <xsl:param name="seq"   as="node()*"/>   <!-- sequence of nodes -->
    <xsl:param name="limit" as="xs:integer"/><!-- maximum string-length -->
    <xsl:param name="qname" as="xs:QName"/>  <!-- QName for wrapper element -->
    <xsl:sequence select="tr:split-sequence-by-string-length($seq, $limit, $qname)"/>
  </xsl:function>
  
  <!--  tr:split-sequence-by-string-length( node()*, xs:integer, xs:QName )
    
        Calls tr:tokenize-sequence-by-string-length() to split elements when their
        string length exceeds the limit. To minimize the number of chunks, 
        tr:group-sequence-by-string-length() is applied on the result.      
  -->
  
  <xsl:function name="tr:split-sequence-by-string-length">
    <xsl:param name="seq"   as="node()*"/>   <!-- sequence of nodes -->
    <xsl:param name="limit" as="xs:integer"/><!-- maximum string-length -->
    <xsl:param name="qname" as="xs:QName"/>  <!-- QName for wrapper element -->
    <xsl:sequence select="tr:group-sequence-by-string-length(
                            tr:tokenize-sequence-by-string-length(
                              $seq, $limit, $qname, ()
                            ), $limit
                          )"/>
  </xsl:function>
  
  <!--  tr:group-sequence-by-string-length( node()*, xs:integer ) 
        
        Groups a sequence of nodes until the maximum string length is reached. The 
        nodes are grouped as pairs. The function calls itself recursively until the 
        maximum string length is reached. This ensures that the token lengths 
        are close to the maximum string length. 
  -->
  
  <xsl:function name="tr:group-sequence-by-string-length" as="node()*">
    <xsl:param name="seq" as="node()*"/>     <!-- sequence of nodes -->
    <xsl:param name="limit" as="xs:integer"/><!-- maximum string length  -->
    <!-- group adjacent nodes in pairs and group them -->
    <xsl:variable name="join" as="node()*">
      <xsl:for-each select="$seq[position() mod 2 = 1]">
        <xsl:variable name="pos" select="position() * 2 - 1" as="xs:integer"/>
        <xsl:choose>
          <xsl:when test="sum(for $i in (., $seq[$pos + 1]) 
                              return string-length($i)) 
                          lt $limit">
            <xsl:copy>
              <xsl:sequence select="$seq/@*, (node(), $seq[$pos + 1]/node())"/>
            </xsl:copy>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="(., $seq[$pos + 1])"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="some $j in (for $i in (1 to count($join)) 
                                  return $join[$i]/string-length() + 
                                         $join[$i + 1]/string-length())
                      satisfies $j lt $limit
                      and count($seq) ne count($join)">
        <xsl:sequence select="tr:group-sequence-by-string-length($join, $limit)"/>  
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$join"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <!--  tr:tokenize-sequence-per-string-length( node()*, xs:integer, xs:QName, element()? )
    
        Splits a sequence of nodes into XML tokens by a specified string length limit.
        Each token is wrapped within an element. In contrast to a simple text splitting,
        the markup is preserved too.
  -->
  
  <xsl:function name="tr:tokenize-sequence-by-string-length" as="node()*">
    <xsl:param name="seq" as="node()*"/>       <!-- sequence of nodes -->
    <xsl:param name="limit" as="xs:integer"/>  <!-- maximum string length -->
    <xsl:param name="qname" as="xs:QName"/>    <!-- QName for wrapper element -->
    <xsl:param name="current" as="element()?"/><!-- should be left empty, parameter is set when the function calls itself -->
    <xsl:for-each select="$seq/node()">
      <xsl:choose>
        <!-- if an element is found, call the function again and pass the node as argument -->
        <xsl:when test="string-length(.) gt $limit and self::*">
          <xsl:sequence select="tr:tokenize-sequence-by-string-length(., $limit, $qname, self::*)"/>
        </xsl:when>
        <!-- split the text according to the maximum string length -->
        <xsl:when test="string-length(.) gt $limit and self::text()">
          <xsl:variable name="str" select="." as="xs:string"/>
          <xsl:for-each select="0 to (string-length($str) - 1) idiv $limit">
            <xsl:element name="{$qname}" namespace="{namespace-uri-from-QName($qname)}" exclude-result-prefixes="#all">
              <xsl:copy-of select="$seq/@*"/>
              <xsl:value-of select="substring($str, . * $limit + 1, $limit)"/>
            </xsl:element>
          </xsl:for-each>
        </xsl:when>
        <!-- preserve original xml structure in the final token -->
        <xsl:otherwise>
          <xsl:element name="{$qname}" namespace="{namespace-uri-from-QName($qname)}" exclude-result-prefixes="#all">
            <xsl:copy-of select="$seq/@*"/>
            <xsl:choose>
              <xsl:when test="$current">
                <xsl:element name="{$current/name()}" namespace="{namespace-uri-from-QName($qname)}" exclude-result-prefixes="#all">
                  <xsl:copy-of select="$seq/@*"/>
                  <xsl:sequence select="."/>
                </xsl:element>
              </xsl:when>
              <xsl:otherwise>
                <xsl:sequence select="."/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:function>
  
</xsl:stylesheet>
