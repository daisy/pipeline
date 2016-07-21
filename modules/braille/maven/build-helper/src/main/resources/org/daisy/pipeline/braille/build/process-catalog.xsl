<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
                xmlns:px="http://www.daisy.org/ns/pipeline"
                xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:xd="http://www.daisy.org/ns/pipeline/doc"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                exclude-result-prefixes="#all" version="2.0">
    
    <xsl:param name="outputDir" required="no" select="''" as="xs:string"/>
    <xsl:param name="version" required="yes"  as="xs:string"/>
    
    <xsl:template match="/*">
        <!-- extract data types -->
        <xsl:variable name="data-types" as="element()*">
            <xsl:for-each select="cat:uri">
                <xsl:variable name="uri" select="resolve-uri(@uri, base-uri(.))"/>
                <xsl:if test="doc-available($uri)">
                    <xsl:apply-templates select="document(@uri)/p:*/p:option/p:pipeinfo/pxd:data-type/*" mode="data-type-xml">
                        <xsl:with-param name="script-uri" tunnel="yes" select="@name"/>
                    </xsl:apply-templates>
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:for-each select="$data-types">
            <xsl:result-document href="{concat($outputDir,replace(@id,'^\{.*/([^/]+)\}(.+)$','/data-types/$1/$2.xml'))}" method="xml">
                <xsl:sequence select="."/>
            </xsl:result-document>
            <xsl:result-document href="{concat($outputDir,replace(@id,'^\{.*/([^/]+)\}(.+)$','/OSGI-INF/data-types/$1/$2.xml'))}" method="xml">
                <scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0" immediate="true" name="{@id}">
                    <scr:implementation class="org.daisy.pipeline.datatypes.UrlBasedDatatypeService"/>
                    <scr:service>
                        <scr:provide interface="org.daisy.pipeline.datatypes.DatatypeService"/>
                    </scr:service>
                    <scr:reference bind="setUriResolver" cardinality="1..1" interface="javax.xml.transform.URIResolver" name="resolver" policy="static"/>
                    <scr:property name="data-type.id" type="String" value="{replace(replace(@id,'^\{http://(.+)\}(.+)$','$1/$2'),'/','')}"/>
                    <scr:property name="data-type.url" type="String" value="{replace(@id,'^\{(.+)\}(.+)$','$1/$2.xml')}"/>
                </scr:component>
            </xsl:result-document>
        </xsl:for-each>
        <xsl:result-document href="{$outputDir}/bnd.bnd" method="text" xml:space="preserve"><c:data>
<xsl:if test="//cat:nextCatalog">Require-Bundle: <xsl:value-of select="string-join(//cat:nextCatalog/translate(@catalog,':','.'),',')"/></xsl:if>
<xsl:variable name="service-components" as="xs:string*"
              select="(//cat:uri[@px:script]/concat('OSGI-INF/',replace(document(@uri,..)/*/@type,'.*:',''),'.xml'),
                       //cat:uri[@px:data-type]/concat('OSGI-INF/',replace(document(@uri,..)/*/@id,'.*:',''),'.xml'),
                       $data-types/@id/replace(.,'^\{.*/([^/]+)\}(.+)$','OSGI-INF/data-types/$1/$2.xml'))"/>
<xsl:if test="exists($service-components)">
        Service-Component: <xsl:value-of select="string-join($service-components,',')"/></xsl:if>
<!-- my xslt skills are long forgotten, this sucks-->
<xsl:if test="(//cat:uri[@px:data-type] or exists($data-types)) and not(//cat:uri[@px:script])">
        Import-Package: org.daisy.pipeline.datatypes,*</xsl:if>
<xsl:if test="//cat:uri[@px:script] and not(//cat:uri[@px:data-type] or exists($data-types))">
        Import-Package: org.daisy.pipeline.script,*</xsl:if>
<xsl:if test="//cat:uri[@px:script] and (//cat:uri[@px:data-type] or exists($data-types))">
        Import-Package: org.daisy.pipeline.script,org.daisy.pipeline.datatypes,*</xsl:if>
        </c:data></xsl:result-document>
        <xsl:result-document href="{$outputDir}/META-INF/catalog.xml" method="xml">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()" mode="ds"/>
                <xsl:for-each select="$data-types">
                    <cat:uri name="{replace(@id,'^\{(.+)\}(.+)$','$1/$2.xml')}"
                             uri="{replace(@id,'^\{.*/([^/]+)\}(.+)$','../data-types/$1/$2.xml')}"/>
                </xsl:for-each>
            </xsl:copy>
        </xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:script]" mode="ds" priority="2">
        <xsl:variable name="type" select="string(document(@uri,.)/*/@type)"/>
        <xsl:variable name="id" select="if (namespace-uri-for-prefix(substring-before($type,':'),document(@uri,.)/*)='http://www.daisy.org/ns/pipeline/xproc') then substring-after($type,':') else $type"/>
        <xsl:variable name="name" select="(document(@uri,.)//*[tokenize(@pxd:role,'\s+')='name'])[1]"/>
        <xsl:variable name="descr" select="(document(@uri,.)//*[tokenize(@pxd:role,'\s+')='desc'])[1]"/>
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
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:extends]" mode="ds">
        <xsl:variable name="generated-href" select="concat($outputDir,f:generated-href(@uri))"/>
        <xsl:result-document href="{$generated-href}" method="xml">
            <xsl:call-template name="extend-script">
                <xsl:with-param name="script-uri-element" select="."/>
                <xsl:with-param name="extends-uri-element" select="(//cat:uri[current()/@px:extends=@name])[1]"/>
            </xsl:call-template>
        </xsl:result-document>
        <xsl:copy>
            <xsl:apply-templates select="@* except @uri" mode="#current"/>
            <xsl:attribute name="uri" select="concat('..',f:generated-href(@uri))"/>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="cat:uri[not(@px:extends)]" mode="ds">
        <xsl:variable name="uri" select="resolve-uri(@uri, base-uri(.))"/>
        <xsl:choose>
            <xsl:when test="doc-available($uri)">
                <xsl:variable name="doc" select="document($uri)"/>
                <xsl:choose>
                    <xsl:when test="$doc/p:*/p:option/p:pipeinfo/pxd:data-type">
                        <xsl:variable name="generated-href" select="concat($outputDir,f:generated-href(@uri))"/>
                        <xsl:result-document href="{$generated-href}" method="xml">
                            <xsl:apply-templates select="$doc" mode="script">
                                <xsl:with-param name="script-uri" tunnel="yes" select="@name"/>
                            </xsl:apply-templates>
                        </xsl:result-document>
                        <xsl:copy>
                            <xsl:apply-templates select="@* except @uri" mode="#current"/>
                            <xsl:attribute name="uri" select="concat('..',f:generated-href(@uri))"/>
                            <xsl:apply-templates mode="#current"/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:next-match/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:function name="f:generated-href">
        <xsl:param name="uri" as="xs:string"/>
        <xsl:value-of select="concat('/generated-scripts/',replace($uri,'^.*/([^/]+)$','$1'))"/>
    </xsl:function>
    
    <!-- recursive template allowing scripts to inherit from scripts that inherit from scripts -->
    <xsl:template name="extend-script">
        <xsl:param name="script-uri-element" as="element()"/>
        <xsl:param name="extends-uri-element" as="element()?"/>
        
        <xsl:variable name="extends-doc">
            <xsl:choose>
                <xsl:when test="$extends-uri-element">
                    <xsl:call-template name="extend-script">
                        <xsl:with-param name="script-uri-element" select="$extends-uri-element"/>
                        <xsl:with-param name="extends-uri-element" select="(//cat:uri[$extends-uri-element/@px:extends=@name])[1]"/>
                    </xsl:call-template>
                    
                </xsl:when>
                <xsl:when test="$script-uri-element/@px:extends">
                    <xsl:variable name="_extends-doc" select="$script-uri-element/document(@px:extends)"/>
                    <xsl:if test="not($_extends-doc)">
                        <xsl:message terminate="yes" select="concat('Unable to resolve script extension: ', $script-uri-element/@px:extends)"/>
                    </xsl:if>
                    <xsl:sequence select="$_extends-doc"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="script-doc" select="$script-uri-element/document(@uri)"/>
        <xsl:if test="not($script-doc)">
            <xsl:message terminate="yes" select="concat('Unable to resolve script: ', $script-uri-element/@uri,' (',base-uri($script-uri-element/@uri),')')"/>
        </xsl:if>
        
        <xsl:choose>
            <xsl:when test="$extends-doc">
                <xsl:apply-templates select="$script-doc" mode="extend-script">
                    <xsl:with-param name="original-script" select="$extends-doc" tunnel="yes"/>
                    <xsl:with-param name="script-uri" tunnel="yes" select="$script-uri-element/@name"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="$script-doc" mode="script">
                    <xsl:with-param name="script-uri" tunnel="yes" select="$script-uri-element/@name"/>
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
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
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template match="cat:uri/@px:script|
                         cat:uri/@px:extends|
                         cat:uri/@px:data-type"
                  mode="ds"/>
    
    <xsl:template match="/*/p:input[@port] | /*/p:option[@name]" mode="extend-script">
        <xsl:param name="original-script" as="document-node()" tunnel="yes"/>
        <xsl:variable name="name" as="xs:string" select="(@port, @name)[1]"/>
        <xsl:variable name="original-input-or-option" as="element()?" select="$original-script/*/(p:input|p:option)[(@port,@name)=$name]"/>
        <xsl:variable name="new-attributes" as="xs:string*" select="@*/concat('{',namespace-uri(.),'}',name(.))"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:sequence select="$original-input-or-option/@*[not(concat('{',namespace-uri(.),'}',name(.))=$new-attributes or name()='select' and current()/@required = 'true')]"/>
            <xsl:apply-templates select="self::p:option/p:pipeinfo/pxd:data-type" mode="data-type-attribute"/>
            <xsl:if test="not(p:documentation)">
                <xsl:sequence select="$original-input-or-option/p:documentation"/>
            </xsl:if>
            <xsl:apply-templates select="node()" mode="#current">
                <xsl:with-param name="original-input-or-option" select="$original-input-or-option" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/*/p:input/p:documentation | /*/p:option/p:documentation" mode="extend-script">
        <xsl:param name="original-input-or-option" as="element()?" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:if test="not(descendant::*[tokenize(@pxd:role,'\s+')='name'])">
                <xsl:sequence select="$original-input-or-option/p:documentation/*[tokenize(@pxd:role,'\s+')='name']"/>
            </xsl:if>
            <xsl:apply-templates select="node()" mode="#current">
                <xsl:with-param name="original-input-or-option" select="$original-input-or-option" tunnel="yes"/>
            </xsl:apply-templates>
            <xsl:if test="not(descendant::*[tokenize(@pxd:role,'\s+')='desc'])">
                <xsl:sequence select="$original-input-or-option/p:documentation/*[tokenize(@pxd:role,'\s+')='desc']"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[tokenize(@pxd:role,'\s+')=('name','desc')]" mode="extend-script">
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
    
    <xsl:template match="/p:*" mode="script" priority="2">
        <xsl:choose>
            <xsl:when test="p:option/p:pipeinfo/pxd:data-type">
                <xsl:next-match/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="/*/p:option[p:pipeinfo/pxd:data-type]" mode="script">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:apply-templates select="p:pipeinfo/pxd:data-type" mode="data-type-attribute"/>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo" mode="script extend-script">
        <xsl:if test="* except pxd:data-type">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:data-type" mode="script extend-script"/>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:data-type" mode="data-type-attribute">
        <xsl:variable name="id" as="xs:string">
            <xsl:apply-templates select="." mode="data-type-id"/>
        </xsl:variable>
        <xsl:attribute name="pxd:data-type" select="replace(replace($id,'^\{http://(.+)\}(.+)$','$1/$2'),'/','')"/>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:data-type/*" mode="data-type-xml">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:if test="not(@id)">
                <xsl:attribute name="id">
                    <xsl:apply-templates select="parent::*" mode="data-type-id"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:data-type" mode="data-type-id" as="xs:string">
        <xsl:param name="script-uri" tunnel="yes"/>
        <xsl:sequence select="(@id,child::*/@id,concat('{',$script-uri,'}',parent::*/parent::*/@name))[1]"/>
    </xsl:template>
    
    <xsl:template match="@*|node()" mode="ds script extend-script data-type-xml">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
