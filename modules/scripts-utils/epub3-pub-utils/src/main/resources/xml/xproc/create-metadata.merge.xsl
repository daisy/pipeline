<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns="http://www.idpf.org/2007/opf" xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all"
    xpath-default-namespace="http://www.idpf.org/2007/opf">

    <!--=========================================-->
    <!-- Merges EPUB Publications Metadata
        
         Input: 
           a set of 'metadata' element in the OPF
           namespace, wrapped in a common root element
           (the name of the wrapper is insignificant)
           
         Output:
           a single 'metadata' element in the OPF
           namespace, containing the 'merged' metadata.
    -->
    <!--TODO: document merge rules. For now, see the tests.-->
    <!--=========================================-->

    <xsl:output indent="yes"/>

    <xsl:key name="refines" match="//meta[@refines]" use="f:unified-id(@refines)"/>

    <xsl:template match="/*">
        <metadata>
            <xsl:variable name="unified-prefix-decl" as="element()*"
                select="f:unified-prefix-decl(/)"/>
            <xsl:if test="exists($unified-prefix-decl)">
                <xsl:attribute name="prefix"
                    select="for $vocab in $unified-prefix-decl return concat($vocab/@prefix,': ',$vocab/@uri)"
                />
            </xsl:if>

            <!-- dc:title(s) and refines -->
            <xsl:variable name="title" select="(//metadata/dc:title[not(@refines)])[1]"/>
            <xsl:apply-templates select="$title"/>
            <xsl:apply-templates
                select="$title/ancestor::metadata/dc:title[not(@refines) and (. != $title)]"/>

            <!-- dc:identifier(s) and refines -->
            <xsl:variable name="identifier" select="(//metadata/dc:identifier[not(@refines)])[1]"/>
            <xsl:apply-templates select="$identifier"/>
            <xsl:apply-templates
                select="$identifier/ancestor::metadata/dc:identifier[not(@refines) and (. != $identifier)]"/>

            <!-- dc:language(s) and refines -->
            <xsl:variable name="language" select="(//metadata/dc:language[not(@refines)])[1]"/>
            <xsl:apply-templates select="$language"/>
            <xsl:apply-templates
                select="$language/ancestor::metadata/dc:language[not(@refines) and (. != $language)]"/>

            <!--generate dc:modified-->
            <meta property="dcterms:modified">
                <xsl:value-of
                    select="format-dateTime(
                    adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H')),
                    '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z')"
                />
            </meta>

            <!--DCMES Optional Elements [0 or more]
               * NOTE: several dc:type are allowed in EPUB 3.01 
               * only one: date | source
            -->
            <xsl:for-each-group
                select="//(dc:contributor|dc:coverage|dc:creator|dc:date|dc:description|dc:format
                          |dc:publisher|dc:relation|dc:rights|dc:source|dc:subject|dc:type)[empty(@refines)]"
                group-by="name()">
                <xsl:choose>
                    <xsl:when test="self::dc:date|self::dc:source">
                        <xsl:apply-templates select="current()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates
                            select="current-group()[ancestor::metadata is current()/ancestor::metadata]"
                        />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each-group>

            <!--meta [0 or more]-->
            <xsl:for-each-group select="//meta[empty(@refines)]"
                group-by="f:expand-property(@property)/@uri">
                <xsl:if test="current-grouping-key()">
                    <xsl:apply-templates
                        select="current-group()[ancestor::metadata is current()/ancestor::metadata]"
                    />
                </xsl:if>
            </xsl:for-each-group>

            <xsl:apply-templates select="//link"/>
        </metadata>
    </xsl:template>

    <xsl:template match="dc:identifier">
        <dc:identifier id="{f:unique-id((@id,generate-id())[1],//@id except @id)}">
            <xsl:apply-templates select="node() | @* except @id"/>
        </dc:identifier>
        <xsl:apply-templates select="key('refines',f:unified-id(@id))"/>
    </xsl:template>

    <xsl:template match="dc:*">
        <xsl:next-match/>
        <xsl:apply-templates select="key('refines',f:unified-id(@id))"/>
    </xsl:template>

    <xsl:template match="meta">
        <xsl:variable name="property" select="f:expand-property(@property)"/>
        <xsl:choose>
            <xsl:when test="$property/@uri='http://purl.org/dc/terms/modified'"/>
            <xsl:when test="not(normalize-space())">
                <xsl:message>[WARNING] Discarding empty property '<xsl:value-of select="@property"
                    />'.</xsl:message>
            </xsl:when>
            <xsl:when test="$property/@uri=''">
                <xsl:message>[WARNING] Discarding property '<xsl:value-of select="@property"/>' from
                    an undeclared vocab.</xsl:message>
            </xsl:when>
            <xsl:when
                test="$property/@prefix='' and $property/@name=('display-seq','meta-auth')">
                <xsl:message>[WARNING] The deprecated property '<xsl:value-of select="@property"
                    />' was found.</xsl:message>
                    <xsl:next-match/>
            </xsl:when>
            <xsl:when
                test="$property/@prefix='' and not($property/@name=('alternate-script','display-seq',
                'file-as','group-position','identifier-type','meta-auth','role','title-type'))">
                <xsl:message>[WARNING] Discarding unknown property '<xsl:value-of select="@property"
                    />'.</xsl:message>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="key('refines',f:unified-id(@id))"/>
    </xsl:template>

    <xsl:template match="link">
        <xsl:variable name="rel" select="f:expand-property(@rel)"/>
        <xsl:choose>
            <xsl:when test="not(@href) or not(@rel)">
                <xsl:message>[WARNING] Discarding link with no @href or @rel
                    attributes.</xsl:message>
            </xsl:when>
            <!-- EPUB3.2: values marc21xml-record, mods-record, onix-record, and xmp-signature of @rel are deprecated and should be replaced by 'record'-->
            <xsl:when
                test="$rel/@prefix='' and $rel/@name=('marc21xml-record',
                'mods-record','onix-record','xml-signature xmp-record')">
                <xsl:message>[WARNING] Found link with deprecated @rel value '<xsl:value-of
                        select="@rel"/>'. This value should be replaced by 'record' with a corresponding 'media-type' attribute.</xsl:message>
                    <xsl:next-match/>
            </xsl:when>
            <xsl:when
                test="$rel/@prefix='' and not($rel/@name=('marc21xml-record',
                'mods-record','onix-record','xml-signature xmp-record','record','acquire','alternate'))">
                <xsl:message>[WARNING] Discarding link with unknown @rel value '<xsl:value-of
                        select="@rel"/>'.</xsl:message>
            </xsl:when>
            <xsl:when test="$rel/@uri=''">
                <xsl:message>[WARNING] Discarding link with @rel value '<xsl:value-of
                        select="@property"/>' from an undeclared vocab.</xsl:message>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@property">
        <xsl:attribute name="property" select="f:expand-property(.)/@name"/>
    </xsl:template>
    <xsl:template match="@scheme">
        <xsl:attribute name="scheme" select="f:expand-property(.)/@name"/>
    </xsl:template>
    <xsl:template match="@id">
        <xsl:attribute name="id" select="f:unified-id(.)"/>
    </xsl:template>
    <xsl:template match="@refines">
        <xsl:attribute name="refines" select="concat('#',f:unified-id(.))"/>
    </xsl:template>


    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/phony" xpath-default-namespace="">
        <!-- avoid SXXP0005 warning -->
        <xsl:next-match/>
    </xsl:template>

    <!-- 
        Returns a non-conflicting ID for an *existing* ID or IDREF attribute
        The non-conflicting ID is created by appending the position number
        of the parent metadata set for all sets after the first
    -->
    <xsl:function name="f:unified-id" as="xs:string">
        <xsl:param name="id" as="attribute()?"/>
        <xsl:variable name="count" select="count($id/ancestor::metadata/preceding-sibling::*)"
            as="xs:integer"/>
        <xsl:sequence
            select="concat(if (starts-with($id,'#')) then substring($id,2) else $id, if ($count) then $count+1 else '')"
        />
    </xsl:function>

    
    <!-- 
        Returns a non-conflicting ID for a *new* ID or IDREF attribute, given
        a sequence of existing IDs.
        The non-conflicting ID is created by prepending a number of 'x'
        to the ID until it doesn't conflict with existing ones.
        This rule guarantees it won't conflict with IDs created by f:unified-id().
    -->
    <xsl:function name="f:unique-id" as="xs:string">
        <xsl:param name="id" as="xs:string"/>
        <xsl:param name="existing" as="xs:string*"/>
        <xsl:sequence
            select="
            if (not($id=$existing)) then $id
            else f:unique-id(concat('x',$id),$existing)
            "
        />
    </xsl:function>

    <!--
        Returns a sequence of `f:property` elements from a property-typeed attribute where:
        
         * @prefix contains the resolved, unified prefix for the property
         * @uri contains the resolved absolute URI of the property
         * @name contains the resolved name for the property, prefixed by the unified prefix
    -->
    <!--TODO move to a generic util ?-->
    <xsl:function name="f:expand-property" as="element(f:property)">
        <xsl:param name="property" as="attribute()?"/>
        <!--TODO move to a global variable when XSpec supports it-->
        <xsl:variable name="all-prefix-decl" as="element()*"
            select="f:all-prefix-decl($property/ancestor::node()[. instance of document-node()])"/>
        <xsl:variable name="unified-prefix-decl" as="element()*"
            select="f:unified-prefix-decl($property/ancestor::node()[. instance of document-node()])"/>
        <xsl:variable name="prefix" select="substring-before($property,':')" as="xs:string"/>
        <xsl:variable name="reference" select="replace($property,'(.+:)','')" as="xs:string"/>
        <xsl:variable name="vocab"
            select="
            ($all-prefix-decl[@id=generate-id($property/ancestor::metadata)]/f:vocab[@prefix=$prefix]/@uri,
            if ($prefix='') then 'http://idpf.org/epub/vocab/package/#'
            else if ($prefix='dcterms') then 'http://purl.org/dc/terms/'
            else if ($prefix='marc') then 'http://id.loc.gov/vocabulary/'
            else if ($prefix='media') then 'http://www.idpf.org/epub/vocab/overlays/#'
            else if ($prefix='onix') then 'http://www.editeur.org/ONIX/book/codelists/current.html#'
            else if ($prefix='xsd') then 'http://www.w3.org/2001/XMLSchema#'
            else '')[1]
            "
            as="xs:string"/>
        <xsl:variable name="unified-prefix"
            select="
            if ($vocab='http://idpf.org/epub/vocab/package/#') then '' 
            else if ($vocab='http://purl.org/dc/terms/') then 'dcterms' 
            else if ($vocab='http://id.loc.gov/vocabulary/') then 'marc'
            else if ($vocab='http://www.idpf.org/epub/vocab/overlays/#') then 'media'
            else if ($vocab='http://www.editeur.org/ONIX/book/codelists/current.html#') then 'onix'
            else if ($vocab='http://www.w3.org/2001/XMLSchema#') then 'xsd'
            else $unified-prefix-decl[@uri=$vocab]/@prefix"
            as="xs:string?"/>
        <f:property prefix="{$unified-prefix}"
            uri="{if($vocab) then concat($vocab,$reference) else ''}"
            name="{if ($unified-prefix) then concat($unified-prefix,':',$reference)  else $reference}"
        />
    </xsl:function>

    <!--
        Returns a sequence of `f:vocab` elements representing vocab declarations
        in a `@prefix` attribute where:
        
         * @prefix contains the declared prefix
         * @uri contains the vocab URI
    -->
    <xsl:function name="f:parse-prefix-decl" as="element(f:vocab)*">
        <xsl:param name="prefix-declaration" as="xs:string?"/>
        <xsl:for-each-group select="tokenize($prefix-declaration,'\s+')"
            group-adjacent="(position()+1) idiv 2">
            <f:vocab prefix="{substring-before(current-group()[1],':')}" uri="{current-group()[2]}"
            />
        </xsl:for-each-group>
    </xsl:function>


    <!--
        Returns all the vocabs declared in the various metadata sets, as `f:vocab` elements
        grouped by `metadata` elements (these latter having `@id` attributes generated by
        `generate-id()`. 
        
        Vocabs that are not used in `@property`, `@scheme` or `@rel` are discarded.
    -->
    <xsl:function name="f:all-prefix-decl" as="element()*">
        <xsl:param name="doc" as="document-node()?"/>
        <xsl:for-each select="$doc//metadata">
            <metadata id="{generate-id(.)}">
                <xsl:sequence
                    select="f:parse-prefix-decl(@prefix)
                    [some $prop in $doc//meta/(@property|@scheme)|$doc//link/@rel
                     satisfies starts-with($prop,concat(@prefix,':'))]"
                />
            </metadata>
        </xsl:for-each>
    </xsl:function>

    <!--
        Returns a sequence of `f:vocab` elements representing unified vocab declarations
    throughout the document passed as argument.
        
        * reserved vocabs are discarded (don't have to be declared)
        * @prefix are unified, if it is overriding a reserved prefix, a new prefix is defined
        
    -->
    <xsl:function name="f:unified-prefix-decl" as="element()*">
        <xsl:param name="doc" as="document-node()"/>
        <xsl:variable name="all-decl" select="f:all-prefix-decl($doc)//f:vocab"
            as="element(f:vocab)*"/>
        <xsl:for-each-group select="$all-decl" group-by="@uri">
            <xsl:if
                test="not(current-grouping-key()=
                ('http://idpf.org/epub/vocab/package/#',
                'http://purl.org/dc/terms/',
                'http://id.loc.gov/vocabulary/',
                'http://www.idpf.org/epub/vocab/overlays/#',
                'http://www.editeur.org/ONIX/book/codelists/current.html#',
                'http://www.w3.org/2001/XMLSchema#'))">
                <xsl:choose>
                    <xsl:when test="@prefix=('dcterms','marc','media','onix','xsd')">
                        <f:vocab
                            prefix="{f:unique-prefix(concat(@prefix,position()+1),$all-decl/@prefix)}"
                            uri="{@uri}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="current()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each-group>
    </xsl:function>

    <!--
        Returns a unique prefix from a given prefix and a sequence of existing prefixes.
        The unique prefix is generated by appending the needed amount of '_'.
    -->
    <xsl:function name="f:unique-prefix" as="xs:string">
        <xsl:param name="prefix" as="xs:string"/>
        <xsl:param name="existing" as="xs:string*"/>
        <xsl:sequence
            select="
            if (not($prefix=$existing)) then $prefix
            else f:unique-prefix(concat($prefix,'_'),$existing)
            "
        />
    </xsl:function>

</xsl:stylesheet>
