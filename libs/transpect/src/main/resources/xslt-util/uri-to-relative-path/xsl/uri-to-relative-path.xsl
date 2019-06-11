<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <!--  * tr:uri-to-relative-path ($base-uri, $rel-uri) 
        * 
        * this function converts an URI to a relative path using another URI as reference.
        *
        * 2015-02-05, Martin Kraetke
        * -->
  
  <xsl:function name="tr:uri-to-relative-path" as="xs:string">
    <xsl:param name="base-uri" as="xs:string"/>     <!-- the URI reference -->
    <xsl:param name="rel-uri" as="xs:string"/>      <!-- the URI to be converted to a relative path -->
    <xsl:variable name="tkn-base-uri" select="tokenize(tr:normalize-uri($base-uri), '/')" as="xs:string+"/>
    <xsl:variable name="tkn-rel-uri" select="tokenize(tr:normalize-uri($rel-uri), '/')" as="xs:string+"/>
    <!-- It seems as if $base-uri needs to end in at least 2 slashes. Otherwise its parent directory will
      be used for comparison. This is due to a flaw in tr:normalize-uri(). -->
<!--<xsl:message select="'BBBBBBBBBB', $base-uri, ' norm:', tr:normalize-uri($base-uri),' tkn:', $tkn-base-uri,' count:', count($tkn-base-uri), ' ::&#xa; ', $rel-uri, ' norm:',tr:normalize-uri($rel-uri),' tkn:', $tkn-rel-uri,' count:', count($tkn-rel-uri)"></xsl:message>-->
    <xsl:variable name="uri-parts-max" select="max((count($tkn-base-uri), count($tkn-rel-uri)))" as="xs:integer"/>
    <!--  *
          * count equal URI parts with same index 
          * -->
    <xsl:variable name="uri-equal-parts" select="for $i in (1 to $uri-parts-max) 
      return $i[$tkn-base-uri[$i] eq $tkn-rel-uri[$i]]" as="xs:integer*"/>
    <xsl:choose>
      <!--  * 
            * both URIs must share the same URI scheme 
            * -->
      <xsl:when test="$uri-equal-parts[1] eq 1">
        <!--  * 
              * drop directories that have equal names but are not physically equal, 
              * e.g. their value should correspond to the index in the sequence 
              * -->
        <xsl:variable name="dir-count-common" select="max(
          for $i in $uri-equal-parts 
          return $i[index-of($uri-equal-parts, $i) eq $i]
          )" as="xs:integer"/>
        <!--  * 
              * difference from common to URI parts to common URI parts
              * -->
        <xsl:variable name="delta-base-uri" select="count($tkn-base-uri) - $dir-count-common" as="xs:integer"/>
        <xsl:variable name="delta-rel-uri" select="count($tkn-rel-uri) - $dir-count-common" as="xs:integer"/>    
        <xsl:variable name="relative-path" select="
          concat(
          (:path:)          string-join(for $i in (1 to $delta-base-uri) return '../', ''),
          (:path parts :)   string-join(for $i in (($dir-count-common + 1) to count($tkn-rel-uri)) return $tkn-rel-uri[$i],'/'),
          if($delta-rel-uri gt 0) then '/' else '', 
          (:filename:)      replace($rel-uri, '^.+/(.+)$', '$1')
          )" as="xs:string"/>
        <xsl:value-of select="$relative-path"/>
      </xsl:when>
      <!--  * 
            * if both URIs share no equal part (e.g. for the reason of different 
            * URI scheme names) then it's not possible to create a relative path.
            * -->
      <xsl:otherwise>
        <xsl:value-of select="$rel-uri"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <!--  *
        * strip filename and normalize slashes
        * -->
  <xsl:function name="tr:normalize-uri" as="xs:string">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:variable name="normalized-uri" select="replace(
      replace($uri, '^(.+)/.+$', '$1'),
      '(\\|/)+', '/'
      )" as="xs:string"/>
    <xsl:value-of select="$normalized-uri"/>
  </xsl:function>
  
</xsl:stylesheet>