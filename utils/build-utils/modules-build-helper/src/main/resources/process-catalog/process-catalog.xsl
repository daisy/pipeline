<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
    xmlns:px="http://www.daisy.org/ns/pipeline" xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc" xmlns:xd="http://www.daisy.org/ns/pipeline/doc"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:c="http://www.w3.org/ns/xproc-step"
    exclude-result-prefixes="#all" version="2.0">

    <xsl:param name="outputDir" required="no" select="''" as="xs:string"/>
    <xsl:param name="version" required="yes"  as="xs:string"/>
    
    

    <xsl:template match="/">
        

        <xsl:result-document href="{$outputDir}/bnd.bnd" method="text" xml:space="preserve"><c:data>
<xsl:if test="//cat:nextCatalog">Require-Bundle: <xsl:value-of select="string-join(//cat:nextCatalog/translate(@catalog,':','.'),',')"/></xsl:if>
<xsl:if test="string(//cat:uri[@px:script]) or //cat:uri[@px:data-type]">
        Service-Component: <xsl:value-of select="string-join((//cat:uri[@px:script]/concat('OSGI-INF/',replace(document(@uri,..)/*/@type,'.*:',''),'.xml'),//cat:uri[@px:data-type]/concat('OSGI-INF/',replace(document(@uri,..)/*/@id,'.*:',''),'.xml')),',')"/></xsl:if>
<!-- my xslt skills are long forgotten, this sucks-->
<xsl:if test="//cat:uri[@px:data-type] and not(//cat:uri[@px:script])">
        Import-Package: org.daisy.pipeline.datatypes,*</xsl:if>
<xsl:if test="//cat:uri[@px:script] and not(//cat:uri[@px:data-type])">
        Import-Package: org.daisy.pipeline.script,*</xsl:if>
<xsl:if test="//cat:uri[@px:script] and //cat:uri[@px:data-type]">
        Import-Package: org.daisy.pipeline.script,org.daisy.pipeline.datatypes,*</xsl:if>
        </c:data></xsl:result-document>
        <xsl:apply-templates mode="ds"/>
    </xsl:template>
    <xsl:template match="cat:uri[@px:script]" mode="ds">
        <xsl:variable name="type" select="string(document(@uri,.)/*/@type)"/>
        <xsl:variable name="id" select="if (namespace-uri-for-prefix(substring-before($type,':'),document(@uri,.)/*)='http://www.daisy.org/ns/pipeline/xproc') then substring-after($type,':') else $type"/>
        <xsl:variable name="name" select="(document(@uri,.)//*[@pxd:role='name'])[1]"/>
        <xsl:variable name="descr" select="(document(@uri,.)//*[@pxd:role='desc'])[1]"/>
        
        <xsl:result-document href="{$outputDir}/OSGI-INF/{replace($id,'.*:','')}.xml" method="xml">
            <scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0" immediate="true" name="{$id}">
                <scr:implementation class="org.daisy.pipeline.script.XProcScriptService"/>
                <scr:service>
                    <scr:provide interface="org.daisy.pipeline.script.XProcScriptService"/>
                </scr:service>
                <scr:property name="script.id" type="String" value="{$id}"/>
                <scr:property name="script.name" type="String" value="{$name}"/>
                <scr:property name="script.description" type="String" value="{$descr}"/>
                <scr:property name="script.url" type="String" value="{@name}"/>
                <scr:property name="script.version" type="String" value="{$version}"/>
            </scr:component>
        </xsl:result-document>
    </xsl:template>
    <xsl:template match="cat:uri[@px:data-type]" mode="ds">
        <xsl:variable name="id" select="string(document(@uri,.)/*/@id)"/>
        
        <xsl:result-document href="{$outputDir}/OSGI-INF/{replace($id,'.*:','')}.xml" method="xml">
            <scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0" immediate="true" name="{$id}">
                <scr:implementation class="org.daisy.pipeline.datatypes.UrlBasedDatatypeService"/>
                <scr:service>
                    <scr:provide interface="org.daisy.pipeline.datatypes.DatatypeService"/>
                </scr:service>
                <scr:reference bind="setUriResolver" cardinality="1..1" interface="javax.xml.transform.URIResolver" name="resolver" policy="static"/>
                <scr:property name="data-type.id" type="String" value="{$id}"/>
                <scr:property name="data-type.url" type="String" value="{@name}"/>
            </scr:component>
        </xsl:result-document>
    </xsl:template>
    
</xsl:stylesheet>
