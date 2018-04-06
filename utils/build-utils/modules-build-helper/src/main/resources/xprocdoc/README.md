xprocdoc
========

A simple Javadoc-style XProc API documentation generator written in XProc and XSLT 2.0.

The tool is implemented as a combination of an XProc pipeline (`xprocdoc.xpl`) and an XSLT 2.0 stylesheet (`xd2html.xsl`). The pipeline collects the documentation information for XProc sources and constructs an intermediate XML document, which is then processed by the stylesheet to produce XHTML output that can be viewed in a web browser.

Documenting XProc pipelines
---------------------------

Documentation for the XProc pipelines must be specified inline, in the actual source files, similar to Javadoc comments in Java source files. The documentation takes the form of XHTML fragments wrapped in the standard XProc p:documentation element, and can be provided for the following XProc elements:

 

- `p:library` - A library containing zero or more XProc steps:

        <p:library version="1.0">
          <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Library for simple DITA processing.</p>
          </p:documentation>
          
          <p:declare-step type="...">
            ...
          </p:declare-step>
          ...
        </p:library>

- `p:declare-step` and `p:pipeline` - An XProc step:

        <p:declare-step>
          <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>This pipeline transforms a DITA topic to XHTML.</p>
          </p:documentation>
          ...
        </p:declare-step>

- `p:input` - An input port of a step:

        <p:declare-step>
          <p:input port="source">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
              <p>The DITA topic to transform.</p>
            </p:documentation>
          </p:input>
          ...
        </p:declare-step>

- `p:output` - An output port of a step:

        <p:declare-step>
          ...
          <p:output port="result">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
              <p>The generated XHTML document.</p>
            </p:documentation>
          </p:output>
          ...
        </p:declare-step>

- `p:option` - An option of a step:

        <p:declare-step>
          ...
          <p:option name="validate" select="'false'">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
              <p>This option indicates whether to validate the
                 topic before it is processed. The value of the
                 option must be a boolean.</p>
            </p:documentation>
          </p:option>
          ...
        </p:declare-step>

Generating XHTML documentation
------------------------------

To generate the XHTML documentation for a set of XProc files, simply pass the files to `source` input port of the `xd:xprocdoc` pipeline. The pipeline will process the XProc sources and will generate a set of XHTML files in the current working directory (or in a location specified using the `output-base-uri` option). The file `index.html` can be used to access the documentation overview page.

The `xd:xprocdoc` pipeline generates documentation for all XProc steps that are:

- direct children of `p:library` and specify the `type` attribute;

  or

- top-level steps (i.e. not contained in `p:library`) with or without the `type` attribute.

(In other words, documentation will be generated only for those steps that can be used by developers, either directly or by importing an XProc library.)

Steps can also be explicitly excluded from the generated documentation by setting the `exclude` attribute (in the `http://github.com/vojtechtoman/xprocdoc` namespace) to `true` on the step's `p:declare-step` or `p:pipeline` element:

    <p:declare-step xd:exclude="true"
                    xmlns:xd="http://www.emc.com/documentum/xml/xproc/doc">
      ...
    </p:declare-step>

The documentation generation pipeline also follows `p:import` statements in XProc libraries. That way, the documentation can be generated for the complete dependency set. (The pipeline supports re-entrant and circular imports.)

Customizing the XHTML output
----------------------------

The properties of the XHTML output can be customized by the following options (all of which are optional):

- `product` - The name of the product that will appear on the generated overview XHTML page.

- `input-base-uri` - The base URI of the source XProc pipelines. This option can be used to customize the way the source URIs are presented in the generated XHTML. For example, if the source pipelines `src1.xpl` and `src2.xpl` are stored in the directory with the base URI `file:/home/fred/pipelines/`, setting the `input-base-uri` option to `file:/home/fred/` will cause the pipelines to appear as `pipelines/src1.xpl` and `pipelines/src2.xpl`. If the `input-base-uri` option is left unspecified, the pipelines will appear as `file:/home/fred/pipelines/src1.xpl` and `file:/home/fred/pipelines/src2.xpl`.

- `output-base-uri` - The base URI of the directory where the generated XHTML output is stored. If not specified, the current working directory will be used.

- `overview-file` - The URI of a file with an XHTML boilerplate text that will be inserted in the generated overview page. If not specified, no boilerplate text will be inserted.

The `xd:xprocdoc` pipeline and the `xd2html.xsl` XSLT stylesheet that it uses are independent. The XProc pipeline is responsible for extracting the documentation from XProc source files and for generating an intermediate XML representation that can be then transformed into XHTML using the stylesheet. It should be relatively straightforward to modify the stylesheet to customize the XHTML output, or to use a completely different stylesheet altogether. Processing the intermediate XML data by other means should also be possible.
