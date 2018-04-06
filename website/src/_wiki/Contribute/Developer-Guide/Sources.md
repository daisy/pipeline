# Source Code

All source code lives in Github repositories and is made available
under the free software licence
([LGPL](https://www.gnu.org/licenses/lgpl.html)).


## Main repositories

A first group of repositories are the principal repositories that make
up the DAISY Pipeline 2 software suite.

- [pipeline-assembly][]: The build script that assembles all
  components into various packages (for different platforms).

The main component is a Java application. It is heavily
modularized. Each of the following repositories produces one or more
JAR artefacts (hereafter called "bundles"). Together all these bundles
form the Java application.

- [pipeline-framework][]: The runtime framework
- [pipeline-scripts][]: The script modules
- [pipeline-scripts-utils][]: Utility modules related to various input or output formats
- [pipeline-modules-common][]: General-purpose utility modules
- [pipeline-mod-braille][]: Utility modules related to braille production
- [pipeline-mod-tts][]: Utility modules related to speech synthesis
- [pipeline-mod-audio][]: Utility modules related to audio encoding
- [pipeline-mod-nlp][]: Utility modules related to natural language processing
- [pipeline-gui][]: The graphical user interface
- [pipeline-updater-gui][]: Graphical user interface for the updater tool

The main Java component, which provides the web server and the GUI, is
complemented by a number of secondary components. They either act as
as clients of the web server (the graphical client and the command
line client), or work independently (the updater).

- [pipeline-webui][]: The web-based graphical user interface, written
  in Java/Scala (uses the Play! framework)
- [pipeline-cli-go][]: The official command line tool, written in Go
- [pipeline-updater][]: The updater tool, written in Go
- [pipeline-clientlib-java][]: Java library for use by clients of the
  Pipeline web server
- [pipeline-clientlib-go][]: Go library for use by clients of the
  Pipeline web server

A second group of repositories are some auxiliary repositories:

- [pipeline-samples][]: Sample documents in various formats for
  demonstrating and testing Pipeline scripts
- [pipeline-build-utils][]: Various build and test utilities specific
  to the Pipeline build system

Finally, there are some repositories that have been deprecated:

- [pipeline-cli][]: Command line tool written in Ruby


## Dependencies

Next to the main Pipeline repositories there are some noteworthy
dependencies (third-party libraries and build tools).

Some libraries were developed as part of DAISY Pipeline but possibly
have a larger scope and are therefore treated specially here:

- [braille-css][]: Braille CSS parser library for Java

Some libraries are projects developed by DAISY members:

- Liblouis ([liblouis][], [liblouis-java][]): Braille translator
  library written in C and maintained by SBS.
- Dotify ([api][dotify.api], [common][dotify.common],
  [formatter.impl][dotify.formatter.impl],
  [hyphenator.impl][dotify.hyphenator.impl],
  [text.impl][dotify.text.impl], [task-api][dotify.task-api],
  [task.impl][dotify.task.impl], [task-runner][dotify.task-runner],
  [translator.impl][dotify.translator.impl] ): Braille translator and
  formatter written in Java and owned by MTM.
- Braille Utils ([api][braille-utils.api], [impl][braille-utils.impl],
  [pef-tools][braille-utils.pef-tools]): Java Library for embossing
  and converting braille in PEF-format, owned by MTM.

Then there are some third-party projects that we have contributed to
(either forked or upstream):

- [epubcheck][]: Validation tool for EPUB
- [jStyleParser][]: CSS Parser library for Java
- [xmlcalabash1][]: XProc engine

Finally there are some auxiliary projects such as build and test
utilities:

- [xprocspec][]: Tool for testing XProc
- [xproc-maven-plugin][]: For running XProcSpec tests in JUnit or Maven
- [xspec-maven-plugin][]: For running
  [XSpec](https://github.com/expath/xspec/wiki) tests in JUnit or Maven
- [maven-parents][]: DAISY organisation wide Maven parent POMs


## Aggregator project

For convenience there is an "aggregator project" (or "super-project")
that aggregates all the main repositories and the most important
dependencies into
[a single repository](https://github.com/daisy/pipeline). The various
"sub-repositories" are arranged in a tree structure like this:

- [assembly][pipeline-assembly]
- [cli][pipeline-cli-go]
- clientlib
  - [java][pipeline-clientlib-java]
  - [go][pipeline-clientlib-go]
- [framework][pipeline-framework]
- [gui][pipeline-gui]
- libs
  - [braille-css][]
  - braille-utils
    - [braille-utils.api][]
    - [braille-utils.impl][]
    - [braille-utils.pef-tools][]
  - [com.xmlcalabash][xmlcalabash1]
  - dotify
    - [dotify.api][]
    - [dotify.common][]
    - [dotify.formatter.impl][]
    - [dotify.hyphenator.impl][]
    - [dotify.text.impl][]
    - [dotify.task-api][]
    - [dotify.task.impl][]
    - [dotify.task-runner][]
    - [dotify.translator.impl][]
  - [jstyleparser][jStyleParser]
  - [liblouis][]
  - [liblouis-java][]
  - [osgi-libs][]
- modules
  - [audio][pipeline-mod-audio]
  - [braille][pipeline-mod-braille]
  - [common][pipeline-modules-common]
  - [nlp][pipeline-mod-nlp]
  - [scripts][pipeline-scripts]
  - [scripts-utils][pipeline-scripts-utils]
  - [tts][pipeline-mod-tts]
- updater
  - [cli][pipeline-updater]
  - [gui][pipeline-updater-gui]
- utils
  - [build-utils][pipeline-build-utils]
  - [xproc-maven-plugin][]
  - [xprocspec][]
  - [xspec-maven-plugin][]
- [website](https://github.com/daisy/pipeline/tree/website)
- [webui][pipeline-webui]

If you want to build the Pipeline or contribute, it is recommended
that you check out this super-repository. You can actually treat it as
a single repository, i.e. you can make commits that touch multiple
sub-repositories, and you can make pull-requests to
[daisy/pipeline](https://github.com/daisy/pipeline). See also
[Contributing](#contributing) below.

Moreover, the super-projects provides a "super-build", meaning that
you can build the whole system with one command. See
[Build System](Building) for more info.


## Languages

The DAISY Pipeline is written in various programming
languages. Languages used in the main code are:

- Java
- Scala
- XProc
- XSLT
- Go

Additional languages used in third-party libraries are:

- C ([liblouis][])
- ANTLR ([braille-css][])


## Modules

Pipeline *modules* are the JAR files (bundles) that contain all the
conversion logic and that are "run" by the Pipeline framework. They
are the most obvious extension point of the Pipeline.

The core Pipeline modules live in these repositories:

- [pipeline-scripts][]
- [pipeline-scripts-utils][]
- [pipeline-modules-common][]
- [pipeline-mod-braille][]
- [pipeline-mod-tts][]
- [pipeline-mod-nlp][]
- [pipeline-mod-audio][]

Some examples of third-party modules are:

- [sbsdev/pipeline-mod-sbs](http://github.com/sbsdev/pipeline-mod-sbs)
- [nlbdev/pipeline-mod-nlb](https://github.com/nlbdev/pipeline-mod-nlb)
- [mtmse/pipeline-mod-mtm](https://github.com/mtmse/pipeline-mod-mtm)
- [snaekobbi/pipeline-mod-nota](https://github.com/snaekobbi/pipeline-mod-nota)
- [celiafi/pipeline-mod-celia](https://github.com/celiafi/pipeline-mod-celia)
- [dedicon/PIP](https://bitbucket.org/dedicon/pip.git)

In general a module is either a "script module" or a "utility module",
or it can also be something in between. Script modules contain a
Pipeline *script*, i.e. a top-level converter exposed in the user
interface. Utility modules contain common functionality that be used
in scripts or other utility modules.

Modules use each other through their APIs and provided (OSGi)
services. The API of a module is comprised of its public Java packages
and its public resources defined in its "catalog.xml" file. Services
are normally implemented in private packages.

The catalog.xml file associates a unique URI to every public resource
in the module. Resources can be

- XSLT files,
- XProc files (step declarations, libraries, or Pipeline scripts which are
  regular step declarations annotated with a `px:script` attribute in
  the catalog),
- CSS files,
- datatype grammar files (annotated with `px:data-type` in the
  catalog),
- or other files.

The public URIs can then be used from other modules to "import" that
particular functionality similar to the way Java packages are
imported. The import mechanism is of course specific to the language
in question: `xsl:include` or `xsl:import` for XSLT, `p:import` for
XProc, `@import` for CSS.

An overview of all modules with documentation of their APIs and
provided services can be found
[here](API-Documentation#apis-of-individual-modules).


## Contributing

We accept contributions of any kind to any part of the DAISY
Pipeline 2. It can be code changes, tests, documentation, or anything
else. Contributions should preferably happen through git/GitHub. The
general workflow is the following:

- Fork the [super-repository](https://github.com/daisy/pipeline) or
  one of the individual sub-repositories to your own Github account.
- Clone the repository and make changes to it.
- Push the changes back to GitHub and make a pull request.
- The branch is automatically tested with Travis.
- We review the PR and possibly request further changes before we
  merge.

Certain smaller contributions like small changes to documentation can
also be made directly within the GitHub web interface.

Contributions should meet a certain standard:

- Code changes should be accompanied by tests and documentation.
- Every commit should perform a single well-defined change. Unrelated
  changes should be in separate commits.
- Every commit should ideally pass the tests.
- Commit messages should clearly describe what the commit does and
  should start with a short subject (separated from the body with an
  empty line).

In addition, when you make a PR against the super-repository, for
technical reasons the branch may not contain any merge commits.




<!-- repository links: -->

[pipeline-assembly]: https://github.com/daisy/pipeline-assembly
[pipeline-framework]: https://github.com/daisy/pipeline-framework
[pipeline-scripts]: https://github.com/daisy/pipeline-scripts
[pipeline-scripts-utils]: https://github.com/daisy/pipeline-scripts-utils
[pipeline-modules-common]: https://github.com/daisy/pipeline-modules-common
[pipeline-mod-braille]: https://github.com/daisy/pipeline-mod-braille
[pipeline-mod-tts]: https://github.com/daisy/pipeline-mod-tts
[pipeline-mod-nlp]: https://github.com/daisy/pipeline-mod-nlp
[pipeline-mod-audio]: https://github.com/daisy/pipeline-mod-audio
[pipeline-gui]: https://github.com/daisy/pipeline-gui
[pipeline-cli-go]: https://github.com/daisy/pipeline-cli-go
[pipeline-cli]: https://github.com/daisy/pipeline-cli
[pipeline-webui]: https://github.com/daisy/pipeline-webui
[pipeline-clientlib-java]: https://github.com/daisy/pipeline-clientlib-java
[pipeline-clientlib-go]: https://github.com/daisy/pipeline-clientlib-go
[pipeline-updater]: https://github.com/daisy/pipeline-updater
[pipeline-updater-gui]: https://github.com/daisy/pipeline-updater-gui
[pipeline-samples]: https://github.com/daisy/pipeline-samples
[pipeline-build-utils]: https://github.com/daisy/pipeline-build-utils
[maven-parents]: https://github.com/daisy/maven-parents
[xprocspec]: https://github.com/daisy/xprocspec
[xproc-maven-plugin]: https://github.com/daisy/xproc-maven-plugin
[xspec-maven-plugin]: https://github.com/daisy/xspec-maven-plugin
[liblouis]: https://github.com/liblouis/liblouis
[liblouis-java]: https://github.com/liblouis/liblouis-java
[dotify.api]: https://github.com/brailleapps/dotify.api
[dotify.common]: https://github.com/brailleapps/dotify.common
[dotify.formatter.impl]: https://github.com/brailleapps/dotify.formatter.impl
[dotify.hyphenator.impl]: https://github.com/brailleapps/dotify.hyphenator.impl
[dotify.text.impl]: https://github.com/brailleapps/dotify.text.impl
[dotify.task-api]: https://github.com/brailleapps/dotify.task-api
[dotify.task.impl]: https://github.com/brailleapps/dotify.task.impl
[dotify.task-runner]: https://github.com/brailleapps/dotify.task-runner
[dotify.translator.impl]: https://github.com/brailleapps/dotify.translator.impl
[braille-css]: https://github.com/daisy/braille-css
[jStyleParser]: https://github.com/daisy/jStyleParser
[braille-utils.api]: https://github.com/brailleapps/braille-utils.api
[braille-utils.impl]: https://github.com/brailleapps/braille-utils.impl
[braille-utils.pef-tools]: https://github.com/brailleapps/braille-utils.pef-tools
[osgi-libs]: https://github.com/daisy/osgi-libs
[epubcheck]: https://github.com/daisy/epubcheck
[jhyphen]: https://github.com/daisy/jhyphen
[xmlcalabash1]: http://github.com/daisy/xmlcalabash1
