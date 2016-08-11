<?xml version="1.0" encoding="UTF-8"?>
<p:pipeline type="px:dotify-obfl-to-pef" version="1.0"
            xmlns:p="http://www.w3.org/ns/xproc"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:dotify="http://code.google.com/p/dotify/"
            xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
            exclude-inline-prefixes="#all">
	
	<p:option name="text-transform" required="true"/>
	
	<p:import href="../obfl-normalize-space.xpl"/>
	<p:import href="http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xpl"/>
	
	<pxi:obfl-normalize-space/>
	
	<dotify:obfl-to-pef locale="und">
		<p:with-option name="mode" select="$text-transform"/>
	</dotify:obfl-to-pef>
	
</p:pipeline>
