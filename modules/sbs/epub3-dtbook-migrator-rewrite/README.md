Nordic EPUB3/DTBook Migrator
============================

[![Build Status](https://travis-ci.org/nlbdev/nordic-epub3-dtbook-migrator.svg)](https://travis-ci.org/nlbdev/nordic-epub3-dtbook-migrator)

The main goal of this project is to provide a EPUB3 to DTBook conversion tool
for the libraries in the Nordic countries providing accessible litterature to
visually impaired readers ([NLB](http://www.nlb.no/), [MTM](http://mtm.se/),
[SPSM](http://www.spsm.se/),
[Celia](http://www.celia.fi/), [Nota](http://www.nota.nu/) and [SBS](http://sbs.ch/)).
The conversion will be implemented in XProc and XSLT and provided as a
[DAISY Pipeline 2](http://www.daisy.org/pipeline2) script.
This conversion will allow the organizations to continue to use their respective
DTBook-based tools for production of Braille and Synthetic Speech,
as long as those are necessary.

This tool attempts to map EPUB3 to DTBook with as little loss as possible (a 1:1 mapping).

While the EPUB3 will consist of multiple HTML files internally, an intermediate
single-page HTML representation is useful for converting to and from DTBook.

Scripts
-------

This project provides the following Pipeline 2 scripts:

 * EPUB3 to DTBook
 * EPUB3 to HTML
 * EPUB3 Validator
 * EPUB3 ASCIIMath to MathML
 * HTML to EPUB3
 * HTML to DTBook
 * HTML Validator
 * DTBook to EPUB3
 * DTBook to HTML
 * DTBook Validator

The EPUB3 to DTBook script will be used to allow new EPUB3 files to be used
with legacy DTBook-based systems.

The DTBook to EPUB3 script allows legacy DTBooks to be upgraded to new
EPUB3-based production systems.

Scripts for converting to and from the intermediary single-HTML representation
of the publications are also provided. These are useful either for debugging,
or if a single-document HTML representation is needed as input to or output from
a HTML-based production system.

Validators for EPUB3, DTBook and single-document HTML files are provided.
The EPUB3 validator allows us to check that new EPUB3 files are valid according
to the nordic markup guidelines. The DTBook and HTML validators can be useful
for DTBook- or HTML-based production systems.

In the nordic markup guidelines, math is marked up using ASCIIMath.
An experimental script for converting this ASCIIMath to MathML is provided.

The grammar used in the EPUB3, HTML and DTBook files is a strict subset of EPUB3, HTML and DTBook,
and is defined in the Nordic markup guidelines. Most DTBooks will work with these scripts,
there are few limitations to the input DTBook grammar. There are more limitations to the HTML/EPUB3
grammar however, because there must be a way to convert it to DTBook.
Most notably, multimedia such as audio and video are currently not allowed in these EPUB3s.

Building
--------

The nordic migrator can be built with Maven,
either directly (with for instance `mvn clean package`),
or indirectly with Docker (with for instance `docker build .`).

References
----------

See [the project homepage](http://nlbdev.github.io/nordic-epub3-dtbook-migrator/) for more information.

