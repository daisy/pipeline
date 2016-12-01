xprocspec documentation
=======================

The documentation for the xprocspec grammar is available online at http://daisy-pipeline.github.io/xprocspec/documentation/index.html.

The documentation is generated based on the Relax NG schema (which can be found through the link above).

To generate the documentation, follow these steps:
 * check out the @darobin/respec to a folder named "respec" in your home directory
 * sudo apt-get install phantomjs
 * you may have to install node/npm as well (not sure)
 * run make.xpl

make.xpl will convert the Relax NG schema for xprocspec (xprocspec.rng) into HTML, merge it with template.html, and store the result as generated.html. Then it will run phantomjs with respec2html.js and generated.html as input and index.html as output.
