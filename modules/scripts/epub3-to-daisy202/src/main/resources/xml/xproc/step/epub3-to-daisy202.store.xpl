<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-daisy202.store" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                name="main">
    
    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true"/>
    
    <p:option name="output-dir" required="true"/>
    
    <p:output port="result.fileset" primary="false">
        <p:pipe step="result.fileset" port="result"/>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <px:fileset-move name="move">
        <p:with-option name="new-base" select="concat($output-dir,replace(/*/@content,'[^a-zA-Z0-9]','_'),'/')">
            <p:pipe step="identifier" port="result"/>
        </p:with-option>
        <p:input port="in-memory.in">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-move>

    <px:fileset-store name="fileset-store" fail-on-error="true">
        <p:input port="fileset.in">
            <p:pipe step="move" port="fileset.out"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe step="move" port="in-memory.out"/>
        </p:input>
    </px:fileset-store>
    <p:identity>
        <p:input port="source">
            <p:pipe step="fileset-store" port="fileset.out"/>
        </p:input>
    </p:identity>
    <p:delete match="@original-href" name="result.fileset"/>
    
    <p:group name="identifier">
        <p:output port="result"/>
        <px:fileset-load href="*/ncc.html">
            <p:input port="fileset">
                <p:pipe step="main" port="source.fileset"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe step="main" port="source.in-memory"/>
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
