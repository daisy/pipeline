---
layout: default
---
# Installation

Start by downloading DAISY Pipeline from the
[downloads]({{site.baseurl}}/Download.html) page. You can choose from
different packages. DAISY Pipeline consists of several components
(explained [here]({{site.baseurl}}/Get-Help/User-Guide/)). Check
whether the package you are downloading includes the component you
wish to install. Also, there are different downloads for different
platforms.

## Packages

These are the installation instructions for each package. Before
starting with the installation make sure the
[system requirements](#system-requirements) are fulfilled.

### Windows

For Windows users there is the Windows installer. It automatically
installs the desktop application, the server and the command line
tool. Simply launch the installer and follow the instructions on the
screen.

### Mac OS

For Mac OS users there is the disk image. It contains the desktop
application. To install, open the image and drag the application file
to the "Applications" folder, or any other destination you want.

<img src="/pipeline/Get-Help/User-Guide/Installation/disk-image.png" alt="Contents of the disc image" width="600px"/>

### Debian

Users of Debian or Debian-based distributions such as Ubuntu can
install DAISY Pipeline via the [ZIP file](#linux-zip), but easier is
to use the Debian package manager. Open a shell window, change to the
directory where you have downloaded the DEB file, then execute the
following command:

~~~sh
dpkg -i daisy-pipeline2_x.y.z.deb
~~~

The Debian package includes the desktop application, the server and
the command line tool.

### Red Hat

Users of Red Hat or other RPM-based distributions can install DAISY
Pipeline via the [ZIP file](#linux-zip), but easier is to use the "YUM"
package manager. Open a shell window, change to the directory where
you have downloaded the RPM file, then execute the following command:

~~~sh
yum install daisy-pipeline2_x.y.z.rpm
~~~

The RPM package includes the desktop application, the server and the
command line tool.

### Linux (ZIP)

The ZIP can be installed on all Linux distros. It includes the desktop
application, the server and the command line tool. To install, simply
extract the contents to any destination you want.


## Updater

Some packages also include an updater tool that you can use for quickly
updating your current installation to the latest version. The updater
can be invoked either via the desktop application or via the command
line. The desktop application has a menu item "Check updates" under
"Help". On the command line it is different for each platform.

<!-- Windows: to do -->

### Mac OS

~~~sh
/Applications/DAISY\ Pipeline\ 2.app/Contents/Java/updater/pipeline-updater \
    -service="http://daisy.github.io/pipeline-assembly/releases/" \
    -install-dir="/Applications/DAISY Pipeline 2.app/Contents/Java/" \
    -descriptor="/Applications/DAISY Pipeline 2.app/Contents/Java/etc/releaseDescriptor.xml" \
    -version=current
~~~

In the above command replace "/Applications" with whichever directory
you have installed the application file in.

<!-- Debian and Red Hat: updater not available -->

### Linux (ZIP)

~~~sh
/home/bert/pipeline2/updater/pipeline-updater \
    -service="http://daisy.github.io/pipeline-assembly/releases/" \
    -install-dir=/home/bert/pipeline2/ \
    -descriptor=/home/bert/pipeline2/etc/releaseDescriptor.xml \
    -version=current
~~~

In the above command replace "/home/bert/pipeline2" with whichever directory
you have unpacked the ZIP file to.


## System Requirements

### Java

The server and the desktop application require a Java runtime
environment. Windows and Mac users do not have to worry about Java
because it is included in the DAISY Pipeline installation. Linux users
however are on their own. The minimum required version of Java is 8.

### JavaFX

<!-- (linked to from pipeline2 start script if no JavaFX is detected) -->

The
[desktop application]({{site.baseurl}}/Get-Help/User-Guide/Desktop-Application/DAISY-Pipeline-2-User-Guide/)
requires [JavaFX][]. If you are on Windows or Mac, a version of Java
that includes JavaFX, namely Oracle's Java SE implementation, is
automatically installed for you. If you are on another platform and
can not use Oracle's Java, have a look at the following alternatives
for acquiring JavaFX:

- for Debian/Ubuntu:
  [https://packages.qa.debian.org/o/openjfx.html](https://packages.qa.debian.org/o/openjfx.html)
- for modile and embedded:
  [http://gluonhq.com/labs/javafxports/](http://gluonhq.com/labs/javafxports/)
- JavaFX can be built from source:
  [https://wiki.openjdk.java.net/display/OpenJFX/Main](https://wiki.openjdk.java.net/display/OpenJFX/Main)


[JavaFX]: https://docs.oracle.com/javafx/2/overview/jfxpub-overview.htm
