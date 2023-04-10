<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:dc10="http://purl.org/dc/elements/1.0/"
                xmlns:dc11="http://purl.org/dc/elements/1.1/"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:ncx="http://www.daisy.org/z3986/2005/ncx/"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:oebpackage="http://openebook.org/namespaces/oeb-package/1.0/"
                name="main" type="px:daisy3-load">

    <p:documentation>
        <p>Creates a fileset document based on a DAISY 3 package file.</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Fileset containing the package file of the input DAISY 3 (marked with
              <code>media-type="application/oebps-package+xml"</code>).</p>
            <p>Will also be used for loading the other manifest items. When items are not present
              in this fileset, it will be attempted to load them from disk.</p>
            <p>It is assumed that if files are already in memory, the doctype declarations are
              present as file attributes (see the "detect-serialization-properties" option of
              <code>px:fileset-load</code>).</p>
        </p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true">
        <p:empty/>
    </p:input>

    <p:output port="result.fileset" primary="true" sequence="false">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The fileset entries are ordered as the appear in the <code>manifest</code> element of
              the input OPF document, except for SMIL documents and their corresponding DTBook
              documents which are listed in the <code>spine</code> order.</p>
            <p>Note: In the resulting fileset, the media type of SMIL documents will be
              <code>application/smil+xml</code> (as opposed to <code>application/smil</code> in DAISY
              3) and the media type of the OPF document will be
              <code>application/oebps-package+xml</code> (as opposed to <code>text/xml</code> in DAISY
              3). The media type of the NCX, DTBook and resources documents will always be
              <code>application/x-dtbncx+xml</code>, <code>application/x-dtbook+xml</code> and
              <code>application/x-dtbresource+xml</code> (even though in the 1.1.0 version of DAISY
              3 it is <code>text/xml</code>).</p>
            <p>All XML documents are loaded into memory.</p>
        </p:documentation>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="load-xml" port="unfiltered.in-memory"/>
    </p:output>

    <p:serialization port="result.fileset" indent="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-join
            px:fileset-intersect
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:smil-to-text-fileset
        </p:documentation>
    </p:import>

    <px:fileset-load media-types="application/oebps-package+xml" detect-serialization-properties="true"
                     name="load-opf">
        <!-- result fileset is normalized -->
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    <px:assert message="There must be exactly one OPF file in the DAISY 3 fileset"
               test-count-min="1" test-count-max="1" error-code="XXXXX"/>

    <p:group name="fileset-from-manifest">
        <p:output port="result"/>
        <p:variable name="opf" select="resolve-uri(base-uri(/*))"/>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="opf-to-fileset.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <!-- normalize -->
        <px:fileset-join/>
        <px:assert message="There must be exactly one OPF item in the OPF manifest and it must point to the OPF itself">
            <p:with-option name="test" select="count(//d:file[@media-type='application/oebps-package+xml'])=1
                                               and //d:file[@media-type='application/oebps-package+xml'][resolve-uri(@href,base-uri(.))=$opf]"/>
        </px:assert>
        <px:assert message="There must be exactly one NCX item in the OPF manifest">
            <p:with-option name="test" select="count(//d:file[@media-type='application/x-dtbncx+xml'])=1"/>
        </px:assert>
        <p:identity name="fileset-from-manifest-without-attrs"/>
        <!--
            add file attributes
        -->
        <p:sink/>
        <px:fileset-join name="join">
            <p:input port="source">
                <p:pipe step="fileset-from-manifest-without-attrs" port="result"/>
                <p:pipe step="load-opf" port="unfiltered.fileset"/>
            </p:input>
        </px:fileset-join>
        <px:fileset-intersect>
            <p:input port="source">
                <p:pipe step="join" port="result"/>
                <p:pipe step="fileset-from-manifest-without-attrs" port="result"/>
            </p:input>
        </px:fileset-intersect>
    </p:group>

    <p:group name="fileset-ordered">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="choose" port="in-memory"/>
        </p:output>
        <!-- re-order the DTBook entries in the file set -->
        <p:choose name="choose">
            <p:when test="count(//d:file[@media-type='application/x-dtbook+xml'])&gt;1">
                <!-- when there is more than one DTBook, delete all entries and re-compute them by
                     parsing each SMIL file -->
                <p:output port="fileset" primary="true"/>
                <p:output port="in-memory" sequence="true">
                    <p:pipe step="load-smils" port="result"/>
                </p:output>
                <p:delete match="d:file[@media-type='application/x-dtbook+xml']"/>
                <px:fileset-load media-types="application/smil+xml" name="load-smils"
                                 detect-serialization-properties="true"/>
                <p:for-each name="dtbook-fileset-without-attrs">
                    <p:output port="result"/>
                    <p:iteration-source>
                        <p:pipe step="load-smils" port="result"/>
                    </p:iteration-source>
                    <px:smil-to-text-fileset/>
                    <p:add-attribute match="d:file"
                                     attribute-name="media-type"
                                     attribute-value="application/x-dtbook+xml"/>
                </p:for-each>
                <p:sink/>
                <!--
                    join and add file attributes
                -->
                <px:fileset-join>
                    <p:input port="source">
                        <p:pipe step="load-smils" port="unfiltered.fileset"/>
                        <p:pipe step="dtbook-fileset-without-attrs" port="result"/>
                        <p:pipe step="fileset-from-manifest" port="result"/>
                    </p:input>
                </px:fileset-join>
            </p:when>
            <p:otherwise>
                <p:output port="fileset" primary="true"/>
                <p:output port="in-memory" sequence="true">
                    <p:empty/>
                </p:output>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:group>

    <!--
        load XML files if not loaded yet
    -->
    <px:fileset-load name="load-xml"
                     media-types="application/smil+xml
                                  application/x-dtbook+xml
                                  application/x-dtbncx+xml
                                  application/x-dtbresource+xml"
                     detect-serialization-properties="true">
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
            <p:pipe step="fileset-ordered" port="in-memory"/>
        </p:input>
    </px:fileset-load>
    <p:sink/>
    <p:identity>
        <p:input port="source">
            <p:pipe step="load-xml" port="unfiltered.fileset"/>
        </p:input>
    </p:identity>

    <!--
        add media-version attributes based on doctype
    -->
    <p:group>
        <p:add-attribute match="d:file[@media-type='application/oebps-package+xml']
                                      [not(@media-version)]
                                      [@doctype-public='+//ISBN 0-9673008-1-9//DTD OEB 1.0.1 Package//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'1.0.1'"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/oebps-package+xml']
                                      [not(@media-version)]
                                      [@doctype-public='+//ISBN 0-9673008-1-9//DTD OEB 1.2 Package//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'1.2'"/>
        </p:add-attribute>
    </p:group>
    <p:group>
        <p:add-attribute match="d:file[@media-type='application/x-dtbncx+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD ncx v1.1.0//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'1.1.0'"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/x-dtbncx+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD ncx 2005-1//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'2005-1'"/>
        </p:add-attribute>
    </p:group>
    <p:group>
        <p:add-attribute match="d:file[@media-type='application/smil+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD dtbsmil v1.1.0//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'dtb-1.1.0'"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/smil+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD dtbsmil 2005-1//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'dtb-2005-1'"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/smil+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD dtbsmil 2005-2//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'dtb-2005-2'"/>
        </p:add-attribute>
    </p:group>
    <p:group>
        <p:add-attribute match="d:file[@media-type='application/x-dtbook+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD dtbook v1.1.0//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'1.1.0'"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/x-dtbook+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD dtbook 2005-1//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'2005-1'"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/x-dtbook+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD dtbook 2005-2//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'2005-2'"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/x-dtbook+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD dtbook 2005-3//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'2005-3'"/>
        </p:add-attribute>
    </p:group>
    <p:group>
        <p:add-attribute match="d:file[@media-type='application/x-dtbresource+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD resource v1.1.0//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'1.1.0'"/>
        </p:add-attribute>
        <p:add-attribute match="d:file[@media-type='application/x-dtbresource+xml']
                                      [not(@media-version)]
                                      [@doctype-public='-//NISO//DTD resource 2005-1//EN']"
                         attribute-name="media-version">
            <p:with-option name="attribute-value" select="'2005-1'"/>
        </p:add-attribute>
    </p:group>

    <!--
        add media-version attributes based on version attribute on root element
    -->
    <p:choose>
        <p:when test="//d:file[@media-type='application/x-dtbncx+xml'][not(@media-version)]">
            <p:identity name="fileset"/>
            <p:delete match="d:file[not(@media-type='application/x-dtbncx+xml') or @media-version]"/>
            <px:fileset-load>
                <p:input port="in-memory">
                    <p:pipe step="load-xml" port="result"/>
                </p:input>
            </px:fileset-load>
            <p:for-each>
                <p:identity name="ncx"/>
                <p:sink/>
                <px:fileset-add-entry>
                    <p:input port="entry">
                        <p:pipe step="ncx" port="result"/>
                    </p:input>
                    <p:with-param port="file-attributes" name="media-version" select="(/ncx|/ncx:ncx)/@version">
                        <p:pipe step="ncx" port="result"/>
                    </p:with-param>
                </px:fileset-add-entry>
            </p:for-each>
            <p:identity name="ncx.fileset"/>
            <p:sink/>
            <px:fileset-join>
                <p:input port="source">
                    <p:pipe step="fileset" port="result"/>
                    <p:pipe step="ncx.fileset" port="result"/>
                </p:input>
            </px:fileset-join>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:choose>
        <p:when test="//d:file[@media-type='application/x-dtbook+xml'][not(@media-version)]">
            <p:identity name="fileset"/>
            <p:delete match="d:file[not(@media-type='application/x-dtbook+xml') or @media-version]"/>
            <px:fileset-load>
                <p:input port="in-memory">
                    <p:pipe step="load-xml" port="result"/>
                </p:input>
            </px:fileset-load>
            <p:for-each>
                <p:identity name="dtbook"/>
                <p:sink/>
                <px:fileset-add-entry>
                    <p:input port="entry">
                        <p:pipe step="dtbook" port="result"/>
                    </p:input>
                    <p:with-param port="file-attributes" name="media-version" select="(/dtbook|/dtb:dtbook)/@version">
                        <p:pipe step="dtbook" port="result"/>
                    </p:with-param>
                </px:fileset-add-entry>
            </p:for-each>
            <p:identity name="dtbook.fileset"/>
            <p:sink/>
            <px:fileset-join>
                <p:input port="source">
                    <p:pipe step="fileset" port="result"/>
                    <p:pipe step="dtbook.fileset" port="result"/>
                </p:input>
            </px:fileset-join>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <!--
        add media-version attributes based on dc:Format metadata in OPF
    -->
    <p:choose>
        <p:variable name="dc-format" select="/oebpackage:package/oebpackage:metadata/oebpackage:dc-metadata
                                             /(dc10:Format|dc11:Format)/string(.)">
            <p:pipe step="load-opf" port="result"/>
        </p:variable>
        <p:when test="$dc-format='ANSI/NISO Z39.86-2002'">
            <p:add-attribute match="d:file[@media-type='application/oebps-package+xml'][not(@media-version)]"
                             attribute-name="media-version">
                <p:with-option name="attribute-value" select="'1.0.1'"/>
            </p:add-attribute>
            <p:add-attribute match="d:file[@media-type='application/x-dtbncx+xml'][not(@media-version)]"
                             attribute-name="media-version">
                <p:with-option name="attribute-value" select="'1.1.0'"/>
            </p:add-attribute>
            <p:add-attribute match="d:file[@media-type='application/smil+xml'][not(@media-version)]"
                             attribute-name="media-version">
                <p:with-option name="attribute-value" select="'dtb-1.1.0'"/>
            </p:add-attribute>
            <p:add-attribute match="d:file[@media-type='application/x-dtbook+xml'][not(@media-version)]"
                             attribute-name="media-version">
                <p:with-option name="attribute-value" select="'1.1.0'"/>
            </p:add-attribute>
        </p:when>
        <p:when test="$dc-format='ANSI/NISO Z39.86-2005'">
            <p:add-attribute match="d:file[@media-type='application/oebps-package+xml'][not(@media-version)]"
                             attribute-name="media-version">
                <p:with-option name="attribute-value" select="'1.2'"/>
            </p:add-attribute>
            <p:add-attribute match="d:file[@media-type='application/x-dtbncx+xml'][not(@media-version)]"
                             attribute-name="media-version">
                <p:with-option name="attribute-value" select="'2005-1'"/>
            </p:add-attribute>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

</p:declare-step>
