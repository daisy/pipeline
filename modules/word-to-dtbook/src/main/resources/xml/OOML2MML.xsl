<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mml="http://www.w3.org/1998/Math/MathML"
  xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/" exclude-result-prefixes="m mml dtbook">
  <xsl:output method="xml" encoding="UTF-16" />
  <!-- %% Global Definitions -->
  <!-- Every single unicode character that is recognized by OMML as an operator -->
  <!-- Templates -->
  <xsl:template name="ooml2mml">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:math>
      <xsl:if test="ancestor-or-self::m:oMathPara">
        <xsl:attribute name="display">block</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="*">
        <xsl:with-param name="sOperators" select="$sOperators"/>
        <xsl:with-param name="sMinuses" select="$sMinuses"/>
        <xsl:with-param name="sNumbers" select="$sNumbers"/>
        <xsl:with-param name="sZeros" select="$sZeros"/>
      </xsl:apply-templates>
    </mml:math>
  </xsl:template>
  <xsl:template match="*">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:apply-templates select="*">
      <xsl:with-param name="sOperators" select="$sOperators"/>
      <xsl:with-param name="sMinuses" select="$sMinuses"/>
      <xsl:with-param name="sNumbers" select="$sNumbers"/>
      <xsl:with-param name="sZeros" select="$sZeros"/>
    </xsl:apply-templates>
  </xsl:template>
  <xsl:template match="m:acc">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:mover>
      <!--<xsl:attribute name="dtbook:accent">true</xsl:attribute>-->
      <xsl:apply-templates select="m:e[1]" />
      <mml:mtext>
        <xsl:call-template name="CreateTokenAttributes">
          <xsl:with-param name="scr" select="m:e[1]/*/m:rPr[last()]/m:scr/@m:val" />
          <xsl:with-param name="sty" select="m:e[1]/*/m:rPr[last()]/m:sty/@m:val" />
          <xsl:with-param name="nor" select="m:e[1]/*/m:rPr[last()]/m:nor/@m:val" />
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:call-template>
        <xsl:choose>
          <xsl:when test="not(m:accPr[last()]/m:chr)">
            <xsl:value-of select="'&#x0302;'" />
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="substring(m:accPr/m:chr/@m:val,1,1)" />
          </xsl:otherwise>
        </xsl:choose>
      </mml:mtext>
    </mml:mover>
  </xsl:template>
  <xsl:template match="m:sPre">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:mmultiscripts>
      <mml:mrow>
        <xsl:apply-templates select="m:e[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
      <mml:mprescripts />
      <mml:mrow>
        <xsl:apply-templates select="m:sub[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
      <mml:mrow>
        <xsl:apply-templates select="m:sup[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
    </mml:mmultiscripts>
  </xsl:template>

  <xsl:template match="m:m">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/> 
    <mml:mtable>
      <xsl:call-template name="CreateMathMLMatrixAttr">
        <xsl:with-param name="mcJc" select="m:mPr[last()]/m:mcs/m:mc/m:mcPr[last()]/m:mcJc/@m:val" />
        <xsl:with-param name="sOperators" select="$sOperators"/>
        <xsl:with-param name="sMinuses" select="$sMinuses"/>
        <xsl:with-param name="sNumbers" select="$sNumbers"/>
        <xsl:with-param name="sZeros" select="$sZeros"/>
      </xsl:call-template>
      <xsl:for-each select="m:mr">
        <mml:mtr>
          <xsl:for-each select="m:e">
            <mml:mtd>
              <xsl:apply-templates select="." >
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers"/>
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:apply-templates>
            </mml:mtd>
          </xsl:for-each>
        </mml:mtr>
      </xsl:for-each>
    </mml:mtable>
  </xsl:template>

  <xsl:template name="CreateMathMLMatrixAttr">
    <xsl:param name="mcJc" />
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="sLowerCaseMcjc" select="translate($mcJc, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                                                   'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:choose>
      <xsl:when test="$sLowerCaseMcjc='left'">
        <!--<xsl:attribute name="dtbook:columnalign">left</xsl:attribute>-->
      </xsl:when>
      <xsl:when test="$sLowerCaseMcjc='right'">
        <!--<xsl:attribute name="dtbook:columnalign">right</xsl:attribute>-->
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="m:phant">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="sLowerCaseWidth" select="translate(m:phantPr[last()]/m:width/@m:val, 
                                                             'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                                             'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:variable name="sLowerCaseAsc" select="translate(m:phantPr[last()]/m:asc/@m:val, 
                                                           'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                                           'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:variable name="sLowerCaseDec" select="translate(m:phantPr[last()]/m:dec/@m:val, 
                                                           'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                                           'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:if test="not($sLowerCaseWidth='off' and 
                        $sLowerCaseAsc='off'   and
                        $sLowerCaseDec='off')">
      <mml:mphantom>
        <xsl:apply-templates select="m:e[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mphantom>
    </xsl:if>
  </xsl:template>

  <xsl:template match="m:rad">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="sLowerCaseDegHide" select="translate(m:radPr[last()]/m:degHide/@m:val, 
                                                                'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                                                'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:choose>
      <xsl:when test="$sLowerCaseDegHide='on'">
        <mml:msqrt>
          <xsl:apply-templates select="m:e[1]">
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers"/>
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:apply-templates>
        </mml:msqrt>
      </xsl:when>
      <xsl:otherwise>
        <mml:mroot>
          <mml:mrow>
            <xsl:apply-templates select="m:e[1]">
              <xsl:with-param name="sOperators" select="$sOperators"/>
              <xsl:with-param name="sMinuses" select="$sMinuses"/>
              <xsl:with-param name="sNumbers" select="$sNumbers"/>
              <xsl:with-param name="sZeros" select="$sZeros"/>
            </xsl:apply-templates>
          </mml:mrow>
          <mml:mrow>
            <xsl:apply-templates select="m:deg[1]">
              <xsl:with-param name="sOperators" select="$sOperators"/>
              <xsl:with-param name="sMinuses" select="$sMinuses"/>
              <xsl:with-param name="sNumbers" select="$sNumbers"/>
              <xsl:with-param name="sZeros" select="$sZeros"/>
            </xsl:apply-templates>
          </mml:mrow>
        </mml:mroot>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- %%Template match m:nary 
      Process an n-ary. 
      
      Decides, based on which arguments are supplied, between
      using an mo, msup, msub, or msubsup for the n-ary operator		
  -->
  <xsl:template match="m:nary">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="sLowerCaseSubHide">
      <xsl:choose>
        <xsl:when test="count(m:naryPr[last()]/m:subHide) = 0">
          <xsl:text>off</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="translate(m:naryPr[last()]/m:subHide/@m:val, 
                                    'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                    'abcdefghijklmnopqrstuvwxyz')" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="sLowerCaseSupHide">
      <xsl:choose>
        <xsl:when test="count(m:naryPr[last()]/m:supHide) = 0">
          <xsl:text>off</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="translate(m:naryPr[last()]/m:supHide/@m:val, 
                                    'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                    'abcdefghijklmnopqrstuvwxyz')" />
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="not($sLowerCaseSupHide='off') and 
                          not($sLowerCaseSubHide='off')">
        <mml:mo>
          <xsl:choose>
            <xsl:when test="not(m:naryPr[last()]/m:chr/@m:val) or
                                      m:naryPr[last()]/m:chr/@m:val=''">
              <xsl:text disable-output-escaping="yes">&amp;#x222b;</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="m:naryPr[last()]/m:chr/@m:val" />
            </xsl:otherwise>
          </xsl:choose>
        </mml:mo>
      </xsl:when>
      <xsl:when test="not($sLowerCaseSubHide='off')">
        <xsl:choose>
          <xsl:when test="m:naryPr[last()]/m:limLoc/@m:val='subSup'">
            <mml:msup>
              <mml:mo>
                <xsl:choose>
                  <xsl:when test="not(m:naryPr[last()]/m:chr/@m:val) or
                                                  m:naryPr[last()]/m:chr/@m:val=''">
                    <xsl:text disable-output-escaping="yes">&amp;#x222b;</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="m:naryPr[last()]/m:chr/@m:val" />
                  </xsl:otherwise>
                </xsl:choose>
              </mml:mo>
              <xsl:apply-templates select="m:sup[1]">
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers"/>
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:apply-templates>
            </mml:msup>
          </xsl:when>
          <xsl:otherwise>
            <mml:mover>
              <mml:mo>
                <xsl:choose>
                  <xsl:when test="not(m:naryPr[last()]/m:chr/@m:val) or
                                                  m:naryPr[last()]/m:chr/@m:val=''">
                    <xsl:text disable-output-escaping="yes">&amp;#x222b;</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="m:naryPr[last()]/m:chr/@m:val" />
                  </xsl:otherwise>
                </xsl:choose>
              </mml:mo>
              <xsl:apply-templates select="m:sup[1]">
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers"/>
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:apply-templates>
            </mml:mover>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="not($sLowerCaseSupHide='off')">
        <xsl:choose>
          <xsl:when test="m:naryPr[last()]/m:limLoc/@m:val='subSup'">
            <mml:msub>
              <mml:mo>
                <xsl:choose>
                  <xsl:when test="not(m:naryPr[last()]/m:chr/@m:val) or
                                                  m:naryPr[last()]/m:chr/@m:val=''">
                    <xsl:text disable-output-escaping="yes">&amp;#x222b;</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="m:naryPr[last()]/m:chr/@m:val" />
                  </xsl:otherwise>
                </xsl:choose>
              </mml:mo>
              <xsl:apply-templates select="m:sub[1]">
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers"/>
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:apply-templates>
            </mml:msub>
          </xsl:when>
          <xsl:otherwise>
            <mml:munder>
              <mml:mo>
                <xsl:choose>
                  <xsl:when test="not(m:naryPr[last()]/m:chr/@m:val) or
                                  m:naryPr[last()]/m:chr/@m:val=''">
                    <xsl:text disable-output-escaping="yes">&amp;#x222b;</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="m:naryPr[last()]/m:chr/@m:val" />
                  </xsl:otherwise>
                </xsl:choose>
              </mml:mo>
              <xsl:apply-templates select="m:sub[1]">
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers"/>
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:apply-templates>
            </mml:munder>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="m:naryPr[last()]/m:limLoc/@m:val='subSup'">
            <mml:msubsup>
              <mml:mo>
                <xsl:choose>
                  <xsl:when test="not(m:naryPr[last()]/m:chr/@m:val) or
                                                  m:naryPr[last()]/m:chr/@m:val=''">
                    <xsl:text disable-output-escaping="yes">&amp;#x222b;</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="m:naryPr[last()]/m:chr/@m:val" />
                  </xsl:otherwise>
                </xsl:choose>
              </mml:mo>
              <xsl:apply-templates select="m:sub[1]">
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers"/>
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:apply-templates>
              <xsl:apply-templates select="m:sup[1]">
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers"/>
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:apply-templates>
            </mml:msubsup>
          </xsl:when>
          <xsl:otherwise>
            <mml:munderover>
              <mml:mo>
                <xsl:choose>
                  <xsl:when test="not(m:naryPr[last()]/m:chr/@m:val) or
                                                  m:naryPr[last()]/m:chr/@m:val=''">
                    <xsl:text disable-output-escaping="yes">&amp;#x222b;</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="m:naryPr[last()]/m:chr/@m:val" />
                  </xsl:otherwise>
                </xsl:choose>
              </mml:mo>
              <mml:mrow>
              <xsl:apply-templates select="m:sub[1]">
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers"/>
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:apply-templates>
              </mml:mrow>
              <mml:mrow>
              <xsl:apply-templates select="m:sup[1]">
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers"/>
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:apply-templates>
              </mml:mrow>
            </mml:munderover>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    <mml:mrow>
      <xsl:apply-templates select="m:e[1]">
        <xsl:with-param name="sOperators" select="$sOperators"/>
        <xsl:with-param name="sMinuses" select="$sMinuses"/>
        <xsl:with-param name="sNumbers" select="$sNumbers"/>
        <xsl:with-param name="sZeros" select="$sZeros"/>
      </xsl:apply-templates>
    </mml:mrow>
  </xsl:template>

  <xsl:template match="m:limLow">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:munder>
      <mml:mrow>
        <xsl:apply-templates select="m:e[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
      <mml:mrow>
        <xsl:apply-templates select="m:lim[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
    </mml:munder>
  </xsl:template>

  <xsl:template match="m:limUpp">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:mover>
      <mml:mrow>
        <xsl:apply-templates select="m:e[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
      <mml:mrow>
        <xsl:apply-templates select="m:lim[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
    </mml:mover>
  </xsl:template>

  <xsl:template match="m:sSub">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:msub>
      <mml:mrow>
        <xsl:apply-templates select="m:e[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
      <mml:mrow>
        <xsl:apply-templates select="m:sub[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
    </mml:msub>
  </xsl:template>

  <xsl:template match="m:sSup">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:msup>
      <mml:mrow>
        <xsl:apply-templates select="m:e[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
      <mml:mrow>
        <xsl:apply-templates select="m:sup[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
    </mml:msup>
  </xsl:template>

  <xsl:template match="m:sSubSup">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:msubsup>
      <mml:mrow>
        <xsl:apply-templates select="m:e[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
      <mml:mrow>
        <xsl:apply-templates select="m:sub[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
      <mml:mrow>
        <xsl:apply-templates select="m:sup[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers"/>
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
    </mml:msubsup>
  </xsl:template>

  <xsl:template match="m:groupChr">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="sLowerCaseOpEmu" select="translate(m:groupChrPr[last()]/m:opEmu/@m:val, 
                                                         'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                                         'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:choose>
      <xsl:when test="$sLowerCaseOpEmu='on'">
        <mml:mrow>
          <xsl:call-template name="CreateGroupChr">
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:call-template>
        </mml:mrow>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="CreateGroupChr">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="CreateGroupChr">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="sLowerCasePos" select="translate(m:groupChrPr[last()]/m:pos/@m:val, 
                                                           'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                                           'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:choose>
      <xsl:when test="$sLowerCasePos!='top' or 
                      not(m:groupChrPr[last()]/m:pos/@m:val)   or
                      m:groupChrPr[last()]/m:pos/@m:val=''">
        <mml:munder>
          <xsl:apply-templates select="m:e[1]">
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:apply-templates>
          <mml:mo>
            <xsl:choose>
              <xsl:when test="string-length(m:groupChrPr[last()]/m:chr/@m:val) &gt;= 1">
                <xsl:value-of select="substring(m:groupChrPr[last()]/m:chr/@m:val,1,1)" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:text disable-output-escaping="yes">&amp;#x023DF;</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </mml:mo>
        </mml:munder>
      </xsl:when>
      <xsl:otherwise>
        <mml:mover>
          <xsl:apply-templates select="m:e[1]">
            <xsl:with-param name="sOperators"/>
            <xsl:with-param name="sMinuses"/>
            <xsl:with-param name="sNumbers"/>
            <xsl:with-param name="sZeros"/>
          </xsl:apply-templates>
          <mml:mo>
            <xsl:choose>
              <xsl:when test="string-length(m:groupChrPr[last()]/m:chr/@m:val) &gt;= 1">
                <xsl:value-of select="substring(m:groupChrPr[last()]/m:chr/@m:val,1,1)" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:text disable-output-escaping="yes">&amp;#x023DF;</xsl:text>
              </xsl:otherwise>
            </xsl:choose>
          </mml:mo>
        </mml:mover>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="fName">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:for-each select="m:fName/*">
      <xsl:apply-templates select=".">
        <xsl:with-param name="sOperators" select="$sOperators"/>
        <xsl:with-param name="sMinuses" select="$sMinuses"/>
        <xsl:with-param name="sNumbers" select="$sNumbers" />
        <xsl:with-param name="sZeros" select="$sZeros"/>
      </xsl:apply-templates>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="m:func">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:mrow>
      <mml:mrow>
        <xsl:call-template name="fName">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:call-template>
      </mml:mrow>
      <mml:mo>&#x02061;</mml:mo>
      <xsl:apply-templates select="*[not(local-name() = 'funcPr')][position() &gt; 1]" >
        <xsl:with-param name="sOperators" select="$sOperators"/>
        <xsl:with-param name="sMinuses" select="$sMinuses"/>
        <xsl:with-param name="sNumbers" select="$sNumbers" />
        <xsl:with-param name="sZeros" select="$sZeros"/>
      </xsl:apply-templates>
    </mml:mrow>
  </xsl:template>

  <!-- %%Template: match m:f 
      
      m:f maps directly to mfrac. 
  -->
  <xsl:template match="m:f">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:mfrac>
      <xsl:call-template name="CreateMathMLFracProp">
        <xsl:with-param name="type" select="m:fPr[last()]/m:type/@m:val" />
        <xsl:with-param name="baseJc" select="m:fPr[last()]/m:baseJc/@m:val" />
        <xsl:with-param name="numJc" select="m:fPr[last()]/m:numJc/@m:val" />
        <xsl:with-param name="denJc" select="m:fPr[last()]/m:type/@m:val" />
        <xsl:with-param name="sOperators" select="$sOperators"/>
        <xsl:with-param name="sMinuses" select="$sMinuses"/>
        <xsl:with-param name="sNumbers" select="$sNumbers" />
        <xsl:with-param name="sZeros" select="$sZeros"/>
      </xsl:call-template>

      <mml:mrow>
        <xsl:apply-templates select="m:num[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
      <mml:mrow>
        <xsl:apply-templates select="m:den[1]">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </mml:mrow>
    </mml:mfrac>
  </xsl:template>


  <!-- %%Template: CreateMathMLFracProp 
      
          Make fraction properties based on supplied parameters.
          OMML differentiates between a linear fraction and a skewed
          one. For MathML, we write both as bevelled.
  -->
  <xsl:template name="CreateMathMLFracProp">
    <xsl:param name="type" />
    <xsl:param name="baseJc" />
    <xsl:param name="numJc" />
    <xsl:param name="denJc" />
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="sLowerCaseType" select="translate($type, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:variable name="sLowerCaseNumJc" select="translate($numJc, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:variable name="sLowerCaseDenJc" select="translate($denJc, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')" />

    <xsl:if test="$sLowerCaseType='skw' or $sLowerCaseType='lin'">
        <!--<xsl:attribute name="dtbook:bevelled">true</xsl:attribute>-->
    </xsl:if>
    <xsl:if test="$sLowerCaseType='nobar'">
        <!--<xsl:attribute name="dtbook:linethickness">0pt</xsl:attribute>-->
    </xsl:if>
    <xsl:choose>
        <xsl:when test="$sLowerCaseNumJc='right'">
            <!--<xsl:attribute name="dtbook:numalign">right</xsl:attribute>-->
        </xsl:when>
        <xsl:when test="$sLowerCaseNumJc='left'">
            <!--<xsl:attribute name="dtbook:numalign">left</xsl:attribute>-->
      </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="$sLowerCaseDenJc='right'">
          <!--<xsl:attribute name="dtbook:numalign">right</xsl:attribute>-->
      </xsl:when>
      <xsl:when test="$sLowerCaseDenJc='left'">
          <!--<xsl:attribute name="dtbook:numalign">left</xsl:attribute>-->
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- %%Template: match m:e | m:den | m:num | m:lim | m:sup | m:sub 
      
      These element delinate parts of an equation (like the numerator).  -->
  <xsl:template match="m:e | m:den | m:num | m:lim | m:sup | m:sub">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:choose>

      <!-- If there is no scriptLevel speified, just call through -->
      <xsl:when test="not(m:argPr[last()]/m:scrLvl/@m:val)">
        <xsl:apply-templates select="*">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </xsl:when>

      <!-- Otherwise, create an mrow
           NP 2026 01 27 : mstyle are discouraged in MathML core -->
      <xsl:otherwise>
        <mml:mrow>
          <xsl:apply-templates select="*">
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:apply-templates>
        </mml:mrow>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="m:bar">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="sLowerCasePos" select="translate(m:barPr/m:pos/@m:val, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                                                             'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:choose>
      <xsl:when test="$sLowerCasePos!='bot' or 
                          not($sLowerCasePos)   or
                          $sLowerCasePos=''   ">
        <mml:mover>
          <!--<xsl:attribute name="dtbook:accent">true</xsl:attribute>-->
          <xsl:apply-templates select="m:e[1]">
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:apply-templates>
          <mml:mo>
            <xsl:text disable-output-escaping="yes">&amp;#x000AF;</xsl:text>
          </mml:mo>
        </mml:mover>
      </xsl:when>
      <xsl:otherwise>
        <mml:munder>
          <xsl:apply-templates select="m:e[1]">
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:apply-templates>
          <mml:mo>
            <xsl:text disable-output-escaping="yes">&amp;#x00332;</xsl:text>
          </mml:mo>
        </mml:munder>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- %%Template match m:d

      Process a delimiter. 
  -->
  <xsl:template match="m:d">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:mrow>
    <!-- Open delimiter -->
    <mml:mo>
      <xsl:choose>
        <xsl:when test="m:dPr[1]/m:begChr/@m:val">
          <xsl:value-of select="m:dPr[1]/m:begChr/@m:val" />
        </xsl:when>
        <xsl:otherwise>(</xsl:otherwise>
      </xsl:choose>
    </mml:mo>
      <!-- now write all the children. Put each one into an mrow
          just in case it produces multiple runs, etc -->
      <xsl:for-each select="m:e">
        <mml:mrow>
          <xsl:apply-templates select=".">
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:apply-templates>
        </mml:mrow>
        <!-- Add separator if not last element -->
        <xsl:if test="position() != last()">
          <mml:mo>
            <xsl:choose>
              <xsl:when test="m:dPr[1]/m:sepChr/@m:val">
                <xsl:value-of select="m:dPr[1]/m:sepChr/@m:val" />
              </xsl:when>
              <xsl:otherwise>,</xsl:otherwise>
            </xsl:choose>
          </mml:mo>
        </xsl:if>
      </xsl:for-each>
      <!-- Close delimiter -->
      <mml:mo>
        <xsl:choose>
          <xsl:when test="m:dPr[1]/m:endChr/@m:val">
            <xsl:value-of select="m:dPr[1]/m:endChr/@m:val" />
          </xsl:when>
          <xsl:otherwise>)</xsl:otherwise>
        </xsl:choose>
      </mml:mo>
    </mml:mrow>
  </xsl:template>

  <xsl:template match="m:r">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="sLowerCaseNor" select="translate(child::m:rPr[last()]/m:nor/@m:val, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                                                             'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:choose>
      <xsl:when test="$sLowerCaseNor='on'">
        <mml:mtext>
          <xsl:value-of select=".//m:t" />
        </mml:mtext>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select=".//m:t">
          <xsl:call-template name="ParseMt">
            <xsl:with-param name="sToParse" select="text()" />
            <xsl:with-param name="scr" select="../m:rPr[last()]/m:scr/@m:val" />
            <xsl:with-param name="sty" select="../m:rPr[last()]/m:sty/@m:val" />
            <xsl:with-param name="nor" select="../m:rPr[last()]/m:nor/@m:val" />
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="CreateTokenAttributes">
    <xsl:param name="scr" />
    <xsl:param name="sty" />
    <xsl:param name="nor" />
    <xsl:param name="nCharToPrint" />
    <xsl:param name="sTokenType" />
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="sLowerCaseNor" select="translate($nor, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 
                                                                 'abcdefghijklmnopqrstuvwxyz')" />
    <xsl:choose>
      <xsl:when test="$sLowerCaseNor = 'on'">
        <!--<xsl:attribute name="dtbook:mathvariant">normal</xsl:attribute>-->
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="mathvariant">
          <xsl:choose>
            <!-- numbers don't care -->
            <xsl:when test="$sTokenType='mn'" />
            <xsl:when test="$scr='monospace'">monospace</xsl:when>
            <xsl:when test="$scr='sans-serif' and $sty='i'">sans-serif-italic</xsl:when>
            <xsl:when test="$scr='sans-serif' and $sty='b'">bold-sans-serif</xsl:when>
            <xsl:when test="$scr='sans-serif'">sans-serif</xsl:when>
            <xsl:when test="$scr='fraktur' and $sty='b'">bold-fraktur</xsl:when>
            <xsl:when test="$scr='fraktur'">fraktur</xsl:when>
            <xsl:when test="$scr='double-struck'">double-struck</xsl:when>
            <xsl:when test="$scr='script' and $sty='b'">bold-script</xsl:when>
            <xsl:when test="$scr='script'">script</xsl:when>
            <xsl:when test="($scr='roman' or not($scr) or $scr='') and $sty='b'">bold</xsl:when>
            <xsl:when test="($scr='roman' or not($scr) or $scr='') and $sty='i'">italic</xsl:when>
            <xsl:when test="($scr='roman' or not($scr) or $scr='') and $sty='p'">normal</xsl:when>
            <xsl:otherwise />
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="fontweight">
          <xsl:choose>
            <xsl:when test="$sty='b' or $sty='bi'">bold</xsl:when>
            <xsl:otherwise>normal</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="fontstyle">
          <xsl:choose>
            <xsl:when test="$sty='p' or $sty='b'">normal</xsl:when>
            <xsl:otherwise>italic</xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <!-- Writing of attributes begins here -->
        <xsl:choose>
          <!-- Don't write mathvariant for operators unless they want to be normal -->
          <xsl:when test="$sTokenType='mo' and $mathvariant!='normal'" />

          <!-- A single character within an mi is already italics, don't write -->
          <xsl:when test="$sTokenType='mi' and $nCharToPrint=1 and ($mathvariant='' or $mathvariant='italic')" />

          <xsl:when test="$sTokenType='mi' and $nCharToPrint &gt; 1 and ($mathvariant='' or $mathvariant='italic')">
            <!--<xsl:attribute name="dtbook:mathvariant">
              <xsl:value-of select="'italic'" />
            </xsl:attribute>-->
          </xsl:when>
          <xsl:when test="$mathvariant!='italic' and $mathvariant!=''">
            <!--<xsl:attribute name="dtbook:mathvariant">
              <xsl:value-of select="$mathvariant" />
            </xsl:attribute>-->
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="not($sTokenType='mi' and $nCharToPrint=1) and $fontstyle='italic'">
              <!--<xsl:attribute name="dtbook:fontstyle">italic</xsl:attribute>-->
            </xsl:if>
            <xsl:if test="$fontweight='bold'">
              <!--<xsl:attribute name="dtbook:fontweight">bold</xsl:attribute>-->
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="m:eqArr">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <mml:mtable>
      <!--<xsl:attribute name="dtbook:frame">none</xsl:attribute>
      <xsl:attribute name="dtbook:columnlines">none</xsl:attribute>
      <xsl:attribute name="dtbook:rowlines">none</xsl:attribute>-->
      <xsl:for-each select="m:e">
        <mml:mtr>
          <mml:mtd>
            <mml:maligngroup />
            <xsl:choose>
              <xsl:when test="m:argPr[last()]/m:scrLvl/@m:val!='0' or 
                              not(m:argPr[last()]/m:scrLvl/@m:val)  or 
                              m:argPr[last()]/m:scrLvl/@m:val=''">
                <mml:mrow>
                  <xsl:call-template name="CreateEqArrRow">
                    <xsl:with-param name="align" select="1" />
                    <xsl:with-param name="ndCur" select="*[1]" />
                    <xsl:with-param name="sOperators" select="$sOperators"/>
                    <xsl:with-param name="sMinuses" select="$sMinuses"/>
                    <xsl:with-param name="sNumbers" select="$sNumbers" />
                    <xsl:with-param name="sZeros" select="$sZeros"/>
                  </xsl:call-template>
                </mml:mrow>
              </xsl:when>
              <xsl:otherwise>
                <!-- NP 2026/01/27 : mstyle are discouraged in favor of mrow with style attribute (for now no style considered) -->
                <mml:mrow>
                  <!--<xsl:attribute name="dtbook:scriptlevel">
                    <xsl:value-of select="m:argPr[last()]/m:scrLvl/@m:val" />
                  </xsl:attribute>-->
                  <xsl:call-template name="CreateEqArrRow">
                    <xsl:with-param name="align" select="1" />
                    <xsl:with-param name="ndCur" select="*[1]" />
                    <xsl:with-param name="sOperators" select="$sOperators"/>
                    <xsl:with-param name="sMinuses" select="$sMinuses"/>
                    <xsl:with-param name="sNumbers" select="$sNumbers" />
                    <xsl:with-param name="sZeros" select="$sZeros"/>
                  </xsl:call-template>
                </mml:mrow>
              </xsl:otherwise>
            </xsl:choose>
          </mml:mtd>
        </mml:mtr>
      </xsl:for-each>
    </mml:mtable>
  </xsl:template>

  <xsl:template name="CreateEqArrRow">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:param name="align" />
    <xsl:param name="ndCur" />
    <xsl:variable name="sAllMt">
      <xsl:for-each select="$ndCur/m:t">
        <xsl:value-of select="." />
      </xsl:for-each>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="local-name($ndCur)='r' and
                          namespace-uri($ndCur)='http://schemas.microsoft.com/office/omml/2004/12/core'">

        <xsl:call-template name="ParseEqArrMr">
          <xsl:with-param name="sToParse" select="$sAllMt" />
          <xsl:with-param name="scr" select="../m:rPr[last()]/m:scr/@m:val" />
          <xsl:with-param name="sty" select="../m:rPr[last()]/m:sty/@m:val" />
          <xsl:with-param name="nor" select="../m:rPr[last()]/m:nor/@m:val" />
          <xsl:with-param name="align" select="$align" />
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$ndCur">
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="count($ndCur/following-sibling::*) &gt; 0">
      <xsl:variable name="cAmp">
        <xsl:call-template name="CountAmp">
          <xsl:with-param name="sAllMt" select="$sAllMt" />
          <xsl:with-param name="cAmp" select="0" />
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:call-template name="CreateEqArrRow">
        <xsl:with-param name="align" select="($align+($cAmp mod 2)) mod 2" />
        <xsl:with-param name="ndCur" select="$ndCur/following-sibling::*[1]" />
        <xsl:with-param name="sOperators" select="$sOperators"/>
        <xsl:with-param name="sMinuses" select="$sMinuses"/>
        <xsl:with-param name="sNumbers" select="$sNumbers" />
        <xsl:with-param name="sZeros" select="$sZeros"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="CountAmp">
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:param name="sAllMt" />
    <xsl:param name="cAmp" />
    <xsl:choose>
      <xsl:when test="string-length(substring-after($sAllMt, '&amp;')) &gt; 0 or 
                          substring($sAllMt, string-length($sAllMt))='&#x0026;'">
        <xsl:call-template name="CountAmp">
          <xsl:with-param name="sAllMt" select="substring-after($sAllMt, '&#x0026;')" />
          <xsl:with-param name="cAmp" select="$cAmp+1" />
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$cAmp" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- %%Template: ParseEqArrMr
          
          Similar to ParseMt, but this one has to do more for an equation 
          array. The presence of &amp; in a run that is in an equation array
          indicates alignment 
  -->
  <xsl:template name="ParseEqArrMr">
    <xsl:param name="sToParse" />
    <xsl:param name="sty" />
    <xsl:param name="scr" />
    <xsl:param name="nor" />
    <xsl:param name="align" />
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:if test="string-length($sToParse) &gt; 0">
      <xsl:choose>
        <xsl:when test="substring($sToParse,1,1) = '&amp;'">
          <xsl:choose>
            <xsl:when test="$align='0'">
              <mml:maligngroup />
            </xsl:when>
            <xsl:when test="$align='1'">
              <mml:malignmark>
                <!--<xsl:attribute name="dtbook:edge">left</xsl:attribute>-->
              </mml:malignmark>
            </xsl:when>
          </xsl:choose>
          <xsl:call-template name="ParseEqArrMr">
            <xsl:with-param name="sToParse" select="substring($sToParse,2)" />
            <xsl:with-param name="scr" select="$scr" />
            <xsl:with-param name="sty" select="$sty" />
            <xsl:with-param name="nor" select="$nor" />
            <xsl:with-param name="align">              
              <xsl:choose>
                <xsl:when test="$align='1'">0</xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
              </xsl:choose>
            </xsl:with-param>
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="sRepNumWith0">
            <xsl:call-template name="SReplaceNumWithZero">
              <xsl:with-param name="sToParse" select="$sToParse" />
              <xsl:with-param name="sOperators" select="$sOperators"/>
              <xsl:with-param name="sMinuses" select="$sMinuses"/>
              <xsl:with-param name="sNumbers" select="$sNumbers" />
              <xsl:with-param name="sZeros" select="$sZeros"/>
            </xsl:call-template>
          </xsl:variable>
          <xsl:variable name="sRepOperWith-">
            <xsl:call-template name="SReplaceOperWithMinus">
              <xsl:with-param name="sToParse" select="$sRepNumWith0" />
              <xsl:with-param name="sOperators" select="$sOperators"/>
              <xsl:with-param name="sMinuses" select="$sMinuses"/>
              <xsl:with-param name="sNumbers" select="$sNumbers" />
              <xsl:with-param name="sZeros" select="$sZeros"/>
            </xsl:call-template>
          </xsl:variable>

          <xsl:variable name="iFirstOper" select="string-length($sRepOperWith-) - string-length(substring-after($sRepOperWith-, '-'))" />
          <xsl:variable name="iFirstNum" select="string-length($sRepOperWith-) - string-length(substring-after($sRepOperWith-, '0'))" />
          <xsl:variable name="iFirstAmp" select="string-length($sRepOperWith-) - string-length(substring-after($sRepOperWith-, '&#x0026;'))" />
          <xsl:variable name="fNumAtPos1">
            <xsl:choose>
              <xsl:when test="substring($sRepOperWith-,1,1)='0'">1</xsl:when>
              <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="fOperAtPos1">
            <xsl:choose>
              <xsl:when test="substring($sRepOperWith-,1,1)='-'">1</xsl:when>
              <xsl:otherwise>0</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>

            <!-- Case I: The string begins with neither a number, nor an operator -->
            <xsl:when test="$fNumAtPos1='0' and $fOperAtPos1='0'">
              <xsl:variable name="nCharToPrint">
                <xsl:choose>
                  <xsl:when test="($iFirstOper=$iFirstNum) and 
                                              ($iFirstAmp=$iFirstOper) and
                                              ($iFirstOper=string-length($sToParse)) and
                                              $fNumAtPos1='0' and
                                              $fOperAtPos1='0'">
                    <xsl:value-of select="string-length($sToParse)" />
                  </xsl:when>
                  <xsl:when test="($iFirstOper &lt; $iFirstNum) and 
                                              ($iFirstOper &lt; $iFirstAmp)">
                    <xsl:value-of select="$iFirstOper - 1" />
                  </xsl:when>
                  <xsl:when test="($iFirstNum &lt; $iFirstOper) and 
                                              ($iFirstNum &lt; $iFirstAmp)">
                    <xsl:value-of select="$iFirstNum - 1" />
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="$iFirstAmp - 1" />
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <mml:mi>
                <xsl:call-template name="CreateTokenAttributes">
                  <xsl:with-param name="scr" select="$scr" />
                  <xsl:with-param name="sty" select="$sty" />
                  <xsl:with-param name="nor" select="$nor" />
                  <xsl:with-param name="nCharToPrint" select="$nCharToPrint" />
                  <xsl:with-param name="sTokenType" select="'mi'" />
                  <xsl:with-param name="sOperators" select="$sOperators"/>
                  <xsl:with-param name="sMinuses" select="$sMinuses"/>
                  <xsl:with-param name="sNumbers" select="$sNumbers" />
                  <xsl:with-param name="sZeros" select="$sZeros"/>
                </xsl:call-template>
                <xsl:value-of select="substring($sToParse,1,$nCharToPrint)" />
              </mml:mi>
              <xsl:call-template name="ParseEqArrMr">
                <xsl:with-param name="sToParse" select="substring($sToParse, $nCharToPrint+1)" />
                <xsl:with-param name="scr" select="$scr" />
                <xsl:with-param name="sty" select="$sty" />
                <xsl:with-param name="nor" select="$nor" />
                <xsl:with-param name="align" select="$align" />
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers" />
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:call-template>
            </xsl:when>

            <!-- Case II: There is an operator at position 1 -->
            <xsl:when test="$fOperAtPos1='1'">
              <mml:mo>
                <xsl:call-template name="CreateTokenAttributes">
                  <xsl:with-param name="scr" />
                  <xsl:with-param name="sty" />
                  <xsl:with-param name="nor" select="$nor" />
                  <xsl:with-param name="sTokenType" select="'mo'" />
                  <xsl:with-param name="sOperators" select="$sOperators"/>
                  <xsl:with-param name="sMinuses" select="$sMinuses"/>
                  <xsl:with-param name="sNumbers" select="$sNumbers" />
                  <xsl:with-param name="sZeros" select="$sZeros"/>
                </xsl:call-template>
                <xsl:value-of select="substring($sToParse,1,1)" />
              </mml:mo>
              <xsl:call-template name="ParseEqArrMr">
                <xsl:with-param name="sToParse" select="substring($sToParse, 2)" />
                <xsl:with-param name="scr" select="$scr" />
                <xsl:with-param name="sty" select="$sty" />
                <xsl:with-param name="nor" select="$nor" />
                <xsl:with-param name="align" select="$align" />
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers" />
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:call-template>
            </xsl:when>

            <!-- Case III: There is a number at position 1 -->
            <xsl:otherwise>
              <xsl:variable name="sConsecNum">
                <xsl:call-template name="SNumStart">
                  <xsl:with-param name="sToParse" select="$sToParse" />
                  <xsl:with-param name="sPattern" select="$sRepNumWith0" />
                  <xsl:with-param name="sOperators" select="$sOperators"/>
                  <xsl:with-param name="sMinuses" select="$sMinuses"/>
                  <xsl:with-param name="sNumbers" select="$sNumbers" />
                  <xsl:with-param name="sZeros" select="$sZeros"/>
                </xsl:call-template>
              </xsl:variable>
              <mml:mn>
                <xsl:call-template name="CreateTokenAttributes">
                  <xsl:with-param name="scr" />
                  <xsl:with-param name="sty" />
                  <xsl:with-param name="nor" select="$nor" />
                  <xsl:with-param name="sTokenType" select="'mn'" />
                  <xsl:with-param name="sOperators" select="$sOperators"/>
                  <xsl:with-param name="sMinuses" select="$sMinuses"/>
                  <xsl:with-param name="sNumbers" select="$sNumbers" />
                  <xsl:with-param name="sZeros" select="$sZeros"/>
                </xsl:call-template>
                <xsl:value-of select="$sConsecNum" />
              </mml:mn>
              <xsl:call-template name="ParseEqArrMr">
                <xsl:with-param name="sToParse" select="substring-after($sToParse, $sConsecNum)" />
                <xsl:with-param name="scr" select="$scr" />
                <xsl:with-param name="sty" select="$sty" />
                <xsl:with-param name="nor" select="$nor" />
                <xsl:with-param name="align" select="$align" />
                <xsl:with-param name="sOperators" select="$sOperators"/>
                <xsl:with-param name="sMinuses" select="$sMinuses"/>
                <xsl:with-param name="sNumbers" select="$sNumbers" />
                <xsl:with-param name="sZeros" select="$sZeros"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- %%Template: ParseMt

          Produce a run of text. Technically, OMML makes no distinction 
          between numbers, operators, and other characters in a run. For 
          MathML we need to break these into mi, mn, or mo elements. 
          
          See also ParseEqArrMr
  -->
  <xsl:template name="ParseMt">
    <xsl:param name="sToParse" />
    <xsl:param name="sty" />
    <xsl:param name="scr" />
    <xsl:param name="nor" />
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:if test="string-length($sToParse) &gt; 0">
      <xsl:variable name="sRepNumWith0">
        <xsl:call-template name="SReplaceNumWithZero">
          <xsl:with-param name="sToParse" select="$sToParse" />
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="sRepOperWith-">
        <xsl:call-template name="SReplaceOperWithMinus">
          <xsl:with-param name="sToParse" select="$sRepNumWith0" />
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="iFirstOper" select="string-length($sRepOperWith-) - string-length(substring-after($sRepOperWith-, '-'))" />
      <xsl:variable name="iFirstNum" select="string-length($sRepOperWith-) - string-length(substring-after($sRepOperWith-, '0'))" />
      <xsl:variable name="fNumAtPos1">
        <xsl:choose>
          <xsl:when test="substring($sRepOperWith-,1,1)='0'">1</xsl:when>
          <xsl:otherwise>0</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="fOperAtPos1">
        <xsl:choose>
          <xsl:when test="substring($sRepOperWith-,1,1)='-'">1</xsl:when>
          <xsl:otherwise>0</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:choose>

        <!-- Case I: The string begins with neither a number, nor an operator -->
        <xsl:when test="$fOperAtPos1='0' and $fNumAtPos1='0'">
          <xsl:variable name="nCharToPrint">
            <xsl:choose>
              <xsl:when test="($iFirstOper=$iFirstNum) and 
                                          ($iFirstOper=string-length($sToParse)) and
                                          (substring($sRepOperWith-, string-length($sRepOperWith-))!='0') and 
                                          (substring($sRepOperWith-, string-length($sRepOperWith-))!='-')">
                <xsl:value-of select="string-length($sToParse)" />
              </xsl:when>
              <xsl:when test="$iFirstOper &lt; $iFirstNum">
                <xsl:value-of select="$iFirstOper - 1" />
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$iFirstNum - 1" />
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <mml:mi>
            <xsl:call-template name="CreateTokenAttributes">
              <xsl:with-param name="scr" select="$scr" />
              <xsl:with-param name="sty" select="$sty" />
              <xsl:with-param name="nor" select="$nor" />
              <xsl:with-param name="nCharToPrint" select="$nCharToPrint" />
              <xsl:with-param name="sTokenType" select="'mi'" />
              <xsl:with-param name="sOperators" select="$sOperators"/>
              <xsl:with-param name="sMinuses" select="$sMinuses"/>
              <xsl:with-param name="sNumbers" select="$sNumbers" />
              <xsl:with-param name="sZeros" select="$sZeros"/>
            </xsl:call-template>
            <xsl:value-of select="substring($sToParse,1,$nCharToPrint)" />
          </mml:mi>
          <xsl:call-template name="ParseMt">
            <xsl:with-param name="sToParse" select="substring($sToParse, $nCharToPrint+1)" />
            <xsl:with-param name="scr" select="$scr" />
            <xsl:with-param name="sty" select="$sty" />
            <xsl:with-param name="nor" select="$nor" />
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:call-template>
        </xsl:when>

        <!-- Case II: There is an operator at position 1 -->
        <xsl:when test="$fOperAtPos1='1'">
          <mml:mo>
            <xsl:call-template name="CreateTokenAttributes">
              <xsl:with-param name="scr" />
              <xsl:with-param name="sty" />
              <xsl:with-param name="nor" select="$nor" />
              <xsl:with-param name="sTokenType" select="'mo'" />
              <xsl:with-param name="sOperators" select="$sOperators"/>
              <xsl:with-param name="sMinuses" select="$sMinuses"/>
              <xsl:with-param name="sNumbers" select="$sNumbers" />
              <xsl:with-param name="sZeros" select="$sZeros"/>
            </xsl:call-template>
            <xsl:value-of select="substring($sToParse,1,1)" />
          </mml:mo>
          <xsl:call-template name="ParseMt">
            <xsl:with-param name="sToParse" select="substring($sToParse, 2)" />
            <xsl:with-param name="scr" select="$scr" />
            <xsl:with-param name="sty" select="$sty" />
            <xsl:with-param name="nor" select="$nor" />
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:call-template>
        </xsl:when>

        <!-- Case III: There is a number at position 1 -->
        <xsl:otherwise>
          <xsl:variable name="sConsecNum">
            <xsl:call-template name="SNumStart">
              <xsl:with-param name="sToParse" select="$sToParse" />
              <xsl:with-param name="sPattern" select="$sRepNumWith0" />
              <xsl:with-param name="sOperators" select="$sOperators"/>
              <xsl:with-param name="sMinuses" select="$sMinuses"/>
              <xsl:with-param name="sNumbers" select="$sNumbers" />
              <xsl:with-param name="sZeros" select="$sZeros"/>
            </xsl:call-template>
          </xsl:variable>
          <mml:mn>
            <xsl:call-template name="CreateTokenAttributes">
              <xsl:with-param name="scr" select="$scr" />
              <xsl:with-param name="sty" select="'p'" />
              <xsl:with-param name="nor" select="$nor" />
              <xsl:with-param name="sTokenType" select="'mn'" />
              <xsl:with-param name="sOperators" select="$sOperators"/>
              <xsl:with-param name="sMinuses" select="$sMinuses"/>
              <xsl:with-param name="sNumbers" select="$sNumbers" />
              <xsl:with-param name="sZeros" select="$sZeros"/>
            </xsl:call-template>
            <xsl:value-of select="$sConsecNum" />
          </mml:mn>
          <xsl:call-template name="ParseMt">
            <xsl:with-param name="sToParse" select="substring-after($sToParse, $sConsecNum)" />
            <xsl:with-param name="scr" select="$scr" />
            <xsl:with-param name="sty" select="$sty" />
            <xsl:with-param name="nor" select="$nor" />
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!-- %%Template: SNumStart 
  
      Return the longest substring of sToParse starting from the 
      start of sToParse that is a number. In addition, it takes the
      pattern string, which is sToParse with all of its numbers 
      replaced with a 0. sPattern should be the same length 
      as sToParse		
  -->
  <xsl:template name="SNumStart">
    <xsl:param name="sToParse" select="''" />
    <xsl:param name="sPattern" select="'$sToParse'"/>
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <!-- if we don't get anything, take the string itself -->

    <xsl:choose>
      <!-- the pattern says this is a number, recurse with the rest -->
      <xsl:when test="substring($sPattern, 1, 1) = '0'">
        <xsl:call-template name="SNumStart">
          <xsl:with-param name="sToParse" select="$sToParse" />
          <xsl:with-param name="sPattern" select="substring($sPattern, 2)" />
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:call-template>
      </xsl:when>

      <!-- the pattern says we've run out of numbers. Take as many
              characters from sToParse as we shaved off sPattern -->
      <xsl:otherwise>
        <xsl:value-of select="substring($sToParse, 1, string-length($sToParse) - string-length($sPattern))" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- %%Template SRepeatCharAcc
  
          The core of SRepeatChar with an accumulator. The current
          string is in param $acc, and we will double and recurse,
          if we're less than half of the required length or else just 
          add the right amount of characters to the accumulator and
          return
  -->
  <xsl:template name="SRepeatCharAcc">
    <xsl:param name="cchRequired" select="1" />
    <xsl:param name="ch" select="'-'" />
    <xsl:param name="acc" select="$ch" />
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:variable name="cchAcc" select="string-length($acc)" />
    <xsl:choose>
      <xsl:when test="(2 * $cchAcc) &lt; $cchRequired">
        <xsl:call-template name="SRepeatCharAcc">
          <xsl:with-param name="cchRequired" select="$cchRequired" />
          <xsl:with-param name="ch" select="$ch" />
          <xsl:with-param name="acc" select="concat($acc, $acc)" />
          <xsl:with-param name="sOperators" select="$sOperators"/>
          <xsl:with-param name="sMinuses" select="$sMinuses"/>
          <xsl:with-param name="sNumbers" select="$sNumbers" />
          <xsl:with-param name="sZeros" select="$sZeros"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="concat($acc, substring($acc, 1, $cchRequired - $cchAcc))" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- %%Template SRepeatChar
  
          Generates a string nchRequired long by repeating the given character ch
  -->
  <xsl:template name="SRepeatChar">
    <xsl:param name="cchRequired" select="1" />
    <xsl:param name="ch" select="'-'" />
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:call-template name="SRepeatCharAcc">
      <xsl:with-param name="cchRequired" select="$cchRequired" />
      <xsl:with-param name="ch" select="$ch" />
      <xsl:with-param name="acc" select="$ch" />
      <xsl:with-param name="sOperators" select="$sOperators"/>
      <xsl:with-param name="sMinuses" select="$sMinuses"/>
      <xsl:with-param name="sNumbers" select="$sNumbers" />
      <xsl:with-param name="sZeros" select="$sZeros"/>
    </xsl:call-template>
  </xsl:template>

  <!-- %%Template SReplaceOperWithMinus
  
      Go through the given string and replace every instance
      of an operator with a minus '-'. This helps quickly identify
      the first instance of an operator.  
  -->
  <xsl:template name="SReplaceOperWithMinus">
    <xsl:param name="sToParse" select="''" />
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:value-of select="translate($sToParse, $sOperators, $sMinuses)" />
  </xsl:template>
  
  <xsl:template name="SReplace">
    <xsl:param name="sInput" />
    <xsl:param name="sOrig" />
    <xsl:param name="sReplacement" />
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <xsl:choose>
      <xsl:when test="not(contains($sInput, $sOrig))">
        <xsl:value-of select="$sInput" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="sBefore" select="substring-before($sInput, $sOrig)" />
        <xsl:variable name="sAfter" select="substring-after($sInput, $sOrig)" />
        <xsl:variable name="sAfterProcessed">
          <xsl:call-template name="SReplace">
            <xsl:with-param name="sInput" select="$sAfter" />
            <xsl:with-param name="sOrig" select="$sOrig" />
            <xsl:with-param name="sReplacement" select="$sReplacement" />
            <xsl:with-param name="sOperators" select="$sOperators"/>
            <xsl:with-param name="sMinuses" select="$sMinuses"/>
            <xsl:with-param name="sNumbers" select="$sNumbers" />
            <xsl:with-param name="sZeros" select="$sZeros"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="concat($sBefore, concat($sReplacement, $sAfterProcessed))" />
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- %%Template SReplaceNumWithZero
  
      Go through the given string and replace every instance
      of an number with a zero '0'. This helps quickly identify
      the first occurence of a number. 
      
      Considers the '.' and ',' part of a number iff they are sandwiched 
      between two other numbers. 0.3 will be recognized as a number,
      x.3 will not be. Since these characters can also be an operator, this 
      should be called before SReplaceOperWithMinus.
  -->
  <xsl:template name="SReplaceNumWithZero">
    <xsl:param name="sToParse" select="''" />
    <xsl:param name="sOperators"/>
    <xsl:param name="sMinuses"/>
    <xsl:param name="sNumbers"/>
    <xsl:param name="sZeros"/>
    <!-- First do a simple replace. Numbers will all be come 0's.
          After this point, the pattern involving the . or , that 
          we are looking for will become 0.0 or 0,0 -->
    <xsl:variable name="sSimpleReplace" select="translate($sToParse, $sNumbers, $sZeros)" />

    <!-- And then, replace 0.0 with just 000. This means that the . will 
          become part of the number -->
    <xsl:variable name="sReplacePeriod">
      <xsl:call-template name="SReplace">
        <xsl:with-param name="sInput" select="$sSimpleReplace"/>
        <xsl:with-param name="sOrig" select="'0.0'"/>
        <xsl:with-param name="sReplacement" select="'000'"/>
        <xsl:with-param name="sOperators" select="$sOperators"/>
        <xsl:with-param name="sMinuses" select="$sMinuses"/>
        <xsl:with-param name="sNumbers" select="$sNumbers" />
        <xsl:with-param name="sZeros" select="$sZeros"/>
      </xsl:call-template>
    </xsl:variable>

    <!-- And then, replace 0,0 with just 000. This means that the , will 
          become part of the number -->
    <xsl:call-template name="SReplace">
      <xsl:with-param name="sInput" select="$sReplacePeriod"/>
      <xsl:with-param name="sOrig" select="'0,0'"/>
      <xsl:with-param name="sReplacement" select="'000'"/>
      <xsl:with-param name="sOperators" select="$sOperators"/>
      <xsl:with-param name="sMinuses" select="$sMinuses"/>
      <xsl:with-param name="sNumbers" select="$sNumbers" />
      <xsl:with-param name="sZeros" select="$sZeros"/>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>
