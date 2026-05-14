<link rel="dp2:permalink" href="http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-to-latex/"/>
<link rev="dp2:doc" href="../src/main/resources/xml/dtbook-to-latex.script.xpl"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>

# DTBook to LaTeX

Transforms a DTBook (DAISY 3 XML) document into a LaTeX document suitable for large print production.

## Synopsis

{{>synopsis}}

## Requirements

The script produces a `.tex` file that must be compiled with **XeLaTeX** or **LuaLaTeX**
(required for font selection via fontspec). The following LaTeX packages must be installed:

- `memoir` class
- `fontspec`, `babel`, `hyperref`, `setspace`
- `tcolorbox` (a recent version is required for sidebars to render correctly)
- `adjustbox`, `enumitem`, `url`, `alphalph`
- `ucharclasses` (optional, only needed for the `backup-unicode-ranges` option)

> **Note:** Producing accessible tagged PDF (PDF/UA-2) requires TeX Live 2025 or later,
> LuaLaTeX, and changes to the generated LaTeX preamble. This is not currently supported
> by this script but may be addressed in a future version. See the
> [LaTeX Tagging Project](https://latex3.github.io/tagging-project/) for background.
