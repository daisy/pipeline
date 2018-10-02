<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:opf="http://www.idpf.org/2007/opf"
    px:input-filesets="epub3"
    px:output-filesets="daisy202"
    type="px:epub3-to-daisy202" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:pxp="http://exproc.org/proposed/steps" xpath-version="2.0">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">EPUB 3 to DAISY 2.02</h1>
        <p px:role="desc">Transforms an EPUB 3 publication into DAISY 2.02.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/epub3-to-daisy202">
            Online documentation
        </a>
    </p:documentation>

    <p:option name="epub" required="true" px:type="anyFileURI" px:media-type="application/epub+zip application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">EPUB 3 Publication</h2>
            <p px:role="desc" xml:space="preserve">The EPUB 3 you want to convert to DAISY 2.02.

You may alternatively use the EPUB package document (the OPF-file) if your input is a unzipped/"exploded" version of an EPUB.</p>
        </p:documentation>
    </p:option>

    <p:option name="validation" required="false" px:type="string" select="'off'">
        <p:pipeinfo>
            <px:data-type>
                <choice xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0">
                    <value>off</value>
                    <a:documentation xml:lang="en">No validation</a:documentation>
                    <value>report</value>
                    <a:documentation xml:lang="en">Report validation issues</a:documentation>
                    <value>abort</value>
                    <a:documentation xml:lang="en">Abort on validation issues</a:documentation>
                </choice>
            </px:data-type>
        </p:pipeinfo>
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Validation</h2>
            <p px:role="desc">Whether to abort on validation issues.</p>
        </p:documentation>
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

    <p:output port="validation-report" sequence="true" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Input validation report</h1>
        </p:documentation>
        <p:pipe step="load" port="validation-report"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Input validation status</h1>
            <p px:role="desc" xml:space="preserve">An XML document describing, briefly, whether the input validation was successful.

[More details on the file format](http://daisy.github.io/pipeline/ValidationStatusXML).</p>
        </p:documentation>
        <p:pipe step="status" port="result"/>
    </p:output>
    
    <p:import href="step/epub3-to-daisy202.load.xpl"/>
    <p:import href="step/epub3-to-daisy202.convert.xpl"/>
    <p:import href="step/epub3-to-daisy202.store.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:variable name="epub-href" select="resolve-uri($epub,base-uri(/*))">
        <p:inline>
            <irrelevant/>
        </p:inline>
    </p:variable>

    <px:epub3-to-daisy202.load name="load">
        <p:with-option name="epub" select="$epub-href"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
        <p:with-option name="validation" select="$validation"/>
    </px:epub3-to-daisy202.load>

    <p:identity>
        <p:input port="source">
            <p:pipe step="load" port="status"/>
        </p:input>
    </p:identity>
    <p:choose name="status">
        <p:when test="/d:validation-status[@result='error']">
            <p:output port="result"/>
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:output port="result">
                <p:pipe step="try-convert" port="status"/>
            </p:output>
            <p:try name="try-convert">
                <p:group>
                    <p:output port="status">
                        <p:pipe step="load" port="status"/>
                    </p:output>
                    
                    <px:epub3-to-daisy202-convert name="convert">
                        <p:input port="fileset.in">
                            <p:pipe port="fileset.out" step="load"/>
                        </p:input>
                        <p:input port="in-memory.in">
                            <p:pipe port="in-memory.out" step="load"/>
                        </p:input>
                    </px:epub3-to-daisy202-convert>
        
                    <px:epub3-to-daisy202.store name="store">
                        <p:input port="fileset.in">
                            <p:pipe step="convert" port="fileset.out"/>
                        </p:input>
                        <p:input port="in-memory.in">
                            <p:pipe step="convert" port="in-memory.out"/>
                        </p:input>
                        <p:with-option name="output-dir" select="$output-dir"/>
                    </px:epub3-to-daisy202.store>
                    
                </p:group>
                <p:catch name="catch">
                    <p:output port="status">
                        <p:inline>
                            <d:validation-status result="error"/>
                        </p:inline>
                    </p:output>
                    <p:identity>
                        <p:input port="source">
                            <p:pipe step="catch" port="error"/>
                        </p:input>
                    </p:identity>
                    <p:choose>
                        <!--
                            catching only errors of a certain type (PED01 PED02) will be easier with XProc 3.0
                        -->
                        <p:when test="/c:errors/c:error/@code=('PED01','PED02')">
                            <px:message message="The EPUB 3 input can not be processed: $1">
                                <p:with-option name="param1" select="/c:errors/c:error/@code=('PED01','PED02')[1]/message"/>
                            </px:message>
                            <px:message message="Aborting..."/>
                        </p:when>
                        <p:otherwise>
                            <p:wrap wrapper="px:cause" match="/*" name="cause"/>
                            <p:error code="runtime">
                                <p:input port="source">
                                    <p:pipe step="cause" port="result"/>
                                </p:input>
                            </p:error>
                        </p:otherwise>
                    </p:choose>
                    <p:sink/>
                </p:catch>
            </p:try>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
