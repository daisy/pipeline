<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:cat="urn:oasis:names:tc:entity:xmlns:xml:catalog"
  xmlns:tr="http://transpect.io" 
  xmlns:s="http://purl.oclc.org/dsdl/schematron"
  xmlns:xso="xsloutputnamespace"
  xmlns="http://purl.oclc.org/dsdl/schematron"
  exclude-result-prefixes="xs cat"
  version="2.0">

  <xsl:import href="http://transpect.io/xslt-util/xslt-based-catalog-resolver/xsl/resolve-uri-by-catalog.xsl"/>
  <xsl:param name="cat:missing-next-catalogs-warning" as="xs:string" select="'no'"/>
  
  <xsl:output indent="yes"/>
	
	<xsl:namespace-alias stylesheet-prefix="xso" result-prefix="xsl"/>

  <xsl:param name="family" />
  <xsl:param name="series" />
  <xsl:param name="publisher" />
  <xsl:param name="s9y1" />
  <xsl:param name="s9y1-path" as="xs:string?"/>
  <xsl:param name="s9y2-path" as="xs:string?"/>
  <xsl:param name="s9y3-path" as="xs:string?"/>
  <xsl:param name="s9y4-path" as="xs:string?"/>
  <xsl:param name="s9y5-path" as="xs:string?"/>
  <xsl:param name="s9y6-path" as="xs:string?"/>
  <xsl:param name="s9y7-path" as="xs:string?"/>
  <xsl:param name="s9y8-path" as="xs:string?"/>
  <xsl:param name="s9y9-path" as="xs:string?"/>
  <xsl:param name="basename" />
  <xsl:param name="fallback-uri" />
  <xsl:param name="rule-category-span-class"/>

  <xsl:variable name="catalog" as="document-node(element(cat:catalog))?" select="collection()[cat:catalog]"/>
  
  <xsl:variable name="paths" as="xs:string*" 
    select="($s9y1-path, $s9y2-path, $s9y3-path, $s9y4-path, $s9y5-path, $s9y6-path, $s9y7-path, $s9y8-path, $s9y9-path)"/>
  
	<!-- prints a status message with the Id of the schematron report or assert when debug is set to yes -->
	<xsl:param name="schematron-rule-msg" select="'no'"/>
  <xsl:param name="debug" select="'no'"/>

  <xsl:function name="tr:family" as="xs:boolean">
    <xsl:param name="doc" as="document-node(element(s:schema))"/>
    <xsl:param name="fam" as="xs:string?"/>
    <xsl:sequence select="
      if ($fam)
      then (tokenize(document-uri($doc), '/')[last() - 1] = $fam)
      else true()
      "/>
  </xsl:function>

  <xsl:function name="tr:file-exists" as="xs:boolean">
    <xsl:param name="uri" as="xs:string"/>
    <xsl:sequence select="unparsed-text-available($uri)"/>
  </xsl:function>

  <xsl:function name="tr:schematron-collection" as="document-node(element(s:schema))*">
    <xsl:param name="paths" as="xs:string*"/>
    <xsl:param name="fam" as="xs:string"/>
    <xsl:for-each select="$paths">
      <xsl:variable name="url" select="concat(tr:resolve-uri-by-catalog(., $catalog), 'schematron/', $fam, '/', $fam, '.sch.xml')" as="xs:string"/>
      <xsl:sequence select="if (doc-available($url))
                            then doc($url) 
                            else ()"/>
    </xsl:for-each>
  </xsl:function>

  <xsl:variable name="schematrons" as="document-node(element(s:schema))*">
    <xsl:apply-templates select="if(not(tr:schematron-collection($paths, $family))) 
                                 then doc(tr:resolve-uri-by-catalog($fallback-uri, $catalog)) 
                                 else tr:schematron-collection($paths, $family)" mode="tr:expand-includes"/>
  </xsl:variable>

  <xsl:variable name="fallback-schematrons" as="document-node(element(s:schema))*">
    <xsl:if test="not($schematrons)
                  and
                  exists($fallback-uri)
                  and
                  not($fallback-uri = '') 
                  and
                  doc-available(resolve-uri($fallback-uri))">
      <xsl:variable name="includes-expanded-doc">
        <xsl:apply-templates select="doc(resolve-uri($fallback-uri))" mode="tr:expand-includes"/>
      </xsl:variable>
      <xsl:choose>
        <!-- handle 404 html documents and other -->
        <xsl:when test="not($includes-expanded-doc/*/self::s:schema)">
          <xsl:message select="'&#xa;&#xa;&#xa;!!! ERROR: fallback-uri is not a valid schematron document:&#xa;', $fallback-uri, '&#xa;&#xa;'" terminate="no"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="$includes-expanded-doc"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:variable>

  <xsl:template match="s:include | s:extends" mode="tr:expand-includes">
    <xsl:apply-templates select="doc(@href)/s:schema/*" mode="#current">
      <xsl:with-param name="is-included" select="true()" tunnel="yes"/>
      <xsl:with-param name="is-included-non-a9s" 
        select="if(matches(@href, 'http://this.transpect.io/a9s/')) then false() else true()" tunnel="yes"/>
      <xsl:with-param name="include-href" select="@href" tunnel="yes"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="/">
    <xsl:message select="concat('[info] Schematron family: ', $family)"/>
    <xsl:if test="not($schematrons/s:schema)">
      <xsl:message select="'[WARNING] No Schematron file and no fallback found!'"/>
    </xsl:if>
    <xsl:message select="'[info] assembled from these URIs:', string-join($schematrons/s:schema/base-uri(), '&#xa;')"/>
    <schema tr:rule-family="{$family}">
      <xsl:variable name="_lang" select="($schematrons/s:schema/@xml:lang)[1]" as="attribute(xml:lang)?"/>
      <xsl:sequence select="$_lang"/>
      <xsl:variable name="titles" as="element(s:title)*">
        <xsl:for-each-group select="$schematrons/s:schema/s:title" group-by="(@xml:lang, '')[1]">
          <title>
            <xsl:copy-of select="@xml:lang, node()"/>
          </title>
        </xsl:for-each-group>
      </xsl:variable>
      <xsl:variable name="_title" as="element(s:title)?" 
        select="($titles[@xml:lang = $_lang], $titles[not(@xml:lang)], $titles)[1]"/>
      <xsl:sequence select="$_title"/>
      <xsl:for-each-group select="$schematrons/s:schema/s:ns" group-by="@uri">
        <!-- Assumption: no two different prefixes for one uri. --> 
        <ns prefix="{@prefix}" uri="{current-grouping-key()}"/>
      </xsl:for-each-group>
      <xsl:for-each-group select="$schematrons/s:schema/xsl:include[not(matches(@href, 'shared-variables.xsl'))]|$schematrons/s:schema/xsl:import" group-by="@href">
        <xso:include href="{current-grouping-key()}"/>
      </xsl:for-each-group>
      <xsl:for-each-group select="$schematrons/s:schema/xsl:param" group-by="@name">
        <xsl:apply-templates select="tr:most-important-element(current-group())" mode="tr:assemble-schematron"/>
      </xsl:for-each-group>
      <xsl:apply-templates select="($schematrons/s:schema/xsl:include[matches(@href, 'shared-variables.xsl')])[1]"  mode="tr:assemble-schematron"/>
      <xsl:variable name="phases" select="$schematrons[1]/s:schema/s:phase" as="element(s:phase)*"/>
      <xsl:for-each-group select="$phases" group-by="@id">
        <phase id="{current-grouping-key()}">
          <xsl:processing-instruction name="origin" select="$phases[1]/base-uri()"/>
          <xsl:for-each-group select="current-group()/s:active" group-by="@pattern">
            <active pattern="{current-grouping-key()}"/>
          </xsl:for-each-group>
        </phase>
      </xsl:for-each-group>
      <xsl:for-each-group select="$schematrons/s:schema/s:let" group-by="@name">
        <xsl:apply-templates select="tr:most-important-element(current-group())" mode="tr:assemble-schematron"/>
      </xsl:for-each-group>
      <xsl:for-each-group select="$schematrons/s:schema/s:pattern" group-by="@id">
        <xsl:apply-templates select="tr:most-important-element(current-group())" mode="tr:assemble-schematron">
          <xsl:with-param name="title" select="$_title" tunnel="yes"/>
        </xsl:apply-templates>
      </xsl:for-each-group>
      <xsl:variable name="_diagnostics">
        <xsl:for-each-group select="$schematrons/s:schema/s:diagnostics/s:diagnostic" group-by="@id">
          <xsl:apply-templates select="tr:most-important-element(current-group())" mode="tr:assemble-schematron">
            <xsl:with-param name="title" select="$_title" tunnel="yes"/>
          </xsl:apply-templates>
        </xsl:for-each-group>
      </xsl:variable>
      <xsl:if test="$_diagnostics[descendant-or-self::*]">
        <diagnostics>
          <xsl:sequence select="$_diagnostics"/>
        </diagnostics>
      </xsl:if>
      <xsl:for-each-group select="$schematrons/s:schema/xsl:function" group-by="@name">
        <xsl:apply-templates select="tr:most-important-element(current-group())" mode="tr:assemble-schematron"/>
      </xsl:for-each-group>
      <xsl:for-each-group select="$schematrons/s:schema/xsl:variable" group-by="@name">
        <xsl:apply-templates select="tr:most-important-element(current-group())" mode="tr:assemble-schematron"/>
      </xsl:for-each-group>
      <xsl:for-each-group select="$schematrons/s:schema/xsl:key" group-by="@name">
        <xsl:apply-templates select="tr:most-important-element(current-group())" mode="tr:assemble-schematron"/>
      </xsl:for-each-group>
      <xsl:for-each-group select="$schematrons/s:schema/xsl:template[@name]" group-by="@name">
        <xsl:apply-templates select="tr:most-important-element(current-group())" mode="tr:assemble-schematron"/>
      </xsl:for-each-group>
      <xsl:apply-templates select="$schematrons/s:schema/xsl:template[@match]" mode="tr:assemble-schematron"/>
    </schema> 
  </xsl:template>
  
  <xsl:function name="tr:most-important-element" as="element()?">
    <xsl:param name="current-group" as="element()*"/>
    <xsl:sequence select="
      if($current-group[@is-included-non-a9s] and $current-group[not(@is-included-non-a9s)]) 
      then $current-group[not(@is-included-non-a9s)][1] 
      else $current-group[1]
      "/>
  </xsl:function>
  
  <xsl:template match="s:pattern | s:let" mode="tr:assemble-schematron">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*" mode="#current"/>
      <!-- The origin of the element, formerly as comment -->
      <xsl:processing-instruction name="origin" select="(@include-href, base-uri(.))[1]"/>
      <xsl:apply-templates mode="#current"/>
    </xsl:copy>
  </xsl:template>
	
	<xsl:template match="s:assert | s:report" mode="tr:assemble-schematron">
		<xsl:choose>
		  <xsl:when test="$schematron-rule-msg eq 'yes' and $debug eq 'yes'">
				<xso:message select="{concat('''', local-name(), ' ', if(@id) then @id else 'no @id found', '''')}"/>		
			</xsl:when>
		</xsl:choose>
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="#current"/>
		  <xsl:if test="not(exists(s:span[@class eq 'srcpath']))">
		    <span class="srcpath"><xso:value-of select="ancestor-or-self::*[@srcpath][1]/@srcpath"/></span>
		  </xsl:if>
		  <xsl:call-template name="default-category"/>
			<xsl:apply-templates mode="#current"/>
		</xsl:copy>
	</xsl:template>
  
  <xsl:template match="s:diagnostic" mode="tr:assemble-schematron">
		<xsl:copy>
			<xsl:apply-templates select="@*" mode="#current"/>
		  <xsl:call-template name="default-category"/>
			<xsl:apply-templates mode="#current"/>
		</xsl:copy>
	</xsl:template>
  
  <xsl:template name="default-category">
	  <xsl:param name="title" as="element(s:title)?" tunnel="yes"/>
    <xsl:if test="$rule-category-span-class and not(exists(s:span[@class = $rule-category-span-class]))">
      <span class="{$rule-category-span-class}">
        <xsl:copy-of select="$title/node()"/>
      </span>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="@role[. = 'warn']" mode="tr:assemble-schematron">
    <xsl:attribute name="{name()}" select="'warning'"/>
  </xsl:template>

  <xsl:template match="@role" mode="tr:assemble-schematron">
    <!-- 'Info' → 'info' -->
    <xsl:attribute name="{name()}" select="lower-case(.)"/>
  </xsl:template>


  <xsl:template match="@* | *" mode="tr:assemble-schematron">
    <xsl:copy copy-namespaces="no">
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@is-included" mode="tr:assemble-schematron"/>
  <xsl:template match="@is-included-non-a9s" mode="tr:assemble-schematron"/>
  <xsl:template match="@include-href" mode="tr:assemble-schematron"/>
  
  <xsl:template match="@* | *" mode="tr:expand-includes">
    <xsl:param name="is-included" select="false()" tunnel="yes"/>
    <xsl:param name="is-included-non-a9s" select="false()" tunnel="yes"/>
    <xsl:param name="include-href" tunnel="yes"/>
    <xsl:copy copy-namespaces="no">
      <xsl:if test="$is-included">
        <xsl:attribute name="is-included" select="'true'"/>
        <xsl:if test="$is-included-non-a9s">
          <xsl:attribute name="is-included-non-a9s" select="'true'"/>
        </xsl:if>
        <xsl:attribute name="include-href" select="$include-href"/>
      </xsl:if>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="/s:schema" mode="tr:expand-includes">
    <xsl:document>
      <xsl:copy>
        <xsl:attribute name="xml:base" select="base-uri()"/>
        <xsl:apply-templates select="@*, node()" mode="#current"/>
      </xsl:copy>  
    </xsl:document>
  </xsl:template>
  
</xsl:stylesheet>
