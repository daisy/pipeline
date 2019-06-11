<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:calstable="http://docs.oasis-open.org/ns/oasis-exchange/table" 
  version="2.0"
  exclude-result-prefixes="xs calstable">

  <!--  This stylesheet normalizes a CALS exchange model table using 
        an adaption of Andrew Welchs table normalization algorithm,
        http://andrewjwelch.com/code/xslt/table/table-normalization.html
        
        It provides the function calstable:normalize() that expects as 
        parameter an element with row childs (i.e., a tbody element).
        It then replaces colspanned and rowspanned cells with a 1×1 cell
        at its origin and empty 1×1 cells (that refer to their origin)
        for the covered range. 
        
        Usage:
        
        Import it from another stylesheet and call the function as follows:
        
        <xsl:template match="*[*:row]">
          <xsl:sequence select="calstable:normalize(.)"/>
        </xsl:template>
        
        Sample invocation:
        saxon -s:test/table.xml -xsl:example.xsl
        
        Please note that the colspecs won’t be modified by this function. It might
        be necessary though to add colnum attributes if they are missing. Currently,
        the function calstable:normalize-colnames() that build on this function
        does this colnum normalisation by itself. We should alter calstable:normalize()
        that it equally accepts a tgroup and then includes normalized colspecs.

        As a sample application, we’ve included calstable:check-normalized().
        It takes a normalized table as first and 'yes' or 'no' as second argument.
        If a normalized table has an irregular column or row count (i.e., if
        there are missing or extraneous cells in the input), it will generate
        a message (and terminate if the 2nd argument is 'yes').

        This approach differs from Andrew’s in the following details:
        
        – It is supplied as a function.
        
        – It acts on CALS exchange tables, both in a namespace or in no namespace.
        
        – It doesn’t repeat the cell contents in the newly generated cells.
          
          It rather generates empty cells whose @calstable:rid attributes
          refer to the original cell’s @calstable:id attribute.
          
          In addition, the original cell will retain the namest, nameend, morerows,
          spanname attributes, but transferred into the calstable namespace.
          
          In the common case of namespaced DocBook tables, there will be no 
          @calstable:* attributes. The newly generated cells will point to
          the original cell by means of an @xml:id / @linkend relationship.
          There are no remaining @calstable:* attributes in this case. 

        Authors:
          Martin Kraetke 
          Gerrit Imsieke
        
        $Id: normalize.xsl 2803 2014-12-11 16:09:07Z gimsieke $
        
        License:
        
        http://creativecommons.org/licenses/by-sa/2.0/
        
        to the extent permitted by the license under which Andrew published 
        the original code (we were unable to determine any).

  -->

  <xsl:function name="calstable:normalize" as="element(*)">
    <!-- tbody or tgroup in a namespace or in no namespace. We accept tbody for backwards compatibility -->
    <xsl:param name="tbody-or-tgroup" as="element()"/>
    <xsl:variable name="tgroup" as="element(*)" select="$tbody-or-tgroup/(. | ..)/self::*:tgroup"/>
    <xsl:choose>
      <xsl:when test="exists($tgroup/*:colspec)">
        <xsl:variable name="colspecs" as="document-node(element(calstable:colspecs))">
          <xsl:document>
            <calstable:colspecs>
              <xsl:apply-templates select="$tgroup/*:colspec[last()]" mode="calstable:colspec"/>
            </calstable:colspecs>
          </xsl:document>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$tbody-or-tgroup/self::*:tgroup">
            <xsl:for-each select="$tgroup">
              <xsl:copy>
                <xsl:copy-of select="@*, node() except (*:colspec | *:spanspec | *[*:row])"/>
                <xsl:apply-templates select="$colspecs/calstable:colspecs/*, *:spanspec" mode="calstable:initial" />
                <xsl:apply-templates select="*[*:row]" mode="calstable:initial">
                  <xsl:with-param name="colspecs" select="$colspecs" tunnel="yes"/>
                  <xsl:with-param name="spanspecs" select="$tgroup/*:spanspec" tunnel="yes"/>
                </xsl:apply-templates>
              </xsl:copy>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="$tbody-or-tgroup" mode="calstable:initial">
                <xsl:with-param name="colspecs" select="$colspecs" tunnel="yes"/>
                <xsl:with-param name="spanspecs" select="$tgroup/*:spanspec" tunnel="yes"/>
              </xsl:apply-templates>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message select="'calstable/xsl/normalize.xsl: No colspec in table with srcpath, id or first entry text ''', 
          (
            $tbody-or-tgroup/ancestor::*[@srcpath][1]/@srcpath/string(),
            $tbody-or-tgroup/(. | ..)/self::*:tgroup/../@*:id/string(),
            ($tbody-or-tgroup//*:row)[1]/*[1]/string()
          )[1],
          '''&#xa;Returning the argument to calstable:normalize() unchanged.'"/>
        <xsl:sequence select="$tbody-or-tgroup"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:function>

  <xsl:template match="*[*:row]" mode="calstable:initial">
    <xsl:param name="colspecs" as="document-node(element(calstable:colspecs))" tunnel="yes"/>
    <xsl:variable name="table_with_no_colspans" as="element(*)*">
      <!-- rows, in a namespace or not -->
      <xsl:apply-templates select="." mode="calstable:colspan"/>
    </xsl:variable>
    <xsl:variable name="table_with_no_rowspans" as="element(*)*">
      <!-- rows, in a namespace or not -->
      <xsl:apply-templates select="$table_with_no_colspans" mode="calstable:rowspan"/>
    </xsl:variable>
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="$table_with_no_rowspans" mode="calstable:final">
        <xsl:with-param name="colspec-doc" as="document-node(element(calstable:colspecs))" select="$colspecs" tunnel="yes"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>
  
  <!-- process embedded tables -->
  <xsl:template match="*:tgroup" mode="calstable:final" >
    <xsl:sequence select="calstable:normalize(.)"/>
  </xsl:template>

  <xsl:function name="calstable:check-normalized" as="element(*)">
    <!-- tbody or thead in a namespace or in no namespace -->
    <xsl:param name="normalized-tgroup-or-tbody" as="element(*)"/>
    <xsl:param name="terminate" as="xs:string"/>
    <!-- 'yes', 'no' -->
    <xsl:variable name="actual-row-lengths" as="xs:integer+"
      select="for $r in $normalized-tgroup-or-tbody/(*/*:row | *:row) return count($r/*)"/>
    <xsl:variable name="actual-col-lengths" as="xs:integer+"
      select="for $i in (1 to max($actual-row-lengths)) 
      return count($normalized-tgroup-or-tbody/(*/*:row | *:row)/*[position() = $i])"/>
    <xsl:variable name="actual-morerows"
      select="for $r in $normalized-tgroup-or-tbody/(*/*:row | *:row) 
              return (for $m in $r/*/@calstable:morerows 
                      return (if (count($r/following-sibling::*:row)-xs:integer($m) lt 0) 
                              then concat('(', count($m/parent::*:entry/preceding-sibling::*:entry)+1,',',count($r/preceding-sibling::*:row)+1, ')') 
                              else ()))"
      as="xs:string*"/>
    <xsl:variable name="irregular-row-lengths"
      select="count(distinct-values($actual-row-lengths)) ne 1" as="xs:boolean"/>
    <xsl:variable name="irregular-col-lengths"
      select="count(distinct-values($actual-col-lengths)) ne 1" as="xs:boolean"/>
    <xsl:if test="$irregular-row-lengths">
      <xsl:message terminate="{$terminate}">Irregular row lengths: <xsl:value-of
          select="$actual-row-lengths"/></xsl:message>
    </xsl:if>
    <xsl:if test="$irregular-col-lengths">
      <xsl:message terminate="{$terminate}">Irregular col lengths: <xsl:value-of
          select="$actual-col-lengths"/></xsl:message>
    </xsl:if>
    <xsl:if test="exists($actual-morerows)">
      <xsl:message terminate="{$terminate}">Too few rows (@morerows): <xsl:value-of
          select="$actual-morerows"/></xsl:message>
    </xsl:if>
    <xsl:sequence select="$normalized-tgroup-or-tbody"/>
  </xsl:function>

  <xsl:template match="@*|node()" mode="calstable:colspan calstable:rowspan calstable:final calstable:initial">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*|node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*:colspec" mode="calstable:colspec">
    <xsl:variable name="preceding" select="preceding-sibling::*[1]/self::*:colspec" as="element(*)?"/>
    <xsl:choose>
      <xsl:when test="exists($preceding)">
        <xsl:variable name="preceding-with-colnum" as="node()+">
          <xsl:apply-templates select="$preceding" mode="#current"/>
        </xsl:variable>
        <xsl:sequence select="$preceding-with-colnum"/>
        <xsl:sequence select="preceding-sibling::node()[. >> $preceding]"/><!-- retain text nodes, comments, PIs -->
        <xsl:copy>
          <xsl:variable name="colnum" as="xs:integer"
            select="xs:integer($preceding-with-colnum[last()]/@colnum) + 1"/>
          <xsl:attribute name="colnum" select="$colnum"/>
          <xsl:attribute name="colname" select="concat('__generated__', $colnum)"/>
          <xsl:copy-of select="@*"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="preceding-sibling::node()"/><!-- note that there may be no other *elements* than colspec here -->
        <xsl:copy>
          <xsl:attribute name="colnum" select="1"/>
          <xsl:attribute name="colname" select="'__generated__1'"/>
          <xsl:copy-of select="@*"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:function name="calstable:col-number" as="xs:integer">
    <xsl:param name="colname" as="xs:string"/>
    <xsl:param name="colspecs" as="document-node(element(calstable:colspecs))"/>
    <xsl:choose>
      <xsl:when test="exists(key('calstable:colspec-by-colname', $colname, $colspecs)/@colnum)">
        <xsl:sequence select="xs:integer(key('calstable:colspec-by-colname', $colname, $colspecs)/@colnum)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes"> There is no colspec for colname <xsl:value-of
            select="$colname"/> (<xsl:copy-of select="$colspecs"/>
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:function>

  <xsl:key name="calstable:colspec-by-colname" match="*:colspec" use="@colname"/>

  <xsl:template match="*:entry | *:entrytbl[not(ancestor::*:entry)]" mode="calstable:colspan">
    <xsl:param name="colspecs" as="document-node(element(calstable:colspecs))?" tunnel="yes"/>
    <xsl:param name="spanspecs" as="element(*)*" tunnel="yes"/>
    <xsl:variable name="namest" as="xs:string?"
      select="if (@spanname) then $spanspecs[@spanname = current()/@spanname]/@namest else @namest"/>
    <xsl:variable name="nameend" as="xs:string?"
      select="if (@spanname) then $spanspecs[@spanname = current()/@spanname]/@nameend else @nameend"/>
    <xsl:variable name="colspan"
      select="if($namest and $nameend)
              then calstable:col-number($nameend, $colspecs) - calstable:col-number($namest, $colspecs) + 1
              else 1"
      as="xs:integer"/>
    <xsl:variable name="id" as="xs:string" select="(@*:id, concat('calstable_', generate-id()))[1]"/>
    <xsl:choose>
      <xsl:when test="$namest and $nameend">
        <xsl:variable name="this" select="." as="element()*"/>
        <xsl:copy>
          <xsl:attribute name="calstable:id" select="$id"/>
          <xsl:attribute name="calstable:colspan" select="$colspan"/>
          <xsl:apply-templates select="@*, node()" mode="#current"/>
        </xsl:copy>
        <xsl:for-each select="2 to $colspan">
          <xsl:for-each select="$this">
            <!-- in XSLT 3, we'd use xsl:copy/@select -->
            <xsl:copy>
              <!-- need to xsl:copy the entry in order to stay namespace-agnostic -->
              <xsl:attribute name="calstable:rid" select="$id"/>
              <xsl:copy-of select="@morerows"/>
            </xsl:copy>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:when>
      <xsl:when test="@morerows">
        <xsl:copy>
          <xsl:attribute name="calstable:id" select="$id"/>
          <xsl:copy-of select="@*, node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@namest | @nameend | @spanname" mode="calstable:colspan">
    <xsl:attribute name="{concat('calstable:', name())}" select="."/>
  </xsl:template>

  <xsl:template match="*:tbody | *:thead | *:tfoot" mode="calstable:rowspan">
    <xsl:copy-of select="*:row[1]"/>
    <xsl:apply-templates select="*:row[2]" mode="calstable:rowspan">
      <xsl:with-param name="previousRow" select="*:row[1]"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="@calstable:id" mode="calstable:rowspan">
    <xsl:attribute name="calstable:rid" select="."/>
  </xsl:template>

  <xsl:template match="*:row" mode="calstable:rowspan">
    <xsl:param name="previousRow" as="element()"/>

    <xsl:variable name="currentRow" select="."/>

    <xsl:variable name="normalizedCells">
      <xsl:for-each select="$previousRow/*">
        <xsl:choose>
          <xsl:when test="@morerows &gt; 0">
            <xsl:copy>
              <xsl:attribute name="morerows">
                <xsl:value-of select="@morerows - 1"/>
              </xsl:attribute>
              <!-- below colspan-generated cell: -->
              <xsl:copy-of select="@calstable:rid"/>
              <!-- below origin cell: -->
              <xsl:apply-templates select="@calstable:id" mode="#current"/>
            </xsl:copy>
          </xsl:when>
          <xsl:otherwise>
            <xsl:copy-of
              select="$currentRow/*[1 + count(current()/preceding-sibling::*[not(@morerows) or (@morerows = 0)])]"
            />
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
      <!-- Extra cells will be appended at the end. Otherwise, the irregular row length wouldn’t be reported. -->
      <xsl:copy-of
        select="$currentRow/*[position() gt (count($previousRow/*) - count($previousRow/*/@morerows[. &gt; 0]))]"
      />
    </xsl:variable>

    <xsl:variable name="newRow" as="element()">
      <xsl:copy>
        <xsl:copy-of select="$currentRow/@*"/>
        <xsl:copy-of select="$normalizedCells"/>
      </xsl:copy>
    </xsl:variable>

    <xsl:copy-of select="$newRow"/>

    <xsl:apply-templates select="following-sibling::*:row[1]" mode="calstable:rowspan">
      <xsl:with-param name="previousRow" select="$newRow"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="@calstable:*[matches(namespace-uri(..), 'docbook')]" mode="calstable:final"/>

  <xsl:template match="@calstable:*[local-name() = ('namest', 'nameend', 'morerows', 'colname')]
                                   [matches(namespace-uri(..), 'docbook')]" 
                mode="calstable:final" priority="1">
    <xsl:attribute name="{local-name()}" select="."/>
  </xsl:template>

  <xsl:template match="*[not(matches(namespace-uri(), 'docbook'))]/@morerows" mode="calstable:final">
    <xsl:attribute name="calstable:morerows" select="."/>
  </xsl:template>

  <xsl:template match="@calstable:id[matches(namespace-uri(..), 'docbook')]" mode="calstable:final"
    priority="2">
    <xsl:attribute name="xml:id" select="."/>
  </xsl:template>

  <xsl:template match="@calstable:rid[matches(namespace-uri(..), 'docbook')]" mode="calstable:final"
    priority="2">
    <xsl:attribute name="linkend" select="."/>
  </xsl:template>
  
  <xsl:template match="*:entry[empty(@*:colname | @*:namest | @*:nameend)]" mode="calstable:final">
    <xsl:param name="colspec-doc" as="document-node(element(calstable:colspecs))" tunnel="yes"/>
    <xsl:copy copy-namespaces="no">
      <xsl:variable name="pos" select="index-of(../*/generate-id(), generate-id())"/>
      <xsl:attribute name="colname" select="$colspec-doc/*/*:colspec[position() = $pos]/@*:colname"/>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>


</xsl:stylesheet>