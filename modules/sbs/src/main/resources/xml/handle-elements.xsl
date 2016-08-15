<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
    version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:brl="http://www.daisy.org/z3986/2009/braille/"
    xmlns:my="http://my-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
    exclude-result-prefixes="dtb pf my" extension-element-prefixes="my">

  <xsl:param name="contraction">2</xsl:param>
  <xsl:param name="show_v_forms" select="true()"/>
  <xsl:param name="downshift_ordinals" select="true()"/>
  <xsl:param name="ascii-braille">no</xsl:param>

  <xsl:variable name="GROSS_FUER_BUCHSTABENFOLGE">╦</xsl:variable>
  <xsl:variable name="GROSS_FUER_EINZELBUCHSTABE">╤</xsl:variable>
  <xsl:variable name="KLEINBUCHSTABE">╩</xsl:variable>
  
  <!-- Tables for computer braille -->
  <xsl:variable name="computer_braille_tables" select="'sbs.dis,sbs-special.cti,sbs-code.cti'"/>

  <!--
      Implement the template translate with the following signature
  -->
  <!--
  <xsl:template name="translate" as="text()">
    <xsl:param name="table" as="xs:string" required="yes"/>
    <xsl:param name="text" as="xs:string" required="no"/>
    ...
  </xsl:template>
  -->
  
  <!--
      Implement the template decode with the following signature (used in brl:literal)
  -->
  <!--
  <xsl:template name="decode" as="text()">
    <xsl:param name="text" as="xs:string" required="no"/>
    ...
  </xsl:template>
  -->
  
  <!-- ======= -->
  <!-- SUB/SUP -->
  <!-- ======= -->

  <!-- bei brl:select wird kein Zeichen gesetzt -->
  <xsl:template match="dtb:sup[descendant::brl:select]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Ziffern bekommen das Exponentzeichen und werden tiefgestellt -->
  <xsl:template match="dtb:sup[matches(., '^[-]*\d+$')]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="table">
          <xsl:call-template name="my:get-tables">
            <xsl:with-param name="context" select="'index'"/>
          </xsl:call-template>
        </xsl:with-param>
        <xsl:with-param name="text" select="concat('&#x257E;',string())"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <!-- alles andere bekommt das Zeichen für den oberen Index -->
  <xsl:template match="dtb:sup">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="text" select="'&#x2580;'"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- bei brl:select wird kein Zeichen gesetzt -->
  <xsl:template match="dtb:sub[descendant::brl:select]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Ziffern bekommen das Zeichen für den unteren Index und werden tiefgestellt -->
  <xsl:template match="dtb:sub[matches(., '^[-]*\d+$')]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="table">
          <xsl:call-template name="my:get-tables">
            <xsl:with-param name="context" select="'index'"/>
          </xsl:call-template>
        </xsl:with-param>
        <xsl:with-param name="text" select="concat('&#x2581;',string())"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <!-- alles andere bekommt das Zeichen für den unteren Index -->
  <xsl:template match="dtb:sub">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="text" select="'&#x2581;'"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- ==== -->
  <!-- CODE -->
  <!-- ==== -->

  <xsl:template match="dtb:code[matches(.,'\s')]">
    <!-- Multi-word code -->
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="table" select="'sbs.dis,sbs-special.cti,sbs-code.cti'"/>
        <xsl:with-param name="text" select="concat('&#x2588;',string(),'&#x2589;')"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="dtb:code">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="table" select="'sbs.dis,sbs-special.cti,sbs-code.cti'"/>
        <xsl:with-param name="text" select="concat('&#x257C;',string())"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <!-- ================ -->
  <!-- Computer Braille -->
  <!-- ================ -->

  <xsl:template match="brl:computer">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="table" select="string($computer_braille_tables)"/>
        <xsl:with-param name="text" select="'&#x257C;'"/>
      </xsl:call-template>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:computer/text()" priority="100">
    <xsl:call-template name="translate">
      <xsl:with-param name="table" select="string($computer_braille_tables)"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ======= -->
  <!-- Abbrevs -->
  <!-- ======= -->

  <!-- don't call handle_abbr from a for-each! As it will redefine the context and getTable will fail when calling local-name() -->
  <xsl:template name="handle_abbr">
    <xsl:param name="context" select="local-name()"/>
    <xsl:param name="content" select="."/>
    <xsl:variable name="braille_tables">
      <xsl:call-template name="my:get-tables">
        <xsl:with-param name="context" select="$context"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="temp">
      <xsl:choose>
        <xsl:when test="my:containsDot($content)">
          <!-- drop all whitespace -->
          <xsl:for-each select="tokenize(string($content), '\s+')">
            <xsl:value-of select="."/>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="outerTokens" select="my:tokenizeForAbbr(normalize-space($content))"/>
          <xsl:for-each select="$outerTokens">
            <xsl:choose>
              <xsl:when test="not(my:isLetter(substring(.,1,1)))">
                <xsl:value-of select="."/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="innerTokens" select="my:tokenizeByCase(.)"/>
                <xsl:for-each select="$innerTokens">
                  <xsl:variable name="i" select="position()"/>
                  <xsl:choose>
                    <xsl:when test="my:isUpper(substring(.,1,1))">
                      <xsl:choose>
                        <xsl:when test="string-length(.) &gt; 1"><xsl:value-of select="$GROSS_FUER_BUCHSTABENFOLGE"/></xsl:when>
                        <xsl:otherwise>
                          <!-- string-length(.) == 1 -->
                          <xsl:choose>
                            <xsl:when test="position()=last()"><xsl:value-of select="$GROSS_FUER_BUCHSTABENFOLGE"/></xsl:when>
                            <xsl:otherwise><xsl:value-of select="$GROSS_FUER_EINZELBUCHSTABE"/></xsl:otherwise>
                          </xsl:choose>
                        </xsl:otherwise>
                      </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                      <!-- lowercase letters -->
                      <xsl:if test="position()=1 or (string-length($innerTokens[$i - 1]) &gt; 1 and my:isUpper(substring($innerTokens[$i - 1],1,1)))"><xsl:value-of select="$KLEINBUCHSTABE"/></xsl:if>
                    </xsl:otherwise>
                  </xsl:choose>
                  <xsl:value-of select="."/>
                </xsl:for-each>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
      <!-- FIXME: The following code should be replaced with the according text matcher below
           (lowercase letter after abbr ending with capital) -->
      <!-- If the last letter was a capital and the following letter is small, insert a KLEINBUCHSTABE -->
      <xsl:if test="matches(string($content), '.*\p{Lu}$') and
                    $content/following-sibling::node()[1][self::text()] and
                    matches(string($content/following-sibling::node()[1]), '^\p{Ll}.*')">
        <xsl:value-of select="$KLEINBUCHSTABE"/>
      </xsl:if>
    </xsl:variable>
    <xsl:call-template name="translate">
      <xsl:with-param name="table" select="string($braille_tables)"/>
      <xsl:with-param name="text" select="string($temp)"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="dtb:abbr">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="handle_abbr"/>
    </xsl:copy>
  </xsl:template>

  <!-- ========================= -->
  <!-- STRONG, EM, BRL:EMPH, DFN -->
  <!-- ========================= -->

  <xsl:template match="dtb:strong|dtb:em|brl:emph|dtb:dfn">
    <xsl:variable name="braille_tables">
      <xsl:call-template name="my:get-tables"/>
    </xsl:variable>
    <xsl:variable name="isFirst" as="xs:boolean"
      select="not(some $id in @id satisfies preceding::*[@brl:continuation and index-of(tokenize(@brl:continuation, '\s+'), $id)])"/>
    <xsl:variable name="isLast" as="xs:boolean">
      <xsl:choose>
        <xsl:when test="not($isFirst)">
          <xsl:variable name="id" select="@id"/>
          <xsl:variable name="continuation" select="preceding::*[@brl:continuation and index-of(tokenize(@brl:continuation, '\s+'), $id)]/@brl:continuation"/>
          <xsl:sequence select="not(following::*[@id and index-of(tokenize($continuation, '\s+'), @id)])"/>
        </xsl:when>
        <xsl:when test="some $id in tokenize(@brl:continuation, '\s+') satisfies following::*[@id eq $id]">
          <xsl:sequence select="false()"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="true()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
	<xsl:when test="@brl:render = 'singlequote'">
          <!-- render the emphasis using singlequotes -->
          <xsl:if test="$isFirst">
            <xsl:call-template name="translate">
              <xsl:with-param name="table" select="$braille_tables"/>
              <xsl:with-param name="text" select="'&#8250;'"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:apply-templates/>
          <xsl:if test="$isLast">
            <xsl:call-template name="translate">
              <xsl:with-param name="table" select="$braille_tables"/>
              <xsl:with-param name="text" select="'&#8249;'"/>
            </xsl:call-template>
          </xsl:if>
	</xsl:when>
	<xsl:when test="@brl:render = 'quote'">
          <!-- render the emphasis using quotes -->
          <xsl:if test="$isFirst">
            <xsl:call-template name="translate">
              <xsl:with-param name="table" select="$braille_tables"/>
              <xsl:with-param name="text" select="'&#x00BB;'"/>
            </xsl:call-template>
          </xsl:if>
          <xsl:apply-templates/>
          <xsl:if test="$isLast">
            <xsl:variable name="last_text_node" select="string((.//text())[position()=last()])"/>
            <xsl:choose>
              <xsl:when test="my:isNumberLike(substring($last_text_node, string-length($last_text_node), 1))">
                <xsl:call-template name="translate">
                  <xsl:with-param name="table" select="$braille_tables"/>
                  <xsl:with-param name="text" select="'&#x2039;'"/>
                </xsl:call-template>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="translate">
                  <xsl:with-param name="table" select="$braille_tables"/>
                  <xsl:with-param name="text" select="'&#x00AB;'"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
	</xsl:when>
	<xsl:when test="@brl:render = 'ignore'">
          <!-- ignore the emphasis for braille -->
          <xsl:apply-templates/>
	</xsl:when>
	<xsl:otherwise>
          <!-- render the emphasis using emphasis annotation -->
          <!-- Since we send every (text) node to liblouis separately, it
	       has no means to know when an empasis starts and when it ends.
	       For that reason we do the announcing here in xslt. This also
	       neatly works around a bug where liblouis doesn't correctly
	       announce multi-word emphasis -->
          <xsl:choose>
            <xsl:when test="not($isFirst) or not($isLast) or (count(tokenize(string(.), '(\s|/|-)+')[string(.) ne '']) > 1)">
              <!-- There are multiple words. -->
              <xsl:if test="$isFirst">
		<!-- Insert a multiple word announcement -->
                <xsl:call-template name="translate">
                  <xsl:with-param name="table" select="$braille_tables"/>
                  <xsl:with-param name="text" select="'&#x2560;'"/>
                </xsl:call-template>
              </xsl:if>
              <xsl:apply-templates/>
              <xsl:if test="$isLast">
		<!-- Announce the end of emphasis -->
                <xsl:call-template name="translate">
                  <xsl:with-param name="table" select="$braille_tables"/>
                  <xsl:with-param name="text" select="'&#x2563;'"/>
                </xsl:call-template>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <!-- Its a single word. Insert a single word announcement unless it is within a word -->
              <xsl:choose>
		<!-- emph is at the beginning of the word -->
		<xsl:when
                    test="my:ends-with-non-word(preceding-sibling::text()[1]) and my:starts-with-word(following-sibling::text()[1])">
                  <xsl:call-template name="translate">
                    <xsl:with-param name="table" select="$braille_tables"/>
                    <xsl:with-param name="text" select="'&#x255F;'"/>
                  </xsl:call-template>
                  <xsl:apply-templates/>
                  <xsl:call-template name="translate">
                    <xsl:with-param name="table" select="$braille_tables"/>
                    <xsl:with-param name="text" select="'&#x2561;'"/>
                  </xsl:call-template>
		</xsl:when>
		<!-- emph is at the end of the word -->
		<xsl:when
                    test="my:ends-with-word(preceding-sibling::text()[1]) and my:starts-with-non-word(following-sibling::text()[1])">
                  <xsl:call-template name="translate">
                    <xsl:with-param name="table" select="$braille_tables"/>
                    <xsl:with-param name="text" select="'&#x255E;'"/>
                  </xsl:call-template>
                  <xsl:apply-templates/>
		</xsl:when>
		<!-- emph is inside the word -->
		<xsl:when
                    test="my:ends-with-word(preceding-sibling::text()[1]) and my:starts-with-word(following-sibling::text()[1])">
                  <xsl:call-template name="translate">
                    <xsl:with-param name="table" select="$braille_tables"/>
                    <xsl:with-param name="text" select="'&#x255E;'"/>
                  </xsl:call-template>
                  <xsl:apply-templates/>
                  <xsl:call-template name="translate">
                    <xsl:with-param name="table" select="$braille_tables"/>
                    <xsl:with-param name="text" select="'&#x2561;'"/>
                  </xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
                  <xsl:call-template name="translate">
                    <xsl:with-param name="table" select="$braille_tables"/>
                    <xsl:with-param name="text" select="'&#x255F;'"/>
                  </xsl:call-template>
                  <xsl:apply-templates/>
		</xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <!-- ====== -->
  <!-- IMAGES -->
  <!-- ====== -->

  <xsl:template match="dtb:imggroup">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="dtb:img">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="text" select="string(@alt)"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="dtb:caption">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate"/>
    </xsl:copy>
  </xsl:template>

  <!-- =========== -->
  <!-- Pagenumbers -->
  <!-- =========== -->

  <xsl:template match="dtb:pagenum/text()" priority="100">
    <!-- do not translate pagenums to braille. They will be translated
    in their respective context by the formatter -->
    <xsl:value-of select="."/>
  </xsl:template>

  <!-- Handle extensions that are defined in the Nordic spec i.e. the
       "Requirements for Text and Image Quality and Markup with DTBook
       XML, Version: 2011-2" -->

  <!-- ========== -->
  <!-- Excercises -->
  <!-- ========== -->

  <!-- Excercise answers and boxes -->
  <xsl:template match="dtb:span[@class=('answer','box')]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:text>---</xsl:text>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="dtb:span[@class='answer_1']">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:text>-</xsl:text>
    </xsl:copy>
  </xsl:template>

  <!-- ================= -->
  <!-- Contraction hints -->
  <!-- ================= -->

  <xsl:template match="brl:num[@role='ordinal']">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
	<xsl:when test="$downshift_ordinals = true()">
          <xsl:call-template name="translate">
            <xsl:with-param name="table">
              <xsl:call-template name="my:get-tables">
                <xsl:with-param name="context" select="'num_ordinal'"/>
              </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="text" select="string(translate(.,'.',''))"/>
          </xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
          <xsl:apply-templates/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:num[@role='roman']">
    <xsl:variable name="braille_tables">
      <xsl:call-template name="my:get-tables">
        <xsl:with-param name="context" select="'num_roman'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
	<xsl:when test="my:isUpper(substring(.,1,1))">
          <!-- we assume that if the first char is uppercase the rest is also uppercase -->
          <xsl:call-template name="translate">
            <xsl:with-param name="table" select="$braille_tables"/>
            <xsl:with-param name="text" select="concat('&#x2566;',string())"/>
          </xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
          <!-- presumably the roman number is in lower case -->
          <xsl:call-template name="translate">
            <xsl:with-param name="table" select="$braille_tables"/>
            <xsl:with-param name="text" select="concat('&#x2569;',string())"/>
          </xsl:call-template>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:num[@role='phone']">
    <!-- Replace ' ' and '/' with '.' -->
    <xsl:variable name="clean_number">
      <xsl:for-each select="tokenize(string(.), '(\s|/)+')">
        <xsl:value-of select="."/>
        <xsl:if test="not(position() = last())">.</xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="text" select="string($clean_number)"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:num[@role='fraction']">
    <xsl:variable name="numerator" select="(tokenize(string(.), '(\s|/)+'))[position()=1]"/>
    <xsl:variable name="denominator" select="(tokenize(string(.), '(\s|/)+'))[position()=2]"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="text" select="string($numerator)"/>
      </xsl:call-template>
      <xsl:call-template name="translate">
        <xsl:with-param name="table">
          <xsl:call-template name="my:get-tables">
            <xsl:with-param name="context" select="'denominator'"/>
          </xsl:call-template>
        </xsl:with-param>
        <xsl:with-param name="text" select="string($denominator)"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:num[@role='mixed']">
    <xsl:variable name="braille_tables">
      <xsl:call-template name="my:get-tables"/>
    </xsl:variable>
    <xsl:variable name="number" select="(tokenize(string(.), '(\s|/)+'))[position()=1]"/>
    <xsl:variable name="numerator" select="(tokenize(string(.), '(\s|/)+'))[position()=2]"/>
    <xsl:variable name="denominator" select="(tokenize(string(.), '(\s|/)+'))[position()=3]"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="table" select="$braille_tables"/>
        <xsl:with-param name="text" select="string($number)"/>
      </xsl:call-template>
      <xsl:call-template name="translate">
        <xsl:with-param name="table" select="$braille_tables"/>
        <xsl:with-param name="text" select="string($numerator)"/>
      </xsl:call-template>
      <xsl:call-template name="translate">
        <xsl:with-param name="table">
          <xsl:call-template name="my:get-tables">
            <xsl:with-param name="context" select="'denominator'"/>
          </xsl:call-template>
        </xsl:with-param>
        <xsl:with-param name="text" select="string($denominator)"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:num[@role='measure']">
    <!-- For all number-unit combinations, e.g. 1 kg, 10 km, etc. drop the space -->
    <xsl:variable name="tokens" select="tokenize(normalize-space(string(.)), '\s+')"/>
    <xsl:variable name="number" select="$tokens[1]"/>
    <xsl:variable name="measure" select="$tokens[position()=last()]"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="text" select="string($number)"/>
      </xsl:call-template>
      <xsl:call-template name="handle_abbr">
	<xsl:with-param name="context" select="'abbr'"/>
	<xsl:with-param name="content" as="text()">
          <xsl:value-of select="$measure"/>
	</xsl:with-param>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="brl:num[@role='isbn']">
    <xsl:variable name="braille_tables">
      <xsl:call-template name="my:get-tables"/>
    </xsl:variable>
    <xsl:variable name="lastChar" select="substring(.,string-length(.),1)"/>
    <xsl:variable name="secondToLastChar" select="substring(.,string-length(.)-1,1)"/>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
	<!-- If the isbn number ends in a capital letter then keep the
             dash, mark the letter with &#x2566; and translate the
             letter with abbr -->
	<xsl:when
            test="$secondToLastChar='-' and string(number($lastChar))='NaN' and my:isUpper($lastChar)">
          <xsl:variable name="clean_number">
            <xsl:for-each select="tokenize(substring(.,1,string-length(.)-2), '(\s|-)+')">
              <xsl:value-of select="string(.)"/>
              <xsl:if test="not(position() = last())">.</xsl:if>
            </xsl:for-each>
          </xsl:variable>
          <xsl:call-template name="translate">
            <xsl:with-param name="table" select="$braille_tables"/>
            <xsl:with-param name="text" select="string($clean_number)"/>
          </xsl:call-template>
          <xsl:call-template name="translate">
            <xsl:with-param name="table" select="$braille_tables"/>
            <xsl:with-param name="text" select="$secondToLastChar"/>
          </xsl:call-template>
          <xsl:call-template name="translate">
            <xsl:with-param name="table">
              <xsl:call-template name="my:get-tables">
                <xsl:with-param name="context" select="'abbr'"/>
              </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="text" select="concat('&#x2566;',$lastChar)"/>
          </xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
          <xsl:variable name="clean_number">
            <xsl:for-each select="tokenize(string(.), '(\s|-)+')">
              <xsl:value-of select="string(.)"/>
              <xsl:if test="not(position() = last())">.</xsl:if>
            </xsl:for-each>
          </xsl:variable>
          <xsl:call-template name="translate">
            <xsl:with-param name="table" select="$braille_tables"/>
            <xsl:with-param name="text" select="string($clean_number)"/>
          </xsl:call-template>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:name">
    <xsl:variable name="braille_tables">
      <xsl:call-template name="my:get-tables">
        <xsl:with-param name="context" select="if (matches(., '\p{Ll}&#x00AD;?\p{Lu}'))
                                               then 'name_capitalized'
                                               else local-name()"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="table" select="$braille_tables"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="brl:place">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:v-form">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:choose>
	<xsl:when test="$show_v_forms = true()">
          <xsl:call-template name="translate">
            <xsl:with-param name="text" select="concat(upper-case(substring(string(),1,1)),lower-case(substring(string(),2)))"/>
          </xsl:call-template>
	</xsl:when>
	<xsl:otherwise>
          <xsl:apply-templates/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:separator">
    <!-- ignore -->
  </xsl:template>

  <xsl:template match="brl:homograph">
    <!-- Join all text elements with a special marker and send the
         whole string to liblouis -->
    <xsl:variable name="text">
      <xsl:for-each select="text()">
        <!-- simply ignore the separator elements -->
        <xsl:value-of select="string(.)"/>
        <xsl:if test="not(position() = last())">&#x250A;</xsl:if>
      </xsl:for-each>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="text" select="string($text)"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:date">
    <xsl:variable name="braille_tables">
      <xsl:call-template name="my:get-tables"/>
    </xsl:variable>
    <xsl:variable name="day_braille_tables">
      <xsl:call-template name="my:get-tables">
        <xsl:with-param name="context" select="'date_day'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="month_braille_tables">
      <xsl:call-template name="my:get-tables">
        <xsl:with-param name="context" select="'date_month'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:for-each select="tokenize(string(@value), '-')">
	<!-- reverse the order, so we have day, month, year -->
	<xsl:sort select="position()" order="descending" data-type="number"/>
	<xsl:choose>
          <xsl:when test="position() = 1">
            <xsl:call-template name="translate">
              <xsl:with-param name="table" select="$day_braille_tables"/>
              <xsl:with-param name="text" select="format-number(. cast as xs:integer,'#')"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="position() = 2">
            <xsl:call-template name="translate">
              <xsl:with-param name="table" select="$month_braille_tables"/>
              <xsl:with-param name="text" select="format-number(. cast as xs:integer,'#')"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
	    <xsl:if test="matches(string(.), '\d+')">
              <xsl:call-template name="translate">
                <xsl:with-param name="table" select="$braille_tables"/>
                <xsl:with-param name="text" select="format-number(. cast as xs:integer,'#')"/>
              </xsl:call-template>
	    </xsl:if>
          </xsl:otherwise>
	</xsl:choose>
      </xsl:for-each>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="brl:time">
    <xsl:variable name="time">
      <xsl:for-each select="tokenize(string(@value), ':')">
	<xsl:choose>
	  <!-- Drop the leading zero for the hours and append a dot -->
	  <xsl:when test="not(position() = last())">
	    <xsl:value-of select="format-number(. cast as xs:integer,'#')"/>
	    <xsl:text>.</xsl:text>
	  </xsl:when>
	  <xsl:otherwise>
	    <xsl:value-of select="."/>
	  </xsl:otherwise>
	</xsl:choose>
      </xsl:for-each>
    </xsl:variable>
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="translate">
        <xsl:with-param name="text" select="string($time)"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <!-- ======= -->
  <!-- ACRONYM -->
  <!-- ======= -->

  <xsl:template match="dtb:acronym">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:call-template name="handle_abbr">
	<xsl:with-param name="context" select="'abbr'"/>
      </xsl:call-template>
    </xsl:copy>
  </xsl:template>

  <!-- ================= -->
  <!-- Content selection -->
  <!-- ================= -->
  
  <!--
      FIXME: also copy? or at least warn that style is ignored
  -->
  <xsl:template match="brl:select">
    <xsl:apply-templates select="brl:when-braille"/>
    <!-- Ignore the brl:otherwise element -->
  </xsl:template>

  <!--
      FIXME: also copy? or at least warn that style is ignored
  -->
  <xsl:template match="brl:when-braille">
    <xsl:apply-templates />
    <!-- Ignore the brl:otherwise element -->
  </xsl:template>

  <!--
      FIXME: also copy? or at least warn that style is ignored
  -->
  <xsl:template match="brl:literal">
    <xsl:choose>
      <xsl:when test="@brl:grade[not(.=$contraction)]">
        <!-- ignore -->
      </xsl:when>
      <xsl:when test="$ascii-braille='yes'">
        <!-- input is already ascii -->
        <xsl:value-of select="."/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="decode"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ======================================= -->
  <!-- Text nodes are translated with liblouis -->
  <!-- ======================================= -->

  <!-- ========================================== -->
  <!-- Comma after ordinals, fraction and sub/sup -->
  <!-- ========================================== -->
  <xsl:template
      match="text()[(preceding::* intersect my:preceding-textnode-within-block(.)/(ancestor::brl:num[@role=('ordinal','fraction','mixed')]|ancestor::dtb:sub|ancestor::dtb:sup)) and matches(string(), '^,')]"
      priority="61">
    <xsl:call-template name="translate">
      <xsl:with-param name="text" select="concat('&#x256C;',string())"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ============================================= -->
  <!-- Punctuation after a number and after ordinals -->
  <!-- ============================================= -->
  <xsl:template
    match="text()[my:starts-with-punctuation(string()) and not(preceding::* intersect my:preceding-textnode-within-block(.)/ancestor::*[@brl:render=('quote','singlequote')])
	   and (my:ends-with-number(string(my:preceding-textnode-within-block(.))) or (preceding::* intersect my:preceding-textnode-within-block(.)/(ancestor::brl:num[@role='ordinal']|ancestor::brl:date)))]"
    priority="60">
    <xsl:call-template name="translate">
      <xsl:with-param name="text" select="concat('&#x250B;',string())"/>
    </xsl:call-template>
  </xsl:template>

  <!-- =============================================== -->
  <!-- lowercase letter after abbr ending with capital -->
  <!-- =============================================== -->
  <!-- FIXME: This doesn't work at the moment because it inserts two apostrophes. It works if this
       code is inside the abbr generating code but has the drawback that the apostrophe is generated
       within the abbr element. It would be better to make the code below work and drop the code in
       the abbr handling code -->
  <!-- <xsl:template -->
  <!--   match="text()[matches(string(.), '^\p{Ll}.*') and (preceding::* intersect my:preceding-textnode-within-block(.)/ancestor::dtb:abbr)[matches(string(.), '.*\p{Lu}$')]]" -->
  <!--   priority="61"> -->
  <!--     <xsl:call-template name="translate"> -->
  <!--       <xsl:with-param name="text" select="concat($KLEINBUCHSTABE,string())"/> -->
  <!--     </xsl:call-template> -->
  <!-- </xsl:template> -->

  <!-- ========================================== -->
  <!-- Apostrophe after v-form or after homograph -->
  <!-- ========================================== -->
  <xsl:template
      match="text()[(preceding::* intersect my:preceding-textnode-within-block(.)/(ancestor::brl:v-form|ancestor::brl:homograph)) and matches(string(), '^''')]"
      priority="61">
    <xsl:call-template name="translate">
      <xsl:with-param name="text" select="concat('&#x250A;',string())"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ================================================= -->
  <!-- Single word mixed emphasis, mixed emphasis before-->
  <!-- ================================================= -->
  <xsl:template
      match="text()[my:starts-with-word(string()) and my:ends-with-word(string(my:preceding-textnode-within-block(.)[ancestor::dtb:em]))]"
      priority="60">
    <xsl:call-template name="translate">
      <xsl:with-param name="text" select="concat('&#x250A;',string())"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ================================================ -->
  <!-- Single word mixed emphasis, mixed emphasis after -->
  <!-- ================================================ -->
  <xsl:template
      match="text()[my:ends-with-word(string()) and my:starts-with-word(string(my:following-textnode-within-block(.)[ancestor::dtb:em]))]"
      priority="60">
    <xsl:call-template name="translate">
      <xsl:with-param name="text" select="concat(string(),'&#x250A;')"/>
    </xsl:call-template>
  </xsl:template>
  
  <!-- ====================== -->
  <!-- Single word mixed abbr -->
  <!-- ====================== -->
  <xsl:template
      match="text()[my:starts-with-word(string()) and not(my:starts-with-number(string())) and my:ends-with-word(string(my:preceding-textnode-within-block(.)[ancestor::dtb:abbr]))]"
      priority="60">
    <xsl:call-template name="translate">
      <xsl:with-param name="text" select="concat('&#x250A;',string())"/>
    </xsl:call-template>
  </xsl:template>

  <!-- ============================================================================= -->
  <!-- 'ich' inside text node followed by chars that could be interpreted as numbers -->
  <!-- ============================================================================= -->
  <xsl:template
      match="text()[(matches(string(), '^ich$', 'i') or matches(string(), '\Wich$', 'i')) and matches(string(following::text()[1]), '^[,;:?!)&#x00bb;&#x00ab;]')]"
      priority="61">
    <xsl:call-template name="translate">
      <xsl:with-param name="text" select="concat(string(),'&#x250A;')"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="text()" priority="50">
    <xsl:call-template name="translate"/>
  </xsl:template>

  <!-- Copy all the rest -->
  <xsl:template match="node()|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
