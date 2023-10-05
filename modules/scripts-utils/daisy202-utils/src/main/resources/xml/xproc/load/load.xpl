<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:daisy202-load">

    <p:documentation>
        <p>Load a DAISY 2.02 fileset based on its NCC.</p>
    </p:documentation>

    <p:serialization port="fileset.out" indent="true"/>

    <p:option name="ncc" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>URI to input NCC.</p>
        </p:documentation>
    </p:option>

    <p:output port="fileset.out" primary="true">
        <p:documentation>A fileset containing references to all the files in the DAISY 2.02 fileset
            and any resources they reference (images etc.). The base URI of each document points to
            the original file, while the href can change during conversions to reflect changes in
            the path and filename of the resulting file. The SMIL files in the fileset are ordered
            according the the "flow" (reading order).</p:documentation>
        <p:pipe port="fileset.out" step="wrapper"/>
    </p:output>

    <p:output port="in-memory.out" sequence="true">
        <p:documentation>The NCC file serialized as XHTML.</p:documentation>
        <p:pipe port="in-memory.out" step="wrapper"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:normalize-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-create
            px:fileset-add-entry
            px:fileset-load
            px:fileset-join
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl">
        <p:documentation>
            px:mediatype-detect
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/smil-utils/library.xpl">
        <p:documentation>
            px:smil-to-audio-fileset
            px:smil-to-text-fileset
        </p:documentation>
    </p:import>
    <cx:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xsl" type="application/xslt+xml">
        <p:documentation>
            pf:html-encoding
        </p:documentation>
    </cx:import>

    <px:normalize-uri name="ncc">
        <p:with-option name="href" select="$ncc"/>
    </px:normalize-uri>

    <p:group name="wrapper">
        <p:output port="fileset.out" primary="true">
            <p:pipe port="result" step="fileset"/>
        </p:output>
        <p:output port="in-memory.out" sequence="true">
            <p:pipe port="result" step="in-memory"/>
        </p:output>
        <p:variable name="href" select="/c:result/string()">
            <p:pipe step="ncc" port="normalized"/>
        </p:variable>
        <px:message severity="DEBUG">
            <p:with-option name="message" select="concat('loading NCC: ',$href)"/>
        </px:message>
        <p:sink/>

        <px:fileset-create/>
        <px:fileset-add-entry media-type="application/xhtml+xml">
            <p:with-option name="href" select="$href"/>
        </px:fileset-add-entry>
        <px:fileset-load name="in-memory.ncc" detect-serialization-properties="true"/>
        <px:message severity="DEBUG"
            message="Making an ordered list of SMIL-files referenced from the NCC according to the flow (reading order)"/>
        <p:xslt>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="ncc-to-smil-fileset.xsl"/>
            </p:input>
        </p:xslt>

        <px:message severity="DEBUG" message="Loading all SMIL files"/>
        <px:fileset-load detect-serialization-properties="true" name="in-memory.smil">
            <p:input port="in-memory">
                <p:empty/>
            </p:input>
        </px:fileset-load>

        <p:for-each px:message="Listing audio files referenced from the SMIL files" px:message-severity="DEBUG">
            <px:smil-to-audio-fileset/>
        </p:for-each>
        <px:fileset-join name="fileset.audio"/>
        <p:sink/>

        <p:group name="html" px:message="Loading all HTML-files" px:message-severity="DEBUG">
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="load" port="result"/>
            </p:output>
            <p:for-each px:message="Listing HTML files referenced from the SMIL files" px:message-severity="DEBUG">
                <p:iteration-source>
                    <p:pipe step="in-memory.smil" port="result"/>
                </p:iteration-source>
                <px:smil-to-text-fileset/>
            </p:for-each>
            <px:fileset-join/>
            <!-- exclude NCC -->
            <p:delete>
                <p:with-option name="match" select="concat('d:file[resolve-uri(@href,base-uri())=&quot;',$href,'&quot;]')"/>
            </p:delete>
            <!-- add media-type -->
            <p:add-attribute match="d:file[matches(lower-case(@href),'\.x?html$')]"
                             attribute-name="media-type" attribute-value="application/xhtml+xml"/>
            <!-- determine encoding -->
            <px:fileset-load media-types="application/xhtml+xml" detect-serialization-properties="true" name="load"/>
            <p:for-each name="html-encodings">
                <p:output port="result"/>
                <p:sink/>
                <px:fileset-add-entry>
                    <p:input port="entry">
                        <p:pipe step="html-encodings" port="current"/>
                    </p:input>
                    <p:with-param port="file-attributes" name="encoding" select="pf:html-encoding(/)">
                        <p:pipe step="html-encodings" port="current"/>
                    </p:with-param>
                </px:fileset-add-entry>
            </p:for-each>
            <px:fileset-join>
                <p:input port="source">
                    <p:pipe step="load" port="result.fileset"/>
                    <p:pipe step="html-encodings" port="result"/>
                </p:input>
            </px:fileset-join>
        </p:group>

        <px:html-load name="html-load"
                      px:message-severity="DEBUG" px:message="Listing all resources referenced from the HTML files">
            <p:input port="source.in-memory">
                <p:pipe step="html" port="in-memory"/>
            </p:input>
        </px:html-load>

        <!-- omit HTML files except those referenced from iframes -->
        <p:delete match="d:file[@media-type='application/xhtml+xml' and not(@kind='content')]"
                  name="fileset.html-resources"/>

        <p:identity name="in-memory">
            <p:input port="source">
                <p:pipe port="result" step="in-memory.ncc"/>
                <p:pipe port="result" step="in-memory.smil"/>
                <p:pipe step="html-load" port="result.in-memory"/>
            </p:input>
        </p:identity>
        <p:sink/>

        <p:add-attribute match="d:file" attribute-name="encoding" name="fileset.ncc">
            <p:input port="source">
                <p:pipe step="in-memory.ncc" port="result.fileset"/>
            </p:input>
            <p:with-option name="attribute-value" select="pf:html-encoding(/)">
                <p:pipe step="in-memory.ncc" port="result"/>
            </p:with-option>
        </p:add-attribute>
        <p:sink/>

        <px:fileset-join>
            <p:input port="source">
                <p:pipe step="fileset.ncc" port="result"/>
                <p:pipe step="in-memory.smil" port="result.fileset"/>
                <p:pipe port="result" step="fileset.audio"/>
                <p:pipe step="html" port="fileset"/>
                <p:pipe port="result" step="fileset.html-resources"/>
            </p:input>
        </px:fileset-join>
        <px:mediatype-detect>
            <p:input port="in-memory">
                <p:pipe port="result" step="in-memory"/>
            </p:input>
        </px:mediatype-detect>
        <p:label-elements match="d:file[@media-type='application/xhtml+xml'][not(@media-version)]"
                          attribute="media-version"
                          label="'4.0'"/>
        <p:identity name="fileset"/>
    </p:group>
</p:declare-step>
