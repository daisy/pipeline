# Overview
There are three main entry points to the converters. XML, Epub 3 and text.

![https://docs.google.com/drawings/d/1u4GGmxszKkIZySu1Fw4lIWGn_jMbAYt6cdQk0u4mgME/pub?w=814&h=592&dummy=.jpg](https://docs.google.com/drawings/d/1u4GGmxszKkIZySu1Fw4lIWGn_jMbAYt6cdQk0u4mgME/pub?w=814&h=592&dummy=.jpg)

## XML
The XML-component can handle any XML-based format if an XSLT-transformation and a validation (optional) is provided for it. Two things determine which path to run:
  * The root element and namespace
  * The selected template (not shown in the image above). The template option is provided to support several implementations for the same format that are profoundly different and cannot be expressed using parameters (e.g. page width, page height, etc).

Note that:
- The OBFL path doesn't change the file in any way.
- The XML path is provided as a way to get the text from an XML-based format into braille, but it offers very little value in terms of layout. For any real use cases, a proper conversion path is highly recommended.

### Functionality and features
For DTbook, Epub 3 and XHTML:
  * Many configurable parameters, for example
    * page dimensions
    * inner and outer margins
    * row spacing
    * maximum volume size
    * duplex/simplex
  * Localization of injected texts, e.g. “caption”, “table of contents”, “image description”. The localizations can include variable data, such as volume number, page number etc, e.g. “volume 1”, “volume three”