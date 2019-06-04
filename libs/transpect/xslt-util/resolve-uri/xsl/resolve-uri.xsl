<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:tr="http://transpect.io"
  version="2.0">

  <!-- If override is an absolute URI, returns it.
       If override is a relative URI, resolves it wrt $uri-so-far.
       If a path component in either URI is '..', its parent path 
       component will be consumed. 
       The parent path of foo/bar/ and foo/bar is foo/. 
       Applying '..' or '../' to either must yield identical results.
       If override is empty or the empty string, a normalized $uri-so-far will be returned.
       This is the inverse function to tr:uri-to-relative-path()
       https://github.com/transpect/xslt-util/blob/master/uri-to-relative-path/xsl/uri-to-relative-path.xsl
  -->
  <xsl:function name="tr:uri-composer" as="xs:string">
    <xsl:param name="uri-so-far" as="xs:string"/>
    <xsl:param name="override" as="xs:string?"/>
    <xsl:variable name="start-regex" as="xs:string" select="'^(file|https?|ftps?|gopher|mailto):/+'"/>
    <xsl:choose>
      <xsl:when test="matches($override, $start-regex)">
        <xsl:sequence select="$override"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="override-path" as="xs:string*" 
          select="tokenize($override, '/+')[. = ('..', '.') or not(position() = last())]"/>
        <xsl:variable name="override-file" as="xs:string?" 
          select="if (not($override)) (: empty or '': keep orig filename, but normalize paths :) 
          				then replace($uri-so-far, '^.+/', '')
          				else replace($override, '^(.+/)?(.+)$', '$2')[not(matches(., '(/|^\.\.$)'))]"/>
        <xsl:variable name="out" as="xs:string+">
          <xsl:analyze-string select="$uri-so-far" regex="{$start-regex}">
            <xsl:matching-substring>
              <xsl:sequence select="."/>
            </xsl:matching-substring>
            <xsl:non-matching-substring>
              <xsl:variable name="tokenized-so-far" as="xs:string*">
                <xsl:choose>
                  <xsl:when test="not(matches(tokenize(., '/')[last()], '^(\.\.)?$'))
                                  and exists($override-file)
                                  (: and not(exists($override-path)) :)">
                    <!-- The commented-out condition created file:/tmp/foo.txt/bar.txt from file:/tmp/foo.txt bar.txt -->
                    <!-- URI ending in what appears to be a regular file name,
                      and the override provides a file name -->
                    <xsl:sequence select="tokenize(., '/')[not(position() = last())]"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:sequence select="tokenize(., '/')"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="encoded-file-override" as="xs:string+">
                <xsl:choose>
                  <xsl:when test="not($override-file)">
                    <xsl:sequence select="''"/>
                  </xsl:when>
                  <xsl:when test="matches($override-file, '[#?]')">
                    <xsl:sequence select="tr:encode-for-uri(replace($override-file, '^([^#?]*)([#?].*)$', '$1'))"/>
                    <!-- We leave the fragment id or query string part as is (for the time being): --> 
                    <xsl:sequence select="replace($override-file, '^([^#?]*)([#?].*)$', '$2')"/>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:sequence select="tr:encode-for-uri($override-file)"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:variable>
              <xsl:variable name="parents-eaten-encoded" as="xs:string?"
                select="string-join(
                          tr:encode-all-but-drive-letter(
                            tr:eat-parent(
                              ( $tokenized-so-far[normalize-space()], $override-path )
                            )
                          ),
                          '/'
                        )"/>
              <xsl:sequence select="if($parents-eaten-encoded ne '') 
                                    then concat(
                                      $parents-eaten-encoded,
                                      '/',
                                      string-join($encoded-file-override, '')
                                    ) 
                                    else string-join($encoded-file-override, '')"/>
            </xsl:non-matching-substring>
          </xsl:analyze-string>
        </xsl:variable>
        <xsl:sequence select="string-join($out, '')"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="tr:encode-for-uri" as="xs:string?">
    <!-- An idempotent encode-for-uri() -->
    <xsl:param name="input" as="xs:string?"/>
    <xsl:choose>
      <xsl:when test="not($input)"/>
      <xsl:when test="matches(replace($input, '%[\dA-F]{2}', ''), '^[-_.~a-z\d]*$', 'i')">
        <!-- already encoded, or nothing to do -->
        <xsl:sequence select="$input"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="encode-for-uri($input)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="tr:encode-all-but-drive-letter" as="xs:string*">
    <xsl:param name="tokens" as="xs:string*"/>
    <xsl:choose>
      <xsl:when test="empty($tokens)"/>
      <xsl:when test="matches($tokens[1], '^[a-z]:$', 'i')">
        <xsl:sequence select="$tokens[1], for $t in $tokens[position() = (2 to last())]
                                          return tr:encode-for-uri($t)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="for $t in $tokens
                              return tr:encode-for-uri($t)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="tr:eat-parent" as="xs:string*">
    <!-- Boils down a tokenized directory path ('..' will eat their parent dirs, '.' will be ignored) -->
    <xsl:param name="tokens" as="xs:string*"/>
    <!-- Discard empty path components or references to the current directory -->
    <xsl:variable name="filtered" select="$tokens[not(. = ('', '.'))]" as="xs:string*"/>
    <xsl:variable name="prelim-pos" select="index-of($filtered, '..')" as="xs:integer*"/>
    <xsl:variable name="pos" select="tr:reprieve-leading-parents($prelim-pos, 1)" as="xs:integer*"/>
    <xsl:choose>
      <xsl:when test="exists($pos)">
        <xsl:sequence select="tr:eat-parent((subsequence($filtered, 1, $pos[1] - 2), subsequence($filtered, $pos[1] + 1)))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$filtered"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  <xsl:function name="tr:reprieve-leading-parents" as="xs:integer*">
    <!-- will turn (1, 2, 4, 5) into (4, 5) 
         and (2, 4, 5) into (2, 4, 5). 
         That is, leading '..' paths will stay untouched in tr:eat-parent() -->
    <xsl:param name="parent-positions" as="xs:integer*"/>
    <xsl:param name="look-at" as="xs:integer"/>
    <xsl:choose>
      <xsl:when test="$parent-positions[1] = $look-at">
        <xsl:sequence select="tr:reprieve-leading-parents(subsequence($parent-positions, 2), $look-at + 1)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$parent-positions"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <!-- This is here for historic reasons. It might benefit from switching to tr:uri-composer(),
    although the latter currently lacks the fn:resolve-uri() invocation. -->
  <xsl:function name="tr:resolve-uri" as="xs:string">
    <xsl:param name="dir" as="xs:string"/>
    <xsl:param name="filename" as="xs:string"/>
    <xsl:sequence select="xs:string(resolve-uri(concat($dir, '/', $filename)))"/>
  </xsl:function>

  
  <!-- You should use the xproc step tr:file-uri if possible. It is 
  applicable to more types of input. Apart from that, the two functions 
  tr:resolve-system-from-uri() and tr:resolve-uri-from-system() should
  swap their names, shouldn’t they? -->
  <xsl:function name="tr:resolve-system-from-uri" as="xs:string">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="matches($uri, '^/')">
        <xsl:sequence select="concat('file:', $uri)"/>
      </xsl:when>
      <xsl:when test="matches($uri, '^\p{L}:')">
        <xsl:sequence select="concat('file:///', $uri)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$uri"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:function name="tr:resolve-uri-from-system" as="xs:string">
    <xsl:param name="systempath" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="matches($systempath, '^file:/+')">
        <xsl:sequence select="replace($systempath, '^file:/+(([a-z]:)/)?', '$2/', 'i')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$systempath"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

</xsl:stylesheet>
