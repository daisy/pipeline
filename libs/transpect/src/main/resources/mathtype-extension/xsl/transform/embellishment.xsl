<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY msup 'emb1PRIME|emb2PRIME|emb3PRIME'>
    <!ENTITY mover 'emb1DOT|emb2DOT|emb3DOT|emb4DOT|embTILDE|embHAT|embRARROW|embLARROW|embBARROW|embR1ARROW|embL1ARROW|embOBAR|embFROWN|embSMILE'>
    <!ENTITY munder 'embU_1DOT|embU_2DOT|embU_3DOT|embU_4DOT|embU_RARROW|embU_LARROW|embU_BARROW|embU_R1ARROW|embU_L1ARROW'>
    <!ENTITY munderaccent 'embU_TILDE|embU_BAR$|embU_FROWN|embU_SMILE'>
    <!ENTITY menclose 'embNOT|embX_BARS|embMBAR|embUP_BAR|embDOWN_BAR'>
    <!ENTITY mmultiscripts 'embBPRIME'>
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns="http://www.w3.org/1998/Math/MathML"
    exclude-result-prefixes="xs"
    version="2.0">

  <xsl:template match="char[embell]" priority="2">
    <xsl:call-template name="embell">
      <xsl:with-param name="embelled-before">
        <xsl:next-match/>
      </xsl:with-param>
      <xsl:with-param name="embells" select="embell"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="embell">
    <xsl:param name="embelled-before"/>
    <xsl:param as="element(embell)*" name="embells"/>
    <xsl:variable name="wrapped">
      <xsl:apply-templates mode="embell" select="$embells[1]/embell">
        <xsl:with-param name="wrap" select="$embelled-before"/>
        <xsl:with-param name="embell" select="$embells[1]"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="count($embells) ge 2">
        <xsl:call-template name="embell">
          <xsl:with-param name="embelled-before" select="$wrapped"/>
          <xsl:with-param name="embells" select="$embells[position() ge 2]"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$wrapped"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="embell[matches(text(), '(&msup;)')]" mode="embell">
    <xsl:param as="node()" name="wrap"/>
    <xsl:param as="element(embell)" name="embell"/>
    <msup>
      <xsl:copy-of select="$wrap"/>
      <xsl:apply-templates select="$embell"/>
    </msup>
  </xsl:template>

  <xsl:template match="embell[matches(text(), '(&mover;)')]" mode="embell">
    <xsl:param as="node()" name="wrap"/>
    <xsl:param as="element(embell)" name="embell"/>
    <mover accent="true">
      <xsl:copy-of select="$wrap"/>
      <xsl:apply-templates select="$embell"/>
    </mover>
  </xsl:template>

  <xsl:template match="embell[matches(text(), '(&munder;)')]" mode="embell">
    <xsl:param as="node()" name="wrap"/>
    <xsl:param as="element(embell)" name="embell"/>
    <munder>
      <xsl:copy-of select="$wrap"/>
      <xsl:apply-templates select="$embell"/>
    </munder>
  </xsl:template>

  <xsl:template match="embell[matches(text(), '(&munderaccent;)')]" mode="embell">
    <xsl:param as="node()" name="wrap"/>
    <xsl:param as="element(embell)" name="embell"/>
    <munder>
      <xsl:attribute name="accentunder">true</xsl:attribute>
      <xsl:copy-of select="$wrap"/>
      <xsl:apply-templates select="$embell"/>
    </munder>
  </xsl:template>

  <xsl:template match="embell[matches(text(), '(&menclose;)')]" mode="embell">
    <xsl:param as="node()" name="wrap"/>
    <xsl:param as="element(embell)" name="embell"/>
    <menclose>
      <xsl:choose>
        <xsl:when test="matches($embell, '(&menclose;)')">
          <xsl:attribute name="notation">
            <xsl:choose>
              <xsl:when test="matches($embell, 'embNOT')">updiagonalstrike</xsl:when>
              <xsl:when test="matches($embell, 'embX_BARS')">updiagonalstrike downdiagonalstrike</xsl:when>
              <xsl:when test="matches($embell, 'embMBAR')">horizontalstrike</xsl:when>
              <xsl:when test="matches($embell, 'embUP_BAR')">updiagonalstrike</xsl:when>
              <xsl:when test="matches($embell, 'embDOWN_BAR')">downdiagonalstrike</xsl:when>
            </xsl:choose>
          </xsl:attribute>
        </xsl:when>
      </xsl:choose>
      <xsl:copy-of select="$wrap"/>
    </menclose>
  </xsl:template>

  <xsl:template match="embell[matches(text(), '(&mmultiscripts;)')]" mode="embell">
    <xsl:param as="node()" name="wrap"/>
    <xsl:param as="element(embell)" name="embell"/>
    <mmultiscripts>
      <xsl:copy-of select="$wrap"/>
      <mprescripts/>
      <none/>
      <xsl:apply-templates select="$embell"/>
    </mmultiscripts>
  </xsl:template>

    <!--
    edot        = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x02D9;</(ns)mo>$-$n</(ns)mover>$n";
    edot/2      = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x00A8;</(ns)mo>$-$n</(ns)mover>$n";
    edot/3      = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x20DB;</(ns)mo>$-$n</(ns)mover>$n";
    edot/4      = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x20DC;</(ns)mo>$-$n</(ns)mover>$n";

    2 => "emb1DOT", # over single dot
    3 => "emb2DOT", # over double dot
    4 => "emb3DOT", # over triple dot
    24 => "emb4DOT", # over quad dot
    -->
    <!-- Dots -->

  <xsl:template match="embell[embell = 'emb1DOT']">
    <mo>&#x2D9;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'emb2DOT']">
    <mo>&#xA8;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'emb3DOT']">
    <mo>&#x20DB;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'emb4DOT']">
    <mo>&#x20DC;</mo>
  </xsl:template>

    <!--
    edot/u      = "<(ns)munder>$+$n#$n<(ns)mo>&$#x02D9;</(ns)mo>$-$n</(ns)munder>$n";
    edot/u/2    = "<(ns)munder>$+$n#$n<(ns)mo>&$#x00A8;</(ns)mo>$-$n</(ns)munder>$n";
    edot/u/3    = "<(ns)munder>$+$n#$n<(ns)mo>&$#x20DB;</(ns)mo>$-$n</(ns)munder>$n";
    edot/u/4    = "<(ns)munder>$+$n#$n<(ns)mo>&$#x20DC;</(ns)mo>$-$n</(ns)munder>$n";

    25 => "embU_1DOT", # under single dot
    26 => "embU_2DOT", # under double dot
    27 => "embU_3DOT", # under triple dot
    28 => "embU_4DOT", # under quad dot
    -->
    <!-- Under-dots -->

  <xsl:template match="embell[embell = 'embU_1DOT']">
    <mo>&#x2D9;</mo>
  </xsl:template>


  <xsl:template match="embell[embell = 'embU_2DOT']">
    <mo>&#xA8;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_3DOT']">
    <mo>&#x20DB;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_4DOT']">
    <mo>&#x20DC;</mo>
  </xsl:template>

    <!--
    eprime      = "<(ns)msup>$+$n#$n<(ns)mo>&$#x2032;</(ns)mo>$-$n</(ns)msup>$n";
    eprime/2    = "<(ns)msup>$+$n#$n<(ns)mo>&$#x2033;</(ns)mo>$-$n</(ns)msup>$n";
    eprime/3    = "<(ns)msup>$+$n#$n<(ns)mo>&$#x2034;</(ns)mo>$-$n</(ns)msup>$n";
    eprime/b    = "<(ns)mmultiscripts>$+$n#$n<(ns)mprescripts/>$n<(ns)none/>$n<(ns)mo>&$#x2035;</(ns)mo>$-$n</(ns)mmultiscripts>$n";

    5 => "emb1PRIME", # single prime
    6 => "emb2PRIME", # double prime
    7 => "embBPRIME", # backwards prime (left of character)
    18 => "emb3PRIME", # triple prime
     -->
    <!-- Primes -->

  <xsl:template match="embell[embell = 'emb1PRIME']">
    <mo>&#x2032;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'emb2PRIME']">
    <mo>&#x2033;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'emb3PRIME']">
    <mo>&#x2034;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embBPRIME']">
    <mo>&#x2035;</mo>
  </xsl:template>

    <!--
    etilde      = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x02DC;</(ns)mo>$-$n</(ns)mover>$n";
    etilde/u    = "<(ns)munder accentunder='true'>$+$n#$n<(ns)mo>&$#x02DC;</(ns)mo>$-$n</(ns)munder>$n";

    8 => "embTILDE", #  tilde
    30 => "embU_TILDE", #  under tilde (~)
    -->
    <!-- Tilde -->

  <xsl:template match="embell[embell = 'embTILDE']">
    <mo>&#x2DC;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_TILDE']">
    <mo>&#x2DC;</mo>
  </xsl:template>

    <!--
    ehat        = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x005E;</(ns)mo>$-$n</(ns)mover>$n";
    9 => "embHAT", #  hat (circumflex)
    -->
    <!-- Hat -->

  <xsl:template match="embell[embell = 'embHAT']">
    <mo>^</mo>
  </xsl:template>
    <!--
    evec        = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x2192;</(ns)mo>$-$n</(ns)mover>$n";
    evec/l      = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x2190;</(ns)mo>$-$n</(ns)mover>$n";
    evec/lr     = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x2194;</(ns)mo>$-$n</(ns)mover>$n";
    evec/h      = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x21C0;</(ns)mo>$-$n</(ns)mover>$n";
    evec/h/l    = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x21BC;</(ns)mo>$-$n</(ns)mover>$n";
    evec/u      = "<(ns)munder>$+$n#$n<(ns)mo>&$#x2192;</(ns)mo>$-$n</(ns)munder>$n";
    evec/u/l    = "<(ns)munder>$+$n#$n<(ns)mo>&$#x2190;</(ns)mo>$-$n</(ns)munder>$n";
    evec/u/lr   = "<(ns)munder>$+$n#$n<(ns)mo>&$#x2194;</(ns)mo>$-$n</(ns)munder>$n";
    evec/u/h    = "<(ns)munder>$+$n#$n<(ns)mo>&$#x21C1;</(ns)mo>$-$n</(ns)munder>$n";
    evec/u/h/l  = "<(ns)munder>$+$n#$n<(ns)mo>&$#x21BD;</(ns)mo>$-$n</(ns)munder>$n";

    11 => "embRARROW", # over right arrow
    12 => "embLARROW", # over left arrow
    13 => "embBARROW", # over both arrow (left and right)
    14 => "embR1ARROW", #  over right single-barbed arrow
    15 => "embL1ARROW", #  over left single-barbed arrow
    33 => "embU_RARROW", # under right arrow
    34 => "embU_LARROW", # under left arrow
    35 => "embU_BARROW", # under both arrow (left and right)
    36 => "embU_R1ARROW", #  under right arrow (1 barb)
    37 => "embU_L1ARROW", #  under left arrow (1 barb)

    -->
    <!-- Arrows -->

  <xsl:template match="embell[embell = 'embRARROW']">
    <mo>&#x2192;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embLARROW']">
    <mo>&#x2190;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embBARROW']">
    <mo>&#x2194;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embR1ARROW']">
    <mo>&#x21C0;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embL1ARROW']">
    <mo>&#x21BC;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_RARROW']">
    <mo>&#x2192;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_LARROW']">
    <mo>&#x2190;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_BARROW']">
    <mo>&#x2194;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_R1ARROW']">
    <mo>&#x21C1;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_L1ARROW']">
    <mo>&#x21BD;</mo>
  </xsl:template>

    <!--
    eobar       = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x00AF;</(ns)mo>$-$n</(ns)mover>$n";
    eubar       = "<(ns)munder accentunder='true'>$+$n#$n<(ns)mo>_</(ns)mo>$-$n</(ns)munder>$n";

    17 => "embOBAR", # over-bar
    29 => "embU_BAR", #  under bar
     -->
    <!-- Bars -->

  <xsl:template match="embell[embell = 'embOBAR']">
    <mo>&#xAF;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_BAR']">
    <mo>_</mo>
  </xsl:template>

    <!--
    earc        = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x2322;</(ns)mo>$-$n</(ns)mover>$n";
    earc/u      = "<(ns)munder accentunder='true'>$+$n#$n<(ns)mo>&$#x2322;</(ns)mo>$-$n</(ns)munder>$n";
    earc/s      = "<(ns)mover accent='true'>$+$n#$n<(ns)mo>&$#x2323;</(ns)mo>$-$n</(ns)mover>$n";
    earc/u/s    = "<(ns)munder accentunder='true'>$+$n#$n<(ns)mo>&$#x2323;</(ns)mo>$-$n</(ns)munder>$n";
    19 => "embFROWN", #  over-arc, concave downward
    20 => "embSMILE", #  over-arc, concave upward
    31 => "embU_FROWN", #  under arc (ends point down)
    32 => "embU_SMILE", #  under arc (ends point up)
    -->
    <!-- Arcs -->

  <xsl:template match="embell[embell = 'embFROWN']">
    <mo>&#x2322;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embSMILE']">
    <mo>&#x2323;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_FROWN']">
    <mo>&#x2322;</mo>
  </xsl:template>

  <xsl:template match="embell[embell = 'embU_SMILE']">
    <mo>&#x2323;</mo>
  </xsl:template>

    <!--
    enot = "<(ns)menclose notation='updiagonalstrike'>$+$n#$-$n</(ns)menclose>$n";
    estrike = "<(ns)menclose notation='updiagonalstrike downdiagonalstrike'>$+$n#$-$n</(ns)menclose>$n";
    estrike/m = "<(ns)menclose notation='horizontalstrike'>$+$n#$-$n</(ns)menclose>$n";
    estrike/up = "<(ns)menclose notation='updiagonalstrike'>$+$n#$-$n</(ns)menclose>$n";
    estrike/dn = "<(ns)menclose notation='downdiagonalstrike'>$+$n#$-$n</(ns)menclose>$n";

    10 => "embNOT", #  diagonal slash through character
    21 => "embX_BARS", # double diagonal bars
    16 => "embMBAR", # mid-height horizontal bar
    22 => "embUP_BAR", # bottom-left to top-right diagonal bar
    23 => "embDOWN_BAR", # top-left to bottom-right diagonal bar
    -->
    <!-- Strikes -->

</xsl:stylesheet>
