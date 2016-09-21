              DAISY Pipeline 2 - 1.10.0-beta1 - September 19, 2016
==============================================================================


 1. What is the DAISY Pipeline 2 ?
 2. Contents of the package
 3. Release notes
 4. Prerequisites
 5. Getting started
 6. Documentation
 7. Known limitations
 8. Contact



1. What is the DAISY Pipeline 2 ?
------------------------------------------------------------------------------

The DAISY Pipeline 2 is an ongoing project to develop a next generation
framework for automated production of accessible materials for people with
print disabilities. It is the follow-up and total redesign of the original
DAISY Pipeline 1 project.

For more information see:

 - the project page: http://www.daisy.org/pipeline2
 - the development site: https://github.com/daisy



2. Contents of the package
------------------------------------------------------------------------------

The package includes:

 - a modular runtime framework for the Pipeline 2 modules
 - a graphical user interface (GUI) to execute pipeline scripts
 - a command line interface (CLI) to execute pipeline scripts
 - dedicated launchers for the Pipeline 2 Web Service, in the "bin" directory
 - a set of processing modules providing the following conversions:
   * daisy202-to-epub3       Transforms a DAISY 2.02 publication into an EPUB 3
                             publication.
   * daisy202-validator      Validates a DAISY 2.02 fileset.
   * daisy3-to-epub3         Transforms a DAISY 3 publication into an EPUB 3
                             publication.
   * dtbook-to-daisy3        Converts multiple dtbooks to daisy 3 format
   * dtbook-to-epub3         Converts multiple dtbooks to epub3 format
   * dtbook-to-html          Transforms DTBook XML into HTML.
   * dtbook-to-pef           Transforms a DTBook (DAISY 3 XML) document into
                             a PEF.
   * dtbook-to-zedai         Transforms DTBook XML into ZedAI XML.
   * dtbook-validator        Validates DTBook documents. Supports inclusion of
                             MathML.
   * epub3-to-daisy202       Transforms an EPUB3 publication into DAISY 2.02.
   * epub3-to-pef            Transforms a EPUB 3 publication into a PEF.
   * html-to-epub3           Transforms (X)HTML documents into an EPUB 3
                             publication.
   * html-to-pef             Transforms a HTML document into a PEF.
   * nimas-fileset-validator Validate a NIMAS Fileset. Supports inclusion of
                             MathML.
   * zedai-to-epub3          Transforms a ZedAI (DAISY 4 XML) document into an
                             EPUB 3 publication.
   * zedai-to-html           Transforms ZedAI XML (ANSI/NISO Z39.98-2012
                             Authoring and Interchange) into HTML.
   * zedai-to-pef            Transforms a ZedAI (DAISY 4 XML) document into a
                             PEF.
 - a set of sample documents to test the provided conversions, in the
   "samples" directory

3. Release Notes
------------------------------------------------------------------------------

The package includes the 1.10.0-beta1 version of the project.
This is a beta release.

### Changes in v1.10.0-beta1

* Distribution
  * [NEW] new graphical user interface (GUI)
  * [NEW] installers for the GUI on Windows
  * [NEW] packaged application on Mac OS X
  * online Java installer bundled in the Windows installer
  * Java Runtime Environment bundled in the Mac OS X app

* Command Line Interface
  * [NEW] `clean` command to remove jobs with an `ERROR` status
  * inputs and options arguments are no longer prefixed with `--i` and `--x`
  * the `version` command now works as expected
  * Properly detect Java under OpenJDK and Ubuntu 15.04

* Framework and API
  * [NEW] Add datatypes to options and list them through the api
  * [NEW] Expose the default value for options
  * [NEW] Add support for job batches
  * Get rid of the folder in zipped ports and options
  * Catch out of memory errors
  * Return a more meaningful error when inputs are not corect
  * Improved control of script removal
  * Improved logging filters
  * Normalize whitespace in script documentation parsing
  * Do not strip out spaces if xml:space="preserve"
  * Allow posting job requests using a namespace prefix   

* Modules
  * [NEW] DAISY 2.02 validator
  * [NEW] DAISY 3 (audio-only) to DAISY 2.02
  * [NEW] EPUB 3 validator (EpubCheck)
  * [NEW] EPUB 3 to PEF script
  * [NEW] HTML to PEF script
  * Experimental audio-only DAISY 3 production
  * Improved MathML production in DAISY 3
  * Improved NIMAS validation
  * Optimized fileset lading
  * ... and various other bug fixes

See also the full release notes on the release page:
  https://github.com/daisy/pipeline-assembly/releases/tag/v1.10.0-beta1

4. Prerequisites                   
------------------------------------------------------------------------------

Modules already include their dependent libraries and only require a recent
Java environment (Java SE 8 update 45 or later).

To get the latest version of Java, go to http://www.java.com/

5. Getting Started
------------------------------------------------------------------------------

### Command line tool ###

 1. get the short help by running the launcher script 'dp2' on
 Mac/Linux or 'dp2.exe' on Windows from the "cli" directory
 2. run 'dp2 help a-script-name' to get the detailed description of a script
 4. execute a job with the 'dp2 a-script-name' subcommand and specify the
 required options (as given with the 'dp2 help a-script-name' command)

For instance:

	> cli\dp2.exe dtbook-to-zedai --source samples\dtbook\hauy_valid.xml
	--output "C:\Users\John Doe\Desktop\out"


will run the DTBook to ZedAI converter on Windows and will output the result 
in the "out" directory on the desktop of the user named "John Doe".


### RESTful Web Service ###

 1. start the web service by running 'bin/pipeline' on Mac/Linux or
 'bin\pipeline.bat' on Windows
 2. the web service is available on http://localhost:8181/ws/
 3. For example, get the list of scripts by issuing a GET request on
 http://localhost:8181/ws/scripts




6. Documentation
------------------------------------------------------------------------------
  
  Usage dp2 [GLOBAL_OPTIONS] command [COMMAND_OPTIONS] [PARAMS]
  
  
  Script commands:
  
     daisy202-to-epub3        Transforms a DAISY 2.02 publication into an EPUB3
                              publication.
     daisy202-validator       Validates a DAISY 2.02 fileset.
     daisy3-to-epub3          Transforms a DAISY 3 publication into an EPUB 3
                              publication.
     dtbook-to-daisy3         Converts multiple dtbooks to daisy 3 format
     dtbook-to-epub3          Converts multiple dtbooks to epub3 format
     dtbook-to-html           Transforms DTBook XML into HTML.
     dtbook-to-pef            Transforms a DTBook (DAISY 3 XML) document into a
                              PEF.
     dtbook-to-zedai          Transforms DTBook XML into ZedAI XML.
     dtbook-validator         Validates DTBook documents. Supports inclusion of
                              MathML.
     epub3-to-daisy202        Transforms an EPUB3 publication into DAISY 2.02.
     epub3-to-pef             Transforms a EPUB 3 publication into a PEF.
     html-to-epub3            Transforms (X)HTML documents into an EPUB 3
                              publication.
     html-to-pef              Transforms a HTML document into a PEF.
     nimas-fileset-validator  Validate a NIMAS Fileset. Supports inclusion of
                              MathML.
     zedai-to-epub3           Transforms a ZedAI (DAISY 4 XML) document into an 
                              EPUB 3 publication.
     zedai-to-html            Transforms ZedAI XML (ANSI/NISO Z39.98-2012 Authoring
                              and Interchange) into HTML.
     zedai-to-pef             Transforms a ZedAI (DAISY 4 XML) document into a PEF.
  
          
  
  General commands:
  
     status        Returns the status of the job with id JOB_ID
     delete        Removes a job from the pipeline
     results       Stores the results from a job
     jobs          Returns the list of jobs present in the server
     log           Stores the results from a job
     queue         Shows the execution queue and the job's priorities. 
     moveup        Moves the job up the execution queue
     movedown      Moves the job down the execution queue
     clean         Removes the jobs with an ERROR status
     halt          Stops the webservice
     version       Prints the version and authentication information
          
  
  
  List of global options:                 dp2 help -g
  List of admin commands:                 dp2 help -a
  Detailed help for a single command:     dp2 help COMMAND  

A complete user guide is in the works and will be available soon.


7. Known limitations
------------------------------------------------------------------------------

Please refer to the issue tracker:
 https://github.com/daisy/pipeline-issues/issues


8. Contact 
------------------------------------------------------------------------------

If you want to join the effort and contribute to the Pipeline 2 project, feel
free to join us on the developers discussion list hosted on Google Groups:
 http://groups.google.com/group/daisy-pipeline-dev

or contact the project lead (Romain Deltour) via email at
 `rdeltour (at) gmail (dot) com`
