---
layout: default
---
# Easy Hacks

<!--
FIXME: duplication with Issue-Tracker.md
-->

{% capture repos %}
  daisy/pipeline
  daisy/pipeline-tasks
  daisy/pipeline-assembly
  daisy/pipeline-build-utils
  daisy/pipeline-cli-go
  daisy/pipeline-clientlib-go
  daisy/pipeline-clientlib-java
  daisy/pipeline-framework
  daisy/pipeline-modules
  daisy/pipeline-mod-audio
  daisy/pipeline-mod-braille
  daisy/pipeline-mod-nlp
  daisy/pipeline-mod-tts
  daisy/pipeline-modules-common
  daisy/pipeline-samples
  daisy/pipeline-scripts
  daisy/pipeline-scripts-utils
  daisy/pipeline-ui
  daisy/pipeline-updater
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

[These issues](https://github.com/search?utf8=%E2%9C%93&type=Issues&q=label%3Aeasyhack+state%3Aopen{% for r in repos %}+repo%3A{{ r | replace:'/','%2F' }}{% endfor %})
have been tagged as relatively easy to fix, so should be good starting
points for new developers.
