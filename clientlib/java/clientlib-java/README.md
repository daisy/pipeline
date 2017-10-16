Pipeline 2 - Java Client Library - Core
-----------------

This project provides a Java library for communicating with the DAISY Pipeline 2 Web Service.

Example; getting a list of all scripts:

    Pipeline2WSResponse response;
    List<Script> scripts;
    
    try {
        // Request the list of scripts
        response = Scripts.get("http://localhost:8182/ws", "clientid", "supersecret");
        
        // Make sure that we received a valid HTTP response
        if (response.status != 200) {
            System.err.println(response.status+" - "+response.statusName+": "+response.statusDescription);
            System.err.println("Response: "+response.asText());
            return;
        }
        
        // Parse the response
        scripts = Script.getScripts(response);
        
    } catch (Pipeline2WSException e) {
        System.err.println("Sorry, something unexpected occured while communicating with the Pipeline 2 framework: "+e.getMessage());
        return;
    }
    
    for (Script script : scripts) {
        System.out.println("Script: "+script.nicename);
    }


See also:

 - the [JavaDoc](https://raw.github.com/daisy/pipeline-clientlib-java/master/lib/doc/index.html)
 - Benetech has also implemented and released their client library here: [github.com/benetech/daisy-pipeline2-client](https://github.com/benetech/daisy-pipeline2-client)
