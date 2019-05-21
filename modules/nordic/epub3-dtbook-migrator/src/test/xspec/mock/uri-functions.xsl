<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" exclude-result-prefixes="#all" version="2.0"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions">

    <xsl:function name="pf:tokenize-uri" as="xs:string*">
        <xsl:param name="uri" as="xs:string?"/>
        <!--
            Uses the regex defined in RFC3986 (Appendix B) to tokenize the URI in 5 parts:
            1. scheme
            2. authority
            3. path
            4. query
            5. fragment
        -->
        <xsl:analyze-string select="concat('X',$uri)" regex="^X(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\?([^#]*))?(#(.*))?">
            <xsl:matching-substring>
                <xsl:sequence select="(substring-before(regex-group(1),':'),regex-group(4),regex-group(5),regex-group(7),regex-group(9))"/>
            </xsl:matching-substring>
        </xsl:analyze-string>
    </xsl:function>

    <xsl:function name="pf:recompose-uri" as="xs:string">
        <xsl:param name="tokens" as="xs:string*"/>
        <xsl:sequence
            select="string-join((
            if($tokens[1]) then ($tokens[1],':') else (),
            if($tokens[2]) then ('//',$tokens[2]) else if($tokens[1] and not($tokens[1]=('mailto','file'))) then '/' else (),
            $tokens[3],
            if($tokens[4]) then ('?',$tokens[4]) else (),
            if($tokens[5]) then ('#',$tokens[5]) else ()
            ),'')"
        />
    </xsl:function>

    <xsl:function name="pf:is-absolute" as="xs:boolean">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:variable name="tokens" select="pf:tokenize-uri(normalize-space($uri))"/>
        <xsl:sequence select="starts-with($tokens[3],'/')"/>
    </xsl:function>

    <xsl:function name="pf:is-relative" as="xs:boolean">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:variable name="tokens" select="pf:tokenize-uri(normalize-space($uri))"/>
        <xsl:sequence select="not($tokens[1]) and not(starts-with($tokens[3],'/'))"/>
    </xsl:function>

    <xsl:function name="pf:get-scheme" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="pf:tokenize-uri(normalize-space($uri))[1]"/>
    </xsl:function>

    <!--TODO write tests-->
    <xsl:function name="pf:get-path" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="pf:tokenize-uri(normalize-space($uri))[3]"/>
    </xsl:function>

    <!--TODO write tests-->
    <xsl:function name="pf:get-extension" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="lower-case(replace(pf:get-path($uri),'^.*\.([^.]*)$','$1'))"/>
    </xsl:function>

    <!--TODO write tests-->
    <xsl:function name="pf:replace-path" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:param name="path" as="xs:string?"/>
        <xsl:variable name="tokens" select="pf:tokenize-uri(normalize-space($uri))" as="xs:string*"/>
        <xsl:sequence select="pf:recompose-uri(($tokens[1],$tokens[2],pf:normalize-path($path),$tokens[4],$tokens[5]))"/>
    </xsl:function>

    <xsl:function name="pf:normalize-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:sequence select="pf:normalize-uri($uri,true())"/>
    </xsl:function>
    
    <xsl:function name="pf:normalize-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:param name="fragment" as="xs:boolean?"/>
        <!--
            http://en.wikipedia.org/wiki/URL_normalization
            - path segment normalization
            - case normalization
            - percent-encoding normalization
            - default http port
        -->

        <!--
            normalize percent encodings:
             * capitalize
             * decode ALPHA (%41–%5A and %61–%7A), DIGIT (%30–%39), hyphen (%2D), period (%2E), underscore (%5F), and tilde (%7E)
        -->
        <xsl:variable name="uri">
            <xsl:choose>
                <xsl:when test="$uri">
                    <xsl:variable name="cp-base" select="string-to-codepoints('0A')" as="xs:integer+"/>
                    <xsl:analyze-string select="$uri" regex="(%[0-9A-F]{{2}})+" flags="i">
                        <xsl:matching-substring>
                            <!-- capitalize -->
                            <xsl:variable name="upper-case" select="upper-case(.)"/>

                            <!-- decode ALPHA/DIGIT/hyphen/period/underscore/tilde -->
                            <xsl:variable name="utf8-bytes" as="xs:integer+">
                                <xsl:analyze-string select="$upper-case" regex="%([0-9A-F]{{2}})" flags="i">
                                    <xsl:matching-substring>
                                        <xsl:variable name="nibble-pair"
                                            select="
                                           for $nibble-char in string-to-codepoints(upper-case(regex-group(1))) return
                                           if ($nibble-char ge $cp-base[2]) then
                                           $nibble-char - $cp-base[2] + 10
                                           else
                                           $nibble-char - $cp-base[1]"
                                            as="xs:integer+"/>
                                        <xsl:sequence select="$nibble-pair[1] * 16 + $nibble-pair[2]"/>
                                    </xsl:matching-substring>
                                </xsl:analyze-string>
                            </xsl:variable>

                            <xsl:value-of select="if ($utf8-bytes = (65 to 90, 97 to 122, 48 to 57, 45, 46, 95, 126)) then codepoints-to-string(pf:utf8-decode($utf8-bytes)) else $upper-case"/>
                        </xsl:matching-substring>
                        <xsl:non-matching-substring>
                            <xsl:value-of select="."/>
                        </xsl:non-matching-substring>
                    </xsl:analyze-string>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$uri"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="tokens" select="pf:tokenize-uri(normalize-space($uri))" as="xs:string*"/>
        <xsl:variable name="scheme" select="$tokens[1]"/>
        <xsl:variable name="authority" select="$tokens[2]"/>
        <xsl:variable name="path" select="$tokens[3]"/>
        <xsl:variable name="query" select="$tokens[4]"/>
        <xsl:variable name="fragment" select="if ($fragment) then $tokens[5] else ()"/>
        
        <!-- lower case scheme and authority components -->
        <xsl:variable name="scheme" select="lower-case($scheme)"/>
        <xsl:variable name="authority" select="lower-case($authority)"/>

        <!-- remove default port -->
        <xsl:variable name="authority" select="if ($scheme='http' and ends-with($authority,':80')) then substring($authority,1,string-length($authority)-3) else $authority"/>

        <!-- normalize path -->
        <xsl:variable name="path" select="pf:normalize-path($path)"/>
        
        <xsl:variable name="uri" select="pf:recompose-uri(($scheme,$authority,$path,$query,$fragment))"/>
        <xsl:variable name="uri" select="pf:file-expand83($uri)" use-when="function-available('pf:file-expand83')"/>
        
        <xsl:sequence select="iri-to-uri($uri)"/>
    </xsl:function>

    <xsl:function name="pf:relativize-uri" as="xs:string">
        <xsl:param name="uri" as="xs:string?"/>
        <xsl:param name="base" as="xs:string?"/>
        <xsl:variable name="uri-tokens" select="pf:tokenize-uri(pf:normalize-uri($uri))"/>
        <xsl:variable name="base-tokens" select="pf:tokenize-uri(pf:normalize-uri($base))"/>

        <xsl:choose>
            <xsl:when test="(not($uri-tokens[1]) or $uri-tokens[1]=$base-tokens[1]) and (not($uri-tokens[2]) or $uri-tokens[2]=$base-tokens[2])">
                <xsl:sequence select="pf:recompose-uri(('','',pf:relativize-path($uri-tokens[3],$base-tokens[3]),$uri-tokens[4],$uri-tokens[5]))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="pf:recompose-uri($uri-tokens)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="pf:normalize-path" as="xs:string?">
        <xsl:param name="path" as="xs:string?"/>
        <xsl:variable name="normalized" as="xs:string?"
            select="
            replace(
                replace(
                    replace(
                        replace(
                            replace($path,'^(\.(/|$))+','')
                        ,'/(\.(/|$))+','/')
                    ,'/+','/')
                ,'(^|/)\.\.$','$1../')
            ,'^/(\.\./)+','/')
            "/>
        <xsl:sequence select="
            if (matches($normalized,'([^/\.]|\.[^/\.]|[^/\.]\.|[^/]{3,})/\.\./')) then
            pf:normalize-path(replace($normalized,'([^/\.]|\.[^/\.]|[^/\.]\.|[^/]{3,})/\.\./',''))
            else 
            $normalized"/>
    </xsl:function>

    <xsl:function name="pf:relativize-path" as="xs:string">
        <xsl:param name="path" as="xs:string?"/>
        <xsl:param name="base" as="xs:string?"/>

        <xsl:choose>
            <xsl:when test="starts-with($path,'/')">
                <xsl:variable name="path-segments" select="tokenize($path, '/')"/>
                <xsl:variable name="base-segments" select="tokenize($base, '/')[position()!=last()]"/>
                <xsl:variable name="common-prefix-length"
                    select="
                    (for $i in 1 to count($base-segments) return
                         if($base-segments[$i] eq $path-segments[$i]) then () else $i -1
                    ,count($base-segments))[1]"/>
                <xsl:variable name="upSteps" select="count($base-segments) -$common-prefix-length"/>
                <xsl:sequence
                    select="string-join((
                    for $i in 1 to $upSteps
                        return '..',
                    for $i in 1 to count($path-segments) - $common-prefix-length 
                        return $path-segments[$common-prefix-length + $i]
                    ),'/')"
                />
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$path"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="pf:longest-common-uri" as="xs:string">
        <xsl:param name="uris" as="xs:string*"/>
        <xsl:choose>
            <xsl:when test="count($uris)=1">
                <xsl:value-of select="$uris"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="tokens" select="pf:tokenize-uri(normalize-space($uris[1]))" as="xs:string*"/>
                <xsl:variable name="uri-filter" select="if (not($tokens[1])) then '' else if ($tokens[2]) then concat($tokens[1],'://',$tokens[2]) else $tokens[1]"/>
                <xsl:variable name="uris" select="$uris[starts-with(.,$uri-filter)]"/>
                <xsl:variable name="main-uri" select="for $part in tokenize(replace(pf:normalize-uri(replace($uris[1],'[#\?].*$','')),'/+','SLASH|/'),'/') return replace($part,'SLASH\|$','/')"/>
                <xsl:variable name="count-common" as="xs:integer*">
                    <xsl:for-each select="$uris[position() &gt; 1]">
                        <xsl:variable name="compare-uri" select="for $part in tokenize(replace(pf:normalize-uri($uris[2]),'/+','SLASH|/'),'/') return replace($part,'SLASH\|$','/')"/>
                        <xsl:sequence select="min(for $i in 1 to count($main-uri) return if ($main-uri[$i]=$compare-uri[$i]) then () else $i)"/>
                    </xsl:for-each>
                </xsl:variable>
                <xsl:variable name="count-common" select="min(($count-common, count($main-uri)))"/>
                <xsl:variable name="longest-common" select="$main-uri[position() &lt; $count-common]"/>
                <xsl:variable name="longest-common" select="concat($longest-common[1],if (matches($longest-common[1],'^\w+:/$') and (not($tokens[1]='file') or $tokens[2])) then '/' else '',string-join($longest-common[position()&gt;1],''))"/>
                <xsl:variable name="longest-common" select="replace($longest-common, '[^/]+$', '')"/>
                <xsl:value-of select="$longest-common"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <!--
        Taken from: http://stackoverflow.com/questions/13697036/url-query-xml-and-xpath-transform
        This could (should?) be replaced by a native Java-based extension function
    -->
    <xsl:function name="pf:unescape-uri" as="xs:string">
        <xsl:param name="string" as="xs:string?"/>
        <xsl:variable name="cp-base" select="string-to-codepoints('0A')" as="xs:integer+"/>
        <xsl:variable name="result">

            <xsl:analyze-string select="$string" regex="(%[0-9A-F]{{2}})+" flags="i">
                <xsl:matching-substring>
                    <xsl:variable name="utf8-bytes" as="xs:integer+">
                        <xsl:analyze-string select="." regex="%([0-9A-F]{{2}})" flags="i">
                            <xsl:matching-substring>
                                <xsl:variable name="nibble-pair"
                                    select="
                                    for $nibble-char in string-to-codepoints( upper-case(regex-group(1))) return
                                    if ($nibble-char ge $cp-base[2]) then
                                    $nibble-char - $cp-base[2] + 10
                                    else
                                    $nibble-char - $cp-base[1]"
                                    as="xs:integer+"/>
                                <xsl:sequence select="$nibble-pair[1] * 16 + $nibble-pair[2]"/>
                            </xsl:matching-substring>
                        </xsl:analyze-string>
                    </xsl:variable>
                    <xsl:value-of select="codepoints-to-string( pf:utf8-decode( $utf8-bytes))"/>
                </xsl:matching-substring>
                <xsl:non-matching-substring>
                    <xsl:value-of select="."/>
                </xsl:non-matching-substring>
            </xsl:analyze-string>
        </xsl:variable>
        <xsl:sequence select="string($result)"/>
    </xsl:function>

    <xsl:function name="pf:utf8-decode" as="xs:integer*">
        <xsl:param name="bytes" as="xs:integer*"/>
        <xsl:choose>
            <xsl:when test="empty($bytes)"/>
            <xsl:when test="$bytes[1] eq 0">
                <!-- The null character is not valid for XML. -->
                <xsl:sequence select="pf:utf8-decode( remove( $bytes, 1))"/>
            </xsl:when>
            <xsl:when test="$bytes[1] le 127">
                <xsl:sequence select="$bytes[1], pf:utf8-decode( remove( $bytes, 1))"/>
            </xsl:when>
            <xsl:when test="$bytes[1] lt 224">
                <xsl:sequence select="
                    ((($bytes[1] - 192) * 64) +
                    ($bytes[2] - 128)        ),
                    pf:utf8-decode( remove( remove( $bytes, 1), 1))"/>
            </xsl:when>
            <xsl:when test="$bytes[1] lt 240">
                <xsl:sequence
                    select="
                    ((($bytes[1] - 224) * 4096) +
                    (($bytes[2] - 128) *   64) +
                    ($bytes[3] - 128)          ),
                    pf:utf8-decode( remove( remove( remove( $bytes, 1), 1), 1))"
                />
            </xsl:when>
            <xsl:when test="$bytes[1] lt 248">
                <xsl:sequence
                    select="
                    ((($bytes[1] - 224) * 262144) +
                    (($bytes[2] - 128) *   4096) +
                    (($bytes[3] - 128) *     64) +
                    ($bytes[4] - 128)            ),
                    pf:utf8-decode( $bytes[position() gt 4])"
                />
            </xsl:when>
            <xsl:otherwise>
                <!-- Code-point valid for XML. -->
                <xsl:sequence select="pf:utf8-decode( remove( $bytes, 1))"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>


</xsl:stylesheet>
