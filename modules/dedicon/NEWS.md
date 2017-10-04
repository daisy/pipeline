v1.6.3
======

Includes
--------

- Symbolenlijst (https://dedicon.atlassian.net/browse/PI3-13)
- Workaround for backslashes in src attributes (https://github.com/daisy/pipeline-mod-braille/issues/162)
- Simplified test environment
- Unit test for out-of-memory exception (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/58)
- Remove hack for resetting print page numbers on frontmatter/bodymatter/rearmatter/colophon
- Small improvements and fixes

v1.6.2
======

Includes
--------

- Works with the latest version of the braille-modules-parent (version 1:10:1)
- Workaround for backslashes in src attributes (https://github.com/daisy/pipeline-mod-braille/issues/162)
- Simplified test environment
- Unit test for out-of-memory exception (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/58)
- Remove hack for resetting print page numbers on frontmatter/bodymatter/rearmatter/colophon
- Small improvements and fixes

v1.6.1
======

Includes
--------

- Support list item components
- Writes BRF files in the ANSI encoding (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/44)
- Moving the print colophon to the last volume (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/46)
- Small improvements and fixes

v1.5.2
======

Includes
--------

- Splitting the output into one BRF file per volume (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/21)
- Add volume headings to document-level TOCs (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/41)
- Fully written out descriptive words instead of abbreviated indicators
  (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/42)

v1.5.1
======

Includes
--------

- Better support for images (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/12)

v1.5.0
======

Includes
--------

- Rendering of notes at the end of the block in which they are referenced
  (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/31)
- Bugfixes

v1.4.0
======

Includes
--------

- Support for generated TOC (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/14)
- Better support for generated title pages (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/16)
- Support for generated colophon (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/22)
- Better options handling (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/23)
- Support for sidebars (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/27)
- Initial support for notes (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/31)
- Support for linegroups and lines (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/32)
- Support for definition lists (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/33)
- Minor fixes.

v1.3.0
======

Includes
--------

- Documentation in the default CSS (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/10)
- Support special classes `precedingemptyline`, `indented` and `table-title`
  (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/8)
- Initial support for tables (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/11)
- Support for images and captions (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/12)
- Support for producer notes (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/13)
- Initial support for generated title pages (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/16)
- Minor fixes.

v1.2.0
======

Includes
--------

- Compatibility update

v1.1.0
======

Includes
--------

- Initial default CSS
- Improved Liblouis table for Dutch grade 0 (https://github.com/snaekobbi/liblouis/issues/6,
  https://github.com/snaekobbi/issues/issues/13)
- Custom handling of superscript and subscript (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/2)
- Handling of xml:space
- Question mark for unknown characters (https://github.com/snaekobbi/pipeline-mod-dedicon/issues/3)

v1.0.0
======

Includes
--------

- Improved Liblouis table for Dutch grade 0 (https://github.com/snaekobbi/liblouis/issues/6,
  https://github.com/snaekobbi/issues/issues/13)
- Custom Liblouis based translator that handles `html:strong` and `html:em`
  (https://github.com/snaekobbi/issues/issues/14, https://github.com/snaekobbi/pipeline-mod-braille/issues/39)
