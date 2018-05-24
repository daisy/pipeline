<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">
  
  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>
  
  <!-- XSweet: picks up URI substrings and renders them as (HTML) anchors with (purported or matched) links -->
<!-- Input: HTML Typescript or relatively clean HTML or XML. -->
<!-- Output: A copy, except that URIs now appear as live links (`a` elements). -->
  
  <xsl:template match="* | @* | processing-instruction() | comment()">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
  
 
  <xsl:variable name="country-codes">ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bl|bm|bn|bo|bq|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cu|cv|cw|cx|cy|cz|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mf|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|um|us|uy|uz|va|vc|ve|vg|vi|vn|vu</xsl:variable>

  <!-- tlds includes three-letter domain names -->
  <xsl:variable name="tlds"         as="xs:string" expand-text="true">com|org|net|gov|mil|edu|io|foundation</xsl:variable>
  
<!-- we think something might be an URL if it includes
     something not punctuation, followed by dot, followed by country code or TLD -->
  <xsl:variable name="uri-match" as="xs:string" expand-text="true">\P{{P}}\.({$country-codes}|{$tlds})</xsl:variable>
 
  <xsl:template match="text()">
    <!-- tokenize by splitting around spaces, plus leading punctuation characters  -->
    <xsl:analyze-string select="." regex="\p{{P}}$|\p{{P}}?\s+">
      <xsl:matching-substring>
        <xsl:value-of select="."/>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
          <xsl:choose>
            <!-- skip file URIs -->
            <xsl:when test="matches(.,'file:/')">
              <xsl:value-of select="."/>
            </xsl:when>
            <xsl:when test="matches(.,$uri-match) and (. castable as xs:anyURI)">
              <xsl:variable name="has-protocol" select="matches(.,'^(https?|ftp)://')"/>
              <a href="{'http://'[not($has-protocol)]}{.}">
                <xsl:value-of select="."/>
              </a>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="."/>
            </xsl:otherwise>
          </xsl:choose>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>
 
 
<!-- Old code wasn't working with a shorter TLD list but it could be okay now ... -->
 <!-- <xsl:variable name="urlchar"      as="xs:string" expand-text="true">[\w\-_]</xsl:variable>
  <xsl:variable name="extraURLchar" as="xs:string">[\w\-\$:;/:@&amp;=+,_]</xsl:variable>
  
  <xsl:variable name="domain"    as="xs:string" expand-text="true">({$urlchar}+\.)</xsl:variable>
  
  <xsl:variable name="tail"      as="xs:string" expand-text="true">(/|(\.(xml|html|htm|gif|jpg|jpeg|pdf|png|svg)))?</xsl:variable>
  <xsl:variable name="pathstep"  as="xs:string" expand-text="true">(/{$urlchar}+)</xsl:variable>
  
  <xsl:variable name="url-match" as="xs:string" expand-text="true">((http|ftp|https):/?/?)?{$domain}+{$tlds}{$pathstep}*{$tail}(\?{$extraURLchar}+)?</xsl:variable>
  
      <xsl:template match="text()" mode="regexing">
        
        <xsl:analyze-string select="." regex="{$url-match}">
          <!-\-(https?:)?(\w+\.)?(\w+)\.(\w\w\w)-\->
      <xsl:matching-substring>
        <xsl:variable name="has-protocol" select="matches(.,'^https?://')"/>
        <a href="{'http://'[not($has-protocol)]}{regex-group(0)}">
          <xsl:value-of select="."/>
        </a>
      </xsl:matching-substring>
      <xsl:non-matching-substring>
        <xsl:value-of select="."/>
      </xsl:non-matching-substring>
    </xsl:analyze-string>
  </xsl:template>-->
  
  <!--<xsl:template match="text()[matches(.,'^https?:')][string(.) castable as xs:anyURI][empty(ancestor::a)]">
    <a href="{encode-for-uri(.)}">
      <xsl:value-of select="."/>
    </a>
  </xsl:template>-->
    
</xsl:stylesheet>