<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pom="http://maven.apache.org/POM/4.0.0">
	
	<xsl:param name="pom_a" required="yes"/>
	<xsl:param name="pom_b" required="yes"/>
	
	<xsl:output method="text"/>
	
	<xsl:template name="main">
		<xsl:choose>
			<xsl:when test="deep-equal(document($pom_a)/*/(* except (pom:version|pom:scm)),
			                           document($pom_b)/*/(* except (pom:version|pom:scm)))">
				<xsl:message>equal</xsl:message>
			</xsl:when>
			<xsl:otherwise>
				<xsl:message terminate="yes">not equal</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
