<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:set-doctype"
                exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Sets, or deletes, the doctype of a file. If `doctype` is the empty string, the doctype will be deleted. It is an error if `doctype` does not match the regex
            `^&lt;!DOCTYPE\s+\w+\s*(|SYSTEM\s+("[^"]*"|'[^']*')|PUBLIC\s+("[\s\w\-'()+,\./:=?;!*#@$_%]*"|'[\s\w\-()+,\./:=?;!*#@$_%]*')\s+("[^"]*"|'[^']*'))(\s*\[[^\]]+\])?>$`. The result port will contain a
            `c:result` document with the URI to the file as its text node.</p>
    </p:documentation>

    <p:output port="result">
        <p:documentation>A document containing the URI to the file, same as the output of a `p:store` operation.</p:documentation>
    </p:output>
    <p:option name="href" required="true">
        <p:documentation>URI to the file you want to set the doctype of.</p:documentation>
    </p:option>
    <p:option name="doctype" required="true">
        <p:documentation>The doctype.</p:documentation>
    </p:option>
    <p:option name="encoding" select="'utf-8'">
        <p:documentation>The encoding to use when reading and writing from and to the file (default: 'utf-8').</p:documentation>
    </p:option>

    <!-- Implemented in ../../../java/org/daisy/pipeline/file/calabash/impl/SetDoctypeProvider.java -->

</p:declare-step>
