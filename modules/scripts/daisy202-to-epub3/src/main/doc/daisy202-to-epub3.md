<link rev="dp2:doc" href="../resources/xml/daisy202-to-epub3.xpl"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>
<meta property="dc:title" content="User Guide - DAISY 2.02 to EPUB 3"/>

<!--
labels: [Type-Doc,Compoment-UserGuide,Component-Module,Component-Script]
sidebar: UserGuideToc
-->

# DAISY 2.02 To EPUB 3

The "DAISY 2.02 to EPUB3" script will convert a DAISY 2.02 DTB (Digital Talking Book) into an EPUB3 publication.
This page describes the steps, comments and issues related to this transformation.

## Table of contents

{{>toc}}

## Synopsis

{{>synopsis}}

## Example running from command line

On Linux and Mac OS X:

    $ cli/dp2 daisy202-to-epub3 --x-href samples/daisy202/dontworrybehappy/ncc.html --x-output ~/Desktop/out --x-mediaoverlay false --x-compatibility-mode false

On Windows:

    $ cli\dp2.exe daisy202-to-epub3 --x-href samples\daisy202\dontworrybehappy\ncc.html --x-output C:\Pipeline2-Output --x-mediaoverlay false --x-compatibility-mode false

This command will create two entries in the output directory. One is a folder called "epub", which is a temporary directory created by the converter. The second is the resulting EPUB3 file. The EPUB3 file is given a name based on the dc:identifier and dc:title metadata elements from the original NCC; "dc:identifier - dc:title.epub".


## Outline

The high-level conversion workflow is as follows:

 # Get the SMIL-based reading order from the NCC
 # Load all the SMILs and upgrade them to EPUB3 Media Overlays
 # Extract content document references from each SMIL
 # Make a content-based reading order
 # Load all the content documents, upgrade them to EPUB3 Content Documents
 # If mediaoverlay = 'true'
   # Rearrange the Media Overlays to match the Content Documents
   # Get all referenced audio, images, etc.
 # Make the EPUB3 Navigation Document based on the NCC
 # Make the EPUB3 Package Document
 # Store the EPUB3 Publication in a OCF ZIP Container

## Errors

This is a list of defined errors for this script. Each error has a unique error code for easy identification.

 * *`PDE01`*: href must be a valid URI. In practice this simply means that the path must be prefixed with "file://", and in Windows, all directory separators (\) must be replaced with forward slashes (/).
 * *`PDE02`*: output must be a valid URI. In practice this simply means that the path must be prefixed with "file://", and in Windows, all directory separators (\) must be replaced with forward slashes (/).
 * *`PDE03`*: When given, mediaoverlay must be either "true" (default) or "false".
 * *`PDE04`*: When given, compatibility-mode must be either "true" (default) or "false".

## See Also

### Main Specifications
[http://www.daisy.org/z3986/specifications/daisy_202.html DAISY 2.02 Specification]
 * [http://www.daisy.org/z3986/specifications/daisy_202.html#ncc The Navigation Control Center (NCC) document]
 * [http://www.daisy.org/z3986/specifications/daisy_202.html#textdoc The text content document (XHTML)]
 * [http://www.daisy.org/z3986/specifications/daisy_202.html#smil The SMIL document]

[http://idpf.org/epub/30/spec/epub30-overview.html EPUB3 Overview]
 * [http://idpf.org/epub/30/spec/epub30-publications.html EPUB Publications 3.0]
 * [http://idpf.org/epub/30/spec/epub30-contentdocs.html EPUB Content Documents 3.0]
   * [http://idpf.org/epub/30/spec/epub30-contentdocs.html#sec-xhtml-nav EPUB Navigation Documents]
   * [http://idpf.org/epub/30/spec/epub30-contentdocs.html#sec-xhtml XHTML Content Documents]
 * [http://idpf.org/epub/30/spec/epub30-ocf.html EPUB Open Container Format (OCF) 3.0]
 * [http://idpf.org/epub/30/spec/epub30-mediaoverlays.html EPUB Media Overlays 3.0]

### Related Specifications
 * [http://www.w3.org/TR/xhtml1/ XHTML 1.0]
 * [http://www.w3.org/TR/html5/ HTML5]
 * [http://www.w3.org/TR/REC-smil/ SMIL 1.0]
 * [http://www.w3.org/TR/SMIL/smil30.html SMIL 3.0]
