liblouis-utils
===============

Building blocks related to the Braille translation library
[liblouis][].

Submodules
----------

- [`liblouis-utils`](liblouis-utils/src/main)
- [`liblouis-core`](liblouis-core/src/main): Java interface for
  liblouis and liblouisutdml, and a registry for liblouis tables.
- [`liblouis-native`](liblouis-native/src/main): The precompiled C
  libraries/executables.
- [`liblouis-calabash`](liblouis-calabash/src/main): XProc bindings.
- [`liblouis-saxon`](liblouis-saxon/src/main): XPath bindings.
- [`liblouis-tables`](liblouis-tables/src/main): The default
  translation tables that come with liblouis.
- [`liblouis-formatter`](liblouis-formatter/src/main): XProc step for
  converting XML with inline Braille CSS to PEF using liblouisutdml.
- [`liblouis-mathml`](liblouis-mathml/src/main): XProc step for
  translating MathML to Braille using liblouisutdml.

[liblouis]: https://code.google.com/p/liblouis
