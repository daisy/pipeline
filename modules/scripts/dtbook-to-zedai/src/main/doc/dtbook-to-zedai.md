<link rev="dp2:doc" href="../resources/xml/dtbook-to-zedai.xpl"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>
<meta property="dc:title" content="User Guide - DTBook to ZedAI"/>

<!--
labels: [Type-Doc,Compoment-UserGuide,Component-Module,Component-Script]
sidebar: UserGuideToc
-->

# DTBook To ZedAI

The "DTBook to ZedAI" script will convert one or more DTBook XML
documents to a single ZedAI document (formally an instance of the Book
profile of the NISO Z39.98-2012 - Authoring and Interchange Framework
for Adaptive XML Publishing).

The script will create:

* A single valid ZedAI file, written to disk.
* A MODS metadata record XML file, written to disk.
* A CSS file, written to disk.

## Table of contents

{{>toc}}

## Synopsis

{{>synopsis}}

<!--
TODO specify whether opt-lang overrides a language code declared in the XML
-->

## Example running from command line

On Linux and Mac OS X:

    $ cli/dp2 dtbook-to-zedai --i-source samples/dtbook/hauy_valid.xml --x-output-dir ~/Desktop/out

On Windows:

    $ cli\dp2.exe dtbook-to-zedai --i-source samples\dtbook\hauy_valid.xml --x-output-dir C:\Pipeline2-Output

Input:

DTBook

~~~xml
<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE dtbook PUBLIC "-//NISO//DTD dtbook 2005-3//EN" "http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd">
<?xml-stylesheet href="dtbookbasic.css" type="text/css"?>
<dtbook xmlns="http://www.daisy.org/z3986/2005/dtbook/" version="2005-3" xml:lang="en-US">
    <head>
        <meta content="pipeline2-dtbook-test-20110301-basic" name="dtb:uid"/>
        <meta content="Pipeline 2 DTBook Test Content: Basic" name="dc:Title"/>
        <meta name="dc:Creator" content="Marisa D."/>
        <meta content="2011-03-01" name="dc:Date"/>
        <meta name="dc:Publisher" content="Marisa D."/>
        <meta content="pipeline2-dtbook-test-20110301-basic" name="dc:Identifier"/>
        <meta content="en-US" name="dc:Language"/>
    </head>
    <!-- test comment -->
    <book>
        <frontmatter>
            <doctitle>Pipeline 2 DTBook Test Content: Basic</doctitle>
            <docauthor>Marisa D.</docauthor>
        </frontmatter>
        <bodymatter>
            <level1>
                <h1>Introduction</h1>
                <p><sent>The DAISY Pipeline 2 is an ongoing project to develop a next generation
                        framework for automated production of accessible materials for people with
                        print disabilities.</sent>
                    <sent>It is the follow-up and total redesign of the original DAISY Pipeline 1
                        project.</sent>
                </p>
                <img height="100px" width="100px" src="image.jpg" alt="DAISY logo"/>
            </level1>
        </bodymatter>
    </book>
</dtbook>
~~~

Output:

1. ZedAI

   ~~~xml
   <?xml-stylesheet href="dtbook-basic-zedai.css" ?>
   <document 
   	xmlns:rend="http://www.daisy.org/ns/z3986/authoring/features/rend/" 
   	xmlns:xlink="http://www.w3.org/1999/xlink" 
   	xmlns="http://www.daisy.org/ns/z3986/authoring/" 
   	xmlns:z3986="http://www.daisy.org/z3986/2011/vocab/decl/#" 
   	xmlns:dcterms="http://purl.org/dc/terms/" 
   	xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp" 
   	xmlns:its="http://www.w3.org/2005/11/its" 
   	profile="http://www.daisy.org/z3986/2011/vocab/profiles/default/" 
   	xml:lang="en-US">
       <head>
            
   	<meta rel="z3986:profile" resource="http://www.daisy.org/z3986/2011/auth/profiles/book/0.8/"/>
   	<meta property="dcterms:title" content="Pipeline 2 DTBook Test Content: Basic"/>
   	<meta property="dcterms:creator" content="Marisa D."/>
   	<meta property="dcterms:date" content="2011-03-01" xml:id="meta-dcdate"/>
   	<meta property="dcterms:publisher" content="Marisa D."/>
   	<meta property="dcterms:identifier" content="pipeline2-dtbook-test-20110301-basic"/>
   	<meta property="dcterms:language" content="en-US"/>
   	<meta rel="z3986:meta-record" resource="dtbook-basic-zedai-mods.xml">
   	    <meta property="z3986:meta-record-type" about="dtbook-basic-zedai-mods.xml" content="z3986:mods"/>
   	    <meta property="z3986:meta-record-version" about="dtbook-basic-zedai-mods.xml" content="3.3"/>
   	</meta>
       </head>
       <!-- test comment -->
       <body>
           <frontmatter>
   	    <section>
   	        <p role="title">Pipeline 2 DTBook Test Content: Basic</p>
   		<p role="author">Marisa D.</p>
   	    </section>
   	</frontmatter>
           <bodymatter>
               <section>
   		<h>Introduction</h>
   		<p><s>The DAISY Pipeline 2 is an ongoing project to develop a next generation
                      framework for automated production of accessible materials for people with
                      print disabilities.</s>
                      <s>It is the follow-up and total redesign of the original DAISY Pipeline 1
                      project.</s>
   		</p>
   		<object src="image.jpg" xml:id="d212e45">
   		    <description>DAISY logo</description>
   	        </object>
   	    </section>
           </bodymatter>
       </body>
   </document>
   ~~~

2. MODS

   ~~~xml
   <mods xmlns="http://www.loc.gov/mods/v3" xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" version="3.3">
       <titleInfo>
   	<title>Pipeline 2 DTBook Test Content: Basic</title>
       </titleInfo>
       <name>
       <namePart>Marisa D.</namePart>
   	<role>
   	    <roleTerm type="text">author</roleTerm>
   	</role>
       </name>
       <identifier type="uid">pipeline2-dtbook-test-20110301-basic</identifier>
       <language>
           <languageTerm type="code" authority="rfc3066">en-US</languageTerm>
       </language>
   </mods>
   ~~~

3. CSS

   ~~~css
   #d212e45{
       height: 100px;
       width: 100px;
   }
   ~~~

# See also

* [Developer documentation](https://code.google.com/archive/p/daisy-pipeline/wikis/DTBookToZedAIDev.wiki)
* [Element conversion rules](https://code.google.com/archive/p/daisy-pipeline/wikis/DTBookToZedAIConversionRules.wiki)
* [ZedAI specifications](http://www.daisy.org/z3998/2012/)

