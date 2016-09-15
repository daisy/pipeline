<p:declare-step version="1.0" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" exclude-inline-prefixes="px">

    <p:import href="../../epub3-ocf-utils/xproc/ocf-finalize.xpl"/>
    <p:import href="compare.xpl"/>

    <p:group name="test-default">
        <px:epub3-ocf-finalize name="test-default.actual">
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/tmp/dir/">
                        <d:file href="Content/package.opf"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:epub3-ocf-finalize>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <d:fileset xml:base="file:/tmp/dir/">
                        <d:file href="mimetype"/>
                        <d:file href="Content/package.opf"/>
                        <d:file href="META-INF/container.xml"
                            media-type="application/oebps-package+xml"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:compare>
        <px:compare>
            <p:log port="result"/>
            <p:input port="source">
                <p:pipe port="container" step="test-default.actual"/>
            </p:input>
            <p:input port="alternate">
                <p:inline>
                    <container xmlns="urn:oasis:names:tc:opendocument:xmlns:container" version="1.0">
                        <rootfiles>
                            <rootfile full-path="Content/package.opf"
                                media-type="application/oebps-package+xml"/>
                        </rootfiles>
                    </container>
                </p:inline>
            </p:input>
        </px:compare>
    </p:group>
    <p:group name="test-multiple-opf">
        <px:epub3-ocf-finalize name="test-multiple-opf.actual">
            <p:input port="source">
                <p:inline>
                    <d:fileset xml:base="file:/tmp/dir/">
                        <d:file href="Content/package.opf"/>
                        <d:file href="Alternate/other.opf"/>
                    </d:fileset>
                </p:inline>
            </p:input>
        </px:epub3-ocf-finalize>
        <px:compare>
            <p:log port="result"/>
            <p:input port="source">
                <p:pipe port="container" step="test-multiple-opf.actual"/>
            </p:input>
            <p:input port="alternate">
                <p:inline>
                    <container xmlns="urn:oasis:names:tc:opendocument:xmlns:container" version="1.0">
                        <rootfiles>
                            <rootfile full-path="Content/package.opf"
                                media-type="application/oebps-package+xml"/>
                            <rootfile full-path="Alternate/other.opf"
                                media-type="application/oebps-package+xml"/>
                        </rootfiles>
                    </container>
                </p:inline>
            </p:input>
        </px:compare>
    </p:group>
    <p:group name="test-no-opf">
        <p:try>
            <p:group>
                <px:epub3-ocf-finalize name="test-no-opf.actual">
                    <p:input port="source">
                        <p:inline>
                            <d:fileset xml:base="file:/tmp/dir/"> </d:fileset>
                        </p:inline>
                    </p:input>
                </px:epub3-ocf-finalize>
            </p:group>
            <p:catch name="catch">
                <p:identity>
                    <p:input port="source">
                        <p:pipe port="error" step="catch"/>
                    </p:input>
                </p:identity>
            </p:catch>
        </p:try>
        <px:compare>
            <p:log port="result"/>
            <p:input port="alternate">
                <p:inline>
                    <c:errors/>
                </p:inline>
            </p:input>
        </px:compare>
    </p:group>
</p:declare-step>
