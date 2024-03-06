<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
                exclude-inline-prefixes="#all"
                px:input-filesets="dtbook"
                px:output-filesets="odt"
                type="px:dtbook-to-odt.script" name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook to ODT</h1>
        <p px:role="desc">Transforms a DTBook (DAISY 3 XML) document into an ODT (Open Document Text).</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-odt/">
            Online documentation
        </a>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Bert Frees</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:bertfrees@gmail.com">bert.frees@sbs.ch</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization" href="http://www.sbs-online.ch/">SBS</dd>
            </dl>
        </address>
    </p:documentation>
    
    <p:input port="source" primary="true" px:media-type="application/x-dtbook+xml">
        <p:documentation>
            <h2 px:role="name">DTBook</h2>
            <p px:role="desc">The DTBook to transform.</p>
        </p:documentation>
    </p:input>
    
    <p:option name="result" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation>
            <h2 px:role="name">ODT</h2>
            <p px:role="desc">The resulting ODT file.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>
    
    <p:option name="template" required="false" px:type="anyFileURI" select="''" px:media-type="application/vnd.oasis.opendocument.text-template">
        <p:documentation>
            <h2 px:role="name">Template</h2>
            <p px:role="desc" xml:space="preserve">OpenOffice template file (.ott) that contains the style definitions.

Defaults to [default.ott](http://daisy.github.io/pipeline/modules/dtbook-to-odt/doc/templates/default.ott.html).

See [Templating](http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-odt/#templating) for more details.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="asciimath" required="false" select="'ASCIIMATH'">
        <p:pipeinfo>
            <px:type>
                <choice>
                    <value>ASCIIMATH</value>
                    <value>MATHML</value>
                </choice>
            </px:type>
        </p:pipeinfo>
        <p:documentation>
            <h2 px:role="name">ASCIIMath handling</h2>
            <p px:role="desc">How to render ASCIIMath-encoded formulas.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="images" required="false" select="'EMBED'">
        <p:pipeinfo>
            <px:type>
                <choice>
                    <value>EMBED</value>
                    <value>LINK</value>
                </choice>
            </px:type>
        </p:pipeinfo>
        <p:documentation>
            <h2 px:role="name">Images handling</h2>
            <p px:role="desc">How to render images.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="page-numbers" required="false" px:type="boolean" select="'true'">
        <p:documentation>
            <h2 px:role="name">Page numbers</h2>
            <p px:role="desc">Whether to show page numbers or not.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="page-numbers-float" required="false" px:type="boolean" select="'true'">
        <p:documentation>
            <h2 px:role="name">Float page numbers</h2>
            <p px:role="desc">Try to float page numbers to an appropriate place as opposed to exactly following print.</p>
        </p:documentation>
    </p:option>
    
    <p:option name="image-dpi" required="false" px:type="integer" select="'600'">
        <p:documentation>
            <h2 px:role="name">Image resolution</h2>
            <p px:role="desc">Resolution of images in DPI.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="dtbook-to-odt.convert.xpl">
        <p:documentation>
            px:dtbook-to-odt
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl">
        <p:documentation>
            px:dtbook-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/odf-utils/library.xpl">
        <p:documentation>
            px:odf-store
        </p:documentation>
    </p:import>

        <!-- =========== -->
        <!-- LOAD DTBOOK -->
        <!-- =========== -->
        
        <p:sink/>
        <px:fileset-add-entry media-type="application/x-dtbook+xml" name="dtbook">
            <p:input port="entry">
                <p:pipe step="main" port="source"/>
            </p:input>
        </px:fileset-add-entry>
        <px:dtbook-load name="load">
            <p:input port="source.in-memory">
                <p:pipe step="dtbook" port="result.in-memory"/>
            </p:input>
        </px:dtbook-load>
        
        <!-- ===================== -->
        <!-- CONVERT DTBOOK TO ODT -->
        <!-- ===================== -->
        
        <px:dtbook-to-odt name="odt">
            <p:input port="content.xsl">
                <p:document href="content.xsl"/>
            </p:input>
            <p:input port="fileset.in">
                <p:pipe step="load" port="result.fileset"/>
            </p:input>
            <p:input port="in-memory.in">
                <p:pipe step="load" port="result.in-memory"/>
            </p:input>
            <p:input port="meta">
                <p:inline>
                    <meta:generator>${project.groupId}/${project.artifactId}/${project.version}</meta:generator>
                </p:inline>
                <p:inline>
                    <meta:user-defined meta:name="dtbook-to-odt.version">${project.version}</meta:user-defined>
                </p:inline>
            </p:input>
            <p:with-option name="temp-dir" select="$temp-dir"/>
            <p:with-option name="template" select="if ($template!='') then $template else resolve-uri('../templates/default.ott')">
                <p:inline>
                    <irrelevant/>
                </p:inline>
            </p:with-option>
            <p:with-option name="asciimath" select="$asciimath"/>
            <p:with-option name="images" select="$images"/>
            <p:with-param port="parameters" name="image_dpi" select="$image-dpi"/>
            <p:with-param port="parameters" name="page_numbers" select="$page-numbers"/>
            <p:with-param port="parameters" name="page_numbers_float" select="$page-numbers-float"/>
        </px:dtbook-to-odt>
        
        <!-- ========= -->
        <!-- STORE ODT -->
        <!-- ========= -->
        
        <px:odf-store name="store">
            <p:input port="source.fileset">
                <p:pipe step="odt" port="fileset.out"/>
            </p:input>
            <p:input port="source.in-memory">
                <p:pipe step="odt" port="in-memory.out"/>
            </p:input>
            <p:with-option name="href" select="concat($result, '/', replace(p:base-uri(/),'^.*/([^/]*)\.[^/\.]*$','$1'), '.odt')">
                <p:pipe step="main" port="source"/>
            </p:with-option>
        </px:odf-store>

</p:declare-step>
