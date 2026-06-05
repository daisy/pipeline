<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
	>
	<xsl:template name="STYLESHEETS">
		<xsl:text>{\stylesheet</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="NORMAL_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon222\snext0 Normal;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="H1_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon0\snext0 heading 1;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="H2_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon0\snext0 heading 2;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="H3_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon0\snext0 heading 3;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="H4_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon0\snext0 heading 4;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="H5_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon0\snext0 heading 5;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="H6_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon0\snext0 heading 6;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="STRONG_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon0 strong;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="EM_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon0 Emphazised;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="TITLE_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon0 title;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="SUBTITLE_STYLE"></xsl:call-template>
		<xsl:text>\sbasedon0 subtitle;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="BLOCKQUOTE_STYLE"/>
		<xsl:text>\sbasedon0\snext0 citation;}</xsl:text>

		<xsl:text>{</xsl:text>
		<xsl:call-template name="BOX_STYLE"/>
		<xsl:text>\sbasedon0\snext0 boxed;}</xsl:text>

		<xsl:text>}</xsl:text>
	</xsl:template>

	<xsl:template name="FONTS">
		<xsl:text>{\fonttbl{\f0\fswiss Arial;}{\f1\fmodern Courier New;}}</xsl:text>
	</xsl:template>

	<xsl:template name="COLORS">
		<xsl:text>{\colortbl;\red0\green0\blue0;\red255\green255\blue255;\red0\green0\blue255;}</xsl:text>
	</xsl:template>

	<xsl:template name="DOC_PROPS">
		<xsl:text>\deflang</xsl:text>
		<xsl:call-template name="LANG_ID">
			<xsl:with-param name="lang" select="//book/@lang|//book/@xml:lang|//dtb:book/@lang|//dtb:book/@xml:lang"/>
		</xsl:call-template>
		<xsl:text>\paperw11905\paperh16838\psz9\margl1134\margr1134\margt1134\margb1134\deftab283\notabind\fet2\ftnnar\aftnnar
</xsl:text>
	</xsl:template>

	<xsl:template name="LANG_ID">
		<xsl:param name="lang"/>
		<xsl:choose>
			<xsl:when test="$lang='ar-SA'">1025</xsl:when>
			<xsl:when test="$lang='bg-BG'">1026</xsl:when>
			<xsl:when test="$lang='ca-ES'">1027</xsl:when>
			<xsl:when test="$lang='zh-TW'">1028</xsl:when>
			<xsl:when test="$lang='cs-CZ'">1029</xsl:when>
			<xsl:when test="$lang='da-DK'">1030</xsl:when>
			<xsl:when test="$lang='de-DE'">1031</xsl:when>
			<xsl:when test="$lang='el-GR'">1032</xsl:when>
			<xsl:when test="$lang='en-US'">1033</xsl:when>
			<xsl:when test="$lang='fi-FI'">1035</xsl:when>
			<xsl:when test="$lang='fr-FR'">1036</xsl:when>
			<xsl:when test="$lang='he-IL'">1037</xsl:when>
			<xsl:when test="$lang='hu-HU'">1038</xsl:when>
			<xsl:when test="$lang='is-IS'">1039</xsl:when>
			<xsl:when test="$lang='it-IT'">1040</xsl:when>
			<xsl:when test="$lang='ja-JP'">1041</xsl:when>
			<xsl:when test="$lang='ko-KR'">1042</xsl:when>
			<xsl:when test="$lang='nl-NL'">1043</xsl:when>
			<xsl:when test="$lang='nb-NO'">1044</xsl:when>
			<xsl:when test="$lang='pl-PL'">1045</xsl:when>
			<xsl:when test="$lang='pt-BR'">1046</xsl:when>
			<xsl:when test="$lang='ro-RO'">1048</xsl:when>
			<xsl:when test="$lang='ru-RU'">1049</xsl:when>
			<xsl:when test="$lang='hr-HR'">1050</xsl:when>
			<xsl:when test="$lang='sk-SK'">1051</xsl:when>
			<xsl:when test="$lang='sq-AL'">1052</xsl:when>
			<xsl:when test="$lang='sv-SE'">1053</xsl:when>
			<xsl:when test="$lang='th-TH'">1054</xsl:when>
			<xsl:when test="$lang='tr-TR'">1055</xsl:when>
			<xsl:when test="$lang='ur-PK'">1056</xsl:when>
			<xsl:when test="$lang='id-ID'">1057</xsl:when>
			<xsl:when test="$lang='uk-UA'">1058</xsl:when>
			<xsl:when test="$lang='be-BY'">1059</xsl:when>
			<xsl:when test="$lang='sl-SI'">1060</xsl:when>
			<xsl:when test="$lang='et-EE'">1061</xsl:when>
			<xsl:when test="$lang='lv-LV'">1062</xsl:when>
			<xsl:when test="$lang='lt-LT'">1063</xsl:when>
			<xsl:when test="$lang='fa-IR'">1065</xsl:when>
			<xsl:when test="$lang='vi-VN'">1066</xsl:when>
			<xsl:when test="$lang='hy-AM'">1067</xsl:when>
			<xsl:when test="$lang='az-Latn-AZ'">1068</xsl:when>
			<xsl:when test="$lang='eu-ES'">1069</xsl:when>
			<xsl:when test="$lang='mk-MK'">1071</xsl:when>
			<xsl:when test="$lang='af-ZA'">1078</xsl:when>
			<xsl:when test="$lang='ka-GE'">1079</xsl:when>
			<xsl:when test="$lang='fo-FO'">1080</xsl:when>
			<xsl:when test="$lang='hi-IN'">1081</xsl:when>
			<xsl:when test="$lang='ms-MY'">1086</xsl:when>
			<xsl:when test="$lang='kk-KZ'">1087</xsl:when>
			<xsl:when test="$lang='ky-KG'">1088</xsl:when>
			<xsl:when test="$lang='sw-KE'">1089</xsl:when>
			<xsl:when test="$lang='uz-Latn-UZ'">1091</xsl:when>
			<xsl:when test="$lang='tt-RU'">1092</xsl:when>
			<xsl:when test="$lang='pa-IN'">1094</xsl:when>
			<xsl:when test="$lang='gu-IN'">1095</xsl:when>
			<xsl:when test="$lang='ta-IN'">1097</xsl:when>
			<xsl:when test="$lang='te-IN'">1098</xsl:when>
			<xsl:when test="$lang='kn-IN'">1099</xsl:when>
			<xsl:when test="$lang='mr-IN'">1102</xsl:when>
			<xsl:when test="$lang='sa-IN'">1103</xsl:when>
			<xsl:when test="$lang='mn-MN'">1104</xsl:when>
			<xsl:when test="$lang='gl-ES'">1110</xsl:when>
			<xsl:when test="$lang='kok-IN'">1111</xsl:when>
			<xsl:when test="$lang='syr-SY'">1114</xsl:when>
			<xsl:when test="$lang='dv-MV'">1125</xsl:when>
			<xsl:when test="$lang='ar-IQ'">2049</xsl:when>
			<xsl:when test="$lang='zh-CN'">2052</xsl:when>
			<xsl:when test="$lang='de-CH'">2055</xsl:when>
			<xsl:when test="$lang='en-GB'">2057</xsl:when>
			<xsl:when test="$lang='es-MX'">2058</xsl:when>
			<xsl:when test="$lang='fr-BE'">2060</xsl:when>
			<xsl:when test="$lang='it-CH'">2064</xsl:when>
			<xsl:when test="$lang='nl-BE'">2067</xsl:when>
			<xsl:when test="$lang='nn-NO'">2068</xsl:when>
			<xsl:when test="$lang='pt-PT'">2070</xsl:when>
			<xsl:when test="$lang='sr-Latn-CS'">2074</xsl:when>
			<xsl:when test="$lang='sv-FI'">2077</xsl:when>
			<xsl:when test="$lang='az-Cyrl-AZ'">2092</xsl:when>
			<xsl:when test="$lang='ms-BN'">2110</xsl:when>
			<xsl:when test="$lang='uz-Cyrl-UZ'">2115</xsl:when>
			<xsl:when test="$lang='ar-EG'">3073</xsl:when>
			<xsl:when test="$lang='zh-HK'">3076</xsl:when>
			<xsl:when test="$lang='de-AT'">3079</xsl:when>
			<xsl:when test="$lang='en-AU'">3081</xsl:when>
			<xsl:when test="$lang='es-ES'">3082</xsl:when>
			<xsl:when test="$lang='fr-CA'">3084</xsl:when>
			<xsl:when test="$lang='sr-Cyrl-CS'">3098</xsl:when>
			<xsl:when test="$lang='ar-LY'">4097</xsl:when>
			<xsl:when test="$lang='zh-SG'">4100</xsl:when>
			<xsl:when test="$lang='de-LU'">4103</xsl:when>
			<xsl:when test="$lang='en-CA'">4105</xsl:when>
			<xsl:when test="$lang='es-GT'">4106</xsl:when>
			<xsl:when test="$lang='fr-CH'">4108</xsl:when>
			<xsl:when test="$lang='ar-DZ'">5121</xsl:when>
			<xsl:when test="$lang='zh-MO'">5124</xsl:when>
			<xsl:when test="$lang='de-LI'">5127</xsl:when>
			<xsl:when test="$lang='en-NZ'">5129</xsl:when>
			<xsl:when test="$lang='es-CR'">5130</xsl:when>
			<xsl:when test="$lang='fr-LU'">5132</xsl:when>
			<xsl:when test="$lang='ar-MA'">6145</xsl:when>
			<xsl:when test="$lang='en-IE'">6153</xsl:when>
			<xsl:when test="$lang='es-PA'">6154</xsl:when>
			<xsl:when test="$lang='fr-MC'">6156</xsl:when>
			<xsl:when test="$lang='ar-TN'">7169</xsl:when>
			<xsl:when test="$lang='en-ZA'">7177</xsl:when>
			<xsl:when test="$lang='es-DO'">7178</xsl:when>
			<xsl:when test="$lang='ar-OM'">8193</xsl:when>
			<xsl:when test="$lang='en-JM'">8201</xsl:when>
			<xsl:when test="$lang='es-VE'">8202</xsl:when>
			<xsl:when test="$lang='ar-YE'">9217</xsl:when>
			<xsl:when test="$lang='en-029'">9225</xsl:when>
			<xsl:when test="$lang='es-CO'">9226</xsl:when>
			<xsl:when test="$lang='ar-SY'">10241</xsl:when>
			<xsl:when test="$lang='en-BZ'">10249</xsl:when>
			<xsl:when test="$lang='es-PE'">10250</xsl:when>
			<xsl:when test="$lang='ar-JO'">11265</xsl:when>
			<xsl:when test="$lang='en-TT'">11273</xsl:when>
			<xsl:when test="$lang='es-AR'">11274</xsl:when>
			<xsl:when test="$lang='ar-LB'">12289</xsl:when>
			<xsl:when test="$lang='en-ZW'">12297</xsl:when>
			<xsl:when test="$lang='es-EC'">12298</xsl:when>
			<xsl:when test="$lang='ar-KW'">13313</xsl:when>
			<xsl:when test="$lang='en-PH'">13321</xsl:when>
			<xsl:when test="$lang='es-CL'">13322</xsl:when>
			<xsl:when test="$lang='ar-AE'">14337</xsl:when>
			<xsl:when test="$lang='es-UY'">14346</xsl:when>
			<xsl:when test="$lang='ar-BH'">15361</xsl:when>
			<xsl:when test="$lang='es-PY'">15370</xsl:when>
			<xsl:when test="$lang='ar-QA'">16385</xsl:when>
			<xsl:when test="$lang='es-BO'">16394</xsl:when>
			<xsl:when test="$lang='es-SV'">17418</xsl:when>
			<xsl:when test="$lang='es-HN'">18442</xsl:when>
			<xsl:when test="$lang='es-NI'">19466</xsl:when>
			<xsl:when test="$lang='es-PR'">20490</xsl:when>
			<xsl:when test="$lang='zh-Hant'">31748</xsl:when>
			<xsl:when test="$lang='sr'">31770</xsl:when>
			<xsl:when test="$lang='sma-NO'">6203</xsl:when>
			<xsl:when test="$lang='sr-Cyrl-BA'">7194</xsl:when>
			<xsl:when test="$lang='zu-ZA'">1077</xsl:when>
			<xsl:when test="$lang='xh-ZA'">1076</xsl:when>
			<xsl:when test="$lang='fy-NL'">1122</xsl:when>
			<xsl:when test="$lang='tn-ZA'">1074</xsl:when>
			<xsl:when test="$lang='se-SE'">2107</xsl:when>
			<xsl:when test="$lang='sma-SE'">7227</xsl:when>
			<xsl:when test="$lang='hr-BA'">4122</xsl:when>
			<xsl:when test="$lang='smn-FI'">9275</xsl:when>
			<xsl:when test="$lang='quz-PE'">3179</xsl:when>
			<xsl:when test="$lang='se-FI'">3131</xsl:when>
			<xsl:when test="$lang='sms-FI'">8251</xsl:when>
			<xsl:when test="$lang='cy-GB'">1106</xsl:when>
			<xsl:when test="$lang='bs-Latn-BA'">5146</xsl:when>
			<xsl:when test="$lang='bs-Cyrl-BA'">8218</xsl:when>
			<xsl:when test="$lang='fil-PH'">1124</xsl:when>
			<xsl:when test="$lang='smj-NO'">4155</xsl:when>
			<xsl:when test="$lang='arn-CL'">1146</xsl:when>
			<xsl:when test="$lang='iu-Latn-CA'">2141</xsl:when>
			<xsl:when test="$lang='mi-NZ'">1153</xsl:when>
			<xsl:when test="$lang='quz-EC'">2155</xsl:when>
			<xsl:when test="$lang='ga-IE'">2108</xsl:when>
			<xsl:when test="$lang='sr-Latn-BA'">6170</xsl:when>
			<xsl:when test="$lang='moh-CA'">1148</xsl:when>
			<xsl:when test="$lang='smj-SE'">5179</xsl:when>
			<xsl:when test="$lang='lb-LU'">1134</xsl:when>
			<xsl:when test="$lang='ns-ZA'">1132</xsl:when>
			<xsl:when test="$lang='quz-BO'">1131</xsl:when>
			<xsl:when test="$lang='se-NO'">1083</xsl:when>
			<xsl:when test="$lang='mt-MT'">1082</xsl:when>
			<xsl:when test="$lang='rm-CH'">1047</xsl:when>
			<xsl:when test="starts-with($lang, 'ar')">1025</xsl:when>
			<xsl:when test="starts-with($lang, 'bg')">1026</xsl:when>
			<xsl:when test="starts-with($lang, 'ca')">1027</xsl:when>
			<xsl:when test="starts-with($lang, 'zh')">1028</xsl:when>
			<xsl:when test="starts-with($lang, 'cs')">1029</xsl:when>
			<xsl:when test="starts-with($lang, 'da')">1030</xsl:when>
			<xsl:when test="starts-with($lang, 'de')">1031</xsl:when>
			<xsl:when test="starts-with($lang, 'el')">1032</xsl:when>
			<xsl:when test="starts-with($lang, 'en')">1033</xsl:when>
			<xsl:when test="starts-with($lang, 'es')">1034</xsl:when>
			<xsl:when test="starts-with($lang, 'fi')">1035</xsl:when>
			<xsl:when test="starts-with($lang, 'fr')">1036</xsl:when>
			<xsl:when test="starts-with($lang, 'he')">1037</xsl:when>
			<xsl:when test="starts-with($lang, 'hu')">1038</xsl:when>
			<xsl:when test="starts-with($lang, 'is')">1039</xsl:when>
			<xsl:when test="starts-with($lang, 'it')">1040</xsl:when>
			<xsl:when test="starts-with($lang, 'ja')">1041</xsl:when>
			<xsl:when test="starts-with($lang, 'ko')">1042</xsl:when>
			<xsl:when test="starts-with($lang, 'nl')">1043</xsl:when>
			<xsl:when test="starts-with($lang, 'no')">1044</xsl:when>
			<xsl:when test="starts-with($lang, 'pl')">1045</xsl:when>
			<xsl:when test="starts-with($lang, 'pt')">1046</xsl:when>
			<xsl:when test="starts-with($lang, 'ro')">1048</xsl:when>
			<xsl:when test="starts-with($lang, 'ru')">1049</xsl:when>
			<xsl:when test="starts-with($lang, 'hr')">1050</xsl:when>
			<xsl:when test="starts-with($lang, 'sk')">1051</xsl:when>
			<xsl:when test="starts-with($lang, 'sq')">1052</xsl:when>
			<xsl:when test="starts-with($lang, 'sv')">1053</xsl:when>
			<xsl:when test="starts-with($lang, 'th')">1054</xsl:when>
			<xsl:when test="starts-with($lang, 'tr')">1055</xsl:when>
			<xsl:when test="starts-with($lang, 'ur')">1056</xsl:when>
			<xsl:when test="starts-with($lang, 'id')">1057</xsl:when>
			<xsl:when test="starts-with($lang, 'uk')">1058</xsl:when>
			<xsl:when test="starts-with($lang, 'be')">1059</xsl:when>
			<xsl:when test="starts-with($lang, 'sl')">1060</xsl:when>
			<xsl:when test="starts-with($lang, 'et')">1061</xsl:when>
			<xsl:when test="starts-with($lang, 'lv')">1062</xsl:when>
			<xsl:when test="starts-with($lang, 'lt')">1063</xsl:when>
			<xsl:when test="starts-with($lang, 'fa')">1065</xsl:when>
			<xsl:when test="starts-with($lang, 'vi')">1066</xsl:when>
			<xsl:when test="starts-with($lang, 'hy')">1067</xsl:when>
			<xsl:when test="starts-with($lang, 'az')">1068</xsl:when>
			<xsl:when test="starts-with($lang, 'eu')">1069</xsl:when>
			<xsl:when test="starts-with($lang, 'mk')">1071</xsl:when>
			<xsl:when test="starts-with($lang, 'af')">1078</xsl:when>
			<xsl:when test="starts-with($lang, 'ka')">1079</xsl:when>
			<xsl:when test="starts-with($lang, 'fo')">1080</xsl:when>
			<xsl:when test="starts-with($lang, 'hi')">1081</xsl:when>
			<xsl:when test="starts-with($lang, 'ms')">1086</xsl:when>
			<xsl:when test="starts-with($lang, 'kk')">1087</xsl:when>
			<xsl:when test="starts-with($lang, 'ky')">1088</xsl:when>
			<xsl:when test="starts-with($lang, 'sw')">1089</xsl:when>
			<xsl:when test="starts-with($lang, 'uz')">1091</xsl:when>
			<xsl:when test="starts-with($lang, 'tt')">1092</xsl:when>
			<xsl:when test="starts-with($lang, 'pa')">1094</xsl:when>
			<xsl:when test="starts-with($lang, 'gu')">1095</xsl:when>
			<xsl:when test="starts-with($lang, 'ta')">1097</xsl:when>
			<xsl:when test="starts-with($lang, 'te')">1098</xsl:when>
			<xsl:when test="starts-with($lang, 'kn')">1099</xsl:when>
			<xsl:when test="starts-with($lang, 'mr')">1102</xsl:when>
			<xsl:when test="starts-with($lang, 'sa')">1103</xsl:when>
			<xsl:when test="starts-with($lang, 'mn')">1104</xsl:when>
			<xsl:when test="starts-with($lang, 'gl')">1110</xsl:when>
			<xsl:when test="starts-with($lang, 'kok')">1111</xsl:when>
			<xsl:when test="starts-with($lang, 'syr')">1114</xsl:when>
			<xsl:when test="starts-with($lang, 'dv')">1125</xsl:when>
			<xsl:otherwise>1024</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="NORMAL_STYLE">
		<xsl:call-template name="NORMAL_STYLE_FONT_ONLY"/>
		<xsl:text>\sb100\sa100\li0\ri0 </xsl:text>
	</xsl:template>
	<xsl:template name="NORMAL_STYLE_FONT_ONLY">
		<xsl:text>\s0\plain\fs20 </xsl:text>
	</xsl:template>
	<xsl:template name="H1_STYLE">
		<xsl:text>\s1\sb200\sa100\li0\ri0\plain\fs32\b </xsl:text>
	</xsl:template>
	<xsl:template name="H2_STYLE">
		<xsl:text>\s2\sb200\sa100\li0\ri0\plain\fs28\b\i </xsl:text>
	</xsl:template>
	<xsl:template name="H3_STYLE">
		<xsl:text>\s3\sb200\sa100\li0\ri0\plain\fs24\b </xsl:text>
	</xsl:template>
	<xsl:template name="H4_STYLE">
		<xsl:text>\s4\sb200\sa100\li0\ri0\plain\fs22\b </xsl:text>
	</xsl:template>
	<xsl:template name="H5_STYLE">
		<xsl:text>\s5\sb200\sa100\li0\ri0\plain\fs22\i </xsl:text>
	</xsl:template>
	<xsl:template name="H6_STYLE">
		<xsl:text>\s6\sb200\sa100\li0\ri0\plain\fs20\i </xsl:text>
	</xsl:template>
	<xsl:template name="STRONG_STYLE">
		<xsl:text>\s7\plain\fs20\b </xsl:text>
	</xsl:template>
	<xsl:template name="EM_STYLE">
		<xsl:text>\s8\plain\fs20\b </xsl:text>
	</xsl:template>
	<xsl:template name="TITLE_STYLE">
		<xsl:text>\s9\sb200\sa200\qc\plain\fs32\b </xsl:text>
	</xsl:template>
	<xsl:template name="SUBTITLE_STYLE">
		<xsl:text>\s10\sb100\sa100\li0\ri0\qc\plain\fs28\b </xsl:text>
	</xsl:template>
	<xsl:template name="BLOCKQUOTE_STYLE">
		<xsl:text>\s11\sb200\sa200\li0\ri0\plain\fs20\i </xsl:text>
	</xsl:template>
	<xsl:template name="BOX_STYLE">
		<xsl:text>\s12\sb200\sa200\li750\ri750\box\brdrs\brdrw1\brsp250\plain\fs20 </xsl:text>
	</xsl:template>
</xsl:stylesheet>