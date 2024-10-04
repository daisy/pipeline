<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
	xmlns:pic="http://schemas.openxmlformats.org/drawingml/2006/picture"
	xmlns:wp="http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
	xmlns:dcterms="http://purl.org/dc/terms/"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties"
	xmlns:ep="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
	xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"
	xmlns:dcmitype="http://purl.org/dc/dcmitype/"
	xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties"
	xmlns:d="org.daisy.pipeline.word_to_dtbook.impl.DaisyClass"
	xmlns="http://www.daisy.org/z3986/2005/dtbook/"
	xmlns:m="http://schemas.openxmlformats.org/officeDocument/2006/math"
	xmlns:mml="http://www.w3.org/1998/Math/MathML"
	exclude-result-prefixes="w pic wp dcterms xsi cp dc a r vt dcmitype xs d m mml">
	
	<xsl:output method="xml" encoding="UTF-8"/>

	<!-- Input file (URI) -->
	<xsl:param name="InputFile" as="xs:string" />
	<!-- Destination folder of .xml file (URI) -->
	<xsl:param name="OutputDir" as="xs:string" />
	
	<!--Implements Image,imagegroup,Note and Notereference-->
	<!--Declaring Global paramaters-->
	<xsl:param name="title" as="xs:string" select="''"/> <!--Holds Documents Title value-->
	<xsl:param name="creator" as="xs:string" select="''"/> <!--Holds Documents creator value-->
	<xsl:param name="publisher" as="xs:string" select="''"/> <!--Holds Documents Publisher value-->
	<xsl:param name="uid" as="xs:string" select="''"/> <!--Holds Document unique id value-->
	<xsl:param name="subject" as="xs:string" select="''"/> <!--Holds Documents Subject value-->
	<xsl:param name="acceptRevisions" as="xs:boolean" select="true()"/>
	<xsl:param name="version" as="xs:string" select="'14'"/> <!--Holds Documents version value-->
	<xsl:param name="pagination" as="xs:string" select="'custom'"/> <!-- Automatic|Custom -->
	<xsl:param name="MasterSub" as="xs:boolean" select="false()"/>
	<xsl:param name="ImageSizeOption" as="xs:string" select="'original'"/> <!-- resize|resample|original -->
	<xsl:param name="DPI" as="xs:integer" select="96"/>
	<xsl:param name="CharacterStyles" as="xs:boolean" select="false()" /> <!-- if true, also convert custom character styles to span with style attribute -->
	<xsl:param name="FootnotesPosition" as="xs:string" select="'end'" /> <!-- page|end| -->
	<xsl:param name="FootnotesLevel" as="xs:integer" select="0" />
	<xsl:param name="FootnotesNumbering" as="xs:string" select="'none'"  />
	<xsl:param name="FootnotesStartValue" as="xs:integer" select="1" />
	<xsl:param name="FootnotesNumberingPrefix" as="xs:string?" select="''"/>
	<xsl:param name="FootnotesNumberingSuffix" as="xs:string?" select="''"/>
	<xsl:param name="Language" as="xs:string?" select="''"/>
	
	<!-- For regression tests comparisons -->
	<xsl:param name="disableDateGeneration" as="xs:boolean" select="false()" />
	<xsl:param name="extractShapes" as="xs:boolean" select="true()" />

	<!-- New object to interact with saxon-->
	<xsl:variable name="myObj" select="d:new($InputFile,$OutputDir,$extractShapes)" />
	
	<!-- Retrieve xml documents from the word file -->
	<xsl:variable name="documentXml"
		as="document-node(element(w:document))"
		select="document(concat('jar:',$InputFile,'!/word/document.xml'))" />
	<xsl:variable name="docPropsAppXml"
		as="document-node(element(ep:Properties))"
		select="document(concat('jar:',$InputFile,'!/docProps/app.xml'))" />
	<xsl:variable name="docPropsCoreXml"
		as="document-node(element(cp:coreProperties))"
		select="document(concat('jar:',$InputFile,'!/docProps/core.xml'))" />
	<xsl:variable name="stylesXml"
		as="document-node(element(w:styles))" 
		select="document(concat('jar:',$InputFile,'!/word/styles.xml'))" />
	<xsl:variable name="numberingXml"
		as="document-node(element(w:numbering))?"
		select="document(concat('jar:',$InputFile,'!/word/numbering.xml'))" />
	<xsl:variable name="footnotesXml"
		as="document-node(element(w:footnotes))?"
		select="document(concat('jar:',$InputFile,'!/word/footnotes.xml'))" />
	<xsl:variable name="endnotesXml"
		as="document-node(element(w:endnotes))?"
		select="document(concat('jar:',$InputFile,'!/word/endnotes.xml'))" />
	
	<xsl:variable name="documentLanguages">
		<!-- Compute runners languages -->
		<xsl:variable name="runnerLanguages">
			<xsl:for-each select="$documentXml//w:body//w:r">
				<xsl:variable name="found">
					<xsl:call-template name="GetRunLanguage">
						<xsl:with-param name="runNode" select="." />
					</xsl:call-template>
				</xsl:variable>
				<lang val="{$found}" />
			</xsl:for-each>
		</xsl:variable>
		<!-- keep uniq runner language
		   Note : we don't count runners occurences here to later weight languages as many runners can be empty texts.
		   We prioritize paragraph based evaluation to deduce document languages importance -->
		<xsl:variable name="uniqRunnerLanguages">
			<xsl:for-each select="$runnerLanguages/*:lang">
				<xsl:variable name="currentVal" select="@*:val"/>
				<xsl:if test="count(preceding-sibling::*:lang[@*:val=$currentVal])=0">
					<lang val="{$currentVal}" />
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<!-- Compute languages of paragraphes -->
		<xsl:variable name="paragraphLanguages">
			<xsl:for-each select="$documentXml//w:body//w:p">
				<xsl:variable name="found">
					<xsl:call-template name="GetParagraphLanguage">
						<xsl:with-param name="paragraphNode" select="." />
					</xsl:call-template>
				</xsl:variable>
				<lang val="{$found}" />
			</xsl:for-each>
			<!-- merge paragraph and runners languages -->
			<xsl:for-each select="$uniqRunnerLanguages/*:lang">
				<lang val="{@*:val}" />
			</xsl:for-each>
		</xsl:variable>
		<!-- <xsl:message terminate="no">progress:Document languages <xsl:value-of select="count($paragraphLanguages/*:lang)" /></xsl:message> -->
		<!-- Count languages -->
		<xsl:variable name="uniqLanguages">
			<xsl:for-each select="$paragraphLanguages/*:lang">
				<xsl:variable name="currentVal" select="@*:val"/>
				<xsl:if test="count(preceding-sibling::*:lang[@*:val=$currentVal])=0">
					<lang val="{$currentVal}"
						count="{count(following-sibling::*:lang[@*:val=$currentVal]) + 1}" />
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:if test="$Language and string-length($Language) &gt; 0">
			<lang val="{@*:val}"/>
		</xsl:if>
		<xsl:for-each select="$uniqLanguages/*:lang">
			<xsl:sort select="@*:count" data-type="number" order="descending"/>
			<xsl:if test="not($Language = @*:val)">
				<lang val="{@*:val}" />
			</xsl:if>
			
		</xsl:for-each>
	</xsl:variable>
	
	<!--Imports all the XSLT-->
	<xsl:import href="Common.xsl"/>
	<!--Implements Table of Contents-->
	<xsl:import href="TOC.xsl"/>
	<!--Implements Paragraph-->
	<!--Implements Table and Frontmatter-->
	<xsl:import href="Common2.xsl"/>
	<!--Implements Lists and levels-->
	<xsl:import href="Common3.xsl"/>
	<!--Implements Maths-->
	<xsl:import href="OOML2MML.xsl"/>
	
	
	<xsl:output method="xml" indent="no" />
	<xsl:variable name="sOperators" as="xs:string"
		select="concat(
				'&#x0021;&#x0022;&#x0028;&#x0029;&#x002B;&#x002C;&#x002D;&#x002F;&#x2AFE;&#x003A;&#x003B;&#x003C;',
				'&#x003D;&#x003E;&#x003F;&#x005B;&#x005C;&#x005D;&#x007B;&#x007C;&#x007D;&#x00A1;&#x00AC;&#x00B1;',
				'&#x00B7;&#x00BF;&#x00D7;&#x00F7;&#x2000;&#x2001;&#x2002;&#x2003;&#x2004;&#x2005;&#x2006;&#x2009;',
				'&#x200A;&#x2010;&#x2012;&#x2013;&#x2014;&#x2016;&#x2020;&#x2021;&#x2022;&#x2024;&#x2025;&#x2026;',
				'&#x203C;&#x2040;&#x204E;&#x204F;&#x2050;&#x205f;&#x2061;&#x2062;&#x2063;&#x2140;&#x2190;&#x2191;',
				'&#x2192;&#x2193;&#x2194;&#x2195;&#x2196;&#x2197;&#x2198;&#x2199;&#x219A;&#x219B;&#x219C;&#x219D;',
				'&#x219E;&#x219F;&#x21A0;&#x21A1;&#x21A2;&#x21A3;&#x21A4;&#x21A5;&#x21A6;&#x21A7;&#x21A8;&#x21A9;',
				'&#x21AA;&#x21AB;&#x21AC;&#x21AD;&#x21AE;&#x21AF;&#x21B0;&#x21B1;&#x21B2;&#x21B3;&#x21B6;&#x21B7;',
				'&#x21BA;&#x21BB;&#x21BC;&#x21BD;&#x21BE;&#x21BF;&#x21C0;&#x21C1;&#x21C2;&#x21C3;&#x21C4;&#x21C5;',
				'&#x21C6;&#x21C7;&#x21C8;&#x21C9;&#x21CA;&#x21CB;&#x21CC;&#x21CD;&#x21CE;&#x21CF;&#x21D0;&#x21D1;',
				'&#x21D2;&#x21D3;&#x21D4;&#x21D5;&#x21D6;&#x21D7;&#x21D8;&#x21D9;&#x21DA;&#x21DB;&#x21DC;&#x21DD;',
				'&#x21DE;&#x21DF;&#x21E0;&#x21E1;&#x21E2;&#x21E3;&#x21E4;&#x21E5;&#x21E6;&#x21E7;&#x21E8;&#x21E9;',
				'&#x21F4;&#x21F5;&#x21F6;&#x21F7;&#x21F8;&#x21F9;&#x21FA;&#x21FB;&#x21FC;&#x21FD;&#x21FE;&#x21FF;',
				'&#x2200;&#x2201;&#x2202;&#x2203;&#x2204;&#x2206;&#x2207;&#x2208;&#x2209;&#x220A;&#x220B;&#x220C;',
				'&#x220D;&#x220F;&#x2210;&#x2211;&#x2212;&#x2213;&#x2214;&#x2215;&#x2216;&#x2217;&#x2218;&#x2219;',
				'&#x221A;&#x221B;&#x221C;&#x221D;&#x2223;&#x2224;&#x2225;&#x2226;&#x2227;&#x2228;&#x2229;&#x222A;',
				'&#x222B;&#x222C;&#x222D;&#x222E;&#x222F;&#x2230;&#x2231;&#x2232;&#x2233;&#x2234;&#x2235;&#x2236;',
				'&#x2237;&#x2238;&#x2239;&#x223A;&#x223B;&#x223C;&#x223D;&#x223E;&#x2240;&#x2241;&#x2242;&#x2243;',
				'&#x2244;&#x2245;&#x2246;&#x2247;&#x2248;&#x2249;&#x224A;&#x224B;&#x224C;&#x224D;&#x224E;&#x224F;',
				'&#x2250;&#x2251;&#x2252;&#x2253;&#x2254;&#x2255;&#x2256;&#x2257;&#x2258;&#x2259;&#x225A;&#x225B;',
				'&#x225C;&#x225D;&#x225E;&#x225F;&#x2260;&#x2261;&#x2262;&#x2263;&#x2264;&#x2265;&#x2266;&#x2267;',
				'&#x2268;&#x2269;&#x226A;&#x226B;&#x226C;&#x226D;&#x226E;&#x226F;&#x2270;&#x2271;&#x2272;&#x2273;',
				'&#x2274;&#x2275;&#x2276;&#x2277;&#x2278;&#x2279;&#x227A;&#x227B;&#x227C;&#x227D;&#x227E;&#x227F;',
				'&#x2280;&#x2281;&#x2282;&#x2283;&#x2284;&#x2285;&#x2286;&#x2287;&#x2288;&#x2289;&#x228A;&#x228B;',
				'&#x228C;&#x228D;&#x228E;&#x228F;&#x2290;&#x2291;&#x2292;&#x2293;&#x2294;&#x2295;&#x2296;&#x2297;',
				'&#x2298;&#x2299;&#x229A;&#x229B;&#x229C;&#x229D;&#x229E;&#x229F;&#x22A0;&#x22A1;&#x22A2;&#x22A3;',
				'&#x22A5;&#x22A6;&#x22A7;&#x22A8;&#x22A9;&#x22AA;&#x22AB;&#x22AC;&#x22AD;&#x22AE;&#x22AF;&#x22B0;',
				'&#x22B1;&#x22B2;&#x22B3;&#x22B4;&#x22B5;&#x22B6;&#x22B7;&#x22B8;&#x22B9;&#x22BA;&#x22BB;&#x22BC;',
				'&#x22BD;&#x22C0;&#x22C1;&#x22C2;&#x22C3;&#x22C4;&#x22C5;&#x22C6;&#x22C7;&#x22C8;&#x22C9;&#x22CA;',
				'&#x22CB;&#x22CC;&#x22CD;&#x22CE;&#x22CF;&#x22D0;&#x22D1;&#x22D2;&#x22D3;&#x22D4;&#x22D5;&#x22D6;',
				'&#x22D7;&#x22D8;&#x22D9;&#x22DA;&#x22DB;&#x22DC;&#x22DD;&#x22DE;&#x22DF;&#x22E0;&#x22E1;&#x22E2;',
				'&#x22E3;&#x22E4;&#x22E5;&#x22E6;&#x22E7;&#x22E8;&#x22E9;&#x22EA;&#x22EB;&#x22EC;&#x22ED;&#x22EE;',
				'&#x22EF;&#x22F0;&#x22F1;&#x22F2;&#x22F3;&#x22F4;&#x22F5;&#x22F6;&#x22F7;&#x22F8;&#x22F9;&#x22FA;',
				'&#x22FB;&#x22FC;&#x22FD;&#x22FE;&#x22FF;&#x2305;&#x2306;&#x2308;&#x2309;&#x230A;&#x230B;&#x231C;',
				'&#x231D;&#x231E;&#x231F;&#x2322;&#x2323;&#x2329;&#x232A;&#x233D;&#x233F;&#x23B0;&#x23B1;&#x25B2;',
				'&#x25B3;&#x25B4;&#x25B5;&#x25B6;&#x25B7;&#x25B8;&#x25B9;&#x25BC;&#x25BD;&#x25BE;&#x25BF;&#x25C0;',
				'&#x25C1;&#x25C2;&#x25C3;&#x25C4;&#x25C5;&#x25CA;&#x25CB;&#x25E6;&#x25EB;&#x25EC;&#x25F8;&#x25F9;',
				'&#x25FA;&#x25FB;&#x25FC;&#x25FD;&#x25FE;&#x25FF;&#x2605;&#x2606;&#x2772;&#x2773;&#x27D1;&#x27D2;',
				'&#x27D3;&#x27D4;&#x27D5;&#x27D6;&#x27D7;&#x27D8;&#x27D9;&#x27DA;&#x27DB;&#x27DC;&#x27DD;&#x27DE;',
				'&#x27DF;&#x27E0;&#x27E1;&#x27E2;&#x27E3;&#x27E4;&#x27E5;&#x27E6;&#x27E7;&#x27E8;&#x27E9;&#x27EA;',
				'&#x27EB;&#x27F0;&#x27F1;&#x27F2;&#x27F3;&#x27F4;&#x27F5;&#x27F6;&#x27F7;&#x27F8;&#x27F9;&#x27FA;',
				'&#x27FB;&#x27FC;&#x27FD;&#x27FE;&#x27FF;&#x2900;&#x2901;&#x2902;&#x2903;&#x2904;&#x2905;&#x2906;',
				'&#x2907;&#x2908;&#x2909;&#x290A;&#x290B;&#x290C;&#x290D;&#x290E;&#x290F;&#x2910;&#x2911;&#x2912;',
				'&#x2913;&#x2914;&#x2915;&#x2916;&#x2917;&#x2918;&#x2919;&#x291A;&#x291B;&#x291C;&#x291D;&#x291E;',
				'&#x291F;&#x2920;&#x2921;&#x2922;&#x2923;&#x2924;&#x2925;&#x2926;&#x2927;&#x2928;&#x2929;&#x292A;',
				'&#x292B;&#x292C;&#x292D;&#x292E;&#x292F;&#x2930;&#x2931;&#x2932;&#x2933;&#x2934;&#x2935;&#x2936;',
				'&#x2937;&#x2938;&#x2939;&#x293A;&#x293B;&#x293C;&#x293D;&#x293E;&#x293F;&#x2940;&#x2941;&#x2942;',
				'&#x2943;&#x2944;&#x2945;&#x2946;&#x2947;&#x2948;&#x2949;&#x294A;&#x294B;&#x294C;&#x294D;&#x294E;',
				'&#x294F;&#x2950;&#x2951;&#x2952;&#x2953;&#x2954;&#x2955;&#x2956;&#x2957;&#x2958;&#x2959;&#x295A;',
				'&#x295B;&#x295C;&#x295D;&#x295E;&#x295F;&#x2960;&#x2961;&#x2962;&#x2963;&#x2964;&#x2965;&#x2966;',
				'&#x2967;&#x2968;&#x2969;&#x296A;&#x296B;&#x296C;&#x296D;&#x296E;&#x296F;&#x2970;&#x2971;&#x2972;',
				'&#x2973;&#x2974;&#x2975;&#x2976;&#x2977;&#x2978;&#x2979;&#x297A;&#x297B;&#x297C;&#x297D;&#x297E;',
				'&#x297F;&#x2980;&#x2982;&#x2983;&#x2984;&#x2985;&#x2986;&#x2987;&#x2988;&#x2989;&#x298A;&#x298B;',
				'&#x298C;&#x298D;&#x298E;&#x298F;&#x2990;&#x2991;&#x2992;&#x2993;&#x2994;&#x2995;&#x2996;&#x2997;',
				'&#x2998;&#x2999;&#x299A;&#x29B6;&#x29B7;&#x29B8;&#x29B9;&#x29C0;&#x29C1;&#x29C4;&#x29C5;&#x29C6;',
				'&#x29C7;&#x29C8;&#x29CE;&#x29CF;&#x29D0;&#x29D1;&#x29D2;&#x29D3;&#x29D4;&#x29D5;&#x29D6;&#x29D7;',
				'&#x29D8;&#x29D9;&#x29DA;&#x29DB;&#x29DF;&#x29E1;&#x29E2;&#x29E3;&#x29E4;&#x29E5;&#x29E6;&#x29EB;',
				'&#x29F4;&#x29F5;&#x29F6;&#x29F7;&#x29F8;&#x29F9;&#x29FA;&#x29FB;&#x29FC;&#x29FD;&#x29FE;&#x29FF;',
				'&#x2A00;&#x2A01;&#x2A02;&#x2A03;&#x2A04;&#x2A05;&#x2A06;&#x2A07;&#x2A08;&#x2A09;&#x2A0A;&#x2A0B;',
				'&#x2A0C;&#x2A0D;&#x2A0E;&#x2A0F;&#x2A10;&#x2A11;&#x2A12;&#x2A13;&#x2A14;&#x2A15;&#x2A16;&#x2A17;',
				'&#x2A18;&#x2A19;&#x2A1A;&#x2A1B;&#x2A1C;&#x2A1D;&#x2A1E;&#x2A1F;&#x2A20;&#x2A21;&#x2A22;&#x2A23;',
				'&#x2A24;&#x2A25;&#x2A26;&#x2A27;&#x2A28;&#x2A29;&#x2A2A;&#x2A2B;&#x2A2C;&#x2A2D;&#x2A2E;&#x2A2F;',
				'&#x2A30;&#x2A31;&#x2A32;&#x2A33;&#x2A34;&#x2A35;&#x2A36;&#x2A37;&#x2A38;&#x2A39;&#x2A3A;&#x2A3B;',
				'&#x2A3C;&#x2A3D;&#x2A3E;&#x2A3F;&#x2A40;&#x2A41;&#x2A42;&#x2A43;&#x2A44;&#x2A45;&#x2A46;&#x2A47;',
				'&#x2A48;&#x2A49;&#x2A4A;&#x2A4B;&#x2A4C;&#x2A4D;&#x2A4E;&#x2A4F;&#x2A50;&#x2A51;&#x2A52;&#x2A53;',
				'&#x2A54;&#x2A55;&#x2A56;&#x2A57;&#x2A58;&#x2A59;&#x2A5A;&#x2A5B;&#x2A5C;&#x2A5D;&#x2A5E;&#x2A5F;',
				'&#x2A60;&#x2A61;&#x2A62;&#x2A63;&#x2A64;&#x2A65;&#x2A66;&#x2A67;&#x2A68;&#x2A69;&#x2A6A;&#x2A6B;',
				'&#x2A6C;&#x2A6D;&#x2A6E;&#x2A6F;&#x2A70;&#x2A71;&#x2A72;&#x2A73;&#x2A74;&#x2A75;&#x2A76;&#x2A77;',
				'&#x2A78;&#x2A79;&#x2A7A;&#x2A7B;&#x2A7C;&#x2A7D;&#x2A7E;&#x2A7F;&#x2A80;&#x2A81;&#x2A82;&#x2A83;',
				'&#x2A84;&#x2A85;&#x2A86;&#x2A87;&#x2A88;&#x2A89;&#x2A8A;&#x2A8B;&#x2A8C;&#x2A8D;&#x2A8E;&#x2A8F;',
				'&#x2A90;&#x2A91;&#x2A92;&#x2A93;&#x2A94;&#x2A95;&#x2A96;&#x2A97;&#x2A98;&#x2A99;&#x2A9A;&#x2A9B;',
				'&#x2A9C;&#x2A9D;&#x2A9E;&#x2A9F;&#x2AA0;&#x2AA1;&#x2AA2;&#x2AA3;&#x2AA4;&#x2AA5;&#x2AA6;&#x2AA7;',
				'&#x2AA8;&#x2AA9;&#x2AAA;&#x2AAB;&#x2AAC;&#x2AAD;&#x2AAE;&#x2AAF;&#x2AB0;&#x2AB1;&#x2AB2;&#x2AB3;',
				'&#x2AB4;&#x2AB5;&#x2AB6;&#x2AB7;&#x2AB8;&#x2AB9;&#x2ABA;&#x2ABB;&#x2ABC;&#x2ABD;&#x2ABE;&#x2ABF;',
				'&#x2AC0;&#x2AC1;&#x2AC2;&#x2AC3;&#x2AC4;&#x2AC5;&#x2AC6;&#x2AC7;&#x2AC8;&#x2AC9;&#x2ACA;&#x2ACB;',
				'&#x2ACC;&#x2ACD;&#x2ACE;&#x2ACF;&#x2AD0;&#x2AD1;&#x2AD2;&#x2AD3;&#x2AD4;&#x2AD5;&#x2AD6;&#x2AD7;',
				'&#x2AD8;&#x2AD9;&#x2ADA;&#x2ADB;&#x2ADC;&#x2ADD;&#x2ADE;&#x2ADF;&#x2AE0;&#x2AE2;&#x2AE3;&#x2AE4;',
				'&#x2AE5;&#x2AE6;&#x2AE7;&#x2AE8;&#x2AE9;&#x2AEA;&#x2AEB;&#x2AEC;&#x2AED;&#x2AEE;&#x2AEF;&#x2AF0;',
				'&#x2AF2;&#x2AF3;&#x2AF4;&#x2AF5;&#x2AF6;&#x2AF7;&#x2AF8;&#x2AF9;&#x2AFA;&#x2AFB;&#x2AFC;&#x2AFD;')" />
	<!-- A string of '-'s repeated exactly as many times as the operators above -->
	<xsl:variable name="sMinuses" as="xs:string">
		<xsl:call-template name="SRepeatChar">
			<xsl:with-param name="cchRequired" select="string-length($sOperators)" />
			<xsl:with-param name="ch" select="'-'" />
		</xsl:call-template>
	</xsl:variable>
	<!-- Every single unicode character that is recognized by OMML as a number -->
	<xsl:variable name="sNumbers" as="xs:string" select="'0123456789'"/>
	<!-- A string of '0's repeated exactly as many times as the list of numbers above -->
	<xsl:variable name="sZeros" as="xs:string">
		<xsl:call-template name="SRepeatChar">
			<xsl:with-param name="cchRequired" select="string-length($sNumbers)" />
			<xsl:with-param name="ch" select="'0'" />
		</xsl:call-template>
	</xsl:variable>
	<!--Extending the DTD to support MathML-->
	
	<!-- Conversion starts here -->
	<xsl:template name="main">
		<!--This Xslt is Adding meta elements in Dtbook head element 
			It is also calling templates Frontmatter, Bodymatter and Rearmatter-->
		<!--Adding dtbook element-->
		<xsl:result-document encoding="utf-8" indent="yes" >
			<!--<xsl:text disable-output-escaping="yes">&lt;?xml-stylesheet href="dtbookbasic.css" type="text/css"?&gt;</xsl:text>-->
			<xsl:text disable-output-escaping="yes">&lt;!DOCTYPE dtbook PUBLIC '-//NISO//DTD dtbook 2005-3//EN' 'http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd'</xsl:text>
			<xsl:if test="count($documentXml//m:*) &gt; 0">
				<xsl:text disable-output-escaping="yes">[
	&lt;!ENTITY % MATHML.prefixed "INCLUDE" &gt;
	&lt;!ENTITY % MATHML.prefix "mml"&gt;
	&lt;!ENTITY % Schema.prefix "sch"&gt;
	&lt;!ENTITY % XLINK.prefix "xlp"&gt;
	&lt;!ENTITY % MATHML.Common.attrib
		"xlink:href    CDATA       #IMPLIED
		xlink:type     CDATA       #IMPLIED
		class          CDATA       #IMPLIED
		style          CDATA       #IMPLIED
		id             ID          #IMPLIED
		xref           IDREF       #IMPLIED
		other          CDATA       #IMPLIED
		xmlns:dtbook   CDATA       #FIXED
			'http://www.daisy.org/z3986/2005/dtbook/'
		dtbook:smilref CDATA       #IMPLIED"&gt;
	&lt;!ENTITY % mathML2 PUBLIC '-//W3C//DTD MathML 2.0//EN' 'http://www.w3.org/Math/DTD/mathml2/mathml2.dtd'&gt;
	%mathML2;
	&lt;!ENTITY % externalFlow "| mml:math"&gt;
	&lt;!ENTITY % externalNamespaces "xmlns:mml CDATA #FIXED
		'http://www.w3.org/1998/Math/MathML'" &gt;
]</xsl:text>
			</xsl:if>
			
			<xsl:text disable-output-escaping="yes"> &gt;&#10;</xsl:text>
			<dtbook version="2005-3">
				<xsl:if test="count($documentXml//m:*) &gt; 0">
					<xsl:namespace name="mml" select="'http://www.w3.org/1998/Math/MathML'" />
				</xsl:if>
				<!-- <xsl:message terminate="no">progress:Parsing document languages</xsl:message> -->
				
				<xsl:attribute name="xml:lang">
					<xsl:value-of select="$documentLanguages/*:lang[1]/@*:val"/>
				</xsl:attribute>
				<xsl:variable name="documentDate" as="xs:string" select="$docPropsCoreXml//cp:coreProperties/dcterms:modified" />
				<xsl:variable name="documentSubject" as="xs:string" select="d:DocPropSubject($myObj)" />
				<xsl:variable name="documentDescription" as="xs:string" select="d:DocPropDescription($myObj)" />
				<!--Adding head element-->
				<head>
					<!--Adding head element-->
					<!--Auto variable holds autogenerated UID value-->
					<xsl:variable name="Auto" as="xs:string" select="concat('AUTO-UID-',d:GenerateId())" />
					<!--Choose block for checking whether user has entered the UID value or not-->
					<xsl:choose>
						<xsl:when test="string-length($uid) = 0">
							<meta name="dtb:uid" content="{$Auto}"/>
						</xsl:when>
						<xsl:otherwise>
							<meta name="dtb:uid" content="{$uid}"/>
						</xsl:otherwise>
					</xsl:choose>
					<meta name="dtb:generator" content="DAISY Pipeline 2 word-to-dtbook 1.0.0"/>
					<!--Choose block for checking whether user has entered the Title of the document or not-->
					<xsl:choose>
						<!--Taking Document Title value from core.xml-->
						<xsl:when test="string-length($title) = 0">
							<meta name="dc:Title" content="{$docPropsCoreXml//cp:coreProperties/dc:title/text()}"/>
						</xsl:when>
						<!--Taking the Title value entered by the user-->
						<xsl:otherwise>
							<meta name="dc:Title" content="{$title}"/>
						</xsl:otherwise>
					</xsl:choose>
					
					<!--Choose block for checking whether user has entered the Creator of the document or not-->
					<xsl:choose>
						<!--Taking Document Creator value from core.xml-->
						<xsl:when test="string-length($creator) = 0">
							<!--Note : Creators are separated with semi-colons in the property-->
							<xsl:variable name="creatorFromPackage" select="$docPropsCoreXml//cp:coreProperties/dc:creator/text()" />
							<xsl:if test="string-length($creatorFromPackage)!=0">
								<meta name="dc:Creator" content="{$creatorFromPackage}"/>
							</xsl:if>
						</xsl:when>
						<!--Taking the Creator value entered by the user-->
						<xsl:otherwise>
							<meta name="dc:Creator" content="{$creator}"/>
						</xsl:otherwise>
					</xsl:choose>
					
					<!--Taking the value of dc:date from core.xml file-->
					<xsl:if test="string-length($documentDate)!=0 and not($disableDateGeneration)">
						<meta name="dc:Date" content="{substring-before($documentDate,'T')}"/>
					</xsl:if>
					<!--Choose block for checking whether user has entered the Publisher of the document or not-->
					<xsl:choose>
						<!--Taking Document publisher value from app.xml-->
						<xsl:when test="string-length($publisher) = 0">
							<xsl:variable name="publisherFromPackage" select="$docPropsAppXml//ep:Properties/ep:Company/text()" />
							<xsl:if test="string-length($publisherFromPackage)!=0">
								<meta name="dc:Publisher" content="{$publisherFromPackage}"/>
							</xsl:if>
						</xsl:when>
						<!--Taking the Publisher value entered by the user-->
						<xsl:otherwise>
							<meta name="dc:Publisher" content="{$publisher}"/>
						</xsl:otherwise>
					</xsl:choose>
					
					<!--Taking the value of dc:subject from core.xml file-->
					<xsl:choose>
						<xsl:when test="string-length($subject)!=0">
							<meta name="dc:Subject" content="{$subject}"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="string-length($documentSubject)!=0">
								<meta name="dc:Subject" content="{$documentSubject}"/>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
					<!--Taking the value of dc:description from core.xml file-->
					<xsl:if test="string-length($documentDescription)!=0">
						<meta name="dc:Description" content="{$documentDescription}"/>
					</xsl:if>
					<!--Choose block for checking whether user has entered the UID value or not for implementing dc:identifier meta tag-->
					<xsl:choose>
						<xsl:when test="string-length($uid) = 0">
							<meta name="dc:Identifier" content="{$Auto}"/>
						</xsl:when>
						<xsl:otherwise>
							<meta name="dc:Identifier" content="{$uid}"/>
						</xsl:otherwise>
					</xsl:choose>
					<xsl:for-each select="$documentLanguages/*:lang">
						<xsl:sequence select="d:sink(d:AddLanguage($myObj,@val))" /> <!-- empty -->
						<meta name="dc:Language" content="{@val}"/>
					</xsl:for-each>
					
					<!--End of Head element-->
				</head>
				<!--Starting Book Element-->
				<book showin="blp">
					<xsl:if test="not($MasterSub)">
						<!-- <xsl:message terminate="no">progress:Checking for page styles</xsl:message> -->
						<xsl:for-each select="$documentXml//w:document/w:body/node()">
							<xsl:if test="self::w:p">
								<xsl:for-each select="w:pPr/w:pStyle[substring(@w:val,1,11)='Frontmatter']">
									<xsl:if test="d:PushPageStyle($myObj,@w:val)"/>
								</xsl:for-each>
								<xsl:for-each select="w:pPr/w:pStyle[substring(@w:val,1,10)='Bodymatter']">
									<xsl:if test="d:PushPageStyle($myObj,@w:val)"/>
								</xsl:for-each>
								<xsl:for-each select="w:pPr/w:pStyle[substring(@w:val,1,10)='Rearmatter']">
									<xsl:if test="d:PushPageStyle($myObj,@w:val)"/>
								</xsl:for-each>
								<xsl:for-each select="w:r/w:rPr/w:rStyle[substring(@w:val,1,11)='Frontmatter']">
									<xsl:if test="d:PushPageStyle($myObj,@w:val)"/>
								</xsl:for-each>
								<xsl:for-each select="w:r/w:rPr/w:rStyle[substring(@w:val,1,10)='Bodymatter']">
									<xsl:if test="d:PushPageStyle($myObj,@w:val)"/>
								</xsl:for-each>
								<xsl:for-each select="w:r/w:rPr/w:rStyle[substring(@w:val,1,10)='Rearmatter']">
									<xsl:if test="d:PushPageStyle($myObj,@w:val)"/>
								</xsl:for-each>
								<xsl:sequence select="d:IncrementCheckingParagraph($myObj)"/> <!-- empty -->
							</xsl:if>
						</xsl:for-each>
						
						<xsl:if test="d:IsInvalidPageStylesSequence($myObj)='true'">
							<xsl:message terminate="yes">
								<xsl:value-of select="d:GetPageStylesErrors($myObj)"/>
							</xsl:message>
						</xsl:if>
					</xsl:if>
					<!-- <xsl:message terminate="no">progress:Starting conversion of <xsl:value-of select="count($documentXml//w:body/*)"/> elements</xsl:message> -->
					<!-- Calling Frontmatter template and passing parameters Title and Creator for doctitle and docpublisher-->
					<!-- <xsl:message terminate="no">progress:Building the frontmatter</xsl:message> -->
					<frontmatter>
						<doctitle>
							<xsl:choose>
								<!--Taking Document Title value from core.xml-->
								<xsl:when test="string-length($title) = 0">
									<xsl:value-of select="$docPropsCoreXml//cp:coreProperties/dc:title"/>
								</xsl:when>
								<!--Taking the Title value entered by the user-->
								<xsl:otherwise>
									<xsl:value-of select="$title"/>
								</xsl:otherwise>
							</xsl:choose>
						</doctitle>
						<docauthor>
							<xsl:choose>
								<!--Taking Document creator value from core.xml-->
								<xsl:when test="string-length($creator) = 0">
									<xsl:value-of select="$docPropsCoreXml//cp:coreProperties/dc:creator"/>
								</xsl:when>
								<!--Taking the Creator value entered by the user-->
								<xsl:otherwise>
									<xsl:value-of select="$creator"/>
								</xsl:otherwise>
							</xsl:choose>
						</docauthor>
						<!-- Only launch if there is a bodymatter style set on a pragraph that is not the first one -->
						<xsl:if test="
							count($documentXml//w:document/w:body/w:p[position() &gt; 1]/w:pPr/w:pStyle[substring(@w:val,1,10)='Bodymatter'])=1
							or count($documentXml//w:document/w:body/w:p/w:r[position() &gt; 1]/w:rPr/w:rStyle[substring(@w:val,1,10)='Bodymatter'])=1
								">
							<!-- <xsl:message terminate="no">progress:Adding frontmatter content found in the document</xsl:message> -->
							<xsl:call-template name="Matter">
								<xsl:with-param name="acceptRevisions" select="$acceptRevisions"/>
								<xsl:with-param name="version" select="$version"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="masterSub" select="$MasterSub"/>
								<xsl:with-param name="sOperators" select="$sOperators"/>
								<xsl:with-param name="sMinuses" select="$sMinuses"/>
								<xsl:with-param name="sNumbers" select="$sNumbers"/>
								<xsl:with-param name="sZeros" select="$sZeros"/>
								<xsl:with-param name="imgOption" select="$ImageSizeOption"/>
								<xsl:with-param name="dpi" select="$DPI"/>
								<xsl:with-param name="charStyles" select="$CharacterStyles"/>
								<xsl:with-param name="matterType" select="'Frontmatter'" />
							</xsl:call-template>
						</xsl:if>
					</frontmatter>
					<!-- <xsl:message terminate="no">progress:Building the bodymatter</xsl:message> -->
					<!--Calling Bodymatter template-->
					<bodymatter id="bodymatter_0001">
						<xsl:call-template name="Matter">
							<xsl:with-param name="acceptRevisions" select="$acceptRevisions"/>
							<xsl:with-param name="version" select="$version"/>
							<xsl:with-param name="pagination" select="$pagination"/>
							<xsl:with-param name="masterSub" select="$MasterSub"/>
							<xsl:with-param name="sOperators" select="$sOperators"/>
							<xsl:with-param name="sMinuses" select="$sMinuses"/>
							<xsl:with-param name="sNumbers" select="$sNumbers"/>
							<xsl:with-param name="sZeros" select="$sZeros"/>
							<xsl:with-param name="imgOption" select="$ImageSizeOption"/>
							<xsl:with-param name="dpi" select="$DPI"/>
							<xsl:with-param name="charStyles" select="$CharacterStyles"/>
							<xsl:with-param name="matterType" select="'Bodymatter'" />
						</xsl:call-template>
					</bodymatter>
					<!--Calling Rearmatter template-->
					<!--NP 20220427 - launch only if a Rearmatter daisy style is found to avoid empty rearmatter -->
					<xsl:if test="(
							count($documentXml//w:document/w:body/w:p/w:pPr/w:pStyle[substring(@w:val,1,10)='Rearmatter'])=1
							or count($documentXml//w:document/w:body/w:p/w:r/w:rPr/w:rStyle[substring(@w:val,1,10)='Rearmatter'])=1
							or count($documentXml//w:document/w:body/w:p/w:r/w:rPr/w:rStyle[@w:val='EndnoteReference']) &gt; 0
							or count($documentXml//w:document/w:body/w:p/w:r/w:endnoteReference)  &gt; 0
						)">
						<rearmatter>
							<xsl:if test="count(
									$documentXml//w:document/w:body/w:p/w:r[
										./w:rPr/w:rStyle[@w:val='EndnoteReference'] 
										or ./w:endnoteReference
									]
								)  &gt; 0">
								<!-- <xsl:message terminate="no">progress:Inserting endnotes in the rearmatter</xsl:message> -->
								<level1>
									<!--Checking if any elements should be translated to the rearmatter-->
									<!--Otherwise Traversing through document.xml file and passing the Endnote id to the Note template.-->
									<xsl:for-each select="(
											$documentXml//w:document/w:body/w:p/w:r[
												./w:rPr/w:rStyle[@w:val='EndnoteReference'] 
												or ./w:endnoteReference
											]
										)">
										<xsl:variable name="endNoteId" as="xs:integer" select="./w:endnoteReference/@w:id"/>
										<xsl:if test="$endNoteId &gt; 0">
											<note>
												<!--Creating attribute ID for Note element-->
												<xsl:attribute name="id">
													<xsl:value-of select="concat('endnote-',$endNoteId)"/>
												</xsl:attribute>
												<!--Creating attribute class for Note element-->
												<xsl:attribute name="class">
													<xsl:value-of select="'Endnote'"/>
												</xsl:attribute>
												<!--Travering each w:endnote element in endnote.xml file-->
												<xsl:for-each select="$endnotesXml//w:endnotes/w:endnote">
													<!--Checks for matching Id-->
													<xsl:if test="@w:id=$endNoteId">
														<!--Travering each element inside w:endnote in endnote.xml file-->
														<xsl:for-each select="./node()">
															<!--Checking for Paragraph element-->
															<xsl:if test="self::w:p">
																<xsl:call-template name="ParagraphStyle">
																	<xsl:with-param name="version" select="$version"/>
																	<xsl:with-param name="flagNote" select="'endnote'"/>
																	<xsl:with-param name="checkid" select="$endNoteId + 1"/>
																	<xsl:with-param name="sOperators" select="$sOperators"/>
																	<xsl:with-param name="sMinuses" select="$sMinuses"/>
																	<xsl:with-param name="sNumbers" select="$sNumbers"/>
																	<xsl:with-param name="sZeros" select="$sZeros"/>
																	<xsl:with-param name="characterparaStyle" select="false()"/>
																</xsl:call-template>
															</xsl:if>
														</xsl:for-each>
														<xsl:sequence select="d:sink(d:InitializeNoteFlag($myObj))"/> <!-- empty -->
													</xsl:if>
												</xsl:for-each>
											</note>
										</xsl:if>
									</xsl:for-each>
								</level1>
							</xsl:if>
							<!-- <xsl:message terminate="no">progress:Adding any rearmatter content found in the document</xsl:message> -->
							<xsl:call-template name="Matter">
								<xsl:with-param name="acceptRevisions" select="$acceptRevisions"/>
								<xsl:with-param name="version" select="$version"/>
								<xsl:with-param name="pagination" select="$pagination"/>
								<xsl:with-param name="masterSub" select="$MasterSub"/>
								<xsl:with-param name="sOperators" select="$sOperators"/>
								<xsl:with-param name="sMinuses" select="$sMinuses"/>
								<xsl:with-param name="sNumbers" select="$sNumbers"/>
								<xsl:with-param name="sZeros" select="$sZeros"/>
								<xsl:with-param name="imgOption" select="$ImageSizeOption"/>
								<xsl:with-param name="dpi" select="$DPI"/>
								<xsl:with-param name="charStyles" select="$CharacterStyles"/>
								<xsl:with-param name="matterType" select="'Rearmatter'" />
							</xsl:call-template>
						</rearmatter>
					</xsl:if>
				</book>
			</dtbook>
		</xsl:result-document>
		<xsl:sequence select="d:End($myObj)" />
	</xsl:template>
</xsl:stylesheet>
