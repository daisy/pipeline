# pipeline

This is a convenience "super" Pipeline project that aggregates all sub-projects and 3rd-party libraries.

This makes branching, building, and releasing of several sub-projects at once easier.

## Build the pipeline

**The build process can take a while, even without the unit test enabled**

This super-project requires the following `tools` available in your `PATH` environment variable:
- [Git](https://git-scm.com/) (`git` in terminal) 
- java 11, preferably [AdopOpenJDK 11](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot), (don't forget to set the `JAVA_HOME` environment variable)
- [Apache Maven 3.6+](https://maven.apache.org/) (`mvn` in terminal),
- [Apache Ant 1.10+](https://ant.apache.org/) (`ant` in terminal)
- [Golang](https://golang.org/) (`go` in terminal)
- [Bazaar](http://bazaar.canonical.com/en/) (`bzr` in terminal)
- [Gradle](https://gradle.org/) (`gradle` in terminal)

You can build the project archive using the following commands in a terminal positioned in the pipeline repository : 
- For macOS : 
```bash
mvn clean install -Dorg.daisy.org.ops4j.pax.url.mvn.settings="settings.xml" -Punpack-cli-mac -Punpack-updater-mac -Passemble-mac-zip
```
- For Linux : 
```bash
mvn clean install -Dorg.daisy.org.ops4j.pax.url.mvn.settings="settings.xml" -Punpack-cli-linux -Punpack-updater-linux -Passemble-linux-zip
```
- For Windows :
```bash
mvn clean install -Dorg.daisy.org.ops4j.pax.url.mvn.settings="settings.xml" -DskipTests -Punpack-cli-win -Punpack-updater-win -Punpack-updater-gui-win -Passemble-win-zip
```

You will find the resulting archive in the folder `assembly/target`. Just unzip its content (a `daisy-pipeline` folder) wherever you want to start using the pipeline (see [how to launch the pipeline](#launch-the-pipeline)).

### re-build / update

If you wish to test a modified module or package, you must specified the new version of the module (see the pom.xml of the module) in the `assembly/pom.xml` maven project.

If you already did the previous build step within the pipeline repo, you can rebuild the archive by going in the `assembly` sub-repo and launching the following command : 
- For macOS : 
```bash
mvn clean install -Punpack-cli-mac -Punpack-updater-mac -Passemble-mac-zip
```
- For Linux : 
```bash
mvn clean install -Punpack-cli-linux -Punpack-updater-linux -Passemble-linux-zip
```
- For Windows :
```bash
mvn clean install -Punpack-cli-win -Punpack-updater-win -Punpack-updater-gui-win -Passemble-win-zip
```

### Launch the pipeline

To launch the pipeline graphical application, go the `daisy-pipeline` folder (from the unzipped archive obtained after the [build](#build-the-pipeline)) and launch the following command from a terminal : 
- Mac and Linux : `bin/pipeline2 gui`
- Windows : `bin/pipeline2.bat gui`

## Build the website

TODO

Requires ruby with bundle gem available in path.

## Code and repo structure

The aggregating and the backporting of changes to the individual projects is done using a tool called [git-subrepo][]. The idea is that all the git magic will be done by the owners, and that committers can just treat this repository as a regular one. There is a rule though that committers need to follow because of some limitations of git-subrepo:

- Pull requests may not contain merge commits. In order to keep things structured, make one pull request per distinct feature.

Committers are of course also free to make pull requests to the individual repositories, or do other advanced things such as switching a certain sub-repository to an existing branch. Advanced git knowledge is required in these cases. Ask for help if needed.

The tree below shows the structure of the project:

- [assembly](https://github.com/daisy/pipeline-assembly)
- [cli](https://github.com/daisy/pipeline-cli-go)
- [clientlib-java](https://github.com/daisy/pipeline-clientlib-java)
- [framework](https://github.com/daisy/pipeline-framework)
- [gui](https://github.com/daisy/pipeline-gui)
- [it](https://github.com/daisy/pipeline-it)
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
  - [xprocspec](https://github.com/daisy/xprocspec)
  - [xproc-maven-plugin](https://github.com/daisy/xproc-maven-plugin)
  - [xspec-maven-plugin](https://github.com/daisy/xspec-maven-plugin)
- [webui](https://github.com/daisy/pipeline-webui)

### Building on CentOS/Redhat
- Install pcregrep (if not already installed): `yum install pcre-tools` or `yum install pcre`
- Install libxml (libxml needs to be at least version 20900, if already installed check version with `xmllint --version`):
- `wget ftp://xmlsoft.org/libxml2/libxml2-2.9.0-1.src.prm`
- `rpmbuild --rebuild libxml2-2.9.0-1.src.rpm`
- `rpm -i --force /path/to/rpm/libxml2-2.9.0-1.rpm`
- Run make: `make dist-rpm`

## Issues

This is also a common issue tracker for the Pipeline. Use the individual issue trackers of the sub-projects listed above for issues that clearly belong to a specific sub-project. Before creating a new issue, please first check the [existing open issues](https://github.com/search?l=&q=is%3Aopen++repo%3Adaisy%2Fpipeline++repo%3Adaisy%2Fpipeline-assembly++repo%3Adaisy%2Fpipeline-build-utils++repo%3Adaisy%2Fpipeline-cli-go++repo%3Adaisy%2Fpipeline-clientlib-go++repo%3Adaisy%2Fpipeline-clientlib-java++repo%3Adaisy%2Fpipeline-framework++repo%3Adaisy%2Fpipeline-gui++repo%3Adaisy%2Fpipeline-it++repo%3Adaisy%2Fpipeline-mod-audio++repo%3Adaisy%2Fpipeline-mod-braille++repo%3Adaisy%2Fpipeline-mod-nlp++repo%3Adaisy%2Fpipeline-mod-tts++repo%3Adaisy%2Fpipeline-modules-common++repo%3Adaisy%2Fpipeline-samples++repo%3Adaisy%2Fpipeline-scripts++repo%3Adaisy%2Fpipeline-scripts-utils++repo%3Adaisy%2Fpipeline-updater++repo%3Adaisy%2Fpipeline-updater-gui++repo%3Adaisy%2Fpipeline-webui++repo%3Adaisy%2Fbraille-css++repo%3Adaisy%2FjStyleParser++repo%3Adaisy%2Fosgi-libs++repo%3Adaisy%2Fxmlcalabash1++repo%3Adaisy%2Fxprocspec++repo%3Adaisy%2Fxproc-maven-plugin++repo%3Adaisy%2Fxspec-maven-plugin++repo%3Asnaekobbi%2Fpipeline-mod-braille&ref=advsearch&type=Issues&utf8=%E2%9C%93).

## Website

The `gh-pages` branch contains the source of the [Pipeline website](http://daisy.github.io/pipeline).

[git-subrepo]: https://github.com/ingydotnet/git-subrepo
