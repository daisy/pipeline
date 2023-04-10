<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                type="px:daisy202-to-epub3"
                name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml"> Transforms DAISY 2.02 into EPUB3. </p:documentation>

    <p:input port="fileset.in" primary="true">
        <!-- TODO: This fileset is assumed to reference SMIL files and HTML files in reading order. px:daisy202-load provides this, but we could provide a step that rearranges the fileset according to the reading order for use by other scripts. -->
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">A fileset containing references to all the DAISY 2.02 files and any resources they reference (audio, images etc.). SMIL files and HTML
            files occur in reading order.</p:documentation>
    </p:input>

    <p:input port="in-memory.in" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The part of the DAISY 2.02 fileset that is loaded into memory.</p:documentation>
    </p:input>

    <p:output port="fileset.out" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">A fileset containing references to the EPUB3 files.</p:documentation>
        <p:pipe port="result" step="result.fileset"/>
    </p:output>

    <p:output port="in-memory.out" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The part of the resulting EPUB3 fileset that is stored in memory.</p:documentation>
        <p:pipe port="result" step="result.in-memory"/>
    </p:output>

    <p:option name="output-dir" required="true" cx:type="xs:anyURI" cx:as="xs:string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">The directory that the EPUB3 fileset is intended to be stored in.</p:documentation>
    </p:option>

    <p:option name="mediaoverlay" required="false" select="'true'" cx:type="xs:boolean" cx:as="xs:string"/>
    <p:option name="compatibility-mode" required="false" select="'true'" cx:type="xs:boolean" cx:as="xs:string"/>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub3-ocf-finalize
        </p:documentation>
    </p:import>

    <p:import href="resolve-links.create-mapping.xpl"/>
    <p:import href="ncc-navigation.xpl"/>
    <p:import href="navigation.xpl"/>
    <p:import href="content.xpl"/>
    <p:import href="media-overlay.xpl"/>
    <p:import href="resources.xpl"/>
    <p:import href="package.xpl"/>
    <cx:import href="http://www.daisy.org/pipeline/modules/daisy202-utils/library.xsl" type="application/xslt+xml">
        <p:documentation>
            pf:daisy202-identifier
        </p:documentation>
    </cx:import>

    <p:variable name="epub-dir" select="concat($output-dir,'epub/')"/>
    <p:variable name="publication-dir" select="concat($epub-dir,'EPUB/')"/>
    <p:variable name="content-dir" select="concat($publication-dir,'Content/')"/>
    <p:variable name="daisy-dir" select="base-uri(/*)">
        <p:pipe port="fileset.in" step="main"/>
    </p:variable> 
    
    <p:group name="pre-processing">
        <p:output port="ncc" primary="true">
            <p:pipe port="result" step="ncc"/>
        </p:output>
        <p:output port="smils" sequence="true">
            <p:pipe port="smils" step="pre-processing.do"/>
        </p:output>
        <p:output port="htmls" sequence="true">
            <p:pipe port="htmls" step="pre-processing.do"/>
        </p:output>
        
        <!-- load the NCC -->
        <px:fileset-filter href="*/ncc.html" name="ncc-fileset">
            <p:input port="source">
                <p:pipe port="fileset.in" step="main"/>
            </p:input>
        </px:fileset-filter>
        <px:fileset-load>
            <p:input port="in-memory">
                <p:pipe port="in-memory.in" step="main"/>
            </p:input>
        </px:fileset-load>
        <p:identity name="ncc"/>
        <px:assert message="There must be exactly one NCC-file" test-count-min="1" test-count-max="1" error-code="PDE06"/>
        
        <!-- Load original SMIL files -->
        <px:fileset-load media-types="application/smil+xml" name="original-smils">
            <p:input port="fileset">
                <p:pipe port="fileset.in" step="main"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe port="in-memory.in" step="main"/>
            </p:input>
        </px:fileset-load>
        
        <!-- Load original HTML files -->
        <px:fileset-filter media-types="application/xhtml+xml text/html">
            <p:input port="source">
                <p:pipe port="fileset.in" step="main"/>
            </p:input>
        </px:fileset-filter>
        <px:fileset-diff>
            <p:input port="secondary">
                <p:pipe port="result" step="ncc-fileset"/>
            </p:input>
        </px:fileset-diff>
        <px:fileset-load name="original-htmls">
            <p:input port="in-memory">
                <p:pipe port="in-memory.in" step="main"/>
            </p:input>
        </px:fileset-load>
        
        <!--pre-process the file set if it's an NCC-only book-->
        <p:count limit="1"/>
        <p:choose name="pre-processing.do">
            <p:when test="number(/*)=0">
                <p:output port="htmls" sequence="true">
                    <p:pipe port="htmls" step="iter-smils"/>
                </p:output>
                <p:output port="smils" sequence="true">
                    <p:pipe port="smils" step="iter-smils"/>
                </p:output>
                <p:for-each name="iter-smils">
                    <p:iteration-source>
                        <p:pipe port="result" step="original-smils"/>
                    </p:iteration-source>
                    <p:output port="htmls" sequence="true">
                        <p:pipe port="result" step="new-html"/>
                    </p:output>
                    <p:output port="smils" sequence="true">
                        <p:pipe port="result" step="new-smil"/>
                    </p:output>
                    <p:string-replace name="new-smil" 
                        match="text/@src"
                        replace="replace(.,'^[^#]+',replace(base-uri(),'(.*/)?([^/]+)\.[^.]*','$2.html'))" />
                    <p:xslt name="new-html">
                        <p:input port="source">
                            <p:pipe port="result" step="ncc"/>
                        </p:input>
                        <p:input port="stylesheet">
                            <p:document href="ncc-to-content.xsl"/>
                        </p:input>
                        <p:with-param name="base" select="replace(base-uri(/*),'^(.+)\.[^.]+(#.*)?$','$1.html$2')"/>
                        <p:with-param name="ids"
                            select="
                            string-join(distinct-values(//text/substring-after(@src,'#')),' ')"/>
                    </p:xslt>
                </p:for-each>
            </p:when>
            <p:otherwise>
                <p:output port="htmls" sequence="true">
                    <p:pipe port="result" step="original-htmls"/>
                </p:output>
                <p:output port="smils" sequence="true">
                    <p:pipe port="result" step="original-smils"/>
                </p:output>
                <p:sink/>
            </p:otherwise>
        </p:choose>
    </p:group>
    
    <!-- Make a map of all links from the SMIL files to the HTML files -->
    <pxi:daisy202-to-epub3-resolve-links-create-mapping name="resolve-links-mapping">
        <p:input port="daisy-smil">
            <p:pipe port="smils" step="pre-processing"/>
        </p:input>
    </pxi:daisy202-to-epub3-resolve-links-create-mapping>
    <p:sink/>

    <!-- Make a Navigation Document based on the DAISY 2.02 NCC. -->

    <p:identity>
        <p:input port="source">
            <p:pipe port="ncc" step="pre-processing"/>
        </p:input>
    </p:identity>
    <px:message message="Extracting dc:identifier from NCC"/>
    <p:add-attribute name="pub-id" match="/*" attribute-name="value">
        <p:with-option name="attribute-value" select="pf:daisy202-identifier(/)"/>
        <p:input port="source">
            <p:inline>
                <d:meta name="pub-id"/>
            </p:inline>
        </p:input>
    </p:add-attribute>
    <p:identity name="ncc.pub-id"/>

    <pxi:daisy202-to-epub3-ncc-navigation name="ncc-navigation">
        <p:with-option name="publication-dir" select="$publication-dir"/>
        <p:with-option name="content-dir" select="$content-dir"/>
        <p:input port="ncc">
            <p:pipe port="ncc" step="pre-processing"/>
        </p:input>
        <p:input port="resolve-links-mapping">
            <p:pipe port="result" step="resolve-links-mapping"/>
        </p:input>
    </pxi:daisy202-to-epub3-ncc-navigation>
    <p:sink/>

    <!-- Convert the content files. -->

    <pxi:daisy202-to-epub3-content name="content-without-navigation">
        <p:with-option name="publication-dir" select="$publication-dir">
            <p:empty/>
        </p:with-option>
        <p:with-option name="content-dir" select="$content-dir">
            <p:empty/>
        </p:with-option>
        <p:with-option name="daisy-dir" select="$daisy-dir">
            <p:empty/>
        </p:with-option>
        <p:input port="content-flow">
            <p:pipe port="htmls" step="pre-processing"/>
        </p:input>
        <p:input port="resolve-links-mapping">
            <p:pipe port="result" step="resolve-links-mapping"/>
        </p:input>
        <p:input port="ncc-navigation">
            <p:pipe port="result" step="ncc-navigation"/>
        </p:input>
    </pxi:daisy202-to-epub3-content>

    <!-- Improve the EPUB 3 Navigation Document based on all the Content Documents. -->
    <pxi:daisy202-to-epub3-navigation name="navigation">
        <p:with-option name="publication-dir" select="$publication-dir">
            <p:empty/>
        </p:with-option>
        <p:with-option name="content-dir" select="$content-dir">
            <p:empty/>
        </p:with-option>
        <p:with-option name="compatibility-mode" select="$compatibility-mode">
            <p:empty/>
        </p:with-option>
        <p:input port="ncc-navigation">
            <p:pipe port="result" step="ncc-navigation"/>
        </p:input>
        <p:input port="content">
            <p:pipe port="content" step="content-without-navigation"/>
        </p:input>
    </pxi:daisy202-to-epub3-navigation>

    <!-- Content Documents -->
    <!-- Nav Doc if it's the only content, or other XHTML otherwise -->
    <p:count limit="1">
        <p:input port="source">
            <p:pipe port="content" step="content-without-navigation"/>
        </p:input>
    </p:count>
    <p:choose name="content-docs">
        <p:when test="number(/*)=0">
            <p:output port="content" sequence="true" primary="true"/>
            <p:output port="fileset">
                <p:pipe port="result" step="fileset"/>
            </p:output>
            <p:identity name="fileset">
                <p:input port="source">
                    <p:pipe port="fileset" step="navigation"/>
                </p:input>
            </p:identity>
            <p:identity name="content">
                <p:input port="source">
                    <p:pipe port="navigation" step="navigation"/>
                </p:input>
            </p:identity>
        </p:when>
        <p:otherwise>
            <p:output port="content" sequence="true" primary="true"/>
            <p:output port="fileset">
                <p:pipe port="result" step="fileset"/>
            </p:output>
            <p:identity name="fileset">
                <p:input port="source">
                    <p:pipe port="fileset" step="content-without-navigation"/>
                </p:input>
            </p:identity>
            <p:identity name="content">
                <p:input port="source">
                    <p:pipe port="content" step="content-without-navigation"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <p:sink/>

    <pxi:daisy202-to-epub3-mediaoverlay name="mediaoverlay">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml"><p>Convert and copy the content files and SMIL-files.</p></p:documentation>
        <p:with-option name="include-mediaoverlay" select="$mediaoverlay"/>
        <p:with-option name="daisy-dir" select="$daisy-dir"/>
        <p:with-option name="publication-dir" select="$publication-dir"/>
        <p:with-option name="content-dir" select="$content-dir"/>
        <p:input port="daisy-smil">
            <p:pipe port="smils" step="pre-processing"/>
        </p:input>
        <p:input port="content">
            <p:pipe port="content" step="content-docs"/>
        </p:input>
    </pxi:daisy202-to-epub3-mediaoverlay>

    <pxi:daisy202-to-epub3-resources name="resources">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">List all referenced auxilliary resources (audio, stylesheets, images, etc.).</p:documentation>
        <p:input port="daisy-smil">
            <p:pipe port="smils" step="pre-processing"/>
        </p:input>
        <p:input port="daisy-content">
            <p:pipe port="content" step="content-docs"/>
        </p:input>
        <p:with-option name="include-mediaoverlay-resources" select="$mediaoverlay">
            <p:empty/>
        </p:with-option>
        <p:with-option name="content-dir" select="$content-dir">
            <p:empty/>
        </p:with-option>
    </pxi:daisy202-to-epub3-resources>

    <pxi:daisy202-to-epub3-package name="package">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">Make and store the OPF.</p:documentation>
        <p:input port="spine">
            <p:pipe port="fileset" step="content-docs"/>
        </p:input>
        <p:input port="resources">
            <p:pipe port="fileset" step="resources"/>
            <p:pipe port="fileset" step="navigation"/>
        </p:input>
        <p:input port="ncc">
            <p:pipe port="ncc" step="pre-processing"/>
        </p:input>
        <p:input port="navigation">
            <p:pipe port="navigation" step="navigation"/>
        </p:input>
        <p:input port="content-docs">
            <p:pipe port="content-navfix" step="navigation"/>
        </p:input>
        <p:input port="mediaoverlay">
            <p:pipe port="mediaoverlay" step="mediaoverlay"/>
        </p:input>
        <p:with-option name="pub-id" select="/*/@value">
            <p:pipe port="result" step="ncc.pub-id"/>
        </p:with-option>
        <p:with-option name="publication-dir" select="$publication-dir"/>
        <p:with-option name="epub-dir" select="$epub-dir"/>
        <p:with-option name="compatibility-mode" select="$compatibility-mode"/>
    </pxi:daisy202-to-epub3-package>
    <p:sink/>

    <px:epub3-ocf-finalize name="finalize">
        <p:input port="source">
            <p:pipe port="result" step="result.fileset-without-ocf-files"/>
        </p:input>
    </px:epub3-ocf-finalize>
    <px:fileset-join>
        <p:input port="source">
            <p:pipe port="result" step="finalize"/>
            <p:pipe port="result" step="result.fileset-without-ocf-files"/>
        </p:input>
    </px:fileset-join>
    <px:message message="Added container files to fileset (mimetype, META-INF/container.xml)"/>
    <p:identity name="result.fileset"/>
    <p:sink/>

    <p:for-each name="result.for-each">
        <p:output port="in-memory">
            <p:pipe port="result" step="result.for-each.in-memory"/>
        </p:output>
        <p:output port="fileset">
            <p:pipe port="result" step="result.for-each.fileset"/>
        </p:output>
        <p:iteration-source>
            <p:pipe port="navigation" step="navigation"/>
            <p:pipe port="ncx" step="navigation"/>
            <p:pipe port="content-navfix" step="navigation"/>
            <p:pipe port="mediaoverlay" step="mediaoverlay"/>
            <p:pipe port="opf-package" step="package"/>
        </p:iteration-source>
        <p:delete match="/*/@original-href | /*/@xml:base"/>
        <p:identity name="result.for-each.in-memory"/>
        <p:add-attribute match="/*" attribute-name="href">
            <p:with-option name="attribute-value" select="base-uri(/*)">
                <p:pipe port="current" step="result.for-each"/>
            </p:with-option>
            <p:input port="source">
                <p:inline exclude-inline-prefixes="#all">
                    <d:file/>
                </p:inline>
            </p:input>
        </p:add-attribute>
        <p:choose>
            <p:when test="/*/@original-href">
                <p:xpath-context>
                    <p:pipe port="current" step="result.for-each"/>
                </p:xpath-context>
                <p:add-attribute match="/*" attribute-name="original-href">
                    <p:with-option name="attribute-value" select="/*/@original-href">
                        <p:pipe port="current" step="result.for-each"/>
                    </p:with-option>
                </p:add-attribute>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>

        <p:wrap-sequence wrapper="d:fileset"/>
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="$epub-dir"/>
        </p:add-attribute>
        <p:identity name="result.for-each.fileset"/>
    </p:for-each>

    <px:fileset-join>
        <p:input port="source">
            <p:pipe port="fileset" step="result.for-each"/>
            <p:pipe port="fileset" step="resources"/>
        </p:input>
    </px:fileset-join>
    <px:mediatype-detect>
        <p:input port="in-memory">
            <p:pipe port="in-memory" step="result.for-each"/>
        </p:input>
    </px:mediatype-detect>
    <px:message message="Prepared final fileset of files for the EPUB package (which excludes the container files)"/>
    <p:identity name="result.fileset-without-ocf-files"/>
    <p:sink/>

    <p:identity name="result.in-memory">
        <p:input port="source">
            <p:pipe port="in-memory" step="result.for-each"/>
            <p:pipe port="in-memory.out" step="finalize"/>
        </p:input>
    </p:identity>
    <px:message message="Prepared final set of converted files for the EPUB package"/>
    <p:sink/>

</p:declare-step>
