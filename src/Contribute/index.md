---
layout: default
---
# Contribute

The DAISY Pipeline 2 project is under active development and welcomes
any kind of contributions.

<!--
Depending on your skills or intents there
are several ways to participate.
-->

## Source Code

DAISY Pipeline 2 is open source software. It is licensed under the
[GNU Lesser General Public License (LGPL)](https://www.gnu.org/licenses/lgpl.html). All
the source code is hosted on
[Github](https://github.com/daisy/pipeline).

## Developers Mailing List

The best way to get in touch with the developers is through our
[mailing list](https://groups.google.com/forum/#!forum/daisy-pipeline-dev)
(hosted on Google Groups).

## Issue Tracker

[This Github project](https://github.com/daisy/pipeline/issues) is the
main issue tracker for DAISY Pipeline 2. However, it is not the only
place where you can find issues because the Pipeline source code is
distributed over several smaller sub-projects that each have their own
issue tracker. If possible, use the individual trackers of the
sub-projects for issues that clearly belong to a specific sub-project.

{% capture repos %}
  daisy/pipeline
  daisy/pipeline-assembly
  daisy/pipeline-framework
  daisy/pipeline-scripts
  daisy/pipeline-build-utils
  daisy/pipeline-webui
  snaekobbi/braille-css
  snaekobbi/jStyleParser
  brailleapps/dotify.api
  brailleapps/dotify.formatter.impl
{% endcapture %}
{% assign repos = repos | normalize_whitespace | split:' ' %}

Before creating a new issue, please first check the
[existing issues](https://github.com/search?utf8=%E2%9C%93&type=Issues&q={% for r in repos %}+repo%3A{{ r | replace:'/','%2F' }}{% endfor %}) to see if a similar issue was
already reported.

## Developer Guide

Guidance on how to get started can be found in the
[developer guide](Developer-Guide).

## Easy Hacks

[These issues](https://github.com/search?utf8=%E2%9C%93&type=Issues&q=label%3Aeasyhack+state%3Aopen{% for r in repos %}+repo%3A{{ r | replace:'/','%2F' }}{% endfor %})
have been tagged as relatively easy to fix, so should be good starting points for new developers.


<!--
API documentation: Javadoc etc.
-->
