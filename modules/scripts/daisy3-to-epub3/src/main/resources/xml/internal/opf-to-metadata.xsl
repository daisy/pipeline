<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
  xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="2.0" exclude-result-prefixes="f xs"
  xpath-default-namespace="http://openebook.org/namespaces/oeb-package/1.0/">

  <xsl:output method="xml" indent="yes"/>

  <!--TODO resolve metadata prefixes from @profile and @prefix
     * default prefixes declared in @profile
        e.g. @profile="http://www.daisy.org/z3998/2011/vocab/profiles/default/"
             declares "dcterms" and "z3998"
     * other prefixes are explicitly declared in @prefix
        e.g. prefix="foaf: http://xmlns.com/foaf/0.1/"
  -->
  <xsl:template match="metadata">
    <!-- TODO dynamically generate @prefix -->
    <metadata prefix="dc: http://purl.org/dc/elements/1.1/">
      <!--== Identifier ==-->
      <!--TODO translate identifiers ?-->
      
      <!--== Title ==-->
      <xsl:apply-templates select="dc-metadata/(dc:title|dc:Title)"/>
      
      <!--== Language ==-->
      <xsl:apply-templates select="dc-metadata/(dc:Language|dc:language)"/>
      
      <!--== Other ==-->
      <xsl:apply-templates/>
      <!--TODO how to translate dtb:* metadata (notably about the source publication) ?-->

    </metadata>
  </xsl:template>
  
  <xsl:template match="dc-metadata">
    <xsl:apply-templates select="* except (dc:title|dc:Title|dc:identifier|dc:Identifier|dc:language|dc:Language|dc:date|dc:Date|dc:type|dc:Type|dc:format|dc:Format)"/>
  </xsl:template>
  <xsl:template match="dc:*">
    <xsl:element name="{lower-case(name())}">
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="."/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
