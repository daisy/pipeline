# Development notes

MathML 3 element set can follow the same rules as MathML 2. Here is an analysis: [ComparisonOfMathMLContentElements](http://code.google.com/p/daisy-pipeline/wiki/ComparisonOfMathMLContentElements).

## Challenges

- Detecting the MathML version. At the start, this will be set by the user as a parameter.
- Can we support integration of MathML in DTBook 2005-1? The modularization mechanisms don't seem to be as well developed (e.g. no `externalFlow`).
