<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron" queryBinding="xslt2">
	<sch:title>
		Verifies that lines does not overlap on front and back of a sheet
		(excluding first line, which is assumed to be a header)
	</sch:title>
	<sch:ns prefix="pef" uri="http://www.daisy.org/ns/2008/pef"/>
	<sch:pattern>
		<sch:rule context="pef:page[position() mod 2 = 1 and ancestor::pef:*[@duplex][1][@duplex='true']]">
			<sch:let name="usedPos1" value="pef:row[position()&gt;1 and translate(text(), '&#x2800;', '')!='']/(
				sum(preceding-sibling::pef:row/(ancestor-or-self::pef:*[@rowgap][1]/@rowgap + 4)),
				sum(preceding-sibling::pef:row/(ancestor-or-self::pef:*[@rowgap][1]/@rowgap + 4)) + 1,
				sum(preceding-sibling::pef:row/(ancestor-or-self::pef:*[@rowgap][1]/@rowgap + 4)) + 2,
				sum(preceding-sibling::pef:row/(ancestor-or-self::pef:*[@rowgap][1]/@rowgap + 4)) + 3
			)"/>
			<sch:let name="usedPos2" value="following-sibling::pef:page[1]/pef:row[position()&gt;1 and translate(text(), '&#x2800;', '')!='']/(
				sum(preceding-sibling::pef:row/(ancestor-or-self::pef:*[@rowgap][1]/@rowgap + 4)),
				sum(preceding-sibling::pef:row/(ancestor-or-self::pef:*[@rowgap][1]/@rowgap + 4)) + 1,
				sum(preceding-sibling::pef:row/(ancestor-or-self::pef:*[@rowgap][1]/@rowgap + 4)) + 2,
				sum(preceding-sibling::pef:row/(ancestor-or-self::pef:*[@rowgap][1]/@rowgap + 4)) + 3
			)"/>
			<sch:let name="used" value="($usedPos1, $usedPos2)"/>
			<sch:assert test="count($used)=count(distinct-values(($used)))">Rows on this sheet are overlapping (offending grid offsets: <sch:value-of select="$used[index-of($used,.)[2]]"/>)</sch:assert>
		</sch:rule>
	</sch:pattern>
	<sch:diagnostics/>
</sch:schema>
