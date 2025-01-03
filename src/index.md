---
layout: home
title: Home
---
# DAISY Pipeline 2

The DAISY Pipeline 2 is an open-source, cross-platform framework and
user interface for the automated processing of digital content in and
between various file formats. It intends to facilitate the production
and maintenance of accessible content for people with print
disabilities.

## Accessibility

The Pipeline was developed by and for the [DAISY
community](http://www.daisy.org/), a group of organizations committed
to making content accessible. It goes without saying that accessiblity
is the main interest of the tool. There are Pipeline transformation
for migrating from one accessible format to another, enriching an
input format with certain accessible features, and producing formats
targeting specific disability reading systems.

## Standards

Accessibility goes hand in hand with standards. Notable file formats
that DAISY Pipeline evolves around are
[EPUB](https://www.w3.org/TR/epub-33/),
[DAISY](https://daisy.org/activities/standards/),
[PEF](https://braillespecs.github.io/pef/pef-specification.html) and
[eBraille](https://daisy.github.io/ebraille/).

Standards are also important under the hood. The system is based on
standard XML processing technologies, notably W3C recommendations like
XProc and XSLT 3.0, but also XPath 3.1, OASIS XML Catalogs, etc. These
technologies are platform neutral, supported by active communities,
and easy to maintain.

## Cross-platform

The application can be run in some form on all common operating
systems. A desktop application is provided for Windows and macOS. In
addition, for Windows users, there is the [Save As DAISY addin for MS
Word](https://daisy.org/activities/software/save-as-daisy-ms-word-add-in/),
which is becoming a DAISY Pipeline user interface specifically for
processing Word documents.

DAISY Pipeline also has programming interfaces. When run as a daemon
(web server), there are no restrictions. The server can be run on any
OS, and its platform neutral RESTful API allows it to be called from
any programming language and makes it interoperable with heterogenous
production workflows. A command-line interface is available too. When
used as a Java library, there are no OS restictions either.

## Modular

The system was designed with a modular architecture. Modularity is the
key to a better scalability and extensibility.

## Collaborative

The project is led and maintained by the DAISY Consortium and involves
several member organizations. This reduces the duplication of effort
and ensures maximum sharing of best practices among the user
community.

## Open-source

All software products maintained by the DAISY Consortium are available
under a business-friendly licence
([LGPL](https://www.gnu.org/licenses/lgpl.html)). This in order to
stimulate collaboration between organizations and to maximize reuse
and integration in other contexts, including commercial software.

Find out how you can [join the community](Contribute).

<section class="important">

## Get up and running

Download [the latest version](Download.html#latest-version) of DAISY
Pipeline 2 and find out [how it works](Get-Help).

</section>

<!--
## Feature Highlights
-->

## Background

The DAISY Pipeline is a collaborative project maintained by the [DAISY
Consortium](http://www.daisy.org/), with numerous organizations
participating and contributing to the development. The DAISY Pipeline
2 project is the follow-up of the original [DAISY
Pipeline](https://daisy.org/info-help/document-archive/archived-projects/pipeline-1/)
(now referred to as "Pipeline 1") project.

The initial DAISY Pipeline project was started in 2006. Since then,
new standards and technologies have emerged and have been embraced in
a total redesign of the DAISY Pipeline framework.

The overarching principles remain the same:

- provide functionality to __produce, maintain, and validate__ accessible digital formats
- embrace __good practices__ for the creation of __quality accessible content__
- support the __single source master__ approach where applicable
- __minimize overlap and duplication__, notably via the development of __reusable components__

By adopting modern standards (and off-the-shelf implementations of
those standards), version 2 of the project aims to

- prepare for the future
- better integrate with the publishing mainstream
- minimize the development and maintenance cost, allowing developers
  to ultimately focus more on actual transformations rather than the
  engine that drives the transformations
- lower the framework learning curve
- increase interoperability with the heterogeneous production workflows
- increase the likelihood of re-use in both open source and commercial applications.

Because a lot of organisations still rely on some Pipeline 1
converters in their production today, we are currently working on
making these legacy converters available through the DAISY Pipeline 2
user and programming interfaces, in order to make the transition to
Pipeline 2 smoother and to retire the Pipeline 1 user interface.

<!-- The project page on http://www.daisy.org/pipeline2 contains a shorter version of this page plus some administrative stuff:

The DAISY Pipeline 2 is an ongoing project to develop an open-source, cross-platform framework for the automated processing of digital content, supporting various input and output formats. It intends to facilitate the production and maintenance of accessible content for people with print disabilities. It is the follow-up and total redesign of the original <a href="http://www.daisy.org/pipeline">DAISY Pipeline 1</a> project.

A follow-up of the DAISY Pipeline 1 project
-------------------------------------------

The initial DAISY Pipeline project was started in 2006. Since then, new standards and technologies have emerged and have been embraced in a total redesign of the DAISY Pipeline framework.

The overarching principles remain the same:

- provide functionality to __produce, maintain, and validate__ accessible digital formats
- embrace __good practices__ for the creation of __quality accessible content__
- support the __single source master__ approach where applicable
- __minimize overlap and duplication__, notably via the development of __reusable components__

By adopting modern standards (and off-the-shelf implementations of those standards), version 2 of the project aims to

- prepare for the future
- better integrate with the publishing mainstream
- minimize the development and maintenance cost, allowing developers to ultimately focus more on actual transformations rather than the engine that drives the transformations
- lower the framework learning curve
- increase interoperability with the heterogeneous production workflows
- increase the likelihood of re-use in both open source and commercial applications.

More information
----------------

For more information on the Pipeline 2 project, please see:

- The Pipeline 2 [website](http://daisy.github.io/pipeline/).
- The Pipeline 2 [development site](https://github.com/daisy/pipeline), hosted on Github
- The Pipeline 2 [current work plan for
  2018](https://docs.google.com/document/d/104Ie8i3Uwo6vpyY9DXUVBfjr7FIA-jUf0sIjXgBgV4s/pub)
  (see also previous work plans for
  [2017](https://docs.google.com/document/d/11M-RamTdRJgjkJxKeR81V6T1Ycg2OL6fW5nOKKgUdEs/pub),
  [2016](https://docs.google.com/document/d/1VfAsEcoC301bDIr8cQ51VyiF_1UU98OM-1G7Yuh06E0/pub),
  [2015](https://docs.google.com/document/d/1wVVbSVHV6FmxnRiwwkknWJ4YH1rJbhPri8JxU0oe6XI/pub),
  [2014-15](https://docs.google.com/file/d/0B175tOPv2T71bGhrd1BqTHRHbFU),
  [2012-13](https://code.google.com/p/daisy-pipeline/wiki/ProjectCharterPhase2)
  and
  [2010-11](https://code.google.com/p/daisy-pipeline/wiki/ProjectCharterPhase1))

-->
