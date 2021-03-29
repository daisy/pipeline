<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0">
	
	<xsl:output method="text"/>
	
	<xsl:template match="/">
		<xsl:variable name="version" select="/pom:project/pom:version"/>
		<!--
		    FIXME: moved to liblouis-utils
		-->
		<xsl:variable name="liblouis-version" select="/pom:project/pom:dependencyManagement
		                                              /pom:dependencies/pom:dependency[pom:artifactId='louis']
		                                              /pom:version"/>
		<xsl:variable name="liblouisutdml-version" select="/pom:project/pom:dependencyManagement
		                                                   /pom:dependencies/pom:dependency[pom:artifactId='louisutdml']
		                                                   /pom:version"/>
		<xsl:variable name="liblouis-java-version" select="/pom:project/pom:dependencyManagement
		                                                   /pom:dependencies/pom:dependency[pom:artifactId='liblouis-java']
		                                                   /pom:version"/>
		<xsl:variable name="dotify.library-version" select="/pom:project/pom:dependencyManagement
		                                                    /pom:dependencies/pom:dependency[pom:artifactId='dotify.library']
		                                                    /pom:version"/>
		<xsl:variable name="braille-css-version" select="/pom:project/pom:dependencyManagement
		                                                 /pom:dependencies/pom:dependency[pom:artifactId='braille-css']
		                                                 /pom:version"/>
		<xsl:variable name="jsass-version" select="/pom:project/pom:dependencyManagement
		                                           /pom:dependencies/pom:dependency[pom:artifactId='io.bit3.jsass']
		                                           /pom:version"/>
		<!--
		    FIXME: moved to libhyphen-utils
		-->
		<xsl:variable name="libhyphen-version" select="/pom:project/pom:dependencyManagement
		                                               /pom:dependencies/pom:dependency[pom:artifactId='hyphen']
		                                               /pom:version"/>
		<xsl:variable name="jhyphen-version" select="/pom:project/pom:dependencyManagement
		                                             /pom:dependencies/pom:dependency[pom:artifactId='jhyphen']
		                                             /pom:version"/>
		<xsl:variable name="texhyphj-version" select="/pom:project/pom:dependencyManagement
		                                             /pom:dependencies/pom:dependency[pom:artifactId='texhyphj']
		                                             /pom:version"/>
		<xsl:text>v</xsl:text>
		<xsl:value-of select="$version"/>
		<xsl:text>&#10;</xsl:text>
		<xsl:text>=======&#10;&#10;</xsl:text>
		<xsl:text>Changes&#10;</xsl:text>
		<xsl:text>-------&#10;</xsl:text>
		<xsl:text>- ...&#10;&#10;</xsl:text>
		<xsl:text>Components&#10;</xsl:text>
		<xsl:text>----------&#10;</xsl:text>
		<xsl:text>- liblouis ([</xsl:text>
		<xsl:value-of select="$liblouis-version"/>
		<xsl:text>](https://github.com/liblouis/liblouis/releases/tag/v</xsl:text>
		<xsl:value-of select="$liblouis-version"/>
		<xsl:text>)), liblouisutdml&#10;</xsl:text>
		<xsl:text>  ([</xsl:text>
		<xsl:value-of select="$liblouisutdml-version"/>
		<xsl:text>](https://github.com/liblouis/liblouisutdml/releases/tag/v</xsl:text>
		<xsl:value-of select="$liblouisutdml-version"/>
		<xsl:text>)), liblouis-java&#10;</xsl:text>
		<xsl:text>  ([</xsl:text>
		<xsl:value-of select="$liblouis-java-version"/>
		<xsl:text>](https://github.com/liblouis/liblouis-java/releases/tag/</xsl:text>
		<xsl:value-of select="$liblouis-java-version"/>
		<xsl:text>))&#10;</xsl:text>
		<xsl:text>- dotify ([</xsl:text>
		<xsl:value-of select="$dotify.library-version"/>
		<xsl:text>](https://github.com/mtmse/dotify.library/releases/tag/</xsl:text>
		<xsl:value-of select="$dotify.library-version"/>
		<xsl:text>)&#10;</xsl:text>
		<xsl:text>- braille-css ([</xsl:text>
		<xsl:value-of select="$braille-css-version"/>
		<xsl:text>](https://github.com/daisy/braille-css/releases/tag/</xsl:text>
		<xsl:value-of select="$braille-css-version"/>
		<xsl:text>))&#10;</xsl:text>
		<xsl:text>- jsass ([</xsl:text>
		<xsl:value-of select="$jsass-version"/>
		<xsl:text>](https://github.com/snaekobbi/jsass/releases/tag/</xsl:text>
		<xsl:value-of select="$jsass-version"/>
		<xsl:text>))&#10;</xsl:text>
		<xsl:text>- libhyphen ([</xsl:text>
		<xsl:value-of select="$libhyphen-version"/>
		<xsl:text>](https://github.com/snaekobbi/libhyphen-nar/releases/tag/</xsl:text>
		<xsl:value-of select="$libhyphen-version"/>
		<xsl:text>)), jhyphen&#10;</xsl:text>
		<xsl:text>  ([</xsl:text>
		<xsl:value-of select="$jhyphen-version"/>
		<xsl:text>](https://github.com/daisy/jhyphen/releases/tag/v</xsl:text>
		<xsl:value-of select="$jhyphen-version"/>
		<xsl:text>))&#10;</xsl:text>
		<xsl:text>- texhyphj ([</xsl:text>
		<xsl:value-of select="$texhyphj-version"/>
		<xsl:text>](https://github.com/joeha480/texhyphj/releases/tag/release-</xsl:text>
		<xsl:value-of select="$texhyphj-version"/>
		<xsl:text>))&#10;&#10;</xsl:text>
	</xsl:template>
</xsl:stylesheet>
