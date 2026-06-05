# Daisy Pipeline 2 TTS Adapter - Microsoft SAPI and OneCore voices

This adapter provides Text-to-speech capabilities to the DAISY pipeline 2 using Microsoft SAPI and (under development) OneCore voices on Windows hosts.

## Building the adapter

This adapter is made of Windows DLLs that use the Java Native Interface to be exposed to the pipeline, and a java-side connector between those DLLs and the pipeline framework.

Stable release versions of the DLLs are provided with the adapter, so the project can be built "as-is" using maven with the command `maven clean install`.

If you want or need to update the DLLs, you can rebuild the DLLs before building the adapter by following the instructions in the next section.

## Rebuilding the "Sapinative" dlls

To build the adapter's DLLs, we recommend installing [Visual Studio Community](https://visualstudio.microsoft.com/fr/vs/community/), with the following tools and functionalities also installed:
- Development tools for C++/Desktop applications, 
- Windows 10 SDK.
The project DLLs are currently built using Visual Studio Community 2022, version 17.3.3.
If you already have Visual Studio Community or Enterprise edition installed, please check that those functionalities are already part of your installation, by going under the `Tools/Get tools and functionalities` section of the application menu.

If you don't want to use visual studio and want to build from the command line, you will need:
- the [MSBuild toolset](https://visualstudio.microsoft.com/fr/downloads/?q=build+tools)
  - When requested, select the "desktop development with C++" workload
- the [Windows 10 SDK (tested with 10.0.19041)](https://go.microsoft.com/fwlink/?linkid=2120843)
More information are provided by Microsoft in [their documentation](https://docs.microsoft.com/en-us/cpp/build/building-on-the-command-line?view=msvc-170) to build projects and solutions from the command line.

The following environment variables also need to be set for the code to build
- JAVA_HOME needs to point to a [java 11 JDK](https://adoptium.net/?variant=openjdk11&jvmVariant=hotspot)
- If you installed MSBuild, add its Bin folder to your PATH environment variable

### With Visual Studio Community

Using Visual Studio Community, the solution should be ready for build.
In Visual Studio, select the "Release" configuration and then build the DLLs for "x86" and "x64" platforms by respectively:
- Select the "x86" platform and launch the build with the menu action "Build/Build the solution"
- Select the "x64" platform and launch the build with the menu action "Build/Build the solution"

If the build was successful, the DLLs under `src/main/resources/(x86|x64)/` will be updated.

Alternatively, you can also open the developer command line with the menu `Tools/Command line/Developer command line` and launch the following commands: 
```
msbuild sapinative.sln -p:Configuration=Release -p:Platform="x86"
msbuild sapinative.sln -p:Configuration=Release -p:Platform="x64"
```

### With MSBuild (with or without Visual Studio)

Build with MSBuild separate installation has not yet been fully tested, so your mileage may vary.

With the `msbuild.exe` parent directory registered in your PATH environment variable, open a command line interpreter and position it at the root of the repository.
Then launch the following commands to update the DLLs:
```
msbuild.exe sapinative.sln -p:Configuration=Release -p:Platform="x86"
msbuild.exe sapinative.sln -p:Configuration=Release -p:Platform="x64"
```

### Activating the DLL unit tests

By default, the DLL unit tests are deactivated to allow the build to proceed on non-windows build servers.
If you want to reactivate the tests, open the test/java/ignore file and uncomment the first line 

## Possible issues or warnings

### Exception raised after unit tests are passed

On some occasions when building the maven project with SAPINative tests activated, you might encounter a jni exception that blocks the unit tests.
The circumstances that make the unit tests raise this exception when unloading the DLLs have not yet been identified, as the exception is not raised consistently in build attempts.
To our best knowledge, this issue has not yet impacted any production scenario as it might occur when the pipeline is exiting and disposing of the "Sapinative" bridge library.