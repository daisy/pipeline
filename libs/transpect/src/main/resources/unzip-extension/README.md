# unzip-extension

An unzip extension step for XML Calabash that unzips entire archives.

## Description 

XProc 1.0 lacks of a native unzip solution. Therefore, XProc processors such as 
XML Calabash or Morgana provide own extension steps for extracting zip archives. 
However, these steps allow only to extract XML files in an archive. To extract other 
file types, we've developed this unzip-extension step for XML calabash. It extracts 
all files to the specified location and provides an XML data set as output.

## XProc example

```xml
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
  xmlns:tr="http://transpect.io"
  version="1.0">
  
  <p:output port="result"/>
  
  <p:import href="http://transpect.io/calabash-extensions/unzip-extension/unzip-declaration.xpl"/>
  
  <tr:unzip name="unzip">
    <p:with-option name="zip" select="'archive.zip'"/>
    <p:with-option name="dest-dir" select="'file:/C:/home/kraetke/output'"/>
    <p:with-option name="overwrite" select="'no'"/>
  </tr:unzip>
  
</p:declare-step>
```

The output would look somewhat like this:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<c:files xmlns:c="http://www.w3.org/ns/xproc-step"
         xml:base="file:/C:/home/kraetke/output">
   <c:file name="dir/myfile.xml"/>
</c:files>
```

## Prerequisites

Java 1.7

## Include in XML Calabash

1. Add the unzip-extension to your XML Calabash `$XPROC-CONFIG` file
```xml
<?xml version="1.0" encoding="UTF-8"?>
<xproc-config xmlns="http://xmlcalabash.com/ns/configuration"
  xmlns:tr="http://transpect.io"
  xmlns:tr-internal="http://transpect.io/internal">

  <implementation type="tr-internal:unzip"                class-name="io.transpect.calabash.extensions.UnZip"/>
  
</xproc-config>
```
2. Add `io/transpect/calabash/extensions/UnZip.class` to your Java `$CLASSPATH` (otherwise XML Calabash will fail with a class not found error)
3. Connect `xmlcatalog/catalog.xml` with your `$XMLCATALOG` via `nextCatalog` statement or just pass `‑Dxml.catalog.files=xmlcatalog/catalog.xml` as parameter to Java
4. Run XML Calabash

```bash
java \
   -cp "$CLASSPATH" \
   -Dfile.encoding=UTF-8 \
   -Dxml.catalog.files=$XMLCATALOG \
   -Dxml.catalog.staticCatalog=1 \
   com.xmlcalabash.drivers.Main \
   -Xtransparent-json \
   -E org.xmlresolver.Resolver \
   -U org.xmlresolver.Resolver \
   -c $XPROC-CONFIG \
   unzip-example.xpl \
   zip=myarchive.zip \
   path=output
```


## Compile

We already provide a class file and a jar file, but feel free to compile this extension for your needs.

1. add the jars of XML Calabash and Saxon to your Java `$CLASSPATH`
2. compile it

```
javac -source 1.7 -target 1.7 -d . -cp $CLASSPATH src/main/java/io/transpect/calabash/extensions/UnZip.java
```
