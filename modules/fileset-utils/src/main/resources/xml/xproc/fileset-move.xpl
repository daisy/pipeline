<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" type="px:fileset-move" name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:d="http://www.daisy.org/ns/pipeline/data">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Move a fileset to a new location</p>
        <p>Fails if the fileset contains files outside of the base directory. No files are
        physically moved, that is done upon calling px:fileset-store and px:fileset-delete.</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input fileset</p>
        </p:documentation>
        <p:empty/>
    </p:input>

    <p:output port="result.fileset" primary="true">
        <p:pipe step="copy" port="result.fileset"/>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The output fileset at the new location.</p>
            <p>The xml:base is changed to "target". The hrefs are not updated, unless the "flatten"
            option is set, in which case they are reduced to the file name. The base URI of the
            in-memory documents are changed accordingly, and "original-href"-attributes are added
            for files that exist on disk.</p>
        </p:documentation>
        <p:pipe step="copy" port="result.in-memory"/>
    </p:output>
    <p:output port="mapping">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A <code>d:fileset</code> document that contains the mapping from the source files
            (<code>@original-href</code>)to the moved files (<code>@href</code>).</p>
            <p>Pass this output to px:fileset-delete after the "result" output has been passed to
            px:fileset-store.</p>
        </p:documentation>
        <p:pipe step="mapping" port="result"/>
    </p:output>

    <p:option name="target" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The target directory.</p>
        </p:documentation>
    </p:option>
    <p:option name="flatten" required="false" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Move all files to a single directory.</p>
        </p:documentation>
    </p:option>
    <p:option name="prefix" required="false" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Prefix to add before file names.</p>
            <p>Only if "flatten" option is set.</p>
        </p:documentation>
    </p:option>

    <p:import href="fileset-join.xpl"/>
    <p:import href="fileset-copy.xpl"/>

    <p:documentation>Copy the fileset</p:documentation>
    <px:fileset-copy name="copy">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
        <p:with-option name="target" select="$target"/>
        <p:with-option name="flatten" select="$flatten"/>
        <p:with-option name="prefix" select="$prefix"/>
    </px:fileset-copy>

    <p:documentation>Mark original files for removal</p:documentation>
    <p:delete match="d:file[not(@original-href)]|
                     d:file/@*[not(name()=('href','original-href'))]"/>
    <p:add-attribute match="d:file" attribute-name="to-delete" attribute-value="true" name="delete"/>
    <p:sink/>

    <px:fileset-join name="mapping">
        <p:input port="source">
            <p:pipe step="copy" port="mapping"/>
            <p:pipe step="delete" port="result"/>
        </p:input>
    </px:fileset-join>
    <p:sink/>

</p:declare-step>
