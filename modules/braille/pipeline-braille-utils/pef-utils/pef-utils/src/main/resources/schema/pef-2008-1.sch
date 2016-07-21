<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://purl.oclc.org/dsdl/schematron">
	<sch:ns prefix="pef" uri="http://www.daisy.org/ns/2008/pef"/>
	<sch:pattern>
		<sch:rule context="pef:page">
			<sch:assert test="(ceiling(sum(pef:row/sum(ancestor-or-self::pef:*[@rowgap][1]/@rowgap)) div 4) + count(pef:row))&lt;=(ancestor::pef:*[@rows][1])/@rows">[Rule 1] Rows do not fit within the defined page height</sch:assert>
		</sch:rule>
	</sch:pattern>
	<sch:pattern>
		<sch:rule context="pef:row">
			<sch:assert test="string-length(text())&lt;=(ancestor::pef:*[@cols][1])/@cols">[Rule 2] Too many characters on row</sch:assert>
		</sch:rule>
	</sch:pattern>
	<sch:diagnostics/>
</sch:schema>
