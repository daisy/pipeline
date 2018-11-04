Assembly for the DAISY Pipeline 2 distribution
==============================================

The default Pipeline 2 distribution is assembled with the [Maven Assembly Plugin](http://maven.apache.org/plugins/maven-assembly-plugin/). 

The library dependencies and list of Pipeline 2 modules are configured in the main `pom.xml` and copied in a set of goals of the `maven-dependency-plugin`.

The project allows to build the following distributions:

 - the "CLI" distribution (with the command line user interface), packaged as a ZIP (_default_)
 - the "macOS Application Bundle" distribution
 - the "Windows installer" distribution
 - the "Developers" distribution generates the configuration files and launcher scripts for a development environment
 - the "Debian" distribution (DEB package)
 - the "Red Hat" distribution (RPM package)
 - the "minimal" distribution, being a ZIP containing the updater and any files not downloaded by the updater
 
See the following sections for more details on how to enable these distributions.


CLI Distribution
----------------

Build the default distribution with:

	make

On Windows and macOS, the Windows installer and macOS application bundle distributions are respectively enabled by default.

macOS Application Bundle Distribution
--------------------------------------

To build a MacOS application bundle:

    make dmg

The `dmg` target will package the DAISY Pipeline as a macOS application bundle (`.app` package), and will put it inside a disk image (`.dmg` file).

The disk image is created with the [`node-appdmg` tool](https://github.com/LinusU/node-appdmg), run with a local version of Node installed by the Maven build in the `target` directory.

This distribution can only be built on a Mac.

Windows Installer Distribution
--------------------------------------

To build an installer for Windows:

    make exe

The `exe` target will package the DAISY Pipeline into an executable installer.

The installer is created with [NSIS](http://nsis.sourceforge.net/Main_Page). 

Developers Distribution
-----------------------

Build the development environment with:

    make dev-launcher

This distribution includes a launcher script for the Pipeline engine, as well as configuration files to include
modules installed in the user's default Maven repository (in `~/.m2/`).

Configuration files are generated for all the POM modules.
    
Debian Distribution
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

Red Hat/CentOS Distribution
---------------------------

Build an RPM package with:

    make rpm

Note: only proven to work on Red Hat/CentOS, although it should be
theoretically possible to build RPMs on other platforms including Mac
OS after installing rpmbuild.

To install the package:

    rpm -i target/rpm/pipeline2/RPMS/x86_64/*.rpm

"Minimal" Distribution
----------------------

This distribution contains the updater and any files not downloaded by the updater. To build it:

    make minimal-zip


Release Procedure
-----------------
See https://github.com/daisy/pipeline/wiki/Releasing#release-procedure.
