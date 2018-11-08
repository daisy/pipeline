<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline"
                xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
                exclude-result-prefixes="#all" version="2.0">
    
    <!-- recursive template allowing scripts to inherit from scripts that inherit from scripts -->
    <xsl:template name="extend-script">
        <xsl:param name="script-uri"/>
        <xsl:param name="extends-uri"/>
        <xsl:param name="catalog-xml" as="element()"/>
        <xsl:if test="not(doc-available($script-uri))">
            <xsl:message terminate="yes" select="concat('Unable to resolve script: ', $script-uri)"/>
        </xsl:if>
        <xsl:variable name="script-doc" select="document($script-uri)"/>
        <xsl:variable name="extends-uri-element" as="element()?" select="$catalog-xml//cat:uri[@name=$extends-uri]"/>
        <xsl:variable name="extends-doc">
            <xsl:choose>
                <xsl:when test="$extends-uri-element/@px:extends">
                    <xsl:variable name="doc">
                        <xsl:call-template name="extend-script">
                            <xsl:with-param name="script-uri" select="$extends-uri-element/resolve-uri(@uri,base-uri(.))"/>
                            <xsl:with-param name="extends-uri" select="$extends-uri-element/resolve-uri(@px:extends,base-uri(.))"/>
                            <xsl:with-param name="catalog-xml" select="$catalog-xml"/>
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:apply-templates select="$doc" mode="finalize-script"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:if test="not(doc-available($extends-uri))">
                        <xsl:message terminate="yes" select="concat('Unable to resolve script extension: ', $extends-uri)"/>
                    </xsl:if>
                    <xsl:sequence select="document($extends-uri)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:apply-templates select="$script-doc" mode="extend-script">
            <xsl:with-param name="original-script" select="$extends-doc" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template mode="extend-script"
                  match="/*/p:input[@port]|
                         /*/p:option[@name]">
        <xsl:param name="original-script" as="document-node()" tunnel="yes"/>
        <xsl:variable name="name" as="xs:string" select="(@port, @name)[1]"/>
        <xsl:variable name="original-input-or-option" as="element()?" select="$original-script/*/(p:input|p:option)[(@port,@name)=$name]"/>
        <xsl:variable name="new-attributes" as="xs:string*" select="@*/concat('{',namespace-uri(.),'}',name(.))"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:sequence select="$original-input-or-option/@*[not(concat('{',namespace-uri(.),'}',name(.))=$new-attributes
                                                                   or concat('{',namespace-uri(.),'}',name(.))='{}select'
                                                                      and current()/@required = 'true'
                                                                   or concat('{',namespace-uri(.),'}',name(.))='{http://www.daisy.org/ns/pipeline/xproc}data-type'
                                                                      and current()/p:pipeinfo/pxd:type)]"/>
            <xsl:if test="not(p:pipeinfo)
                          and not(@pxd:type)
                          and $original-input-or-option/p:pipeinfo/pxd:type">
                <p:pipeinfo>
                    <xsl:sequence select="$original-input-or-option/p:pipeinfo/pxd:type"/>
                </p:pipeinfo>
            </xsl:if>
            <xsl:if test="not(p:documentation)">
                <xsl:sequence select="$original-input-or-option/p:documentation"/>
            </xsl:if>
            <xsl:apply-templates select="node()" mode="#current">
                <xsl:with-param name="original-input-or-option" select="$original-input-or-option" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="extend-script"
                  match="/*/p:input/p:pipeinfo|
                         /*/p:option/p:pipeinfo">
        <xsl:param name="original-input-or-option" as="element()?" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:if test="not(pxd:type) and not(parent::*/@pxd:type)">
                <xsl:sequence select="$original-input-or-option/p:pipeinfo/pxd:type"/>
            </xsl:if>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="extend-script"
                  match="/*/p:input/p:documentation|
                         /*/p:option/p:documentation">
        <xsl:param name="original-input-or-option" as="element()?" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:if test="not(descendant::*[tokenize(@pxd:role,'\s+')='name'])">
                <xsl:sequence select="$original-input-or-option/p:documentation/*[tokenize(@pxd:role,'\s+')='name']"/>
            </xsl:if>
            <xsl:apply-templates mode="#current"/>
            <xsl:if test="not(descendant::*[tokenize(@pxd:role,'\s+')='desc'])">
                <xsl:sequence select="$original-input-or-option/p:documentation/*[tokenize(@pxd:role,'\s+')='desc']"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="extend-script"
                  match="*[tokenize(@pxd:role,'\s+')=('name','desc')]">
        <xsl:param name="original-input-or-option" as="element()?" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@* except @pxd:inherit" mode="#current"/>
            <xsl:if test="@pxd:inherit = 'prepend'">
                <xsl:copy-of select="$original-input-or-option//*[tokenize(@pxd:role,'\s+')=current()/tokenize(@pxd:role,'\s+')]/node()"/>
                <xsl:text><![CDATA[

]]></xsl:text>
            </xsl:if>
            <xsl:copy-of select="node()"/>
            <xsl:if test="@pxd:inherit = 'append'">
                <xsl:text><![CDATA[

]]></xsl:text>
                <xsl:copy-of select="$original-input-or-option//*[tokenize(@pxd:role,'\s+')=current()/tokenize(@pxd:role,'\s+')]/node()"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="finalize-script extend-script"
                  match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
