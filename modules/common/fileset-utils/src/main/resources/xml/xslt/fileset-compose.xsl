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
            <xsl:variable name="files-and-anchors-and-clips" as="map(*)*">
                <xsl:variable name="a" as="map(*)*">
                    <xsl:apply-templates mode="parse" select="$a"/>
                </xsl:variable>
                <xsl:variable name="b" as="map(*)*">
                    <xsl:apply-templates mode="parse" select="$b"/>
                </xsl:variable>
                <xsl:variable name="a-files" as="map(*)*" select="$a[not(map:contains(.,'id') or map:contains(.,'clipBegin'))]"/>
                <xsl:variable name="a-anchors" as="map(*)*" select="$a[map:contains(.,'id')]"/>
                <xsl:variable name="a-clips" as="map(*)*" select="$a[map:contains(.,'clipBegin')]"/>
                <xsl:variable name="b-files" as="map(*)*" select="$b[not(map:contains(.,'id') or map:contains(.,'clipBegin'))]"/>
                <xsl:variable name="b-anchors" as="map(*)*" select="$b[map:contains(.,'id')]"/>
                <xsl:variable name="b-clips" as="map(*)*" select="$b[map:contains(.,'clipBegin')]"/>
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
                <xsl:for-each select="$a-anchors">
                    <xsl:variable name="a-anchor" as="map(*)" select="."/>
                    <xsl:variable name="b-anchor" as="map(*)*"
                                  select="$b-anchors[.('original-id')=$a-anchor('id') and
                                                     .('original-href')=$a-anchor('href')]"/>
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
                <xsl:for-each select="$a-clips">
                    <xsl:variable name="a-clip" as="map(*)" select="."/>
                    <xsl:variable name="a-clipBegin" as="xs:decimal" select="$a-clip('clipBegin')"/>
                    <xsl:variable name="a-clipEnd" as="xs:decimal" select="$a-clip('clipEnd')"/>
                    <xsl:variable name="a-original-clipBegin" as="xs:decimal" select="$a-clip('original-clipBegin')"/>
                    <xsl:variable name="a-original-clipEnd" as="xs:decimal" select="$a-clip('original-clipEnd')"/>
                    <!-- clips in B that overlap with the clip in A -->
                    <xsl:variable name="b-clip" as="map(*)*"
                                  select="$b-clips[.('original-href')=$a-clip('href') and
                                                   .('original-clipBegin') &lt; $a-clipEnd and
                                                   $a-clipBegin &lt; .('original-clipEnd')]"/>
                    <xsl:variable name="b-clip" as="map(*)*">
                        <xsl:perform-sort select="$b-clip">
                            <xsl:sort select=".('original-clipBegin')"/>
                        </xsl:perform-sort>
                    </xsl:variable>
                    <xsl:variable name="b-clip" as="map(*)*">
                        <xsl:iterate select="$b-clip">
                            <xsl:param name="collect" as="map(*)*" select="()"/>
                            <xsl:on-completion select="$collect"/>
                            <xsl:variable name="b-clip" as="map(*)" select="."/>
                            <xsl:variable name="b-original-clipBegin" as="xs:decimal" select="$b-clip('original-clipBegin')"/>
                            <xsl:variable name="b-original-clipEnd" as="xs:decimal" select="$b-clip('original-clipEnd')"/>
                            <xsl:choose>
                                <xsl:when test="some $clip in $collect satisfies (
                                                  $b-original-clipBegin &lt; $clip('original-clipEnd') and
                                                  $clip('original-clipBegin') &lt; $b-original-clipEnd)">
                                    <!-- There are overlapping clips in B. This is currently not
                                         supported. Retain only a set of clips that don't overlap. -->
                                    <xsl:call-template name="pf:warn">
                                        <xsl:with-param name="msg" select="'Ambiguous mapping for clip.'"/>
                                    </xsl:call-template>
                                    <xsl:next-iteration/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:next-iteration>
                                        <xsl:with-param name="collect" select="($collect,$b-clip)"/>
                                    </xsl:next-iteration>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:iterate>
                    </xsl:variable>
                    <xsl:iterate select="$b-clip">
                        <xsl:param name="last-covered-end" as="xs:decimal" select="$a-clipBegin"/>
                        <xsl:param name="uncovered" as="map(*)*" select="()"/>
                        <xsl:on-completion>
                            <xsl:variable name="uncovered" as="map(*)*">
                                <xsl:sequence select="$uncovered"/>
                                <xsl:if test="$a-clipEnd &gt; $last-covered-end">
                                    <xsl:map>
                                        <xsl:map-entry key="'href'" select="$a-clip('href')"/>
                                        <xsl:map-entry key="'clipEnd'" select="$a-clip('clipEnd')"/>
                                        <xsl:map-entry key="'original-href'" select="$a-clip('original-href')"/>
                                        <xsl:map-entry key="'original-clipEnd'" select="$a-clip('original-clipEnd')"/>
                                        <xsl:map-entry key="'clipBegin'" select="$last-covered-end"/>
                                        <!-- interpolate between clip A's original-clipBegin and original-clipEnd -->
                                        <xsl:map-entry key="'original-clipBegin'"
                                                       select="$last-covered-end + $a-original-clipBegin - $a-clipBegin"/>
                                    </xsl:map>
                                </xsl:if>
                            </xsl:variable>
                            <xsl:if test="exists($uncovered)">
                                <xsl:variable name="b-file" as="map(*)*"
                                              select="$b-files[.('original-href')=$a-clip('href')]"/>
                                <xsl:choose>
                                    <xsl:when test="exists($b-file)">
                                        <!-- There could be multiple files in B that originate from the same
                                             file in A. This means the document is split. Assuming that if none
                                             of the files list the fragment, the first contains it (in other
                                             words the fragment is not deleted). -->
                                        <xsl:variable name="b-file" as="map(*)" select="$b-file[1]"/>
                                        <xsl:for-each select="$uncovered">
                                            <xsl:variable name="a-clip" as="map(*)" select="."/>
                                            <xsl:map>
                                                <xsl:map-entry key="'href'" select="$b-file('href')"/>
                                                <xsl:map-entry key="'clipBegin'" select="$a-clip('clipBegin')"/>
                                                <xsl:map-entry key="'clipEnd'" select="$a-clip('clipEnd')"/>
                                                <xsl:map-entry key="'original-href'" select="$a-clip('original-href')"/>
                                                <xsl:map-entry key="'original-clipBegin'" select="$a-clip('original-clipBegin')"/>
                                                <xsl:map-entry key="'original-clipEnd'" select="$a-clip('original-clipEnd')"/>
                                            </xsl:map>
                                        </xsl:for-each>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:sequence select="$uncovered"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                        </xsl:on-completion>
                        <xsl:variable name="b-clip" as="map(*)" select="."/>
                        <xsl:variable name="b-clipBegin" as="xs:decimal" select="$b-clip('clipBegin')"/>
                        <xsl:variable name="b-clipEnd" as="xs:decimal" select="$b-clip('clipEnd')"/>
                        <xsl:variable name="b-original-clipBegin" as="xs:decimal" select="$b-clip('original-clipBegin')"/>
                        <xsl:variable name="b-original-clipEnd" as="xs:decimal" select="$b-clip('original-clipEnd')"/>
                        <xsl:variable name="a-intersect-clipBegin" as="xs:decimal" select="max(($b-original-clipBegin,$a-clipBegin))"/>
                        <xsl:variable name="a-intersect-clipEnd" as="xs:decimal" select="min(($b-original-clipEnd,$a-clipEnd))"/>
                        <xsl:map>
                            <xsl:map-entry key="'href'" select="$b-clip('href')"/>
                            <xsl:map-entry key="'original-href'" select="$a-clip('original-href')"/>
                            <!--
                                interpolate between clip B's clipBegin and clipEnd
                                - it is assumed that audio is never stretched
                                - (small) cuts are possible (due to re-sampling)
                            -->
                            <xsl:variable name="offset" as="xs:decimal" select="$b-clipBegin - $b-original-clipBegin"/>
                            <xsl:map-entry key="'clipBegin'" select="min(($a-intersect-clipBegin + $offset,$b-clipEnd))"/>
                            <xsl:map-entry key="'clipEnd'" select="min(($a-intersect-clipEnd + $offset,$b-clipEnd))"/>
                            <!-- interpolate between clip A's original-clipBegin and original-clipEnd -->
                            <xsl:map-entry key="'original-clipBegin'"
                                           select="$a-original-clipBegin - $a-clipBegin + $a-intersect-clipBegin"/>
                            <xsl:map-entry key="'original-clipEnd'"
                                           select="if ($a-intersect-clipEnd &lt; $a-clipEnd)
                                                   then $a-original-clipBegin - $a-intersect-clipBegin + $a-intersect-clipEnd
                                                   else $a-original-clipEnd"/>
                        </xsl:map>
                        <xsl:next-iteration>
                            <xsl:with-param name="last-covered-end" select="$a-intersect-clipEnd"/>
                            <xsl:with-param name="uncovered" as="map(*)*">
                                <xsl:sequence select="$uncovered"/>
                                <xsl:if test="$a-intersect-clipBegin &gt; $last-covered-end">
                                    <xsl:map>
                                        <xsl:map-entry key="'href'" select="$a-clip('href')"/>
                                        <xsl:map-entry key="'original-href'" select="$a-clip('original-href')"/>
                                        <xsl:map-entry key="'clipBegin'" select="$last-covered-end"/>
                                        <xsl:map-entry key="'clipEnd'" select="$a-intersect-clipBegin"/>
                                        <!-- interpolate between clip A's original-clipBegin and original-clipEnd -->
                                        <xsl:map-entry key="'original-clipBegin'"
                                                       select="$a-original-clipBegin - $a-clipBegin + $last-covered-end"/>
                                        <xsl:map-entry key="'original-clipEnd'"
                                                       select="$a-original-clipBegin - $a-clipBegin + $a-intersect-clipBegin"/>
                                    </xsl:map>
                                </xsl:if>
                            </xsl:with-param>
                        </xsl:next-iteration>
                    </xsl:iterate>
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
                    <xsl:for-each select="$b-anchors">
                        <xsl:variable name="b-anchor" as="map(*)" select="."/>
                        <xsl:choose>
                            <xsl:when test="some $a-anchor in $a-anchors
                                            satisfies $a-anchor('href')=$b-anchor('original-href') and
                                                      $a-anchor('id')=$b-anchor('original-id')">
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
                    <xsl:for-each select="$b-clips">
                        <xsl:variable name="b-clip" as="map(*)" select="."/>
                        <xsl:variable name="b-clipBegin" as="xs:decimal" select="$b-clip('clipBegin')"/>
                        <xsl:variable name="b-clipEnd" as="xs:decimal" select="$b-clip('clipEnd')"/>
                        <xsl:variable name="b-original-clipBegin" as="xs:decimal" select="$b-clip('original-clipBegin')"/>
                        <xsl:variable name="b-original-clipEnd" as="xs:decimal" select="$b-clip('original-clipEnd')"/>
                        <!-- clips in A that overlap with the clip in B -->
                        <xsl:variable name="a-clip" as="map(*)*"
                                      select="$a-clips[.('href')=$b-clip('original-href') and
                                                       .('clipBegin') &lt; $b-original-clipEnd and
                                                       $b-original-clipBegin &lt; .('clipEnd')]"/>
                        <xsl:variable name="a-clip" as="map(*)*">
                            <xsl:perform-sort select="$a-clip">
                                <xsl:sort select=".('clipBegin')"/>
                            </xsl:perform-sort>
                        </xsl:variable>
                        <xsl:iterate select="$a-clip">
                            <xsl:param name="last-covered-end" as="xs:decimal" select="$b-original-clipBegin"/>
                            <xsl:param name="uncovered" as="map(*)*" select="()"/>
                            <xsl:on-completion>
                                <xsl:variable name="uncovered" as="map(*)*">
                                    <xsl:sequence select="$uncovered"/>
                                    <xsl:if test="$b-original-clipBegin &gt; $last-covered-end">
                                        <xsl:map>
                                            <xsl:map-entry key="'href'" select="$b-clip('href')"/>
                                            <xsl:map-entry key="'clipEnd'" select="$b-clip('clipEnd')"/>
                                            <xsl:map-entry key="'original-href'" select="$b-clip('original-href')"/>
                                            <xsl:map-entry key="'original-clipEnd'" select="$b-clip('original-clipEnd')"/>
                                            <xsl:map-entry key="'original-clipBegin'" select="$last-covered-end"/>
                                            <!-- interpolate between clip B's clipBegin and clipEnd -->
                                            <xsl:map-entry key="'clipBegin'"
                                                           select="$last-covered-end + $b-clipBegin - $b-original-clipBegin"/>
                                        </xsl:map>
                                    </xsl:if>
                                </xsl:variable>
                                <xsl:if test="exists($uncovered)">
                                    <!-- Assuming that the fragment in B originates from an existing fragment. (Note that we
                                         could check whether mapping A maps the referenced fragment to another fragment, but
                                         we're assuming it is not the case.) -->
                                    <xsl:variable name="a-file" as="map(*)*"
                                                  select="$a-files[.('href')=$b-clip('original-href')]"/>
                                    <xsl:choose>
                                        <xsl:when test="exists($a-file)">
                                            <!-- A file in B could originate from multiple files in A. This means documents
                                                 are merged. Assuming that if none of the files list the fragment, the first
                                                 contains it (in other words the fragment is not deleted). -->
                                            <xsl:variable name="a-file" as="map(*)" select="$a-file[1]"/>
                                            <xsl:for-each select="$uncovered">
                                                <xsl:variable name="b-clip" as="map(*)" select="."/>
                                                <xsl:map>
                                                    <xsl:map-entry key="'href'" select="$b-clip('href')"/>
                                                    <xsl:map-entry key="'clipBegin'" select="$b-clip('clipBegin')"/>
                                                    <xsl:map-entry key="'clipEnd'" select="$b-clip('clipEnd')"/>
                                                    <xsl:map-entry key="'original-href'" select="$a-file('original-href')"/>
                                                    <xsl:map-entry key="'original-clipBegin'" select="$b-clip('original-clipBegin')"/>
                                                    <xsl:map-entry key="'original-clipEnd'" select="$b-clip('original-clipEnd')"/>
                                                </xsl:map>
                                            </xsl:for-each>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:sequence select="$uncovered"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:if>
                            </xsl:on-completion>
                            <xsl:variable name="a-clip" as="map(*)" select="."/>
                            <xsl:variable name="a-clipBegin" as="xs:decimal" select="$a-clip('clipBegin')"/>
                            <xsl:variable name="a-clipEnd" as="xs:decimal" select="$a-clip('clipEnd')"/>
                            <xsl:next-iteration>
                                <xsl:with-param name="last-covered-end" select="$a-clipEnd"/>
                                <xsl:with-param name="uncovered" as="map(*)*">
                                    <xsl:sequence select="$uncovered"/>
                                    <xsl:if test="$a-clipBegin &gt; $last-covered-end">
                                        <xsl:map>
                                            <xsl:map-entry key="'href'" select="$b-clip('href')"/>
                                            <xsl:map-entry key="'original-href'" select="$b-clip('original-href')"/>
                                            <xsl:map-entry key="'original-clipBegin'" select="$last-covered-end"/>
                                            <xsl:map-entry key="'original-clipEnd'" select="$a-clipBegin"/>
                                            <!-- interpolate between clip B's clipBegin and clipEnd -->
                                            <xsl:map-entry key="'clipBegin'"
                                                           select="$b-clipBegin - $b-original-clipBegin + $last-covered-end"/>
                                            <xsl:map-entry key="'clipEnd'"
                                                           select="$b-clipBegin - $b-original-clipBegin + $a-clipBegin"/>
                                        </xsl:map>
                                    </xsl:if>
                                </xsl:with-param>
                            </xsl:next-iteration>
                        </xsl:iterate>
                    </xsl:for-each>
                </xsl:if>
            </xsl:variable>
            <xsl:variable name="files-and-anchors-and-clips" as="element()*"> <!-- element(d:file|d:anchor|d:clip)* -->
                <xsl:for-each select="$files-and-anchors-and-clips">
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
                        <xsl:when test="$attributes[name()='clipBegin']">
                            <d:clip>
                                <xsl:sequence select="$attributes"/>
                            </d:clip>
                        </xsl:when>
                        <xsl:otherwise>
                            <d:file>
                                <xsl:sequence select="$attributes"/>
                            </d:file>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="files-with-anchors-and-clips" as="element(d:file)*">
                <xsl:for-each-group select="$files-and-anchors-and-clips" group-by="@original-href">
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
                            <xsl:for-each select="current-group()[self::d:clip]">
                                <xsl:copy>
                                    <xsl:sequence select="@clipBegin"/>
                                    <xsl:sequence select="@clipEnd"/>
                                    <xsl:if test="not(@clipBegin=@original-clipBegin and
                                                      @clipEnd=@original-clipEnd)">
                                        <xsl:sequence select="@original-clipBegin"/>
                                        <xsl:sequence select="@original-clipEnd"/>
                                    </xsl:if>
                                </xsl:copy>
                            </xsl:for-each>
                        </d:file>
                    </xsl:for-each-group>
                </xsl:for-each-group>
            </xsl:variable>
            <xsl:sequence select="$files-with-anchors-and-clips"/>
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

    <xsl:template mode="parse"
                  match="/d:fileset/d:file/d:clip[@clipBegin and @clipEnd]">
        <xsl:param name="normalized-parent" as="map(*)" required="yes"/>
        <xsl:map>
            <xsl:map-entry key="'href'" select="$normalized-parent('href')"/>
            <xsl:map-entry key="'original-href'" select="$normalized-parent('original-href')"/>
            <xsl:map-entry key="'clipBegin'" select="xs:decimal(@clipBegin)"/>
            <xsl:map-entry key="'clipEnd'" select="xs:decimal(@clipEnd)"/>
            <xsl:choose>
                <xsl:when test="@original-clipBegin and @original-clipEnd">
                    <xsl:map-entry key="'original-clipBegin'" select="xs:decimal(@original-clipBegin)"/>
                    <xsl:map-entry key="'original-clipEnd'" select="xs:decimal(@original-clipEnd)"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:map-entry key="'original-clipBegin'" select="xs:decimal(@clipBegin)"/>
                    <xsl:map-entry key="'original-clipEnd'" select="xs:decimal(@clipEnd)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:map>
    </xsl:template>

    <xsl:template mode="parse" match="@*|node()" priority="0.4"/>

</xsl:stylesheet>
