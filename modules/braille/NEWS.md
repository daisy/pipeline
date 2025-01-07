v1.15.0
=======

Changes
-------
- Support for Finnish hyphenation (https://github.com/celiafi/pipeline-mod-celia/issues/48)
- Various bugfixes and improvements

v1.14.30
========

Changes
-------
- Major code refactoring
- `-obfl-` prefix for CSS rules, properties and values is now optional
- Allow optional `-daisy-` prefix before certain rules, properties and values
- Support `border-pattern` shorthand
- Support dot pattern values in `border-style` and `border` declarations
- Changed default value for `hyphenation` option to `manual`
- Various bugfixes and improvements

v1.14.28
========

Changes
-------
- Add fallback font for HTML and PDF previews

v1.14.26
========

Changes
-------
- Update to Liblouis [3.30.0](https://github.com/liblouis/liblouis/releases/tag/v3.30.0)
- New "Include PDF" option is now also available for DTBook to braille
- Default handling of `<wbr>` (word break opportunity) tag in HTML
- Various other bugfixes and improvements

Components
----------
- **liblouis** ([**3.30.0**](https://github.com/liblouis/liblouis/releases/tag/v3.30.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.2](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- dotify ([1.0.7](https://github.com/mtmse/dotify.library/releases/tag/1.0.7)
- braille-css ([1.24.1](https://github.com/daisy/braille-css/releases/tag/1.24.1))
- **jsass** ([**5.11.1-p1**](https://github.com/daisy/jsass/releases/tag/5.11.1-p1))
- **libhyphen** ([**2.8.8-p1**](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.8.8-p1)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.25
========

Changes
-------
- Bugfixes and further improvements to braille configuration
- Option to store intermediary DTBook with CSS styles inlined

Components
----------
- liblouis ([3.27.0](https://github.com/liblouis/liblouis/releases/tag/v3.27.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.2](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- dotify ([1.0.7](https://github.com/mtmse/dotify.library/releases/tag/1.0.7)
- braille-css ([1.24.1](https://github.com/daisy/braille-css/releases/tag/1.24.1))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.24
========

Changes
-------
- Major simplification of configuration in graphical user interface.
  - Options that have no effect without custom user style sheet are only shown when the provided
    user style sheet contains the corresponding Sass variable.
  - The default style sheet has been simplified by moving several CSS rules to Sass partials.
  - The "stylesheet-parameters" option is hidden from the GUI. Instead, any Sass variable contained
    in the user style sheet is presented as an option.
  - The "transform" option is hidden from the GUI as well.
  - The "preview-table" option was made into a simple drop-down list.
  - Added the possibility to set parameters of the braille translator and formatter, such as duplex,
    from Sass.
  - The descriptions of options were simplified.

Components
----------
- liblouis ([3.27.0](https://github.com/liblouis/liblouis/releases/tag/v3.27.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.2](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- dotify ([1.0.7](https://github.com/mtmse/dotify.library/releases/tag/1.0.7)
- **braille-css** ([**1.24.1**](https://github.com/daisy/braille-css/releases/tag/1.24.1))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.21
========

Changes
-------
- Option to include PDF version of the braille result showing ASCII braille

Components
----------
- liblouis ([3.27.0](https://github.com/liblouis/liblouis/releases/tag/v3.27.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.2](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- dotify ([1.0.7](https://github.com/mtmse/dotify.library/releases/tag/1.0.7)
- **braille-css** ([**1.24.0**](https://github.com/daisy/braille-css/releases/tag/1.24.0))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.19
========

Changes
-------
- Adapt hyphenation to `xml:lang`
- Add support for `@hyphenation-resource` rules
- Add possibility to extend hyphenation table with exception words via `exception-words` descriptor
  of `@hyphenation-resource` rule
- Various bugfixes

Components
----------
- liblouis ([3.27.0](https://github.com/liblouis/liblouis/releases/tag/v3.27.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.2](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- dotify ([1.0.7](https://github.com/mtmse/dotify.library/releases/tag/1.0.7)
- **braille-css** ([**1.23.0**](https://github.com/daisy/braille-css/releases/tag/1.23.0))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.16
========

Changes
-------
- Update to Liblouis [3.27.0](https://github.com/liblouis/liblouis/releases/tag/v3.27.0)
- Fixed bug that resulted in premature line breaks
- Various other bugfixes

Components
----------
- **liblouis** ([**3.27.0**](https://github.com/liblouis/liblouis/releases/tag/v3.27.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.2](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- dotify ([1.0.7](https://github.com/mtmse/dotify.library/releases/tag/1.0.7)
- braille-css ([1.22.1](https://github.com/daisy/braille-css/releases/tag/1.22.1))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.15
========

Changes
-------
- New option to store intermediary HTML with CSS styles inlined
- Various bugfixes

Components
----------
- liblouis ([3.25.0](https://github.com/liblouis/liblouis/releases/tag/v3.27.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.2](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- **dotify** ([**1.0.7**](https://github.com/mtmse/dotify.library/releases/tag/1.0.7)
- **braille-css** ([**1.22.1**](https://github.com/daisy/braille-css/releases/tag/1.22.1))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.14
========

Changes
-------
- Update to Liblouis [3.25.0](https://github.com/liblouis/liblouis/releases/tag/v3.25.0)
- New `allow-text-overflow-trimming` option to truncate text that overflows its containing box
  (notably text within page margins that is too long to fit the space). Note that this was already a
  feature but only available through the stylesheet-parameters option and not documented.
- Fix unneeded repeating of headings in a new volume when headings have a top margin
- The default value of `levels-in-footer` is now `0`
- The `hyphens` property does not influence wrapping of compound words with a hyphen anymore
- A Libhyphen table linked using a relative path from within a `@text-transform` rule is now
  recompiled on every run (no caching)

Components
----------
- **liblouis** ([**3.25.0**](https://github.com/liblouis/liblouis/releases/tag/v3.25.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.2](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- dotify ([1.0.6](https://github.com/mtmse/dotify.library/releases/tag/1.0.6)
- **braille-css** ([**1.22.0**](https://github.com/daisy/braille-css/releases/tag/1.22.0))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.11
========

Changes
-------
- Update to Liblouis [3.24.0](https://github.com/liblouis/liblouis/releases/tag/v3.24.0)

Components
----------
- **liblouis** ([**3.24.0**](https://github.com/liblouis/liblouis/releases/tag/v3.24.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.2](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- dotify ([1.0.6](https://github.com/mtmse/dotify.library/releases/tag/1.0.6)
- braille-css ([1.21.0](https://github.com/daisy/braille-css/releases/tag/1.21.0))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.10
========

Changes
-------
- Various bugfixes

Components
----------
- liblouis ([3.21.0](https://github.com/liblouis/liblouis/releases/tag/v3.21.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.2](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- **dotify** ([**1.0.6**](https://github.com/mtmse/dotify.library/releases/tag/1.0.6)
- braille-css ([1.21.0](https://github.com/daisy/braille-css/releases/tag/1.21.0))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.8
=======

Changes
-------
- New option to control hyphenation at page and volume boundaries
  (https://github.com/daisy/pipeline-modules/issues/47,
  https://github.com/mtmse/dotify.library/pull/39)
- Allow disabling hyphenation completely (also where there are soft hyphens)
- Default handling of lists, including support for `start` and `value` attributes
- Changed default suffix of counter styles to ". "
- Deprecated the support for XSLT braille style sheets. It is now advised to apply any XSLT style
  sheets during pre-processing.
- Improved hyphenation of Norwegian text.
- Various bugfixes

Components
----------
- liblouis ([3.21.0](https://github.com/liblouis/liblouis/releases/tag/v3.21.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  **liblouis-java** ([**5.0.2**](https://github.com/liblouis/liblouis-java/releases/tag/5.0.2))
- **dotify** ([**1.0.5**](https://github.com/mtmse/dotify.library/releases/tag/1.0.5)
- braille-css ([1.21.0](https://github.com/daisy/braille-css/releases/tag/1.21.0))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

Closed issues
-------------
- https://github.com/daisy/pipeline-modules/issues/47
- https://github.com/mtmse/dotify.library/pull/39

v1.14.7
=======

Changes
-------
- Update to Liblouis [3.21.0](https://github.com/liblouis/liblouis/releases/tag/v3.21.0)
- Support for `volume` counter
- Support applying custom counter styles (defined with `@counter-style`) to page and volume counters
- Support applying counter style (using the `counter()` function) to the following numeric OBFL
  variables:
  - `-obfl-volume`
  - `-obfl-volumes`
  - `-obfl-sheets-in-document`
  - `-obfl-sheets-in-volume`
  - `-obfl-started-volume-number`
  - `-obfl-started-page-number`
  - `-obfl-started-volume-first-content-page`

Components
----------
- **liblouis** ([**3.21.0**](https://github.com/liblouis/liblouis/releases/tag/v3.21.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([5.0.1](https://github.com/liblouis/liblouis-java/releases/tag/5.0.1))
- dotify ([1.0.3](https://github.com/mtmse/dotify.library/releases/tag/1.0.3)
- braille-css ([1.21.0](https://github.com/daisy/braille-css/releases/tag/1.21.0))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.14.6
=======

Changes
-------
- New values for `notes-placement` option to specify the layout of notes: "end-of-block",
  "end-of-chapter" and "custom"
- Enhanced CSS module (Sass partial) for table styling
  - Possibility to duplicate tables (and style each copy differently)
- New CSS module (Sass partial) for styling of definition lists
- Allow PEF preview to have different representations of the same braille cell in different contexts
  (https://github.com/daisy/pipeline-mod-braille/issues/199)
- Generalized and improved `ascii-file-format` option
  - Generalized to `output-file-format`
  - Support for `(table:LOCALE)` in `output-file-format` query
  - Support for `(blank-last-page)` in `output-file-format` query to ensure that each volume has a
    blank backside
  - Support for `(sheets-multiple-of-two)` in `output-file-format` query to ensure that volumes have
    a number of sheets that is a multiple of two (for magazine layout)
  - Support for `(format:pef)` in `output-file-format` query (default value)
  - Storing (intermediary) PEF is now optional
- Support characters above U+FFFF (https://github.com/liblouis/liblouis-java/issues/20)
- Support for `hyphenate-character` property
- Support for `text-transform` inside `@left` (https://github.com/daisy/pipeline-modules/issues/36,
  https://github.com/mtmse/obfl/issues/32, https://github.com/mtmse/dotify.library/pull/33)
- Support for `counter-set` inside `@begin`
- Changed default value of "scope" argument of `flow()` function to `backward`
- Allow `flow()` function with `document` scope everywhere
- Allow `display: -obfl-list-of-references` outside of `@begin` or `@end` areas
- Allow using `flow()` instead of `-obfl-collection()`
- Deprecated `-obfl-list-of-references-range` property
- Support custom `text-transform` (defined through `@text-transform`) combined with other
  `text-transform` (e.g. `-louis-bold`)
- Fixed regression in vendor prefixed `text-transform` values (e.g. `-louis-bold`)
- Fixed line breaking around `text-transform` styled segments
- Fixed non-standard hyphenation
- Various other bugfixes

Components
----------
- **liblouis** ([**3.19.0**](https://github.com/liblouis/liblouis/releases/tag/v3.19.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  **liblouis-java** ([**5.0.1**](https://github.com/liblouis/liblouis-java/releases/tag/5.0.1))
- **dotify** ([**1.0.3**](https://github.com/mtmse/dotify.library/releases/tag/1.0.3)
- **braille-css** ([**1.21.0**](https://github.com/daisy/braille-css/releases/tag/1.21.0))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

Closed issues
-------------
- https://github.com/daisy/pipeline-modules/issues/36
- https://github.com/daisy/pipeline-mod-braille/issues/75
- https://github.com/daisy/pipeline-mod-braille/issues/163
- https://github.com/daisy/pipeline-mod-braille/issues/199
- https://github.com/mtmse/obfl/issues/32
- https://github.com/mtmse/dotify.library/pull/33
- https://github.com/mtmse/dotify.library/pull/35

v1.14.4
=======

Changes
-------
- Update to Liblouis [3.19.0](https://github.com/liblouis/liblouis/releases/tag/v3.19.0)
- Various bugfixes

Components
----------
- **liblouis** ([**3.19.0**](https://github.com/liblouis/liblouis/releases/tag/v3.19.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([4.3.1](https://github.com/liblouis/liblouis-java/releases/tag/4.3.1))
- dotify ([1.0.1](https://github.com/mtmse/dotify.library/releases/tag/1.0.1)
- braille-css ([1.19.0](https://github.com/daisy/braille-css/releases/tag/1.19.0))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))


v1.14.3
=======

Changes
-------
- Update to Liblouis [3.17.0](https://github.com/liblouis/liblouis/releases/tag/v3.17.0)
- New option to choose from a list of braille codes (Liblouis tables)
  (https://github.com/daisy/pipeline-modules/issues/38)
- Possibility to specify the braille code's locale as a transformer feature
  (e.g. `(locale:nl)`). The input document's language is only used if no locale or Liblouis table
  has been specified. (https://github.com/daisy/pipeline-modules/issues/37)
- By default text is transcribed to braille during formatting. A `force-pre-translation` transformer
  feature was added to override this behavior.
- Option to specify the layout of notes
- CSS module (Sass partial) for table styling
- Support for `@counter-style` rules
- Support for relative `url()` values
- Support for `:-obfl-alternate-scenario(2)` etc.
- Support for `-obfl-list-of-references-range: volume`
- Support for new `::-obfl-on-collection-start` and `::-obfl-on-collection-end` pseudo-elements
- Support for `display: -obfl-list-of-references` on any element (not only on `::alternate`
  elements)
- Improved hyphenation of German text.
- Fixed bug in handling of `-obfl-marker` property resulting in missing markers in page margin
  (https://github.com/mtmse/dotify.library/pull/19)
- Fixed translation of text around `<br/>` elements (https://github.com/daisy/pipeline/issues/603,
  https://github.com/mtmse/dotify.library/pull/17)
- Various other bugfixes (https://github.com/mtmse/dotify.library/pull/9,
  https://github.com/mtmse/dotify.library/pull/14, https://github.com/mtmse/dotify.library/pull/15)

Components
----------
- **liblouis** ([**3.17.0**](https://github.com/liblouis/liblouis/releases/tag/v3.17.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([4.3.1](https://github.com/liblouis/liblouis-java/releases/tag/4.3.1))
- **dotify** ([**1.0.1**](https://github.com/mtmse/dotify.library/releases/tag/1.0.1)
- braille-css ([1.19.0](https://github.com/daisy/braille-css/releases/tag/1.19.0))
- jsass ([5.10.4-p1](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

Closed issues
-------------
- https://github.com/daisy/pipeline/issues/603
- https://github.com/daisy/pipeline-modules/issues/37
- https://github.com/daisy/pipeline-modules/issues/38
- https://github.com/mtmse/dotify.library/pull/9
- https://github.com/mtmse/dotify.library/pull/10
- https://github.com/mtmse/dotify.library/pull/14
- https://github.com/mtmse/dotify.library/pull/15
- https://github.com/mtmse/dotify.library/pull/17
- https://github.com/mtmse/dotify.library/pull/19
- https://github.com/daisy/braille-css/issues/26


v1.14.2
=======

Changes
-------
- Option to exclude certain headings from the generated braille TOC. (You may need to use this
  option because the way the TOC is generated is slightly different than before.)
- Support `white-space` on `-obfl-evaluate()`
- Improved support for laying out tables as lists
  - Changed meaning of `::table-by()` pseudo-element (which is yet to be documented)
  - Preserve styling of table cells when rearranging them
  - Support noterefs in table header cells
  - Fixed possible error when colspan/rowspan present
- Improved volume breaking speed and quality
  (https://github.com/mtmse/dotify.formatter.impl/pull/40)
- Fixed stacked pseudo-elements and pseudo-classes on pseudo-elements in combination with @extend
  (Sass)
- Fixed several bugs related to leaders (https://github.com/mtmse/dotify.formatter.impl/pull/37)
- Fixed `::obfl-on-resumed` bug (https://github.com/mtmse/dotify.formatter.impl/issues/42)
- Fixed `volume-break-after` bug
- Various other bugfixes and improvements

Components
----------
- liblouis ([3.16.0](https://github.com/liblouis/liblouis/releases/tag/v3.16.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([4.3.1](https://github.com/liblouis/liblouis-java/releases/tag/4.3.1))
- **dotify** ([**1.0.0**](https://github.com/mtmse/dotify.library/releases/tag/1.0.0)
- **braille-css** ([**1.19.0**](https://github.com/daisy/braille-css/releases/tag/1.19.0))
- **jsass** ([**5.10.4-p1**](https://github.com/daisy/jsass/releases/tag/5.10.4-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

Closed issues
-------------
- https://github.com/mtmse/dotify.formatter.impl/pull/37
- https://github.com/mtmse/dotify.formatter.impl/issues/38
- https://github.com/mtmse/dotify.formatter.impl/pull/39
- https://github.com/mtmse/dotify.formatter.impl/pull/40
- https://github.com/mtmse/dotify.formatter.impl/issues/42
- https://github.com/mtmse/dotify.formatter.impl/pull/43

v1.14.0
=======

Changes
-------
- Update to Liblouis [3.16.0](https://github.com/liblouis/liblouis/releases/tag/v3.16.0)
- Support for `-obfl-right-text-indent` (https://github.com/sbsdev/pipeline-mod-sbs/issues/51,
  https://github.com/nlbdev/pipeline/issues/169, https://github.com/mtmse/obfl/pull/9,
  https://github.com/mtmse/dotify.formatter.impl/pull/31)
- Support for `volume-break-after` (https://github.com/daisy/pipeline-modules/issues/32)
- Support for `:top-of-page` pseudo-class (limited to `display: none`)
- Support for media queries ([`width` and
  `height`](https://www.w3.org/TR/mediaqueries-4/#mf-dimensions))
  (https://github.com/daisy/pipeline-modules/issues/31)
- Allow XSLT style sheets to be applied before TOC is generated
- Add `stylesheet-parameters` option for specifying values for XSLT parameters and Sass variables
  used in style sheets that were provided through the `stylesheets` option.
- HTML to PEF: add features that were already present on other braille scripts
  - Support for XSLT in `stylesheet` option
  - Make `include-obfl` output OBFL even when conversion to PEF fails
- Improved support for `text/x-scss` media type
- Support for integer values in `@text-transform` rules
- `title` attributes in generated TOC
- Bugs fixed in generated TOC
- Fixed concurrency issue (mixed up braille output due to multiple concurrent Liblouis threads)
- Fixed bug causing double application of `text-transform` on pseudo-elements
- Various other bugfixes

Components
----------
- **liblouis** ([**3.16.0**](https://github.com/liblouis/liblouis/releases/tag/v3.16.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  **liblouis-java** ([**4.3.1**](https://github.com/liblouis/liblouis-java/releases/tag/4.3.1))
- **dotify** (**api** [**5.0.7**](https://github.com/mtmse/dotify.api/releases/tag/releases%2Fv5.0.7), common
  [4.4.1](https://github.com/mtmse/dotify.common/releases/tag/releases%2Fv4.4.1), hyphenator.impl
  [5.0.0](https://github.com/brailleapps/dotify.hyphenator.impl/releases/tag/releases%2Fv5.0.0), translator.impl
  [5.0.0](https://github.com/brailleapps/dotify.translator.impl/releases/tag/releases%2Fv5.0.0), **formatter.impl**
  [**5.0.7**](https://github.com/mtmse/dotify.formatter.impl/releases/tag/releases%2Fv5.0.7), text.impl
  [5.0.0](https://github.com/brailleapps/dotify.text.impl/releases/tag/releases%2Fv5.0.0), streamline-api
  [1.5.0](https://github.com/brailleapps/streamline-api/releases/tag/releases%2Fv1.5.0), streamline-engine
  [1.3.0](https://github.com/brailleapps/streamline-engine/releases/tag/releases%2Fv1.3.0), task.impl
  [5.0.0](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv5.0.0))
- brailleutils (api
  [3.0.1](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.1), impl
  [3.0.0](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0), pef-tools
  [2.2.0](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.2.0))
- **braille-css** ([**1.18.0**](https://github.com/daisy/braille-css/releases/tag/1.18.0))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

Closed issues
-------------
- https://github.com/daisy/pipeline-modules/issues/31
- https://github.com/daisy/pipeline-modules/issues/32
- https://github.com/mtmse/obfl/pull/9
- https://github.com/mtmse/obfl/issues/29
- https://github.com/mtmse/dotify.api/pull/12
- https://github.com/mtmse/dotify.api/pull/15
- https://github.com/mtmse/dotify.formatter.impl/pull/31
- https://github.com/mtmse/dotify.formatter.impl/issues/34
- https://github.com/mtmse/dotify.formatter.impl/issues/35

v1.13.7
=======

Changes
-------
- Support for `@text-transform foo { system: braille-translator }` rules to let custom
  sub-translators handle specific parts of the document
  (https://github.com/daisy/pipeline-mod-braille/issues/196,
  https://github.com/daisy/pipeline-mod-braille/issues/191)
- Improved braille translation by performing Unicode normalization of text when needed
  (https://github.com/daisy/pipeline-mod-braille/issues/197)
- New `allow-volume-break-inside-leaf-section-factor` and
  `prefer-volume-break-before-higher-level-factor` settings for more fine-grained control over
  volume breaking (https://github.com/daisy/pipeline-mod-braille/issues/205)
- Support for `(dots-for-undefined-char:'...')` in translator query
  (https://github.com/daisy/pipeline-mod-braille/issues/206)
- New "preamble" option in EPUB 3 to PEF to add boilerplate text
  (https://github.com/daisy/pipeline-mod-braille/issues/194)
- Support for embedded SCSS stylesheets (with media type `text/x-scss`)
- Various other improvements and bugfixes (https://github.com/daisy/pipeline-modules/issues/24,
  https://github.com/daisy/pipeline-mod-braille/issues/204, ...)

Components
----------
- liblouis ([3.13.0](https://github.com/liblouis/liblouis/releases/tag/v3.13.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  **liblouis-java** ([**4.3.0**](https://github.com/liblouis/liblouis-java/releases/tag/4.3.0))
- dotify (api 5.0.5, common
  [4.4.1](https://github.com/mtmse/dotify.common/releases/tag/releases%2Fv4.4.1), hyphenator.impl
  [5.0.0](https://github.com/brailleapps/dotify.hyphenator.impl/releases/tag/releases%2Fv5.0.0), translator.impl
  [5.0.0](https://github.com/brailleapps/dotify.translator.impl/releases/tag/releases%2Fv5.0.0), formatter.impl
  [5.0.6](https://github.com/mtmse/dotify.formatter.impl/releases/tag/releases%2Fv5.0.6), text.impl
  [5.0.0](https://github.com/brailleapps/dotify.text.impl/releases/tag/releases%2Fv5.0.0), streamline-api
  [1.5.0](https://github.com/brailleapps/streamline-api/releases/tag/releases%2Fv1.5.0), streamline-engine
  [1.3.0](https://github.com/brailleapps/streamline-engine/releases/tag/releases%2Fv1.3.0), task.impl
  [5.0.0](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv5.0.0))
- brailleutils (api
  [3.0.1](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.1), impl
  [3.0.0](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0), pef-tools
  [2.2.0](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.2.0))
- **braille-css** ([**1.17.0**](https://github.com/daisy/braille-css/releases/tag/1.17.0))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

Closed issues
-------------
- https://github.com/daisy/pipeline-mod-braille/issues/191
- https://github.com/daisy/pipeline-mod-braille/issues/194
- https://github.com/daisy/pipeline-mod-braille/issues/196
- https://github.com/daisy/pipeline-mod-braille/issues/197
- https://github.com/daisy/pipeline-mod-braille/issues/200
- https://github.com/daisy/pipeline-mod-braille/issues/204
- https://github.com/daisy/pipeline-mod-braille/issues/205
- https://github.com/daisy/pipeline-mod-braille/issues/206

v1.13.6
=======

Changes
-------
- Support for margin, padding and border properties inside `@sequence-interrupted`,
  `@sequence-resumed`, `@any-interrupted` and `@any-resumed`
- Fix regression in `target-counter()`, `target-string()`, `target-text()` and `target-content()`

v1.13.5
=======

Changes
-------
- Update to Liblouis [3.13.0](https://github.com/liblouis/liblouis/releases/tag/v3.13.0)
  (https://github.com/daisy/pipeline-mod-braille/issues/198)
- HTML's `lang` attribute is now recognized.
- Various changes to support repeating a heading when a section is resumed in a new volume
  - Support for `string(...)` inside `@-obfl-volume-transition`
    (https://github.com/mtmse/obfl/issues/8, https://github.com/mtmse/dotify.formatter.impl/pull/16)
  - Support for `string(..., start-except-first)`
  - New OBFL variable `$started-volume-first-content-page` in the context of a `::obfl-on-resumed`
    pseudo-element (https://github.com/mtmse/obfl/issues/3,
    https://github.com/mtmse/dotify.formatter.impl/issues/18)
- Various smaller fixes and invisible changes

Components
----------
- **liblouis** ([**3.13.0**](https://github.com/liblouis/liblouis/releases/tag/v3.13.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([4.2.0](https://github.com/liblouis/liblouis-java/releases/tag/4.2.0))
- **dotify** (**api** **5.0.5**, **common**
  [**4.4.1**](https://github.com/mtmse/dotify.common/releases/tag/releases%2Fv4.4.1), hyphenator.impl
  [5.0.0](https://github.com/brailleapps/dotify.hyphenator.impl/releases/tag/releases%2Fv5.0.0), translator.impl
  [5.0.0](https://github.com/brailleapps/dotify.translator.impl/releases/tag/releases%2Fv5.0.0), **formatter.impl**
  [**5.0.6**](https://github.com/mtmse/dotify.formatter.impl/releases/tag/releases%2Fv5.0.6), text.impl
  [5.0.0](https://github.com/brailleapps/dotify.text.impl/releases/tag/releases%2Fv5.0.0), streamline-api
  [1.5.0](https://github.com/brailleapps/streamline-api/releases/tag/releases%2Fv1.5.0), streamline-engine
  [1.3.0](https://github.com/brailleapps/streamline-engine/releases/tag/releases%2Fv1.3.0), task.impl
  [5.0.0](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv5.0.0))
- brailleutils (api
  [3.0.1](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.1), impl
  [3.0.0](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0), pef-tools
  [2.2.0](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.2.0))
- **braille-css** ([**1.16.0**](https://github.com/daisy/braille-css/releases/tag/1.16.0))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.13.4
=======

Changes
-------
- Changed behavior of `::obfl-on-resumed` pseudo-element

v1.13.3
=======

Changes
-------
- Support for `::obfl-on-resumed` pseudo-element inside TOCs
  (https://github.com/mtmse/obfl/issues/2)

Components
----------
- liblouis ([3.11.0](https://github.com/liblouis/liblouis/releases/tag/v3.11.0)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([4.2.0](https://github.com/liblouis/liblouis-java/releases/tag/4.2.0))
- **dotify** (**api** **5.0.2**, common
  [4.4.0](https://github.com/brailleapps/dotify.common/releases/tag/releases%2Fv4.4.0), hyphenator.impl
  [5.0.0](https://github.com/brailleapps/dotify.hyphenator.impl/releases/tag/releases%2Fv5.0.0), translator.impl
  [5.0.0](https://github.com/brailleapps/dotify.translator.impl/releases/tag/releases%2Fv5.0.0), **formatter.impl**
  [**5.0.4**](https://github.com/mtmse/dotify.formatter.impl/releases/tag/releases%2Fv5.0.4), text.impl
  [5.0.0](https://github.com/brailleapps/dotify.text.impl/releases/tag/releases%2Fv5.0.0), streamline-api
  [1.5.0](https://github.com/brailleapps/streamline-api/releases/tag/releases%2Fv1.5.0), streamline-engine
  [1.3.0](https://github.com/brailleapps/streamline-engine/releases/tag/releases%2Fv1.3.0), task.impl
  [5.0.0](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv5.0.0))
- brailleutils (api
  [3.0.1](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.1), impl
  [3.0.0](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0), pef-tools
  [2.2.0](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.2.0))
- braille-css ([1.15.0](https://github.com/daisy/braille-css/releases/tag/1.15.0))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.2](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.13.2
=======

Changes
-------
- Support `text-transform: uncontracted` (https://github.com/daisy/pipeline-modules/issues/9)
- Support for `@any-interrupted` and `@any-resumed`
- Support for `-obfl-evaluate(...)` inside `@sequence-interrupted`, `@sequence-resumed`,
  `@any-interrupted` and `@any-resumed`
- Bugfixes in white space processing, line breaking and page breaking

v1.13.1
=======

Changes
-------
- Bugfixes

v1.13.0
=======

Changes
-------
- Update to Liblouis [3.11.0](https://github.com/liblouis/liblouis/releases/tag/v3.11.0)
  (https://github.com/daisy/pipeline-mod-braille/issues/190)
- Update to latest Dotify which includes many improvements, notably some important bugfixes in the
  formatter (see [release notes of
  v5.0.2](https://github.com/brailleapps/dotify.formatter.impl/releases/tag/releases%2Fv5.0.2))
- Support for flowing of text into the page header or footer area (set `margin-top: 0` or
  `margin-bottom: 0` on a `@page` rule that has content in the margin)
  (https://github.com/sbsdev/pipeline-mod-sbs/issues/37,
  https://github.com/brailleapps/dotify.formatter.impl/issues/63)
- `target-counter()` function can reference span elements
  (https://github.com/sbsdev/pipeline-mod-sbs/issues/60,
  https://github.com/brailleapps/dotify.formatter.impl/issues/25,
  https://github.com/brailleapps/dotify.formatter.impl/pull/44)
- Both `@volume:first` and `@volume:last` rules match when there is only a single volume
- Allow the evaluation of `$volume` (with `-obfl-evaluate()`) everywhere
  (https://github.com/sbsdev/pipeline-mod-sbs/issues/68)
- Support for preserving white space in `-obfl-evaluate()` function
  (https://github.com/sbsdev/pipeline-mod-sbs/issues/68,
  https://github.com/brailleapps/dotify.formatter.impl/pull/99)
- Support for `::alternate(2)`, `::alternate(3)`,
  etc. (https://github.com/sbsdev/pipeline-mod-sbs/issues/68)
- Support for `text-transform: -louis-foo` when `foo` is the name of an emphasis class in the
  Liblouis table.
- Avoid line breaks between segments that are not separated by white space, such as a text node and
  an immediately following note reference (https://github.com/sbsdev/pipeline-mod-sbs/issues/63)
- Enhanced the CSS module for volume breaking so that preference is
  given to split points before higher level chapters
- Fixed bug in non-standard hyphenation that caused case information to get lost
- Improved PEF preview so that all empty pages are visible
- Various bugfixes (https://github.com/nlbdev/pipeline/issues/141,
  https://github.com/brailleapps/dotify.formatter.impl/pull/94, ...)
- Various invisible changes

Components
----------
- **liblouis** ([**3.11.0**](https://github.com/liblouis/liblouis/releases/tag/v3.11.0)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), **liblouis-java**
  ([**4.2.0**](https://github.com/liblouis/liblouis-java/releases/tag/4.2.0))
- **dotify** (**api** [**5.0.0**](https://github.com/brailleapps/dotify.api/releases/tag/releases%2Fv5.0.0), **common**
  [**4.4.0**](https://github.com/brailleapps/dotify.common/releases/tag/releases%2Fv4.4.0), **hyphenator.impl**
  [**5.0.0**](https://github.com/brailleapps/dotify.hyphenator.impl/releases/tag/releases%2Fv5.0.0), **translator.impl**
  [**5.0.0**](https://github.com/brailleapps/dotify.translator.impl/releases/tag/releases%2Fv5.0.0), **formatter.impl**
  **5.0.2-RC2**, **text.impl**
  [**5.0.0**](https://github.com/brailleapps/dotify.text.impl/releases/tag/releases%2Fv5.0.0), **streamline-api**
  [**1.5.0**](https://github.com/brailleapps/streamline-api/releases/tag/releases%2Fv1.5.0), **streamline-engine**
  [**1.3.0**](https://github.com/brailleapps/streamline-engine/releases/tag/releases%2Fv1.3.0), **task.impl**
  [**5.0.0**](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv5.0.0))
- brailleutils (api
  [3.0.1](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.1), impl
  [3.0.0](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0), pef-tools
  [2.2.0](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.2.0))
- **braille-css** ([**1.15.0**](https://github.com/daisy/braille-css/releases/tag/1.15.0))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), **jhyphen**
  ([**1.0.2**](https://github.com/daisy/jhyphen/releases/tag/1.0.2))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.12.0
=======

Changes
-------
- Support for fractional `line-height`
- Bugfixes

v1.11.2
=======

Changes
-------
- Syntax of style attributes has changed:
  http://braillespecs.github.io/braille-css/20181031/#style-attribute
- Support for `:-obfl-alternate-scenario` pseudo-class and `-obfl-scenario-cost` property
  (https://github.com/nlbdev/pipeline/issues/207)
- Support for `@-obfl-volume-transition` rules (https://github.com/braillespecs/obfl/issues/70,
  https://github.com/sbsdev/pipeline-mod-sbs/issues/68)
- CSS/XSL module for basic volume breaking
  (https://github.com/daisy/pipeline-mod-braille/issues/182,
  https://github.com/daisy/pipeline-mod-braille/pull/186)
- Various invisible changes (https://github.com/daisy/pipeline-mod-braille/issues/99,
  https://github.com/daisy/pipeline-mod-braille/pull/171,
  https://github.com/daisy/pipeline-mod-braille/pull/180, ...)
- Various bugfixes

Components
----------
- liblouis ([3.6.0](https://github.com/liblouis/liblouis/releases/tag/v3.6.0)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v)), liblouis-java
  ([3.1.0](https://github.com/liblouis/liblouis-java/releases/tag/3.1.0))
- **dotify** (**api** [**4.4.0**](https://github.com/brailleapps/dotify.api/releases/tag/releases%2Fv4.4.0), **common**
  [**4.3.0**](https://github.com/brailleapps/dotify.common/releases/tag/releases%2Fv4.3.0), hyphenator.impl
  [4.0.0](https://github.com/brailleapps/dotify.hyphenator.impl/releases/tag/releases%2Fv4.0.0), translator.impl
  [4.0.0](https://github.com/brailleapps/dotify.translator.impl/releases/tag/releases%2Fv4.0.0), **formatter.impl**
  [**4.4.0**](https://github.com/brailleapps/dotify.formatter.impl/releases/tag/releases%2Fv4.4.0), text.impl
  [4.0.0](https://github.com/brailleapps/dotify.text.impl/releases/tag/releases%2Fv4.0.0), **streamline-api**
  [**1.3.0**](https://github.com/brailleapps/streamline-api/releases/tag/releases%2Fv1.3.0), **streamline-engine**
  [**1.2.0**](https://github.com/brailleapps/streamline-engine/releases/tag/releases%2Fv1.2.0), **task.impl**
  [**4.5.0**](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv4.5.0))
- brailleutils (api
  [3.0.1](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.1), impl
  [3.0.0](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0), pef-tools
  [2.2.0](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.2.0))
- **braille-css** ([**1.14.0**](https://github.com/daisy/braille-css/releases/tag/1.14.0))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/)), jhyphen
  ([1.0.0](https://github.com/daisy/jhyphen/releases/tag/v1.0.0))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.11.1
=======

Changes
-------
- Support for extended `leader(<braille-string>[,[<integer>|<percentage>][,[left|center|right]]?]?)`
  function (https://github.com/sbsdev/pipeline-mod-sbs/issues/51,
  https://github.com/nlbdev/pipeline/issues/169)
- Support for `text-transform: -louis-emph-4` to `-louis-emph-10`
  (https://github.com/nlbdev/pipeline/issues/107)
- Support for `::after` and `::before` pseudo-elements inside elements with `display: none`
- Bugfixes (https://github.com/daisy/pipeline-mod-braille/issues/173, ...)
- Update to Liblouis 3.6.0

Components
----------
- **liblouis** ([**3.6.0**](https://github.com/liblouis/liblouis/releases/tag/v3.6.0)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),,
  liblouis-java ([3.1.0](https://github.com/liblouis/liblouis-java/releases/tag/3.1.0))
- dotify (api [4.1.0](https://github.com/brailleapps/dotify.api/releases/tag/releases%2Fv4.1.0), common
  [4.1.0](https://github.com/brailleapps/dotify.common/releases/tag/releases%2Fv4.1.0), hyphenator.impl
  [4.0.0](https://github.com/brailleapps/dotify.hyphenator.impl/releases/tag/releases%2Fv4.0.0), translator.impl
  [4.0.0](https://github.com/brailleapps/dotify.translator.impl/releases/tag/releases%2Fv4.0.0), formatter.impl
  [4.1.0](https://github.com/brailleapps/dotify.formatter.impl/releases/tag/releases%2Fv4.1.0), text.impl
  [4.0.0](https://github.com/brailleapps/dotify.text.impl/releases/tag/releases%2Fv4.0.0), streamline-api
  [1.0.0](https://github.com/brailleapps/streamline-api/releases/tag/releases%2Fv1.0.0), streamline-engine
  [1.1.0](https://github.com/brailleapps/streamline-engine/releases/tag/releases%2Fv1.1.0), task.impl
  [4.1.0](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv4.1.0))
- brailleutils (api
  [3.0.1](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.1), impl
  [3.0.0](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0), pef-tools
  [2.2.0](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.2.0))
- braille-css ([1.13.0](https://github.com/daisy/braille-css/releases/tag/1.13.0))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.0](https://github.com/daisy/jhyphen/releases/tag/v1.0.0))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.11.0
=======

Changes
-------
- New script for adding a braille rendition to an EPUB
  (https://github.com/snaekobbi/pipeline-mod-braille/pull/6,
  https://github.com/daisy/pipeline-mod-braille/issues/164,
  https://github.com/sbsdev/pipeline-mod-sbs/issues/58)
- More usable message log (https://github.com/daisy/pipeline-mod-braille/issues/38)
- `include-obfl` option now outputs OBFL even when conversion to PEF fails
  (https://github.com/daisy/pipeline-mod-braille/issues/124)
- Support for custom page counters (https://github.com/brailleapps/dotify/issues/165,
  https://github.com/brailleapps/dotify/issues/180,
  https://github.com/braillespecs/braille-css/issues/47)
- Fixes to volume breaking (https://github.com/brailleapps/dotify.formatter.impl/pull/28,
  https://github.com/nlbdev/pipeline/issues/80, https://github.com/nlbdev/pipeline/issues/118,
  https://github.com/nlbdev/pipeline/issues/121,
  https://github.com/sbsdev/pipeline-mod-sbs/issues/33,
  https://github.com/daisy/pipeline-mod-braille/issues/136)
- Fixed handling of cross-references in EPUB 3
  (https://github.com/daisy/pipeline-mod-braille/issues/126)
- Improved language detection in EPUB 3
- Addition of `dc:language` in PEF metadata
- Improved white space handling
- Fixed behavior of `target-content()` w.r.t. pseudo-elements
- Fixed border alignment (https://github.com/nlbdev/pipeline/issues/128)
- Support for `page` property inside `::before` and `::after` pseudo-elements
- Fixed support for `line-height` in combination with page footer
  (https://github.com/brailleapps/dotify/issues/196,
  https://github.com/brailleapps/dotify.formatter.impl/pull/29)
- Limited support of `target-counter()` to elements in normal flow
- Various other bugfixes (`text-transform`, `-obfl-fallback-collection`, ...)

Components
----------
- liblouis ([3.0.0.alpha1](https://github.com/liblouis/liblouis/releases/tag/v3.0.0.alpha1)),
  liblouisutdml ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)),
  liblouis-java ([3.1.0](https://github.com/liblouis/liblouis-java/releases/tag/3.1.0))
- **dotify** (**api** [**4.1.0**](https://github.com/brailleapps/dotify.api/releases/tag/releases%2Fv4.1.0), **common**
  [**4.1.0**](https://github.com/brailleapps/dotify.common/releases/tag/releases%2Fv4.1.0), **hyphenator.impl**
  [**4.0.0**](https://github.com/brailleapps/dotify.hyphenator.impl/releases/tag/releases%2Fv4.0.0), **translator.impl**
  [**4.0.0**](https://github.com/brailleapps/dotify.translator.impl/releases/tag/releases%2Fv4.0.0), **formatter.impl**
  [**4.1.0**](https://github.com/brailleapps/dotify.formatter.impl/releases/tag/releases%2Fv4.1.0), **text.impl**
  [**4.0.0**](https://github.com/brailleapps/dotify.text.impl/releases/tag/releases%2Fv4.0.0), **streamline-api**
  [**1.0.0**](https://github.com/brailleapps/streamline-api/releases/tag/releases%2Fv1.0.0), **streamline-engine**
  [**1.1.0**](https://github.com/brailleapps/streamline-engine/releases/tag/releases%2Fv1.1.0), **task.impl**
  [**4.1.0**](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv4.1.0))
- **brailleutils** (**api**
  [**3.0.1**](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.1), **impl**
  [**3.0.0**](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0), **pef-tools**
  [**2.2.0**](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.2.0))
- braille-css ([1.13.0](https://github.com/daisy/braille-css/releases/tag/1.13.0))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.0](https://github.com/daisy/jhyphen/releases/tag/v1.0.0))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.10.1
=======

Changes
-------
- Liblouis update
- Dotify update (https://github.com/daisy/pipeline-mod-braille/pull/138,
  https://github.com/daisy/pipeline-mod-braille/pull/139)
- Improved performance (https://github.com/snaekobbi/issues/issues/28,
  https://github.com/ndw/xmlcalabash1/pull/256) <!-- fixed memory leak and various step
  optimizations -->
- Improved thread safety (https://github.com/liblouis/liblouis-java/issues/8)
- Fix to PEF preview
- Support for new `border-align`, `border-top-align`, `border-right-align`, `border-bottom-align`,
  `border-left-align`, `border-style`, `border-top-style`, `border-right-style`,
  `border-bottom-style`, `border-left-style`, `border-width`, `border-top-width`,
  `border-right-width`, `border-bottom-width`, `border-left-width`, `border-top-pattern`,
  `border-right-pattern`, `border-bottom-pattern` and `border-left-pattern` properties and changed
  behavior of existing `border`, `border-top`, `border-right`, `border-bottom` and `border-left`
  properties (https://github.com/braillespecs/braille-css/issues/44)
- Improvements to print page number ranges
  - Changed behavior of `string()` keywords `page-start` and `page-start-except-last`: on the first
    page `page-start` now behaves like `page-first`
    (https://github.com/sbsdev/pipeline-mod-sbs/issues/42,
    https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49)
  - Dropped support for `page-last-except-start` and `spread-last-except-start`
  - Fixed behavior of `page-last`, `page-start-except-last`, `spread-last` and
    `spread-start-except-last`: "last" now includes "border" pagenums
    (https://github.com/sbsdev/pipeline-mod-sbs/issues/45)
  - Fixed behavior of `page-start`, `page-start-except-last`, `spread-start` and
    `spread-start-except-last`: "start" now does not include "border" pagenums, except on the first
    page (https://github.com/sbsdev/pipeline-mod-sbs/issues/45,
    https://github.com/brailleapps/dotify/issues/150,
    https://github.com/brailleapps/dotify.formatter.impl/pull/16) <!--
  • Fixed behavior w.r.t. "border" pagenums that precede an element with top padding (78cbc55) -->
- Improved manual volume breaking (https://github.com/sbsdev/pipeline-mod-sbs/issues/33,
  https://github.com/brailleapps/dotify/issues/212,
  https://github.com/brailleapps/dotify.formatter.impl/issues/2)
- Support for `-obfl-underline: ⠂` (https://github.com/daisy/pipeline-mod-braille/issues/96,
  https://github.com/brailleapps/dotify.formatter.impl/pull/14,
  https://github.com/sbsdev/pipeline-mod-sbs/issues/34)
- Support for grouping endnotes according to volume
  (https://github.com/brailleapps/dotify.formatter.impl/pull/18,
  https://github.com/braillespecs/obfl/issues/58) <!--
  http://braillespecs.github.io/braille-css/20161201/obfl#lists-of-references -->
  - `display:-obfl-list-of-properties` value
  - support for `::-obfl-on-volume-start` and `::-obfl-on-volume-end` pseudo-elements on
    `-obfl-list-of-properties` elements
- Support for `@text-transform` rules (https://github.com/sbsdev/pipeline-mod-sbs/issues/38)
- Fixes to line breaking and white space handling
  (https://github.com/sbsdev/pipeline-mod-sbs/issues/61,
  https://github.com/sbsdev/pipeline-mod-sbs/issues/32, ...) <!-- 2568b3d, c44e94b, 6d0b8f2,
  e4b6911, https://github.com/sbsdev/pipeline-mod-sbs/issues/31,
  https://github.com/snaekobbi/pipeline-mod-dedicon/issues/58 -->
- Removal of erroneous empty pages <!-- https://github.com/snaekobbi/pipeline-mod-dedicon/issues/57,
  https://github.com/sbsdev/pipeline-mod-sbs/issues/33 -->
- Truncation of long header and footer lines
  (https://github.com/brailleapps/dotify.formatter.impl/pull/10,
  https://github.com/sbsdev/pipeline-mod-sbs/issues/28)
- Support for `counter-set:page` inside `@begin` and `@end` areas
  (https://github.com/daisy/pipeline-mod-braille/issues/121)
- Support for `symbols()` function with `target-counter()` on main page area
  (https://github.com/daisy/pipeline-mod-braille/issues/115,
  https://github.com/brailleapps/dotify.formatter.impl/pull/9)
- Support for `text-transform` on `target-counter()` function
  (https://github.com/daisy/pipeline-mod-braille/issues/114,
  https://github.com/brailleapps/dotify.formatter.impl/pull/9)
- Fixed behavior of `hyphens:none` <!-- https://github.com/sbsdev/pipeline-mod-sbs/issues/13 -->
- Fixed support for `margin-top` on `::-obfl-on-toc-start`, `::-obfl-on-toc-end`,
  `::-obfl-on-volume-start` and `::-obfl-on-volume-end` pseudo-elements <!--
  https://github.com/sbsdev/pipeline-mod-sbs/issues/31 -->
- Fixed behavior of `counter()` on `::alternate::alternate` <!--
  https://github.com/sbsdev/pipeline-mod-sbs/issues/59 -->
- Fixed behavior of `-obfl-evaluate()` when expression evaluates to nothing
  (https://github.com/brailleapps/dotify.formatter.impl/pull/15)
- Fixed behavior of `hyphens` in presence of `text-transform`
- Support for `list-style` as an alias for `list-style-type`
  (https://github.com/daisy/pipeline-mod-braille/issues/98)
- Support for CSS value `initial`
- Various other small fixes <!--
  • Allow blocks within blocks with a `-obfl-underline` property
    (https://github.com/brailleapps/dotify.formatter.impl/pull/17)
  • Fixed handling of Sass variables with spaces or special characters
  • Fixed style inheritance on `::-obfl-on-toc-start`, `::-obfl-on-toc-end`,
    `::-obfl-on-volume-start` and `::-obfl-on-volume-end` pseudo-elements
  • `content` property now only allowed on `::before`, `::after`, `::alternate` and
  • `::footnote-call` pseudo-elements
    ...
• Fixed behavior of `string-set` and `string()` within named flows
  (https://github.com/daisy/pipeline-mod-braille/issues/110,
  https://github.com/sbsdev/pipeline-mod-sbs/issues/36): omit for now because broken in 8d1a23a (and
  fixed in 5674a23 but not on master yet) -->

Components
----------
- **liblouis** ([**3.0.0.alpha1**](https://github.com/liblouis/liblouis/releases/tag/v3.0.0.alpha1)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), **liblouis-java**
  ([**3.1.0**](https://github.com/liblouis/liblouis-java/releases/tag/3.1.0))
- **dotify** (**api** [**3.1.0**](https://github.com/brailleapps/dotify.api/releases/tag/releases%2Fv3.1.0), **common**
  [**3.0.0**](https://github.com/brailleapps/dotify.common/releases/tag/releases%2Fv3.0.0), **hyphenator.impl**
  [**3.0.0**](https://github.com/brailleapps/dotify.hyphenator.impl/releases/tag/releases%2Fv3.0.0), **translator.impl**
  [**3.0.0**](https://github.com/brailleapps/dotify.translator.impl/releases/tag/releases%2Fv3.0.0), **formatter.impl**
  [**3.1.0**](https://github.com/brailleapps/dotify.formatter.impl/releases/tag/releases%2Fv3.1.0), **text.impl**
  [**3.0.0**](https://github.com/brailleapps/dotify.text.impl/releases/tag/releases%2Fv3.0.0), **task-api**
  [**3.0.0**](https://github.com/brailleapps/dotify.task-api/releases/tag/releases%2Fv3.0.0), **task-runner**
  [**2.0.0**](https://github.com/brailleapps/dotify.task-runner/releases/tag/releases%2Fv2.0.0), **task.impl**
  [**3.0.0**](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv3.0.0))
- brailleutils (api
  [3.0.0](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.0), impl
  [3.0.0-beta](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0-beta), pef-tools
  [2.0.0-alpha](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.0.0-alpha))
- **braille-css** ([**1.13.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.13.0))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.0](https://github.com/daisy/jhyphen/releases/tag/v1.0.0))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.10.0
=======

Changes
-------
- Improvements to in-script documentation (option descriptions etc.)
  (https://github.com/daisy/pipeline-mod-braille/pull/137)

v1.9.16
=======

Changes
-------
- Dotify update
- Bugfixes

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([2.0.0](https://github.com/liblouis/liblouis-java/releases/tag/2.0.0))
- **dotify** (**api** [**2.10.0**](https://github.com/brailleapps/dotify.api/releases/tag/releases%2Fv2.10.0), **common**
  [**2.1.0**](https://github.com/brailleapps/dotify.common/releases/tag/releases%2Fv2.1.0), hyphenator.impl
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv2.0.1), translator.impl
  [2.3.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv2.3.0), **formatter.impl**
  [**2.6.0**](https://github.com/brailleapps/dotify.formatter.impl/releases/tag/releases%2Fv2.6.0), text.impl
  [2.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv2.0.0), **task-api**
  [**2.3.0**](https://github.com/brailleapps/dotify.task-api/releases/tag/releases%2Fv2.3.0), **task-runner**
  [**1.1.0**](https://github.com/brailleapps/dotify.task-runner/releases/tag/releases%2Fv1.1.0), **task.impl**
  [**2.8.0**](https://github.com/brailleapps/dotify.task.impl/releases/tag/releases%2Fv2.8.0))
- brailleutils (api
  [3.0.0](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.0), impl
  [3.0.0-beta](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0-beta), pef-tools
  [2.0.0-alpha](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.0.0-alpha))
- braille-css ([1.12.0](https://github.com/snaekobbi/braille-css/releases/tag/1.12.0))
- jstyleparser ([1.20-p9](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p9))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.8.8](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.0](https://github.com/daisy/jhyphen/releases/tag/v1.0.0))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.15
=======

Changes
-------
- Bugfixes

v1.9.14
=======

Changes
-------
- More control over BRF output with `ascii-file-format` option
  (https://github.com/daisy/pipeline-mod-braille/issues/103)
- Support for block underlining with `-obfl-underline` property
  (https://github.com/braillespecs/obfl/issues/50, https://github.com/joeha480/dotify/pull/208,
  https://github.com/brailleapps/dotify.api/pull/2)
- Support for `@volume:last`
- Support for table of contents in end area of volume
  (https://github.com/braillespecs/obfl/issues/55)
- Support `page` property on elements flowed into `@begin` and `@end` areas of volumes
  (https://github.com/daisy/pipeline-mod-braille/issues/104)
- Fixed behavior of padding (https://github.com/daisy/pipeline-mod-braille/issues/109,
  https://github.com/snaekobbi/pipeline-mod-braille/issues/30)
- Support for OBFL variables `$sheets-in-volume` and `$sheets-in-document`
  (https://github.com/joeha480/dotify/issues/198, https://github.com/joeha480/dotify/pull/199)
- Support for collecting information about flows with `-obfl-use-when-collection-not-empty` property
  (https://github.com/joeha480/dotify/issues/200)
- Support for `text-transform` on `-obfl-evaluate()` function
  (https://github.com/daisy/pipeline-mod-braille/issues/114,
  https://github.com/brailleapps/dotify.formatter.impl/pull/9)
- Support for hyphenation with Hyphen on Windows
  (https://github.com/daisy/pipeline-mod-braille/issues/107)
- Internal changes (https://github.com/joeha480/dotify/issues/118,
  https://github.com/joeha480/dotify/issues/188, https://github.com/joeha480/dotify/pull/207, ...)
- Bugfixes (https://github.com/daisy/pipeline-mod-braille/issues/50, ...)

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([2.0.0](https://github.com/liblouis/liblouis-java/releases/tag/2.0.0))
- **dotify** (**api** [**2.8.0**](https://github.com/brailleapps/dotify.api/releases/tag/releases%2Fv2.8.0), common
  [2.0.2](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv2.0.2), hyphenator.impl
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv2.0.1), translator.impl
  [2.3.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv2.3.0), **formatter.impl**
  [**2.4.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv2.4.0), text.impl
  [2.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv2.0.0), task-api
  [2.1.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-api%2Fv2.1.0), task-runner
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-runner%2Fv1.0.0), task.impl
  [2.4.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task.impl%2Fv2.4.0))
- **brailleutils** (**api**
  [**3.0.0**](https://github.com/brailleapps/braille-utils.api/releases/tag/releases%2Fv3.0.0), **impl**
  [**3.0.0-beta**](https://github.com/brailleapps/braille-utils.impl/releases/tag/releases%2Fv3.0.0-beta), **pef-tools**
  [**2.0.0-alpha**](https://github.com/brailleapps/braille-utils.pef-tools/releases/tag/releases%2Fv2.0.0-alpha))
- **braille-css** ([**1.11.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.11.0))
- **jstyleparser** ([**1.20-p9**](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p9))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- **libhyphen** ([**2.8.8**](https://github.com/daisy/libhyphen-nar/releases/tag/2.8.8)), jhyphen
  ([1.0.0](https://github.com/daisy/jhyphen/releases/tag/v1.0.0))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.13
=======

Changes
-------
- Support for non-standard hyphenation with Hyphen
  (https://github.com/snaekobbi/pipeline-mod-braille/issues/55)
- Internal changes
- Bugfixes

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([2.0.0](https://github.com/liblouis/liblouis-java/releases/tag/2.0.0))
- **dotify** (**api** [**2.7.0**](https://github.com/brailleapps/dotify.api/releases/tag/releases%2Fv2.7.0), common
  [2.0.2](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv2.0.2), hyphenator.impl
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv2.0.1), translator.impl
  [2.3.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv2.3.0), **formatter.impl**
  [**2.3.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv2.3.0), text.impl
  [2.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv2.0.0), task-api
  [2.1.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-api%2Fv2.1.0), task-runner
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-runner%2Fv1.0.0), **task.impl**
  [**2.4.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task.impl%2Fv2.4.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.10.1**](https://github.com/snaekobbi/braille-css/releases/tag/1.10.1))
- **jstyleparser** ([**1.20-p8**](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p8))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([1.0.0](https://github.com/daisy/jhyphen/releases/tag/v1.0.0))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.12
=======

Changes
-------
- Improved support for `symbols()` function
  (https://github.com/snaekobbi/pipeline-mod-braille/issues/15)
- Support for non-standard hyphenation (https://github.com/snaekobbi/pipeline-mod-braille/issues/55)
- Internal changes (https://github.com/daisy/pipeline-mod-braille/issues/100, ...)
- Bugfixes

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), **liblouis-java**
  ([**2.0.0**](https://github.com/liblouis/liblouis-java/releases/tag/2.0.0))
- dotify (api [2.5.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv2.5.0), common
  [2.0.2](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv2.0.2), hyphenator.impl
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv2.0.1), translator.impl
  [2.3.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv2.3.0), formatter.impl
  [2.2.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv2.2.1), text.impl
  [2.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv2.0.0), task-api
  [2.1.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-api%2Fv2.1.0), task-runner
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-runner%2Fv1.0.0), task.impl
  [2.3.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task.impl%2Fv2.3.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.9.1**](https://github.com/snaekobbi/braille-css/releases/tag/1.9.1))
- **jstyleparser** ([**1.20-p7**](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p7))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.11
=======

Changes
-------
- New option `include-obfl` (https://github.com/daisy/pipeline-mod-braille/issues/90)
- New option `maximum-number-of-sheets`
- Support for XSLT in `stylesheet` option
  (https://github.com/snaekobbi/pipeline-mod-braille/issues/63)
- Improved support for numbering with `symbols()` function
  (https://github.com/snaekobbi/pipeline-mod-braille/issues/15)
- Support for colspan and rowspan on data cells of tables that are layed out as lists
- Support for `page-start-except-last` and `spread-start-except-last` keywords in `string()`
  function
- Parameter `skip-margin-top-of-page` (https://github.com/daisy/pipeline-mod-braille/issues/97)
- Bugfixes (https://github.com/braillespecs/obfl/issues/31,
  https://github.com/joeha480/dotify/issues/134, https://github.com/joeha480/dotify/pull/189,
  https://github.com/joeha480/dotify/issues/194, ...)

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.4.0](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))
- **dotify** (**api** [**2.5.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv2.5.0), common
  [2.0.2](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv2.0.2), hyphenator.impl
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv2.0.1), **translator.impl**
  [**2.3.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv2.3.0), **formatter.impl**
  [**2.2.1**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv2.2.1), **text.impl**
  [**2.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv2.0.0), task-api
  [2.1.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-api%2Fv2.1.0), task-runner
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-runner%2Fv1.0.0), **task.impl**
  [**2.3.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task.impl%2Fv2.3.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.9.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.9.0))
- **jstyleparser** ([**1.20-p6**](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p6))
- jsass ([4.1.0-p1](https://github.com/daisy/jsass/releases/tag/))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.10
=======

Changes
-------
- New epub3-to-pef script (https://github.com/snaekobbi/pipeline-mod-braille/issues/43,
  https://github.com/daisy/pipeline-mod-braille/pull/79)
- Support for Sass style sheets (https://github.com/daisy/pipeline-mod-braille/pull/78)
- New options `duplex`, `page-width`, `page-height`, `levels-in-footer`, `hyphenation`,
  `line-spacing`, `capital-letters`, `include-captions`, `include-images`, `include-line-groups`,
  `include-production-notes`, `show-braille-page-numbers`, `show-print-page-numbers` and
  `force-braille-page-break` (https://github.com/snaekobbi/pipeline-mod-braille/issues/27)
- Improved support for laying out tables as lists
  - `::list-header` pseudo-element
  - Support pseudo-elements such as `::before` and pseudo-classes such as `:first-child` on
    `::table-by()` and `::list-item` pseudo-elements
  - Improved algorithm for finding headers
- Support for footnotes and endnotes (https://github.com/snaekobbi/pipeline-mod-braille/pull/4,
  https://github.com/snaekobbi/pipeline-mod-braille/issues/9,
  https://github.com/snaekobbi/pipeline-mod-braille/issues/12)
  - `@footnotes` page area
  - `max-height` and `-obfl-fallback-flow` properties
  - `::footnote-call` and `::alternate` pseudo-elements
    (https://github.com/snaekobbi/braille-css/issues/12)
  - `target-content()` function
  - `volume` argument for `flow()` function
- Support for matrix tables (https://github.com/snaekobbi/pipeline-mod-braille/issues/14)
  - `display:table` property
  - `-obfl-table-col-spacing`, `-obfl-table-row-spacing`, `-obfl-preferred-empty-space` and
    `render-table-by:column` properties
- Support for `page` property inside `@begin` and `@end` rules
- Support for `xml-stylesheet` processing instruction
  (https://github.com/snaekobbi/pipeline-mod-braille/issues/53)
- Support for `:not()` and `:has()` pseudo-classes
  (https://github.com/snaekobbi/braille-css/issues/8,
  https://github.com/snaekobbi/braille-css/issues/14)
- Support for `xml:space="preserve"` in default CSS
  (https://github.com/daisy/pipeline-mod-braille/issues/53)
- Support for rowgap in PEF preview (https://github.com/daisy/pipeline-mod-braille/issues/52)
- Bugfixes (https://github.com/daisy/pipeline-mod-braille/issues/73, ...)

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.4.0](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))
- **dotify** (**api** [**2.4.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv2.4.0), **common**
  [**2.0.2**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv2.0.2), hyphenator.impl
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv2.0.1), **translator.impl**
  [**2.1.1**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv2.1.1), **formatter.impl**
  [**2.1.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv2.1.0), text.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv1.0.0), **task-api**
  [**2.1.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-api%2Fv2.1.0), **task-runner**
  [**1.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-runner%2Fv1.0.0), **task.impl**
  [**2.1.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task.impl%2Fv2.1.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.8.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.8.0))
- jstyleparser ([1.20-p5](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p5))
- **jsass** ([**4.1.0-p1**](https://github.com/daisy/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.9
======

Changes
-------
- New `toc-depth` option for generating table of contents
- Support for rendering table of contents at the beginning of volumes
  - `display:-obfl-toc` value
  - `-obfl-toc-range` property
  - `::-obfl-on-toc-start`, `::-obfl-on-volume-start`, `::-obfl-on-volume-end` and
    `::-obfl-on-toc-end` pseudo-elements
- Advanced support for generated content
  - stacked pseudo-elements like `::before::before`
  - `::duplicate` pseudo-element
  - `-obfl-evaluate()` function
- Support for laying out tables as lists
  - `render-table-by` and `table-header-policy` properties
  - `::table-by()` and `::list-item` pseudo-elements
- Bugfixes

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.4.0](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))
- dotify (api [2.1.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv2.1.0), common
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv2.0.1), hyphenator.impl
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv2.0.1), translator.impl
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv2.0.1), formatter.impl
  [2.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv2.0.0), text.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv1.0.0), task-api
  [2.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-api%2Fv2.0.0), task.impl
  [2.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task.impl%2Fv2.0.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.7.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.7.0))
- **jstyleparser** ([**1.20-p5**](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p5))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.8
======

Changes
-------
- New `ascii-table` option (https://github.com/snaekobbi/pipeline-mod-braille/issues/56,
  https://github.com/daisy/pipeline-mod-braille/pull/56)
- Support for marks in left or right margin (https://github.com/joeha480/dotify/issues/145)
  - `@left` and `@right` page margins
  - `-obfl-marker` and `-obfl-marker-indicator()`
- Support for `letter-spacing` and `word-spacing`
  (https://github.com/snaekobbi/pipeline-mod-braille/issues/24)
- Initial support for volumes (https://github.com/snaekobbi/pipeline-mod-braille/issues/13,
  https://github.com/daisy/pipeline-mod-braille/pull/61)
  - `@volume`, `@volume:first`, `@volume:last` and `@volume:nth()` rules
  - `min-length` and `max-length` properties
  - `@begin` and `@end` volume areas
  - `flow` and `flow()`
- Fixes in `string()` and `string-set` (https://github.com/daisy/pipeline-mod-braille/issues/64,
  https://github.com/daisy/pipeline-mod-braille/issues/65)
- Fixes in logging (https://github.com/daisy/pipeline-assembly/issues/87)
- Major revision of translator API

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.4.0](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))
- **dotify** (**api** [**2.1.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv2.1.0), common
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv2.0.1), hyphenator.impl
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv2.0.1), translator.impl
  [2.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv2.0.1), **formatter.impl**
  [**2.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv2.0.0), text.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv1.0.0), task-api
  [2.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-api%2Fv2.0.0), task.impl
  [2.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task.impl%2Fv2.0.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.6.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.6.0))
- **jstyleparser** ([**1.20-p4**](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p4))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.7
======

Changes
-------
- Support for hyphenation in Dotify translator
  (https://github.com/daisy/pipeline-mod-braille/issues/44)
- Fixes in `page-break-before` and `page-break-after`
- Support for `orphans` and `widows`
- Support for print page number ranges (https://github.com/snaekobbi/pipeline-mod-braille/issues/31,
  https://github.com/joeha480/obfl/issues/24)
- Support `text-transform` in headers and footers

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.4.0](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))
- **dotify** (**api** [**2.0.1**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv2.0.1), **common**
  [**2.0.1**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv2.0.1), **hyphenator.impl**
  [**2.0.1**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv2.0.1), **translator.impl**
  [**2.0.1**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv2.0.1), **formatter.impl**
  [**2.0.0-alpha**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv2.0.0-alpha), text.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv1.0.0), **task-api**
  [**2.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-api%2Fv2.0.0), **task.impl**
  [**2.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task.impl%2Fv2.0.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- braille-css ([1.4.0](https://github.com/snaekobbi/braille-css/releases/tag/1.4.0))
- jstyleparser ([1.20-p3](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p3))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.6
======

Changes
-------
- Support for multi-line headers and footers
- Support for `page-break-before:right`, `page-break-after:right`, `page-break-before:avoid` and
  `page-break-after:always`
- Support for `string-set` and `counter-set`
- Support for translation while formatting
- Support for qualified names in CSS attribute selectors
- Use of `(formatter:dotify)` by default
- Bug fixes in `text-indent` and `text-align`
  (https://github.com/daisy/pipeline-mod-braille/issues/54,
  https://github.com/daisy/pipeline-mod-braille/issues/55)
- Fixes in white space handling

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.4.0](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))
- **dotify** (**api** [**1.4.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv1.4.0), common
  [1.2.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv1.2.0), hyphenator.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv1.0.0), **translator.impl**
  [**1.2.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv1.2.0), **formatter.impl**
  [**1.2.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv1.2.0), text.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv1.0.0), task-api
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-api%2Fv1.0.0), **task.impl**
  [**1.0.1**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task.impl%2Fv1.0.1))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.4.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.4.0))
- **jstyleparser** ([**1.20-p3**](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p3))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.5
======

Changes
-------
- Integration of Dotify's TaskSystem (https://github.com/daisy/pipeline-mod-braille/pull/39)
- Support for row spacing (https://github.com/snaekobbi/pipeline-mod-braille/issues/26,
  https://github.com/snaekobbi/braille-css/issues/5)
- Correct handling of empty blocks (https://github.com/daisy/pipeline-mod-braille/issues/49)

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.4.0](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))  
- **dotify** (api [1.2.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv1.2.0), **common**
  [**1.2.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv1.2.0), hyphenator.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv1.0.0), translator.impl
  [1.1.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv1.1.0), **formatter.impl**
  [**1.1.3**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv1.1.3), **text.impl**
  [**1.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.text.impl%2Fv1.0.0), **task-api**
  [**1.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task-api%2Fv1.0.0), **task.impl**
  [**1.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.task.impl%2Fv1.0.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.3.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.3.0))
- jstyleparser ([1.20-p2](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p2))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.4
======

Changes
-------
- New `stylesheet` option (https://github.com/daisy/pipeline-mod-braille/issues/46) replaces
  `default-stylesheet` option (https://github.com/daisy/pipeline-mod-braille/issues/34)
- Improvements to default style sheets (https://github.com/daisy/pipeline-mod-braille/issues/40)
- Support for more border patterns (https://github.com/daisy/pipeline-mod-braille/issues/45)
- Bug fixes in margins (https://github.com/daisy/pipeline-mod-braille/pull/42) and line breaking
  (https://github.com/daisy/pipeline-mod-braille/pull/43)

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.4.0](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))
- **dotify** (**api** [**1.2.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv1.2.0), common
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv1.0.0), hyphenator.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv1.0.0), **translator.impl**
  [**1.1.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv1.1.0), formatter.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv1.0.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- braille-css ([1.2.0](https://github.com/snaekobbi/braille-css/releases/tag/1.2.0))
- jstyleparser ([1.20-p2](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p2))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.3
======

Changes
-------
- Bug fixes (https://github.com/daisy/pipeline-mod-braille/issues/35,
  https://github.com/daisy/pipeline-mod-braille/issues/33)

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.4.0](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))
- dotify (api [1.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv1.0.1), common
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv1.0.0), hyphenator.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv1.0.0), translator.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv1.0.0), formatter.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv1.0.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.2.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.2.0))
- jstyleparser ([1.20-p2](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p2))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.2
======

Changes
-------
- Correct handling of white space (https://github.com/snaekobbi/pipeline-mod-braille/issues/52)
- Support for vertical positioning (https://github.com/snaekobbi/pipeline-mod-braille/issues/28,
  https://github.com/snaekobbi/braille-css/issues/2)
- Support for namespaces in CSS (https://github.com/snaekobbi/pipeline-mod-braille/issues/11)
- Fixed bug in system startup (https://github.com/snaekobbi/system/issues/2)

Components
----------
- liblouis ([2.6.3](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.4.0](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))
- dotify (api [1.0.1](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv1.0.1), common
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv1.0.0), hyphenator.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv1.0.0), translator.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv1.0.0), formatter.impl
  [1.0.0](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv1.0.0))
- brailleutils (api
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), impl
  [2.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), pef-tools
  [1.0.0](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.1.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.1.0))
- **jstyleparser** ([**1.20-p2**](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p2))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.1
======

Changes
-------
- HTML to PEF conversion script
- Direct DTBook to PEF conversion script (not through ZedAI)
  (https://github.com/snaekobbi/pipeline-mod-braille/issues/45)
- `transform` option for transformer queries
- Dotify based formatter (https://github.com/daisy/pipeline-mod-braille/pull/11,
  https://github.com/snaekobbi/pipeline-mod-braille/pull/2, https://github.com/snaekobbi/pipeline-mod-braille/issues/32,
  https://github.com/snaekobbi/pipeline-mod-braille/pull/33)
- Support for `text-transform` property (https://github.com/daisy/pipeline-mod-braille/pull/23)
- Better logging (https://github.com/daisy/pipeline-mod-braille/issues/19)
- Framework redesign (https://github.com/daisy/pipeline-mod-braille/pull/15,
  https://github.com/snaekobbi/pipeline-mod-braille/pull/1)
- Other internal changes (https://github.com/daisy/pipeline-mod-braille/issues/10,
  https://github.com/daisy/pipeline-mod-braille/pull/25, https://github.com/daisy/pipeline-mod-braille/pull/29,
  https://github.com/snaekobbi/pipeline-mod-braille/pull/3, https://github.com/snaekobbi/pipeline-mod-braille/pull/35,
  https://github.com/snaekobbi/pipeline-mod-braille/issues/44)

Components
----------
- **liblouis** ([**2.6.3**](https://github.com/liblouis/liblouis/releases/tag/v2.6.3)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), **liblouis-java**
  ([**1.4.0**](https://github.com/liblouis/liblouis-java/releases/tag/1.4.0))
- **dotify** (**api** [**1.0.1**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.api%2Fv1.0.1), **common**
  [**1.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.common%2Fv1.0.0), **hyphenator.impl**
  [**1.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.hyphenator.impl%2Fv1.0.0), **translator.impl**
  [**1.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.translator.impl%2Fv1.0.0), **formatter.impl**
  [**1.0.0**](https://github.com/joeha480/dotify/releases/tag/releases%2Fdotify.formatter.impl%2Fv1.0.0))
- **brailleutils** (**api**
  [**2.0.0**](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.api%2Fv2.0.0), **impl**
  [**2.0.0**](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv2.0.0), **pef-tools**
  [**1.0.0**](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv1.0.0))
- **braille-css** ([**1.0.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.0.0))
- **jstyleparser** ([**1.20-p1**](https://github.com/snaekobbi/jStyleParser/releases/tag/jstyleparser-1.20-p1))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9
====

Components
----------
- liblouis ([2.5.4](https://github.com/liblouis/liblouis/releases/tag/liblouis_2_5_4)), liblouisutdml
  ([2.5.0](https://github.com/liblouis/liblouisutdml/releases/tag/v2.5.0)), liblouis-java
  ([1.2.0](https://github.com/liblouis/liblouis-java/releases/tag/1.2.0))
- brailleutils (core [1.2.0](https://github.com/daisy/osgi-libs/releases/tag/brailleutils-core-1.2.0), catalog
  [1.2.0](https://github.com/daisy/osgi-libs/releases/tag/brailleutils-catalog-1.2.0))
- jstyleparser ([1.13](https://github.com/daisy/osgi-libs/releases/tag/jstyleparser-1.13.0-p1))
- libhyphen ([2.6.0](https://github.com/daisy/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))
