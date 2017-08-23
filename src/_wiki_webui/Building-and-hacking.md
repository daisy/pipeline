This page will help you set up a development environment and presents a development workflow.

### Prerequisites and dependencies

The only hard requirement is that you have Java 8 SDK installed.

You should also have a running instance of the Pipeline engine set up so that you can actually run some scripts.

The Web UI is built with the Play Framework. Necessary dependencies for this will be downloaded the first time you build the Web UI.

The `pipeline-clientlib-java` Java library is used for parsing and serializing the XML that is received from and sent to the engine. If you want to change the way the Web UI communicates with the engine, you may want to make the changes in that library instead of the Web UI.

There are two repositories you can use:
- [The Web UI repository](https://github.com/daisy/pipeline-webui)
- [The Pipeline 2 super project](https://github.com/daisy/pipeline)

The Pipeline 2 super project is easier to use if you want to start a development version of the engine for testing the Web UI, and/or change `pipeline-clientlib-java` or other relevant libraries at the same time as you make changes to the Web UI.

Note that if you use the super project, you will need to have `Make` installed.

### Building

- Make sure `$JAVA_HOME` points to Java 8.

#### From the Web UI repository

- For some reason the application is unable to initialize the database itself, therefore upon the first run copy a clean pre-initialized version of the database:

  ```sh
  rm -r dp2webui && cp -r dp2webui-cleandb dp2webui
  ```

- Build and run the application by bringing up a shell with `./activator` (or alternatively a graphical interface with `./activator ui`) and invoking `playRun`. Changes to the code are automatically picked up.

#### From the Pipeline 2 super project

To start the Web UI, simply run:

```
make run-webui
```

The Web UI will start on [http://localhost:9000/](http://localhost:9000/)

If you have set up your environment to build the engine, you can start the engine by simply running:

```
make run
```

This way of running the engine will run the "dev-launcher" version of the engine. The dev-launcher allows you to easily build a custom version of the engine, and changes to engine components are automatically and dynamically loaded.

### Workflow/tips for developing and debugging

- An Eclipse project can be generated with `./activator eclipse`.
- Compile and run the application as explained above.
- Compilation errors appear in the application itself as well as the activator shell (or ui).
- The most straightforward and effective way to test/debug the application is to test it manually in the browser.
- In addition, print dumps can be made with `console.log` (JavaScript) and `Logger.info` (Java).
- Watching the network traffic in the development tools of your browser can also be useful.
- Running the Web UI together with the engine dev-launcher can be useful not only for developing the Web UI but also for testing scripts while developing them.

#### Known issues/annoyances
- Derby (database) can cause difficulties during development, sometimes the Play process or the activator needs to be restarted.
