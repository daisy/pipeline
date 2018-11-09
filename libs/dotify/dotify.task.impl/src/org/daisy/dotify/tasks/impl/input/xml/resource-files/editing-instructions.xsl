<?xml version="1.0" encoding="UTF-8"?>
<!--
	Adds support for deleting, moving and inserting elements into a source file before processing.
	
	Delete example:
	<?dotify-delete id=block_1?>
	
	Move example:
	<?dotify-move id=sidebar_1?>
	
	Insert example:
	<?dotify-insert
		<p>This paragraph will only show up in braille.</p>
	?>
 -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <!-- delete instruction -->
    <xsl:template match="*[//processing-instruction('dotify-delete')[starts-with(.,  'id=')][current()/@id=normalize-space(substring(., 4))]]"/>
    <xsl:template match="processing-instruction('dotify-delete')"/>
        
    <!-- move instruction -->
    <xsl:template match="*[//processing-instruction('dotify-move')[starts-with(.,  'id=')][current()/@id=normalize-space(substring(., 4))]]"/>
    <xsl:template match="processing-instruction('dotify-move')">
        <xsl:copy-of select="//*[@id=normalize-space(substring(current(), 4))]"/>
    </xsl:template>
    
    <!-- insert instruction -->
    <xsl:template match="processing-instruction('dotify-insert')">
        <xsl:value-of select="current()" disable-output-escaping="yes"/>
    </xsl:template>

    <!-- Copy -->
    <xsl:template match="*|processing-instruction()|comment()">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>