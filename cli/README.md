Command-Line Interface for the DAISY Pipeline 2 (Golang)
=======================================================
[![Build Status](https://img.shields.io/github/actions/workflow/status/daisy/pipeline-cli-go/main.yml)](https://github.com/daisy/pipeline-cli-go/actions/workflows/main.yml)

How to build
------------
1. Install golang from the [official site](http://golang.org/doc/install). If you'll be creating distributions of the cli please install from the [sources](http://golang.org/doc/install/source)

2. Run `make`. The building process will an executable named "dp2" in the build/bin/ folder.

3. Copy the default configuration file to the same directory as the binary:

        cp dp2/config.yml build/bin/

How to build and distribute using maven
---------------------------------------
In order to allow the go client play nice with the rest of the pipeline ecosystem a maven build process is provided, although right now it only works on linux and mac systems ( You should be able to make it work using cygwin though).

        mvn clean install

You can find in the target/bin directory all the binaries from windows,mac and linux platforms.

Usage
-----

```
Usage dp2 [GLOBAL_OPTIONS] command [COMMAND_OPTIONS] [PARAMS]


Script commands:

        zedai-to-epub3             Transforms a ZedAI (DAISY 4 XML) document into an EPUB 3 publication.
        dtbook-to-zedai             Transforms DTBook XML into ZedAI XML.
        dtbook-to-html             Transforms DTBook XML into HTML.
        nimas-fileset-validator             Validate a NIMAS Fileset. Supports inclusion of MathML.
        zedai-to-html             Transforms ZedAI XML (ANSI/NISO Z39.98-2012 Authoring and Interchange) into HTML.
        daisy3-to-epub3             Transforms a DAISY 3 publication into an EPUB 3 publication.
        html-to-epub3             Transforms an (X)HTML document into an EPUB 3 publication.
        dtbook-to-epub3             Converts multiple dtbooks to epub3 format
        daisy202-to-epub3             Transforms a DAISY 2.02 publication into an EPUB3 publication.
        dtbook-validator             Validates DTBook documents. Supports inclusion of MathML.
        

General commands:

        status             Returns the status of the job with id JOB_ID
        delete             Removes a job from the pipeline
        results             Stores the results from a job
        jobs             Returns the list of jobs present in the server
        log             Stores the results from a job
        halt             Stops the webservice

List of global options:                 dp2 help -g
Detailed help for a single command:     dp2 help COMMAND
```

Configuration
-------------

Modify the settings in the file config.yml or alternatively use the global witches:

```
Global Options:
       --host [HOST]    Pipeline's webservice host (default http://localhost)
       --port [PORT]    Pipeline's webserivce port (default 8181)
       --exec_line_win [EXEC_LINE_WIN]  Pipeline webserivice executable path in windows systems (default )
       --debug [DEBUG]  Print debug messages. true or false.  (default false)
       --ws_path [WS_PATH]      Pipeline's webservice path, as in http://daisy.org:8181/path (default ws)
       --exec_line_nix [EXEC_LINE_NIX]  Pipeline webserivice executable path in unix-like systems (default /home/javi/bin/pipeline2)
       --client_secret [CLIENT_SECRET]  Client secrect for authenticated requests (default supersecret)
       --timeout [TIMEOUT]      Http connection timeout in seconds (default 10)
       --starting [STARTING]    Start the webservice in the local computer if it is not running. true or false (default false)
       --ws_timeup [WS_TIMEUP]  Time to wait until the webserivce starts in seconds (default 25)
       --client_key [CLIENT_KEY]        Client key for authenticated requests (default clientid)
       -f,--file [FILE]        Alternative configuration file
```
