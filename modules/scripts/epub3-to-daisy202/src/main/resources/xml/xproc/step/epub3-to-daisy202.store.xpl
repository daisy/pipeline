<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-daisy202.store" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                name="main">
    
    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>
    
    <p:option name="output-dir" required="true"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <px:fileset-move name="move">
        <p:with-option name="new-base" select="concat($output-dir,replace(/*/@content,'[^a-zA-Z0-9]','_'),'/')">
            <p:pipe port="result" step="identifier"/>
        </p:with-option>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-move>

    <px:fileset-store name="fileset-store">
        <p:input port="fileset.in">
            <p:pipe port="fileset.out" step="move"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="move"/>
        </p:input>
    </px:fileset-store>
    
    <p:group name="identifier">
        <p:output port="result"/>
        <px:fileset-load href="*/ncc.html">
            <p:input port="fileset">
                <p:pipe port="fileset.in" step="main"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe port="in-memory.in" step="main"/>
            </p:input>
        </px:fileset-load>
        <!--
            these assertions should normally never fail
        -->
        <px:assert test-count-min="1" test-count-max="1" error-code="PED01" message="There must be exactly one ncc.html in the resulting DAISY 2.02 fileset"/>
        <p:filter select="/*/*/*[@name='dc:identifier']"/>
        <px:assert test-count-min="1" error-code="PED02" message="There must be at least one dc:identifier meta element in the resulting ncc.html"/>
        <p:split-sequence test="position()=1"/>
    </p:group>
    <p:sink/>
    
</p:declare-step>
