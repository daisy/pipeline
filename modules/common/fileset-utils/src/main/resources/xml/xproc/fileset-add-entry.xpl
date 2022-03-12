<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-inline-prefixes="px"
                type="px:fileset-add-entry" name="main">

  <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    <p>Add a new entry to a fileset.</p>
  </p:documentation>

  <p:input port="source" primary="true">
    <p:inline exclude-inline-prefixes="#all"><d:fileset/></p:inline>
  </p:input>
  <p:input port="source.in-memory" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The input fileset</p>
    </p:documentation>
    <p:empty/>
  </p:input>

  <p:output port="result" primary="true"/>
  <p:output port="result.in-memory" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The fileset with the new entry added</p>
      <p>The result.in-memory port contains all the documents from the source.in-memory port, and if
      the new entry was provided via de "entry" port, that document is appended (or prepended,
      depending on the "first" option).</p>
      <p>If the input fileset already contained a file with the same URI as the new entry, it is not
      added, unless when the 'replace' option is set, in which case the old entry is removed and the
      new one is added to the beginning or the end. If the 'replace-attributes' option is set,
      attributes of the existing entry may be added or replaced.</p>
    </p:documentation>
    <p:pipe step="result" port="in-memory"/>
  </p:output>

  <p:input port="entry" sequence="true">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The document to add to the fileset (at most one)</p>
      <p>Must be empty if the href option is specified.</p>
    </p:documentation>
    <p:empty/>
  </p:input>

  <p:option name="href" select="''">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>The URI of the new entry</p>
      <p>If the entry is provided via a document on the entry port, the href option must not be
      specified. In this case the entry gets the base URI of the document.</p>
    </p:documentation>
  </p:option>

  <p:option name="media-type" select="''"/>
  <p:option name="original-href" select="''"><!-- if relative; will be resolved relative to the file --></p:option>
  <p:option name="first" cx:as="xs:boolean" select="false()"/>
  <p:option name="replace" cx:as="xs:boolean" select="false()"/>
  <p:option name="replace-attributes" cx:as="xs:boolean" select="false()"/>

  <p:input port="file-attributes" kind="parameter" primary="false">
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
      <p>Additional attributes to add to the new d:file entry, or to the existing entry if the
      fileset already contained a file with the same URI as the provided entry and if
      <code>replace-attributes</code> is set to <code>true</code>.</p>
      <p>Attributes named <code>href</code>, <code>original-href</code> or <code>media-type</code>
      are not allowed.</p>
    </p:documentation>
    <p:inline>
      <c:param-set/>
    </p:inline>
  </p:input>

  <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
    <p:documentation>
      px:assert
      px:message
    </p:documentation>
  </p:import>
  <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
    <p:documentation>
      px:set-base-uri
    </p:documentation>
  </p:import>
  <cx:import href="http://www.daisy.org/pipeline/modules/file-utils/uri-functions.xsl" type="application/xslt+xml">
    <p:documentation>
      pf:is-relative
      pf:relativize-uri
      pf:normalize-uri
    </p:documentation>
  </cx:import>

  <p:variable name="fileset-base" cx:as="xs:string" select="pf:normalize-uri(base-uri(/*))"/>
  <p:variable name="fileset-xml-base" cx:as="xs:string" select="/*/@xml:base/pf:normalize-uri(.)"/>

  <px:assert message="Expected $1 on the entry port" error-code="XXXXX">
    <p:input port="source">
      <p:pipe step="main" port="entry"/>
    </p:input>
    <p:with-option name="test-count-min" select="if ($href='') then '1' else '0'"/>
    <p:with-option name="test-count-max" select="if ($href='') then '1' else '0'"/>
    <p:with-option name="param1"         select="if ($href='') then '1 document' else '0 documents'"/>
  </px:assert>

  <p:group>
  <p:variable name="file-base" select="pf:normalize-uri(if ($href='') then base-uri(/*) else $href)"/>

  <p:identity>
    <p:input port="source">
      <p:pipe step="main" port="source"/>
    </p:input>
  </p:identity>
  <p:choose name="check-base">
    <p:when test="$fileset-xml-base='' and pf:is-relative($file-base)">
      <px:message severity="WARN" message="Adding a relative resource to a file set with no base directory"/>
    </p:when>
    <p:otherwise>
      <p:identity/>
    </p:otherwise>
  </p:choose>

  <!--
      Create the new d:file entry
  -->
  <px:set-base-uri>
    <p:input port="source">
      <p:inline>
        <d:file/>
      </p:inline>
    </p:input>
    <p:with-option name="base-uri" select="base-uri(/*)"/>
  </px:set-base-uri>
  <p:choose>
    <p:when test="not($media-type='')">
      <p:add-attribute match="/*" attribute-name="media-type">
        <p:with-option name="attribute-value" select="$media-type"/>
      </p:add-attribute>
    </p:when>
    <p:otherwise>
      <p:identity/>
    </p:otherwise>
  </p:choose>
  <p:add-attribute match="/*" attribute-name="href">
    <p:with-option name="attribute-value" select="if ($fileset-xml-base='')
                                                  then $file-base
                                                  else pf:relativize-uri(resolve-uri($file-base,$fileset-base),$fileset-base)"/>
  </p:add-attribute>
  <p:choose>
    <p:when test="not($original-href='')">
      <p:add-attribute match="/*" attribute-name="original-href">
        <p:with-option name="attribute-value" select="pf:normalize-uri(resolve-uri($original-href,$fileset-base))"/>
      </p:add-attribute>
    </p:when>
    <p:otherwise>
      <p:identity/>
    </p:otherwise>
  </p:choose>
  </p:group>

  <!--
      Add custom attributes
  -->
  <p:identity name="entry-without-attributes"/>
  <p:sink/>
  <p:xslt>
    <p:input port="source">
      <p:pipe step="main" port="file-attributes"/>
    </p:input>
    <p:input port="stylesheet">
      <p:inline>
        <xsl:stylesheet version="2.0">
          <xsl:template match="/*">
            <d:file>
              <xsl:for-each select="*">
                <xsl:attribute name="{@name}" select="@value"/>
              </xsl:for-each>
            </d:file>
          </xsl:template>
        </xsl:stylesheet>
      </p:inline>
    </p:input>
    <p:input port="parameters">
      <p:empty/>
    </p:input>
  </p:xslt>
  <px:assert message="href, original-href and media-type are not allowed file attributes" error-code="XXXXX" name="attributes">
    <p:with-option name="test" select="not(exists(/d:file[@href or @original-href or @media-type]))"/>
  </px:assert>
  <p:sink/>
  <p:set-attributes match="/*">
    <p:input port="source">
      <p:pipe step="entry-without-attributes" port="result"/>
    </p:input>
    <p:input port="attributes">
      <p:pipe step="attributes" port="result"/>
    </p:input>
  </p:set-attributes>
  <p:identity name="new-entry"/>

  <!--
      Insert the entry as the last or first child of the file set - unless it already exists
  -->
  <p:group name="result">
    <p:output port="fileset" primary="true"/>
    <p:output port="in-memory" sequence="true">
      <p:pipe step="if-present-in-input" port="in-memory"/>
    </p:output>
    <p:variable name="href-normalized" select="/*/@href">
      <p:pipe port="result" step="new-entry"/>
    </p:variable>
    <p:identity>
      <p:input port="source">
        <p:pipe port="source" step="main"/>
      </p:input>
    </p:identity>
    <p:choose name="if-present-in-input">
      <p:when test="not($replace) and /*/d:file[@href=$href-normalized]">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
          <p:pipe step="main" port="source.in-memory"/>
        </p:output>
        <p:choose>
          <p:when test="$replace-attributes">
            <p:set-attributes>
              <p:input port="attributes">
                <p:pipe step="new-entry" port="result"/>
              </p:input>
              <p:with-option name="match" select="concat('d:file[@href=&quot;',$href-normalized,'&quot;]')"/>
            </p:set-attributes>
          </p:when>
          <p:otherwise>
            <p:identity/>
          </p:otherwise>
        </p:choose>
        <p:identity/>
      </p:when>
      <p:otherwise>
        <p:output port="fileset" primary="true">
          <p:pipe step="fileset" port="result"/>
        </p:output>
        <p:output port="in-memory" sequence="true">
          <p:pipe step="in-memory" port="result"/>
        </p:output>
        <p:choose>
          <p:when test="$replace">
            <p:delete>
              <p:with-option name="match" select="concat('d:file[@href=&quot;',$href-normalized,'&quot;]')"/>
            </p:delete>
          </p:when>
          <p:otherwise>
            <p:identity/>
          </p:otherwise>
        </p:choose>
        <p:insert match="/*" name="fileset">
          <p:input port="insertion">
            <p:pipe port="result" step="new-entry"/>
          </p:input>
          <p:with-option name="position" select="if ($first) then 'first-child' else 'last-child'"/>
        </p:insert>
        <p:choose>
          <p:when test="not($first)">
            <p:identity>
              <p:input port="source">
                <p:pipe step="main" port="source.in-memory"/>
                <p:pipe step="main" port="entry"/>
              </p:input>
            </p:identity>
          </p:when>
          <p:otherwise>
            <p:identity>
              <p:input port="source">
                <p:pipe step="main" port="entry"/>
                <p:pipe step="main" port="source.in-memory"/>
              </p:input>
            </p:identity>
          </p:otherwise>
        </p:choose>
        <p:identity name="in-memory"/>
      </p:otherwise>
    </p:choose>
  </p:group>

</p:declare-step>
