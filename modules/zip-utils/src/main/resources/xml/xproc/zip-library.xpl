<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
           xmlns:letex="http://www.le-tex.de/namespace">

    <p:declare-step type="px:zip">
        <!--
            Documentation taken from http://exproc.org/proposed/steps/other.html#zip
            (https://github.com/xproc/exproc.org/blob/master/proposed/steps/other.xml)
        -->
        <p:documentation xmlns="http://docbook.org/ns/docbook">
            <para>A step for creating ZIP archives.</para>

            <para>The ZIP archive is identified by the <option>href</option>. The
            <option>manifest</option> (described below) provides the list of files to be processed
            in the archive.  The <option>command</option> indicates the nature of the processing:
            “<literal>update</literal>”, “<literal>freshen</literal>”, “<literal>create</literal>”,
            or “<literal>delete</literal>”.</para>

            <para>If files are added to the archive, <option>compression-method</option> indicates
            how they should be added: “<literal>stored</literal>” or
            “<literal>deflated</literal>”. For deflated files, the
            <option>compression-level</option> identifies the kind of compression:
            “<literal>smallest</literal>”, “<literal>fastest</literal>”,
            “<literal>default</literal>”, “<literal>huffman</literal>”, or
            “<literal>none</literal>”.</para>

            <para>The entries identified by the <option>manifest</option> are processed. The
            manifest must conform to the following schema:</para>

            <programlisting><![CDATA[default namespace c="http://www.w3.org/ns/xproc-step"
start = zip-manifest
zip-manifest =
   element c:zip-manifest {
      entry*
   }
entry =
   element c:entry {
      attribute name { text }
    & attribute href { text }
    & attribute comment { text }?
    & attribute method { "deflated" | "stored" }
    & attribute level { "smallest" | "fastest" | "huffman" | "default" | "none" }
      empty
   }]]></programlisting>

            <para>For example:</para>

            <programlisting><![CDATA[<zip-manifest xmlns="http://www.w3.org/ns/xproc-step">
  <entry name="file1.xml" href="http://example.org/file1.xml" comment="An example file"/>
  <entry name="path/to/file2.xml" href="http://example.org/file2.xml" method="stored"/>
</zip-manifest>]]></programlisting>

            <para>If the <option>command</option> is “<literal>delete</literal>”, then
            <filename>file1.xml</filename> and <filename>path/to/file2.xml</filename> will be
            deleted from the archive. Otherwise, the file that appears on the <port>source</port>
            port that has the base URI <uri>http://example.org/file1.xml</uri> will be stored in the
            archive as <filename>file1.xml</filename> (using the default method and level), the file
            that appears on the <port>source</port> port that has the base URI
            <uri>http://example.org/file2.xml</uri> will be stored in the archive as
            <filename>path/to/file2.xml</filename> without being compressed.</para>

            <para>A <tag>c:zipfile</tag> description of the archive content is produced on the
            <port>result</port> port.</para>
        </p:documentation>
        <p:input port="source" sequence="true" primary="true"/>
        <p:input port="manifest"/>
        <p:output port="result"/>
        <p:option name="href" required="true"/>                    <!-- xsd:anyURI -->
        <p:option name="compression-method"/>                      <!-- stored|deflated -->
        <p:option name="compression-level"/>                       <!-- smallest|fastest|default|huffman|none -->
        <p:option name="command" select="'update'"/>               <!-- update|freshen|create|delete -->
        <p:option name="byte-order-mark"/>                         <!-- xsd:boolean -->
        <p:option name="cdata-section-elements" select="''"/>      <!-- ListOfQNames -->
        <p:option name="doctype-public"/>                          <!-- xsd:string -->
        <p:option name="doctype-system"/>                          <!-- xsd:anyURI -->
        <p:option name="encoding"/>                                <!-- xsd:string -->
        <p:option name="escape-uri-attributes" select="'false'"/>  <!-- xsd:boolean -->
        <p:option name="include-content-type" select="'true'"/>    <!-- xsd:boolean -->
        <p:option name="indent" select="'false'"/>                 <!-- xsd:boolean -->
        <p:option name="media-type"/>                              <!-- xsd:string -->
        <p:option name="method" select="'xml'"/>                   <!-- xsd:QName -->
        <p:option name="normalization-form" select="'none'"/>      <!-- NormalizationForm -->
        <p:option name="omit-xml-declaration" select="'true'"/>    <!-- xsd:boolean -->
        <p:option name="standalone" select="'omit'"/>              <!-- true|false|omit -->
        <p:option name="undeclare-prefixes"/>                      <!-- xsd:boolean -->
        <p:option name="version" select="'1.0'"/>                  <!-- xsd:string -->
        <!--
            Implemented in Java (XMLCalabash)
        -->
    </p:declare-step>

    <p:declare-step type="px:unzip">
        <!--
            Documentation taken from http://exproc.org/proposed/steps/other.html#unzip
            (https://github.com/xproc/exproc.org/blob/master/proposed/steps/other.xml)
        -->
        <p:documentation xmlns="http://docbook.org/ns/docbook" xmlns:xlink="http://www.w3.org/1999/xlink">
            <para>A step for extracting information out of ZIP archives.</para>

            <para>The value of the <option>href</option> option <rfc2119>must</rfc2119> be an
            IRI. It is a <glossterm>dynamic error</glossterm> if the document so identified does not
            exist or cannot be read.</para>

            <para>The value of the <option>file</option> option, if specified,
            <rfc2119>must</rfc2119> be the fully qualified path-name of a document in the
            archive. It is <glossterm>dynamic error</glossterm> if the value specified does not
            identify a file in the archive.</para>

            <para>The output from the <tag>pxp:unzip</tag> step <rfc2119>must</rfc2119> conform to
            the <link
            xlink:href="http://exproc.org/proposed/steps/schemas/ziptoc.rnc">ziptoc.rnc</link>
            schema.</para>

            <para>If the <option>file</option> option is specified, the selected file in the archive
            is extracted and returned:</para>

            <itemizedlist>
                <listitem>
                    <para>If the <option>content-type</option> is not specified, or if an XML
                    content type is specified, the file is parsed as XML and returned. It is a
                    <glossterm>dynamic error</glossterm> if the file is not well-formed XML.</para>
                </listitem>
                <listitem>
                    <para>If the <option>content-type</option> specified is not an XML content type,
                    the file is base64 encoded and returned in a single <tag>c:data</tag>
                    element.</para>
                </listitem>
            </itemizedlist>

            <para>If the <option>file</option> option <emphasis>is not</emphasis> specified, a table
            of contents for the archive is returned.</para>

            <para>For example, the contents of the XML Calabash 0.8.5 distribution archive might be
            reported like this:</para>

            <informalexample>
                <programlisting><![CDATA[<c:zipfile xmlns:c="http://www.w3.org/ns/xproc-step"
           href="http://xmlcalabash.com/download/calabash-0.8.5.zip">
   <c:directory name="calabash-0.8.5/" date="2008-11-04T19:29:20.000-05:00"/>
   <c:directory name="calabash-0.8.5/docs/" date="2008-11-04T19:29:20.000-05:00"/>
   <c:file compressed-size="11942" size="36677" name="calabash-0.8.5/docs/CDDL+GPL.txt"
           date="2008-11-04T19:29:20.000-05:00"/>
   <c:file compressed-size="928" size="2110" name="calabash-0.8.5/docs/ChangeLog"
           date="2008-11-04T19:29:20.000-05:00"/>
   <c:file compressed-size="6817" size="17987" name="calabash-0.8.5/docs/GPL.txt"
           date="2008-11-04T19:29:20.000-05:00"/>
   <c:file compressed-size="494" size="830" name="calabash-0.8.5/docs/NOTICES"
           date="2008-11-04T19:29:20.000-05:00"/>
   <c:directory name="calabash-0.8.5/lib/" date="2008-11-04T19:29:20.000-05:00"/>
   <c:file compressed-size="389650" size="407421" name="calabash-0.8.5/lib/calabash.jar"
           date="2008-11-04T19:29:20.000-05:00"/>
   <c:file compressed-size="1237" size="2493" name="calabash-0.8.5/README"
           date="2008-11-04T19:29:20.000-05:00"/>
   <c:directory name="calabash-0.8.5/xpl/" date="2008-11-04T19:29:20.000-05:00"/>
   <c:file compressed-size="175" size="255" name="calabash-0.8.5/xpl/pipe.xpl"
           date="2008-11-04T19:29:20.000-05:00"/>
</c:zipfile>]]></programlisting>
            </informalexample>
        </p:documentation>
        <p:output port="result"/>
        <p:option name="href" required="true"/>    <!-- xsd:anyURI -->
        <p:option name="file"/>                    <!-- string -->
        <p:option name="content-type"/>            <!-- string -->
        <!--
            Implemented in Java (XMLCalabash)
        -->
    </p:declare-step>

    <p:declare-step type="letex:unzip">
        <p:documentation>Will extract a complete zip file or a single contained file to a specified destination folder.</p:documentation>
        <p:option name="zip" required="true">
            <p:documentation>The file:/ URL to the zip file.</p:documentation>
        </p:option>
        <p:option name="dest-dir" required="true">
            <p:documentation>A file:/ URL to the destination folder. Will be created if it does not exist (subject to the $overwrite option).</p:documentation>
        </p:option>
        <p:option name="overwrite" required="false" select="'no'">
            <p:documentation>Whether existing directories and files will be overwritten.</p:documentation>
        </p:option>
        <p:option name="file" required="false">
            <p:documentation>Optionally, a specific relative path to a file within the zip file. Will be restored to its 
                relative path below $dest-dir.</p:documentation>
        </p:option>
        <p:output port="result" primary="true">
            <p:documentation>A c:files document with the extracted files, as c:file elements (no hierarchy). There is an 
                @xml:base attribute on /c:files that contains the $dest-dir location URL. A c:file element will contain a @name
                attribute with the corresponding file’s relative location.</p:documentation>
        </p:output>
        <!--
            Implemented in ../../../java/org/daisy/pipeline/zip/calabash/impl/UnZipProvider.java
        -->
    </p:declare-step>

</p:library>
