<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-epub3" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
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
    
    <p:input port="epub.in.fileset" primary="true"/>
    <p:input port="epub.in.in-memory" sequence="true"/>
    
    <p:output port="epub.out.fileset" primary="true">
        <p:pipe step="out.fileset" port="result"/>
    </p:output>
    <p:output port="epub.out.in-memory" sequence="true">
        <p:pipe step="out.in-memory" port="result"/>
    </p:output>
    
    <p:option name="result-base" required="true"/>
    <p:option name="braille-translator" required="true" />
    <p:option name="stylesheet" required="true"/>
    <p:option name="apply-document-specific-stylesheets" required="true"/>
    <p:option name="set-default-rendition-to-braille" required="true"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl"/>
    
    <p:declare-step type="pxi:fileset-from-in-memory" name="fileset-from-in-memory">
        <p:input port="source" sequence="true"/>
        <p:output port="result"/>
        <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
        <px:fileset-create name="base" base="/"/>
        <p:for-each>
            <p:iteration-source>
                <p:pipe step="fileset-from-in-memory" port="source"/>
            </p:iteration-source>
            <px:fileset-add-entry>
                <p:with-option name="href" select="resolve-uri(base-uri(/*))"/>
                <p:input port="source">
                    <p:pipe port="result" step="base"/>
                </p:input>
            </px:fileset-add-entry>
        </p:for-each>
        <px:fileset-join/>
    </p:declare-step>
    
    <p:variable name="default-stylesheet" select="resolve-uri('../css/default.css')">
        <p:inline>
            <irrelevant/>
        </p:inline>
    </p:variable>
    
    <pxi:fileset-from-in-memory name="epub.in.in-memory.fileset">
        <p:input port="source">
            <p:pipe step="main" port="epub.in.in-memory"/>
        </p:input>
    </pxi:fileset-from-in-memory>
    <p:sink/>
    
    <p:identity>
        <p:input port="source">
            <p:pipe step="main" port="epub.in.fileset"/>
        </p:input>
    </p:identity>
    
    <!--
        Make sure that the base uri of the fileset is the directory containing the mimetype
        file. This will normally also eliminate any relative hrefs starting with "..", which is
        needed because px:fileset-move doesn't handle these correctly.
    -->
    <p:choose>
        <p:when test="//d:file[matches(@href,'^(.+/)?mimetype$')]">
            <px:fileset-rebase>
                <p:with-option name="new-base"
                               select="//d:file[matches(@href,'^(.+/)?mimetype$')][1]
                                       /replace(resolve-uri(@href,base-uri(.)),'mimetype$','')"/>
            </px:fileset-rebase>
        </p:when>
        <p:otherwise>
            <px:error code="XXXXX" message="Fileset must contain a 'mimetype' file"/>
        </p:otherwise>
    </p:choose>
    
    <px:fileset-move name="move">
        <p:with-option name="new-base" select="$result-base"/>
        <p:input port="in-memory.in">
            <p:pipe step="main" port="epub.in.in-memory"/>
        </p:input>
    </px:fileset-move>
    
    <!--
        container.xml
    -->
    
    <px:fileset-load name="original-container">
        <p:input port="in-memory">
            <p:pipe step="move" port="in-memory.out"/>
        </p:input>
        <p:with-option name="href" select="resolve-uri('META-INF/container.xml',$result-base)"/>
    </px:fileset-load>
    
    <!--
        default rendition package document
    -->
    
    <px:fileset-load media-types="application/oebps-package+xml">
        <p:input port="fileset">
            <p:pipe step="move" port="fileset.out"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe step="move" port="in-memory.out"/>
        </p:input>
    </px:fileset-load>
    <p:split-sequence test="position()=1"/>
    <p:identity name="default-rendition.package-document"/>
    
    <!--
        braille rendition file set
    -->
    
    <p:xslt name="braille-rendition.fileset">
        <p:input port="source">
            <p:pipe step="default-rendition.package-document" port="result"/>
            <p:pipe step="move" port="fileset.out"/>
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
                      select="resolve-uri('EPUB/package-braille.opf',$result-base)"/>
        <p:with-option name="output-base-uri"
                       select="resolve-uri('EPUB/package-braille.opf',$result-base)"/>
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
    <px:set-base-uri>
        <p:with-option name="base-uri" select="resolve-uri('META-INF/metadata.xml',$result-base)"/>
    </px:set-base-uri>
    <p:identity name="metadata"/>
    
    <!--
        braille rendition xhtml documents
    -->
    <px:fileset-filter media-types="application/xhtml+xml" name="braille-rendition.html.fileset">
        <p:input port="source">
            <p:pipe step="braille-rendition.fileset" port="result"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="move" port="in-memory.out"/>
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
                        <p:pipe step="move" port="in-memory.out"/>
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
                    <px:set-base-uri>
                        <!--
                            using "base-uri(parent::*)" because link has the base-uri of this XProc file
                        -->
                        <p:with-option name="base-uri"
                                       select="//html:link[@rel='stylesheet' and @type='text/css' and @media='embossed']
                                               /resolve-uri(@href,base-uri(parent::*))">
                            <p:pipe step="extract-css.result" port="result"/>
                        </p:with-option>
                    </px:set-base-uri>
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
                                /resolve-uri((@default-href,@href)[1],base-uri(.))">
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
                <p:with-param name="rendition-mapping.base" select="resolve-uri('EPUB/renditionMapping.html',$result-base)"/>
            </p:xslt>
        </p:group>
        <p:identity name="resource-map"/>
    </p:for-each>
    
    <!--
        braille rendition css files
    -->
    
    <px:fileset-create name="base"/>
    <p:for-each>
        <p:iteration-source>
            <p:pipe step="braille-rendition.html" port="css"/>
        </p:iteration-source>
        <px:fileset-add-entry>
            <p:input port="source">
                <p:pipe step="base" port="result"/>
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
    <px:set-base-uri>
        <p:with-option name="base-uri" select="resolve-uri('EPUB/renditionMapping.html',$result-base)"/>
    </px:set-base-uri>
    <p:identity name="rendition-mapping"/>
    
    <!--
        braille rendition smil files
    -->
    
    <px:fileset-filter media-types="application/smil+xml" name="braille-rendition.smil.fileset">
        <p:input port="source">
            <p:pipe step="braille-rendition.fileset" port="result"/>
        </p:input>
    </px:fileset-filter>
    <px:fileset-load>
        <p:input port="in-memory">
            <p:pipe step="move" port="in-memory.out"/>
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
    <p:identity name="braille-rendition.package-document-with-dc-language-and-css"/>
    
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
    <px:set-base-uri>
        <p:with-option name="base-uri" select="resolve-uri('META-INF/container.xml',$result-base)"/>
    </px:set-base-uri>
    <p:identity name="container"/>
    
    <!--
        combine everything
    -->
    
    <px:fileset-join>
        <p:input port="source">
            <p:pipe step="move" port="fileset.out"/>
            <p:pipe step="braille-rendition.html.fileset" port="result"/>
            <p:pipe step="braille-rendition.css.fileset" port="result"/>
            <p:pipe step="braille-rendition.smil.fileset" port="result"/>
        </p:input>
    </px:fileset-join>
    <p:delete match="d:file[@default-href]/@original-href"/>
    <p:delete match="d:file/@default-href"/>
    <!--
        delete @original-href from files that exist in memory
    -->
    <p:delete>
        <p:with-option name="match"
                       select="concat('d:file[resolve-uri(@href,base-uri(.))=&quot;',
                                      resolve-uri('META-INF/container.xml',$result-base),
                                      '&quot;]/@original-href')"/>
    </p:delete>
    <px:fileset-add-entry href="META-INF/container.xml"/>
    <px:fileset-add-entry href="META-INF/metadata.xml"/>
    <px:fileset-add-entry href="EPUB/package-braille.opf"/>
    <px:fileset-add-entry href="EPUB/renditionMapping.html"/>
    <p:add-attribute match="d:file[@href='EPUB/renditionMapping.html']" attribute-name="indent" attribute-value="true"/>
    <p:identity name="out.fileset"/>
    <p:sink/>
    
    <px:select-by-base name="remove-container-from-memory">
        <p:input port="source">
            <p:pipe step="move" port="in-memory.out"/>
        </p:input>
        <p:with-option name="base" select="resolve-uri('META-INF/container.xml',$result-base)"/>
    </px:select-by-base>
    <p:sink/>
    <p:identity name="out.in-memory">
        <p:input port="source">
            <p:pipe step="remove-container-from-memory" port="not-matched"/>
            <p:pipe step="container" port="result"/>
            <p:pipe step="metadata" port="result"/>
            <p:pipe step="braille-rendition.package-document-with-dc-language-and-css" port="result"/>
            <p:pipe step="braille-rendition.html" port="result"/>
            <p:pipe step="braille-rendition.html" port="css"/>
            <p:pipe step="braille-rendition.smil" port="result"/>
            <p:pipe step="rendition-mapping" port="result"/>
        </p:input>
    </p:identity>
    <p:sink/>
    
</p:declare-step>
