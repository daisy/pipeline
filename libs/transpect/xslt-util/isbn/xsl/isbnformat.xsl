<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="xs"
  version="2.0">
   
   <xsl:variable name="ranges" select="document('RangeMessage.xml')"/>
   
   <xsl:key name="ranges-by-country" match="Group[Prefix]" use="Prefix"/>
   <xsl:key name="matching-rule" match="Rule" use="Range"/>
   
   <xsl:function name="tr:format-isbn">
    <xsl:param name="isbn"/>
    <xsl:variable name="length" select="string-length($isbn)" as="xs:integer"/>
    <xsl:variable name="isbn-after-978" select="replace(replace($isbn,'[-\s]+',''), '^978-?', '')"/>
     <xsl:variable name="isbn-body" select="substring($isbn-after-978,1,9)"/>
      <xsl:variable name="country-regex" select="if (matches($isbn-after-978,'^([0-5]|7)')) then '(\d{1})'
                                                else if (matches($isbn-after-978,'^[80-94]')) then '(\d{2})'
                                                else if (matches($isbn-after-978,'^[600-649]|[950-989]')) then '(\d{3})'
                                                else if (matches($isbn-after-978,'^[9900-9989]')) then '(\d{4})'
                                                else if (matches($isbn-after-978,'^[99900-99999]')) then '(\d{5})'
                                                else '' "/>
     
      <xsl:variable name="country" select="replace($isbn-after-978,concat($country-regex,'.*'),'$1')"/>
      <xsl:variable name="possible-ranges" select="key('ranges-by-country',concat('978-',$country),$ranges)/Rules/Rule"/>
      <xsl:variable name="isbn-body-range" select="replace($isbn-body, concat('^', $country, '(\d{7})\d*$'), '$1')"/>
      <xsl:variable name="matching-rule" as="element(Rule)?"
                  select="$possible-ranges[xs:integer($isbn-body-range) &gt; xs:integer((substring-before(Range,'-'))) 
                                           and xs:integer($isbn-body-range) &lt; xs:integer((substring-after(Range,'-')))]"/>
      <xsl:variable name="publisher-regex" select="concat('(\d{',$matching-rule/Length,'})')"/>
     <xsl:choose>
      <xsl:when test="exists($matching-rule)">
        <xsl:variable name="formatted" 
          select="if ($length = 10) 
                  then replace($isbn, concat($country-regex, $publisher-regex, '(\d*)(\d{1}|X)$'), '$1-$2-$3-$4')
                  else 
                    if($length = 13) 
                    then replace($isbn, concat('^(978)?[-]?', $country-regex, $publisher-regex, '(\d*)(\d{1})$'), '$1-$2-$3-$4-$5')
                    else $isbn"/>
        <xsl:sequence select="$formatted"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$isbn"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
   
</xsl:stylesheet>
