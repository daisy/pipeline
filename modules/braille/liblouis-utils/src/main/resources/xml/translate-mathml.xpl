<?xml version="1.1" encoding="UTF-8"?>
<p:pipeline type="louis:translate-mathml" name="translate-mathml"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:louis="http://liblouis.org/liblouis"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Translate a MathML document to Braille using liblouisutdml.</p>
    </p:documentation>
    
    <p:option name="temp-dir" required="true"/>
    <p:option name="math-code" required="true"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/braille/liblouis-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    
    <px:fileset-create base="http://www.daisy.org/pipeline/modules/braille/liblouis-mathml/lbu_files/"/>
    <px:fileset-add-entry name="styles">
        <p:with-option name="href" select="concat(lower-case($math-code), '.cfg')"/>
    </px:fileset-add-entry>
    <p:sink/>
    
    <px:mkdir>
        <p:with-option name="href" select="$temp-dir"/>
    </px:mkdir>
    
    <louis:translate-file>
        <p:input port="source">
            <p:pipe step="translate-mathml" port="source"/>
        </p:input>
        <p:input port="styles">
            <p:pipe step="styles" port="result"/>
        </p:input>
        <p:input port="semantics">
            <p:empty/>
        </p:input>
        <p:with-param name="page-width" port="page-layout" select="200.0"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
    </louis:translate-file>
    
    <p:string-replace match="/louis:result/text()"
                      replace="translate(replace(., '&#x0A;&#x0C;$', ''), ' ', '&#x2800;')"/>
    
</p:pipeline>
