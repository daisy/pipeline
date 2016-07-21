pipeline-braille-utils
======================

Library of building blocks that the
[top-level scripts](../pipeline-braille-scripts) are made up from. The
building blocks are divided into logical *groups*:

- [`common-utils`](common-utils/src/main)
- [`css-utils`](css-utils)
- [`dotify-utils`](dotify-utils)
- [`libhyphen-utils`](libhyphen-utils)
- [`liblouis-utils`](liblouis-utils)
- [`pef-utils`](pef-utils)
- [`texhyph-utils`](texhyph-utils)

Each of these utility modules collect all the XSLT/XPath functions and
XProc steps of that group into one `library.xsl` and one
`library.xpl`, so that they can be made available with a single
import. These library files make up the interface between the scripts
and the utils, scripts should not have to use any lower-level parts
directly.

The implementation of the functions and steps can either be found in
the utils module itself (which is mostly the case when they are
written in XSLT/XProc), or otherwise in a submodule. Submodules are
grouped in a directory with the same name as the utils module minus
the `-utils`.  They typically contain Java code. Some recurring types
of modules are modules for Saxon XPath extension functions (ending in
`-saxon`) and modules for custom Calabash steps (`-calabash`).
Generally, the XPath and XProc bindings are separated from the core
functionality (`-core` modules). Precompiled native binaries are
bundled in `-native` modules.
