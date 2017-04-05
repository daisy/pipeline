# Daisy Pipeline Eclipse Development Environment Setup Instructions


## Prerequisites


1. If you are using linux (Debian/Ubuntu), install the following packages, otherwise, install [Oracle JAVA 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 

 * sudo apt-get install openjdk-8-jdk (visit [Openjdk](http://openjdk.java.net/) for more information)
 * sudo apt-get install openjfx (visit [OPenJFX](https://wiki.openjdk.java.net/display/OpenJFX/Main) for more information)

2. Set JAVA_HOME and PATH variables

 * For Linux : "vim ~/.bashrc &" and add the following lines :

    export JAVA_HOME=/Path/To/Your/OPENJDK/Folder
    export PATH=$PATH:$JAVA_HOME/bin

 * For Windows : 

    Right click on "My Computer" and select "Properties" > "Advanced"
    Click on the Environment Variables button
    Under System Variables, click on New
    In the Variable Name field, enter "JAVA_HOME"
    In the Variable Value field, enter your JDK installation path
    Click on OK and Apply Changes as prompted
    Follow the Above step and edit the "Path" in "System Variables" 
    Add ";Path\To\Java\Folder\bin" in the value column

3. Install m2eclipse plugin

 * Select "Help" -> "Install New SoftWare..."
 * In "Work with: " enter the following website [http://download.eclipse.org/technology/m2e/releases](http://download.eclipse.org/technology/m2e/releases)
 * Install the software	
 * For more information, please visit [M2Eclipse](http://www.eclipse.org/m2e/) 

4. Install Egit plugin

 * Select "Help" -> "Install New SoftWare..."
 * In "Work with: " enter the following website [http://download.eclipse.org/egit/updates/](http://download.eclipse.org/egit/updates/)
 * Install the software
 * For more information, please visit [Egit](http://www.eclipse.org/egit/) 


## Work folder Setup


 * Run "git clone [https://github.com/daisy/pipeline.git](https://github.com/daisy/pipeline.git)"

Here is a table of correspondence between sub-project individual git repository and location in the pipeline super project

| Sub-project Git Repository                                                      | Folder                          |
|---------------------------------------------------------------------------------|---------------------------------|
| [pipeline-assembly](https://github.com/daisy/pipeline-assembly.git)             | pipeline/assembly/              |
| [pipeline-framework](https://github.com/daisy/pipeline-framework.git)           | pipeline/framework/             |
| [pipeline-modules-common](https://github.com/daisy/pipeline-modules-common.git) | pipeline/modules/common/        |
| [pipeline-scripts-utils](https://github.com/daisy/pipeline-scripts-utils.git)   | pipeline/modules/scripts-utils/ |
| [pipeline-build-utils](https://github.com/daisy/pipeline-build-utils.git)       | pipeline/utils/build-utils/     |
| [pipeline-scripts](https://github.com/daisy/pipeline-scripts.git)               | pipeline/modules/scripts/       |
| [pipeline-mod-braille](https://github.com/daisy/pipeline-mod-braille.git)       | pipeline/modules/braille/       |
| [pipeline-mod-nlp](https://github.com/daisy/pipeline-mod-nlp.git)               | pipeline/modules/nlp/           |
| [pipeline-mod-tts](https://github.com/daisy/pipeline-mod-tts.git)               | pipeline/modules/tts/           |
| [pipeline-mod-audio](https://github.com/daisy/pipeline-mod-audio.git)           | pipeline/modules/audio/         |
| [pipeline-cli-go](https://github.com/daisy/pipeline-cli.git)                    | pipeline/cli/                   |
| [pipeline-gui](https://github.com/daisy/pipeline-gui.git)                       | pipeline/gui/                   |
| [pipeline-updater](https://github.com/daisy/pipeline-updater.git)               | pipeline/updater/cli/           |
| [pipeline-updater-gui](https://github.com/daisy/pipeline-updater-gui.git)       | pipeline/updater/gui/           |


## Import Git Repository In Eclipse


 * Open Git persperctive "Window" -> "Perspective" -> "Open Pespective" -> "Other..." -> "Git"
 * Click on "Add an existing local Git Repository to this view"
 * Go to "pipeline" folder
 * Check "Look for nested repositories"
 * Select the repositories that you want to import in Egit


## Import Projects In Eclipse


 * Import each of the above projects in eclipse using File -> Import -> Maven -> Existing Maven Projects
 * In "Root Directory", go to the root of each of project and click on "Select All" button
 * Check "Add project(s) to working set"
 * Name the working set


## Compile And Launch Pipeline Command Line Mode


 * Open a terminal
 * Go to pipeline/pipeline-assembly/
 * mvn clean package -P dev-launcher
 * Launch "pipeline/pipeline-assembly/target/dev-launcher/bin/pipeline2"
 * Launch "pipeline/pipeline-assembly/target/cli/linux32/dp2 help" to check how to use dp2


## Debug Pipeline With Eclipse 


 * In Eclipse select "Run" -> "Debug Configurations" 
 * Double click on "Remote Java Application" 
 * Give a name for the debug configurations
 * Select the project that you want to debug
 * Select "Connection type" as "Standard (Socket Attach)"
 * In "Connection Properties", enter "localhost" for host and "5005" for port
 * Check "Allow termination of remote VM"
 * Click on "Apply"
 * Click on "Window" -> "Preferences"
 * Go to "General" -> "Network Connections"
 * In "Active Provider" select "Direct"
 * Click on "OK"
 * Restart Eclipse
 * Launch "pipeline/pipeline-assembly/tar * /dev-launcher/bin/pipeline2 debug"
 * Click on the debug button in eclipse

For more information, visit [Eclipse Remote Debug](https://www.ibm.com/developerworks/library/os-eclipse-javadebug/)


## Troubleshooting


### General Notes


1. Remove "Project build error: Unknown packaging: bundle" error

 * Edit pipeline/pipeline-build-utils/modules-parent/pom.xml
 * Find the section for artifact "maven-bundle-plugin" 
 * Add "<extensions>true</extensions>" after "<artifactId>"" tag


2. Remove "Duplicate bundle executions found. Please remove any explicitly defined bundle executions in your pom.xml" error

 * Resolve those problems after importing all the projects
 * Edit the pom file
 * Add a whitespace anywhere and then remove it
 * Save the pom.xml file


3. If some of modules are not taken into account while importing in eclipse, open the "pom.xml" file in each of folder mentioned in "Work folder Setup" and uncomment commented module in "<modules>" tag 


### Module specific errors


1. Pipeline-module-common

Once this project is imported, rename (right click on the project name and "Refactor" -> "Rename") "common-utils" to "modules-common-utils"


2. Pipeline-mod-braille

Once this project is imported, rename (right click on the project name and "Refactor" -> "Rename") 

 * "common-utils" to "brailles-common-utils" 
 * "css-utils" to "brailles-css-utils"


For "Plugin execution not covered by lifecycle configuration: org.daisy.pipeline.build:modules-build-helper:2.0.0:process-catalog (execution: process-catalog, phase: generate-resources)" error once this project is imported. Edit the pom.xml in "pipeline-mod-braille/parent" folder and add the following lines under the org.eclipse.m2e session

```xml
	<pluginExecution>
	  <pluginExecutionFilter>
	    <groupId>org.daisy.pipeline.build</groupId>
	    <artifactId>modules-build-helper</artifactId>
	    <versionRange>[2.0.0,)</versionRange>
	    <goals>
	      <goal>process-catalog</goal>
	    </goals>
	  </pluginExecutionFilter>
	  <action>
	    <execute>
	      <runOnIncremental>false</runOnIncremental>
	    </execute>
	  </action>
	</pluginExecution>
```


3. Pipeline-scripts-utils

For "Plugin execution not covered by lifecycle configuration: org.ops4j.pax.exam:maven-paxexam-plugin:1.2.4:generate-depends-file (execution: generate-depends-file, phase: generate-resources)" error once this project is imported. Edit the pom.xml in "pipeline-scripts-utils/parent" folder and add the following lines in "pluginManagement" HTML tag

```xml
	<plugin>
	  <groupId>org.eclipse.m2e</groupId>
	  <artifactId>lifecycle-mapping</artifactId>
	  <version>1.0.0</version>
	  <configuration>
	    <lifecycleMappingMetadata>
	      <pluginExecutions>
	        <pluginExecution>
	          <pluginExecutionFilter>
	            <groupId>org.ops4j.pax.exam</groupId>
	            <artifactId>maven-paxexam-plugin</artifactId>
	            <versionRange>[1.2.4,)</versionRange>
	            <goals>
	              <goal>generate-depends-file</goal>
	            </goals>
	          </pluginExecutionFilter>
	          <action>
	            <execute>
	              <runOnIncremental>false</runOnIncremental>
	            </execute>
	          </action>
	        </pluginExecution>
	      </pluginExecutions>
	    </lifecycleMappingMetadata>
	  </configuration>
	</plugin>
```


4. Pipeline-scripts

For "Plugin execution not covered by lifecycle configuration: org.ops4j.pax.exam:maven-paxexam-plugin:1.2.4:generate-depends-file (execution: generate-depends-file, phase: generate-resources)" error once this project is imported. Edit the pom.xml in "pipeline-scripts/parent" folder and add the following lines in "pluginManagement" HTML tag

```xml
	<plugin>
	  <groupId>org.eclipse.m2e</groupId>
	  <artifactId>lifecycle-mapping</artifactId>
	  <version>1.0.0</version>
	  <configuration>
	    <lifecycleMappingMetadata>
	      <pluginExecutions>
	        <pluginExecution>
	          <pluginExecutionFilter>
	            <groupId>org.ops4j.pax.exam</groupId>
	            <artifactId>maven-paxexam-plugin</artifactId>
	            <versionRange>[1.2.4,)</versionRange>
	            <goals>
	              <goal>generate-depends-file</goal>
	            </goals>
	          </pluginExecutionFilter>
	          <action>
	            <execute>
	              <runOnIncremental>false</runOnIncremental>
	            </execute>
	          </action>
	        </pluginExecution>
	      </pluginExecutions>
	    </lifecycleMappingMetadata>
	  </configuration>
	</plugin>
```

For "Plugin execution not covered by lifecycle configuration: org.daisy.pipeline.build:modules-build-helper:2.0.0:process-catalog (execution: process-catalog, phase: generate-resources)" error once this project is imported. Edit the pom.xml in "pipeline-scripts/parent" folder and add the following lines under the org.eclipse.m2e session

```xml
	<pluginExecution>
	  <pluginExecutionFilter>
	    <groupId>org.daisy.pipeline.build</groupId>
	    <artifactId>modules-build-helper</artifactId>
	    <versionRange>[2.0.0,)</versionRange>
	    <goals>
	      <goal>process-catalog</goal>
	    </goals>
	  </pluginExecutionFilter>
	  <action>
	    <execute>
	      <runOnIncremental>false</runOnIncremental>
	    </execute>
	  </action>
	</pluginExecution>
```

For "Plugin execution not covered by lifecycle configuration: org.apache.servicemix.tooling:depends-maven-plugin:1.2:generate-depends-file (execution: generate-depends-file, phase: generate-resources)" error once this project is imported. Edit the pom.xml in "pipeline-scripts/parent" folder and add the following lines under the org.eclipse.m2e session

```xml
	<pluginExecution>
	  <pluginExecutionFilter>
	    <groupId>org.apache.servicemix.tooling</groupId>
	    <artifactId>depends-maven-plugin</artifactId>
	    <versionRange>[1.2,)</versionRange>
	    <goals>
	      <goal>generate-depends-file</goal>
	    </goals>
	  </pluginExecutionFilter>
	  <action>
	    <execute>
	      <runOnIncremental>false</runOnIncremental>
	    </execute>
	  </action>
	</pluginExecution>
```

For "Plugin execution not covered by lifecycle configuration: org.codehaus.mojo:shell-maven-plugin:1.0-beta-1:shell (execution: generate-ott, phase: generate-resources)" error once this project is imported. Edit the pom.xml in "pipeline-scripts/parent" folder and add the following lines under the org.eclipse.m2e session

```xml
	<pluginExecution>
		<pluginExecutionFilter>
			<groupId>org.codehaus.mojo</groupId>
			<artifactId>shell-maven-plugin</artifactId>
			<versionRange>[1.0-beta-1,)</versionRange>
			<goals>
				<goal>shell</goal>
			</goals>
		</pluginExecutionFilter>
		<action>
			<ignore />
		</action>
	</pluginExecution>
```


5. Pipeline-mod-tts

For "Plugin execution not covered by lifecycle configuration: org.daisy.pipeline.build:modules-build-helper:2.0.0:process-catalog (execution: process-catalog, phase: generate-resources)" error once this project is imported. Edit the pom.xml in "pipeline-mod-tts/parent" folder and add the following lines in "pluginManagement" HTML tag

```xml
	<plugin>
	  <groupId>org.eclipse.m2e</groupId>
	  <artifactId>lifecycle-mapping</artifactId>
	  <version>1.0.0</version>
	  <configuration>
	    <lifecycleMappingMetadata>
	      <pluginExecutions>
	        <pluginExecution>
	          <pluginExecutionFilter>
	            <groupId>org.daisy.pipeline.build</groupId>
	            <artifactId>modules-build-helper</artifactId>
	            <versionRange>[2.0.0,)</versionRange>
	            <goals>
	              <goal>process-catalog</goal>
	            </goals>
	          </pluginExecutionFilter>
	          <action>
	            <execute>
	              <runOnIncremental>false</runOnIncremental>
	            </execute>
	          </action>
	        </pluginExecution>
	      </pluginExecutions>
	    </lifecycleMappingMetadata>
	  </configuration>
	</plugin>
```

