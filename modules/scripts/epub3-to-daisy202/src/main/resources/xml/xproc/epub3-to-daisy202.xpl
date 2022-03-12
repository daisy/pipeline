<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:epub3-to-daisy202.script" name="main"
                px:input-filesets="epub3"
                px:output-filesets="daisy202"
                xpath-version="2.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">EPUB 3 to DAISY 2.02</h1>
        <p px:role="desc">Transforms an EPUB 3 publication into DAISY 2.02.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/epub3-to-daisy202/">
            Online documentation
        </a>
    </p:documentation>

    <p:option name="epub" required="true" px:type="anyFileURI" px:media-type="application/epub+zip application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB 3 Publication</h2>
            <p px:role="desc" xml:space="preserve">The EPUB 3 you want to convert to DAISY 2.02.

You may alternatively use the "mimetype" document if your input is a unzipped/"exploded" version of an EPUB.</p>
        </p:documentation>
    </p:option>

    <p:option name="validation" select="'off'">
        <!-- defined in common-options.xpl -->
    </p:option>

    <p:option name="temp-dir" required="true" px:output="temp" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Temporary directory</h2>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DAISY 2.02</h2>
        </p:documentation>
    </p:option>

    <p:output port="validation-report" sequence="true">
        <!-- defined in common-options.xpl -->
        <p:pipe step="load" port="validation-report"/>
    </p:output>

    <p:output port="validation-status">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Input validation status</h1>
            <p px:role="desc" xml:space="preserve">An XML document describing, briefly, whether the input validation was successful.

[More details on the file format](http://daisy.github.io/pipeline/StatusXML).</p>
        </p:documentation>
        <p:pipe step="status" port="result"/>
    </p:output>
    
    <p:import href="step/epub3-to-daisy202.convert.xpl">
        <p:documentation>
            px:epub3-to-daisy202
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
            px:error
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-store
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub-load
        </p:documentation>
    </p:import>

    <p:variable name="epub-href" select="resolve-uri($epub,base-uri(/*))">
        <p:inline>
            <irrelevant/>
        </p:inline>
    </p:variable>

    <px:epub-load name="load" version="3" store-to-disk="true" px:progress="0.1" px:message="Loading EPUB 3">
        <p:with-option name="href" select="$epub-href"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
        <p:with-option name="validation" select="$validation"/>
    </px:epub-load>
    
    <p:identity>
        <p:input port="source">
            <p:pipe step="load" port="validation-status"/>
        </p:input>
    </p:identity>
    <p:choose name="status" px:progress="0.9">
        <p:when test="/d:validation-status[@result='error']">
            <p:output port="result"/>
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:output port="result">
                <p:pipe step="try-convert" port="status"/>
            </p:output>
            <p:try name="try-convert" px:progress="1">
                <p:group>
                    <p:output port="status"/>
                    
                    <px:epub3-to-daisy202 name="convert" px:progress="9/10">
                        <p:input port="source.fileset">
                            <p:pipe step="load" port="result.fileset"/>
                        </p:input>
                        <p:input port="source.in-memory">
                            <p:pipe step="load" port="result.in-memory"/>
                        </p:input>
                        <p:with-option name="output-dir" select="$output-dir"/>
                    </px:epub3-to-daisy202>

                    <px:fileset-store name="store" fail-on-error="true" px:progress="1/10" px:message="Storing DAISY 2.02">
                        <p:input port="fileset.in">
                            <p:pipe step="convert" port="result.fileset"/>
                        </p:input>
                        <p:input port="in-memory.in">
                            <p:pipe step="convert" port="result.in-memory"/>
                        </p:input>
                    </px:fileset-store>

                    <p:identity cx:depends-on="store">
                        <p:input port="source">
                            <p:pipe step="load" port="validation-status"/>
                        </p:input>
                    </p:identity>
                </p:group>
                <p:catch name="catch">
                    <p:output port="status"/>
                    <p:identity>
                        <p:input port="source">
                            <p:inline>
                                <d:validation-status result="error"/>
                            </p:inline>
                        </p:input>
                    </p:identity>
                    <p:choose>
                        <p:xpath-context>
                            <p:pipe step="catch" port="error"/>
                        </p:xpath-context>
                        <!-- catch only errors of type PED01/PED02 -->
                        <p:when test="/c:errors/c:error/@code=('PED01','PED02')">
                            <px:message message="The EPUB 3 input can not be processed: $1">
                                <p:with-option name="param1" select="/c:errors/c:error/@code=('PED01','PED02')[1]/message">
                                    <p:pipe step="catch" port="error"/>
                                </p:with-option>
                            </px:message>
                            <px:message message="Aborting..."/>
                        </p:when>
                        <p:otherwise>
                            <!-- re-throw error -->
                            <px:error>
                                <p:input port="error">
                                    <p:pipe step="catch" port="error"/>
                                </p:input>
                            </px:error>
                        </p:otherwise>
                    </p:choose>
                </p:catch>
            </p:try>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
