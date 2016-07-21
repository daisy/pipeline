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
  (https://github.com/daisy/pipeline-mod-braille/issues/114)
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
- **dotify** (**api** **[2.8.0**](https://github.com/brailleapps/dotify.api/releases/tag/releases%2Fv2.8.0), common
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
  [**3.0.0-beta**](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.impl%2Fv3.0.0-beta), **pef-tools**
  [**2.0.0-alpha**](https://github.com/brailleapps/brailleutils/releases/tag/releases%2Fbraille-utils.pef-tools%2Fv2.0.0-alpha))
- **braille-css** ([**1.11.0**](https://github.com/snaekobbi/braille-css/releases/tag/1.11.0))
- **jstyleparser** ([**1.20-p9**](https://github.com/snaekobbi/jStyleParser/releases/tag/jStyleParser-1.20-p9))
- jsass ([4.1.0-p1](https://github.com/snaekobbi/jsass/releases/tag/4.1.0-p1))
- **libhyphen** ([**2.8.8**](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.8.8)), jhyphen
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
- jsass ([4.1.0-p1](https://github.com/snaekobbi/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- jsass ([4.1.0-p1](https://github.com/snaekobbi/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- jsass ([4.1.0-p1](https://github.com/snaekobbi/jsass/releases/tag/))
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))

v1.9.10
=======

Changes
-------
- New epub3-to-pef script (https://github.com/snaekobbi/pipeline-mod-braille/issues/43,
  https://github.com/daisy/pipeline-mod-braille/pull/79)
- Support for SASS style sheets (https://github.com/daisy/pipeline-mod-braille/pull/78)
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
- **jsass** ([**4.1.0-p1**](https://github.com/snaekobbi/jsass/releases/tag/4.1.0-p1))
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- Support for text-transform property (https://github.com/daisy/pipeline-mod-braille/pull/23)
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
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
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
- libhyphen ([2.6.0](https://github.com/snaekobbi/libhyphen-nar/releases/tag/2.6.0)), jhyphen
  ([0.1.5](https://github.com/daisy/jhyphen/releases/tag/v0.1.5))
- texhyphj ([1.2](https://github.com/joeha480/texhyphj/releases/tag/release-1.2))
