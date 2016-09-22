<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:fileset-add-entry" name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
  xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="px">

  <p:input port="source"/>
  <p:output port="result"/>

  <p:option name="href" required="true"/>
  <p:option name="media-type" select="''"/>
  <p:option name="ref" select="''"><!-- if relative; will be resolved relative to the file --></p:option>
  <p:option name="original-href" select="''"><!-- if relative; will be resolved relative to the file --></p:option>
  <p:option name="first" select="'false'"/>

  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
  <p:import href="fileset-add-ref.xpl"/>

  <p:variable name="fileset-base" select="/*/@xml:base"/>

  <p:choose name="check-base">
    <!-- TODO: replace by uri-utils 'is-relative' function (depending on how that impacts performance) -->
    <p:when test="not(/*/@xml:base) and not(matches($href,'^[^/]+:')) and not(starts-with($href,'/'))">
      <px:message severity="WARN" message="Adding a relative resource to a file set with no base URI"/>
    </p:when>
    <p:otherwise>
      <p:identity/>
    </p:otherwise>
  </p:choose>

  <!--Create the new d:file entry-->
  <p:add-attribute match="/*" attribute-name="xml:base">
    <p:input port="source">
      <p:inline>
        <d:file/>
      </p:inline>
    </p:input>
    <p:with-option name="attribute-value" select="base-uri(/*)"/>
  </p:add-attribute>
  <p:choose>
    <p:when test="$fileset-base">
      <p:identity/>
    </p:when>
    <p:otherwise>
      <p:delete match="/*/@xml:base"/>
    </p:otherwise>
  </p:choose>
  <p:add-attribute match="/*" attribute-name="media-type">
    <p:with-option name="attribute-value" select="$media-type"/>
  </p:add-attribute>
  <p:add-attribute match="/*" attribute-name="href">
    <p:with-option name="attribute-value" select="if (starts-with($href, $fileset-base) and ends-with($fileset-base,'/')) then substring-after($href, $fileset-base) else $href"/>
  </p:add-attribute>
  <p:add-attribute match="/*" attribute-name="original-href">
    <p:with-option name="attribute-value" select="if ($original-href and $fileset-base) then resolve-uri($original-href, $fileset-base) else ''"/>
  </p:add-attribute>
  <p:delete match="@media-type[not(normalize-space())]"/>
  <p:delete match="@original-href[not(normalize-space())]"/>
  <p:choose>
    <p:when
      test="starts-with(/*/@href,'/') or contains(substring-before(/*/@href,'/'),':') or contains(/*/@href,'/.') or contains(/*/@href,'//') or string-length(translate(/*/@href,'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_./%():','')) &gt; 0 or contains(/*/@href,'%') and count(tokenize(/*/@href,'%')[not(starts-with(.,'20'))]) = 0
      or starts-with(/*/@original-href,'/') or contains(substring-before(/*/@original-href,'/'),':') or contains(/*/@original-href,'/.') or contains(/*/@original-href,'//') or string-length(translate(/*/@original-href,'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_./%():','')) &gt; 0 or contains(/*/@original-href,'%') and count(tokenize(/*/@original-href,'%')[not(starts-with(.,'20'))]) = 0">
      <!-- URI probably needs normalization -->
      <px:message severity="DEBUG" message="URI normalization: $1">
        <p:with-option name="param1"
          select="string-join((concat('href=&quot;',/*/@href,'&quot;'),if (/*/@original-href) then concat('original-href=&quot;',/*/@original-href,'&quot;') else (),if (/*/@original-href!=$original-href or /*/@href!=$href) then concat('xml:base=&quot;',$fileset-base,'&quot;') else ()),' ')"
        />
      </px:message>
      <p:xslt>
        <p:input port="parameters">
          <p:empty/>
        </p:input>
        <p:input port="stylesheet">
          <p:document href="../xslt/file-normalize.xsl"/>
        </p:input>
      </p:xslt>
    </p:when>
    <p:otherwise>
      <!-- skip URI normalization, it seems not to be necessary -->
      <p:identity/>
    </p:otherwise>
  </p:choose>
  <p:delete match="/*/@xml:base"/>
  <p:identity name="new-entry"/>

  <!-- Insert the entry as the last or first child of the file set - unless it already exists -->
  <p:group>
    <p:variable name="href-normalized" select="/*/@href">
      <p:pipe port="result" step="new-entry"/>
    </p:variable>

    <p:identity>
      <p:input port="source">
        <p:pipe port="source" step="main"/>
      </p:input>
    </p:identity>
    <p:choose>
      <p:when test="/*/d:file[@href=$href-normalized]">
        <p:identity/>
      </p:when>
      <p:otherwise>
        <p:insert match="/*">
          <p:input port="insertion">
            <p:pipe port="result" step="new-entry"/>
          </p:input>
          <p:with-option name="position" select="if ($first='true') then 'first-child' else 'last-child'"/>
        </p:insert>
      </p:otherwise>
    </p:choose>

    <p:choose>
      <p:when test="$ref=''">
        <p:identity/>
      </p:when>
      <p:otherwise>
        <px:fileset-add-ref>
          <p:with-option name="href" select="$href-normalized"/>
          <p:with-option name="ref" select="$ref"/>
        </px:fileset-add-ref>
      </p:otherwise>
    </p:choose>
  </p:group>

</p:declare-step>
