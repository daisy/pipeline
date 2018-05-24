#!/bin/bash
# For producing HTML5 outputs via XSweet XSLT from sources extracted from .docx (Office Open XML)

# $DOCNAME is any short identifier
DOCNAME="DEMOTEXT"                                          

# $DOCXdocumentXML is the 'word/document.xml' file extracted (unzipped) from a .docx file
# (Also, its neighbor files from the .docx package should be available.)
DOCXdocumentXML="Demo_text_docx/word/document.xml"

# Bind $DOCXdocumentXML and $DOCNAME via $1 and $2 and try
# CL > ./ExtractandRefine.sh yourdocumentname path/to/your/document.xml
# (Which would make it possible to call this script from another one
# and even loop over file sets.)

# Note Saxon is included with this distribution, qv for license.
saxonHE="java -jar lib/SaxonHE9-7-0-8J/saxon9he.jar"  # SaxonHE (XSLT 2.0 processor)
EXTRACT="docx-html-extract.xsl"                       # "Extraction" stylesheet
REFINE1="handle-notes.xsl"                            # "Refinement" stylesheets
REFINE2="scrub.xsl"
REFINE3="join-elements.xsl"
REFINE4="zorba-map.xsl"
RIPTEXT="plaintext.xsl"

# Intermediate and final outputs (serializations) are all left on the file system.

$saxonHE -xsl:$EXTRACT -s:$DOCXdocumentXML        -o:$DOCNAME-EXTRACTED.html
echo Made $DOCNAME-EXTRACTED.html

$saxonHE -xsl:$REFINE1 -s:$DOCNAME-EXTRACTED.html -o:$DOCNAME-REFINED_1.html
echo Made $DOCNAME-REFINED_1.html

$saxonHE -xsl:$REFINE2 -s:$DOCNAME-REFINED_1.html -o:$DOCNAME-REFINED_2.html
echo Made $DOCNAME-REFINED_2.html

$saxonHE -xsl:$REFINE3 -s:$DOCNAME-REFINED_2.html -o:$DOCNAME-REFINED_3.html
echo Made $DOCNAME-REFINED_3.html

$saxonHE -xsl:$REFINE4 -s:$DOCNAME-REFINED_3.html -o:$DOCNAME-REFINED_4.html
echo Made $DOCNAME-REFINED_4.html

$saxonHE -xsl:$RIPTEXT -s:$DOCNAME-REFINED_4.html -o:$DOCNAME.txt
echo Made $DOCNAME.txt

