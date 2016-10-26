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
- [framework](https://github.com/daisy/pipeline-framework)
- [gui](https://github.com/daisy/pipeline-gui)
- libs
  - [braille-css](https://github.com/daisy/braille-css)
  - braille-utils
    - [braille-utils.api](https://github.com/brailleapps/braille-utils.api)
    - [braille-utils.impl](https://github.com/brailleapps/braille-utils.impl)
    - [braille-utils.pef-tools](https://github.com/brailleapps/braille-utils.pef-tools)
  - dotify
    - [dotify.api](https://github.com/brailleapps/dotify.api)
    - [dotify.common](https://github.com/brailleapps/dotify.common)
    - [dotify.formatter.impl](https://github.com/brailleapps/dotify.formatter.impl)
    - [dotify.hyphenator.impl](https://github.com/brailleapps/dotify.hyphenator.impl)
    - [dotify.text.impl](https://github.com/brailleapps/dotify.text.impl)
    - [dotify.task-api](https://github.com/brailleapps/dotify.task-api)
    - [dotify.task.impl](https://github.com/brailleapps/dotify.task.impl)
    - [dotify.task-runner](https://github.com/brailleapps/dotify.task-runner)
    - [dotify.translator.impl](https://github.com/brailleapps/dotify.translator.impl)
  - [jstyleparser](https://github.com/daisy/jStyleParser)
  - [osgi-libs](https://github.com/daisy/osgi-libs)
- modules
  - [audio](https://github.com/daisy/pipeline-mod-audio)
  - [braille](https://github.com/daisy/pipeline-mod-braille)
  - [common](https://github.com/daisy/pipeline-modules-common)
  - [nlp](https://github.com/daisy/pipeline-mod-nlp)
  - [scripts](https://github.com/daisy/pipeline-scripts)
  - [scripts-utils](https://github.com/daisy/pipeline-scripts-utils)
  - [tts](https://github.com/daisy/pipeline-mod-tts)
- updater
  - [cli](https://github.com/daisy/pipeline-updater)
  - [gui](https://github.com/daisy/pipeline-updater-gui)
- utils
  - [build-utils](https://github.com/daisy/pipeline-build-utils)
  - [xproc-maven-plugin](https://github.com/daisy/xproc-maven-plugin)
  - [xspec-maven-plugin](https://github.com/daisy/xspec-maven-plugin)
- [webui](https://github.com/daisy/pipeline-webui)

## Building
### Building on CentOS/Redhat
Install pcregrep (if not already installed):`sudo yum install pcre`

Install libxml (libxml needs to be at least version 20900, if already installed check version with `xmllint --version`):

`wget ftp://xmlsoft.org/libxml2/libxml2-2.9.0-1.src.prm`

`rpmbuild --rebuild libxml2-2.9.0-1.src.rpm`

`rpm -i --force /path/to/rpm/libxml2-2.9.0-1.rpm`

Run make: `make dist-rpm`

## Issues

This is also a common issue tracker for the Pipeline. Use the individual issue trackers of the sub-projects listed above for issues that clearly belong to a specific sub-project. Before creating a new issue, please first check the [existing issues](https://github.com/search?utf8=%E2%9C%93&q=repo%3Adaisy%2Fpipeline-assembly+repo%3Adaisy%2Fpipeline-framework+repo%3Adaisy%2Fpipeline-scripts+repo%3Adaisy%2Fpipeline-build-utils+repo%3Adaisy%2Fpipeline-webui+repo%3Asnaekobbi%2Fbraille-css+repo%3Asnaekobbi%2FjStyleParser+repo%3Abrailleapps%2Fdotify.api+repo%3Abrailleapps%2Fdotify.formatter.impl&type=Issues&ref=searchresults).

## Website

The `gh-pages` branch contains the source of the [Pipeline website](http://daisy.github.io/pipeline).

[git-subrepo]: https://github.com/ingydotnet/git-subrepo
