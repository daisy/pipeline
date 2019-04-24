<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns="" xpath-default-namespace=""
                exclude-result-prefixes="#all">
    
    <xsl:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xsl"/>
    
    <!--
        the smil
    -->
    <xsl:variable name="smil" select="collection()[1]/*"/>
    <!--
        the corresponding content document(s) in spine order
    -->
    <xsl:variable name="html" select="collection()[position()&gt;1]/*"/>
    
    <xsl:variable name="smil-base-uri" select="pf:base-uri($smil)"/>
    
    <xsl:key name="absolute-id" match="*[@id]" use="concat(pf:normalize-uri(pf:html-base-uri(.)),'#',@id)"/>
    
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
    
    <xsl:template match="/smil/body/seq">
        <xsl:variable name="seq-base-uri" select="pf:base-uri(.)"/>
        <xsl:variable name="missing-pars" as="element()*">
            <xsl:for-each select="$html">
                <xsl:variable name="html-base-uri" select="pf:html-base-uri(.)"/>
                <xsl:for-each select="//*[self::html:h1 or
                                          self::html:h2 or
                                          self::html:h3 or
                                          self::html:h4 or
                                          self::html:h5 or
                                          self::html:h6 or
                                          self::html:span[matches(@class,'(^|\s)page-(front|normal|special)(\s|$)')]]">
                    <!--
                        we are sure these elements have an id attribute, thanks to add-missing-ids.xsl
                    -->
                    <xsl:variable name="id" as="xs:string" select="@id"/>
                    <xsl:choose>
                        <xsl:when test="$referenced-html-elements intersect .">
                            <!-- The SMIL already references the element. No need to do anything. -->
                        </xsl:when>
                        <xsl:when test="$referenced-html-elements/descendant::* intersect .">
                            <!--
                                The SMIL already references a containing element. FIXME: Unlikely to
                                happen for headings, but less unlikely for page numbers.
                            -->
                            <xsl:message terminate="yes">FIXME</xsl:message>
                        </xsl:when>
                        <xsl:otherwise>
                            <!-- in case there are multiple html files and colliding ids, add a suffix -->
                            <!-- FIXME: check that id does not exist yet -->
                            <xsl:variable name="par-id" as="xs:string"
                                          select="concat(
                                                    'par_',$id,
                                                    if (
                                                      count(
                                                        $html//*[self::html:h1 or self::html:h2 or
                                                                 self::html:h3 or self::html:h4 or
                                                                 self::html:h5 or self::html:h6 or
                                                                 self::html:span[
                                                                   matches(@class,'(^|\s)page-(front|normal|special)(\s|$)')]]
                                                                [@id=$id]
                                                      )&gt;1
                                                    ) then concat('_',generate-id(.))
                                                      else '')"/>
                            <!-- FIXME: check that id does not exist yet -->
                            <xsl:variable name="text-id" as="xs:string" select="replace($par-id,'par_','text_')"/>
                            <xsl:choose>
                                <xsl:when test="$referenced-html-elements/ancestor::* intersect .">
                                    <!--
                                        The SMIL already references a contained element. Happens if the
                                        granularity is too fine, e.g. for word-level synchronization. Merge
                                        all the segments into a single par.
                                    -->
                                    <xsl:variable name="segments" as="element()*"
                                                  select="$referenced-html-elements[ancestor::* intersect current()]"/>
                                    <xsl:choose>
                                        <xsl:when test="replace(string-join($segments/string(.),''),'\s+','')=replace(string(.),'\s+','')">
                                            <xsl:variable name="audio-segments" as="element()*"
                                                          select="for $s in $segments return
                                                                  for $id in $s/concat(pf:normalize-uri(pf:html-base-uri(.)),'#',@id) return
                                                                  $smil//text[pf:normalize-uri(pf:resolve-uri(@src,.))=$id]/parent::*/audio"/>
                                            <xsl:choose>
                                                <xsl:when test="every $i in 1 to count($audio-segments) - 1
                                                                satisfies ($audio-segments[$i]/@src=$audio-segments[$i + 1]/@src and
                                                                           $audio-segments[$i]/@clip-end=$audio-segments[$i + 1]/@clip-begin)">
                                                    <par id="{$par-id}" endsync="last">
                                                        <text id="{$text-id}" src="{pf:relativize-uri(concat($html-base-uri,'#',$id),$seq-base-uri)}"/>
                                                        <audio src="{pf:relativize-uri($audio-segments[1]/pf:resolve-uri(@src,.),$seq-base-uri)}"
                                                               clip-begin="{$audio-segments[1]/@clip-begin}"
                                                               clip-end="{$audio-segments[last()]/@clip-end}"/>
                                                    </par>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                  <xsl:message terminate="yes"
                                                               select="concat(
                                                                         'SMIL &quot;',replace($smil-base-uri,'^.*/([^/]+)^','$1'),
                                                                         '&quot; references one or more segments inside a ',
                                                                         if (starts-with(local-name(),'h')) then 'heading' else 'page number',
                                                                         ' but the corresponding audio clips can not be combined: ',
                                                                         string-join($audio-segments/concat(@src,' (',@clip-begin,'-',@clip-end,')'),', '))"/>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                      </xsl:when>
                                      <xsl:otherwise>
                                          <xsl:message terminate="yes"
                                                       select="concat('SMIL &quot;',replace($smil-base-uri,'^.*/([^/]+)^','$1'),
                                                                      '&quot; references one or more segments inside a ',
                                                                      if (starts-with(local-name(),'h')) then 'heading' else 'page number',
                                                                      ' but these segments do not add up to the complete ',
                                                                      if (starts-with(local-name(),'h')) then 'heading' else 'page number',
                                                                      ': ',string-join($segments/concat($html-base-uri,'#',@id),', '))"/>
                                      </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                    <par id="{$par-id}" endsync="last">
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
                <!-- pf:base-uri will return empty sequence for $missing-pars -->
                <xsl:sort select="index-of($html[pf:base-uri(.)=current()/pf:resolve-uri(substring-before(text/@src,'#'),
                                                                                         (pf:base-uri(.),$seq-base-uri)[1])],
                                           $html)"/>
                <xsl:sort select="$html[pf:html-base-uri(.)=current()/pf:resolve-uri(substring-before(text/@src,'#'),
                                                                                     (pf:base-uri(.),$seq-base-uri)[1])]
                                  //*[@id=substring-after(current()/text/@src,'#')][1]/count(preceding::*|ancestor::*)"/>
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
                                                          self::html:h6 or
                                                          self::html:span[matches(@class,'(^|\s)page-(front|normal|special)(\s|$)')]])">
            <xsl:next-match/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
