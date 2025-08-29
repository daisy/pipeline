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
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:GenerateModuleClass="org.daisy.pipeline.maven.plugin.GenerateModuleClassFunctionProvider$GenerateModuleClass"
                exclude-result-prefixes="#all" version="2.0">
    
    <xsl:param name="generatedSourcesDirectory" required="yes" as="xs:string"/>
    <xsl:param name="generatedResourcesDirectory" required="yes" as="xs:string"/>
    <xsl:param name="moduleName" required="yes" as="xs:string"/>
    <xsl:param name="moduleVersion" required="yes" as="xs:string"/>
    <xsl:param name="moduleTitle" required="yes" as="xs:string"/>
    
    <xsl:include href="../lib/uri-functions.xsl"/>
    <xsl:include href="../lib/extend-script.xsl"/>
    
    <xsl:template match="/*">
        <!--
            extract data types
        -->
        <xsl:for-each select="cat:uri[@name or @px:content-type='script']">
            <xsl:variable name="uri" select="resolve-uri(@uri,base-uri(.))"/>
            <xsl:if test="doc-available($uri)">
                <xsl:variable name="xproc">
                    <xsl:choose>
                        <xsl:when test="@px:extends">
                            <xsl:call-template name="extend-script">
                                <xsl:with-param name="script-uri" select="$uri"/>
                                <xsl:with-param name="extends-uri" select="for $u in tokenize(@px:extends,'\s+')[not(.='')]
                                                                           return resolve-uri($u,base-uri(.))"/>
                                <xsl:with-param name="catalog-xml" select="/*"/>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="document($uri)[/p:*]"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="data-types" as="element()*">
                    <xsl:apply-templates select="$xproc/p:*/p:option/p:pipeinfo/pxd:type/*" mode="data-type-xml"/>
                </xsl:variable>
                <xsl:for-each select="$data-types">
                    <xsl:variable name="path" select="concat('/data-types/',replace(@id,'^.*:',''),'.xml')"/>
                    <xsl:result-document href="{concat($generatedResourcesDirectory,$path)}" method="xml">
                        <xsl:sequence select="."/>
                    </xsl:result-document>
                    <xsl:call-template name="data-type-class">
                        <xsl:with-param name="id" select="@id"/>
                        <xsl:with-param name="url" select="$path"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:if>
        </xsl:for-each>
        <!--
            process XProc files
        -->
        <xsl:apply-templates mode="process-xproc"/>
        <!--
            process catalog
        -->
        <xsl:variable name="catalog" as="node()*">
            <xsl:apply-templates/>
        </xsl:variable>
        <xsl:if test="$catalog/self::*">
            <xsl:result-document href="{$generatedResourcesDirectory}/META-INF/catalog.xml" method="xml">
                <xsl:copy>
                    <xsl:apply-templates select="@*"/>
                    <xsl:sequence select="$catalog"/>
                </xsl:copy>
            </xsl:result-document>
        </xsl:if>
        <!--
            generate Java files
        -->
        <xsl:if test="./child::*">
            <xsl:call-template name="module-class">
                <xsl:with-param name="catalog-xml" select="."/>
            </xsl:call-template>
        </xsl:if>
        <xsl:apply-templates mode="java"/>
    </xsl:template>
    
    <xsl:template name="module-class">
        <xsl:param name="catalog-xml" as="element(cat:catalog)" required="yes"/>
        <xsl:variable name="className" select="concat('Module_',replace($moduleName,'-','_'))"/>
        <xsl:result-document href="{$generatedSourcesDirectory}/org/daisy/pipeline/modules/impl/{$className}.java" method="text" xml:space="preserve"><c:data><xsl:value-of select="GenerateModuleClass:apply(GenerateModuleClass:new(),
                                                                         $className,
                                                                         $moduleName,
                                                                         $moduleVersion,
                                                                         $moduleTitle,
                                                                         $catalog-xml)"/></c:data></xsl:result-document>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="cat:uri">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="cat:uri[not(@name)]" priority="0.9"/>
    
    <xsl:template match="cat:uri[@px:content-type='params']" priority="1">
        <xsl:if test="@px:extends">
            <xsl:message terminate="yes">unexpected @px:extends: <xsl:sequence select="."/></xsl:message>
        </xsl:if>
        <xsl:if test="not(@name)">
            <xsl:message terminate="yes">missing @name: <xsl:sequence select="."/></xsl:message>
        </xsl:if>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:extends]">
        <xsl:copy>
            <xsl:apply-templates select="@* except @uri" mode="#current"/>
            <xsl:attribute name="uri" select="f:generated-href(@uri)"/>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="cat:uri[not(@px:extends)]">
        <xsl:variable name="uri" select="resolve-uri(@uri, base-uri(.))"/>
        <xsl:choose>
            <xsl:when test="doc-available($uri)">
                <xsl:choose>
                    <xsl:when test="document($uri)/p:*/p:option/p:pipeinfo/pxd:type">
                        <xsl:copy>
                            <xsl:apply-templates select="@* except @uri" mode="#current"/>
                            <xsl:attribute name="uri" select="f:generated-href(@uri)"/>
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
    
    <xsl:template match="cat:uri[@px:content-type=('calabash-config',
                                                   'liblouis-tables',
                                                   'libhyphen-tables')]|
                         cat:uri/@px:content-type[.=('script',
                                                     'xslt-package',
                                                     'data-type',
                                                     'params',
                                                     'user-agent-stylesheet',
                                                     'calabash-config',
                                                     'liblouis-tables',
                                                     'libhyphen-tables')]|
                         cat:uri/@px:extends|
                         cat:uri[@px:content-type=('data-type','script')]/@px:id|
                         cat:uri[@px:content-type='user-agent-stylesheet']/@px:stylesheet-type|
                         cat:uri[@px:content-type='user-agent-stylesheet']/@px:stylesheet-for-content-type|
                         cat:uri[@px:content-type='user-agent-stylesheet']/@px:stylesheet-for-media|
                         cat:nextCatalog"/>
    
    <xsl:template match="cat:uri[@px:content-type='script']" mode="java">
        <xsl:variable name="id" as="xs:string">
            <xsl:choose>
                <xsl:when test="@px:id">
                    <xsl:sequence select="@px:id"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="type" select="string(document(@uri,.)/*/@type)"/>
                    <xsl:sequence select="if (namespace-uri-for-prefix(substring-before($type,':'),document(@uri,.)/*)
                                              ='http://www.daisy.org/ns/pipeline/xproc')
                                          then substring-after($type,':')
                                          else $type"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <!--
            assuming catalog.xml is placed in META-INF
        -->
        <xsl:variable name="uri" select="resolve-uri(@uri, base-uri(.))"/>
        <xsl:variable name="uri" select="if (@px:extends or
                                             doc-available($uri) and document($uri)/p:*/p:option/p:pipeinfo/pxd:type)
                                         then f:generated-href(@uri)
                                         else @uri"/>
        <xsl:variable name="path" select="pf:normalize-path(concat('/META-INF/',$uri))"/>
        <xsl:variable name="desc" as="element()?" select="(document(@uri,.)//*[tokenize(@pxd:role,'\s+')='desc'])[1]"/>
        <xsl:variable name="desc" select="if ($desc/@xml:space='preserve')
                                          then tokenize(string($desc),'&#xa;')[1]
                                          else normalize-space(string($desc))"/>
        <xsl:call-template name="script-class">
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="desc" select="$desc"/>
            <xsl:with-param name="url" select="$path"/>
            <xsl:with-param name="version" select="$moduleVersion"/>
        </xsl:call-template>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template name="script-class">
        <xsl:param name="id" as="xs:string" required="yes"/>
        <xsl:param name="desc" as="xs:string" required="yes"/>
        <xsl:param name="url" as="xs:string" required="yes"/>
        <xsl:param name="version" as="xs:string" required="yes"/>
        <xsl:variable name="className" select="concat('XProcScript_',replace($id,'[:.-]','_'))"/>
        <xsl:result-document href="{$generatedSourcesDirectory}/org/daisy/pipeline/script/impl/{$className}.java"
                             method="text" xml:space="preserve"><c:data>package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "<xsl:value-of select="$id"/>",
	service = { XProcScriptService.class },
	property = {
		"script.id:String=<xsl:value-of select="$id"/>",
		"script.description:String=<xsl:value-of select="replace(replace($desc,'\\','\\\\'),'&quot;','\\&quot;')"/>",
		"script.url:String=<xsl:value-of select="$url"/>",
		"script.version:String=<xsl:value-of select="$version"/>"
	}
)
public class <xsl:value-of select="$className"/> extends XProcScriptService {
	@Activate
	public void activate(Map&lt;?,?&gt; properties) {
		super.activate(properties, <xsl:value-of select="$className"/>.class);
	}
}</c:data></xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:content-type='data-type']" mode="java">
        <xsl:variable name="id" select="@px:id"/>
        <!--
            assuming catalog.xml is placed in META-INF
        -->
        <xsl:variable name="path" select="pf:normalize-path(concat('/META-INF/',@uri))"/>
        <xsl:call-template name="data-type-class">
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="url" select="$path"/>
        </xsl:call-template>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template name="data-type-class">
        <xsl:param name="id" as="xs:string" required="yes"/>
        <xsl:param name="url" as="xs:string" required="yes"/>
        <xsl:variable name="className" select="concat('Datatype_',replace($id,'[:.-]','_'))"/>
        <xsl:result-document href="{$generatedSourcesDirectory}/org/daisy/pipeline/datatypes/impl/{$className}.java"
                             method="text" xml:space="preserve"><c:data>package org.daisy.pipeline.datatypes.impl;

import java.util.Map;
import javax.xml.transform.URIResolver;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.UrlBasedDatatypeService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "<xsl:value-of select="$id"/>",
	service = { DatatypeService.class },
	property = {
		"data-type.id:String=<xsl:value-of select="$id"/>",
		"data-type.url:String=<xsl:value-of select="$url"/>"
	}
)
public class <xsl:value-of select="$className"/> extends UrlBasedDatatypeService {
	@Activate
	public void activate(Map&lt;?,?&gt; properties) {
		super.activate(properties, <xsl:value-of select="$className"/>.class);
	}
}</c:data></xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:content-type='user-agent-stylesheet']" mode="java">
        <!--
            assuming catalog.xml is placed in META-INF
        -->
        <xsl:variable name="path" select="pf:normalize-path(concat('/META-INF/',@uri))"/>
        <xsl:call-template name="user-agent-stylesheet-class">
            <xsl:with-param name="path" select="$path"/>
            <xsl:with-param name="type" select="(@px:stylesheet-type,
                                                 if (ends-with($path,'.scss')) then 'text/x-scss' else (),
                                                 'text/css')[1]"/>
            <xsl:with-param name="content-type" select="@px:stylesheet-for-content-type/tokenize(.,'\s+')[not(.='')]"/>
            <xsl:with-param name="media" select="(@px:stylesheet-for-media,'all')[1]"/>
        </xsl:call-template>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template name="user-agent-stylesheet-class">
        <xsl:param name="path" as="xs:string" required="yes"/>
        <xsl:param name="type" as="xs:string" required="yes"/>
        <xsl:param name="content-type" as="xs:string*" required="yes"/>
        <xsl:param name="media" as="xs:string" required="yes"/>
        <xsl:variable name="className" select="concat('UserAgentStylesheet_',
                                                      string-join(for $t in $content-type return replace($t,'[^a-zA-Z]','_'),'_'),
                                                      '_',replace($media,'[^a-zA-Z]','_'))"/>
        <xsl:result-document href="{$generatedSourcesDirectory}/org/daisy/pipeline/css/impl/{$className}.java"
                             method="text" xml:space="preserve"><c:data>package org.daisy.pipeline.css.impl;

import java.net.URL;
import java.util.Arrays;
<xsl:if test="exists($content-type)"
>import java.util.Collection;
import java.util.Collections;
</xsl:if
>
import org.daisy.common.file.URLs;
import org.daisy.pipeline.css.UserAgentStylesheet;
import org.daisy.pipeline.css.Medium;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "<xsl:value-of select="$className"/>",
	service = { UserAgentStylesheet.class }
)
public class <xsl:value-of select="$className"/> implements UserAgentStylesheet {

	private final URL resource = URLs.getResourceFromJAR("<xsl:value-of select="$path"/>", <xsl:value-of select="$className"/>.class);
	private final String type = "<xsl:value-of select="replace(replace($type,'\\','\\\\'),'&quot;','\\&quot;')"/>";
	<xsl:if test="exists($content-type)"
	>private final Collection&lt;String&gt; contentTypes = Collections.unmodifiableList(
		Arrays.asList(<xsl:value-of select="string-join(for $t in $content-type return
		                                                concat('&quot;',replace(replace($t,'\\','\\\\'),'&quot;','\\&quot;'),'&quot;'),', ')"/>));
	</xsl:if
	>private final String mediaQuery = "<xsl:value-of select="replace(replace($media,'\\','\\\\'),'&quot;','\\&quot;')"/>";

	public URL getURL() {
		return resource;
	}

	public String getType() {
		return type;
	}

	public boolean matchesContentType(String contentType) {
		return <xsl:value-of select="if (empty($content-type))
		                             then 'true'
		                             else 'contentTypes.contains(contentType)'"/>;
	}

	public boolean matchesMedium(Medium medium) {
		return medium.matches(mediaQuery);
	}
}</c:data></xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:content-type='calabash-config']" mode="java">
        <!--
            assuming catalog.xml is placed in META-INF
        -->
        <xsl:variable name="path" select="pf:normalize-path(concat('/META-INF/',@uri))"/>
        <xsl:call-template name="calabash-config-class">
            <xsl:with-param name="path" select="$path"/>
        </xsl:call-template>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template name="calabash-config-class">
        <xsl:param name="path" as="xs:string" required="yes"/>
        <xsl:variable name="className" select="concat('ConfigurationFileProvider_',replace($moduleName,'-','_'))"/>
        <xsl:result-document href="{$generatedSourcesDirectory}/org/daisy/common/xproc/calabash/impl/{$className}.java"
                             method="text" xml:space="preserve"><c:data>package org.daisy.common.xproc.calabash.impl;

import java.util.Map;

import org.daisy.common.xproc.calabash.BundledConfigurationFileProvider;
import org.daisy.common.xproc.calabash.ConfigurationFileProvider;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "calabash-config-<xsl:value-of select="$moduleName"/>",
	service = { ConfigurationFileProvider.class },
	property = {
		"path:String=<xsl:value-of select="$path"/>",
	}
)
public class <xsl:value-of select="$className"/> extends BundledConfigurationFileProvider {
	@Activate
	public void activate(Map&lt;?,?&gt; properties) {
		super.activate(properties, <xsl:value-of select="$className"/>.class);
	}
}</c:data></xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:content-type='liblouis-tables']" mode="java">
        <!--
            assuming catalog.xml is placed in META-INF
        -->
        <xsl:variable name="path" select="pf:normalize-path(concat('/META-INF/',@uri))"/>
        <xsl:call-template name="liblouis-table-path-class">
            <xsl:with-param name="identifier" select="@name"/>
            <xsl:with-param name="path" select="$path"/>
            <xsl:with-param name="includes" select="(@px:include,'*')[1]"/>
        </xsl:call-template>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template name="liblouis-table-path-class">
        <xsl:param name="identifier" as="xs:string" required="yes"/>
        <xsl:param name="path" as="xs:string" required="yes"/>
        <xsl:param name="includes" as="xs:string" required="yes"/>
        <xsl:variable name="className" select="concat('LiblouisTablePath_',
                                                      replace(if (@px:id) then @px:id else $identifier,'[^a-zA-Z]','_'))"/>
        <xsl:result-document href="{$generatedSourcesDirectory}/org/daisy/pipeline/braille/liblouis/impl/{$className}.java"
                             method="text" xml:space="preserve"><c:data>package org.daisy.pipeline.braille.liblouis.impl;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "<xsl:value-of select="if (@px:id) then @px:id
	                              else concat('org.daisy.pipeline.braille.liblouis.impl.',$className)"/>",
	service = { org.daisy.pipeline.braille.liblouis.LiblouisTablePath.class },
	property = {
		"identifier:String=<xsl:value-of select="$identifier"/>",
		"path:String=<xsl:value-of select="$path"/>",
		"includes:String=<xsl:value-of select="$includes"/>"
	}
)
public class <xsl:value-of select="$className"/> extends org.daisy.pipeline.braille.liblouis.LiblouisTablePath {
	@Activate
	public void activate(Map&lt;?,?&gt; properties) {
		super.activate(properties, <xsl:value-of select="$className"/>.class);
	}
}</c:data></xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:content-type='libhyphen-tables']" mode="java">
        <!--
            assuming catalog.xml is placed in META-INF
        -->
        <xsl:variable name="path" select="pf:normalize-path(concat('/META-INF/',@uri))"/>
        <xsl:call-template name="libhyphen-table-path-class">
            <xsl:with-param name="identifier" select="@name"/>
            <xsl:with-param name="path" select="$path"/>
            <xsl:with-param name="includes" select="(@px:include,'*')[1]"/>
        </xsl:call-template>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template name="libhyphen-table-path-class">
        <xsl:param name="identifier" as="xs:string" required="yes"/>
        <xsl:param name="path" as="xs:string" required="yes"/>
        <xsl:param name="includes" as="xs:string" required="yes"/>
        <xsl:variable name="className" select="concat('LibhyphenTablePath_',
                                                      replace(if (@px:id) then @px:id else $identifier,'[^a-zA-Z]','_'))"/>
        <xsl:result-document href="{$generatedSourcesDirectory}/org/daisy/pipeline/braille/libhyphen/impl/{$className}.java"
                             method="text" xml:space="preserve"><c:data>package org.daisy.pipeline.braille.libhyphen.impl;

import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "<xsl:value-of select="if (@px:id) then @px:id
	                              else concat('org.daisy.pipeline.braille.libhyphen.impl.',$className)"/>",
	service = { org.daisy.pipeline.braille.libhyphen.LibhyphenTablePath.class },
	property = {
		"identifier:String=<xsl:value-of select="$identifier"/>",
		"path:String=<xsl:value-of select="$path"/>",
		"includes:String=<xsl:value-of select="$includes"/>"
	}
)
public class <xsl:value-of select="$className"/> extends org.daisy.pipeline.braille.libhyphen.LibhyphenTablePath {
	@Activate
	public void activate(Map&lt;?,?&gt; properties) throws IllegalArgumentException {
		super.activate(properties, <xsl:value-of select="$className"/>.class);
	}
}</c:data></xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:export-functions]" mode="java">
        <xsl:variable name="uri-element" as="element()" select="."/>
        <xsl:call-template name="xslt-functions-class">
            <xsl:with-param name="path" select="pf:normalize-path(concat('/META-INF/',@uri))"/>
            <xsl:with-param name="functions"
                            select="for $f in @px:export-functions/tokenize(.,'\s+')[not(.='')]
                                    return resolve-QName($f,$uri-element)"/>
        </xsl:call-template>
        <xsl:next-match/>
    </xsl:template>
    
    <xsl:template name="xslt-functions-class">
        <xsl:param name="path" required="yes"/>
        <xsl:param name="functions" as="xs:QName*" required="yes"/>
        <xsl:variable name="className" select="concat('XsltFunctions_',replace($moduleName,'-','_'))"/>
        <xsl:result-document href="{$generatedSourcesDirectory}/org/daisy/common/xpath/saxon/impl/{$className}.java"
                             method="text" xml:space="preserve"><c:data>package org.daisy.common.xpath.saxon.impl;

import java.util.ArrayList;
import java.util.Collection;

import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.StructuredQName;

import org.daisy.common.file.URLs;
import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.XsltFunction;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "<xsl:value-of select="concat('org.daisy.common.xpath.saxon.impl.',$className)"/>",
	service = { ExtensionFunctionProvider.class }
)
public class <xsl:value-of select="$className"/> implements ExtensionFunctionProvider {

	private final Collection&lt;ExtensionFunctionDefinition&gt; functions;

	public <xsl:value-of select="$className"/>() {
		functions = new ArrayList&lt;&gt;();<xsl:for-each select="$functions">
		functions.add(
			new XsltFunction(
				URLs.getResourceFromJAR("<xsl:value-of select="$path"/>", <xsl:value-of select="$className"/>.class),
				new StructuredQName("",
				                    "<xsl:value-of select="namespace-uri-from-QName(.)"/>",
				                    "<xsl:value-of select="local-name-from-QName(.)"/>")));</xsl:for-each>
	}

	@Override
	public Collection&lt;ExtensionFunctionDefinition&gt; getDefinitions() {
		return functions;
	}
}</c:data></xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[@px:extends]" mode="process-xproc">
        <xsl:result-document href="{resolve-uri(f:generated-href(@uri),concat($generatedResourcesDirectory,'/META-INF/catalog.xml'))}"
                             method="xml">
            <xsl:variable name="uri" select="resolve-uri(@uri, base-uri(.))"/>
            <xsl:variable name="doc">
                <xsl:call-template name="extend-script">
                    <xsl:with-param name="script-uri" select="$uri"/>
                    <xsl:with-param name="extends-uri" select="for $u in tokenize(@px:extends,'\s+')[not(.='')]
                                                               return resolve-uri($u,base-uri(.))"/>
                    <xsl:with-param name="catalog-xml" select="/*"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$doc/p:*/p:option/p:pipeinfo/pxd:type">
                    <xsl:apply-templates select="$doc" mode="finalize-script">
                        <xsl:with-param name="script-uri" select="$uri" tunnel="yes"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$doc"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:result-document>
    </xsl:template>
    
    <xsl:template match="cat:uri[not(@px:extends)][@name or @px:content-type='script']" mode="process-xproc">
        <xsl:variable name="uri" select="resolve-uri(@uri, base-uri(.))"/>
        <xsl:if test="doc-available($uri)">
            <xsl:variable name="doc" select="document($uri)"/>
            <xsl:if test="$doc/p:*/p:option/p:pipeinfo/pxd:type">
                <xsl:result-document href="{resolve-uri(f:generated-href(@uri),concat($generatedResourcesDirectory,'/META-INF/catalog.xml'))}"
                                     method="xml">
                    <xsl:apply-templates select="$doc" mode="finalize-script">
                        <xsl:with-param name="script-uri" select="$uri" tunnel="yes"/>
                    </xsl:apply-templates>
                </xsl:result-document>
            </xsl:if>
        </xsl:if>
    </xsl:template>
    
    <xsl:function name="f:generated-href">
        <xsl:param name="uri" as="xs:string"/>
        <xsl:value-of select="replace($uri,'^(.*/)?([^/]+)$','$1__processed__$2')"/>
    </xsl:function>
    
    <xsl:template match="/*/p:option[p:pipeinfo/pxd:type]" mode="finalize-script">
        <xsl:param name="script-uri" tunnel="yes" required="yes"/>
        <xsl:variable name="catalog-xml" as="element(cat:catalog)" select="collection()[1]/*"/>
        <xsl:choose>
            <xsl:when test="exists($catalog-xml//cat:uri[@name or @px:content-type='script']
                                                        [resolve-uri(@uri,base-uri(.))=$script-uri])">
                <xsl:copy>
                    <xsl:apply-templates select="@*" mode="#current"/>
                    <xsl:apply-templates select="p:pipeinfo/pxd:type" mode="data-type-attribute"/>
                    <xsl:apply-templates select="node()" mode="#current"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo" mode="finalize-script">
        <xsl:if test="* except pxd:type">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:type" mode="finalize-script"/>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:type" mode="data-type-attribute">
        <xsl:attribute name="pxd:type">
            <xsl:apply-templates select="." mode="data-type-id"/>
        </xsl:attribute>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:type/*" mode="data-type-xml">
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:attribute name="id">
                <xsl:apply-templates select="parent::*" mode="data-type-id"/>
            </xsl:attribute>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/*/p:option/p:pipeinfo/pxd:type" mode="data-type-id" as="xs:string">
        <!-- FIXME: make @type optional -->
        <xsl:if test="not(/*/@type)">
            <xsl:message terminate="yes">missing @type: <xsl:sequence select="/*"/></xsl:message>
        </xsl:if>
        <xsl:sequence select="concat(/*/@type,'-',
                                     for $name in parent::*/parent::*/@name return
                                     if (contains($name,':')) then substring-after($name,':') else $name)"/>
    </xsl:template>
    
    <xsl:template match="@*|node()" mode="data-type-xml">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
