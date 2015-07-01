<?xml version="1.0" encoding="utf-8"?>
<!-- Rules generated on: 2009-11-10 14:48:28 -->
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
	<sch:title>DTBook 2005-3 Schematron basic tests</sch:title>
	<sch:ns prefix="dtb" uri="http://www.daisy.org/z3986/2005/dtbook/"/>
	<!-- Rule 1: Disallowed element: note 
	<sch:pattern name="no_note" id="no_note">
		<sch:rule context="dtb:note">
			<sch:assert test="false()">[Rule 1] No 'note'</sch:assert>
		</sch:rule>
	</sch:pattern>
	-->
	<!-- Rule 2: Disallowed element: table -->
	<sch:pattern name="no_table" id="no_table">
		<sch:rule context="dtb:table">
			<sch:assert test="false()">[Rule 2] No 'table'</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- Rule 3: Document contains an unsupported language -->
	<sch:pattern name="xml_lang" id="xml_lang">
		<sch:rule context="*[@xml:lang]">
			<sch:assert test="@xml:lang='sv' or @xml:lang='sv-SE' or @xml:lang='en' or @xml:lang='en-US' or @xml:lang='en-GB' or @xml:lang='no' or @xml:lang='de' or @xml:lang='fr' or @xml:lang='fi'">[Rule 3] Unsupported language.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- Rule 4: Only allowed elements in level containing a TOC list -->
	<sch:pattern name="check_toc" id="check_toc">
		<sch:rule context="dtb:level1[dtb:list[@class='toc']]">
			<sch:assert test="count(dtb:pagenum|dtb:h1|dtb:list)=count(*[descendant::text()[normalize-space(.)!='']]) and count(dtb:list)=1">[Rule 4] Disallowed elements in level containing a TOC list.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- Rule 5: level1 must have text content -->
	<sch:pattern name="check_level1" id="check_level1">
		<sch:rule context="dtb:level1">
			<sch:assert test="descendant::text()">[Rule 5] level1 must have text content.</sch:assert>
		</sch:rule>
	</sch:pattern>
	<!-- Rule 6: Disallow level1@class='part'
	<sch:pattern name="no_part" id="no_part">
		<sch:rule context="dtb:level1[@class]">
			<sch:assert test="@class!='part'">[Rule 6] @class='part' is not supported.</sch:assert>
		</sch:rule>
	</sch:pattern>
	 -->
	<!-- Rule 7: Disallow frontmatter contents together with toc
	<sch:pattern name="no_frontmatter_contents" id="no_frontmatter_contents">
		<sch:rule context="dtb:frontmatter[dtb:level1[@class='toc' or dtb:list[@class='toc']]]">
			<sch:report test="*[not(self::dtb:doctitle or self::dtb:docauthor or self::dtb:level1[@class='backCoverText' or @class='rearjacketcopy' or @class='colophon' or @class='toc' or dtb:list[@class='toc']])]">[Rule 7] Contents in frontmatter (aside from doctitle, docauthor, rearjacketcopy, colophon and toc) is not supported together with toc.</sch:report>
		</sch:rule>
	</sch:pattern>
	 -->
</sch:schema>
