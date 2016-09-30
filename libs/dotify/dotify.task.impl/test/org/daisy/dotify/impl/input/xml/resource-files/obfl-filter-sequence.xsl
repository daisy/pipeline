<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
	xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
	exclude-result-prefixes="xs xd"
	version="1.0">
	
	<xsl:import href="obfl-normalize.xsl"/>
	
	<xsl:template match="obfl:*[parent::obfl:obfl and not(self::obfl:sequence)]"/>

</xsl:stylesheet>