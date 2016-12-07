---
layout: default
---
# Command Line Tool

The command line is not a standalone tool. It only works in
combination with a running [Pipeline server](Pipeline-as-Service). The
server may be running on the same computer or a different one. See
[Configuration](#configuration) for how to specify the address of the
server.

## Installation

Choose a package from the
[downloads page]({{site.baseurl}}/Download.html) that includes the
command line tool, then follow the installation instructions for that
package.

## Usage

The executable is called `dp2`, or `dp2.exe` on Windows. Where exactly
this file is located depends on the installation.

In order to get an overview of the commands, run `dp2 help`:

    Usage: dp2 [GLOBAL_OPTIONS] COMMAND [COMMAND_OPTIONS]
    
    Script commands:{% sparql script in "SELECT ?id ?desc WHERE { [] a dp2:script ; dp2:id ?id ; dp2:desc ?desc } ORDER BY ?id" %}
        {{script.id}}{% assign len = script.id | size %}{% for i in (len..26) %} {% endfor %}{{script.desc}}{% endsparql %}
    
    General commands:
        status                     Returns the status of the job with id JOB_ID
        delete                     Removes a job from the pipeline
        results                    Stores the results from a job
        jobs                       Returns the list of jobs present in the server
        log                        Stores the results from a job
        queue                      Shows the execution queue and the job's priorities.
        moveup                     Moves the job up the execution queue
        movedown                   Moves the job down the execution queue
        clean                      Removes the jobs with an ERROR status
        halt                       Stops the webservice
        version                    Prints the version and authentication information

    List of global options:                 dp2 help -g
    List of admin commands:                 dp2 help -a
    Detailed help for a single command:     dp2 help COMMAND

To get the list of admin commands, run `dp2 help -a`:

    Admin commands:
        list                       Returns the list of the available clients
        create                     Creates a new client
        remove                     Removes a client
        modify                     Modifies a client
        client                     Prints the detailed client inforamtion
        properties                 List the pipeline ws runtime properties
        sizes                      Prints the total size or a detailed list of job data stored in the
                                   server

For detailed help on a specific script or other command, run `dp2 help COMMAND`. For example:

    Usage: dp2 [GLOBAL_OPTIONS] dtbook-to-epub3 [OPTIONS]
    
    Options:
        --source SOURCE                One or more DTBook files to be transformed. In the case of
                                       multiple files, a merge will be performed.
        --language [LANGUAGE]          Language code of the input document.
        --assert-valid [ASSERT-VALID]  Whether to stop processing and raise an error on validation issues.
        --tts-config [TTS-CONFIG]      Configuration file for the Text-To-Speech.
        --audio [AUDIO]                Whether to use a speech synthesizer to produce audio files.
        -o,--output [OUTPUT]           Path where to store the results. This option is mandatory when
                                       the job is not executed in the background
        -z,--zip                       Write the output to a zip file rather than to a folder
        -n,--nicename [NICENAME]       Set job's nice name
        -r,--priority [PRIORITY]       Set job's priority (high|medium|low)
        -q,--quiet                     Do not print the job's messages
        -p,--persistent                Delete the job after it is executed
        -b,--background

## Configuration

Configuration settings can be passed as options. In order to get the
full list of available options, run `dp2 help -g`. The default
settings may be altered in a file called `config.yml`. This file is
located in the same directory as the `dp2` executable. To change the
location, use the `--file` option.

    Global Options:
        --host [HOST]                    Pipeline's webservice host (default http://localhost)
        --port [PORT]                    Pipeline's webserivce port (default 8181)
        --ws_path [WS_PATH]              Pipeline's webservice path, as in http://daisy.org:8181/path
                                         (default ws)
        --timeout [TIMEOUT]              Http connection timeout in seconds (default 60)
        --client_key [CLIENT_KEY]        Client key for authenticated requests (default clientid)
        --client_secret [CLIENT_SECRET]  Client secrect for authenticated requests (default supersecret)
                                         true or false (default true)
        --starting [STARTING]            Start the webservice in the local computer if it is not running.
        --exec_line [EXEC_LINE]          Pipeline webserivice executable path (default ../bin/pipeline2)
        --ws_timeup [WS_TIMEUP]          Time to wait until the webservice starts in seconds (default 25)
        --debug [DEBUG]                  Print debug messages. true or false (default false)
        -f,--file [FILE]                 Alternative configuration file
