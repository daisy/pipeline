<p:library version="1.0"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:letex="http://www.le-tex.de/namespace"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal">
    
    <p:declare-step type="px:zip">
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
    </p:declare-step>
    
    <p:declare-step type="px:unzip">
        <p:output port="result"/>
        <p:option name="href" required="true"/>    <!-- xsd:anyURI -->
        <p:option name="file"/>                    <!-- string -->
        <p:option name="content-type"/>            <!-- string -->
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
    </p:declare-step>
    
</p:library>