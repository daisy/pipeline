<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>
    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:param name="limit-scope" as="xs:boolean" select="false()"/>

    <xsl:template match="/d:fileset">
        <xsl:variable name="a" as="element(d:fileset)" select="."/>
        <xsl:variable name="b" as="element(d:fileset)" select="collection()[2]/*"/>
        <xsl:variable name="base" as="xs:string?" select="($b,$a)[@xml:base][1]/pf:normalize-uri(base-uri(.))"/>
        <xsl:copy>
            <xsl:if test="exists($base)">
                <xsl:attribute name="xml:base" select="$base"/>
            </xsl:if>
            <xsl:sequence select="@* except @xml:base"/>
            <xsl:variable name="files-and-anchors" as="map(*)*">
                <xsl:variable name="a" as="map(*)*">
                    <xsl:apply-templates mode="parse" select="$a"/>
                </xsl:variable>
                <xsl:variable name="b" as="map(*)*">
                    <xsl:apply-templates mode="parse" select="$b"/>
                </xsl:variable>
                <xsl:variable name="a-files" as="map(*)*" select="$a[not(map:contains(.,'id'))]"/>
                <xsl:variable name="a-anchors" as="map(*)*" select="$a[map:contains(.,'id')]"/>
                <xsl:variable name="b-files" as="map(*)*" select="$b[not(map:contains(.,'id'))]"/>
                <xsl:variable name="b-anchors" as="map(*)*" select="$b[map:contains(.,'id')]"/>
                <xsl:for-each select="$a-files">
                    <xsl:variable name="a-file" as="map(*)" select="."/>
                    <xsl:variable name="b-file" as="map(*)*"
                                  select="$b-files[.('original-href')=$a-file('href')]"/>
                    <xsl:choose>
                        <xsl:when test="exists($b-file)">
                            <!-- There could be multiple files in B that originate from the same file in A. This
                                 means the document is split. -->
                            <xsl:for-each select="$b-file">
                                <xsl:variable name="b-file" as="map(*)" select="."/>
                                <xsl:map>
                                    <xsl:map-entry key="'href'" select="$b-file('href')"/>
                                    <xsl:map-entry key="'original-href'" select="$a-file('original-href')"/>
                                </xsl:map>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="$a-file"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
                <xsl:variable name="b-anchors-grouped-by-original-href-id" as="map(xs:string,map(xs:string,map(*)*))">
                    <xsl:map>
                        <xsl:for-each-group select="$b-anchors" group-by=".('original-href')">
                            <xsl:map-entry key="current-grouping-key()">
                                <xsl:map>
                                    <xsl:for-each-group select="current-group()" group-by=".('original-id')">
                                        <xsl:map-entry key="current-grouping-key()" select="current-group()"/>
                                    </xsl:for-each-group>
                                </xsl:map>
                            </xsl:map-entry>
                        </xsl:for-each-group>
                    </xsl:map>
                </xsl:variable>
                <xsl:for-each select="$a-anchors">
                    <xsl:variable name="a-anchor" as="map(*)" select="."/>
                    <xsl:variable name="b-anchor" as="map(*)*"
                                  select="for $m in $b-anchors-grouped-by-original-href-id($a-anchor('href'))
                                          return $m($a-anchor('id'))"/>

                    <xsl:choose>
                        <xsl:when test="exists($b-anchor)">
                            <xsl:if test="count($b-anchor) &gt; 1">
                                <!-- There are multiple fragments in B that originate from the same fragment in
                                     A. This is currently not supported. Ignore all except the first. -->
                                <xsl:call-template name="pf:warn">
                                    <xsl:with-param name="msg" select="'Ambiguous mapping for anchor.'"/>
                                </xsl:call-template>
                            </xsl:if>
                            <xsl:variable name="b-anchor" as="map(*)" select="$b-anchor[1]"/>
                            <xsl:map>
                                <xsl:map-entry key="'href'" select="$b-anchor('href')"/>
                                <xsl:map-entry key="'id'" select="$b-anchor('id')"/>
                                <xsl:map-entry key="'original-href'" select="$a-anchor('original-href')"/>
                                <xsl:map-entry key="'original-id'" select="$a-anchor('original-id')"/>
                            </xsl:map>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="b-file" as="map(*)*"
                                          select="$b-files[.('original-href')=$a-anchor('href')]"/>
                            <xsl:choose>
                                <xsl:when test="exists($b-file)">
                                    <!-- There could be multiple files in B that originate from the same file in
                                         A. This means the document is split. Assuming that if none of the files
                                         list the fragment, the first contains it (in other words the fragment
                                         is not deleted). -->
                                    <xsl:variable name="b-file" as="map(*)" select="$b-file[1]"/>
                                    <xsl:map>
                                        <xsl:map-entry key="'href'" select="$b-file('href')"/>
                                        <xsl:map-entry key="'id'" select="$a-anchor('id')"/>
                                        <xsl:map-entry key="'original-href'" select="$a-anchor('original-href')"/>
                                        <xsl:map-entry key="'original-id'" select="$a-anchor('original-id')"/>
                                    </xsl:map>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:sequence select="$a-anchor"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
                <xsl:if test="not($limit-scope)">
                    <xsl:for-each select="$b-files">
                        <xsl:variable name="b-file" as="map(*)" select="."/>
                        <xsl:choose>
                            <xsl:when test="some $a-file in $a-files
                                            satisfies $a-file('href')=$b-file('original-href')">
                                <!-- A file in B originates from a file in A. This has already been handled. -->
                            </xsl:when>
                            <xsl:otherwise>
                                <!-- Assuming that the file in B originates from an existing file. (Note that we could check
                                     whether mapping A maps the referenced file to another file, but we're assuming it is
                                     not the case.) -->
                                <xsl:sequence select="$b-file"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                    <xsl:variable name="a-anchors-grouped-by-href-id" as="map(xs:string,map(xs:string,map(*)*))">
                        <xsl:map>
                            <xsl:for-each-group select="$a-anchors" group-by=".('href')">
                                <xsl:map-entry key="current-grouping-key()">
                                    <xsl:map>
                                        <xsl:for-each-group select="current-group()" group-by=".('id')">
                                            <xsl:map-entry key="current-grouping-key()" select="current-group()"/>
                                        </xsl:for-each-group>
                                    </xsl:map>
                                </xsl:map-entry>
                            </xsl:for-each-group>
                        </xsl:map>
                    </xsl:variable>
                    <xsl:for-each select="$b-anchors">
                        <xsl:variable name="b-anchor" as="map(*)" select="."/>
                        <xsl:choose>
                            <xsl:when test="some $m in $a-anchors-grouped-by-href-id($b-anchor('original-href'))
                                            satisfies exists($m($b-anchor('original-id')))">
                                <!-- A fragment in B originates from a fragment in A. This has already been handled. -->
                            </xsl:when>
                            <xsl:otherwise>
                                <!-- Assuming that the fragment in B originates from an existing fragment. (Note that we
                                     could check whether mapping A maps the referenced fragment to another fragment, but
                                     we're assuming it is not the case.) -->
                                <xsl:variable name="a-file" as="map(*)*"
                                              select="$a-files[.('href')=$b-anchor('original-href')]"/>
                                <xsl:choose>
                                    <xsl:when test="exists($a-file)">
                                        <!-- A file in B could originate from multiple files in A. This means documents are
                                             merged. Assuming that if none of the files list the fragment, the first contains
                                             it (in other words the fragment is not deleted). -->
                                        <xsl:variable name="a-file" as="map(*)" select="$a-file[1]"/>
                                        <xsl:map>
                                            <xsl:map-entry key="'href'" select="$b-anchor('href')"/>
                                            <xsl:map-entry key="'id'" select="$b-anchor('id')"/>
                                            <xsl:map-entry key="'original-href'" select="$a-file('original-href')"/>
                                            <xsl:map-entry key="'original-id'" select="$b-anchor('original-id')"/>
                                        </xsl:map>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:sequence select="$b-anchor"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:if>
            </xsl:variable>
            <xsl:variable name="files-and-anchors" as="element()*"> <!-- element(d:file|d:anchor)* -->
                <xsl:for-each select="$files-and-anchors">
                    <xsl:variable name="attributes" as="attribute()*">
                        <xsl:variable name="map" as="map(*)*" select="."/>
                        <xsl:for-each select="map:keys($map)">
                            <xsl:attribute name="{.}" select="$map(.)"/>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="$attributes[name()='id']">
                            <d:anchor>
                                <xsl:sequence select="$attributes"/>
                            </d:anchor>
                        </xsl:when>
                        <xsl:otherwise>
                            <d:file>
                                <xsl:sequence select="$attributes"/>
                            </d:file>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="files-with-anchors" as="element(d:file)*">
                <xsl:for-each-group select="$files-and-anchors" group-by="@original-href">
                    <xsl:for-each-group select="current-group()" group-by="@href">
                        <d:file>
                            <xsl:attribute name="href" select="if (exists($base))
                                                               then pf:relativize-uri(@href,$base)
                                                               else @href"/>
                            <xsl:if test="not(@original-href=@href)">
                                <xsl:sequence select="@original-href"/>
                            </xsl:if>
                            <xsl:for-each select="current-group()[self::d:anchor]">
                                <xsl:copy>
                                    <xsl:sequence select="@id"/>
                                    <xsl:if test="not(@original-id=@id)">
                                        <xsl:sequence select="@original-id"/>
                                    </xsl:if>
                                </xsl:copy>
                            </xsl:for-each>
                        </d:file>
                    </xsl:for-each-group>
                </xsl:for-each-group>
            </xsl:variable>
            <xsl:sequence select="$files-with-anchors"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="parse" match="/d:fileset">
        <xsl:apply-templates mode="#current" select="*">
            <xsl:with-param name="base" tunnel="yes" select="base-uri(.)"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="parse"
                  match="/d:fileset/d:file[@href]">
        <xsl:variable name="normalized-file" as="map(*)">
            <xsl:map>
                <xsl:apply-templates mode="#current" select="@*"/>
            </xsl:map>
        </xsl:variable>
        <xsl:variable name="normalized-file" as="map(*)"
                      select="if (@original-href)
                              then $normalized-file
                              else map:put($normalized-file,'original-href',$normalized-file('href'))"/>
        <xsl:sequence select="$normalized-file"/>
        <xsl:apply-templates mode="#current">
            <xsl:with-param name="normalized-parent" select="$normalized-file"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="parse"
                  match="d:file/@href|
                         d:file/@original-href">
        <xsl:param name="base" tunnel="yes" required="yes"/>
        <xsl:map-entry key="name()" select="pf:normalize-uri(resolve-uri(.,$base))"/>
    </xsl:template>

    <xsl:template mode="parse"
                  match="/d:fileset/d:file/d:anchor[@id]">
        <xsl:param name="normalized-parent" as="map(*)" required="yes"/>
        <xsl:map>
            <xsl:map-entry key="'href'" select="$normalized-parent('href')"/>
            <xsl:map-entry key="'original-href'" select="$normalized-parent('original-href')"/>
            <xsl:map-entry key="'id'" select="string(@id)"/>
            <xsl:map-entry key="'original-id'" select="if (@original-id)
                                                       then string(@original-id)
                                                       else string(@id)"/>
        </xsl:map>
    </xsl:template>

    <xsl:template mode="parse" match="@*|node()" priority="0.4"/>

</xsl:stylesheet>
