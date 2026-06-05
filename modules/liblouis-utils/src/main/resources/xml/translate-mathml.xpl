<?xml version="1.1" encoding="UTF-8"?>
<p:pipeline xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
            xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
            xmlns:c="http://www.w3.org/ns/xproc-step"
            xmlns:louis="http://liblouis.org/liblouis"
            type="louis:translate-mathml" name="main"
            exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Translate a MathML document to Braille using liblouisutdml.</p>
    </p:documentation>
    
    <p:option name="math-code" required="true"/>
    
    <p:import href="translate-file.xpl">
        <p:documentation>
            louis:translate-file
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:mkdir
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-create
            px:fileset-add-entry
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>
    
    <px:fileset-create base="http://www.daisy.org/pipeline/modules/braille/liblouis-mathml/lbu_files/"/>
    <px:fileset-add-entry name="styles">
        <p:with-option name="href" select="concat(lower-case($math-code), '.cfg')"/>
    </px:fileset-add-entry>
    <p:sink/>
    
    <px:mkdir>
        <p:with-option name="href" select="//c:param[@name='temp-dir' and not(@namespace[not(.='')])][1]/@value">
            <p:pipe step="main" port="parameters"/>
        </p:with-option>
    </px:mkdir>
    
    <px:assert message="'temp-dir' parameter is required">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
        <p:with-option name="test" select="exists(//c:param[@name='temp-dir' and not(@namespace[not(.='')])])">
            <p:pipe step="main" port="parameters"/>
        </p:with-option>
    </px:assert>
    
    <louis:translate-file>
        <p:input port="styles">
            <p:pipe step="styles" port="result.fileset"/>
        </p:input>
        <p:input port="semantics">
            <p:empty/>
        </p:input>
        <p:with-param name="page-width" port="page-layout" select="200.0"/>
        <p:with-option name="temp-dir" select="//c:param[@name='temp-dir' and not(@namespace[not(.='')])][1]/@value">
            <p:pipe step="main" port="parameters"/>
        </p:with-option>
    </louis:translate-file>
    
    <p:string-replace match="/louis:result/text()"
                      replace="translate(replace(., '&#x0A;&#x0C;$', ''), ' ', '&#x2800;')"/>
    
</p:pipeline>
