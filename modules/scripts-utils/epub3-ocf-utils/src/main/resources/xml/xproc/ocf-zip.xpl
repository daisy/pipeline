<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-ocf-zip" xmlns:p="http://www.w3.org/ns/xproc" xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" version="1.0" name="main">

    <p:input port="source"/>
    <p:output port="result"/>
    <p:option name="target" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl"/>

    <p:variable name="mimetype-target" select="resolve-uri('mimetype', base-uri(/*))">
        <p:pipe port="source" step="main"/>
    </p:variable>

    <px:mkdir name="mkdir">
        <p:with-option name="href" select="replace($target,'/[^/]+$','/')"/>
    </px:mkdir>

    <!-- Create a c:data with the mimetype content -->
    <p:string-replace match="text()" replace="normalize-space()">
        <p:input port="source">
            <p:inline>
                <c:data>application/epub+zip</c:data>
            </p:inline>
        </p:input>
    </p:string-replace>
    <p:store method="text" name="mimetype.store">
        <p:with-option name="href" select="$mimetype-target"/>
    </p:store>

    <px:fileset-add-entry first="true">
        <p:with-option name="href" select="$mimetype-target">
            <p:pipe port="result" step="mimetype.store"/>
        </p:with-option>
        <p:input port="source">
            <p:pipe port="source" step="main"/>
        </p:input>
    </px:fileset-add-entry>

    <px:zip-manifest-from-fileset/>
    <p:add-attribute match="c:entry[@name='mimetype']" attribute-name="compression-method" attribute-value="stored"/>
    <p:identity name="manifest" cx:depends-on="mkdir"/>

    <px:zip compression-method="deflated" cx:depends-on="mimetype.store">
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:input port="manifest">
            <p:pipe port="result" step="manifest"/>
        </p:input>
        <p:with-option name="href" select="$target"/>
    </px:zip>

</p:declare-step>
