<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline"
                xmlns:pxd="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
                exclude-result-prefixes="#all" version="2.0">
    
    <!-- recursive template allowing scripts to inherit from scripts that inherit from scripts -->
    <xsl:template name="extend-script">
        <xsl:param name="script-uri"/>
        <xsl:param name="extends-uri" as="xs:string*"/>
        <xsl:param name="catalog-xml" as="element()"/>
        <xsl:if test="not(doc-available($script-uri))">
            <xsl:message terminate="yes" select="concat('Unable to resolve: ', $script-uri)"/>
        </xsl:if>
        <xsl:variable name="script-doc" select="document($script-uri)"/>
        <xsl:variable name="extends-doc" as="document-node()*">
            <xsl:for-each select="$extends-uri">
                <xsl:variable name="extends-uri" select="."/>
                <xsl:variable name="extends-uri-element" as="element()?"
                              select="$catalog-xml//cat:uri[@name=$extends-uri or
                                                            resolve-uri(@uri,base-uri(.))=$extends-uri]"/>
                <xsl:choose>
                    <xsl:when test="$extends-uri-element/@px:extends">
                        <xsl:variable name="extends-uri" select="$extends-uri-element/resolve-uri(@uri,base-uri(.))"/>
                        <xsl:variable name="doc">
                            <xsl:call-template name="extend-script">
                                <xsl:with-param name="script-uri" select="$extends-uri"/>
                                <xsl:with-param name="extends-uri"
                                                select="for $u in tokenize($extends-uri-element/@px:extends,'\s+')[not(.='')]
                                                        return resolve-uri($u,base-uri($extends-uri-element))"/>
                                <xsl:with-param name="catalog-xml" select="$catalog-xml"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:document>
                            <xsl:apply-templates select="$doc" mode="finalize-script">
                                <xsl:with-param name="script-uri" tunnel="yes" select="$extends-uri"/>
                            </xsl:apply-templates>
                        </xsl:document>
                    </xsl:when>
                    <xsl:when test="$extends-uri-element">
                        <xsl:variable name="extends-uri" select="$extends-uri-element/resolve-uri(@uri,base-uri(.))"/>
                        <xsl:if test="not(doc-available($extends-uri))">
                            <xsl:message terminate="yes" select="concat('Unable to resolve: ', $extends-uri)"/>
                        </xsl:if>
                        <xsl:sequence select="document($extends-uri)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:if test="not(doc-available($extends-uri))">
                            <xsl:message terminate="yes" select="concat('Unable to resolve: ', $extends-uri)"/>
                        </xsl:if>
                        <xsl:sequence select="document($extends-uri)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:variable>
        <xsl:apply-templates select="$script-doc" mode="extend-script">
            <xsl:with-param name="original-script" select="$extends-doc" tunnel="yes"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template mode="extend-script"
                  match="/p:declare-step[p:input[@kind='parameter' and @pxd:options]]">
        <xsl:param name="original-script" as="document-node()*" tunnel="yes"/>
        <xsl:variable name="inner" as="element(p:declare-step)">
            <xsl:next-match/>
        </xsl:variable>
        <xsl:variable name="generated-names" as="xs:string*">
            <xsl:call-template name="generate-ids">
                <xsl:with-param name="amount" select="2"/>
                <xsl:with-param name="prefix" select="'step'"/>
                <xsl:with-param name="in-use" select="$inner//@name"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="outer-name" select="$generated-names[1]"/>
        <xsl:variable name="inner-name" select="$generated-names[2]"/>
        <xsl:variable name="inner-type" select="if (@type)
                                                then substring-after(@type,':')
                                                else 'step'"/>
        <xsl:variable name="private-namespace" select="'org.daisy.pipeline.build/modules-build-helper/extend-script.xsl'"/>
        <p:declare-step version="1.0" name="{$outer-name}">
            <xsl:if test="@type">
                <xsl:namespace name="{substring-before(@type,':')}"
                               select="namespace-uri-for-prefix(substring-before(@type,':'),$inner)"/>
                <xsl:sequence select="@type"/>
            </xsl:if>
            <xsl:for-each select="$inner/(p:documentation|p:input|p:output|p:option)">
                <xsl:choose>
                    <xsl:when test="self::p:input[@kind='parameter' and @pxd:options]">
                        <xsl:variable name="option-namespaces" as="xs:string*" select="tokenize(@pxd:options,'\s+')[not(.='')]"/>
                        <xsl:for-each select="$original-script/*/p:option
                                              [@name[contains(.,':') and
                                                     namespace-uri-for-prefix(substring-before(.,':'),..)=$option-namespaces]]">
                            <xsl:copy>
                                <xsl:namespace name="{substring-before(@name,':')}"
                                               select="namespace-uri-for-prefix(substring-before(@name,':'),.)"/>
                                <xsl:sequence select="@*|node()"/>
                            </xsl:copy>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="self::p:option[contains(@name,':')]">
                        <xsl:copy>
                            <xsl:namespace name="{substring-before(@name,':')}"
                                           select="namespace-uri-for-prefix(substring-before(@name,':'),.)"/>
                            <xsl:sequence select="@*|node()"/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:when test="self::p:output">
                        <xsl:copy>
                            <xsl:sequence select="@*|p:documentation|p:pipeinfo"/>
                            <p:pipe step="{$inner-name}" port="{@port}"/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            <xsl:for-each select="$inner">
                <xsl:copy>
                    <xsl:namespace name="ex" select="$private-namespace"/>
                    <xsl:sequence select="@* except (@type,@version)"/>
                    <xsl:attribute name="type" select="concat('ex:', $inner-type)"/>
                    <xsl:for-each select="node() except p:documentation">
                        <xsl:choose>
                            <xsl:when test="self::p:input|self::p:output|self::p:option">
                                <xsl:copy>
                                    <xsl:sequence select="(@* except @pxd:*)|
                                                          (node() except (p:documentation|p:pipeinfo))"/>
                                </xsl:copy>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:sequence select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:copy>
            </xsl:for-each>
            <xsl:element name="{$inner-type}" namespace="{$private-namespace}">
                <xsl:attribute name="name" select="$inner-name"/>
                <xsl:attribute name="pxd:progress" select="1"/>
                <xsl:for-each select="$inner/p:input">
                    <xsl:choose>
                        <xsl:when test="@kind='parameter' and @pxd:options">
                            <xsl:variable name="port" select="@port"/>
                            <xsl:variable name="option-namespaces" as="xs:string*" select="tokenize(@pxd:options,'\s+')[not(.='')]"/>
                            <xsl:for-each select="$original-script/*/p:option
                                                  [@name[contains(.,':') and
                                                         namespace-uri-for-prefix(substring-before(.,':'),..)=$option-namespaces]]">
                                <p:with-param port="{$port}" name="{@name}" select="${@name}">
                                    <xsl:namespace name="{substring-before(@name,':')}"
                                                   select="namespace-uri-for-prefix(substring-before(@name,':'),.)"/>
                                </p:with-param>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <p:input port="{@port}">
                                <p:pipe step="{$outer-name}" port="{@port}"/>
                            </p:input>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
                <xsl:for-each select="$inner/p:option">
                    <p:with-option name="{@name}" select="${@name}">
                        <xsl:if test="contains(@name,':')">
                            <xsl:namespace name="{substring-before(@name,':')}"
                                           select="namespace-uri-for-prefix(substring-before(@name,':'),.)"/>
                        </xsl:if>
                    </p:with-option>
                </xsl:for-each>
            </xsl:element>
        </p:declare-step>
    </xsl:template>

    <xsl:template mode="extend-script"
                  match="/*/p:input[@port]|
                         /*/p:output[@port]|
                         /*/p:option[@name]">
        <xsl:param name="original-script" as="document-node()*" tunnel="yes"/>
        <xsl:variable name="original-input-output-option" as="element()?"
                      select="if (self::p:input)
                              then $original-script/*/p:input[@port=current()/@port]
                              else if (self::p:output)
                              then $original-script/*/p:output[@port=current()/@port]
                              else if (not(contains(@name,':')))
                              then $original-script/*/p:option[@name=current()/@name]
                              else for $option-local-name in @name/substring-after(.,':') return
                                   for $option-namespace in @name/namespace-uri-for-prefix(substring-before(.,':'),..) return
                                   $original-script/*/p:option[
                                     $option-local-name=@name/substring-after(.,':') and
                                     $option-namespace=@name/namespace-uri-for-prefix(substring-before(.,':'),..)
                                   ]
                              "/>
        <xsl:variable name="new-attributes" as="xs:string*" select="@*/concat('{',namespace-uri(.),'}',name(.))"/>
        <xsl:copy>
            <xsl:if test="not(@cx:as)">
                <xsl:for-each select="$original-input-output-option/@cx:as">
                    <xsl:if test="contains(string(.),':')">
                        <xsl:namespace name="{substring-before(string(.),':')}"
                                       select="namespace-uri-for-prefix(substring-before(.,':'),parent::*)"/>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:sequence select="$original-input-output-option
                                  /@*[not(concat('{',namespace-uri(.),'}',name(.))=$new-attributes
                                          or concat('{',namespace-uri(.),'}',name(.))='{}select'
                                             and current()/@required = 'true'
                                          or concat('{',namespace-uri(.),'}',name(.))='{http://www.daisy.org/ns/pipeline/xproc}type'
                                             and current()/p:pipeinfo/pxd:type)]"/>
            <xsl:if test="not(p:pipeinfo)
                          and not(@pxd:type)
                          and $original-input-output-option/p:pipeinfo/pxd:type">
                <p:pipeinfo>
                    <xsl:sequence select="$original-input-output-option/p:pipeinfo/pxd:type"/>
                </p:pipeinfo>
            </xsl:if>
            <xsl:if test="not(p:documentation)">
                <xsl:sequence select="$original-input-output-option/p:documentation"/>
            </xsl:if>
            <xsl:apply-templates select="node()" mode="#current">
                <xsl:with-param name="original-input-output-option" select="$original-input-output-option" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="extend-script"
                  match="/*/p:input/p:pipeinfo|
                         /*/p:output/p:pipeinfo|
                         /*/p:option/p:pipeinfo">
        <xsl:param name="original-input-output-option" as="element()?" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:if test="not(pxd:type) and not(parent::*/@pxd:type)">
                <xsl:sequence select="$original-input-output-option/p:pipeinfo/pxd:type"/>
            </xsl:if>
            <xsl:apply-templates mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="extend-script"
                  match="/*/p:input/p:documentation|
                         /*/p:output/p:documentation|
                         /*/p:option/p:documentation">
        <xsl:param name="original-input-output-option" as="element()?" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@*" mode="#current"/>
            <xsl:if test="not(descendant::*[tokenize(@pxd:role,'\s+')='name'])">
                <xsl:sequence select="$original-input-output-option/p:documentation/*[tokenize(@pxd:role,'\s+')='name']"/>
            </xsl:if>
            <xsl:apply-templates mode="#current"/>
            <xsl:if test="not(descendant::*[tokenize(@pxd:role,'\s+')='desc'])">
                <xsl:sequence select="$original-input-output-option/p:documentation/*[tokenize(@pxd:role,'\s+')='desc']"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="extend-script"
                  match="*[tokenize(@pxd:role,'\s+')=('name','desc')]">
        <xsl:param name="original-input-output-option" as="element()?" tunnel="yes"/>
        <xsl:copy>
            <xsl:apply-templates select="@* except @pxd:inherit" mode="#current"/>
            <xsl:if test="@pxd:inherit = 'prepend'">
                <xsl:copy-of select="$original-input-output-option
                                     //*[tokenize(@pxd:role,'\s+')=current()/tokenize(@pxd:role,'\s+')]/node()"/>
                <xsl:text><![CDATA[

]]></xsl:text>
            </xsl:if>
            <xsl:copy-of select="node()"/>
            <xsl:if test="@pxd:inherit = 'append'">
                <xsl:text><![CDATA[

]]></xsl:text>
                <xsl:copy-of select="$original-input-output-option
                                     //*[tokenize(@pxd:role,'\s+')=current()/tokenize(@pxd:role,'\s+')]/node()"/>
            </xsl:if>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="finalize-script extend-script"
                  match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template name="generate-ids" as="xs:string*">
        <xsl:param name="amount" as="xs:integer" required="yes"/>
        <xsl:param name="prefix" as="xs:string" required="yes"/>
        <xsl:param name="in-use" as="xs:string*" select="()"/>
        <xsl:param name="_feed" as="xs:integer" select="1"/>
        <xsl:variable name="id" select="concat($prefix,$_feed)"/>
        <xsl:choose>
            <xsl:when test="$id=$in-use">
                <xsl:call-template name="generate-ids">
                    <xsl:with-param name="amount" select="$amount"/>
                    <xsl:with-param name="prefix" select="$prefix"/>
                    <xsl:with-param name="in-use" select="$in-use"/>
                    <xsl:with-param name="_feed" select="$_feed + 1"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$id"/>
                <xsl:if test="$amount &gt; 1">
                    <xsl:call-template name="generate-ids">
                        <xsl:with-param name="amount" select="$amount - 1"/>
                        <xsl:with-param name="prefix" select="$prefix"/>
                        <xsl:with-param name="in-use" select="$in-use"/>
                        <xsl:with-param name="_feed" select="$_feed + 1"/>
                    </xsl:call-template>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
