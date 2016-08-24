<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE resources PUBLIC "-//NISO//DTD resource 2005-1//EN"
"http://www.daisy.org/z3986/2005/resource-2005-1.dtd">
<resources xmlns="http://www.daisy.org/z3986/2005/resource/" version="2005-1">

	<!-- Escapable -->
	<scope nsuri="http://www.w3.org/2001/SMIL20/">
		<nodeSet id="ns001" select="//seq[@class='list']">
			<resource xml:lang="en" id="resID000">
				<text>list</text>
			</resource>
		</nodeSet>
		<nodeSet id="ns002" select="//seq[@class='m:math']">
			<resource xml:lang="en" id="resID001">
				<text>m:math</text>
			</resource>
		</nodeSet>
		<nodeSet id="ns003" select="//seq[@class='sidebar']">
			<resource xml:lang="en" id="resID002">
				<text>sidebar</text>
			</resource>
		</nodeSet>
		<nodeSet id="ns004" select="//seq[@class='table']">
			<resource xml:lang="en" id="resID003">
				<text>table</text>
			</resource>
		</nodeSet>

	</scope>

	<!-- Skippable -->
	<scope nsuri="http://www.daisy.org/z3986/2005/ncx/">
		<nodeSet id="ns005" select="//smilCustomTest[@bookStruct='PAGE_NUMBER']">
			<resource xml:lang="en" id="resID006">
				<text>page</text>
			</resource>
		</nodeSet>
		<nodeSet id="ns006" select="//smilCustomTest[@bookStruct='OPTIONAL_SIDEBAR']">
			<resource xml:lang="en" id="resID007">
				<text>sidebar</text>
			</resource>
		</nodeSet>
		<nodeSet id="ns007" select="//smilCustomTest[@bookStruct='OPTIONAL_PRODUCER_NOTE']">
			<resource xml:lang="en" id="resID008">
				<text>producer note</text>
			</resource>
		</nodeSet>

	</scope>
</resources>
