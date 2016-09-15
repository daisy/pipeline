<p:library version="1.0"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

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
    
    <p:declare-step type="px:zip-manifest-from-fileset">
        <p:input port="source"/>
        <p:output port="result"/>
        <p:xslt>
            <p:input port="stylesheet">
                <p:document href="../xslt/fileset-to-zip-manifest.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:declare-step>
    
    <p:import href="unzip-fileset.xpl"/>
    
</p:library>
