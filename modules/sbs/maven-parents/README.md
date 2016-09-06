# maven-parent

Mavent parent pom for SBS projects

## Build

```sh
mvn clean deploy
```

## Release

```sh
mvn release:clean release:prepare -DdryRun=true
mvn release:clean release:prepare release:perform
```

## Generate a dependency graph

Generate a graph of all the dependencies using the [Maven Graph Plugin][].

```sh
mvn -P graph graph:project
```

## Authors

- [Bert Frees][frees]
- [Christian Egli][egli]

## License

Copyright 2014 [Swiss Library for the Blind, Visually Impaired and Print Disabled][sbs]

Licensed under [GNU General Public License][] as published by the Free
Software Foundation, either version 3 of the License, or (at your
option) any later version.

[DAISY Pipeline 2]: https://github.com/daisy-consortium/pipeline-assembly/releases
[frees]: https://github.com/bertfrees
[egli]: https://github.com/egli
[sbs]: http://www.sbs.ch/
[GNU General Public License]: http://www.gnu.org/licenses/gpl.html
[Maven Graph Plugin]: http://mvnplugins.fusesource.org/maven/1.4/maven-graph-plugin/index.html
