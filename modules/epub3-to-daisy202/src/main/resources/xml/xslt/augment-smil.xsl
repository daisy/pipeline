<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns="" xpath-default-namespace=""
                exclude-result-prefixes="#all">
    
    <xsl:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xsl"/>
    <xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>
    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>
    
    <!--
        the smil
    -->
    <xsl:variable name="smil" select="collection()[1]/*"/>
    <!--
        the corresponding content document(s) in spine order
    -->
    <xsl:variable name="html" select="collection()[position()&gt;2]/*"/>
    <!--
        the list of pagebreak elements
    -->
    <xsl:variable name="page-list" select="collection()[2]"/>
    
    <xsl:variable name="smil-base-uri" select="pf:base-uri($smil)"/>
    
    <xsl:key name="id" match="*[@id]" use="@id"/>
    <xsl:key name="absolute-id" match="*[@id]" use="concat(pf:normalize-uri(pf:html-base-uri(.)),'#',@id)"/>
    <xsl:key name="absolute-src" match="text" use="pf:normalize-uri(pf:resolve-uri(@src,.))"/>
    
    <xsl:variable name="referenced-html-elements" as="element()*">
        <xsl:for-each select="//text">
            <xsl:variable name="absolute-src" select="pf:normalize-uri(pf:resolve-uri(@src,.))"/>
            <xsl:variable name="referenced-element" as="element()*" select="$html/key('absolute-id',$absolute-src)"/>
            <xsl:if test="count($referenced-element)=0">
                <xsl:message terminate="yes"
                             select="concat('SMIL &quot;',replace($smil-base-uri,'^.*/([^/]+)^','$1'),
                                            '&quot; references a non-existing element &quot;',$absolute-src,'&quot;')"/>
            </xsl:if>
            <xsl:if test="count($referenced-element)&gt;1">
                <xsl:message select="concat('Reference &quot;',@src,'&quot; in SMIL &quot;',
                                            replace($smil-base-uri,'^.*/([^/]+)^','$1'),'&quot; is ambiguous')"/>
            </xsl:if>
            <xsl:sequence select="$referenced-element[1]"/>
        </xsl:for-each>
    </xsl:variable>
    
    <xsl:variable name="page-number-elements" as="element()*">
      <xsl:for-each select="$html">
        <xsl:variable name="content-doc" select="."/>
        <xsl:variable name="content-doc-uri" select="pf:normalize-uri(base-uri(.))"/>
        <xsl:for-each select="$page-list//d:file[pf:normalize-uri(resolve-uri(@href,base-uri(.)))=$content-doc-uri]/d:anchor">
          <xsl:sequence select="key('id',@id,$content-doc)"/>
        </xsl:for-each>
      </xsl:for-each>
    </xsl:variable>
    
    <xsl:template match="/smil/body/seq" priority="1">
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'par_'"/>
            <xsl:with-param name="for-elements"
                            select="($html//*[self::html:h1 or
                                              self::html:h2 or
                                              self::html:h3 or
                                              self::html:h4 or
                                              self::html:h5 or
                                              self::html:h6],
                                     $page-number-elements)
                                    except $referenced-html-elements"/>
            <xsl:with-param name="in-use" select="(//*/@id,
                                                   //*/@id[matches(.,'^(text|audio)_')]/replace(.,'^(text|audio)','par'))"/>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="/smil/body/seq">
        <xsl:variable name="seq" select="."/>
        <xsl:variable name="seq-base-uri" select="pf:base-uri(.)"/>
        <xsl:variable name="missing-pars" as="element()*">
            <xsl:for-each select="$html">
                <xsl:variable name="html-base-uri" select="pf:html-base-uri(.)"/>
                <xsl:variable name="heading-and-pagenum-elements" as="element()*"
                              select="(//*[self::html:h1 or
                                           self::html:h2 or
                                           self::html:h3 or
                                           self::html:h4 or
                                           self::html:h5 or
                                           self::html:h6],
                                       //* intersect $page-number-elements)"/>
                <xsl:for-each select="$heading-and-pagenum-elements">
                    <!--
                        we know these elements have an id attribute (added in create-ncc.xpl)
                    -->
                    <xsl:variable name="id" as="xs:string" select="@id"/>
                    <xsl:choose>
                        <xsl:when test="$referenced-html-elements intersect .">
                            <!-- The SMIL already references the element. No need to do anything. -->
                        </xsl:when>
                        <xsl:when test="$referenced-html-elements/descendant::* intersect .
                                        or $heading-and-pagenum-elements/descendant::* intersect .">
                            <!--
                                The SMIL already references, or will reference, a containing element.
                                Unlikely to happen for headings, but less unlikely for page numbers.
                            -->
                            <xsl:choose>
                                <xsl:when test=". intersect $page-number-elements">
                                    <!--
                                        If it's a page number element, we can point from the NCC to the par
                                        that contains it (this happens in create-linkbacks.xsl)
                                    -->
                                    <xsl:message select="concat(
                                                           'SMIL references an element that contains a page number. ',
                                                           'NCC will point to the containing element instead of the page number.')"/>
                                    <!--
                                        Create a temporary seq that points to the par of the containing element.
                                        Later it will be removed.
                                    -->
                                    <seq textref="{pf:relativize-uri(concat($html-base-uri,'#',$id),$seq-base-uri)}">
                                        <xsl:attribute name="contained-in">
                                            <xsl:variable name="contained-in" as="attribute(id)">
                                                <xsl:for-each select="($heading-and-pagenum-elements,$referenced-html-elements)
                                                                      [descendant::* intersect current()][1]">
                                                    <xsl:choose>
                                                        <xsl:when test="$referenced-html-elements intersect .">
                                                            <xsl:variable name="absolute-id" select="concat(pf:normalize-uri(pf:html-base-uri(.)),'#',@id)"/>
                                                            <xsl:sequence select="$smil/key('absolute-src',$absolute-id)/parent::par/@id"/>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:call-template name="pf:generate-id"/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:for-each>
                                            </xsl:variable>
                                            <xsl:sequence select="string($contained-in)"/>
                                        </xsl:attribute>
                                    </seq>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:message terminate="yes">
                                        <xsl:text>Text/audio synchronization too coarse. SMIL references an ancestor of the heading with id </xsl:text>
                                        <xsl:value-of select="@id"/>
                                    </xsl:message>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="par-id" as="xs:string">
                                <xsl:call-template name="pf:generate-id"/>
                            </xsl:variable>
                            <xsl:variable name="text-id" as="xs:string" select="replace($par-id,'par_','text_')"/>
                            <xsl:choose>
                                <xsl:when test="$referenced-html-elements/ancestor::* intersect .">
                                    <!--
                                        The SMIL already references a contained element. Happens if the
                                        granularity is too fine, e.g. for word-level synchronization. Merge
                                        all the segments into a single par. (Note that we can't wrap the
                                        segments in a seq because the NCC may only reference par or text.)
                                    -->
                                    <xsl:variable name="segments" as="element()*"
                                                  select="$referenced-html-elements[ancestor::* intersect current()]"/>
                                    <xsl:choose>
                                        <!--
                                            Test whether the segments make up the whole heading or page number
                                        -->
                                        <xsl:when test="replace(string-join($segments/string(.),''),'[\s\p{Z}\p{P}]+','')
                                                        =replace(string(.),'[\s\p{Z}\p{P}]+','')">
                                            <xsl:variable name="smil-segments" as="element()*"
                                                          select="for $s in $segments return
                                                                  for $id in $s/concat(pf:normalize-uri(pf:html-base-uri(.)),'#',@id) return
                                                                  $smil//text[pf:normalize-uri(pf:resolve-uri(@src,.))=$id]/parent::*"/>
                                            <xsl:variable name="audio-segments" as="element()*" select="$smil-segments/audio"/>
                                            <xsl:choose>
                                                <!--
                                                    Test whether the audio clips are from the same audio file and follow each
                                                    other directly.
                                                -->
                                                <xsl:when test="every $i in 1 to count($audio-segments) - 1
                                                                satisfies ($audio-segments[$i]/@src=$audio-segments[$i + 1]/@src and
                                                                           $audio-segments[$i]/@clip-end=$audio-segments[$i + 1]/@clip-begin)">
                                                    <xsl:variable name="audio-id" as="xs:string" select="replace($par-id,'par_','audio_')"/>
                                                    <par id="{$par-id}" endsync="last">
                                                        <xsl:if test=". intersect $page-number-elements">
                                                            <xsl:attribute name="system-required" select="'pagenumber-on'"/>
                                                        </xsl:if>
                                                        <text id="{$text-id}" src="{pf:relativize-uri(concat($html-base-uri,'#',$id),$seq-base-uri)}"/>
                                                        <audio id="{$audio-id}"
                                                               src="{pf:relativize-uri($audio-segments[1]/pf:resolve-uri(@src,.),$seq-base-uri)}"
                                                               clip-begin="{$audio-segments[1]/@clip-begin}"
                                                               clip-end="{$audio-segments[last()]/@clip-end}"/>
                                                    </par>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                  <!--
                                                      Not terminating here because we can recover from it in create-linkbacks.xsl,
                                                      by linking to the first segment.

                                                      An alternative would be to merge the audio segments by wrapping them in a
                                                      seq (seq can be child of par and parent of audio).

                                                      Another alternative would be to merge audio files.
                                                  -->
                                                  <xsl:call-template name="pf:debug">
                                                      <xsl:with-param name="msg">SMIL &quot;<xsl:value-of select="
                                                      replace($smil-base-uri,'^.*/([^/]+)^','$1') "/>&quot; references one or more segments inside a
                                                      <xsl:value-of select=" if (starts-with(local-name(),'h')) then 'heading' else 'page number'"/>
                                                      but the corresponding audio clips can not be combined: <xsl:value-of select="
                                                      string-join($audio-segments/concat(@src,' (',@clip-begin,'-',@clip-end,')'),', ')"/></xsl:with-param>
                                                  </xsl:call-template>
                                                  <!--
                                                      Create an intermediary seq that contains all the segments. Later it will
                                                      be replaced with the pars in the seq.
                                                  -->
                                                  <seq id="{$par-id}" textref="{pf:relativize-uri(concat($html-base-uri,'#',$id),$seq-base-uri)}">
                                                      <xsl:choose>
                                                          <xsl:when test=". intersect $page-number-elements">
                                                              <xsl:for-each select="$smil-segments">
                                                                  <xsl:attribute name="system-required" select="'pagenumber-on'"/>
                                                                  <xsl:sequence select="@*|node()"/>
                                                              </xsl:for-each>
                                                          </xsl:when>
                                                          <xsl:otherwise>
                                                              <xsl:sequence select="$smil-segments"/>
                                                          </xsl:otherwise>
                                                      </xsl:choose>
                                                  </seq>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                      </xsl:when>
                                      <xsl:otherwise>
                                          <!--
                                              Not terminating because we can recover from it in create-linkbacks.xsl, by
                                              linking to the first segment.
                                          -->
                                          <xsl:call-template name="pf:debug">
                                              <xsl:with-param name="msg">SMIL &quot;<xsl:value-of select="
                                              replace($smil-base-uri,'^.*/([^/]+)^','$1') "/>&quot; references one or more segments inside a
                                              <xsl:value-of select=" if (starts-with(local-name(),'h')) then 'heading' else 'page number'"/>
                                              but these segments do not add up to the complete <xsl:value-of select="
                                              if (starts-with(local-name(),'h')) then 'heading' else 'page number'"/>. The NCC will contain a
                                              link to the first segment.</xsl:with-param>
                                          </xsl:call-template>
                                          <!--
                                              Create an intermediary seq that contains all the segments. Later it will be
                                              replaced with the pars in the seq.
                                          -->
                                          <seq id="{$par-id}" textref="{pf:relativize-uri(concat($html-base-uri,'#',$id),$seq-base-uri)}">
                                              <xsl:variable name="smil-segments" as="element()*"
                                                            select="for $s in $segments return
                                                                    for $id in $s/concat(pf:normalize-uri(pf:html-base-uri(.)),'#',@id) return
                                                                    $smil//text[pf:normalize-uri(pf:resolve-uri(@src,.))=$id]/parent::*"/>
                                              <xsl:choose>
                                                  <xsl:when test=". intersect $page-number-elements">
                                                      <xsl:for-each select="$smil-segments">
                                                          <xsl:attribute name="system-required" select="'pagenumber-on'"/>
                                                          <xsl:sequence select="@*|node()"/>
                                                      </xsl:for-each>
                                                  </xsl:when>
                                                  <xsl:otherwise>
                                                      <xsl:sequence select="$smil-segments"/>
                                                  </xsl:otherwise>
                                              </xsl:choose>
                                          </seq>
                                      </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                    <par id="{$par-id}" endsync="last">
                                        <xsl:if test=". intersect $page-number-elements">
                                            <xsl:attribute name="system-required" select="'pagenumber-on'"/>
                                        </xsl:if>
                                        <text id="{$text-id}" src="{pf:relativize-uri(concat($html-base-uri,'#',$id),$seq-base-uri)}"/>
                                    </par>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:variable>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="(par,$missing-pars)">
                <!--
                    First sort by spine order.

                    (pf:base-uri will return empty sequence for $missing-pars)
                -->
                <xsl:sort select="index-of($html[pf:base-uri(.)=current()/pf:resolve-uri(substring-before(@textref|text/@src,'#'),
                                                                                         (pf:base-uri(.),$seq-base-uri)[1])],
                                           $html)"/>
                <!--
                    Then sort by document order within content document.

                    In case this does not match the default reading order (such as when a note body
                    does not come right after the corresponding note ref in the content document),
                    this is fixed in a subsequent step.
                -->
                <xsl:sort select="$html[pf:html-base-uri(.)=current()/pf:resolve-uri(substring-before(@textref|text/@src,'#'),
                                                                                     (pf:base-uri(.),$seq-base-uri)[1])]
                                  //*[@id=substring-after(current()/(@textref|text/@src),'#')][1]/count(preceding::*|ancestor::*)"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="/smil/body/seq/par">
        <xsl:variable name="absolute-src" select="pf:normalize-uri(pf:resolve-uri(text/@src,.))"/>
        <xsl:variable name="referenced-element" as="element()" select="($html/key('absolute-id',$absolute-src))[1]"/>
        <xsl:if test="not($referenced-element/ancestor::*[self::html:h1 or
                                                          self::html:h2 or
                                                          self::html:h3 or
                                                          self::html:h4 or
                                                          self::html:h5 or
                                                          self::html:h6]
                          or $referenced-element/ancestor::* intersect $page-number-elements)">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
