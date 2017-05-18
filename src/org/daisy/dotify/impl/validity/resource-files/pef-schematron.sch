<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron">
    <sch:ns prefix="pef" uri="http://www.daisy.org/ns/2008/pef"/>
    
    <!-- Rule 1: Check that result does not have too many rows on a page -->
    <sch:pattern name="rows_on_page" id="rows_on_page">
        <sch:rule context="pef:page">
            <sch:assert test="(ceiling(sum(pef:row/sum(ancestor-or-self::pef:*[@rowgap][1]/@rowgap)) div 4) + count(pef:row))&lt;=(ancestor::pef:*[@rows][1])/@rows">[Rule 1] Rows do not fit within the defined page height</sch:assert>
        </sch:rule>
    </sch:pattern>
    
    <!-- Rule 2: Check that result does not have too many characters on a row -->
    <sch:pattern xmlns="http://relaxng.org/ns/structure/1.0" name="chars_on_row" id="chars_on_row">
        <sch:rule context="pef:row">
            <sch:assert test="string-length(text())&lt;=(ancestor::pef:*[@cols][1])/@cols">[Rule 2] Too many characters on row</sch:assert>
        </sch:rule>
    </sch:pattern>
    <sch:diagnostics/>
</sch:schema>