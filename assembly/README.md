Assembly for the DAISY Pipeline 2 distribution
==============================================

This project provides two things:

1. A build script to package a DAISY Pipeline 2 distribution.
2. A Maven POM file for using DAISY Pipeline 2 as a library.

The project allows to build the following distributions:

- a ZIP for Linux/maxOS/Windows
- a "minimal" ZIP distribution that will complete itself upon first update
- a Debian package
- a Red Hat package
- a Docker image

Run `make help` to get the full list of commands.

ZIP Distribution
----------------

The following options are available to customize what is included in the ZIP:

- `--without-jre`
- `--with-jre32`
- `--without-osgi`
- `--without-persistence`
- `--without-webservice`
- `--without-cli`
- `--without-updater`
- `--with-simple-api`

"Minimal" distribution
----------------------

This distribution contains the updater and any files not downloaded by the updater. To build it:

    make minimal-zip

Debian distribution
-------------------

Build a Debian package with:

    make deb

To inspect package contents and metadata:

    dpkg-deb -c target/*.deb
    dpkg-deb -f target/*.deb

To install the package:

    dpkg -i target/*.deb

To uninstall:

    dpkg -r daisy-pipeline

Red Hat/CentOS distribution
---------------------------

Build an RPM package with:

    make rpm

Note: only proven to work on Red Hat/CentOS, although it should be
theoretically possible to build RPMs on other platforms including Mac
OS after installing rpmbuild.

To install the package:

    rpm -i target/rpm/pipeline2/RPMS/x86_64/*.rpm
