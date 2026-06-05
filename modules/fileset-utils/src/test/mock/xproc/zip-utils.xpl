<p:library version="1.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:declare-step type="px:unzip">
        <p:output port="result"/>
        <p:option name="href" required="true"/>
        <p:option name="file"/>
        <p:option name="content-type"/>
    </p:declare-step>

</p:library>
