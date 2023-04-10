<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="px:epub3-to-daisy3.script"
                px:input-filesets="epub3"
                px:output-filesets="daisy3">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">EPUB 3 to DAISY 3</h1>
        <p px:role="desc">Transforms an EPUB 3 publication into DAISY 3.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/epub3-to-daisy3/">
            Online documentation
        </a>
    </p:documentation>

    <p:option name="epub" required="true" px:type="anyFileURI" px:media-type="application/epub+zip application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB 3 Publication</h2>
            <p px:role="desc" xml:space="preserve">The EPUB 3 you want to convert to DAISY 3.

You may alternatively use the EPUB package document (the OPF-file) if your input is a unzipped/"exploded" version of an EPUB.</p>
        </p:documentation>
    </p:option>

    <p:option name="validation" select="'off'">
        <!-- defined in ../../../../../common-options.xpl -->
    </p:option>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <!-- directory used for temporary files -->
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DAISY 3</h2>
        </p:documentation>
    </p:option>

    <p:output port="validation-report" sequence="true">
        <!-- defined in ../../../../../common-options.xpl -->
        <p:pipe step="load" port="validation-report"/>
    </p:output>

    <p:output port="status" px:media-type="application/vnd.pipeline.status+xml">
        <!-- whether the validation of the input was successful -->
        <p:pipe step="status" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-store
        </p:documentation>
    </p:import>
    <p:import href="epub3-to-daisy3.xpl">
        <p:documentation>
            px:epub3-to-daisy3
        </p:documentation>
    </p:import>

    <px:epub-load version="3" name="load" px:message="Loading EPUB 3" px:progress="1/10">
        <p:with-option name="href" select="$epub"/>
        <p:with-option name="validation" select="not($validation='off')"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
    </px:epub-load>
    <p:sink/>

    <p:identity>
        <p:input port="source">
            <p:pipe step="load" port="validation-status"/>
        </p:input>
    </p:identity>
    <p:choose>
        <p:when test="/d:validation-status[@result='error']">
            <p:choose>
                <p:when test="$validation='abort'">
                    <p:identity px:message="The EPUB input is invalid. See validation report for more info."
                                px:message-severity="ERROR"/>
                </p:when>
                <p:otherwise>
                    <p:identity px:message="The EPUB input is invalid. See validation report for more info."
                                px:message-severity="WARN"/>
                </p:otherwise>
            </p:choose>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    <p:choose name="status" px:progress="9/10">
        <p:when test="/d:validation-status[@result='error'] and $validation='abort'">
            <p:output port="result"/>
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:output port="result"/>

            <px:epub3-to-daisy3 name="convert" px:message="Converting to DAISY 3" px:progress="8/9">
                <p:input port="source.fileset">
                    <p:pipe step="load" port="result.fileset"/>
                </p:input>
                <p:input port="source.in-memory">
                    <p:pipe step="load" port="result.in-memory"/>
                </p:input>
                <p:with-option name="output-dir" select="$output-dir"/>
            </px:epub3-to-daisy3>

            <px:fileset-store name="store" px:message="Storing DAISY 3" px:progress="1/9">
                <p:input port="in-memory.in">
                    <p:pipe step="convert" port="result.in-memory"/>
                </p:input>
            </px:fileset-store>

            <p:identity cx:depends-on="store">
                <p:input port="source">
                    <p:inline><d:validation-status result="ok"/></p:inline>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>

</p:declare-step>
