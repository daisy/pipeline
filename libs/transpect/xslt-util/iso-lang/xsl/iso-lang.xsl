<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:tr="http://transpect.io"
    exclude-result-prefixes="xs"
    version="2.0">
    
    <!--  function tr:is-valid-iso-lang-code
          checks whether string matches ISO 639-1 language code
    -->
    
  <xsl:function name="tr:is-valid-iso-lang-code" as="xs:boolean">
    <xsl:param name="lang" as="xs:string"/>
    <xsl:sequence select="some $i in document('')//tr:langs/tr:lang satisfies $i/@code eq $lang"/>
  </xsl:function>
  
  <xsl:function name="tr:lang-is-ltr" as="xs:boolean">
    <xsl:param name="lang" as="xs:string"/>
    <xsl:sequence select="$lang = document('')//tr:langs/tr:lang[@dir eq 'ltr']/@code"/>
  </xsl:function>
  
  <xsl:function name="tr:lang-is-rtl" as="xs:boolean">
    <xsl:param name="lang" as="xs:string"/>
    <xsl:sequence select="$lang = document('')//tr:langs/tr:lang[@dir eq 'rtl']/@code"/>
  </xsl:function>
  
  <xsl:function name="tr:lang-dir" as="xs:string">
    <xsl:param name="lang" as="xs:string"/>
    <xsl:sequence select="document('')//tr:langs/tr:lang[$lang eq @code]/@dir"/>
  </xsl:function>
  
  <tr:langs>
    <tr:lang code="alpha2" dir="ltr" value="English"/>
    <tr:lang code="aa" dir="ltr" value="Afar"/>
    <tr:lang code="ab" dir="ltr" value="Abkhazian"/>
    <tr:lang code="ae" dir="ltr" value="Avestan"/>
    <tr:lang code="af" dir="ltr" value="Afrikaans"/>
    <tr:lang code="ak" dir="ltr" value="Akan"/>
    <tr:lang code="am" dir="ltr" value="Amharic"/>
    <tr:lang code="an" dir="ltr" value="Aragonese"/>
    <tr:lang code="ar" dir="rtl" value="Arabic"/>
    <tr:lang code="as" dir="ltr" value="Assamese"/>
    <tr:lang code="av" dir="ltr" value="Avaric"/>
    <tr:lang code="ay" dir="ltr" value="Aymara"/>
    <tr:lang code="az" dir="ltr" value="Azerbaijani"/>
    <tr:lang code="ba" dir="ltr" value="Bashkir"/>
    <tr:lang code="be" dir="ltr" value="Belarusian"/>
    <tr:lang code="bg" dir="ltr" value="Bulgarian"/>
    <tr:lang code="bh" dir="ltr" value="Bihari languages"/>
    <tr:lang code="bi" dir="ltr" value="Bislama"/>
    <tr:lang code="bm" dir="ltr" value="Bambara"/>
    <tr:lang code="bn" dir="ltr" value="Bengali"/>
    <tr:lang code="bo" dir="ltr" value="Tibetan"/>
    <tr:lang code="br" dir="ltr" value="Breton"/>
    <tr:lang code="bs" dir="ltr" value="Bosnian"/>
    <tr:lang code="ca" dir="ltr" value="Catalan; Valencian"/>
    <tr:lang code="ce" dir="ltr" value="Chechen"/>
    <tr:lang code="ch" dir="ltr" value="Chamorro"/>
    <tr:lang code="co" dir="ltr" value="Corsican"/>
    <tr:lang code="cr" dir="ltr" value="Cree"/>
    <tr:lang code="cs" dir="ltr" value="Czech"/>
    <tr:lang code="cu" dir="ltr" value="Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic"/>
    <tr:lang code="cv" dir="ltr" value="Chuvash"/>
    <tr:lang code="cy" dir="ltr" value="Welsh"/>
    <tr:lang code="da" dir="ltr" value="Danish"/>
    <tr:lang code="de" dir="ltr" value="German"/>
    <tr:lang code="dv" dir="rtl" value="Divehi; Dhivehi; Maldivian"/>
    <tr:lang code="dz" dir="ltr" value="Dzongkha"/>
    <tr:lang code="ee" dir="ltr" value="Ewe"/>
    <tr:lang code="el" dir="ltr" value="Greek, Modern (1453-)"/>
    <tr:lang code="en" dir="ltr" value="English"/>
    <tr:lang code="eo" dir="ltr" value="Esperanto"/>
    <tr:lang code="es" dir="ltr" value="Spanish; Castilian"/>
    <tr:lang code="et" dir="ltr" value="Estonian"/>
    <tr:lang code="eu" dir="ltr" value="Basque"/>
    <tr:lang code="fa" dir="rtl" value="Persian"/>
    <tr:lang code="ff" dir="ltr" value="Fulah"/>
    <tr:lang code="fi" dir="ltr" value="Finnish"/>
    <tr:lang code="fj" dir="ltr" value="Fijian"/>
    <tr:lang code="fo" dir="ltr" value="Faroese"/>
    <tr:lang code="fr" dir="ltr" value="French"/>
    <tr:lang code="fy" dir="ltr" value="Western Frisian"/>
    <tr:lang code="ga" dir="ltr" value="Irish"/>
    <tr:lang code="gd" dir="ltr" value="Gaelic; Scottish Gaelic"/>
    <tr:lang code="gl" dir="ltr" value="Galician"/>
    <tr:lang code="gn" dir="ltr" value="Guarani"/>
    <tr:lang code="gu" dir="ltr" value="Gujarati"/>
    <tr:lang code="gv" dir="ltr" value="Manx"/>
    <tr:lang code="ha" dir="rtl" value="Hausa"/>
    <tr:lang code="he" dir="rtl" value="Hebrew"/>
    <tr:lang code="hi" dir="ltr" value="Hindi"/>
    <tr:lang code="ho" dir="ltr" value="Hiri Motu"/>
    <tr:lang code="hr" dir="ltr" value="Croatian"/>
    <tr:lang code="ht" dir="ltr" value="Haitian; Haitian Creole"/>
    <tr:lang code="hu" dir="ltr" value="Hungarian"/>
    <tr:lang code="hy" dir="ltr" value="Armenian"/>
    <tr:lang code="hz" dir="ltr" value="Herero"/>
    <tr:lang code="ia" dir="ltr" value="Interlingua (International Auxiliary Language Association)"/>
    <tr:lang code="id" dir="ltr" value="Indonesian"/>
    <tr:lang code="ie" dir="ltr" value="Interlingue; Occidental"/>
    <tr:lang code="ig" dir="ltr" value="Igbo"/>
    <tr:lang code="ii" dir="ltr" value="Sichuan Yi; Nuosu"/>
    <tr:lang code="ik" dir="ltr" value="Inupiaq"/>
    <tr:lang code="io" dir="ltr" value="Ido"/>
    <tr:lang code="is" dir="ltr" value="Icelandic"/>
    <tr:lang code="it" dir="ltr" value="Italian"/>
    <tr:lang code="iu" dir="ltr" value="Inuktitut"/>
    <tr:lang code="ja" dir="ltr" value="Japanese"/>
    <tr:lang code="jv" dir="ltr" value="Javanese"/>
    <tr:lang code="ka" dir="ltr" value="Georgian"/>
    <tr:lang code="kg" dir="ltr" value="Kongo"/>
    <tr:lang code="ki" dir="ltr" value="Kikuyu; Gikuyu"/>
    <tr:lang code="kj" dir="ltr" value="Kuanyama; Kwanyama"/>
    <tr:lang code="kk" dir="ltr" value="Kazakh"/>
    <tr:lang code="kl" dir="ltr" value="Kalaallisut; Greenlandic"/>
    <tr:lang code="km" dir="ltr" value="Central Khmer"/>
    <tr:lang code="kn" dir="ltr" value="Kannada"/>
    <tr:lang code="ko" dir="ltr" value="Korean"/>
    <tr:lang code="kr" dir="ltr" value="Kanuri"/>
    <tr:lang code="ks" dir="rtl" value="Kashmiri"/>
    <tr:lang code="ku" dir="rtl" value="Kurdish"/>
    <tr:lang code="kv" dir="ltr" value="Komi"/>
    <tr:lang code="kw" dir="ltr" value="Cornish"/>
    <tr:lang code="ky" dir="ltr" value="Kirghiz; Kyrgyz"/>
    <tr:lang code="la" dir="ltr" value="Latin"/>
    <tr:lang code="lb" dir="ltr" value="Luxembourgish; Letzeburgesch"/>
    <tr:lang code="lg" dir="ltr" value="Ganda"/>
    <tr:lang code="li" dir="ltr" value="Limburgan; Limburger; Limburgish"/>
    <tr:lang code="ln" dir="ltr" value="Lingala"/>
    <tr:lang code="lo" dir="ltr" value="Lao"/>
    <tr:lang code="lt" dir="ltr" value="Lithuanian"/>
    <tr:lang code="lu" dir="ltr" value="Luba-Katanga"/>
    <tr:lang code="lv" dir="ltr" value="Latvian"/>
    <tr:lang code="mg" dir="ltr" value="Malagasy"/>
    <tr:lang code="mh" dir="ltr" value="Marshallese"/>
    <tr:lang code="mi" dir="ltr" value="Maori"/>
    <tr:lang code="mk" dir="ltr" value="Macedonian"/>
    <tr:lang code="ml" dir="ltr" value="Malayalam"/>
    <tr:lang code="mn" dir="ltr" value="Mongolian"/>
    <tr:lang code="mr" dir="ltr" value="Marathi"/>
    <tr:lang code="ms" dir="ltr" value="Malay"/>
    <tr:lang code="mt" dir="ltr" value="Maltese"/>
    <tr:lang code="my" dir="ltr" value="Burmese"/>
    <tr:lang code="na" dir="ltr" value="Nauru"/>
    <tr:lang code="nb" dir="ltr" value="Bokmål, Norwegian; Norwegian Bokmål"/>
    <tr:lang code="nd" dir="ltr" value="Ndebele, North; North Ndebele"/>
    <tr:lang code="ne" dir="ltr" value="Nepali"/>
    <tr:lang code="ng" dir="ltr" value="Ndonga"/>
    <tr:lang code="nl" dir="ltr" value="Dutch; Flemish"/>
    <tr:lang code="nn" dir="ltr" value="Norwegian Nynorsk; Nynorsk, Norwegian"/>
    <tr:lang code="no" dir="ltr" value="Norwegian"/>
    <tr:lang code="nr" dir="ltr" value="Ndebele, South; South Ndebele"/>
    <tr:lang code="nv" dir="ltr" value="Navajo; Navaho"/>
    <tr:lang code="ny" dir="ltr" value="Chichewa; Chewa; Nyanja"/>
    <tr:lang code="oc" dir="ltr" value="Occitan (post 1500); Provençal"/>
    <tr:lang code="oj" dir="ltr" value="Ojibwa"/>
    <tr:lang code="om" dir="ltr" value="Oromo"/>
    <tr:lang code="or" dir="ltr" value="Oriya"/>
    <tr:lang code="os" dir="ltr" value="Ossetian; Ossetic"/>
    <tr:lang code="pa" dir="ltr" value="Panjabi; Punjabi"/>
    <tr:lang code="pi" dir="ltr" value="Pali"/>
    <tr:lang code="pl" dir="ltr" value="Polish"/>
    <tr:lang code="ps" dir="rtl" value="Pushto; Pashto"/>
    <tr:lang code="pt" dir="ltr" value="Portuguese"/>
    <tr:lang code="qu" dir="ltr" value="Quechua"/>
    <tr:lang code="rm" dir="ltr" value="Romansh"/>
    <tr:lang code="rn" dir="ltr" value="Rundi"/>
    <tr:lang code="ro" dir="ltr" value="Romanian; Moldavian; Moldovan"/>
    <tr:lang code="ru" dir="ltr" value="Russian"/>
    <tr:lang code="rw" dir="ltr" value="Kinyarwanda"/>
    <tr:lang code="sa" dir="ltr" value="Sanskrit"/>
    <tr:lang code="sc" dir="ltr" value="Sardinian"/>
    <tr:lang code="sd" dir="ltr" value="Sindhi"/>
    <tr:lang code="se" dir="ltr" value="Northern Sami"/>
    <tr:lang code="sg" dir="ltr" value="Sango"/>
    <tr:lang code="si" dir="ltr" value="Sinhala; Sinhalese"/>
    <tr:lang code="sk" dir="ltr" value="Slovak"/>
    <tr:lang code="sl" dir="ltr" value="Slovenian"/>
    <tr:lang code="sm" dir="ltr" value="Samoan"/>
    <tr:lang code="sn" dir="ltr" value="Shona"/>
    <tr:lang code="so" dir="ltr" value="Somali"/>
    <tr:lang code="sq" dir="ltr" value="Albanian"/>
    <tr:lang code="sr" dir="ltr" value="Serbian"/>
    <tr:lang code="ss" dir="ltr" value="Swati"/>
    <tr:lang code="st" dir="ltr" value="Sotho, Southern"/>
    <tr:lang code="su" dir="ltr" value="Sundanese"/>
    <tr:lang code="sv" dir="ltr" value="Swedish"/>
    <tr:lang code="sw" dir="ltr" value="Swahili"/>
    <tr:lang code="ta" dir="ltr" value="Tamil"/>
    <tr:lang code="te" dir="ltr" value="Telugu"/>
    <tr:lang code="tg" dir="ltr" value="Tajik"/>
    <tr:lang code="th" dir="ltr" value="Thai"/>
    <tr:lang code="ti" dir="ltr" value="Tigrinya"/>
    <tr:lang code="tk" dir="ltr" value="Turkmen"/>
    <tr:lang code="tl" dir="ltr" value="Tagalog"/>
    <tr:lang code="tn" dir="ltr" value="Tswana"/>
    <tr:lang code="to" dir="ltr" value="Tonga (Tonga Islands)"/>
    <tr:lang code="tr" dir="ltr" value="Turkish"/>
    <tr:lang code="ts" dir="ltr" value="Tsonga"/>
    <tr:lang code="tt" dir="ltr" value="Tatar"/>
    <tr:lang code="tw" dir="ltr" value="Twi"/>
    <tr:lang code="ty" dir="ltr" value="Tahitian"/>
    <tr:lang code="ug" dir="ltr" value="Uighur; Uyghur"/>
    <tr:lang code="uk" dir="ltr" value="Ukrainian"/>
    <tr:lang code="ur" dir="rtl" value="Urdu"/>
    <tr:lang code="uz" dir="ltr" value="Uzbek"/>
    <tr:lang code="ve" dir="ltr" value="Venda"/>
    <tr:lang code="vi" dir="ltr" value="Vietnamese"/>
    <tr:lang code="vo" dir="ltr" value="Volapük"/>
    <tr:lang code="wa" dir="ltr" value="Walloon"/>
    <tr:lang code="wo" dir="ltr" value="Wolof"/>
    <tr:lang code="xh" dir="ltr" value="Xhosa"/>
    <tr:lang code="yi" dir="rtl" value="Yiddish"/>
    <tr:lang code="yo" dir="ltr" value="Yoruba"/>
    <tr:lang code="za" dir="ltr" value="Zhuang; Chuang"/>
    <tr:lang code="zh" dir="ltr" value="Chinese"/>
    <tr:lang code="zu" dir="ltr" value="Zulu"/>
  </tr:langs>
  
</xsl:stylesheet>