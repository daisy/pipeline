Changes in release v1.13.3
==========================

### Modules

- Changes to braille production scripts, see [release notes of Braille modules v1.13.4](https://github.com/daisy/pipeline-modules/blob/master/braille/NEWS.md#v1134)

### Details

- [Closed issues in pipeline-assembly](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.13.3)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.13.3)
- [Closed issues in pipeline-modules](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules+milestone%3Av1.13.3)

Changes in release v1.13.2
==========================

### Modules

- Changes to braille production scripts, see [release notes of Braille modules v1.13.3](https://github.com/daisy/pipeline-modules/blob/master/braille/NEWS.md#v1133)

### Details

- [Closed issues in pipeline-assembly](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.13.2)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.13.2)
- [Closed issues in pipeline-modules](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules+milestone%3Av1.13.2)

Changes in release v1.13.1
==========================

### Modules

- **FIX** Bug in DTBook to DAISY 3 script

### Details

- [Closed issues in pipeline-assembly](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.13.1)
- [Closed issues in pipeline-modules](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules+milestone%3Av1.13.1)


Changes in release v1.13.0
==========================

### Distribution/Installation
- The minimum required Java version is changed back to 8 (but newer versions are also still supported)
- **FIX** Loading of TTS configuration on Mac OS
- **FIX** Problem with windows start menu

### Framework
- The core part of Pipeline including all the conversion scripts can now be run outside the OSGi framework. This means it is possible to use Pipeline as a regular Java library from any Java application and get most of the functionality. The web server and the graphical user interface are not part of this yet.
- **FIX** Out of memory error when zipping up a lot of mp3's

### Modules

- **NEW** Produced EPUBs are now compliant with the EPUB 3.2 specification
- **NEW** "Accessibility check" option for the "EPUB 3 Validator" script that invokes DAISY Ace if it is installed on the system.
- **FIX** Validation issues with produced DAISY 2.02, especially in case of text-only
- **FIX** Attempt to fix timeout issues in text-to-speech process
- **FIX** Speed up the DTBook to ZedAI conversion
- **FIX** Make it possible to run a DAISY 3 to EPUB 3 conversion offline, or when the DAISY website is down
- EPUBCheck updated to 4.2.2
- Various changes to braille production scripts, see [release notes of Braille modules v1.12.1](https://github.com/daisy/pipeline-mod-braille/blob/master/NEWS.md#v1121)
- XProc code cleanup

### Details

- [Closed issues in pipeline](https://github.com/issues?q=repo%3Adaisy%2Fpipeline+milestone%3Av1.13.0)
- [Closed issues in pipeline-assembly](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.13.0)
- [Closed issues in pipeline-build-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-build-utils+milestone%3Av1.13.0)
- [Closed issues in pipeline-cli-go](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-cli-go+milestone%3Av1.13.0)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-framework+milestone%3Av1.13.0)
- [Closed issues in pipeline-gui](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-gui+milestone%3Av1.13.0)
- [Closed issues in pipeline-modules](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules+milestone%3Av1.13.0)
- [Closed issues in pipeline-mod-audio](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-audio+milestone%3Av1.13.0)
- [Closed issues in pipeline-mod-braille](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-braille+milestone%3Av1.13.0)
- [Closed issues in pipeline-mod-nlp](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-nlp+milestone%3Av1.13.0)
- [Closed issues in pipeline-mod-tts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-tts+milestone%3Av1.13.0)
- [Closed issues in pipeline-modules-common](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules-common+milestone%3Av1.13.0)
- [Closed issues in pipeline-scripts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts+milestone%3Av1.13.0)
- [Closed issues in pipeline-scripts-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts-utils+milestone%3Av1.13.0)
- [Closed issues in pipeline-tasks](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-tasks+milestone%3Av1.13.0)
- [Closed issues in xproc-maven-plugin](https://github.com/issues?q=repo%3Adaisy%2Fxproc-maven-plugin+milestone%3Av1.13.0)
- [Closed issues in xspec-maven-plugin](https://github.com/issues?q=repo%3Adaisy%2Fxspec-maven-plugin+milestone%3Av1.13.0)


Changes in release v1.12.1
==========================

This is a bugfix release. It fixes **epub3-validator** which was broken in [release
v1.12.0](https://github.com/daisy/pipeline-assembly/releases/tag/v1.12.0).

### Details

- [Closed issues](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+repo%3Adaisy%2Fpipeline+milestone%3Av1.12.1)

Changes in release v1.12.0
==========================

### Distribution/Installation

- **FIX** Installation issues on Windows
- Minimum Java requirement is changed to Java 11

### Command Line Interface

- **NEW** Progress indication of jobs
- **NEW** Improved help for script options, incl. possible values and default values
- **NEW** Validation of script options
- **NEW** Configurable verbosity of help command

### Framework

- Refactoring of messaging system, incl. support for progress indication
- Calabash (XProc engine) updated to version 1.1.20
- Saxon (XSLT/XPath engine) updated to version 9.8.0.8

### Modules

- **NEW** Improved HTML chunking, incl. new option "chunk-size" for daisy3-to-epub3, dtbook-to-epub3, dtbook-to-html, zedai-to-epub3 and zedai-to-html scripts
- **NEW** Configuration file for well-known TTS voices
- Scripts with TTS now fail when audio is missing
- Various changes to braille production scripts, see [release notes of Braille modules v1.11.2](https://github.com/daisy/pipeline-mod-braille/blob/master/NEWS.md#v1112)
- XProc code cleanup

### Details

- [Closed issues in osgi-libs](https://github.com/issues?q=repo%3Adaisy%2Fosgi-libs+milestone%3Av1.12.0)
- [Closed issues in pipeline](https://github.com/issues?q=repo%3Adaisy%2Fpipeline+milestone%3Av1.12.0)
- [Closed issues in pipeline-assembly](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.12.0)
- [Closed issues in pipeline-build-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-build-utils+milestone%3Av1.12.0)
- [Closed issues in pipeline-cli-go](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-cli-go+milestone%3Av1.12.0)
- [Closed issues in pipeline-clientlib-go](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-clientlib-go+milestone%3Av1.12.0)
- [Closed issues in pipeline-clientlib-java](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-clientlib-java+milestone%3Av1.12.0)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-framework+milestone%3Av1.12.0)
- [Closed issues in pipeline-gui](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-gui+milestone%3Av1.12.0)
- [Closed issues in pipeline-mod-audio](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-audio+milestone%3Av1.12.0)
- [Closed issues in pipeline-mod-braille](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-braille+milestone%3Av1.12.0)
- [Closed issues in pipeline-mod-nlp](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-nlp+milestone%3Av1.12.0)
- [Closed issues in pipeline-mod-tts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-tts+milestone%3Av1.12.0)
- [Closed issues in pipeline-modules-common](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules-common+milestone%3Av1.12.0)
- [Closed issues in pipeline-scripts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts+milestone%3Av1.12.0)
- [Closed issues in pipeline-scripts-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts-utils+milestone%3Av1.12.0)
- [Closed issues in pipeline-tasks](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-tasks+milestone%3Av1.12.0)
- [Closed issues in pipeline-updater](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-updater+milestone%3Av1.12.0)
- [Closed issues in xmlcalabash1](https://github.com/issues?q=repo%3Adaisy%2Fxmlcalabash1+milestone%3Av1.12.0)
- [Closed issues in xproc-maven-plugin](https://github.com/issues?q=repo%3Adaisy%2Fxproc-maven-plugin+milestone%3Av1.12.0)
- [Closed issues in xspec-maven-plugin](https://github.com/issues?q=repo%3Adaisy%2Fxspec-maven-plugin+milestone%3Av1.12.0)


Changes in release v1.11.1
==========================

### Distribution/Installation

- **FIX** Installation and launch issues on Windows

### Modules

- **FIX** Error in **epub3-to-daisy202** when EPUB has multiple `dc:identifier` in OPF
- **FIX** Support "dir" attribute on dtbook element in **dtbook-to-epub3**
- **FIX** Support validating unzipped EPUBs
- **FIX** Issue with adapter for Qfrency speech engine
- Various changes to braille scripts, see [release notes of Braille modules v1.11.1](https://github.com/daisy/pipeline-mod-braille/blob/master/NEWS.md#v1111)

### Details

- [Closed issues in pipeline-tasks](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-tasks+milestone%3Av1.11.1)
- [Closed issues in pipeline](https://github.com/issues?q=repo%3Adaisy%2Fpipeline+milestone%3Av1.11.1)
- [Closed issues in pipeline-assembly](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.11.1)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-framework+milestone%3Av1.11.1)
- [Closed issues in pipeline-modules-common](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules-common+milestone%3Av1.11.1)
- [Closed issues in pipeline-mod-braille](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-braille+milestone%3Av1.11.1)
- [Closed issues in pipeline-mod-tts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-tts+milestone%3Av1.11.1)
- [Closed issues in pipeline-scripts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts+milestone%3Av1.11.1)
- [Closed issues in pipeline-scripts-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts-utils+milestone%3Av1.11.1)


Changes in release v1.11
========================

### Distribution/Installation

- **NEW** The application is now available as a set of Docker images

### Graphical User Interface

- Improved accessibility
- Improved troubleshooting for application launch issues on Windows

### Framework

- Simplified configuration

### Modules

- **NEW** DTBook to ODT (OpenDocument Text) script
- **NEW** DTBook to RTF (Rich Text Format) script
- **NEW** Adapter for Qfrency speech engine
- **NEW** EPUB 3 enhancer script for adding a braille rendition to an EPUB
- various changes to braille scripts, see [release notes of Braille modules v1.11.0](https://github.com/daisy/pipeline-mod-braille/blob/master/NEWS.md#v1110)
- **FIX** Issues with lost significant spaces in dtbook-to-zedai and zedai-to-html
- **FIX** Improve support for DTBook 1.1.0
- Simplified configuration of text-to-speech

### Details

- [Closed issues in pipeline-tasks](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-tasks+milestone%3Av1.11.0)
- [Closed issues in pipeline](https://github.com/issues?q=repo%3Adaisy%2Fpipeline+milestone%3Av1.11.0)
- [Closed issues in pipeline-assembly](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.11.0)
- [Closed issues in pipeline-build-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-build-utils+milestone%3Av1.11.0)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-framework+milestone%3Av1.11.0)
- [Closed issues in pipeline-gui](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-gui+milestone%3Av1.11.0)
- [Closed issues in pipeline-mod-audio](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-audio+milestone%3Av1.11.0)
- [Closed issues in pipeline-mod-braille](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-braille+milestone%3Av1.11.0)
- [Closed issues in pipeline-mod-nlp](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-nlp+milestone%3Av1.11.0)
- [Closed issues in pipeline-mod-tts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-tts+milestone%3Av1.11.0)
- [Closed issues in pipeline-modules-common](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules-common+milestone%3Av1.11.0)
- [Closed issues in pipeline-scripts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts+milestone%3Av1.11.0)
- [Closed issues in pipeline-scripts-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts-utils+milestone%3Av1.11.0)


Changes in release v1.10.4
==========================

This is a bugfix release. It includes fixes to **daisy202-to-epub3** and **epub3-to-daisy202** that
were advertised but not included in
[release v1.10.2](https://github.com/daisy/pipeline-assembly/releases/tag/v1.10.2).

### Details

- [Closed issues](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+repo%3Adaisy%2Fpipeline-modules-common+repo%3Adaisy%2Fpipeline-scripts-utils+repo%3Adaisy%2Fpipeline-scripts+milestone%3Av1.10.4)



Changes in release v1.10.3
==========================

- Fixed regression in all modules that perform XSD validation

### Details

- [Closed issues](https://github.com/issues?q=repo%3Adaisy%2Fpipeline+milestone%3Av1.10.3)



Changes in release v1.10.2
==========================

### Installer/Updater

- **FIX** Updater couldn't find the Pipeline installation from the registry on 64bit Windows
- **FIX** Updater couldn't find releases info ("404 not found")

### Framework and API

- Improve launch-time stability
- Support file names with spaces inside zipped job context
- Improve search algorithm for binaries
- **FIX** Spaces in paths of book files cause job failure

### Modules

- Add basic tests for all scripts
- **daisy202-to-epub3** Various bugfixes and/or tests:
  - **FIX** smil references inside links should also be removed
  - **FIX** Whenever a `epub:textref` attribute is added to a SMIL, an `attribute-value` attribute
    with the same value is added
  - **FIX** `epub:textref` in SMIL refers to `.html` files instead of `.xhtml` files
  - **FIX** Remove superfluous xmlns:d from package document metadata
- **dtbook-to-epub3** various bugfixes and/or tests:
  - **FIX** Issue with whitespace being removed
  - **FIX** The "assert validity" option on dtbook-to-epub3 does not seem to work
- **daisy202-validator**
  - **FIX** Attribute "shape" not allowed here
- **braille modules**
  - See [release notes of Braille modules v1.10.1](https://github.com/daisy/pipeline-mod-braille/blob/master/NEWS.md#v1101)
- **TTS modules**
  - **FIX** problem finding lame
- **Utility modules**
  - **fileset-utils** change the actual base URI of documents in `px:fileset-load`

### Build maintenance

- Cleanup dependencies in Maven POMs
- Update Calabash to v1.1.9
- Reorganize the build of some modified/OSGified 3rd party libraries
- Move web API tests to the `pipeline-framework` project
- Add a `modules-test-helper` project for reducing boiloplate in  Pipeline modules tests
- Various enhancements to the `pax-exam-helper` test helper
- Various improvements to the `xproc-maven-plugin`
- Re-enable all XSpec and and XProcSpec tests in the modules

### Details

- [Closed issues in pipeline-tasks](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-tasks+milestone%3Av1.10.2)
- [Closed issues in pipeline](https://github.com/issues?q=repo%3Adaisy%2Fpipeline+milestone%3Av1.10.2)
- [Closed issues in pipeline-assembly](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.10.2)
- [Closed issues in pipeline-build-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-build-utils+milestone%3Av1.10.2)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-framework+milestone%3Av1.10.2)
- [Closed issues in pipeline-gui](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-gui+milestone%3Av1.10.2)
- [Closed issues in pipeline-it](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-it+milestone%3Av1.10.2)
- [Closed issues in pipeline-mod-audio](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-audio+milestone%3Av1.10.2)
- [Closed issues in pipeline-mod-braille](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-braille+milestone%3Av1.10.2)
- [Closed issues in pipeline-mod-nlp](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-nlp+milestone%3Av1.10.2)
- [Closed issues in pipeline-mod-tts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-tts+milestone%3Av1.10.2)
- [Closed issues in pipeline-modules-common](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules-common+milestone%3Av1.10.2)
- [Closed issues in pipeline-scripts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts+milestone%3Av1.10.2)
- [Closed issues in pipeline-scripts-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts-utils+milestone%3Av1.10.2)
- [Closed issues in pipeline-updater-gui](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-updater-gui+milestone%3Av1.10.2)



Changes in release v1.10.1
==========================

### Modules

- Fixed missing audio bug in scripts with speech synthesis



Changes in release v1.10
========================

### Distribution

- **NEW** new graphical user interface (GUI)
- **NEW** installers for the GUI on Windows
- **NEW** packaged application on Mac OS X
- online Java installer bundled in the Windows installer
- Java Runtime Environment bundled in the Mac OS X app

### Command Line Interface

- **NEW** `clean` command to remove jobs with an `ERROR` status
- inputs and options arguments are no longer prefixed with `--i` and `--x`
- the `version` command now works as expected
- Properly detect Java under OpenJDK and Ubuntu 15.04

### Framework and API

- **NEW** Add datatypes to options and list them through the api
- **NEW** Expose the default value for options
- **NEW** Add support for job batches
- Get rid of the folder in zipped ports and options
- Catch out of memory errors
- Return a more meaningful error when inputs are not corect
- Improved control of script removal
- Improved logging filters
- Normalize whitespace in script documentation parsing
- Do not strip out spaces if xml:space="preserve"
- Allow posting job requests using a namespace prefix

### Modules

- **NEW** DAISY 2.02 validator
- **NEW** DAISY 3 (audio-only) to DAISY 2.02
- **NEW** EPUB 3 validator (EpubCheck)
- **NEW** EPUB 3 to PEF script
- **NEW** HTML to PEF script
- Experimental audio-only DAISY 3 production
- Improved MathML production in DAISY 3
- Improved NIMAS validation
- Optimized fileset lading
- ... and various other bug fixes

### Details

- [Closed issues in pipeline-tasks](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-tasks+milestone%3Av1.10)
- [Closed issues in pipeline-issues](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-issues+milestone%3Av1.10)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-framework+milestone%3Av1.10)
- [Closed issues in pipeline-scripts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts+milestone%3Av1.10)
- [Closed issues in pipeline-scripts-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts-utils+milestone%3Av1.10)
- [Closed issues in pipeline-modules-common](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules-common+milestone%3Av1.10)
- [Closed issues in pipeline-mod-braille](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-braille+milestone%3Av1.10)
- [Closed issues in pipeline-mod-audio](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-audio+milestone%3Av1.10)
- [Closed issues in pipeline-mod-nlp](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-nlp+milestone%3Av1.10)
- [Closed issues in pipeline-mod-tts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-tts+milestone%3Av1.10)
- [Closed issues in pipeline-cli-go](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-cli-go+milestone%3Av1.10)
- Detailed release notes for pipeline-mod-braille:
  [1.9.16](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.16)
  [1.9.15](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.15)
  [1.9.14](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.14)
  [1.9.13](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.13)
  [1.9.12](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.12)
  [1.9.11](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.11)
  [1.9.10](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.10)
  [1.9.9](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.9)
  [1.9.8](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.8)
  [1.9.7](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.7)
  [1.9.6](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.6)
  [1.9.5](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.5)
  [1.9.4](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.4)
  [1.9.3](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.3)
  [1.9.2](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.2)
  [1.9.1](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.9.1)



Changes in release v1.9
=======================

### Distribution

- **NEW** new CLI implementation (in [Go](https://golang.org/))
- **NEW** per-platform distribution, with native CLI executables
- improve Java detection on Mac OS X
- notify users of unsupported Java version
- improve build of Debian packages

### Framework and API

- **NEW** Primary outputs are returned in the jobs list from the web API
- **NEW** Script version is returned in the web API
- Update Calabash (XProc engine) to v1.0.23
- Configurable number of concurrent threads
- Fix support for UNC paths on Windows

### Modules

- **NEW** DAISY 2.02 validator
- **NEW** DTBook to ODT script
- **NEW** DTBook to EPUB 3 with TTS-narrated Media Overlays
- **NEW** EPUB 3 to DAISY 2.02 script (experimental)
- **audio** new API for audio encoders
- **audio** make sure we have logs when Lame is failing
- **epub3** Set the title metadata in EPUB 3 HTML Content Docs
- **epub3** Fix improper tagging of spine items as "non-linear"
- **daisy202-to-epub3** Align audio-only conversion to the TIES guidelines
- **dtbook** Fix loss of MathML IDs when converting to ZedAI or EPUB 3
- **dtbook** Fix support for validation of DTBook 2005-1 and 1.1.0
- **nlp** Proper detection of sentences and words in multiple HTML documents
- **tts** Fix a bug with voice listing on Mac OS X
- **tts** Possibility to use XSLT in TTS SSML adapters
- **tts** Improve multithreading
- **tts** Gender-based voice selection
- **tts** New modules to process EPUB 3 documents
- **tts** Fine selection of TTS voice with aural CSS
- **tts** improve support for SAPI5
- **tts** Delete the generated audio directory when the JVM exits gracefully

### Details

- [Closed issues in pipeline-tasks](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-tasks+milestone%3Av1.9)
- [Closed issues in pipeline-issues](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-issues+milestone%3Av1.9)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-framework+milestone%3Av1.9)
- [Closed issues in pipeline-scripts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts+milestone%3Av1.9)
- [Closed issues in pipeline-scripts-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts-utils+milestone%3Av1.9)
- [Closed issues in pipeline-modules-common](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules-common+milestone%3Av1.9)
- [Closed issues in pipeline-mod-braille](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-braille+milestone%3Av1.9)
- [Closed issues in pipeline-mod-audio](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-audio+milestone%3Av1.9)
- [Closed issues in pipeline-mod-nlp](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-nlp+milestone%3Av1.9)
- [Closed issues in pipeline-mod-tts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-tts+milestone%3Av1.9)
- [Closed issues in pipeline-cli-go](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-cli-go+milestone%3Av1.9)



Changes in release v1.8.1
=========================

### Windows Installer

- **FIX** issue that prevented a 64-bit JRE to be detected

### Framework and API

- **FIX** a memory leak in the XProc adapter.
- **FIX** a bug preventing the deletion of a job's log in Windows
- **FIX** URI mapping for the non-local mode
- **FIX** Recalculate priorities when a new job is sent to the queue
- **FIX** Add `nicename` to the `jobElement` schema (Web API)
- **FIX** The CLI now returns 255 if the job failed

### Web UI

- **FIX** issue with reports not being displayed

### Modules

- **zedai-to-epub3** Fix a conversion issue when the source has multiple `toc` elements

### Details

- [Closed issues in pipeline-tasks](https://github.com/daisy/pipeline-tasks/issues?q=milestone%3Av1.8.1)
- [Closed issues in pipeline-issues](https://github.com/daisy/pipeline-issues/issues?q=milestone%3Av1.8.1)
- [Closed issues in pipeline-assembly](https://github.com/daisy/pipeline-assembly/issues?q=milestone%3Av1.8.1)
- [Closed issues in pipeline-framework](https://github.com/daisy/pipeline-framework/issues?q=milestone%3Av1.8.1)
- [Closed issues in pipeline-webui](https://github.com/daisy/pipeline-webui/issues?q=milestone%3Av1.8.1)
- [Closed issues in pipeline-scripts](https://github.com/daisy/pipeline-scripts/issues?q=milestone%3Av1.8.1)



Changes in release v1.8
=======================

### Framework

- Update Calabash (XProc engine) to version 1.0.18
- Update Saxon (XSLT/XPath engine) to version 9.5.1.5
- Reorganize the framework's packages and projects
- The IP address the web service binds to is now configurable
- Catch logging statements from EclipseLink libraries
- Add a priority-management system to the job queue
- New utility class `BinaryFinder` to find executables in `$PATH`

### Modules

- **NEW** dtbook-to-daisy3 script with TTS-based audio production
- **NEW** modules for TTS-based audio production, including adapters for: Acapela TTS (v7), eSpeak,
  Microsoft Windows SAPI5, Max OS X Speech<br />_Note: the SAPI5 adapter requires the
  pre-installation of
  [Visual C++ Redistributable Packages](www.microsoft.com/en-us/download/details.aspx?id=30679)
  runtime components._
- **NEW** modules for NLP-based structure detection
- **NEW** EpubCheck adapter module (script not included in this release)
- **braille** Property for using an externally installed liblouisutdml only
- **braille** Remove `-brl-` prefix from Braille CSS properties
- **braille** Add CSS properties `border`, `margin`, `padding`, `left`, `right`
- **braille** Deprecate CSS "display: toc-item"
- **braille** Improve `pef:compare`
- **braille** css-core: allow functions in 'content' declarations
- **braille** liblouis-formatter: render TOC items more accurately
- **braille** Update to liblouis 2.5.4 and liblouis-java 1.2.0
- **braille** Add many tests
- **common-utils** New `px:message` step that allows to set logging levels
- **common-utils** New `px:i18n-translate` XPath function and XProc step used for localization
- **css-utils** New XSLT utility to retrieve a list of CSS stylesheet URIs from a document
- **daisy202-to-epub3** New option to set the output file name
- **daisy202-to-epub3** The default EPUB file nameuse is now only based on the identifier
- **daisy202-to-epub3** Copy more of the metadata to the resulting EPUB3
- **daisy202-to-epub3** Improved performance
- **daisy3-to-epub3** temporary files are no longer included in the result directory
- **dtbook-to-epub3** temporary files are no longer included in the result directory
- **dtbook-to-zedai** Better conversion of image descriptions in prodnotes
- **epub3-utils** Compatibility with the latest EPUB 3.0.1 specifications
- **epub3-utils** Allow non-linear spine items in `px:epub3-opf-create`
- **epub3-utils** Allow non-numbered page breaks (use a hyphen in the Nav Doc)
- **file-utils** Expand 8.3 file names during URL normalization
- **file-utils** Add a 2-args pf:normalize-uri that discards URI fragments
- **fileset-utils** Add support for "file:/...zip!/..." URIs
- **fileset-utils** Added "encode-as-base64" option to `px:unzip-fileset`
- **fileset-utils** Various fixes and improvements to `px:fileset-store`
- **html-utils** Rewrite of the HTML to XHTML5 upgrader + tests
- **html-utils** Simplify and improve the `html-to-fileset` implementation
- **html-to-epub3** Better conversion of `longdesc` and `aria-describedat` attributes
- **html-to-epub3** DIAGRAM descriptions are now converted to HTML embedded in hidden `iframe`
  elements
- **mediaoverlays-utils** improved performance
- **validation-utils** Added support for message severity and report metadata
- **zedai-to-epub3** temporary files are no longer included in the result directory
- **zip-utils** don't create d:file elements for directories when unzipping
- **all** Integration of XSpec testing
- **all** Update custom XPath functions to the new Saxon 9.5 API
- **all** reorganize Maven POMs and BoMs
- **all** and other small fixes and improvements

### Web UI

- The Web UI now must run on the same file system as the Pipeline engine
- better file names for downloads
- **FIXED** incorrect content type was returned when downloading single files
- **FIXED** Unable to set password for newly created account
- **FIXED** Missing submit button in "add user" section of admin settings
- **FIXED** Web UI does not allow downloading results bigger than 100 MB

### CLI (Ruby implementation)

- Add job priority option and print it in the job status
- Add client priority options
- Add queue command and resource
- Add options to move jobs up and down the execution queue

### Installation

- Change java version check from nsis installer
- A Debian package can now be produced from the assembly project

### Details

- [Closed issues in pipeline-tasks](https://github.com/daisy/pipeline-tasks/issues?milestone=1&state=closed)
- [Closed issues in pipeline-issues](https://github.com/daisy/pipeline-issues/issues?milestone=1&state=closed)
- [Closed issues in pipeline-framework](https://github.com/daisy/pipeline-framework/issues?milestone=1&state=closed)
- [Closed issues in pipeline-modules-common](https://github.com/daisy/pipeline-modules-common/issues?milestone=1&state=closed)
- [Closed issues in pipeline-scripts-utils](https://github.com/daisy/pipeline-scripts-utils/issues?milestone=1&state=closed)
- [Closed issues in pipeline-scripts](https://github.com/daisy/pipeline-scripts/issues?milestone=1&state=closed)
- [Closed issues in pipeline-mod-braille](https://github.com/daisy/pipeline-mod-braille/issues?milestone=1&state=closed)
- [Closed issues in pipeline-mod-audio](https://github.com/daisy/pipeline-mod-audio/issues?milestone=1&state=closed)
- [Closed issues in pipeline-mod-nlp](https://github.com/daisy/pipeline-mod-nlp/issues?milestone=1&state=closed)
- [Closed issues in pipeline-mod-tts](https://github.com/daisy/pipeline-mod-tts/issues?milestone=1&state=closed)



Changes in release v1.7
=======================

### Command-line tool

- Now the results are always get through a zipped file with the `--output` option
- Handle `VALIDATION_FAIL` status 
- Fix single result handling
- Move `.lastid` to the appropriate folder ( `%APP_DATA%/Daisy Pipeline 2/dp2/` in windows;
  `~/.daisy-pipeline/dp2` in linux and `~/Library/Application Support/DAISY Pipeline 2/dp2` in OS X
- Added suport for multiple-valued options

### Web API

- `/scripts/$ID` : All the outputs are filtered out
- `/scripts/$ID` : Order of options preserved from the script when building the xml representation.
- alive: `@mode` disappears in favor of `@localfs=(true|false)`
- `jobs/$ID` : The file size is returned along with the result files (not for the zip files).
- `jobs/$ID` : When the local fs is accessible the actual location is returned in the result xml
  response. This can be used to fetch the results from disk bypassing the web ui.
- MD5 and file size added to the http headers when a file is returned.
- Added support for multiple-valued options.

### Framework

- When a validation fails during the job execution the `VALIDATION_FAIL` status is
  returned. (Currently only working with validation scripts but all the scripts that validate
  outputs could implement this functionality in the future).
- Update to guava version 15.0
- Custom logger avoids creating default log file and duplicating framework logging lines.
- The framework controls all the outputs as it used to do in remote mode and they have to be fetched
  through the web api
- Fixed size limits for inputs and options.

### Modules

- **NEW**: asciimath-utils module wrapping ASCIIMathML.js
- **common-utils**: addeed missing DTDs to catalogs
- **common-utils**: `px:assert`: added test-count-min and test-count-max options
- **common-utils**: new `px:tokenize` step
- **fileset-utils**: `px:fileset-store`: store c:data documents as text
- **fileset-utils**: `px:fileset-store`: don't systemtically indent XML
- **fileset-utils**: `px:fileset-store`: store `c:data` documents as text
- **fileset-utils**: `px:fileset-store`: support serialization options
- **fileset-utils**: new `px:fileset-rebase` step
- **html-to-epub3**: support multiple HTML documents as input
- **html-to-epub3**: allow to provide custom metadata
- **html-to-epub3**: improved HTML chunking
- **html-to-epub3**: support empty page breaks in page lists
- **epub3-pub-utils**: better metadata merging
- **zip-utils**: new `px:unzip-fileset` step
- **zedai-to-pef**: page numbering improvements (e.g. support `counter-reset: braille-page' in CSS)
- **zedai-to-pef**: better whitespace handing (e.g. support `xml:space=preserve')
- **zedai-to-pef**: update dependencies (liblouis, libhyphen dictionaries, etc.)
- **all**: harmonized all URIs of public components
- **all**: use fileset-utils for all file set loading
- **all**: various fixes and improvements

### Web UI

- support for running behind proxies (no absolute URLs; the absolute URL to the Web UI must be set
  in e-mail settings if you want to enable e-mail support).
- Added support for hiding scripts from guests and public users.
- Support for the new job result API where you can download individual files. when there's only one
  file in the results, the main download button downloads that file directly. Otherwise it downloads
  the zip.
- support for HTML reports that are displayed inline on the job status page when the job finishes.
- Temporary and result directories are not handled by the Web UI anymore; they are handled by the
  Pipeline 2 engine. No need to configure them in the UI anymore.
- Ability to compile the webui in a continous integration environment (i.e. Jenkins)
- Renamed project from `pipeline2-webui` to `daisy-pipeline-webui`
- Split desktop and server into separate maven projects (desktop depends on server)
- Packaging of the distributables are now performed by the "pipeline-assembly" project
