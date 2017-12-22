---
layout: default
---
# Issue Tracker

[This Github project](https://github.com/daisy/pipeline/issues) is the
main issue tracker for DAISY Pipeline 2. However, it is not the only
place where you can find issues because the Pipeline source code is
distributed over several smaller sub-projects that each have their own
issue tracker. If possible, use the individual trackers of the
sub-projects for issues that clearly belong to a specific
sub-project. All sub-projects are listed
[here](https://github.com/daisy/pipeline#code). Follow the link to the
appropriate sub-project and open the **Issues** tab.

{% capture repos %}
  daisy/pipeline
  daisy/pipeline-tasks
  daisy/pipeline-assembly
  daisy/pipeline-build-utils
  daisy/pipeline-cli-go
  daisy/pipeline-clientlib-go
  daisy/pipeline-clientlib-java
  daisy/pipeline-framework
  daisy/pipeline-gui
  daisy/pipeline-it
  daisy/pipeline-mod-audio
  daisy/pipeline-mod-braille
  daisy/pipeline-mod-nlp
  daisy/pipeline-mod-tts
  daisy/pipeline-modules-common
  daisy/pipeline-samples
  daisy/pipeline-scripts
  daisy/pipeline-scripts-utils
  daisy/pipeline-updater
  daisy/pipeline-updater-gui
  daisy/pipeline-webui
  daisy/braille-css
  daisy/jStyleParser
  daisy/osgi-libs
  daisy/xmlcalabash1
  daisy/xprocspec
  daisy/xproc-maven-plugin
  daisy/xspec-maven-plugin
  snaekobbi/pipeline-mod-braille
{% endcapture %}
{% assign repos = repos | normalize_whitespace | split:' ' %}

Before creating a new issue, please first check the
[existing issues](https://github.com/search?q=is%3Aopen{% for r in repos %}+repo%3A{{ r | replace:'/','%2F' }}{% endfor %}&type=Issues&s=updated&o=desc) to see if a similar issue was
already reported.
