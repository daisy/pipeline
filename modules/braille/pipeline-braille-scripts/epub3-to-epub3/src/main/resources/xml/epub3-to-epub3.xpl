<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-epub3" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:ocf="urn:oasis:names:tc:opendocument:xmlns:container"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:rendition="http://www.idpf.org/2013/rendition"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Braille in EPUB 3</h1>
        <p px:role="desc">Transforms an EPUB 3 publication into an EPUB 3 publication with a braille rendition.</p>
        <dl px:role="author">
            <dt>Name:</dt>
            <dd px:role="name">Bert Frees</dd>
            <dt>E-mail:</dt>
            <dd><a px:role="contact" href="mailto:bertfrees@gmail.com">bertfrees@gmail.com</a></dd>
        </dl>
    </p:documentation>
    
    <p:option name="source" required="true" px:type="anyFileURI" px:media-type="application/epub+zip">
        <p:documentation>
            <h2 px:role="name">Input EPUB 3</h2>
        </p:documentation>
    </p:option>
    
    <p:option name="braille-translator" required="false" px:data-type="transform-query" select="'(translator:liblouis)'">
        <p:documentation>
            <h2 px:role="name">Braille translator query</h2>
        </p:documentation>
    </p:option>
    
    <p:option name="stylesheet" required="false" px:type="string" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Style sheets</h2>
            <p px:role="desc" xml:space="preserve">CSS style sheets to apply. A space separated list of URIs, absolute or relative to source.

All CSS style sheets are applied at once, but the order in which they are specified has an influence
on the cascading order.

If the "Apply document-specific CSS" option is enabled, the document-specific style sheets will be
applied before the ones specified through this option (see below).
</p>
        </p:documentation>
    </p:option>
    
    <p:option name="apply-document-specific-stylesheets" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Apply document-specific CSS</h2>
            <p px:role="desc" xml:space="preserve">If this option is enabled, any pre-existing CSS in the EPUB for medium "embossed" will be taken into account for the translation, or preserved in the result EPUB.

The HTML files inside the source EPUB may already contain CSS that applies to embossed media. Style
sheets can be associated with an HTML file in several ways: linked (using an 'xml-stylesheet'
processing instruction or a 'link' element), embedded (using a 'style' element) and/or inlined
(using 'style' attributes).

Document-specific CSS takes precedence over any CSS provided through the "Style sheets" option. For
instance, if the EPUB already contains the rule `p { padding-left: 2; }`, and using this script the
rule `p#docauthor { padding-left: 4; }` is provided, then the `padding-left` property will get the
value `2` because that's what was defined in the EPUB, even though the provided CSS is more
specific.
</p>
        </p:documentation>
    </p:option>
    
    <p:option name="set-default-rendition-to-braille" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Set default rendition to braille.</h2>
            <p px:role="desc">Make the generated braille rendition the default rendition.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation>
            <h2 px:role="name">Output EPUB 3</h2>
        </p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <p:variable name="default-stylesheet" select="resolve-uri('../css/default.css')">
        <p:inline>
            <irrelevant/>
        </p:inline>
    </p:variable>
    
    <p:variable name="source.base" select="concat($source,'!/')"/>
    
    <px:fileset-create name="target.base.fileset">
        <p:with-option name="base" select="concat($output-dir,'/',replace($source,'^.*/(([^/]+)\.epub|([^/]+))$','$2$3.epub'),'!/')"/>
    </px:fileset-create><p:group>
    
    <p:variable name="target.base" select="base-uri(/*)"/>
    <p:variable name="target" select="substring-before($target.base,'!/')"/>
    
    <px:unzip name="source-zipfile">
        <p:with-option name="href" select="$source"/>
    </px:unzip>
    <p:for-each>
        <p:iteration-source select="//c:file[not(@name='META-INF/container.xml')]"/>
        <p:variable name="href" select="/*/@name"/>
        <px:fileset-add-entry>
            <p:input port="source">
                <p:pipe step="target.base.fileset" port="result"/>
            </p:input>
            <p:with-option name="href" select="$href"/>
            <p:with-option name="original-href" select="resolve-uri($href,$source.base)"/>
        </px:fileset-add-entry>
    </p:for-each>
    <px:fileset-join name="source.fileset"/>
    <px:fileset-load name="source.in-memory">
        <p:input port="in-memory">
            <p:empty/>
        </p:input>
    </px:fileset-load>
    <p:sink/>
    
    <px:unzip file="META-INF/container.xml" content-type="application/xml" name="original-container">
        <p:with-option name="href" select="$source"/>
    </px:unzip>
    
    <!--
        default rendition package document
    -->
    
    <px:unzip content-type="application/oebps-package+xml">
        <p:with-option name="href" select="$source"/>
        <p:with-option name="file" select="//ocf:rootfile[1]/@full-path"/>
    </px:unzip>
    <p:add-attribute match="/*" attribute-name="xml:base">
        <p:with-option name="attribute-value" select="resolve-uri(//ocf:rootfile[1]/@full-path,$target.base)">
            <p:pipe step="original-container" port="result"/>
        </p:with-option>
    </p:add-attribute>
    <p:delete match="/*/@xml:base" name="default-rendition.package-document"/>
    
    <!--
        braille rendition file set
    -->
    
    <p:xslt name="braille-rendition.fileset">
        <p:input port="source">
            <p:pipe step="target.base.fileset" port="result"/>
            <p:pipe step="default-rendition.package-document" port="result"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="braille-rendition.fileset.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
    <!--
        braille rendition package document
    -->
    
    <p:xslt name="braille-rendition.package-document">
        <p:input port="source">
            <p:pipe step="default-rendition.package-document" port="result"/>
            <p:pipe step="braille-rendition.fileset" port="result"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="braille-rendition.package-document.xsl"/>
        </p:input>
        <p:with-param name="braille-rendition.package-document.base"
                      select="resolve-uri('EPUB/package-braille.opf',$target.base)"/>
    </p:xslt>
    
    <!--
        metadata.xml
    -->
    
    <p:identity>
        <p:input port="source">
            <p:inline xmlns="http://www.idpf.org/2007/opf">
<metadata xmlns:dcterms="http://purl.org/dc/terms/"/></p:inline>
        </p:input>
    </p:identity>
    <p:add-attribute match="/opf:metadata" attribute-name="unique-identifier">
        <p:with-option name="attribute-value" select="/opf:package/@unique-identifier">
            <p:pipe step="braille-rendition.package-document" port="result"/>
        </p:with-option>
    </p:add-attribute>
    <p:insert match="/opf:metadata" position="last-child">
        <p:input port="insertion" select="for $unique-identifier in /opf:package/@unique-identifier
                                          return /opf:package/opf:metadata/dc:identifier[@id=$unique-identifier]">
            <p:pipe step="braille-rendition.package-document" port="result"/>
        </p:input>
    </p:insert>
    <p:insert match="/opf:metadata" position="last-child">
        <p:input port="insertion" select="/opf:package/opf:metadata/opf:meta[@property='dcterms:modified']">
            <p:pipe step="braille-rendition.package-document" port="result"/>
        </p:input>
    </p:insert>
    <p:add-attribute match="/*" attribute-name="xml:base">
        <p:with-option name="attribute-value" select="resolve-uri('META-INF/metadata.xml',$target.base)"/>
    </p:add-attribute>
    <p:delete match="/*/@xml:base"/>
    <p:identity name="metadata"/>
    
    <!--
        braille rendition xhtml documents
    -->
    <px:fileset-filter media-types="application/xhtml+xml" name="braille-rendition.html.fileset">
        <p:input port="source">
            <p:pipe step="braille-rendition.fileset" port="result"/>
        </p:input>
    </px:fileset-filter>
    <p:label-elements match="d:file" attribute="original-href">
        <p:with-option name="label" select="concat('resolve-uri(@original-href,&quot;',$source.base,'&quot;)')"/>
    </p:label-elements>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:empty/>
        </p:input>
    </px:fileset-load>
    <p:for-each name="braille-rendition.html">
        <p:output port="result" primary="true">
            <p:pipe step="html" port="result"/>
        </p:output>
        <p:output port="css" sequence="true">
            <p:pipe step="extract-css" port="css"/>
        </p:output>
        <p:output port="resource-map" sequence="true">
            <p:pipe step="resource-map" port="result"/>
        </p:output>
        <p:variable name="lang" select="(/*/opf:metadata/dc:language[not(@refines)])[1]/text()">
            <p:pipe port="result" step="default-rendition.package-document"/>
        </p:variable>
        <px:message message="Generating $1" severity="INFO">
            <p:with-option name="param1" select="substring-after(base-uri(/*),'!/')"/>
        </px:message>
        <p:choose>
            <p:when test="$apply-document-specific-stylesheets='true'">
                <px:message severity="DEBUG" message="Inlining document-specific CSS"/>
                <css:apply-stylesheets>
                    <p:input port="context">
                        <p:pipe step="source.in-memory" port="result"/>
                    </p:input>
                </css:apply-stylesheets>
            </p:when>
            <p:otherwise>
                <p:delete match="@style"/>
            </p:otherwise>
        </p:choose>
        <css:delete-stylesheets/>
        <css:inline media="embossed">
            <p:with-option name="default-stylesheet" select="($stylesheet,$default-stylesheet)[not(.='')][1]"/>
        </css:inline>
        <px:transform name="transform">
            <p:with-option name="query" select="concat('(input:html)(input:css)(output:html)(output:css)(output:braille)',
                                                       $braille-translator,
                                                       '(locale:',$lang,')')"/>
        </px:transform>
        <p:group name="extract-css">
            <p:output port="result" primary="true">
                <p:pipe step="extract-css.result" port="result"/>
            </p:output>
            <p:output port="css" sequence="true">
                <p:pipe step="css" port="result"/>
            </p:output>
            <css:extract name="extract"/>
            <p:delete match="@style" name="without-css"/>
            <p:choose>
                <p:xpath-context>
                    <p:pipe step="extract" port="stylesheet"/>
                </p:xpath-context>
                <p:when test="normalize-space(string(/*))=''">
                    <p:identity/>
                </p:when>
                <p:otherwise>
                    <p:add-attribute match="/html:link" attribute-name="href" name="css-link">
                        <p:input port="source">
                            <p:inline xmlns="http://www.w3.org/1999/xhtml">
                                <link rel="stylesheet" type="text/css" media="embossed"/>
                            </p:inline>
                        </p:input>
                        <p:with-option name="attribute-value" select="replace(base-uri(/*),'^.*/(([^/]+)\.x?html|([^/]+))$','$2$3.css')"/>
                    </p:add-attribute>
                    <!--
                        assuming there is one and only one head element
                    -->
                    <p:insert match="html:head" position="last-child">
                        <p:input port="source">
                            <p:pipe step="without-css" port="result"/>
                        </p:input>
                        <p:input port="insertion">
                            <p:pipe step="css-link" port="result"/>
                        </p:input>
                    </p:insert>
                </p:otherwise>
            </p:choose>
            <p:identity name="extract-css.result"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="extract" port="stylesheet"/>
                </p:input>
            </p:identity>
            <p:choose>
                <p:when test="normalize-space(string(/*))=''">
                    <p:identity>
                        <p:input port="source">
                            <p:empty/>
                        </p:input>
                    </p:identity>
                </p:when>
                <p:otherwise>
                    <p:add-attribute match="/*" attribute-name="xml:base">
                        <!--
                            using "base-uri(parent::*)" because link has the base-uri of this XProc file
                        -->
                        <p:with-option name="attribute-value"
                                       select="//html:link[@rel='stylesheet' and @type='text/css' and @media='embossed']
                                               /resolve-uri(@href,base-uri(parent::*))">
                            <p:pipe step="extract-css.result" port="result"/>
                        </p:with-option>
                    </p:add-attribute>
                </p:otherwise>
            </p:choose>
            <p:identity name="css"/>
        </p:group>
        <p:xslt name="html">
            <p:input port="source">
                <p:pipe step="extract-css" port="result"/>
                <p:pipe step="braille-rendition.fileset" port="result"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="update-cross-references.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:group>
            <p:variable name="braille-rendition.html.base" select="base-uri(/*)">
                <p:pipe step="braille-rendition.html" port="current"/>
            </p:variable>
            <p:variable name="default-rendition.html.base"
                        select="//d:file[resolve-uri(@href,base-uri(.))=$braille-rendition.html.base]
                                /resolve-uri(@original-href,base-uri(.))">
                <p:pipe step="braille-rendition.fileset" port="result"/>
            </p:variable>
            <p:xslt template-name="main">
                <p:input port="stylesheet">
                    <p:document href="resource-map.xsl"/>
                </p:input>
                <p:input port="source">
                    <p:pipe step="default-rendition.package-document" port="result"/>
                    <p:pipe step="braille-rendition.package-document" port="result"/>
                </p:input>
                <p:with-param name="default-rendition.html.base" select="$default-rendition.html.base"/>
                <p:with-param name="braille-rendition.html.base" select="$braille-rendition.html.base"/>
                <p:with-param name="rendition-mapping.base" select="resolve-uri('EPUB/renditionMapping.html',$target.base)"/>
            </p:xslt>
        </p:group>
        <p:identity name="resource-map"/>
    </p:for-each>
    
    <!--
        braille rendition css files
    -->
    
    <p:for-each>
        <p:iteration-source>
            <p:pipe step="braille-rendition.html" port="css"/>
        </p:iteration-source>
        <px:fileset-add-entry>
            <p:input port="source">
                <p:pipe step="target.base.fileset" port="result"/>
            </p:input>
            <p:with-option name="href" select="base-uri(/*)"/>
            <p:with-option name="media-type" select="/c:result/@content-type"/> <!-- text/plain -->
        </px:fileset-add-entry>
    </p:for-each>
    <px:fileset-join name="braille-rendition.css.fileset"/>
    
    <!--
        rendition mapping document
    -->
    
    <p:insert match="//html:nav" position="last-child">
        <p:input port="source">
            <p:inline xmlns="http://www.w3.org/1999/xhtml">
<html>
   <head>
      <meta charset="utf-8"/>
   </head>
   <body>
      <nav epub:type="resource-map"/>
   </body>
</html></p:inline>
        </p:input>
        <p:input port="insertion" select="/html:nav[@epub:type='resource-map']/*">
            <p:pipe step="braille-rendition.html" port="resource-map"/>
        </p:input>
    </p:insert>
    <p:add-attribute match="/*" attribute-name="xml:base">
        <p:with-option name="attribute-value" select="resolve-uri('EPUB/renditionMapping.html',$target.base)"/>
    </p:add-attribute>
    <p:delete match="/*/@xml:base" name="rendition-mapping"/>
    
    <!--
        braille rendition smil files
    -->
    
    <px:fileset-filter media-types="application/smil+xml" name="braille-rendition.smil.fileset">
        <p:input port="source">
            <p:pipe step="braille-rendition.fileset" port="result"/>
        </p:input>
    </px:fileset-filter>
    <p:label-elements match="d:file" attribute="original-href">
        <p:with-option name="label" select="concat('resolve-uri(@original-href,&quot;',$source.base,'&quot;)')"/>
    </p:label-elements>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:empty/>
        </p:input>
    </px:fileset-load>
    <p:for-each>
        <p:add-xml-base name="_1"/>
        <p:xslt>
            <p:input port="source">
                <p:pipe step="_1" port="result"/>
                <p:pipe step="braille-rendition.fileset" port="result"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="update-cross-references.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:delete match="/*/@xml:base"/>
    </p:for-each>
    <p:identity name="braille-rendition.smil"/>
    
    <!--
        braille rendition package document with new dc:language and css files
    -->
    
    <p:xslt>
        <p:input port="source">
            <p:pipe step="braille-rendition.package-document" port="result"/>
            <p:pipe step="braille-rendition.css.fileset" port="result"/>
            <p:pipe step="braille-rendition.html" port="result"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="braille-rendition.package-document-with-dc-language-and-css.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    <p:delete match="/*/@xml:base" name="braille-rendition.package-document-with-dc-language-and-css"/>
    
    <!--
        container.xml
    -->
    
    <p:insert match="/ocf:container/ocf:rootfiles">
        <p:input port="source">
            <p:pipe step="original-container" port="result"/>
        </p:input>
        <p:input port="insertion">
            <p:inline xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                <rootfile full-path="EPUB/package-braille.opf" media-type="application/oebps-package+xml"
                          rendition:accessMode="tactile" rendition:label="Pre-translated to braille"/>
            </p:inline>
        </p:input>
        <p:with-option name="position" select="if ($set-default-rendition-to-braille='true') then 'first-child' else 'last-child'"/>
    </p:insert>
    <p:add-attribute match="/ocf:container/ocf:rootfiles/ocf:rootfile[last()]" attribute-name="rendition:language">
        <p:with-option name="attribute-value" select="/opf:package/opf:metadata/dc:language[1]/string(.)">
            <p:pipe step="braille-rendition.package-document-with-dc-language-and-css" port="result"/>
        </p:with-option>
    </p:add-attribute>
    <p:add-attribute match="/ocf:container/ocf:rootfiles/ocf:rootfile[last()]" attribute-name="rendition:layout">
        <p:with-option name="attribute-value"
                       select="(/opf:package/opf:metadata/opf:meta[@property='rendition:layout']/string(.),'reflowable')[1]">
            <p:pipe step="braille-rendition.package-document-with-dc-language-and-css" port="result"/>
        </p:with-option>
    </p:add-attribute>
    <p:insert position="last-child" match="/ocf:container">
        <p:input port="insertion">
            <p:inline xmlns="urn:oasis:names:tc:opendocument:xmlns:container">
                <link href="EPUB/renditionMapping.html" rel="mapping" media-type="application/xhtml+xml"/>
            </p:inline>
        </p:input>
    </p:insert>
    <p:add-attribute match="/*" attribute-name="xml:base">
        <p:with-option name="attribute-value" select="resolve-uri('META-INF/container.xml',$target.base)"/>
    </p:add-attribute>
    <p:delete match="/*/@xml:base" name="container"/>
    
    <!-- ===== -->
    <!-- Store -->
    <!-- ===== -->
    
    <px:fileset-join>
        <p:input port="source">
            <p:pipe step="braille-rendition.html.fileset" port="result"/>
            <p:pipe step="braille-rendition.css.fileset" port="result"/>
            <p:pipe step="braille-rendition.smil.fileset" port="result"/>
            <p:pipe step="source.fileset" port="result"/>
        </p:input>
    </px:fileset-join>
    <px:fileset-add-entry href="META-INF/container.xml"/>
    <px:fileset-add-entry href="META-INF/metadata.xml"/>
    <px:fileset-add-entry href="EPUB/package-braille.opf"/>
    <px:fileset-add-entry href="EPUB/renditionMapping.html"/>
    <p:add-attribute match="d:file[@href='EPUB/renditionMapping.html']" attribute-name="indent" attribute-value="true"/>
    <px:fileset-store>
        <p:input port="in-memory.in">
            <p:pipe step="source.in-memory" port="result"/>
            <p:pipe step="container" port="result"/>
            <p:pipe step="metadata" port="result"/>
            <p:pipe step="braille-rendition.package-document-with-dc-language-and-css" port="result"/>
            <p:pipe step="braille-rendition.html" port="result"/>
            <p:pipe step="braille-rendition.html" port="css"/>
            <p:pipe step="braille-rendition.smil" port="result"/>
            <p:pipe step="rendition-mapping" port="result"/>
        </p:input>
    </px:fileset-store>
    </p:group>
    
</p:declare-step>
