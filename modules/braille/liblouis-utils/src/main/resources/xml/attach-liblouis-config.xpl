<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="pxi:attach-liblouis-config" name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:louis="http://liblouis.org/liblouis"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:input port="source" sequence="true"/>
    <p:output port="result" sequence="true"/>
    
    <p:option name="directory" required="true"/>
    
    <p:import href="utils/fileset-add-tempfile.xpl"/>
    <p:import href="utils/select-by-position.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    
    <px:fileset-create name="directory">
        <p:with-option name="base" select="$directory">
            <p:empty/>
        </p:with-option>
    </px:fileset-create>
    <p:sink/>
    
    <p:group name="liblouis-ini-file">
        <p:output port="result"/>
        <px:fileset-create>
            <p:with-option name="base" select="resolve-uri('../lbu_files/formatter/')">
                <p:inline>
                    <irrelevant/>
                </p:inline>
            </p:with-option>
        </px:fileset-create>
        <px:fileset-add-entry href="liblouisutdml.ini"/>
        <px:fileset-add-entry href="braille-patterns.cti"/>
        <px:fileset-add-entry href="nabcc.dis"/>
        <px:fileset-add-entry href="whitespace.cti"/>
        <px:fileset-add-entry href="pagenum.cti"/>
        <p:for-each>
            <p:iteration-source select="/d:fileset/d:file"/>
            <px:copy-resource fail-on-error="true">
                <p:with-option name="href" select="/*/resolve-uri(@href, base-uri(.))"/>
                <p:with-option name="target" select="/*/resolve-uri(@href, $directory)"/>
            </px:copy-resource>
        </p:for-each>
        <p:sink/>
        <px:fileset-add-entry href="liblouisutdml.ini">
            <p:input port="source">
                <p:pipe step="directory" port="result"/>
            </p:input>
        </px:fileset-add-entry>
    </p:group>
    <p:sink/>
    
    <pxi:fileset-add-tempfile name="styles-directory" suffix=".cfg">
        <p:input port="source">
            <p:inline><louis:config-file># --------------------------------------------------------------------------------------------------
# Default styles
# --------------------------------------------------------------------------------------------------

style contentsheader
    leftMargin 0
    rightMargin 0
    firstLineIndent 0
    format leftJustified
    braillePageNumberFormat blank

style preformatted
    firstLineIndent 0
    format leftJustified
    skipNumberLines yes
</louis:config-file></p:inline>
        </p:input>
        <p:input port="directory">
            <p:pipe step="liblouis-ini-file" port="result"/>
        </p:input>
    </pxi:fileset-add-tempfile>
    <p:sink/>
    
    <pxi:fileset-add-tempfile name="semantics-directory" suffix=".sem">
        <p:input port="source">
            <p:inline><louis:semantic-file># --------------------------------------------------------------------------------------------------
# Default semantics
# --------------------------------------------------------------------------------------------------

contentsheader &amp;xpath(//louis:toc)
none           &amp;xpath(//louis:include)
none           &amp;xpath(//louis:result)
preformatted   &amp;xpath(//louis:line)
preformatted   &amp;xpath(//louis:border)
pagenum        &amp;xpath(//louis:print-page)
softreturn     &amp;xpath(//louis:line-break)
skip           &amp;xpath(//louis:page-layout)
skip           &amp;xpath(//louis:styles)
skip           &amp;xpath(//louis:semantics)
</louis:semantic-file></p:inline>
        </p:input>
        <p:input port="directory">
            <p:pipe step="directory" port="result"/>
        </p:input>
    </pxi:fileset-add-tempfile>
    <p:sink/>
    
    <p:for-each name="attach-liblouis-styles">
        <p:iteration-source>
            <p:pipe step="main" port="source"/>
        </p:iteration-source>
        <pxi:select-by-position name="select">
            <p:input port="source">
                <p:pipe step="main" port="source"/>
            </p:input>
            <p:with-option name="position" select="p:iteration-position()">
                <p:empty/>
            </p:with-option>
        </pxi:select-by-position>
        <p:xslt name="generate-liblouis-styles">
            <p:input port="source">
                <p:pipe step="select" port="matched"/>
                <p:pipe step="select" port="not-matched"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="generate-liblouis-styles.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:sink/>
        <p:choose name="base-directory">
            <p:xpath-context>
                <p:pipe step="attach-liblouis-styles" port="current"/>
            </p:xpath-context>
            <p:when test="/louis:toc">
                <p:output port="result" primary="true"/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="directory" port="result"/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:output port="result" primary="true"/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="styles-directory" port="result"/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
        <p:sink/>
        <pxi:fileset-add-tempfile suffix=".cfg">
            <p:input port="source">
                <p:pipe step="generate-liblouis-styles" port="secondary"/>
            </p:input>
            <p:input port="directory">
                <p:pipe step="base-directory" port="result"/>
            </p:input>
        </pxi:fileset-add-tempfile>
        <p:wrap name="liblouis-styles" match="/*" wrapper="louis:styles"/>
        <p:sink/>
        <p:insert match="/*" position="last-child">
            <p:input port="source">
                <p:pipe step="generate-liblouis-styles" port="result"/>
            </p:input>
            <p:input port="insertion">
                <p:pipe step="liblouis-styles" port="result"/>
            </p:input>
        </p:insert>
    </p:for-each>
    
    <p:for-each name="attach-liblouis-semantics">
        <p:xslt name="generate-liblouis-semantics">
            <p:input port="stylesheet">
                <p:document href="generate-liblouis-semantics.xsl"/>
            </p:input>
            <p:input port="parameters" select="/*/louis:page-layout/c:param-set">
                <p:pipe step="attach-liblouis-semantics" port="current"/>
            </p:input>
        </p:xslt>
        <p:sink/>
        <p:choose name="base-directory">
            <p:xpath-context>
                <p:pipe step="attach-liblouis-semantics" port="current"/>
            </p:xpath-context>
            <p:when test="/louis:toc">
                <p:output port="result" primary="true"/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="directory" port="result"/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:output port="result" primary="true"/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="semantics-directory" port="result"/>
                    </p:input>
                </p:identity>
            </p:otherwise>
        </p:choose>
        <p:sink/>
        <pxi:fileset-add-tempfile suffix=".sem">
            <p:input port="source">
                <p:pipe step="generate-liblouis-semantics" port="result"/>
            </p:input>
            <p:input port="directory">
                <p:pipe step="base-directory" port="result"/>
            </p:input>
        </pxi:fileset-add-tempfile>
        <p:wrap name="liblouis-semantics" match="/*" wrapper="louis:semantics"/>
        <p:sink/>
        <p:insert match="/*" position="last-child">
            <p:input port="source">
                <p:pipe step="attach-liblouis-semantics" port="current"/>
            </p:input>
            <p:input port="insertion">
                <p:pipe step="liblouis-semantics" port="result"/>
            </p:input>
        </p:insert>
    </p:for-each>
    
</p:declare-step>
