<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0">
	
	<xsl:output method="text"/>
	
	<xsl:template match="/">
		<xsl:variable name="version" select="/pom:project/pom:version"/>
		<!--
		    FIXME: moved to liblouis-native
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
		<xsl:variable name="dotify.api-version" select="/pom:project/pom:dependencyManagement
		                                                /pom:dependencies/pom:dependency[pom:artifactId='dotify.api']
		                                                /pom:version"/>
		<xsl:variable name="dotify.common-version" select="/pom:project/pom:dependencyManagement
		                                                   /pom:dependencies/pom:dependency[pom:artifactId='dotify.common']
		                                                   /pom:version"/>
		<xsl:variable name="dotify.hyphenator.impl-version" select="/pom:project/pom:dependencyManagement
		                                                            /pom:dependencies/pom:dependency[pom:artifactId='dotify.hyphenator.impl']
		                                                            /pom:version"/>
		<xsl:variable name="dotify.translator.impl-version" select="/pom:project/pom:dependencyManagement
		                                                            /pom:dependencies/pom:dependency[pom:artifactId='dotify.translator.impl']
		                                                            /pom:version"/>
		<xsl:variable name="dotify.formatter.impl-version" select="/pom:project/pom:dependencyManagement
		                                                           /pom:dependencies/pom:dependency[pom:artifactId='dotify.formatter.impl']
		                                                           /pom:version"/>
		<xsl:variable name="dotify.text.impl-version" select="/pom:project/pom:dependencyManagement
		                                                      /pom:dependencies/pom:dependency[pom:artifactId='dotify.text.impl']
		                                                      /pom:version"/>
		<xsl:variable name="dotify.task.impl-version" select="/pom:project/pom:dependencyManagement
		                                                      /pom:dependencies/pom:dependency[pom:artifactId='dotify.task.impl']
		                                                      /pom:version"/>
		<xsl:variable name="streamline-api-version" select="/pom:project/pom:dependencyManagement
		                                                    /pom:dependencies/pom:dependency[pom:artifactId='streamline-api']
		                                                    /pom:version"/>
		<xsl:variable name="streamline-engine-version" select="/pom:project/pom:dependencyManagement
		                                                       /pom:dependencies/pom:dependency[pom:artifactId='streamline-engine']
		                                                       /pom:version"/>
		<xsl:variable name="braille-utils.api-version" select="/pom:project/pom:dependencyManagement
		                                                      /pom:dependencies/pom:dependency[pom:artifactId='braille-utils.api']
		                                                      /pom:version"/>
		<xsl:variable name="braille-utils.impl-version" select="/pom:project/pom:dependencyManagement
		                                                       /pom:dependencies/pom:dependency[pom:artifactId='braille-utils.impl']
		                                                       /pom:version"/>
		<xsl:variable name="braille-utils.pef-tools-version" select="/pom:project/pom:dependencyManagement
		                                                            /pom:dependencies/pom:dependency[pom:artifactId='braille-utils.pef-tools']
		                                                            /pom:version"/>
		<xsl:variable name="braille-css-version" select="/pom:project/pom:dependencyManagement
		                                                 /pom:dependencies/pom:dependency[pom:artifactId='braille-css']
		                                                 /pom:version"/>
		<xsl:variable name="jsass-version" select="/pom:project/pom:dependencyManagement
		                                           /pom:dependencies/pom:dependency[pom:artifactId='io.bit3.jsass']
		                                           /pom:version"/>
		<!--
		    FIXME: moved to libhyphen-native
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
		<xsl:text>- dotify (api [</xsl:text>
		<xsl:value-of select="$dotify.api-version"/>
		<xsl:text>](https://github.com/brailleapps/dotify.api/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$dotify.api-version"/>
		<xsl:text>), common&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$dotify.common-version"/>
		<xsl:text>](https://github.com/brailleapps/dotify.common/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$dotify.common-version"/>
		<xsl:text>), hyphenator.impl&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$dotify.hyphenator.impl-version"/>
		<xsl:text>](https://github.com/brailleapps/dotify.hyphenator.impl/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$dotify.hyphenator.impl-version"/>
		<xsl:text>), translator.impl&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$dotify.translator.impl-version"/>
		<xsl:text>](https://github.com/brailleapps/dotify.translator.impl/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$dotify.translator.impl-version"/>
		<xsl:text>), formatter.impl&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$dotify.formatter.impl-version"/>
		<xsl:text>](https://github.com/brailleapps/dotify.formatter.impl/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$dotify.formatter.impl-version"/>
		<xsl:text>), text.impl&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$dotify.text.impl-version"/>
		<xsl:text>](https://github.com/brailleapps/dotify.text.impl/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$dotify.text.impl-version"/>
		<xsl:text>), streamline-api&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$streamline-api-version"/>
		<xsl:text>](https://github.com/brailleapps/streamline-api/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$streamline-api-version"/>
		<xsl:text>), streamline-engine&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$streamline-engine-version"/>
		<xsl:text>](https://github.com/brailleapps/streamline-engine/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$streamline-engine-version"/>
		<xsl:text>), task.impl&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$dotify.task.impl-version"/>
		<xsl:text>](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$dotify.task.impl-version"/>
		<xsl:text>))&#10;</xsl:text>
		<xsl:text>- brailleutils (api&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$braille-utils.api-version"/>
		<xsl:text>](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$braille-utils.api-version"/>
		<xsl:text>), impl&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$braille-utils.impl-version"/>
		<xsl:text>](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$braille-utils.impl-version"/>
		<xsl:text>), pef-tools&#10;</xsl:text>
		<xsl:text>  [</xsl:text>
		<xsl:value-of select="$braille-utils.pef-tools-version"/>
		<xsl:text>](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv</xsl:text>
		<xsl:value-of select="$braille-utils.pef-tools-version"/>
		<xsl:text>))&#10;</xsl:text>
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
