# Daisy Pipeline 2 TTS Adapter - Microsoft SAPI and OneCore voices

This adapter provides Text-to-speech capabilities to the DAISY pipeline 2 using Microsoft SAPI and (under developement) OneCore voices on Windows hosts.

## Building the adapter

This adapter is made of Windows DLLs that use the Java Native Interface to be exposed to the pipeline, and a java-side connector between those DLLs and the pipeline framework.

Stable release versions of the dlls are provided with the adapter, so the project can be build "as-is" using maven with the command `maven clean install`.

If you want or need to update the dlls, you can rebuild the dlls before building the adapter following the instructions in the next section.

## Rebuilding the "sapinative" dlls

To build the adapter's dlls, we recommend to install [Visual studio 2019 or newer](https://visualstudio.microsoft.com/fr/vs/community/) (VS2019 community is currently used), with development tools for C++/Desktop application, including windows 10 SDK
(you might need to check if you have those option installed under the menu "Tools/Get tools and functionnalities")

Or if you don't want to use visual studio (untested) and want to build from the command line, you will need:
- the [MSBuild toolset](https://visualstudio.microsoft.com/fr/downloads/?q=build+tools)
  - When requested, select the "desktop development with C++" workload
- the [Windows 10 SDK (tested with 10.0.19041)](https://go.microsoft.com/fwlink/?linkid=2120843)
More informations are provided by Microsoft in [their documentation](https://docs.microsoft.com/en-us/cpp/build/building-on-the-command-line?view=msvc-170) for buliding such project from the command line.

The following environment variable also need to be set for the code to build
- JAVA_HOME needs to point to a [java 11 JDK](https://adoptium.net/?variant=openjdk11&jvmVariant=hotspot)
- If you installed MSBuild, add its Bin folder to your PATH environment variable

### With visual studio

Using visual studio 2019, the projects and solution should be ready for build.
In Visual Studio, first select the "Release" configuration and then build the DLLs for x86 and x64 platform:
- Select the "x86" platform and launch the build with the menu action "Build/Build the solution"
- Select the "x64" platform and launch the build with the menu action "Build/Build the solution"

If the build was successful, this should have updated the DLLs under `src/main/resources/(x86|x64)/sapinative.dll`

Alternatively, you can also open the developer command line with the menu "Tools/Command line/Developer command line" and launch the following commands: 
```
msbuild sapinative.sln -p:Configuration=Release -p:Platform="x86"
msbuild sapinative.sln -p:Configuration=Release -p:Platform="x64"
```

### With MSBuild (with or without visual studio)

Build with MSBuild separate installation has not yet be tested, so your mileage may vary.

From a command line interpreter positionned at the root of the repository, you should be able to launch the following commands to update the dlls (with MSBuild accessible from your PATH):

```
msbuild.exe sapinative.sln -p:Configuration=Release -p:Platform="x86"
msbuild.exe sapinative.sln -p:Configuration=Release -p:Platform="x64"
```

### Activating the dll unit tests

By default, the dll unit tests are deactivated to allow the build to proceed of non-windows build servers.
If you want to reactivate the tests, open the test/java/ignore file and uncomment the first line 

## Possible issues or warnings

### Exception raised after unit tests are passed

On some occasion when building the maven project with SAPINative tests activated, you might encountered an jni exception that does block the unit tests.
The circumstances that make the unit tests raising this exception when unloading the DLL has not yet been identified, the exception not being raised consistently in builds attempts.
From our best knowledge, this issue has not yet impacted any production scenario as it might occure when the pipeline is exiting and disposing the sapinative bridge library.