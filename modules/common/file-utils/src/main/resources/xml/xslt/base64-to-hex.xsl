<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions">

    <xsl:output encoding="UTF-8"/>

    <xsl:param name="offset" select="'-1'"/>
    <xsl:param name="length" select="'-1'"/>

    <xsl:template match="/*">
        <xsl:variable name="hex" select="f:base64-to-hex(.)"/>
        <xsl:variable name="hex"
            select="if (xs:integer($offset) &gt;= 0 and xs:integer($offset) &gt; xs:integer(@offset)) then replace($hex,concat('^.{', xs:string( (xs:integer($offset) - xs:integer(@offset)) * 2 ),'}'),'') else $hex"/>
        <xsl:variable name="hex"
            select="if (xs:integer($offset) &gt;= 0 and xs:integer($length) &gt;= 0 and xs:integer(@offset) + xs:integer(@length) &gt; xs:integer($offset) + xs:integer($length)) then replace($hex,concat('.{', xs:string( (xs:integer(@offset) + xs:integer(@length) - xs:integer($offset) - xs:integer($length)) * 2 ),'}$'),'') else $hex"/>

        <c:result offset="{if (xs:integer($offset) &gt;= 0) then xs:integer($offset) else xs:integer(@offset)}"
            length="{if (xs:integer($length) &gt;= 0) then xs:integer($length) else xs:integer(@length)}" encoding="utf-8" content-type="text/plain">
            <xsl:value-of select="$hex"/>
        </c:result>

    </xsl:template>

    <!-- This function could potentially be extracted into common-utils/.../numeral-conversion.xsl -->
    <xsl:function name="f:base64-to-hex" as="xs:string">
        <xsl:param name="base64"/>

        <xsl:variable name="binary">
            <xsl:analyze-string select="$base64" regex="(.)">
                <xsl:matching-substring>
                    <xsl:choose xml:space="preserve">
                        <xsl:when test=".='A'"><xsl:value-of select="'000000'"/></xsl:when>
                        <xsl:when test=".='B'"><xsl:value-of select="'000001'"/></xsl:when>
                        <xsl:when test=".='C'"><xsl:value-of select="'000010'"/></xsl:when>
                        <xsl:when test=".='D'"><xsl:value-of select="'000011'"/></xsl:when>
                        <xsl:when test=".='E'"><xsl:value-of select="'000100'"/></xsl:when>
                        <xsl:when test=".='F'"><xsl:value-of select="'000101'"/></xsl:when>
                        <xsl:when test=".='G'"><xsl:value-of select="'000110'"/></xsl:when>
                        <xsl:when test=".='H'"><xsl:value-of select="'000111'"/></xsl:when>
                        <xsl:when test=".='I'"><xsl:value-of select="'001000'"/></xsl:when>
                        <xsl:when test=".='J'"><xsl:value-of select="'001001'"/></xsl:when>
                        <xsl:when test=".='K'"><xsl:value-of select="'001010'"/></xsl:when>
                        <xsl:when test=".='L'"><xsl:value-of select="'001011'"/></xsl:when>
                        <xsl:when test=".='M'"><xsl:value-of select="'001100'"/></xsl:when>
                        <xsl:when test=".='N'"><xsl:value-of select="'001101'"/></xsl:when>
                        <xsl:when test=".='O'"><xsl:value-of select="'001110'"/></xsl:when>
                        <xsl:when test=".='P'"><xsl:value-of select="'001111'"/></xsl:when>
                        <xsl:when test=".='Q'"><xsl:value-of select="'010000'"/></xsl:when>
                        <xsl:when test=".='R'"><xsl:value-of select="'010001'"/></xsl:when>
                        <xsl:when test=".='S'"><xsl:value-of select="'010010'"/></xsl:when>
                        <xsl:when test=".='T'"><xsl:value-of select="'010011'"/></xsl:when>
                        <xsl:when test=".='U'"><xsl:value-of select="'010100'"/></xsl:when>
                        <xsl:when test=".='V'"><xsl:value-of select="'010101'"/></xsl:when>
                        <xsl:when test=".='W'"><xsl:value-of select="'010110'"/></xsl:when>
                        <xsl:when test=".='X'"><xsl:value-of select="'010111'"/></xsl:when>
                        <xsl:when test=".='Y'"><xsl:value-of select="'011000'"/></xsl:when>
                        <xsl:when test=".='Z'"><xsl:value-of select="'011001'"/></xsl:when>
                        <xsl:when test=".='a'"><xsl:value-of select="'011010'"/></xsl:when>
                        <xsl:when test=".='b'"><xsl:value-of select="'011011'"/></xsl:when>
                        <xsl:when test=".='c'"><xsl:value-of select="'011100'"/></xsl:when>
                        <xsl:when test=".='d'"><xsl:value-of select="'011101'"/></xsl:when>
                        <xsl:when test=".='e'"><xsl:value-of select="'011110'"/></xsl:when>
                        <xsl:when test=".='f'"><xsl:value-of select="'011111'"/></xsl:when>
                        <xsl:when test=".='g'"><xsl:value-of select="'100000'"/></xsl:when>
                        <xsl:when test=".='h'"><xsl:value-of select="'100001'"/></xsl:when>
                        <xsl:when test=".='i'"><xsl:value-of select="'100010'"/></xsl:when>
                        <xsl:when test=".='j'"><xsl:value-of select="'100011'"/></xsl:when>
                        <xsl:when test=".='k'"><xsl:value-of select="'100100'"/></xsl:when>
                        <xsl:when test=".='l'"><xsl:value-of select="'100101'"/></xsl:when>
                        <xsl:when test=".='m'"><xsl:value-of select="'100110'"/></xsl:when>
                        <xsl:when test=".='n'"><xsl:value-of select="'100111'"/></xsl:when>
                        <xsl:when test=".='o'"><xsl:value-of select="'101000'"/></xsl:when>
                        <xsl:when test=".='p'"><xsl:value-of select="'101001'"/></xsl:when>
                        <xsl:when test=".='q'"><xsl:value-of select="'101010'"/></xsl:when>
                        <xsl:when test=".='r'"><xsl:value-of select="'101011'"/></xsl:when>
                        <xsl:when test=".='s'"><xsl:value-of select="'101100'"/></xsl:when>
                        <xsl:when test=".='t'"><xsl:value-of select="'101101'"/></xsl:when>
                        <xsl:when test=".='u'"><xsl:value-of select="'101110'"/></xsl:when>
                        <xsl:when test=".='v'"><xsl:value-of select="'101111'"/></xsl:when>
                        <xsl:when test=".='w'"><xsl:value-of select="'110000'"/></xsl:when>
                        <xsl:when test=".='x'"><xsl:value-of select="'110001'"/></xsl:when>
                        <xsl:when test=".='y'"><xsl:value-of select="'110010'"/></xsl:when>
                        <xsl:when test=".='z'"><xsl:value-of select="'110011'"/></xsl:when>
                        <xsl:when test=".='0'"><xsl:value-of select="'110100'"/></xsl:when>
                        <xsl:when test=".='1'"><xsl:value-of select="'110101'"/></xsl:when>
                        <xsl:when test=".='2'"><xsl:value-of select="'110110'"/></xsl:when>
                        <xsl:when test=".='3'"><xsl:value-of select="'110111'"/></xsl:when>
                        <xsl:when test=".='4'"><xsl:value-of select="'111000'"/></xsl:when>
                        <xsl:when test=".='5'"><xsl:value-of select="'111001'"/></xsl:when>
                        <xsl:when test=".='6'"><xsl:value-of select="'111010'"/></xsl:when>
                        <xsl:when test=".='7'"><xsl:value-of select="'111011'"/></xsl:when>
                        <xsl:when test=".='8'"><xsl:value-of select="'111100'"/></xsl:when>
                        <xsl:when test=".='9'"><xsl:value-of select="'111101'"/></xsl:when>
                        <xsl:when test=".='+'"><xsl:value-of select="'111110'"/></xsl:when>
                        <xsl:when test=".='/'"><xsl:value-of select="'111111'"/></xsl:when>
                    </xsl:choose>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:variable>

        <xsl:variable name="hex">
            <xsl:analyze-string select="$binary" regex="(....)">
                <xsl:matching-substring>
                    <xsl:choose xml:space="preserve">
                        <xsl:when test=".='0000'"><xsl:value-of select="'0'"/></xsl:when>
                        <xsl:when test=".='0001'"><xsl:value-of select="'1'"/></xsl:when>
                        <xsl:when test=".='0010'"><xsl:value-of select="'2'"/></xsl:when>
                        <xsl:when test=".='0011'"><xsl:value-of select="'3'"/></xsl:when>
                        <xsl:when test=".='0100'"><xsl:value-of select="'4'"/></xsl:when>
                        <xsl:when test=".='0101'"><xsl:value-of select="'5'"/></xsl:when>
                        <xsl:when test=".='0110'"><xsl:value-of select="'6'"/></xsl:when>
                        <xsl:when test=".='0111'"><xsl:value-of select="'7'"/></xsl:when>
                        <xsl:when test=".='1000'"><xsl:value-of select="'8'"/></xsl:when>
                        <xsl:when test=".='1001'"><xsl:value-of select="'9'"/></xsl:when>
                        <xsl:when test=".='1010'"><xsl:value-of select="'A'"/></xsl:when>
                        <xsl:when test=".='1011'"><xsl:value-of select="'B'"/></xsl:when>
                        <xsl:when test=".='1100'"><xsl:value-of select="'C'"/></xsl:when>
                        <xsl:when test=".='1101'"><xsl:value-of select="'D'"/></xsl:when>
                        <xsl:when test=".='1110'"><xsl:value-of select="'E'"/></xsl:when>
                        <xsl:when test=".='1111'"><xsl:value-of select="'F'"/></xsl:when>
                    </xsl:choose>
                </xsl:matching-substring>
            </xsl:analyze-string>
        </xsl:variable>

        <xsl:value-of select="$hex"/>
    </xsl:function>

</xsl:stylesheet>
