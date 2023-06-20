<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:html="http://www.w3.org/1999/xhtml"
                px:input-filesets="daisy202"
                px:output-filesets="epub3"
                type="px:daisy202-to-epub3.script">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DAISY 2.02 to EPUB 3</h1>
        <p px:role="desc">Transforms a DAISY 2.02 publication into an EPUB 3 publication.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/daisy202-to-epub3/">
            Online Documentation
        </a>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Jostein Austvik Jacobsen</dd>
                <dt>E-mail:</dt>
                <dd><a px:role="contact" href="mailto:josteinaj@gmail.com">josteinaj@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization">NLB</dd>
            </dl>
        </address>
    </p:documentation>

    <p:option name="href" required="true" px:type="anyFileURI" px:media-type="application/xhtml+xml text/html">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">NCC</h2>
        </p:documentation>
    </p:option>
    <p:option name="output" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB 3</h2>
        </p:documentation>
    </p:option>
    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>
    <p:option name="mediaoverlay" required="false" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Include Media Overlay</h2>
            <p px:role="desc">Whether or not to include media overlays and associated audio files.</p>
        </p:documentation>
    </p:option>
    <p:option name="compatibility-mode" required="false" select="'true'" px:type="boolean">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Backwards compatible</h2>
            <p px:role="desc">Whether or not to include NCX-file, OPF guide element and backwards-compatible metadata.</p>
            <!-- TODO: if true, should convert filenames to simple ASCII filenames -->
        </p:documentation>
    </p:option>
    <p:option name="epub-filename" required="false" select="''" px:type="string">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB 3 file name</h2>
            <p px:role="desc" xml:space="preserve">By default, the file name is the dc:identifier with a ".epub" file extension.

This option can be used to set a custom file name.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub3-store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/daisy202-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="convert/convert.xpl"/>

    <p:variable name="output-dir" select="if (ends-with($output,'/')) then $output else concat($output,'/')"/>
    <p:variable name="tempDir" select="if (ends-with($temp-dir,'/')) then $temp-dir else concat($temp-dir,'/')"/>

    <!-- validate options -->
    <p:identity>
        <p:input port="source">
            <p:inline>
                <dummy-doc-for-assertions/>
            </p:inline>
        </p:input>
    </p:identity>
    <px:assert message="href: '$1' is not a valid URI. You probably either forgot to prefix the path with file:/, or if you're using Windows, remember to replace all directory separators (\) with forward slashes (/)." error-code="PDE01">
        <p:with-option name="test" select="matches($href,'\w+:/')"/>
        <p:with-option name="param1" select="$href"/>
    </px:assert>
    <px:assert message="output: '$1' is not a valid URI. You probably either forgot to prefix the path with file:/, or if you're using Windows, remember to replace all directory separators (\) with forward slashes (/)." error-code="PDE05">
        <p:with-option name="test" select="matches($output-dir,'\w+:/')"/>
        <p:with-option name="param1" select="$output-dir"/>
    </px:assert>
    <px:assert message="tempDir: '$1' is not a valid URI. You probably either forgot to prefix the path with file:/, or if you're using Windows, remember to replace all directory separators (\) with forward slashes (/)." error-code="PDE02">
        <p:with-option name="test" select="matches($tempDir,'\w+:/')"/>
        <p:with-option name="param1" select="$tempDir"/>
    </px:assert>
    <px:assert message="mediaoverlay: '$1' is not a valid value. When given, mediaoverlay must be either 'true' (default) or 'false'." error-code="PDE03">
        <p:with-option name="test" select="$mediaoverlay='true' or $mediaoverlay='false'"/>
        <p:with-option name="param1" select="$mediaoverlay"/>
    </px:assert>
    <px:assert message="compatibility-mode: '$1' is not a valid value. When given, compatibility-mode must be either 'true' (default) or 'false'." error-code="PDE04">
        <p:with-option name="test" select="$compatibility-mode='true' or $compatibility-mode='false'"/>
        <p:with-option name="param1" select="$compatibility-mode"/>
    </px:assert>

    <!-- load -->
    <px:daisy202-load name="load">
        <p:with-option name="ncc" select="$href"/>
    </px:daisy202-load>

    <!-- convert -->
    <px:daisy202-to-epub3 name="convert">
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="load"/>
        </p:input>
        <p:with-option name="output-dir" select="$tempDir"/>
        <p:with-option name="compatibility-mode" select="$compatibility-mode"/>
        <p:with-option name="mediaoverlay" select="$mediaoverlay"/>
    </px:daisy202-to-epub3>
    
    <!-- decide filename -->
    <px:fileset-load media-types="application/oebps-package+xml">
        <p:input port="in-memory">
            <p:pipe port="in-memory.out" step="convert"/>
        </p:input>
    </px:fileset-load>
    <p:split-sequence test="position()=1"/>
    <p:add-attribute match="/*" attribute-name="result-uri" cx:depends-on="mkdir">
        <p:with-option name="attribute-value" select="concat($output-dir,encode-for-uri(replace(if ($epub-filename='') then concat(//dc:identifier,'.epub') else $epub-filename,'[/\\?%*:|&quot;&lt;&gt;]','')))"/>
    </p:add-attribute>
    <p:delete match="/*/*"/>
    <p:identity name="result-uri"/>
    <p:sink/>
    <px:mkdir name="mkdir">
        <p:with-option name="href" select="$output-dir"/>
    </px:mkdir>
    
    <!-- store -->
    <px:epub3-store>
        <p:with-option name="href" select="/*/@result-uri">
            <p:pipe port="result" step="result-uri"/>
        </p:with-option>
        <p:input port="fileset.in">
            <p:pipe port="fileset.out" step="convert"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="convert"/>
        </p:input>
    </px:epub3-store>

</p:declare-step>
