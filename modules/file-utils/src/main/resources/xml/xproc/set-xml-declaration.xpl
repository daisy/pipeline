<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:set-xml-declaration">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Sets, or deletes, the xml declaration of a file. If `xml-declaration` is the empty string, the xml declaration will be deleted. It is an error if `xml-declaration` does not match the regex
            `^&lt;\?xml[ \t\r\n]+version[ \t\r\n]*=[ \t\r\n]*(''1\.[0-9]+''|&quot;1.[0-9]+&quot;)([ \t\r\n]+encoding[ \t\r\n]*=[ \t\r\n]*(''[A-Za-z][A-Za-z0-9._-]*''|&quot;[A-Za-z][A-Za-z0-9._-]*&quot;))?([ \t\r\n]+standalone[ \t\r\n]*=[ \t\r\n]*(''(yes|no)''|&quot;(yes|no)&quot;))?[ \t\r\n]*\?&gt;$`.
            The result port will contain a `c:result` document with the URI to the file as its text node.</p>
    </p:documentation>
    
    <p:output port="result">
        <p:documentation>A document containing the URI to the file, same as the output of a `p:store` operation.</p:documentation>
    </p:output>
    <p:option name="href" required="true">
        <p:documentation>URI to the file you want to set the xml declaration of.</p:documentation>
    </p:option>
    <p:option name="xml-declaration" required="true">
        <p:documentation>The xml declaration.</p:documentation>
    </p:option>
    <p:option name="encoding" select="'utf-8'">
        <p:documentation>The encoding to use when reading and writing from and to the file (default: 'utf-8').</p:documentation>
    </p:option>

    <!-- Implemented in ../../../java/org/daisy/pipeline/file/calabash/impl/SetXmlDeclarationProvider.java -->

</p:declare-step>
