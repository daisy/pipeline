[pipeline-mod-braille][]
========================

[![Build Status](https://travis-ci.org/daisy/pipeline-mod-braille.png?branch=master)](https://travis-ci.org/daisy/pipeline-mod-braille)

Braille Production Modules for the [DAISY Pipeline 2][pipeline].

Project layout
--------------
Because of the very modular nature of DAISY Pipeline 2, browsing the
code is not always easy. In order to make it more obvious where to
find a particular piece of code, I've tried to organize the modules
into subdirectories in a logical and consistent way.

- [`pipeline-braille-scripts`](pipeline-braille-scripts) contains the
  two top-level *scripts* that are presented to the end-user, notably
  `zedai-to-pef` and `dtbook-to-pef`.

- [`pipeline-braille-utils`](pipeline-braille-utils) contains everything
  else: all the low-level building blocks that the scripts are made up
  from. The building blocks are divided into logical *groups* such as
  `css-utils`, `pef-utils`, `liblouis-utils`, etc.

Building
--------
Build and run the unit tests with:

```sh
mvn clean install
```

The required version of Java is 8.

Semantic versioning
-------------------
Individual modules are versioned according to [SemVer](http://semver.org/). In order to ease the
release process, the correct version must be set whenever a change is made to a module (in both
`maven/bom/pom.xml` and module's own POM).

Release procedure
-----------------
- Version number should match next version of pipeline-assembly.

  ```sh
  VERSION=1.9.4
  ```

- Create a release branch.

  ```sh
  git checkout -b release/${VERSION}
  ```
  
- Resolve snapshot dependencies and commit.
- Semantic versioning should have been done already (see above). However because some modules might
  have been disabled in the aggregator POMs because they haven't changed since the previous release,
  but might have to be updated anyway because of major version increments of depending modules
  (depending through "Import-Package"), that should be double-checked now. For each of the 4 script
  modules `dtbook-to-pef`, `html-to-pef`, `epub2-to-pef` and `zedai-to-pef`, if the module is
  disabled in the aggregator POMs, run `mvn clean test`.
- Generate release notes template, edit and commit. (View changes since previous release with `git diff v1.9.3...HEAD`, and look for relevant Github issues on [https://github.com/search](https://github.com/search?o=desc&q=involves%3Abertfrees+repo%3Adaisy%2Fpipeline-mod-braille+repo%3Asnaekobbi%2Fpipeline-mod-braille+repo%3Asnaekobbi%2Fissues+repo%3Asnaekobbi%2Fliblouis+repo%3Aliblouis%2Fliblouis+repo%3Asnaekobbi%2Fbraille-css+repo%3Asnaekobbi%2FjStyleParser+repo%3Ajoeha480%2Fdotify&s=updated&type=Issues))

  ```sh
  make release-notes
  ```

- Perform the release with Maven.

  ```sh
  mvn clean release:clean release:prepare
  mvn release:perform
  ```
  
- Revert snapshot increments of modules in `maven/bom/pom.xml` and `maven/parent/pom.xml`, update
  parent version to new snapshot in all module POMs, comment out all modules in all aggregator POMs
  and amend to the last commit.
- Push and make a pull request (for turning an existing issue into a PR use the `-i <issueno>` switch).

  ```sh
  git push origin release/${VERSION}:release/${VERSION}
  hub pull-request -b daisy:master -h daisy:release/${VERSION} -m "Release version ${VERSION}"
  ```
  
- Stage the artifact on https://oss.sonatype.org and comment on pull request.

  ```sh
  ghi comment -m staged ${ISSUE_NO}
  ```
  
- Test and stage all projects that depend on this release before continuing.
- Release the artifact on https://oss.sonatype.org and close pull request.

  ```sh
  ghi comment --close -m released ${ISSUE_NO}
  ```
  
- Push the tag.

  ```sh
  git push origin v${VERSION}
  ```

- Add a link to the release notes to http://github.com/daisy/pipeline-mod-braille/releases/v${VERSION}.

See also
--------
 - [ZedAI to PEF script user guide](http://code.google.com/p/daisy-pipeline/wiki/ZedAIToPEFDoc)

Authors
-------
- [Bert Frees][bert]

License
-------
Copyright 2012-2014 [DAISY Consortium][daisy] 

This program is free software: you can redistribute it and/or modify
it under the terms of the [GNU Lesser General Public License][lgpl]
as published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Lesser General Public License for more details.


[pipeline-mod-braille]: https://github.com/daisy/pipeline-mod-braille
[pipeline]: http://code.google.com/p/daisy-pipeline
[bert]: http://github.com/bertfrees
[daisy]: http://www.daisy.org
[lgpl]: http://www.gnu.org/licenses/lgpl.html
