# Release Management

## Release schedule

There is currently no fixed release schedule. A release is made when
there are (important) enough new improvements or bug fixes to justify
one, or when we get special requests from users.

There are three kinds of releases:

- available as full download or update
- available as full download only
- available as update only

In general, smaller releases like bugfix releases can only be
installed with the updater tool. Upon special request they can also be
made available via download. Major releases with significant changes
to the runtime can not be installed with the updater, for technical
reasons.


## Versioning rules

Different versioning rules apply for different parts of the
Pipeline. (For an overview of the various components see the page
about [source code](Source).)

* The version number of pipeline-assembly determines the main version
  of the Pipeline. Smaller changes are marked by updates of the patch
  segment, bigger changes are marker by updates of the minor segment.

* The version numbers of "bom", "parent" and "aggregator" POMs are
  aligned with the version of pipeline-assembly. Their major and minor
  segments should always match the latest version of
  pipeline-assembly, their patch segments may vary. The "bom" (bill of
  material) POMs are the places where the "current" versions of all
  bundles are listed. The boms are imported by the assembly, and every
  POM that has a dependency that is declared in a bom should import
  that bom. This way the version declarations are nicely centralized
  and reduced to a minimum.

* Version numbering of bundles should be done according to the
  versioning rules of OSGi a.k.a. [semantic versioning](semver). Note
  that the "API" of a bundle is not always strictly defined (e.g. in
  case of public XSLT, XProc or CSS code) so there is a bit of room
  for interpretation. As an exception to this rule, the version
  numbers of forks of third-party libraries should match those of the
  corresponding original versions, possibly with some segments added
  to the number to distinguish versions.

* For pipeline-clientlib-java, pipeline-webui, pipeline-cli-go and
  pipeline-updater, semantic versioning applies as well.

* pipeline-clientlib-go is not versioned.

When updating version numbers, care must be taken to keep versions
numbers in project declarations and dependency declaractions
aligned. If both are snapshots, they must match. The super-build will
error if this is not the case. In addition, if a release has been made
of a certain component, or if significant changes have been made to it
since the last release, all dependent components should ideally be
updated as soon as possible to compile and test against the latest
version. This allows you to see the consequences of a change
immediately so that bugs are spotted sooner, and by running the test
suite you can ensure that all the latest components are compatible. It
can be enforced with the command `make check-versions` in the
super-project.


## Release procedure

The assumption is made that at the time a release is made, a number of
things have already been done beforehand, during development:

- Versions numbers have been updated according to the versioning rules
  described above.
- NEWS files (in
  [pipeline-assembly](https://github.com/daisy/pipeline-assembly/blob/develop/NEWS.md)
  and
  [pipeline-mod-braille](https://github.com/daisy/pipeline-mod-braille/blob/develop/NEWS.md))
  have been updated with changes since the last release.
- Milestones have been added to Github issues.

To initiate a release, run the following command in the super-project:

~~~sh
make release
~~~

This will generate instructions for releasing the assembly and all its
components. The instructions are mostly just shell commands that can
be executed without much thinking.

The release script covers the following:

- creating release branches
- building, uploading and staging Maven artifacts
- manual releasing of non-Maven components
- publishing the release descriptor to a branch

Things the script does not cover include:

- closing the milestones
- finalizing the release notes (NEWS files) of pipeline-mod-braille
  and pipeline-assembly
- editing the
  [distribution README file](https://github.com/daisy/pipeline-assembly/blob/master/src/main/resources/README.txt)
  in pipeline-assembly
- creating pull requests
- releasing staged artifacts
- merging release branches
- pushing git tags
- editing the Github releases of pipeline-mod-braille and pipeline-assembly
- building and uploading the distribution packages to the release page
  of pipeline-assembly (if not a updater-only release)
- updating the website
- sending the release announcement

Ideally the release notes should already be made prior to making the
release, and milestones should have been set, however because life is
not perfect, it is best to search the Github issues (use the link at
[https://github.com/daisy/pipeline#issues](https://github.com/daisy/pipeline#issues))
for mistakes or things that were forgotten:

- Sort the issues by "Recently updated"
- Search for issues closed after a certain date with `closed:>yyyy-mm-dd`.
- Search for issues in the current milestone with `milestone:v0.0.0`

Finalize the release notes by adding a link to the
pipeline-mod-braille release notes as well as a list of links to
closed issues:

```md
### Details
- [Closed issues in pipeline-tasks](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-tasks+milestone%3Av1.10.0)
- [Closed issues in pipeline](https://github.com/issues?q=repo%3Adaisy%2Fpipeline+milestone%3Av1.10.0)
- [Closed issues in pipeline-assembly](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.10.0)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-framework+milestone%3Av1.10.0)
- [Closed issues in pipeline-gui](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-gui+milestone%3Av1.10.0)
- [Closed issues in pipeline-mod-audio](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-audio+milestone%3Av1.10.0)
- [Closed issues in pipeline-mod-braille](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-braille+milestone%3Av1.10.0)
- ...
```

The "Release notes" section of the distribution README file in
pipeline-assembly can be taken from the NEWS file. The details should
be replaced with a link to the Github release page which has a full
copy of the release notes.

An email announcement should be sent to the following lists:

- daisy-pipeline-dev@googlegroups.com
- technical-developments@mail.daisy.org
- support@mail.daisy.org
- daisy-members@daisy.org

The announcement can be based on the following template:

> Subject: Pipeline 2 vXYZ released
>
> Dear all,
>
> Version XYZ of the DAISY Pipeline 2 project is now available for download. The following alternative packages are proposed:
>
> - for Windows users (exe file, XX MB):
>   https://github.com/daisy-consortium/pipeline-assembly/releases/download/vXYZ/pipeline2-XYZ_windows.exe
> - for Mac OS X users (dmg file, XX MB):
>   https://github.com/daisy-consortium/pipeline-assembly/releases/download/vXYZ/pipeline2-XYZ_mac.dmg
> - for Linux users  (zip file, XX MB):
>   https://github.com/daisy-consortium/pipeline-assembly/releases/download/vXYZ/pipeline2-XYZ_linux.zip
>
> All distributions include the new graphical user interface (GUI), all the conversion scripts, and a native command line tool (CLI).
>
> This release features notably:
> - **XXX** _insert highlights of the release_
>
> If you have any problems or identify some bugs, please report them to the Pipeline team using our issue tracker on GitHub:
> https://github.com/daisy/pipeline/issues
>
> For details, please refer to the full release notes:
> https://github.com/daisy/pipeline-assembly/releases/tag/vXYZ
>
> User documentation is available on the project’s web site:
> http://daisy.github.io/pipeline/
>
> As usual, feedback is warmly welcome!

pipeline-webui and other components that are not included in
pipeline-assembly are released separately.



[semver]: http://semver.org
