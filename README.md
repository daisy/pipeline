# pipeline

## Code

This is a convenience "super" Pipeline project that aggregates all sub-projects and 3rd-party libraries.

This makes branching, building, and releasing of several sub-projects at once easier.

The aggregating and the backporting of changes to the individual projects is done using a tool called [git-subrepo][]. The idea is that all the git magic will be done by the owners, and that committers can just treat this repository as a regular one. There is a rule though that committers need to follow because of some limitations of git-subrepo:

- Pull requests may not contain merge commits. In order to keep things structured, make one pull request per distinct feature.

Committers are of course also free to make pull requests to the individual repositories, or do other advanced things such as switching a certain sub-repository to an existing branch. Advanced git knowledge is required in these cases. Ask for help if needed.

The tree below shows the structure of the project:

- [assembly](https://github.com/daisy/pipeline-assembly)
- [cli](https://github.com/daisy/pipeline-cli-go)
- clientlib
  - [java](https://github.com/daisy/pipeline-clientlib-java)
  - [go](https://github.com/daisy/pipeline-clientlib-go)
- [framework](https://github.com/daisy/pipeline-framework)
- [gui](https://github.com/daisy/pipeline-gui)
- libs
  - [braille-css](https://github.com/daisy/braille-css)
  - [dotify.library](https://github.com/mtmse/dotify.library)
  - [jsass](https://github.com/snaekobbi/jsass)
  - [jstyleparser](https://github.com/daisy/jStyleParser)
  - [liblouis-java](https://github.com/liblouis/liblouis-java)
  - [osgi-libs](https://github.com/daisy/osgi-libs)
- [modules](https://github.com/daisy/pipeline-modules)
- [ui](https://github.com/daisy/pipeline-ui)
- utils
  - [build-utils](https://github.com/daisy/pipeline-build-utils)
  - [xprocspec](https://github.com/daisy/xprocspec)
  - [xproc-maven-plugin](https://github.com/daisy/xproc-maven-plugin)
  - [xspec-maven-plugin](https://github.com/daisy/xspec-maven-plugin)
- [webui](https://github.com/daisy/pipeline-webui)

See http://daisy.github.io/pipeline/Contribute/Developer-Guide/Sources/ for more information about the source code.


## Issues

This is also a common issue tracker for the Pipeline. Use the individual issue trackers of the sub-projects listed above for issues that clearly belong to a specific sub-project. Before creating a new issue, please first check the [existing open issues](https://github.com/search?l=&q=is%3Aopen++repo%3Adaisy%2Fpipeline++repo%3Adaisy%2Fpipeline-assembly++repo%3Adaisy%2Fpipeline-build-utils++repo%3Adaisy%2Fpipeline-cli-go++repo%3Adaisy%2Fpipeline-clientlib-go++repo%3Adaisy%2Fpipeline-clientlib-java++repo%3Adaisy%2Fpipeline-framework++repo%3Adaisy%2Fpipeline-gui++repo%3Adaisy%2Fpipeline-ui++repo%3Adaisy%2Fpipeline-it++repo%3Adaisy%2Fpipeline-mod-audio++repo%3Adaisy%2Fpipeline-mod-braille++repo%3Adaisy%2Fpipeline-mod-nlp++repo%3Adaisy%2Fpipeline-mod-tts++repo%3Adaisy%2Fpipeline-modules++repo%3Adaisy%2Fpipeline-modules-common++repo%3Adaisy%2Fpipeline-samples++repo%3Adaisy%2Fpipeline-scripts++repo%3Adaisy%2Fpipeline-scripts-utils++repo%3Adaisy%2Fpipeline-updater++repo%3Adaisy%2Fpipeline-updater-gui++repo%3Adaisy%2Fpipeline-webui++repo%3Adaisy%2Fbraille-css++repo%3Adaisy%2FjStyleParser++repo%3Adaisy%2Fosgi-libs++repo%3Adaisy%2Fxmlcalabash1++repo%3Adaisy%2Fxprocspec++repo%3Adaisy%2Fxproc-maven-plugin++repo%3Adaisy%2Fxspec-maven-plugin++repo%3Asnaekobbi%2Fpipeline-mod-braille&ref=advsearch&type=Issues&utf8=%E2%9C%93).


## Website

The `gh-pages` branch contains the source of the [Pipeline website](http://daisy.github.io/pipeline).

[git-subrepo]: https://github.com/ingydotnet/git-subrepo
