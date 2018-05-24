# XSLT Pipelines and pipeline languages

XSLT:

* A 4GL language specifically designed for and well-suited to this task
* Its "document transformation" architecture fits well with most publishing workflows (in some form or other)
* Standards basis and available open source tools render it effectively platform (vendor) independent

Pipelines:

The way XSLT (and its forerunner technologies) works is by specifying a *transformation*, but it does not answer the question of what that consists of, exactly. We change this into that, but it turns out that the "this" is detailed and complex (it is not just a tree, it is a trunk, bark and leaves) -- and so is the "that".

A tried and true method of handling complexity in transformations is to break it down into simpler parts, which can be developed and tested separately before they are coordinated together. XSLT, as a declarative and functional language, mandates no specific mechanism by which its own operations may be achieved -- its own deployment architectures are to various for that, and anyone who has the capability of running one transformation, has the capability of running a second one after the first. If the input of the second transformation is the result of the first, this is a pipeline. So pipelining is what you do with XSLT, not the way you do it.

In the case of XSweet, its pipelines may be considerably longer, as many as eight, ten, 12 or more XSLT transformations in a chain, each consuming the output of the last. Moreover, there are particular operations that XSweet includes, that require not a single transformation but a small coordination or choreography of several "document" inputs and outputs. For example, one operation (header promotion) requires that a document be run through a transformation producing an analysis, which is subsequently used to produce *an XSLT stylesheet*, which is then applied back again to the original document. (This way, features of the particular document can be encoded directly into the transformation to be applied to it.) Pipelines, in other words, can have branches, both on the input side (such as multiple document inputs not just because you have a stack of chapters, but also because you have a metadata or configuration that goes with all of them) and on the output side (multiple outputs for multiple purposes of analysis, representation and formatting).

How is such a pipeline achieved? *Any way you like*. However, as with everything, there are tradeoffs. In particular, in the case of an XSLT application running over XML documents (stored in a file system or shared over the wire), there are considerations related to the overhead of moving and manipulating data in such a system. Sometimes, the same operation that will take minutes to accomplish in one environment, will take only seconds in another (i.e. order of magnitude difference can be observed) only because of how the transformations are executed and chained. However, a slow pipeline technology might have other advantages. It might be quick and easy to set up using a language a programmer already knows.

XSweet was designed and tested using the industry-leading XSLT engine, SaxonHE, open source software written in Java, supporting XSLT and XPath 3.0. However, we have pipelined calls to execute particular transformations in numerous ways -- the only thing they having in common is XSLT and Saxon themselves.






