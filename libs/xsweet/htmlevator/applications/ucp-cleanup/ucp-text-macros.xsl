<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY lsquo "&#x2018;" >
<!ENTITY rsquo "&#x2019;" >
<!ENTITY ldquo "&#x201c;" >
<!ENTITY rdquo "&#x201d;" >

]>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="#all">

  <!-- Note the default namespace for matching (given above) is
     "http://www.w3.org/1999/xhtml" -->

  <!-- The results will have XML syntax but no XML declaration or DOCTYPE declaration
     (as permitted by HTML5). -->

  <!-- XSweet: provides a "bridge filter" for final tuning of HTML contents; a generalized sub-editorial preprocessor supporting string replacement. [3] -->
  <!-- Input:  HTML -->
  <!-- Output: A copy of the input, with text munging -->
  <!-- Limitation: Doesn't discriminate between ws that is "safe to munge" (eg paragraph content) and "significant" ws (eg code blocks or ASCII art): this will treat all text indiscriminately. -->
  
  <xsl:output method="xml" omit-xml-declaration="yes"/>

  <xsl:mode on-no-match="shallow-copy"/>

<!-- Available xsw: element semantics provide a series of text level substitutions.
    
    These are applied serially and their order of application can make a difference.
    
  xsw:match - matches a substring using a regular expression. A replacement string appears on @replace.
    It can have replacement expressions.
  xsw:match with @when-after or @when-next - qualifies the match so it only happens when the text preceding
    the matched node (in the same paragraph) or following it also matches a given regular expression.
    This helps matching across element boundaries.
    Note that in this case the replacement doesn't touch the substring matched in the preceding or following text.
  xsw:match-first and xsw:match-last
    Like xsw:match and working similarly, but only working on the first (leading) or last (trailing) bit of text.
  xsw:message Emit an XSLT runtime message (for debugging)
  xsw:munge-quotes - subsequence specifically for normalizing 'directional' quotation marks
  xsw:slide-punct - moves (some) punctuation around <em> element boundaries
  
  -->

  <!-- The replacements will be made in order so earlier matches take precedence. -->
  
  <xsl:character-map name="forTesting">
    <xsl:output-character character="&#x9;" string="TAB"/>
    <xsl:output-character character="&#x200a;" string="HAIRSPACE"/>
  </xsl:character-map>
  
  <!-- $space is (plain) space and LF, no tab -->
  <xsl:variable as="xs:string" name="space">[&#x20;&#xA;]</xsl:variable>
  
  <xsl:variable name="operations" expand-text="yes" as="element(xsw:sequence)">
    <sequence xmlns="http://coko.foundation/xsweet">
      <!-- Two adjacent hyphens become an em dash: "\-\-" (escaped in regex) becomes "—" -->
      <match replace="&#x2014;">\-\-</match>

      <!-- An en dash surround on both sides by spaces should be converted to an em dash: " – " -> " — "-->
      <splice replace="&#x2014;">
        <prae>\s+</prae>
        <quid>&#x2013;</quid>
        <post>\s+</post>
      </splice>
      <!-- nb using 'match', the padding ws is removed -->
      <!--<xsw:match replace="&#x2014;">\s+&#x2013;\s+</xsw:match>-->
      
      <!-- Spaces touching tabs should be removed -->
      <!-- (Runs of tabs might remain where they were mixed with spaces.) -->
      <match replace="&#x9;">{$space}*&#x9;+{$space}*</match>

      <!-- Replace runs of multiple consecutive tabs with just one tab-->
      <match replace="&#x9;">&#x9;+</match>
      
      <!-- Equal signs should be surrounded on either side by one and only one space: " = "-->
      <!-- (First we pad, then we remove extra.)-->
      <match replace=" = ">=</match>
      <match replace=" = ">\s+=\s+</match>
      
      <!-- Remove spaces at the very beginning and ends of ps-->
      <!-- 'match-first' is a no-op except for at beginnings of ps -->
      <match-first>^{$space}+</match-first>
      <!-- (Doesn't strip tabs) -->

      <!-- Remove tabs that end a paragraph (not ones that start)-->
      <!-- 'match-last' is a no-op except for at ends of ps -->
      <match-last>\s+$</match-last>
      <!-- (Removes all spaces including tabs)    -->

      <!-- replace three dots with horizontal ellipsis -->
      <match replace="&#x2026;">\.\.\.</match>

      <!-- replace hyphen+digit with en dash + digit -->
      <!--<xsw:match replace="&#x2013;$1">-(\d)</xsw:match>-->
      <!-- (splice does the same thing as plain match, except works across element boundaries) -->
      <splice replace="&#x2013;">
          <!--<xsw:prae>x</xsw:prae>-->
          <quid>\-</quid>
          <post>\d</post>
      </splice>
      
      <!-- Replace runs of multiple consecutive spaces with just one space-->
      <match replace="&#x20;">{$space}+</match>
      
      <!-- omit whitespace around em dashes -->
      <match replace="&#x2014;">\s*&#x2014;\s*</match>
      
      <!--  Handling initial letters - inserting nbsp for runs of spaces between punctuated capitals ...   -->
      <splice replace="&#xA0;">
        <prae>[A-Z]\.</prae>
        <quid>\s*</quid>
        <post>[A-Z]\.</post>
      </splice>
      <!-- ... We have to do this twice! to handle multiple runs of initials. -->
      <splice replace="&#xA0;">
        <prae>[A-Z]\.</prae>
        <quid>\s*</quid>
        <post>[A-Z]\.</post>
      </splice>
      
      <match replace="B.C.E.">B\.&#xA0;C\.&#xA0;E\.</match>
      <match replace="A.C.E.">A\.&#xA0;C\.&#xA0;E\.</match>
      <match replace="U.S.">U\.&#xA0;S\.</match>
      <match replace="D.C.">D\.&#xA0;C\.</match>
      <match replace="A.M.">A\.&#xA0;M\.</match>
      <match replace="P.M.">P\.&#xA0;M\.</match>
      <match replace="A.D.">A\.&#xA0;D\.</match>
      <match replace="B.C.">B\.&#xA0;C\.</match>

      <!-- subsequence to perform all quotation mark munging -->
      <munge-quotes/>
      
      
      <!-- Punctuation-related cleanup - spaces before certain punctuation signs -    -->
      <match replace="$1">\s+([,;:!\}}\?\)\]\.])</match>
      
      <!-- slide punct moves punctuation around element boundaries -->
      <slide-punct/>
    </sequence>
  </xsl:variable>
  
  <xsl:template match="h4 | h5 | h6">
    <h3>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </h3>
  </xsl:template>
  
  <xsl:template match="body//text()">
    <xsl:apply-templates select="$operations">
      <xsl:with-param name="original" select="."/>
    </xsl:apply-templates>
  </xsl:template>
  
  <!--<xsl:template match="body//text()">
    <text>
      <i><xsl:value-of select="."/></i>
      <o>
        <xsl:apply-templates select="$operations">
          <xsl:with-param name="original" select="."/>
        </xsl:apply-templates>
        
      </o>
    </text>
  </xsl:template>-->
  
  <!-- passing through ws-only -->
  <xsl:template match="text()[not(matches(.,'\S'))]" priority="10">
    <xsl:value-of select="."/>
  </xsl:template>
  
  <xsl:template match="xsw:sequence" as="xs:string">
    <xsl:param name="original" as="text()"/>
    <xsl:param name="starting" as="xs:string" select="string($original)"/>
    <xsl:iterate select="*">
      <xsl:param name="original" select="$original" as="text()"/>
      <xsl:param name="str"      select="$starting" as="xs:string"/>
      <xsl:on-completion select="$str"/>
      <xsl:next-iteration>
        <xsl:with-param name="original" select="$original"/>
        <xsl:with-param name="str">
          <xsl:apply-templates select=".">
            <xsl:with-param name="original" select="$original"/>
            <xsl:with-param name="str"      select="$str"/>
          </xsl:apply-templates>
        </xsl:with-param>
      </xsl:next-iteration>
    </xsl:iterate>
  </xsl:template>
  
  <xsl:template match="xsw:match">
    <xsl:param name="str" required="yes" as="xs:string"/>
    <!-- permits empty xsw:match as a no-op -->
    <xsl:choose>
      <xsl:when test="matches(.,'\S')">
        <xsl:sequence select="replace($str,string(.),(@replace,'')[1], 's')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$str"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="xsw:match[exists(@when-next)]">
    <xsl:param name="original" required="yes" as="text()"/>
    <xsl:param name="str" required="yes" as="xs:string"/>
    <xsl:variable name="regex" select=". || '$'"/>
    <xsl:variable name="after" select="string-join($original/xsw:container(.)/descendant::text()[. >> $original],'')"/>
    <xsl:variable name="after-regex" select="'^' || @when-next"/>

    <xsl:choose>
      <xsl:when test="matches($after,$after-regex)">
        <!--<xsl:sequence select="$str"/> ... <xsl:sequence select="$regex"/>-->
        <!--<xsl:sequence select="string(@replace)"/>-->
        <xsl:sequence select="replace($str,$regex,(@replace,'')[1], 's')"/>    
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$str"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="xsw:match[exists(@when-after)]">
    <xsl:param name="original" required="yes" as="text()"/>
    <xsl:param name="str" required="yes" as="xs:string"/>
    <xsl:variable name="regex" select="'^' || ."/>
    <xsl:variable name="fore" select="string-join($original/xsw:container(.)/descendant::text()[. &lt;&lt; $original],'')"/>
    <xsl:variable name="fore-regex" select="@when-after || '$'"/>
    
    <xsl:choose>
      <xsl:when test="matches($fore,$fore-regex)">
        <!--<xsl:sequence select="$str"/> ... <xsl:sequence select="$regex"/>-->
        <!--<xsl:sequence select="string(@replace)"/>-->
        <xsl:sequence select="replace($str,$regex,(@replace,'')[1], 's')"/>    
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$str"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template priority="5" match="xsw:match[exists(@when-next)][exists(@when-after)]">
    <xsl:param name="original" required="yes" as="text()"/>
    <xsl:param name="str" required="yes" as="xs:string"/>
    <xsl:variable name="regex" select="'^' || . || '$'"/>
    <xsl:variable name="fore" select="string-join($original/xsw:container(.)/descendant::text()[. &lt;&lt; $original],'')"/>
    <xsl:variable name="fore-regex" select="@when-after || '$'"/>
    <xsl:variable name="after" select="string-join($original/xsw:container(.)/descendant::text()[. >> $original],'')"/>
    <xsl:variable name="after-regex" select="'^' || @when-next"/>
    
    <xsl:choose>
      <xsl:when test="matches($fore,$fore-regex) and matches($after,$after-regex)">
        <!--<xsl:sequence select="$str"/> ... <xsl:sequence select="$regex"/>-->
        <!--<xsl:sequence select="string(@replace)"/>-->
        <xsl:sequence select="replace($str,$regex,(@replace,'')[1], 's')"/>    
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$str"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!--Splice is a generalized match that looks across element boundaries. Its prae and post element children are optional. -->
  
  <xsl:template match="xsw:splice" xpath-default-namespace="http://coko.foundation/xsweet">
    <!-- Inside this template, path 'prae' amounts to 'xsw:prae'   -->
    <xsl:param name="original" required="yes" as="text()"/>
    <xsl:param name="str" required="yes" as="xs:string"/>
    
    <xsl:variable name="ahead" select="string-join($original/xsw:container(.)/descendant::text()[. &lt;&lt; $original],'')"/>
    <xsl:variable name="after" select="string-join($original/xsw:container(.)/descendant::text()[. &gt;&gt; $original],'')"/>
    
    <xsl:variable name="replacement" select="@replace[matches(.,'\S')]/string(.)"/>
    <xsl:variable name="full" select="('(' || prae || ')' || quid || '('  || post || ')' )"/>
    <xsl:variable name="full-replace" select="('$1' || $replacement || '$2')"/>
    
    <!-- four ways this can happen:
       all: prae+quid+post matches $str - replace prae+quid+post with prae+@replace+post
       fore: prae$ matches $before and ^quid+post matches $str - replace ^quid+post with @replace+post
       aft: ^post matches $after and prae+quid$ matches $str - replace prae+quid$ with prae+@replace
       solo: prae$ matches $before, ^post matches $after, and ^quid$ matches $str - replace ^quid$ with @replace      
     otherwise return $str
      
    -->
    
    <xsl:choose>
      <!-- pre-empting when not matching quid at all ... we can skip the fancy stuff -->
      <xsl:when test="not(matches($str, quid))">
        <xsl:sequence select="$str"/>
      </xsl:when>
      <xsl:when test="matches($ahead, (prae || '$')) and matches($after, ('^' || post))">
        <xsl:sequence
          select=" replace($str,( '(' || prae || ')' || quid || '$'), ('$1' || $replacement ), 's')
          => replace(     ('^' || quid || '(' || post || ')'), ($replacement || '$1' ), 's')
          => replace(     $full, $full-replace, 's')"
        />
      </xsl:when>
      <xsl:when test="matches($ahead, (prae || '$'))">
        <xsl:sequence
          select=" replace($str,('^' || quid || '(' || post || ')'),($replacement || '$1' ), 's')
          => replace(     $full, $full-replace, 's')"
        />
      </xsl:when>
      <xsl:when test="matches($after, ('^' || post))">
        <xsl:sequence
          select=" replace($str,( '(' || prae || ')' || quid || '$'),('$1' || $replacement), 's')
          => replace(     $full, $full-replace, 's')"
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="replace($str, $full, $full-replace, 's')"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
    
  <xsl:template match="xsw:match-first">
    <xsl:param name="original" required="yes" as="text()"/>
    <xsl:param name="str"      required="yes" as="xs:string"/>
    <xsl:variable name="where" select="$original/xsw:container(.)"/>
    <xsl:choose>
      <xsl:when test="$original is $where/descendant::text()[1]">
        <xsl:sequence select="replace($str,string(.),(@replace,'')[1], 's')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$str"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="xsw:match-last">
    <xsl:param name="original" required="yes" as="text()"/>
    <xsl:param name="str"      required="yes" as="xs:string"/>
    <xsl:variable name="where" select="$original/xsw:container(.)"/>
    <xsl:choose>
      <xsl:when test="$original is $where/descendant::text()[last()]">
        <xsl:sequence select="replace($str,string(.),(@replace,'')[1], 's')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$str"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:variable name="quot"     as="xs:string">"</xsl:variable>
  <xsl:variable name="apos"     as="xs:string">'</xsl:variable>
  <!--<xsl:variable name="quotapos" as="xs:string">['"]</xsl:variable>-->
  
  <xsl:variable name="quote-operations" as="element(xsw:sequence)" expand-text="true">
    <xsw:sequence>
      
      <!-- Alex's sequence - first, reduce all left- and right-facing quotations marks with their 'straight' analog
          
          u2018 and u2019 -> u0027
          u201c and u201d -> u0022
          also ` and `` to their respective u0027 and u0022
-->
      <xsw:match replace="{$apos}" >[&lsquo;&rsquo;]</xsw:match>
      <xsw:match replace="{$quot}" >[&ldquo;&rdquo;]</xsw:match>
      <xsw:match replace="{$quot}" >``</xsw:match>
      <xsw:match replace="{$apos}" >`</xsw:match>
      
      <!--           
     Then apply heuristics to map back again -
     
     apostrophe+alphabetical character (u0027+letter) -> left single quotation mark (u2018+letter)
     alphabetical character+apostrophe (letter+u0027( -> alphabetical character+right single quotation mark (letter+u2019)
     quotation mark+alphabetical character (u0022+letter) -> left double quotation mark+alphabetical character (u201c+letter)
     alphabetical character+quotation mark (letter+u0022) -> alphabetical character+right double quotation mark (letter+u201d)
          
          -->
      
      <!-- $livechar is any character except a space or quotation mark (open, close or straight) -->
      <xsl:variable name="livechar">[^\s\p{Ps}\p{Pe}"']</xsl:variable>
      <xsl:variable name="singles">['&rsquo;&lsquo;]</xsl:variable>
      <xsl:variable name="doubles">["&rdquo;&ldquo;]</xsl:variable>
      
      <xsw:match replace="&ldquo;$1">"({$livechar})</xsw:match>
      <xsw:match replace="$1&rdquo;">({$livechar})["&ldquo;]</xsw:match>

      <xsw:match replace="$1&rsquo;">({$livechar})'</xsw:match>
      <xsw:match replace="&lsquo;$1">'({$livechar})</xsw:match>
      
      <!--  now the combinations   -->
      <xsw:match replace="&ldquo;$1">{$doubles}([&lsquo;&ldquo;])</xsw:match>
      <xsw:match replace="$1&rdquo;">([&rsquo;&rdquo;]){$doubles}</xsw:match>
      
      <!-- Inserting hair spaces between *certain* pairings now     -->
      <xsw:match replace="&ldquo;&#x200a;&ldquo;">&ldquo;&ldquo;</xsw:match>
      <xsw:match replace="&ldquo;&#x200a;&lsquo;">&ldquo;&lsquo;</xsw:match>
      <xsw:match replace="&lsquo;&#x200a;&ldquo;">&lsquo;&ldquo;</xsw:match>
      <xsw:match replace="&lsquo;&#x200a;&lsquo;">&lsquo;&lsquo;</xsw:match>
      <xsw:match replace="&rsquo;&#x200a;&rdquo;">&rsquo;&rdquo;</xsw:match>
      <xsw:match replace="&rsquo;&#x200a;&rdquo;">&rsquo;&rsquo;</xsw:match>
      <xsw:match replace="&rdquo;&#x200a;&rsquo;">&rdquo;&rdquo;</xsw:match>
      <xsw:match replace="&rdquo;&#x200a;&rsquo;">&rdquo;&rsquo;</xsw:match>
      
      <!-- brute s/r (from spec)
            
            em dash+right double quote (u2014+u201d) -> em dash+left double quote (u2014+u201c)
            left double quote+em dash (u201c+u2014)-> right double quote+em dash (u201d+u2014)
            " 'em" or " ‘em" (space+u0027+"em" or space+u2019+"em") -> " ’em" (space+u2019+"em")
            "'n'" or "'n'" (u0027+"n"+u0027 or u2018+"n"+u2018) -> "’n’" (u2019+"n"+u2019)"
            " 'tis" (space+u0027+"tis" or space+u2018+"tis") -> " ’tis" (space+u2019+"tis")-->
      <xsw:match replace="&rsquo;$1">{$singles}(\d)</xsw:match>               
      <xsw:match replace="&rsquo;em">{$singles}em</xsw:match>          
      <xsw:match replace="&rsquo;n&rsquo;">{$singles}n{$singles}</xsw:match>          
      <xsw:match replace="&rsquo;$1">{$singles}([Tt](wa|i)s\s)</xsw:match>          
      
    </xsw:sequence>
  </xsl:variable>
  
  <xsl:template match="xsw:munge-quotes">
    <xsl:param name="original" required="yes" as="text()"/>
    <xsl:param name="str"      required="yes" as="xs:string"/>
    
    <xsl:apply-templates select="$quote-operations">
      <xsl:with-param name="starting" select="$str"/>
      <xsl:with-param name="original" select="$original"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <!--<xsl:variable name="puncts">[,\.:;\?!]</xsl:variable>
  <xsl:variable name="not-puncts">[^,\.:;\?!]</xsl:variable>-->
  
<!-- This utility performs a "punctuation slide" around 'em' element boundaries - any punctuation given in $puncts
     is removed from text directly following emphasis, and into text inside emphasis when it appears directly after it. -->
  
  <xsl:template match="xsw:slide-punct">
    <xsl:param name="original" required="yes" as="text()"/>
    <xsl:param name="str"      required="yes" as="xs:string"/>
    
    <xsl:variable name="prev" select="$original/preceding::text()[1][xsw:container(.) is xsw:container($original)]"/>
    <xsl:variable name="next" select="$original/following::text()[1][xsw:container(.) is xsw:container($original)]"/>
    
    <!-- The operation to perform on this text node, if any, depends on what it is  -->
    <xsl:variable name="replacement" expand-text="true">
      <xsl:variable name="emphasized"      as="xs:boolean"
        select="exists($original/(ancestor::em|ancestor::b|ancestor::i|ancestor::u))"/>
      <xsl:variable name="prev-emphasized" as="xs:boolean"
        select="exists($prev/    (ancestor::em|ancestor::b|ancestor::i|ancestor::u))"/>
      <xsl:variable name="next-emphasized" as="xs:boolean"
        select="exists($next/    (ancestor::em|ancestor::b|ancestor::i|ancestor::u))"/>
      <xsl:choose>
        <!-- if following emphasis but not emphasized, we remove initial punctuation...   -->
        <xsl:when test="not($emphasized) and $prev-emphasized">
          <xsw:match>^\p{{P}}</xsw:match>
        </xsl:when>
        <!-- if emphasized but the next bit of text is not emphasized, we acquire its leading punctuation  -->
        <xsl:when test="$emphasized and not($next-emphasized)">
          <xsl:variable name="p" select="replace($next, '\P{P}.*$', '', 's')"/>
          <xsw:value-of>{ $str }{ $p }</xsw:value-of>
        </xsl:when>
        <xsl:otherwise>
          <!-- without the dummy match, we will drop the content, so we include it - -->
          <xsw:match/>     
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <xsl:apply-templates select="$replacement">
      <xsl:with-param name="original" select="$original"/>
      <xsl:with-param name="str" select="$str"/>
    </xsl:apply-templates>
    
  </xsl:template>
  
  <xsl:template match="xsw:*">
    <xsl:param name="str" required="yes" as="xs:string"/>
    <xsl:sequence select="$str"/>
  </xsl:template>
  
  <xsl:template match="xsw:value-of">
    <xsl:sequence select="string(.)"/>
  </xsl:template>
  
  <xsl:template match="xsw:message">
    <xsl:param name="str" required="yes" as="xs:string"/>
    <xsl:message>
      <xsl:value-of select="."/>
    </xsl:message>
  </xsl:template>
  
  <!-- returns the closest ancestor p or p-like (block) object -->
  <xsl:function name="xsw:container" as="element()?">
    <xsl:param name="who" as="node()"/>
    <xsl:sequence select="$who/ancestor::*[self::p|self::h1|self::h2|self::h3|self::h4|self::h5|self::h6|self::div|self::pre|self::li][1]"/>
  </xsl:function>
</xsl:stylesheet>
