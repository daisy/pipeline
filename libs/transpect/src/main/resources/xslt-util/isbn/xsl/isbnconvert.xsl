<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  exclude-result-prefixes="xs"
  version="2.0">

  <xsl:variable name="isbn10-to-isbn13-with-978-hyphen-minus" as="xs:boolean"
    select="true()"/>
  
  <xsl:function name="tr:isbn10-to-isbn13" as="xs:string">
    <xsl:param name="isbn10" as="xs:string"/>
    <xsl:sequence select="tr:isbn10-to-isbn13($isbn10, $isbn10-to-isbn13-with-978-hyphen-minus)"/>
  </xsl:function>
  
  <xsl:function name="tr:isbn10-to-isbn13" as="xs:string">
    <xsl:param name="isbn10" as="xs:string"/>
    <xsl:param name="hyphen-after-978" as="xs:boolean"/>
    <xsl:variable name="normalized-isbn" as="xs:string"
      select="replace($isbn10, '[-\s]+', '')"/>
    <xsl:choose>
      <xsl:when test="string-length($normalized-isbn) = 13 and starts-with($normalized-isbn, '978')">
        <xsl:choose>
          <xsl:when test="$hyphen-after-978">
            <xsl:value-of select="replace($normalized-isbn, '^978', '978-')"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:sequence select="$normalized-isbn"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="string-length($normalized-isbn) = 10">
        <xsl:variable name="isbn13-with-new-check-digit" as="xs:string">
          <xsl:analyze-string select="concat('978', $normalized-isbn)" regex="^(.)(.)(.)(.)(.)(.)(.)(.)(.)(.)(.)(.).$">
            <xsl:matching-substring>
              <xsl:variable name="sum" as="xs:integer"
                select="(xs:integer(regex-group(1)) * 1) +
                        (xs:integer(regex-group(2)) * 3) +
                        (xs:integer(regex-group(3)) * 1) +
                        (xs:integer(regex-group(4)) * 3) +
                        (xs:integer(regex-group(5)) * 1) +
                        (xs:integer(regex-group(6)) * 3) +
                        (xs:integer(regex-group(7)) * 1) +
                        (xs:integer(regex-group(8)) * 3) +
                        (xs:integer(regex-group(9)) * 1) +
                        (xs:integer(regex-group(10)) * 3) +
                        (xs:integer(regex-group(11)) * 1) +
                        (xs:integer(regex-group(12)) * 3)"/>
              <xsl:value-of select="concat(substring(regex-group(0), 0, 13), (10 - ($sum mod 10)) mod 10)"/>
            </xsl:matching-substring>
            <xsl:non-matching-substring/>
          </xsl:analyze-string>
        </xsl:variable>
        <xsl:choose>
          <!-- valid ISBN13? -->
          <xsl:when test="(
                            sum(
                              for $i in(
                                substring($isbn13-with-new-check-digit, 1, 1),
                                substring($isbn13-with-new-check-digit, 3, 1),
                                substring($isbn13-with-new-check-digit, 5, 1),
                                substring($isbn13-with-new-check-digit, 7, 1),
                                substring($isbn13-with-new-check-digit, 9, 1),
                                substring($isbn13-with-new-check-digit, 11, 1),
                                substring($isbn13-with-new-check-digit, 13, 1)
                              ) return xs:integer($i)
                            ) + 
                            sum(
                              for $n in (
                                substring($isbn13-with-new-check-digit, 2, 1),
                                substring($isbn13-with-new-check-digit, 4, 1),
                                substring($isbn13-with-new-check-digit, 6, 1),
                                substring($isbn13-with-new-check-digit, 8, 1),
                                substring($isbn13-with-new-check-digit, 10, 1),
                                substring($isbn13-with-new-check-digit, 12, 1)
                              ) return 3 * xs:integer($n)
                            )
                          ) mod 10 = 0">
            <xsl:choose>
              <xsl:when test="$hyphen-after-978">
                <xsl:value-of select="replace($isbn13-with-new-check-digit, '^978', '978-')"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$isbn13-with-new-check-digit"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>
            <xsl:message select="'isbn10-to-isbn13 warning (internal): no valid isbn13!'"/>
            <xsl:value-of select="$isbn10"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message select="'isbn10-to-isbn13 warning: the given isbn number has not exactly 10 digits!'"/>
        <xsl:value-of select="$isbn10"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
