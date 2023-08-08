---
layout: default
---
# Issue Tracker

[This Github project](https://github.com/daisy/pipeline/issues) is the
main issue tracker for DAISY Pipeline 2. However, it is not the only
place where you can find issues, as the Pipeline source code is
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

Before creating a new issue, please first check the
[existing issues](https://github.com/search?q=is%3Aopen{% for r in repos %}+repo%3A{{ r | replace:'/','%2F' }}{% endfor %}&type=Issues&s=updated&o=desc) to
search for similar issues with related keywords.

## Providing Error Logs for an Issue

For all new issues, we ask that users provide their error logs via
[GIST](https://help.github.com/articles/about-gists/), a fast and
simple file-sharing service for GitHub, for us to diagnose and fix the
problem.

### Finding your Log Files

If an error message was displayed when you tried to start DAISY
Pipeline 2, the log files should have been automatically opened with
File Explorer in a minimized window. If they were, continue to the
next section **Uploading your Log Files**.

If the log files weren't opened: open File Explorer, copy/paste
**%AppData%/DAISY Pipeline 2/log** in the navigation bar, and press
**Enter**.

### Uploading your Log Files

1. Create a [GIST](https://gist.github.com).
2. Select all of your log files and drag them into the GIST.
3. Submit the new GIST as a Secret GIST and provide the link to it in
   the appropriate section of your GitHub issue.

__NOTE__: Do __NOT__ paste the contents of your logs into the issue,
as the issue will be much too long.
