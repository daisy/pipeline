<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

    <xsl:include href="epub3-vocab.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>

    <!--
        prefix declarations have been previously normalized by px:epub3-merge-prefix according
        to $reserved-prefixes
    -->

    <xsl:param name="reserved-prefixes" required="yes"/>
    <xsl:param name="log-conflicts" required="yes"/>

    <!-- existing ids outside metadata elements (required by f:unified-id) -->
    <!-- if it weren't for XSpec this could have been a xsl:variable -->
    <xsl:param name="existing-id-outside-metadata" as="xs:string*" select="//@id[not(ancestor::metadata)]"/>

    <!--=========================================-->
    <!--TODO: document merge rules. For now, see the tests.-->
    <!--=========================================-->

    <xsl:key name="unified-id" match="//meta[@id]|//dc:*[@id]" use="f:unified-id(@id)"/>
    <xsl:key name="unified-refines" match="//meta[@refines]" use="f:unified-id(@refines)"/>

    <xsl:template match="/*" priority="1">
        <xsl:variable name="prefix-attr" as="element(f:vocab)*" select="f:parse-prefix-decl(@prefix)"/>
        <xsl:variable name="implicit-prefixes" as="element(f:vocab)*"
                      select="if ($reserved-prefixes='#default')
                              then for $used in distinct-values(
                                                  //meta/(@property|@scheme)[contains(.,':')]/substring-before(.,':'))
                                   return $f:default-prefixes[@prefix=$used]
                              else f:parse-prefix-decl($reserved-prefixes)"/>
        <xsl:next-match>
            <xsl:with-param name="prefix-attr" tunnel="yes" select="$prefix-attr"/>
            <xsl:with-param name="implicit-prefixes" tunnel="yes" select="$implicit-prefixes"/>
            <xsl:with-param name="vocabs" tunnel="yes" select="($prefix-attr,$implicit-prefixes)"/>
        </xsl:next-match>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:param name="prefix-attr" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:param name="implicit-prefixes" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:param name="vocabs" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <metadata>
            <xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
            <xsl:variable name="dcterms-vocab" as="element(f:vocab)"
                          select="($implicit-prefixes[@uri=$f:default-prefixes[@prefix='dcterms']/@uri],
                                   $f:default-prefixes[@prefix='dcterms'])[1]"/>
            <xsl:choose>
                <xsl:when test="not($prefix-attr[@uri=$dcterms-vocab/@uri])
                                and not($implicit-prefixes[@uri=$dcterms-vocab/@uri])">
                    <xsl:attribute name="prefix"
                                   select="string-join((@prefix,
                                                        concat($dcterms-vocab/@prefix,': ',$dcterms-vocab/@uri)),' ')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="@prefix"/>
                </xsl:otherwise>
            </xsl:choose>

            <!-- dc:title(s) and refines -->
            <xsl:variable name="titles" select="//metadata/dc:title[not(@refines)]"/>
            <!-- Only keep first group of fields  -->
            <xsl:variable name="first-group" as="element()*"
                          select="$titles intersect $titles[1]/ancestor::metadata/dc:title"/>
            <xsl:apply-templates select="$first-group"/>
            <!-- Discard fields that are not from the same metadata document -->
            <xsl:if test="$log-conflicts='true'">
                <xsl:variable name="new-value" as="xs:string" select="f:serialize-value($first-group)"/>
                <xsl:for-each-group select="$titles except $first-group"
                                    group-by="count(ancestor::metadata/preceding::metadata)">
                    <xsl:variable name="value" as="xs:string"
                                  select="f:serialize-value(current-group())"/>
                    <xsl:if test="$value!=$new-value">
                        <xsl:message>Updating '<xsl:value-of select="name()"
                        />': replacing value <xsl:value-of select="$value"/> with <xsl:value-of
                        select="$new-value"/>.</xsl:message>
                    </xsl:if>
                </xsl:for-each-group>
            </xsl:if>

            <!-- dc:identifier(s) and refines -->
            <xsl:variable name="identifiers" select="//metadata/dc:identifier[not(@refines)]"/>
            <!-- Only keep first group of fields  -->
            <xsl:variable name="first-group" as="element()*"
                          select="$identifiers intersect $identifiers[1]/ancestor::metadata/dc:identifier"/>
            <xsl:apply-templates select="$first-group"/>
            <!-- Discard fields that are not from the same metadata document -->
            <xsl:if test="$log-conflicts='true'">
                <xsl:variable name="new-value" as="xs:string" select="f:serialize-value($first-group)"/>
                <xsl:for-each-group select="$identifiers except $first-group"
                                    group-by="count(ancestor::metadata/preceding::metadata)">
                    <xsl:variable name="value" as="xs:string"
                                  select="f:serialize-value(current-group())"/>
                    <xsl:if test="$value!=$new-value">
                        <xsl:message>Updating '<xsl:value-of select="name()"
                        />': replacing value <xsl:value-of select="$value"/> with <xsl:value-of
                        select="$new-value"/>.</xsl:message>
                    </xsl:if>
                </xsl:for-each-group>
            </xsl:if>

            <!-- dc:language(s) and refines -->
            <xsl:variable name="languages" select="//metadata/dc:language[not(@refines)]"/>
            <!-- Only keep first group of fields  -->
            <xsl:variable name="first-group" as="element()*"
                          select="$languages intersect $languages[1]/ancestor::metadata/dc:language"/>
            <xsl:apply-templates select="$first-group"/>
            <!-- Discard fields that are not from the same metadata document -->
            <xsl:if test="$log-conflicts='true'">
                <xsl:variable name="new-value" as="xs:string" select="f:serialize-value($first-group)"/>
                <xsl:for-each-group select="$languages except $first-group"
                                    group-by="count(ancestor::metadata/preceding::metadata)">
                    <xsl:variable name="value" as="xs:string"
                                  select="f:serialize-value(current-group())"/>
                    <xsl:if test="$value!=$new-value">
                        <xsl:message>Updating '<xsl:value-of select="name()"
                        />': replacing value <xsl:value-of select="$value"/> with <xsl:value-of
                        select="$new-value"/>.</xsl:message>
                    </xsl:if>
                </xsl:for-each-group>
            </xsl:if>

            <!--generate dcterms:modified-->
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
                <!-- Only keep first field or group of fields  -->
                <xsl:variable name="first-group" as="element()*">
                    <xsl:variable name="first-group" as="element()*"
                                  select="current-group()[ancestor::metadata is current()/ancestor::metadata]"/>
                    <xsl:choose>
                        <xsl:when test="self::dc:date|self::dc:source">
                            <xsl:variable name="first" as="element()" select="$first-group[1]"/>
                            <xsl:sequence select="$first"/>
                            <!-- Discard other fields in the same metadata document -->
                            <xsl:for-each select="$first-group except $first">
                                <xsl:call-template name="pf:warn">
                                    <xsl:with-param name="msg">Discarding '<xsl:value-of select="name()"
                                        />' with value '<xsl:value-of select="string(.)"/>': only one '<xsl:value-of
                                        select="name()"/>' allowed.</xsl:with-param>
                                </xsl:call-template>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="$first-group"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:apply-templates select="$first-group"/>
                <!-- Discard fields that are not from the same metadata document -->
                <xsl:if test="$log-conflicts='true'">
                    <xsl:variable name="new-value" as="xs:string"
                                  select="f:serialize-value($first-group)"/>
                    <xsl:for-each-group select="current-group() except $first-group"
                                        group-by="count(ancestor::metadata/preceding::metadata)">
                        <xsl:variable name="value" as="xs:string"
                                      select="f:serialize-value(current-group())"/>
                        <xsl:if test="$value!=$new-value">
                            <xsl:message>Updating '<xsl:value-of select="name()"
                            />': replacing value <xsl:value-of select="$value"/> with <xsl:value-of
                            select="$new-value"/>.</xsl:message>
                        </xsl:if>
                    </xsl:for-each-group>
                </xsl:if>
            </xsl:for-each-group>

            <!--meta [0 or more]-->
            <xsl:for-each-group select="//meta[empty(@refines)]" group-by="f:expand-property(@property,$vocabs)/@uri">
                <xsl:choose>
                    <xsl:when test="current-grouping-key()">
                        <xsl:variable name="property" as="xs:string" select="current-grouping-key()"/>
                        <!-- Only keep first group of fields  -->
                        <xsl:variable name="first-group" as="element(opf:meta)*"
                                      select="current-group()[ancestor::metadata is current()/ancestor::metadata]"/>
                        <xsl:variable name="first-group-processed" as="element(opf:meta)*">
                            <xsl:apply-templates select="$first-group"/>
                        </xsl:variable>
                        <xsl:sequence select="$first-group-processed"/>
                        <!-- Discard fields that are not from the same metadata document -->
                        <xsl:if test="$log-conflicts='true'">
                            <xsl:variable name="discarded" as="element(opf:meta)*"
                                          select="current-group() except $first-group"/>
                            <xsl:variable name="discarded" as="element(opf:meta)*">
                                <xsl:for-each select="$discarded">
                                    <xsl:choose>
                                        <xsl:when test="$property='http://purl.org/dc/terms/modified'">
                                            <!-- only show warning when discarded value was not recently generated -->
                                            <xsl:variable name="show-warning" as="xs:boolean">
                                                <!--
                                                    dates generated by Pipeline are in either of the forms
                                                    - [Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][Z]
                                                    - [Y0001]-[M01]-[D01]T[H01]:[m01]:[s01]Z
                                                    both of which may be parsed using the same format string yyyy-MM-dd'T'HH:mm:ssX
                                                -->
                                                <xsl:try select="(current-dateTime()
                                                                  - pf:parse-dateTime(string(.),'yyyy-MM-dd''T''HH:mm:ssX'))
                                                                 &gt; xs:dayTimeDuration('PT1M')">
                                                    <xsl:catch>
                                                        <xsl:sequence select="true()"/>
                                                    </xsl:catch>
                                                </xsl:try>
                                            </xsl:variable>
                                            <xsl:if test="$show-warning">
                                                <xsl:message>Discarding property '<xsl:value-of select="@property"
                                                />' with value '<xsl:value-of select="."
                                                />'. A new value is generated.</xsl:message>
                                            </xsl:if>
                                        </xsl:when>
                                        <xsl:when test="not(@property) and @name and @content">
                                            <!-- Don't mention OPF 2 fields. We assume that the corresponding OPF 3
                                                 fields are also present. -->
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:sequence select="."/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                            </xsl:variable>
                            <xsl:if test="exists($discarded)">
                                <!--
                                    FIXME: also mention meta[@refines] elements that are discarded
                                    because the element they refine is discarded
                                -->
                                <xsl:choose>
                                    <xsl:when test="exists($first-group-processed)">
                                        <xsl:variable name="new-value" as="xs:string"
                                                      select="f:serialize-value($first-group-processed[not(@refines)])"/>
                                        <xsl:for-each-group select="$discarded"
                                                            group-by="count(ancestor::metadata/preceding::metadata)">
                                            <xsl:variable name="value" as="xs:string"
                                                          select="f:serialize-value(current-group())"/>
                                            <xsl:if test="$value!=$new-value">
                                                <xsl:message>Updating property '<xsl:value-of select="@property"
                                                />': replacing value <xsl:value-of select="$value"/> with <xsl:value-of
                                                select="$new-value"/>.</xsl:message>
                                            </xsl:if>
                                        </xsl:for-each-group>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:for-each select="$discarded">
                                            <xsl:call-template name="pf:warn">
                                                <xsl:with-param name="msg">Discarding property '<xsl:value-of
                                                    select="@property" />' with value '<xsl:value-of select="string(.)"
                                                    />'.</xsl:with-param>
                                            </xsl:call-template>
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                        </xsl:if>
                    </xsl:when>
                    <xsl:when test="not(@property) and @name and @content">
                        <!-- Don't mention OPF 2 fields. We assume that the corresponding OPF 3 fields are also
                             present. -->
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="pf:warn">
                            <xsl:with-param name="msg">Discarding property '<xsl:value-of
                                select="@property"/>' from an undeclared vocab.</xsl:with-param>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each-group>

            <!-- process meta that refine manifest items within the same package document -->
            <xsl:for-each select="//metadata">
                <xsl:variable name="manifest" as="element(opf:manifest)?" select="parent::package/manifest"/>
                <xsl:apply-templates select="meta[replace(@refines,'^#','')=$manifest//item/@id]">
                    <xsl:with-param name="copy-refines" tunnel="yes" select="true()"/>
                </xsl:apply-templates>

                <xsl:for-each select="meta[@refines][not(replace(@refines,'^#','')=$manifest//item/@id) and
                                      not(exists(key('unified-id',f:unified-id(@refines))))]">
                    <xsl:call-template name="pf:warn">
                        <xsl:with-param name="msg">Discarding property '<xsl:value-of select="@property"
                            />' with broken refines attribute '<xsl:value-of select="@refines"/>'.</xsl:with-param>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:for-each>

            <xsl:apply-templates select="//link"/>
        </metadata>
    </xsl:template>

    <xsl:template match="dc:identifier[not(@id)]">
        <dc:identifier>
            <!-- make sure there is an ID for the unique-identifier attribute to point to -->
            <xsl:attribute name="id" select="f:unique-id(generate-id(),//@id)"/>
            <xsl:apply-templates select="@*|node()"/>
        </dc:identifier>
    </xsl:template>

    <xsl:template match="dc:*">
        <xsl:next-match/>
        <xsl:apply-templates select="key('unified-refines',f:unified-id(@id))"/>
    </xsl:template>

    <xsl:template match="meta">
        <xsl:param name="vocabs" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:variable name="property" select="f:expand-property(@property,$vocabs)"/>
        <xsl:choose>
            <xsl:when test="$property/@uri='http://purl.org/dc/terms/modified'">
                <!-- only show warning when discarded value was not recently generated -->
                <xsl:variable name="show-warning" as="xs:boolean">
                    <xsl:try select="(current-dateTime() - pf:parse-dateTime(string(.),'yyyy-MM-dd''T''HH:mm:ssX'))
                                     &gt; xs:dayTimeDuration('PT1M')">
                        <xsl:catch>
                            <xsl:sequence select="true()"/>
                        </xsl:catch>
                    </xsl:try>
                </xsl:variable>
                <xsl:if test="$show-warning">
                    <xsl:message>Discarding property '<xsl:value-of select="@property"
                        />' with value '<xsl:value-of select="."/>'. A new value is generated.</xsl:message>
                </xsl:if>
            </xsl:when>
            <xsl:when test="not(@property) and @name and @content">
                <!-- Discard OPF 2 fields. They will be added again later based on the OPF 3 fields
                     if the compatibility-mode option is set. We assume that if there are OPF 2
                     fields in the input, the corresponding OPF 3 fields are also present. -->
            </xsl:when>
            <xsl:when test="not(normalize-space())">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">Discarding empty property '<xsl:value-of select="@property"
                        />'.</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$property/@uri=''">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">Discarding property '<xsl:value-of select="@property"/>' from
                        an undeclared vocab.</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when
                test="$property/@prefix='' and $property/@name=('display-seq','meta-auth')">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">The deprecated property '<xsl:value-of select="@property"
                        />' was found.</xsl:with-param>
                </xsl:call-template>
                <xsl:next-match/>
            </xsl:when>
            <xsl:when
                test="$property/@prefix='' and not($property/@name=('alternate-script',
                                                                    'display-seq',
                                                                    'file-as',
                                                                    'group-position',
                                                                    'identifier-type',
                                                                    'meta-auth',
                                                                    'role',
                                                                    'title-type',
                                                                    'pageBreakSource'))">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">Discarding unknown property '<xsl:value-of select="@property"
                        />'.</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="key('unified-refines',f:unified-id(@id))"/>
    </xsl:template>

    <xsl:template match="link">
        <xsl:param name="vocabs" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:variable name="rel" select="f:expand-property(@rel,$vocabs)"/>
        <xsl:choose>
            <xsl:when test="not(@href) or not(@rel)">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">Discarding link with no @href or @rel
                        attributes.</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <!-- EPUB3.2: values marc21xml-record, mods-record, onix-record, and xmp-signature of @rel are deprecated and should be replaced by 'record'-->
            <xsl:when
                test="$rel/@prefix='' and $rel/@name=('marc21xml-record',
                'mods-record','onix-record','xml-signature xmp-record')">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">Found link with deprecated @rel value '<xsl:value-of
                        select="@rel"/>'. This value should be replaced by 'record' with a corresponding 'media-type' attribute.</xsl:with-param>
                </xsl:call-template>
                <xsl:next-match/>
            </xsl:when>
            <xsl:when
                test="$rel/@prefix='' and not($rel/@name=('marc21xml-record',
                'mods-record','onix-record','xml-signature xmp-record','record','acquire','alternate'))">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">Discarding link with unknown @rel value '<xsl:value-of
                        select="@rel"/>'.</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$rel/@uri=''">
                <xsl:call-template name="pf:warn">
                    <xsl:with-param name="msg">Discarding link with @rel value '<xsl:value-of
                        select="@property"/>' from an undeclared vocab.</xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@property">
        <xsl:param name="vocabs" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:attribute name="property" select="f:expand-property(.,$vocabs)/@name"/>
    </xsl:template>

    <xsl:template match="@scheme">
        <xsl:param name="vocabs" as="element(f:vocab)*" tunnel="yes" required="yes"/>
        <xsl:attribute name="scheme" select="f:expand-property(.,$vocabs)/@name"/>
    </xsl:template>

    <xsl:template match="@id">
        <xsl:attribute name="id" select="f:unified-id(.)"/>
    </xsl:template>

    <xsl:template match="@refines">
        <xsl:param name="copy-refines" tunnel="yes" as="xs:boolean?" select="false()"/>
        <xsl:choose>
            <xsl:when test="$copy-refines">
                <xsl:sequence select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="refines" select="concat('#',f:unified-id(.))"/>
            </xsl:otherwise>
        </xsl:choose>
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
        The non-conflicting ID is created by appending 1 + the number of ID attributes with the same
        name in preceding metadata elements, if this resulting number is more than one.
    -->
    <xsl:function name="f:unified-id" as="xs:string">
        <xsl:param name="attr" as="attribute()?"/>
        <xsl:variable name="id" as="xs:string"
                      select="if (starts-with($attr,'#')) then substring($attr,2) else string($attr)"/>
        <xsl:variable name="count" as="xs:integer"
                      select="count($attr/ancestor::metadata/preceding::metadata//*/@id[string(.)=$id])"/>
        <xsl:sequence
            select="f:unique-id(
                      concat(
                        $id,
                        if ($count) then $count+1 else ''),
                        $existing-id-outside-metadata)"
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
        Returns a `f:property` element from a property-typeed attribute where:
        
         * @prefix contains the resolved, unified prefix for the property
         * @uri contains the resolved absolute URI of the property
         * @name contains the resolved name for the property, prefixed by the unified prefix
    -->
    <xsl:function name="f:expand-property" as="element(f:property)">
        <xsl:param name="property" as="attribute()?"/>
        <xsl:param name="vocabs" as="element(f:vocab)*"/>
        <xsl:variable name="prefix" select="substring-before($property,':')" as="xs:string"/>
        <xsl:variable name="reference" select="replace($property,'(.+:)','')" as="xs:string"/>
        <xsl:variable name="vocab" as="xs:string"
                      select="($vocabs[@prefix=$prefix]/@uri,
                               if ($prefix='' and $reference!='')
                                 then (
                                        if ($property/parent::meta and name($property)='property')    then $vocab-package-meta-uri
                                   else if ($property/parent::link and name($property)='rel')         then $vocab-package-link-uri
                                   else if ($property/parent::item and name($property)='properties')  then $vocab-package-item-uri
                                   else if ($property/parent::itemref and name($property)='property') then $vocab-package-itemref-uri
                                   else () (:should not happen:)
                                 )
                                 else (),
                               ''
                              )[1]"/>
        <f:property prefix="{$prefix}"
                    uri="{if ($vocab) then concat($vocab,$reference) else ''}"
                    name="{string-join(($prefix,$reference)[not(.='')],':')}"/>
    </xsl:function>

    <xsl:function name="f:serialize-value" as="xs:string">
        <xsl:param name="elems" as="element()*"/>
        <xsl:sequence select="if (count($elems)=0)
                              then '[]'
                              else if (count($elems)=1)
                              then concat('''',$elems/string(.),'''')
                              else concat('[''',string-join($elems/string(.),''', '''),''']')"/>
    </xsl:function>

</xsl:stylesheet>
