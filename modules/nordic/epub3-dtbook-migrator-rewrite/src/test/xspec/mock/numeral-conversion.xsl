<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">
    <xsl:output indent="yes"/>
    
    <xsl:function name="pf:numeric-is-roman" as="xs:boolean">
        <xsl:param name="roman" as="xs:string"/>
        <xsl:value-of select="(for $char in string-to-codepoints($roman) return contains('MDCLXVI',upper-case(codepoints-to-string($char)))) = true()"/>
    </xsl:function>
    
    <xsl:function name="pf:numeric-roman-to-decimal" as="xs:integer">
        <xsl:param name="roman" as="xs:string"/>
        <!-- TODO: throw error for strings containing characters other than MDCLXVI (case insensitive), the seven characters still in use. -->
        <xsl:variable name="hindu-sequence"
            select="for $char in string-to-codepoints($roman) return
                                number(replace(replace(replace(replace(replace(replace(replace(upper-case(codepoints-to-string($char)),'I','1'),'V','5'),'X','10'),'L','50'),'C','100'),'D','500'),'M','1000'))"/>
        <xsl:variable name="hindu-sequence-signed"
            select="for $i in 1 to count($hindu-sequence) return if (subsequence($hindu-sequence,$i+1) &gt; $hindu-sequence[$i]) then -$hindu-sequence[$i] else $hindu-sequence[$i]"/>
        <xsl:value-of select="sum($hindu-sequence-signed)"/>
    </xsl:function>
    
    <xsl:function name="pf:numeric-decimal-to-roman" as="xs:string">
        <xsl:param name="hindu" as="xs:integer"/>
        <!-- TODO: throw error for non-positive integers -->
        <xsl:variable name="hindu-sequence"
            select="reverse(for $num in string-to-codepoints(string($hindu)) return codepoints-to-string($num))"/>
        <xsl:variable name="dec1" select="if ($hindu-sequence[1] = '1') then 'I' else
                                          if ($hindu-sequence[1] = '2') then 'II' else
                                          if ($hindu-sequence[1] = '3') then 'III' else
                                          if ($hindu-sequence[1] = '4') then 'IV' else
                                          if ($hindu-sequence[1] = '5') then 'V' else
                                          if ($hindu-sequence[1] = '6') then 'VI' else
                                          if ($hindu-sequence[1] = '7') then 'VII' else
                                          if ($hindu-sequence[1] = '8') then 'VIII' else
                                          if ($hindu-sequence[1] = '9') then 'IX' else ''"/>
        <xsl:variable name="dec2" select="if ($hindu-sequence[2] = '1') then 'X' else
                                          if ($hindu-sequence[2] = '2') then 'XX' else
                                          if ($hindu-sequence[2] = '3') then 'XXX' else
                                          if ($hindu-sequence[2] = '4') then 'XL' else
                                          if ($hindu-sequence[2] = '5') then 'L' else
                                          if ($hindu-sequence[2] = '6') then 'LX' else
                                          if ($hindu-sequence[2] = '7') then 'LXX' else
                                          if ($hindu-sequence[2] = '8') then 'LXXX' else
                                          if ($hindu-sequence[2] = '9') then 'XC' else ''"/>
        <xsl:variable name="dec3" select="if ($hindu-sequence[3] = '1') then 'C' else
                                          if ($hindu-sequence[3] = '2') then 'CC' else
                                          if ($hindu-sequence[3] = '3') then 'CCC' else
                                          if ($hindu-sequence[3] = '4') then 'CD' else
                                          if ($hindu-sequence[3] = '5') then 'D' else
                                          if ($hindu-sequence[3] = '6') then 'DC' else
                                          if ($hindu-sequence[3] = '7') then 'DCC' else
                                          if ($hindu-sequence[3] = '8') then 'DCCC' else
                                          if ($hindu-sequence[3] = '9') then 'CM' else ''"/>
        <xsl:variable name="dec4" select="string-join(for $i in 1 to xs:integer(concat('0',string-join(reverse(subsequence($hindu-sequence,4)),''))) return 'M','')"/>
        <xsl:value-of select="concat($dec4,$dec3,$dec2,$dec1)"/>
    </xsl:function>
    
    <xsl:function name="pf:numeric-alpha-to-decimal" as="xs:integer">
        <xsl:param name="alpha" as="xs:string"/>
        <xsl:sequence select="string-to-codepoints($alpha)-96"/>
    </xsl:function>
    
    <!--<xsl:template match="/*" name="test">
        <tests>
            <is-roman>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('M')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('m')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('C')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('c')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('D')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('d')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('L')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('l')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('X')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('x')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('V')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('v')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('I')"/></num>
                <num answer="true"><xsl:value-of select="pf:numeric-is-roman('i')"/></num>
                <num answer="false"><xsl:value-of select="pf:numeric-is-roman('a')"/></num>
                <num answer="false"><xsl:value-of select="pf:numeric-is-roman('z')"/></num>
                <num answer="false"><xsl:value-of select="pf:numeric-is-roman('δ')"/></num>
            </is-roman>
            <roman-to-hindu>
                <num answer="1">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('I')"/>
                </num>
                <num answer="5">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('V')"/>
                </num>
                <num answer="10">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('X')"/>
                </num>
                <num answer="50">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('L')"/>
                </num>
                <num answer="100">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('C')"/>
                </num>
                <num answer="500">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('D')"/>
                </num>
                <num answer="1000">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('M')"/>
                </num>
                <num answer="2006">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('MMVI')"/>
                </num>
                <num answer="1944">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('MCMXLIV')"/>
                </num>
                <num answer="4">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('IIII')"/>
                </num>
                <num answer="90">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('LXL')"/>
                </num>
                <num answer="78">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('LXXIIX')"/>
                </num>
                <num answer="1606">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('MCCCCCCVI')"/>
                </num>
                <num answer="1666">
                    <xsl:value-of select="pf:numeric-roman-to-hindu('mdclxvi')"/>
                </num>
            </roman-to-hindu>
            <hindu-to-roman>
                <num answer="I">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(1)"/>
                </num>
                <num answer="V">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(5)"/>
                </num>
                <num answer="X">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(10)"/>
                </num>
                <num answer="L">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(50)"/>
                </num>
                <num answer="C">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(100)"/>
                </num>
                <num answer="D">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(500)"/>
                </num>
                <num answer="M">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(1000)"/>
                </num>
                <num answer="MMVI">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(2006)"/>
                </num>
                <num answer="MCMXLIV">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(1944)"/>
                </num>
                <num answer="IV">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(4)"/>
                </num>
                <num answer="XC">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(90)"/>
                </num>
                <num answer="LXXVIII">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(78)"/>
                </num>
                <num answer="MDCVI">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(1606)"/>
                </num>
                <num answer="MDCLXVI">
                    <xsl:value-of select="pf:numeric-hindu-to-roman(1666)"/>
                </num>
            </hindu-to-roman>
        </tests>
    </xsl:template>-->
    
</xsl:stylesheet>
