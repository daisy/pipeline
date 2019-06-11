<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="#all"
  version="2.0">
  
  <!-- tr:flat-list-to-tree( element()*, xs:integer, xs:integer, xs:QName, xs:string, xs:string )
       
       this function creates a tree from a flat list of elements
       which provide their list level as attribute value. the wrapper 
       elements to be generated can be passed as QName.

       - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
       | input                        | output                             |
       - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
       | <li class="toc_1">1</li>     | <ol>                               |
       | <li class="toc_1">2</li>     |   <li class="toc_1">1</li>         |
       | <li class="toc_2">2.1</li>   |   <li class="toc_1">2</li>         |
       | <li class="toc_2">2.2</li>   |   <ol>                             |
       | <li class="toc_3">2.2.1</li> |     <li class="toc_2">2.1</li>     |
       |                              |     <li class="toc_2">2.2</li>     |
       |                              |     <ol>                           |
       |                              |       <li class="toc_3">2.2.1</li> |
       |                              |     </ol>                          |
       |                              |   </ol>                            |
       |                              | </ol>                              |
       - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
       
       an example on how to invoke this function can be found in test.xsl
  -->
  
  <xsl:function name="tr:flat-list-to-tree" as="element()*">
    <xsl:param name="list" as="element()*"/>          <!-- flat sequence of elements -->
    <xsl:param name="level" as="xs:integer"/>         <!-- current level, start with 0 when invoking this function -->
    <xsl:param name="max" as="xs:integer"/>           <!-- max level for creating subtrees -->
    <xsl:param name="wrapper-qname" as="xs:QName"/>   <!-- Qualified Name for list wrapper, e.g. QName('http://www.w3.org/1999/xhtml', 'ol') -->
    <xsl:param name="level-att-name" as="xs:string"/> <!-- name of attribute which provides the list level -->
    <xsl:param name="level-att-regex" as="xs:string"/><!-- strip characters from the level attribute (need to be casted to xs:integer in the end) -->
    <xsl:element name="{$wrapper-qname}" 
                 namespace="{namespace-uri-from-QName($wrapper-qname)}" 
                 exclude-result-prefixes="#all">
      <xsl:for-each-group select="$list" 
                          group-adjacent="boolean(self::*[xs:integer(
                                                                     replace(@*[name() eq $level-att-name],
                                                                             $level-att-regex,
                                                                             '')
                                                                     ) = $level])">
        <xsl:choose>
          <xsl:when test="current-grouping-key()">
            <xsl:sequence select="current-group()"/>
          </xsl:when>
          <xsl:when test="$level lt $max">
            <xsl:sequence select="tr:flat-list-to-tree(current-group(), 
                                                       $level + 1, 
                                                       $max, 
                                                       $wrapper-qname, 
                                                       $level-att-name, 
                                                       $level-att-regex)"/>
          </xsl:when>
        </xsl:choose>
      </xsl:for-each-group>
    </xsl:element>
  </xsl:function>
  
</xsl:stylesheet>