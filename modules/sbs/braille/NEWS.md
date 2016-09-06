v1.8.0
======

Includes
--------
- Support for SJW vs. Rucksack series
- Improved rendering of volume numbers in TOC
- Improved rendering of print page numbers in TOC (https://github.com/sbsdev/pipeline-mod-sbs/issues/24)
- Various other improvements to default CSS
- Correct mapping of contraction grades (https://github.com/snaekobbi/pipeline-mod-sbs/issues/20)
- Other fixes (https://github.com/sbsdev/pipeline-mod-sbs/issues/23)

v1.7.0
======

Includes
--------
- Correct underlining of headings (https://github.com/sbsdev/pipeline-mod-sbs/issues/18)
- Fix for `text-transform: volume|volumes`

v1.6.0
======

Includes
--------
- Fix for current volume and total number of volumes on title page
- Fix for print page numbers (https://github.com/snaekobbi/pipeline-mod-sbs/issues/16)
- Fix for `prodnote`
- Internal change:
  - preserving of all inline element tags during translation phase
    (https://github.com/sbsdev/pipeline-mod-sbs/issues/14)

v1.5.0
======

Includes
--------
- Improved default CSS style sheet with support for:
  - footnotes
  - images
  - lists
  - table of contents
  - print page number ranges
  - `sidebar`
  - `prodnote`
  - `brl:running-line`
  - `brl:select`
  - class `pageref` links
- Automatic insertion of title pages (https://github.com/snaekobbi/pipeline-mod-sbs/issues/7)
- Fixes related to white space (https://github.com/snaekobbi/pipeline-mod-sbs/issues/11,
  https://github.com/sbsdev/sbs-braille-tables/issues/1)

v1.4.0
======

Includes
--------
- SBS-specific DTBook to PEF script with:
  - an SBS-specific default CSS style sheet
  - an SBS-compatible default ASCII-table
- Improved support for text-level semantics (`strong`, `em`, `dfn`, `sub`, `sup`, `code`, `abbr`,
  `brl:computer`, `brl:emph`, `brl:num`, `brl:name`, `brl:place`, `brl:v-form`, `brl:homograph`,
  `brl:date`, `brl:time`, ...) (https://github.com/snaekobbi/pipeline-mod-sbs/issues/2)
- Support for `text-transform: volume` and `text-transform: volumes`

v1.3.1
======
Bugfix release

v1.3.0
======
Compatibility update

v1.2.0
======

Includes
--------
- Support for `text-transform: print-page`

v1.1.0
======

Includes
--------
- Support for grade 0
- Bugfixes

v1.0.0
======

Includes
--------
- Custom Liblouis table from sbs-braille-tables [2.0](https://github.com/sbsdev/sbs-braille-tables/releases/tag/v2.0)
  (https://github.com/snaekobbi/liblouis/issues/5, https://github.com/snaekobbi/issues/issues/13)
- Custom hyphenation table from sbs-hyphenation-tables
  [1.17](https://github.com/sbsdev/sbs-hyphenation-tables/releases/tag/v1.17)
- Custom Liblouis based translator that handles `html:strong` and `html:em`
  (https://github.com/snaekobbi/issues/issues/14, https://github.com/snaekobbi/pipeline-mod-braille/issues/37)
