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
about [source code](Sources).)

* The version number of pipeline-assembly determines the main version
  of the Pipeline. Smaller changes are marked by updates of the patch
  segment, bigger changes are marker by updates of the minor segment.

* The version numbers of "bom", "parent" and "aggregator" POMs of
  framework and modules are aligned with the version of
  pipeline-assembly. Their major and minor segments should always
  match the latest version of pipeline-assembly, their patch segments
  may vary. The "bom" (bill of material) POMs are the places where the
  "current" versions of all bundles are listed. The boms are imported
  by the assembly, and every POM that has a dependency that is
  declared in a bom should import that bom. This way the version
  declarations are nicely centralized and reduced to a minimum.

* Version numbering of bundles should be done according to the
  versioning rules of OSGi a.k.a. [semantic versioning][semver]. Note
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

The things the script does not cover are listed in the following checklist:

- Fill the "v1.10.0" milestones with issues and resolve the open
  issues (see below).
- Finalize the release notes (NEWS files) of pipeline-mod-braille and
  pipeline-assembly (see below).
- Make sure the super repo is in sync with the master branches of all
  the sub repos. This can be verified with `git subrepo-status --fetch`.
- Create a release branch "release/v1.10.0" in the super project.
- Do the release with `make release` (see above).
- Edit the [distribution README file](https://github.com/daisy/pipeline-assembly/blob/master/src/main/resources/README.txt)
  in pipeline-assembly.
- Create pull requests from the release branches in the sub repos. Add
  them to the "v1.10.0" milestone.
- Merge the branches after having verified that the Travis checks
  pass.
- Close the milestones (should be 100% complete).
- Release the staged artifacts on the oss.sonatype.org website.
- Push the generated git tags (`git push --tags`).
- Edit the Github releases of
  [pipeline-mod-braille](https://github.com/daisy/pipeline-mod-braille/releases/tag/v1.10.0)
  and
  [pipeline-assembly](https://github.com/daisy/pipeline-assembly/releases/tag/v1.10.0). The
  pipeline-mod-braille release contains only a link to the NEWS file: 
  
  > [Release notes](https://github.com/daisy/pipeline-mod-braille/blob/master/NEWS.md#v1100)

  The pipeline-assembly release contains the full release notes from
  the NEWS file (see below).
- Build and upload the distribution packages to the release page of
  pipeline-assembly (if not a updater-only release).
  - To build the EXE: `make dist-exe`
  - To build the DMG: `make dist-dmg`
  - To build the ZIP: `make dist-zip-linux`
  - To build the DEB: `make dist-deb`
  - To build the RPM: `make dist-rpm` (must be built on a Red Hat machine)
- Publish the Github release.
- Publish a new version of the website after having updated
  `src/_data/versions.yml` and `src/_data/downloads.yml`.
- Send the email announcements (see below).
- Update the super repo with `git subrepo-rebase release/v1.10.0 master`.
- Delete the "release/v1.10.0" branch.

Ideally the release notes should already be made prior to making the
release, and milestones should have been set, however because life is
not perfect, it is best to search the Github issues (use the link at
[https://github.com/daisy/pipeline#issues](https://github.com/daisy/pipeline#issues))
for mistakes or things that were forgotten:

- Sort the issues by "Recently updated"
- Search for issues closed after a certain date (e.g. the date of the previous release) with `closed:>yyyy-mm-dd`.
- Search for issues without milestone with `no:milestone`
- Search for issues in the current milestone with `milestone:v1.10.0`
  (only works for the "pipeline", "pipeline-tasks",
  "pipeline-assembly", "pipeline-framework" and the various module
  repositories)

Finalize the release notes by adding a link to the
pipeline-mod-braille release notes as well as a list of links to
closed issues (skip the empty ones):

```md
### Details
- [Closed issues in pipeline-tasks](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-tasks+milestone%3Av1.10.0)
- [Closed issues in pipeline](https://github.com/issues?q=repo%3Adaisy%2Fpipeline+milestone%3Av1.10.0)
- [Closed issues in pipeline-assembly](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-assembly+milestone%3Av1.10.0)
- [Closed issues in pipeline-cli-go](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-cli-go+milestone%3Av1.10.0)
- [Closed issues in pipeline-framework](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-framework+milestone%3Av1.10.0)
- [Closed issues in pipeline-gui](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-gui+milestone%3Av1.10.0)
- [Closed issues in pipeline-mod-audio](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-audio+milestone%3Av1.10.0)
- [Closed issues in pipeline-mod-braille](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-braille+milestone%3Av1.10.0)
- [Closed issues in pipeline-mod-nlp](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-nlp+milestone%3Av1.10.0)
- [Closed issues in pipeline-mod-tts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-mod-tts+milestone%3Av1.10.0)
- [Closed issues in pipeline-modules-common](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-modules-common+milestone%3Av1.10.0)
- [Closed issues in pipeline-scripts](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts+milestone%3Av1.10.0)
- [Closed issues in pipeline-scripts-utils](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-scripts-utils+milestone%3Av1.10.0)
- [Closed issues in pipeline-updater](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-updater+milestone%3Av1.10.0)
- [Closed issues in pipeline-updater-gui](https://github.com/issues?q=repo%3Adaisy%2Fpipeline-updater-gui+milestone%3Av1.10.0)
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
