# Installation

Start by downloading DAISY Pipeline from the
[downloads](http://daisy.github.io/pipeline/Download.html) page. You
can choose from different packages. DAISY Pipeline consists of several
components (explained [here](User-Guide)). Check whether the package
you are downloading includes the component you wish to install. Also,
there are different downloads for different platforms.

## Packages

These are the installation instructions for each package. Before
starting with the installation make sure the
[system requirements](#system-requirements) are fulfilled.

### Windows

For Windows users there is the Windows installer. It installs the
desktop application. Launch the installer and follow the instructions
on the screen. The desktop application can also be used to run a
server.

### Mac OS

For Mac OS users there is the disk image. It contains the desktop
application. To install, open the image and drag the application file
to the "Applications" folder, or any other destination you want.

<img src="Get-Help/User-Guide/Installation/disk-image.png" alt="Contents of the disc image" width="600px"/>

The desktop application can also be used to run a server.

### Debian

Users of Debian or Debian-based distributions such as Ubuntu can
install DAISY Pipeline via the [ZIP file](#linux-zip), but easier is
to use the Debian package manager. Open a shell window, change to the
directory where you have downloaded the DEB file, then execute the
following command:

~~~sh
dpkg -i daisy-pipeline2_x.y.z.deb
~~~

The Debian package includes the server and the command line tool, not
the desktop application.

### Red Hat

Users of Red Hat or other RPM-based distributions can install DAISY
Pipeline via the [ZIP file](#linux-zip), but easier is to use the "YUM"
package manager. Open a shell window, change to the directory where
you have downloaded the RPM file, then execute the following command:

~~~sh
yum install daisy-pipeline2_x.y.z.rpm
~~~

The RPM package includes the server and the command line tool, not the
desktop application.

### Linux (ZIP)

The ZIP package for Linux can be installed on all distros. It includes
the server and the command line tool. To install, extract the contents
to any destination you want.

### Docker

The Docker distribution is not available as a download on the
website. It comes in the form of a Docker image that you can obtain
via the Docker command line interface:

~~~sh
docker pull daisyorg/pipeline
~~~

It is also possible to get a specific version:

~~~sh
docker pull daisyorg/pipeline:<version>
~~~

or to get the latest development version:

~~~sh
docker pull daisyorg/pipeline:latest-snapshot
~~~

You can find the available versions at
[https://hub.docker.com/r/daisyorg/pipeline/tags](https://hub.docker.com/r/daisyorg/pipeline/tags). After
having pulled the image you're ready to run the Pipeline web server:

~~~sh
docker run --detach \
           -p 8181:8181 \
           -e PIPELINE2_WS_HOST=0.0.0.0 \
           daisyorg/pipeline
~~~

This will make the web service available on the address
http://localhost:8181/ws. By default, authentication is enabled. The
key and secret are "clientid" and
"sekret". [Environment variables]({{site.baseurl}}/Get-Help/User-Guide/Pipeline-as-Service/#environment-variables)
can be provided with one or more `-e` arguments.

The Pipeline web application is available as a Docker image too. You
can find the available versions at
[https://hub.docker.com/r/daisyorg/pipeline-webui/tags](https://hub.docker.com/r/daisyorg/pipeline-webui/tags).

~~~sh
docker pull daisyorg/pipeline-webui
~~~

The application is launched as follows. This assumes the web server is
already started and accessible on port 8181.

~~~sh
docker run --detach \
           -p 9000:9000 \
           daisyorg/pipeline-webui
~~~

It is also possible to link the two containers without exposing the
web service to the host:

~~~sh
docker run --detach \
           --name pipeline \
           -e PIPELINE2_WS_HOST=0.0.0.0 \
           -e PIPELINE2_WS_AUTHENTICATION=false \
           daisyorg/pipeline
docker run --detach \
           --link pipeline \
           -p 9000:9000 \
           -e DAISY_PIPELINE2_URL=http://pipeline:8181/ws \
           daisyorg/pipeline-webui
~~~

For running more complex configurations like these,
[Docker compose](https://docs.docker.com/compose/) is recommended. A
complete example, that also persists the application data, is given
below. Simply create a file called "docker-compose.yml" with the
following content and run `docker-compose up`.

~~~yaml
version: "2.1"
services:
  pipeline:
    image: daisyorg/pipeline
    environment:
      PIPELINE2_WS_HOST: "0.0.0.0"
      PIPELINE2_WS_AUTHENTICATION: "false"
    volumes:
      - "pipeline-data:/opt/daisy-pipeline2/data"
  webui:
    image: daisyorg/pipeline-webui
    environment:
      DAISY_PIPELINE2_URL: http://pipeline:8181/ws
    ports:
      - "9000:9000"
    volumes:
      - "webui-data:/opt/daisy-pipeline2-webui/data"
    depends_on:
      pipeline:
        condition: service_healthy
volumes:
  pipeline-data:
  webui-data:
~~~

To learn more about Docker see [https://docs.docker.com](https://docs.docker.com).

## System Requirements

### Java

The standalone server requires a Java runtime environment. The minimum
required version of Java is 11. We recommend installing Java from
https://adoptium.net/. Users of the desktop application (Windows and
MacOS) do not have to worry about Java because it is included in the
application.
