<?xml version="1.0" encoding="UTF-8"?>
<!--
    
    Version
    2008-03-31
    
    Description
    Adds dc:Language, dc:Date and dc:Publisher to dtbook, if not present in input,
    or given but with null/whitespace only content values.
        
    dc:Language is taken from xml:lang if set, else inparam
    dc:Date is taken as inparam
    dc:Publisher is taken as inparam
    
    Removes dc:description and dc:subject if not valued
    
    Removes dc:Format (will be added by the fileset generator)
    
    Nodes
    dtbook/head
    
    Namespaces
    (x) "http://www.daisy.org/z3986/2005/dtbook/"
    
    Doctype
    (x) DTBook
    
    Author
    Markus Gylling, DAISY
	
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns="http://www.daisy.org/z3986/2005/dtbook/"
	exclude-result-prefixes="dtb">
    
    <xsl:include href="recursive-copy2.xsl"/>
    <xsl:include href="output2.xsl"/>
	<xsl:include href="library.xsl" />
    
    <xsl:param name="dateValue" select="current-date()"/>
    <xsl:param name="langValue" select="pf:default-locale()"/>
    <xsl:param name="publisherValue"/>
    

    <xsl:template match="dtb:head">
     
        <xsl:copy>
            <xsl:copy-of select="@*"/>
       
            <xsl:if test="count(dtb:meta[@name='dc:Date'])=0">
            	<xsl:message terminate="no">Adding dc:Date metadata element</xsl:message>
            	<xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Date</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:value-of select="$dateValue"/>
					</xsl:attribute>
				</xsl:element>
            </xsl:if>
            
            <xsl:if test="count(dtb:meta[@name='dc:Publisher'])=0">
            	<xsl:message terminate="no">Adding dc:Publisher metadata element</xsl:message>
            	<xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Publisher</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:value-of select="$publisherValue"/>
					</xsl:attribute>
				</xsl:element>
            </xsl:if>
                        
            <xsl:if test="count(dtb:meta[@name='dc:Language'])=0">
            	<xsl:message terminate="no">Adding dc:Language metadata element</xsl:message>
            	<xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Language</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:choose>
							<xsl:when test="//dtb:dtbook/@xml:lang">
								<xsl:value-of select="//dtb:dtbook/@xml:lang"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$langValue"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
				</xsl:element>
            
            </xsl:if>
        	<xsl:apply-templates/>         
        </xsl:copy>
    </xsl:template>
     
    <xsl:template match="dtb:meta[@name='dc:Publisher']">
    	<xsl:choose>
	    	<xsl:when test="normalize-space(@content) eq ''">
	        	<xsl:message terminate="no">Adding value to empty dc:Publisher metadata element</xsl:message>
	            <xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Publisher</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:value-of select="$publisherValue"/>
					</xsl:attribute>
				</xsl:element>
	        </xsl:when>
	       	<xsl:otherwise>
	        	<xsl:copy-of select="."/>
	        </xsl:otherwise>
        </xsl:choose>        
    </xsl:template>
    
    <xsl:template match="dtb:meta[@name='dc:Language']">
    	<xsl:choose>
	    	<xsl:when test="normalize-space(@content) eq ''">
	        	<xsl:message terminate="no">Adding value to empty dc:Language metadata element</xsl:message>
	            <xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Language</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
							<xsl:choose>
								<xsl:when test="//dtb:dtbook/@xml:lang">
									<xsl:value-of select="//dtb:dtbook/@xml:lang"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:value-of select="$langValue"/>
								</xsl:otherwise>
							</xsl:choose>
					</xsl:attribute>
				</xsl:element>
	        </xsl:when>
	        <xsl:otherwise>
	        	<xsl:copy-of select="."/>
	        </xsl:otherwise>	        
        </xsl:choose>        
    </xsl:template>
    
    <xsl:template match="dtb:meta[@name='dc:Date']">
    	<xsl:choose>
	    	<xsl:when test="normalize-space(@content) eq ''">
	        	<xsl:message terminate="no">Adding value to empty dc:Date metadata element</xsl:message>
	            <xsl:element name="meta" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:attribute name="name">
						<xsl:text>dc:Date</xsl:text>
					</xsl:attribute>
					<xsl:attribute name="content">
						<xsl:value-of select="$dateValue"/>
					</xsl:attribute>
				</xsl:element>
	        </xsl:when>   
	        <xsl:otherwise>
	        	<xsl:copy-of select="."/>
	        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>      
    
	<xsl:template match="dtb:meta[@name='dc:Description']">
    	<xsl:choose>
	    	<xsl:when test="normalize-space(@content) eq ''">
	        	<xsl:message terminate="no">Removing dc:Description lacking content</xsl:message>	            
	        </xsl:when>   
	        <xsl:otherwise>
	        	<xsl:copy-of select="."/>
	        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>      
    
    <xsl:template match="dtb:meta[@name='dc:Subject']">
    	<xsl:choose>
	    	<xsl:when test="normalize-space(@content) eq ''">
	        	<xsl:message terminate="no">Removing dc:Subject lacking content</xsl:message>	            
	        </xsl:when>   
	        <xsl:otherwise>
	        	<xsl:copy-of select="."/>
	        </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
	
	<xsl:template match="dtb:meta[lower-case(@name)='dc:format']"/>
    
	
	
     
</xsl:stylesheet>
