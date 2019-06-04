<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <xsl:function name="tr:check-isbn">
    <xsl:param name="isbn"/>
    <xsl:param name="length"/>
    <xsl:sequence select="if ($length = 10) then tr:check-isbn10($isbn) else tr:check-isbn13($isbn)"/>
  </xsl:function>
  
  <xsl:param name="input" as="xs:string"><!-- for testing --></xsl:param>
  
  <!--<xsl:template name="test" as="element(tr:result)">
    <xsl:variable name="check-digit" as="xs:string" select="substring($input, string-length($input))"/>
    <tr:result>
      <xsl:choose>
        <xsl:when test="matches(replace($input, '[^\dX]', ''), '^\d{9}[\dX]$')">
          <xsl:sequence select="tr:check-isbn10($input) = $check-digit"/>
        </xsl:when>
        <xsl:when test="matches(replace($input, '\D', ''), '^\d{13}$')">
          <xsl:sequence select="tr:check-isbn13($input) = $check-digit"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="false()"/>
        </xsl:otherwise>
      </xsl:choose>
    </tr:result>
    <xsl:sequence></xsl:sequence>
  </xsl:template>-->
  
  <xsl:function name="tr:check-isbn10" as="xs:string">
    <xsl:param name="isbn"/>
      <xsl:variable name="isbn_9"/>
      <xsl:variable name="sum" select="(
                                       (for $d in string-to-codepoints(translate($isbn,' -','')) 
                                        return if ($d = 88) then 10 else ($d - 48)) [1] * 10         
                                     + (for $d in string-to-codepoints(translate($isbn,' -','')) 
                                        return if ($d = 88) then 10 else ($d - 48)) [2] * 9         
                                     + (for $d in string-to-codepoints(translate($isbn,' -','')) 
                                        return if ($d = 88) then 10 else ($d - 48)) [3] * 8         
                                     + (for $d in string-to-codepoints(translate($isbn,' -','')) 
                                        return if ($d = 88) then 10 else ($d - 48)) [4] * 7         
                                     + (for $d in string-to-codepoints(translate($isbn,' -','')) 
                                        return if ($d = 88) then 10 else ($d - 48)) [5] * 6         
                                     + (for $d in string-to-codepoints(translate($isbn,' -','')) 
                                        return if ($d = 88) then 10 else ($d - 48)) [6] * 5         
                                     + (for $d in string-to-codepoints(translate($isbn,' -','')) 
                                        return if ($d = 88) then 10 else ($d - 48)) [7] * 4         
                                     + (for $d in string-to-codepoints(translate($isbn,' -','')) 
                                        return if ($d = 88) then 10 else ($d - 48)) [8] * 3         
                                     + (for $d in string-to-codepoints(translate($isbn,' -','')) 
                                        return if ($d = 88) then 10 else ($d - 48)) [9] * 2 
                                     ) 
                                     mod 11"/>
    <xsl:sequence select="string(11 - ($sum - (floor($sum div 11) * 11)))"/>
  </xsl:function>
 
  <xsl:function name="tr:check-isbn13" as="xs:string">
    <xsl:param name="isbn"/>
    <xsl:variable name="isbn_12" select="substring($isbn,1,12)"/>
    <xsl:variable name="sum" select="(sum(
                                         (for $d in string-to-codepoints(translate($isbn_12,' -','')) 
                                          return if ($d = 88) then 10 else ($d - 48)
                                         )[position() mod 2 = 1]
                                      )          
                                      + sum(for $d in 
                                             (
                                              (for $d in string-to-codepoints(translate($isbn_12,' -','')) 
                                               return if ($d = 88) then 10 else ($d - 48)
                                              )[position() mod 2 = 0]
                                             ) 
                                            return 3 * $d) 
                                      ) 
                                      mod 10"/>
     <xsl:sequence select="if ($sum ne 0) then string(10 - ($sum - (floor($sum div 10) * 10))) else '0'"/>
  </xsl:function>
  
</xsl:stylesheet>