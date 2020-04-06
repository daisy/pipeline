<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:ssml="http://www.w3.org/2001/10/synthesis"
    version="2.0">
	

    <!--  the SSML needs to be serialized because of the starting \voice command -->
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

    <xsl:param name="voice" select="''"/>
    <xsl:param name="ending-mark" select="''"/>

    <xsl:variable name="end">
    	<xsl:if test="$ending-mark != ''">
    		<ssml:mark name="{$ending-mark}"/>
    	</xsl:if>
    	<ssml:break time="250ms"/>
    </xsl:variable>

	<xsl:variable name="ipa_to_sampa">
		<!-- Lower case SAMPA symbols -->
		<entry key="97">a</entry>
		<entry key="98">b</entry>
		<entry key="595">b_&lt;</entry>
		<entry key="99">c</entry>
		<entry key="100">d</entry>
		<entry key="598">d`</entry>
		<entry key="599">d_&lt;</entry>
		<entry key="101">e</entry>
		<entry key="102">f</entry>
		<entry key="609">g</entry>
		<entry key="608">g_&lt;</entry>
		<entry key="104">h</entry>
		<entry key="614">h\</entry>
		<entry key="105">i</entry>
		<entry key="106">j</entry>
		<entry key="669">j\</entry>
		<entry key="107">k</entry>
		<entry key="108">l</entry>
		<entry key="621">l`</entry>
		<entry key="634">l\</entry>
		<entry key="109">m</entry>
		<entry key="110">n</entry>
		<entry key="627">n`</entry>
		<entry key="111">o</entry>
		<entry key="112">p</entry>
		<entry key="632">p\</entry>
		<entry key="113">q</entry>
		<entry key="114">r</entry>
		<entry key="637">r`</entry>
		<entry key="633">r\</entry>
		<entry key="635">r\`</entry>
		<entry key="115">s</entry>
		<entry key="642">s`</entry>
		<entry key="597">s\</entry>
		<entry key="116">t</entry>
		<entry key="648">t`</entry>
		<entry key="117">u</entry>
		<entry key="118">v</entry>
		<entry key="651">v\</entry>
		<entry key="119">w</entry>
		<entry key="120">x</entry>
		<entry key="615">x\</entry>
		<entry key="121">y</entry>
		<entry key="122">z</entry>
		<entry key="656">z`</entry>
		<entry key="657">z\</entry>
		<!-- Capital case SAMPA symbols -->
		<entry key="593">A</entry>
		<entry key="946">B</entry>
		<entry key="665">B\</entry>
		<entry key="231">C</entry>
		<entry key="240">D</entry>
		<entry key="603">E</entry>
		<entry key="625">F</entry>
		<entry key="611">G</entry>
		<entry key="610">G\</entry>
		<entry key="667">G\_&lt;</entry>
		<entry key="613">H</entry>
		<entry key="668">H\</entry>
		<entry key="618">I</entry>
		<entry key="7547">I\</entry>
		<entry key="626">J</entry>
		<entry key="607">J\</entry>
		<entry key="644">J\_&lt;</entry>
		<entry key="620">K</entry>
		<entry key="622">K\</entry>
		<entry key="654">L</entry>
		<entry key="671">L\</entry>
		<entry key="623">M</entry>
		<entry key="624">M\</entry>
		<entry key="331">N</entry>
		<entry key="628">N\</entry>
		<entry key="596">O</entry>
		<entry key="664">O\</entry>
		<entry key="594">Q</entry>
		<entry key="641">R</entry>
		<entry key="640">R\</entry>
		<entry key="643">S</entry>
		<entry key="952">T</entry>
		<entry key="650">U</entry>
		<entry key="7551">U\</entry>
		<entry key="652">V</entry>
		<entry key="653">W</entry>
		<entry key="967">X</entry>
		<entry key="295">X\</entry>
		<entry key="655">Y</entry>
		<entry key="658">Z</entry>
		<!-- Other SAMPA symbols (with IPA equivalent) -->
		<entry key="46">.</entry>
		<entry key="712">"</entry>
		<entry key="716">%</entry>
		<entry key="690">_j</entry>
		<entry key="720">:</entry>
		<entry key="721">:\</entry>
		<entry key="601">@</entry>
		<entry key="600">@\</entry>
		<entry key="602">@`</entry>
		<entry key="230">{</entry>
		<entry key="649">}</entry>
		<entry key="616">1</entry>
		<entry key="248">2</entry>
		<entry key="604">3</entry>
		<entry key="606">3\</entry>
		<entry key="638">4</entry>
		<entry key="619">5</entry>
		<entry key="592">6</entry>
		<entry key="612">7</entry>
		<entry key="629">8</entry>
		<entry key="339">9</entry>
		<entry key="630">&amp;</entry>
		<entry key="660">?</entry>
		<entry key="661">?\</entry>
		<entry key="674">&lt;\</entry>
		<entry key="673">&gt;\</entry>
		<entry key="42779">^</entry>
		<entry key="42780">!</entry>
		<entry key="451">!\</entry>
		<entry key="124">|</entry>
		<entry key="448">|\</entry>
		<entry key="8214">||</entry>
		<entry key="449">|\|\</entry>
		<entry key="450">=\</entry>
		<entry key="8255">-\</entry>
	</xsl:variable>
	<xsl:variable name="sampa_diacritic">
	<!-- Diacritics SAMPA symbols -->
		<entry key="776">_"</entry>
		<entry key="799">_+</entry>
		<entry key="800">_-</entry>
		<entry key="780">_/</entry>
		<entry key="805">_0</entry>
		<entry key="809">=</entry>
		<entry key="700">_&gt;</entry>
		<entry key="740">_?\</entry>
		<entry key="770">_\</entry>
		<entry key="815">_^</entry>
		<entry key="794">_}</entry>
		<entry key="734">`</entry>
		<entry key="771">~</entry>
		<entry key="792">_A</entry>
		<entry key="826">_a</entry>
		<entry key="783">_B</entry>
		<entry key="7621">_B_L</entry>
		<entry key="796">_c</entry>
		<entry key="810">_d</entry>
		<entry key="820">_e</entry>
		<entry key="8600">&lt;F&gt;</entry>
		<entry key="770">_F</entry>
		<entry key="736">_G</entry>
		<entry key="769">_H</entry>
		<entry key="7620">_H_T</entry>
		<entry key="688">_h</entry>
		<entry key="690">_j</entry>
		<entry key="816">_k</entry>
		<entry key="768">_L</entry>
		<entry key="737">_l</entry>
		<entry key="772">_M</entry>
		<entry key="827">_m</entry>
		<entry key="828">_N</entry>
		<entry key="8319">_n</entry>
		<entry key="825">_O</entry>
		<entry key="798">_o</entry>
		<entry key="793">_q</entry>
		<entry key="8599">&lt;R&gt;</entry>
		<entry key="780">_R</entry>
		<entry key="7624">_R_F</entry>
		<entry key="797">_r</entry>
		<entry key="779">_T</entry>
		<entry key="804">_t</entry>
		<entry key="812">_v</entry>
		<entry key="695">_w</entry>
		<entry key="774">_X</entry>
		<entry key="829">_x</entry>
	</xsl:variable>

    <xsl:template match="*">
    	<xsl:if test="$voice != ''">
    		<xsl:value-of select="concat('\voice{', $voice, '}')"/>
    	</xsl:if>
    	<xsl:apply-templates mode="serialize" select="."/>
    	<xsl:apply-templates mode="serialize" select="$end"/>
  	</xsl:template>

	<xsl:template match="text()" mode="serialize">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match="ssml:token" mode="serialize" priority="3">
		<xsl:apply-templates mode="serialize" select="node()"/>
	</xsl:template>
	
	<xsl:template match="ssml:phoneme" mode="serialize" priority="3">
		<!-- Following the documentation, acapela only supports IPA alaphabet for phoneme tags, 
			but requires to use hexa code with &#x<code>; at least for non-ascii char.
			For SAMPA, we must use the prx (or prn) tag with a space between 
			each phoneme of the phonetic string.
			It is recommended to check the "language manuals" of acapela to adapt 
			the phoneme to language-specific acapela's versions of SAMPA :
			We noticed a divergence between the SAMPA specification and acapela phonetic description 
			of nasalized vowels in french (like the E~ vowel in SAMPA must be noted e~ in acapela's version of SAMPA)
			
			18/03/2020 : Acapela helpdesk notified us that IPA phonemes were not supported 
       			despite what is indicated in the documentation. -->
		<xsl:choose>
        	<xsl:when test="@ssml:alphabet='ipa'">
        		<!--  
        		<xsl:value-of select="'&lt;phoneme alphabet=&quot;ipa&quot; ph=&quot;'"/> -->
				<xsl:value-of select="'\prx='"/>
				<xsl:variable name="sampa_conversion">
	            	<xsl:for-each select="string-to-codepoints(@ssml:ph)">
						<!-- <xsl:choose>
							<xsl:when test=". > 127">
								<xsl:value-of select="'&amp;#x'"/>
								<xsl:call-template name="ConvertDecToHex">
									<xsl:with-param name="index" select="."/>
								</xsl:call-template>
								<xsl:value-of select="';'"/>
								
							</xsl:when>
							<xsl:otherwise> 
								<xsl:value-of select="codepoints-to-string((.))"/>
							</xsl:otherwise>
						</xsl:choose> -->
						
	
						<!-- prx tag with space-separated sampa phonemes-->
						<xsl:variable name="key" select="string(.)" />
						<xsl:choose>
							<xsl:when test="$sampa_diacritic/entry[@key=$key]">
								<xsl:value-of select="$sampa_diacritic/entry[@key=$key]"/>
							</xsl:when>
							<xsl:otherwise> 
								<xsl:value-of select="' '"/>
								<xsl:value-of select="$ipa_to_sampa/entry[@key=$key]"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:variable>
				
				<xsl:choose>
					<xsl:when test="ancestor-or-self::node()/@xml:lang = 'fr'">
						<!-- in the french version of SAMPA (and in acapela language manual), 
							lowercase e, a and o are used for nasalized vowel /ɛ̃/ /ɑ̃/ and /ɔ̃/ 
							instead of E A and O. We correct the result here to match 
							the acapela version of sampa -->
						<xsl:value-of select="replace(replace(replace($sampa_conversion,'A~','a~'),'E~','e~'),'O~','o~')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$sampa_conversion"/>
					</xsl:otherwise>
				</xsl:choose>
				
				<xsl:value-of select="' \'"/>
				<!-- <xsl:value-of select="'&quot;&gt;'"/>
				<xsl:apply-templates mode="serialize" select="node()"/>
				<xsl:value-of select="'&lt;/phoneme&gt;'"/> -->
        	</xsl:when>
        	<xsl:otherwise> 
                <xsl:value-of select="concat('\prx= ', @ssml:ph, ' \')"/>
        	</xsl:otherwise>
    	</xsl:choose>
	</xsl:template>

	<xsl:template match="ssml:mark" mode="serialize" priority="3">
	    <!--  we can use any name as long as it is unique -->
		<xsl:value-of select="concat('&lt;mark name=&quot;', generate-id(), '&quot;/>')"/>
	</xsl:template>

	<xsl:template match="ssml:*" mode="serialize" priority="2">
		<xsl:value-of select="concat('&lt;', local-name())"/>
		<xsl:apply-templates select="@*" mode="serialize"/>
		<xsl:value-of select="'>'"/>
		<xsl:if test="local-name() = 's'">
		  <!-- Acapela can fail notifying marks if the sentence doesn't start with a white space. -->
		  <xsl:value-of select="' '"/>
		</xsl:if>
		<xsl:apply-templates mode="serialize" select="node()"/>
		<xsl:value-of select="concat('&lt;/', local-name(), '>')"/>
	</xsl:template>

	<xsl:template match="*" mode="serialize" priority="1">
		<xsl:apply-templates mode="serialize" select="node()"/>
	</xsl:template>

	<xsl:template match="@*" mode="serialize">
		<xsl:value-of select="concat(' ', local-name(), '=&quot;', ., '&quot;')"/>
	</xsl:template>

    <!-- Template to convert a decimal number to hexadecimal
    	(from the doc, acapela supports hexa entity in phonemes for non-ascii characters)
		
		Code from https://www.getsymphony.com/download/xslt-utilities/view/78502/ : -->
	<xsl:template name="ConvertDecToHex">
    <xsl:param name="index" />
    <xsl:if test="$index > 0">
      <xsl:call-template name="ConvertDecToHex">
        <xsl:with-param name="index" select="floor($index div 16)" />
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="$index mod 16 &lt; 10">
          <xsl:value-of select="$index mod 16" />
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="$index mod 16 = 10">A</xsl:when>
            <xsl:when test="$index mod 16 = 11">B</xsl:when>
            <xsl:when test="$index mod 16 = 12">C</xsl:when>
            <xsl:when test="$index mod 16 = 13">D</xsl:when>
            <xsl:when test="$index mod 16 = 14">E</xsl:when>
            <xsl:when test="$index mod 16 = 15">F</xsl:when>
            <xsl:otherwise>A</xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>
