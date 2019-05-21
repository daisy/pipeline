<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-epub3-store.step" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:l="http://xproc.org/library" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:html="http://www.w3.org/1999/xhtml" xmlns:cx="http://xmlcalabash.com/ns/extensions"
    xmlns:dc="http://purl.org/dc/elements/1.1/">

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true">
        <p:empty/>
    </p:input>
    <p:input port="report.in" sequence="true">
        <p:empty/>
    </p:input>
    <p:input port="status.in">
        <p:inline>
            <d:validation-status result="ok"/>
        </p:inline>
    </p:input>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="fileset.out" step="choose"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="in-memory.out" step="choose"/>
    </p:output>
    <p:output port="report.out" sequence="true">
        <p:pipe port="report.in" step="main"/>
        <p:pipe port="report.out" step="choose"/>
    </p:output>
    <p:output port="status.out">
        <p:pipe port="result" step="status"/>
    </p:output>

    <p:option name="fail-on-error" required="true"/>
    <p:option name="output-dir" required="true"/>

    <p:import href="validation-status.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl"/>

    <px:assert message="'fail-on-error' should be either 'true' or 'false'. was: '$1'. will default to 'true'.">
        <p:with-option name="param1" select="$fail-on-error"/>
        <p:with-option name="test" select="$fail-on-error = ('true','false')"/>
    </px:assert>

    <p:choose name="choose">
        <p:xpath-context>
            <p:pipe port="status.in" step="main"/>
        </p:xpath-context>
        <p:when test="/*/@result='ok' or $fail-on-error = 'false'">
            <p:output port="fileset.out" primary="true">
                <p:pipe port="result" step="epub3-store.step.result.fileset"/>
            </p:output>
            <p:output port="in-memory.out" sequence="true">
                <p:empty/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:empty/>
            </p:output>










            <p:group name="epub3-store.step.store-epub3">
                <!-- TODO: replace this p:group with px:epub3-store when px:set-doctype is fixed in the next pipeline 2 version -->

                <p:output port="result" primary="false">
                    <p:pipe port="result" step="epub3-store.step.store-epub3.zip"/>
                </p:output>

                <p:delete match="/*/d:file/@doctype" name="epub3-store.step.store-epub3.delete-doctype"/>
                <p:add-attribute match="/*/d:file[@indent='true']" attribute-name="indent" attribute-value="false" name="epub3-store.step.store-epub3.set-indent-false">
                    <!-- temporary workaround until https://github.com/daisy/pipeline-modules-common/issues/69 is fixed -->
                </p:add-attribute>
                <p:add-attribute match="/*/d:file[ends-with(@media-type,'+xml') or ends-with(@media-type,'/xml')]" attribute-name="encoding" attribute-value="us-ascii" name="epub3-store.step.store-epub3.set-ascii-encoding"/>
                <px:fileset-store name="epub3-store.step.store-epub3.fileset-store">
                    <p:input port="in-memory.in">
                        <p:pipe port="in-memory.in" step="main"/>
                    </p:input>
                </px:fileset-store>
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="fileset.out" step="epub3-store.step.store-epub3.fileset-store"/>
                    </p:input>
                </p:identity>
                <p:viewport match="d:file[@media-type='application/xhtml+xml']" name="epub3-store.step.store-epub3.viewport-store-doctype">
                    <p:variable name="href" select="resolve-uri(/*/@href,base-uri(/*))"/>
                    <p:variable name="doctype" select="'&lt;!DOCTYPE html&gt;'"/>
                    <px:set-doctype name="epub3-store.step.store-epub3.viewport-store-doctype.set-doctype">
                        <p:with-option name="doctype" select="$doctype"/>
                        <p:with-option name="href" select="$href"/>
                    </px:set-doctype>
                    <p:add-attribute match="/*" attribute-name="doctype" name="epub3-store.step.store-epub3.viewport-store-doctype.set-doctype-attribute-in-fileset" cx:depends-on="epub3-store.step.store-epub3.viewport-store-doctype.set-doctype">
                        <p:input port="source">
                            <p:pipe port="current" step="epub3-store.step.store-epub3.viewport-store-doctype"/>
                        </p:input>
                        <p:with-option name="attribute-value" select="$doctype"/>
                    </p:add-attribute>
                </p:viewport>
                <p:viewport match="d:file[ends-with(@media-type,'+xml') or ends-with(@media-type,'/xml')]" name="epub3-store.step.store-epub3.viewport-store-xml-declaration">
                    <p:variable name="href" select="resolve-uri(/*/@href,base-uri(/*))"/>
                    <p:variable name="xml-declaration" select="'&lt;?xml version=&quot;1.0&quot; encoding=&quot;utf-8&quot;?&gt;'"/>
                    <px:set-xml-declaration name="epub3-store.step.store-epub3.viewport-store-xml-declaration.set-xml-declaration">
                        <p:with-option name="xml-declaration" select="$xml-declaration"/>
                        <p:with-option name="href" select="$href"/>
                    </px:set-xml-declaration>
                    <p:add-attribute match="/*" attribute-name="xml-declaration" name="epub3-store.step.store-epub3.viewport-store-xml-declaration.set-xml-declaration-attribute-in-fileset">
                        <p:input port="source">
                            <p:pipe port="current" step="epub3-store.step.store-epub3.viewport-store-xml-declaration"/>
                        </p:input>
                        <p:with-option name="attribute-value" select="$xml-declaration">
                            <p:pipe port="result" step="epub3-store.step.store-epub3.viewport-store-xml-declaration.set-xml-declaration"/>
                        </p:with-option>
                    </p:add-attribute>
                </p:viewport>

                <px:epub3-ocf-zip name="epub3-store.step.store-epub3.zip" cx:depends-on="epub3-store.step.store-epub3.fileset-store">
                    <p:with-option name="target" select="concat($output-dir,/*/text(),'.epub')">
                        <p:pipe port="result" step="epub3-store.step.metadata.identifier"/>
                    </p:with-option>
                </px:epub3-ocf-zip>
            </p:group>
            <!--<px:epub3-store name="epub3-store.step.store-epub3">
                <p:input port="in-memory.in">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
                <p:with-option name="href" select="concat($output-dir,/*/@content,'.epub')">
                    <p:pipe port="result" step="metadata.identifier"/>
                </p:with-option>
            </px:epub3-store>-->

            <px:fileset-create name="epub3-store.step.create-epub-fileset">
                <p:with-option name="base" select="$output-dir">
                    <p:pipe port="result" step="epub3-store.step.store-epub3"/>
                </p:with-option>
            </px:fileset-create>
            <px:fileset-add-entry media-type="application/epub+zip" name="epub3-store.step.add-epub-to-fileset">
                <p:with-option name="href" select="concat(/*/text(),'.epub')">
                    <p:pipe port="result" step="epub3-store.step.metadata.identifier"/>
                </p:with-option>
            </px:fileset-add-entry>
            <p:identity name="epub3-store.step.result.fileset"/>

            <!-- get metadata -->
            <px:fileset-load media-types="application/oebps-package+xml" name="epub3-store.step.metadata.load-opf">
                <p:input port="fileset">
                    <p:pipe port="fileset.in" step="main"/>
                </p:input>
                <p:input port="in-memory">
                    <p:pipe port="in-memory.in" step="main"/>
                </p:input>
            </px:fileset-load>
            <px:assert test-count-min="1" test-count-max="1" message="There must be exactly one Package Document in the EPUB." error-code="NORDICDTBOOKEPUB011"/>
            <p:filter select="//dc:identifier[not(@refines)]" name="epub3-store.step.metadata.filter-dc-identifier"/>
            <px:assert message="The EPUB Package Document (the OPF file) must have a 'dc:identifier' element" test-count-min="1" error-code="NORDICDTBOOKEPUB012"/>
            <p:split-sequence test="position() = 1" name="epub3-store.step.metadata.get-first-dc-identifier"/>
            <p:identity name="epub3-store.step.metadata.identifier"/>
            <p:sink/>










        </p:when>
        <p:otherwise>
            <p:output port="fileset.out" primary="true"/>
            <p:output port="in-memory.out" sequence="true">
                <p:pipe port="fileset.in" step="main"/>
            </p:output>
            <p:output port="report.out" sequence="true">
                <p:empty/>
            </p:output>

            <p:identity/>
        </p:otherwise>
    </p:choose>

    <p:choose name="status">
        <p:xpath-context>
            <p:pipe port="status.in" step="main"/>
        </p:xpath-context>
        <p:when test="/*/@result='ok' and $fail-on-error='true'">
            <p:output port="result"/>
            <px:nordic-validation-status>
                <p:input port="source">
                    <p:pipe port="report.out" step="choose"/>
                </p:input>
            </px:nordic-validation-status>
        </p:when>
        <p:otherwise>
            <p:output port="result"/>
            <p:identity>
                <p:input port="source">
                    <p:pipe port="status.in" step="main"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>

</p:declare-step>
