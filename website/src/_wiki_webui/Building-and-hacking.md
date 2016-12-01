This page will help you set up a development environment and presents a development workflow.

### Prerequisites and dependencies

- Java 8
- Play framework, automatically downloaded and installed with `./activator`
- Ivy, also installed automatically
- All code related to communicating with the Pipeline engine through the web API lives in [pipeline-clientlib-java][]. The clientlib-java submodule is for parsing and serializing the XML that is received from and sent to the engine. The clientlib-java-http submodule is for the HTTP communication and uses clientlib-java.
- In order to test the complete functionality of the UI, a running instance of the Pipeline engine is required.

### Building

- For the latest up to date version, check out the branch "big-update".
- Make sure `$JAVA_HOME` points to Java 8.
- For some reason the application is unable to initialize the database itself, therefore upon the first run copy a clean pre-initialized version of the database:

  ```sh
  rm -r dp2webui && cp -r dp2webui-cleandb dp2webui
  ```
- Build and run the application by bringing up a shell with `./activator` (or alternatively a graphical interface with `./activator ui`) and invoking `playRun`. Changes to the code are automatically picked up.

### Workflow/tips for developing and debugging

- An Eclipse project can be generated with `./activator eclipse`.
- Compile and run the application as explained above.
- Compilation errors appear in the application itself as well as the activator shell (or ui).
- The most straightforward and effective way to test/debug the application is to test it manually in the browser.
- In addition, print dumps can be made with `console.log` (JavaScript) and `Logger.info` (Java).
- Watching the network traffic in the development tools of your browser can also be useful.
- Your best option for getting an instance of the Pipeline engine up and running is through the "dev-launcher" build of pipeline-assembly. This method allows you to easily build a custom version of the engine, and changes to engine components are automatically and dynamically loaded. For more details see https://github.com/snaekobbi/wiki/wiki/Building#pipeline-assembly.
- Whenever you want to update to a new (development) version of [pipeline-clientlib-java][], ???

#### Known issues/annoyances
- Derby (database) can cause difficulties during development, sometimes the Play process or the activator needs to be restarted.


[pipeline-clientlib-java]: https://github.com/daisy/pipeline-clientlib-java
