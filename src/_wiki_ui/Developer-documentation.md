# Developer documentation

## Prerequisites

* [Node](https://nodejs.org/)

## Steps to run the code

* Check out this repository
* Run `yarn` to install dependencies
* Run `yarn dev` to run the app in development mode

## Steps to build and sign the code

### Mac

* Configure your machine for code signing and notarizing
  * Get credentials (e.g. signing certificate)
  * Create a file called `.env` in the root with this information:
```
APPLE_ID=*******
APPLE_ID_PASS=*********
APPLE_ID_TEAM=********
```
* Build the distributable
  * `yarn dist --mac`
  * This creates a `dmg` file, signs the code, and notarizes the app.

### Windows

Build the release version of the application with the following command line: `yarn dist --win`.

The command should produce:

* An installer executable (ending by Setup <version>.exe)
* A portable version of the app provided as an executable,
* A zipped version of the application,
* An win-unpacked folder with the release version of the application.

As this project relies on electron-builder, it should be possible for anyone wanting to sign the executable to follow the [electron-builder documentation regarding code signing](https://www.electron.build/code-signing.html).

Note that this process has not been investigated yet by the development team, as a separate signing process is currently used after the project release build process.

# Structure & concepts

There is a link in this repository to the Pipeline assembly subrepo, referencing the version of the Pipeline engine that is used in this app. Theoretically, it should get built along with everything else, but if you are having issues, try to build the engine by itself, e.g. first run `make src/resources/daisy-pipeline` from the project root, before running `yarn dev`. 

This project was created from [this electron template](https://github.com/daltonmenezes/electron-app).

In the main process, it starts the Pipeline engine upon app startup, and interacts with it via HTTP calls to the Pipeline's web service. 

There is one main window, consisting of a tabbed job management interface, one settings window, and an about window.

There is also a system tray icon and menu, useful for keeping the engine running (it takes a few seconds to start so this can save time).

The Pipeline engine is instructed to use an APPDATA directory for its data directory, and then the job results are copied by the Pipeline UI to a directory that the user sets in Settings.

# Other notes

## About file inputs

We can't use HTML `<input type="file" ...>` because even with the folder option enabled by `"webkitdirectory"`, it won't let users select an empty folder.

We also can't repurpose `<input type="file" ...>` as a control to trigger electron's native file picker because you can't set the value on the input field programmatically, which would be required.

So we have a custom control called "FileOrFolder" which consists of a button to bring up a file/folder picker dialog, and a text field to optionally use to type in the path. 

## About tooling

This project uses React and Redux. The data store is maintained on the backend and synchronized on the front end.