<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0" exclude-inline-prefixes="#all" xmlns:ex="http://example.net/ns" type="ex:pipeline">

    <p:input port="source">
        <p:inline>
            <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                    <meta charset="utf-8"/>
                    <title>Hello world!</title>
                    <meta name="viewport" content="width=device-width"/>
                </head>
                <body>
                    <h1>Hello world!</h1>
                </body>
            </html>
        </p:inline>
    </p:input>

    <p:output port="result"/>

    <p:option name="name" required="true"/>

    <p:xslt>
        <p:with-param name="name" select="$name"/>
        <p:input port="stylesheet">
            <p:document href="../xslt/stylesheet.xsl"/>
        </p:input>
    </p:xslt>

</p:declare-step>
