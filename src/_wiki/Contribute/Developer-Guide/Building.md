# Building

DAISY Pipeline's build system makes use of a variety of build tools:
[Maven][], [Gradle][], [Makefiles][Make], [Autotools][], etc. This is
a concequence of using different programming languages and third-party
software. The build system tries to hide as much as possible the
complexity this brings.

The system of connecting the individual components is (almost)
entirely Maven based. This means that upon building a certain
component, the artifact is installed in a Maven repository (which can
be online or offline) to other components that want to make use of
it. Dependencies are resolved and downloaded automatically and do not
have to be copied explicitely.

Gradle is Maven compatible. Some components such as
[Liblouis](https://github.com/liblouis/liblouis) are built with
Autotools which is not inherently Maven compatible, but the binary
artifacts are made part of the Maven system by packaging them into a
special kind of JAR file (see
[liblouis-nar](https://github.com/liblouis/liblouis-nar).

[The "super-build" project](#super-build) attempts to further simplify
things by wrapping the entire code base in a single Make based build
script that allows you to build everything with one simple
command. The script also supports building incrementally which is an
indispensable feature for a project this size.

**Important**: the super-build script is intended to be run on MacOS
or Linux. In theory it could also be run with Cygwin although some
issues are definitely expected there. Contact us if you have ideas for
making the script cross-platform.

For typical modifications coming from external contributors, it is
enough to recompile only a small part of Pipeline, i.e. one or two
components, so the super-build script is not really required in these
cases.


## Prerequisites

The following tools are required to build the Pipeline. Some tools are
only needed for some components.

- Java >= 8
- Maven >= 3.0.0
- Go (for cli and updater)
- GNU Make (for super-project, Liblouis, ...)
- Ruby and [Bundler](https://bundler.io/) (for website)


## Building individual components

Individual components may be built by checking out the git repository
in question and following the build instructions specific to that
repository, which can normally be found in a README file.

In case a component has dependencies from other repositories, those
dependencies must have been built beforehand. The Maven dependency
resolution system takes care of the rest. There are two kinds of Maven
dependencies: released artifacts and snapshots. Released artifacts are
built only once after which they are made available online. Snapshots
on the other hand are development versions which can be built more
than once with the same version number. This makes snapshot
dependencies unstable. Snapshots are built automatically for all
Pipeline components and are available online. These "official"
snapshot always correspond to the "master" branch of their respective
git repositories.

You can use "official" snapshots by adding the following repositories
in the "profiles" element of `~/.m2/settings.xml` or
`/etc/maven/settings.xml`:

```xml
<profile>
    <id>sonatype-snapshots</id>
    <activation>
        <activeByDefault>true</activeByDefault>
    </activation>
    <repositories>
        <repository>
            <id>sonatype-snapshots</id>
            <name>OSS Sonatype repo (snapshots)</name>
            <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
                <checksumPolicy>warn</checksumPolicy>
            </snapshots>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>sonatype-snapshots</id>
            <name>OSS Sonatype repo (snapshots)</name>
            <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
                <checksumPolicy>warn</checksumPolicy>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</profile>
```

As a result, a dependency only needs to be built locally when local
changes have been made to the source code, or if the required version
does not match what is currently on the master branch.

To get an overview of all the different Pipeline repositories and what
they contain, refer to the page about the [source code](Sources).

The order in which individual components need to be built might not
always be immediately clear. This is one of the reasons for having the
[super-build](#super-build).


## Super-build

The super-build makes abstraction of dependencies and allows you to
build the whole Pipeline project as if it were in one piece. The whole
Pipeline code base is aggregated in the so-called
[super-project](Sources#aggregator-project). It also includes some
(forks of) third-party libraries that require changes frequently. A
Makefile contains logic to compute interdependencies and build and
connect the different components.

With a simple command you can build and package the main application
into a DMG, a EXE, a ZIP (for Linux), a DEB and a RPM:

```
make dist
```

There are also commands to run all tests, to build and run the
application, to build the website, to create an Eclipse project,
etc. To get a list of all available commands run `make help`.

Note that all individual snapshot artifacts are internal to the
super-build, meaning you can not expect to be able to use a snapshot
dependency that was built as part of the super-build when building an
individual component, or the other way around. However there is a
trick to enable this possibility anyway: after running `eval $(make
dump-maven-cmd)` in the super-project, `mvn` will be a shell alias
that behaves like you expect.


[Maven]: https://en.wikipedia.org/wiki/Apache_Maven
[Gradle]: https://en.wikipedia.org/wiki/Gradle
[Make]: https://en.wikipedia.org/wiki/Make_(software)
[Autotools]: https://en.wikipedia.org/wiki/GNU_build_system
