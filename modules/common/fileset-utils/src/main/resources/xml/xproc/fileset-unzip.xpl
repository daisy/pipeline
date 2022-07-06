<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:letex="http://www.le-tex.de/namespace"
                type="px:fileset-unzip" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Unpack a ZIP archive.</p>
    </p:documentation>

    <p:option name="href" required="true"> <!-- anyURI (file) -->
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The value of the "href" option must be an IRI. It is a dynamic error if the document
            so identified does not exist or cannot be read. </p>
        </p:documentation>
    </p:option>
    <p:option name="unzipped-basedir"> <!-- anyURI (directory) -->
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>If specified, the ZIP archive is unpacked to this location. It is a dynamic error if
            the "store-to-disk" option is true and the value of the "unzipped-basedir" option does
            not identify a directory.</p>
        </p:documentation>
    </p:option>
    <p:option name="store-to-disk" cx:as="xs:boolean" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Whether to store the ZIP entries to disk, or only list them in the fileset manifest
            on the "result" port.</p>
        </p:documentation>
    </p:option>
    <p:option name="overwrite" cx:as="xs:boolean" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Whether existing directories and files will be overwritten when "store-to-disk" is
            set to true.</p>
        </p:documentation>
    </p:option>

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Fileset manifest that lists all the entries in the ZIP, optionally extracted to
            <code>unzipped-basedir</code>. If the entries were stored to disk, the
            <code>original-href</code> attributes have the same value as the <code>href</code>
            attributes. If the entries were not stored to disk, the <code>original-href</code>
            attributes point to the entries inside the ZIP file.</p>
        </p:documentation>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:error
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl">
        <p:documentation>
            px:unzip
            letex:unzip
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>

    <p:choose>
        <p:when test="$store-to-disk and not(p:value-available('unzipped-basedir'))">
            <px:error code="PZU001" message="When store-to-disk='true' then unzipped-basedir must also be defined"/>
        </p:when>
        <p:when test="$store-to-disk">
            <p:variable name="basedir" select="if (ends-with($unzipped-basedir,'/'))
                                               then $unzipped-basedir
                                               else concat($unzipped-basedir,'/')"/>
            <!-- unzip all files directly to disk -->
            <letex:unzip>
                <p:with-option name="zip" select="$href"/>
                <p:with-option name="dest-dir" select="$basedir"/>
                <p:with-option name="overwrite" select="if ($overwrite) then 'yes' else 'no'"/>
            </letex:unzip>
            <p:rename match="/*" new-name="d:fileset"/>
            <p:rename match="/*/*" new-name="d:file"/>
            <p:rename match="/*/*/@name" new-name="href"/>
            <p:viewport match="/*/*">
                <p:add-attribute match="/*" attribute-name="original-href">
                    <p:with-option name="attribute-value" select="resolve-uri(/*/@href,base-uri(.))"/>
                </p:add-attribute>
            </p:viewport>
        </p:when>
        <p:otherwise>
            <px:unzip>
                <p:with-option name="href" select="$href"/>
            </px:unzip>
            <p:rename match="/*" new-name="d:fileset"/>
            <p:choose>
                <p:when test="p:value-available('unzipped-basedir')">
                    <px:set-base-uri>
                        <p:with-option name="base-uri" select="if (ends-with($unzipped-basedir,'/'))
                                                               then $unzipped-basedir
                                                               else concat($unzipped-basedir,'/')"/>
                    </px:set-base-uri>
                </p:when>
                <p:otherwise>
                    <px:set-base-uri>
                        <p:with-option name="base-uri" select="concat($href,'!/')"/>
                    </px:set-base-uri>
                </p:otherwise>
            </p:choose>
            <p:add-xml-base/>
            <p:delete match="/*/@*[not(name()='xml:base')]"/>
            <p:delete match="/*/*[ends-with(@name,'/')]"/>
            <p:delete match="/*/*/@*[not(name()='name')]"/>
            <p:rename match="/*/*" new-name="d:file"/>
            <p:label-elements match="/*/d:file" attribute="href" label="@name"/>
            <p:label-elements match="/*/d:file" attribute="original-href">
                <p:with-option name="label" select="concat('resolve-uri(@name,&quot;',concat($href,'!/'),'&quot;)')"/>
            </p:label-elements>
            <p:delete match="/*/*/@name"/>
        </p:otherwise>
    </p:choose>

</p:declare-step>
