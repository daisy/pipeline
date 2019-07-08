[![Build Status](https://travis-ci.org/transpect/mathtype-extension.svg?branch=master)](https://travis-ci.org/transpect/mathtype-extension)

# mathtype-extension

An extension step for XML Calabash that converts a Mathtype Equation (MTEF) to MathML.

Incorporates (J)Ruby mathtype gem: https://github.com/sbulka/mathtype (forked from [Jure Triglav](https://github.com/jure)'s gem)

XSLT adapted from: https://github.com/jure/mathtype_to_mathml/tree/master/lib/xsl

Written by Sebastian Bulka, le-tex publishing services GmbH

Development was sponsored by 

* [Carl Hanser Verlag](http://www.hanser-fachbuch.de/)
* [VDE Verband der Elektrotechnik Elektronik Informationstechnik e.V.](https://www.vde.com/)
* [STM Document Engineering Private Limited](http://www.stmdocs.in/)

If you or your organization benefit from this tool, if you are interested in supporting the continuous maintenance, or if you have a feature request: All development costs have not been covered yet; we do welcome more sponsors. If you are interested, please get in touch with @sbulka, @mkraetke, or @gimsieke.

# Usage
  There are different ways to call the xproc-step.
## Standalone
1. Call pipeline for one file.  
   For setting the MATHTYPE_CP variable, see section 'Java classpath' below.  
	```java -cp $MATHTYPE_CP com.xmlcalabash.drivers.Main -c file:///uri/of/transpect-config.xml mathtype-example.xpl file=file:///uri/of/bin-file.bin```
	
## XProc extension
1. Call &lt;tr:mathtype2mml&gt; from your pipeline.  
	This requires you to have xproc-util available in calabash, to store debug-files.  
	<p:import href="mathtype2mml-declaration-internal.xpl"/>

2. Call &lt;tr:mathtype2mml-internal&gt; from your pipeline.  
	No xproc-util needed, but no debug-files stored therefore.  
	The debug is still available on xproc-ports, if you want to use them yourself.

## From other front-end pipelines such as docx2tex

See https://github.com/transpect/docx2tex (note the MathType extension submodule might not be up to date)

# Options
Configure tr:mathtype2mml step by passing options to it:

 * href (required):      file name (not URI) of one file containing a Mathtype Equation  
   supported filetypes:  
   * .bin (OLE object)  
   * .wmf
 * debug:					 Output debug messages (xsl:message) if set to 'yes'. Default is 'no'.
 * debug-dir:				 If debug is set, also output intermediate results for each internal xsl-step.
 * mml-space-handling:	 How to handle Mathtype-spacing in Mathml. Possible: 'char', 'mspace'. Default: mspace  

 # Spaces
  MathML states that whitespace-characters should be normalized before rendering.  
  Problems arise because in Mathtype, whitespace is sometimes also used for indentation.  
  The option for mml-space-handling was introduced so spaces can be converted to will not be normalized.  
  If 'char' is chosen, the Unicode-characters for spaces are used, wrapped in &lt;mtext&gt;.  
  Opening the MathML in e.g. your browser, spaces will likely get normalized.
  If 'mspace' is chosen, every Mathtype-space will be represented by &lt;mspace&gt;.  
  You can define your preferred width for each different Mathtype-Space.  
  Insert what you want to see on the mspace/@width, including unit. (e.g. '1pt' for what is called 1pt-space in Mathtype)  
  Every option has a default width:
   * em-width	Default: '1em'
   * en-width	Default: '0.33em'
   * standard-width	Default: '0.16em'
   * thin-width	Default: '0.08em'
   * hair-width	Default: '0.08em'
   * zero-width	Default: '0em'

# Having issues?
You can file an issue on github for inconveniences with your conversion.  
This may be asking for help, reporting bugs or requesting features.

Its sufficient to describe your Problem in natural language, e.g:  
"In MathML output, the last parenthese is missing."  
If possible, please attach the file which contains your formula, so we can investigate what went wrong.  

# Java classpath
The extension is shipped with a copy of jruby and some ruby gems.  
These need to be on your classpath, or java wont find the files.  
In addition, calabash and saxon should be included too, to be able to process XProc and XSLT.

Example (from https://github.com/transpect/calabash-frontend/blob/master/calabash.sh):   
```MATHTYPE_CP= "/path/to/calabash/distro/xmlcalabash-1.1.15-97.jar:/path/to/calabash/saxon/saxon9he.jar:/path/to/calabash/extensions/transpect/mathtype-extension/:/path/to/calabash/extensions/transpect/mathtype-extension/lib/jruby-complete-9.1.8.0.jar:/path/to/calabash/extensions/transpect/mathtype-extension/ruby/stdlib:/path/to/calabash/extensions/transpect/mathtype-extension/ruby/ruby-ole-1.2.12.1/lib:/path/to/calabash/extensions/transpect/mathtype-extension/ruby/nokogiri-1.7.0.1-java/lib:/path/to/calabash/extensions/transpect/mathtype-extension/ruby/bindata-2.3.5/lib:/path/to/calabash/extensions/transpect/mathtype-extension/ruby/mathtype-0.0.7.5/lib"```


# Compilation

If you happen to change java sources, you need to set the classpath as describe above for compilation too.  
    ```javac -target 1.7 -source 1.7 -cp $MATHTYPE_CP Ole2Xml.java Ole2XmlConverter.java```
