---
layout: home
title: Home
---
# DAISY Pipeline

The DAISY Pipeline is an open-source, cross-platform framework and
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
a DAISY Pipeline user interface specifically for processing Word
documents.

DAISY Pipeline also has programming interfaces. When run as a daemon
(web server), there are no restrictions. The server can be run on any
OS, and its platform neutral RESTful API allows it to be called from
any programming language and makes it interoperable with heterogenous
production workflows. A command-line interface is available too. When
used as a Java library, there are no OS restrictions either.

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
Pipeline and find out [how it works](Get-Help).

</section>

## Background

The DAISY Pipeline is a collaborative project maintained by the [DAISY
Consortium](http://www.daisy.org/), with numerous organizations
participating and contributing to the development. The "Pipeline 2"
project is the follow-up of the original [DAISY
Pipeline](https://daisy.org/info-help/document-archive/archived-projects/pipeline-1/)
(sometimes referred to as "Pipeline 1") project.

The initial DAISY Pipeline project was started in 2006. Since then,
new standards and technologies have emerged and have been embraced in
a total redesign of the DAISY Pipeline framework.

The overarching principles remain the same:

- provide functionality to __produce, maintain, and validate__ accessible digital formats
- embrace __good practices__ for the creation of __quality accessible content__
- support the __single source master__ approach where applicable
- __minimize overlap and duplication__, notably via the development of __reusable components__

By adopting modern standards (and off-the-shelf implementations of
those standards), the "Pipeline 2" project aims to

- prepare for the future
- better integrate with the publishing mainstream
- minimize the development and maintenance cost, allowing developers
  to ultimately focus more on actual transformations rather than the
  engine that drives the transformations
- lower the framework learning curve
- increase interoperability with the heterogeneous production workflows
- increase the likelihood of re-use in both open source and commercial applications.

The current version of DAISY Pipeline is feature complete and
production grade software. The legacy version is not maintained and
not recommended anymore. Users of the legacy version are advised to
update to the current version if possible. Organisations that rely on
legacy converters that are not available in the current version, are
asked to let us know, and we will make sure these converters are
ported.
