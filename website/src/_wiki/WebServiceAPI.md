<!--
summary Web Service API documentation
-->

# Web Service API

This document outlines the web service API for the DAISY Pipeline.

## Resources

RESTful web services describe a system in terms of its resources.
Generally speaking, resources can be created, retrieved, modified, and
deleted.

The main resources in this web service are *scripts*, *jobs*,
*clients* and *data-types*.

A *script* is a Pipeline XProc script. The script is identified with a
unique URI. It is described in terms of its functionality and all of
its inputs and options.

A *job* is the execution of a script. Jobs have unique IDs and are
created using input and option values. Jobs produce downloadable
results.

A *client* is an application using the Pipeline via its API. Examples
of clients are web applications and command line utilities. A client
is not a human user.

A *data type* determines the possible values a script option can
take. It is defined in an XML syntax (a subset of RelaxNG).

## Accessing Resources

RESTful applications communicate via HTTP and use standard methods and
[status codes](http://en.wikipedia.org/wiki/List_of_HTTP_status_codes)
to create, get, and delete resources.

The following HTTP methods are used here:

 * `GET`
 * `POST`
 * `PUT`
 * `DELETE`

Every method is not implemented for every resource because in some
cases, the method wouldn't be relevant. Any unimplemented methods will
respond with HTTP 405 "Method Not Allowed".

## The API

### WS Status

 * HTTP Method: `GET` 
 * URI: `/alive`
 * Response(s):
   * `HTTP 200 OK`: Response body contains XML data. See a
     [sample response document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/alive.xml).

### Scripts

####  Get all scripts

 * HTTP Method: `GET` 
 * URI: `/scripts`
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains XML data. See a
     [sample response document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/scripts.xml).
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.

#### Get a single script

 * HTTP Method: `GET`
 * URI: `/scripts/$ID`
   * Where $ID is the script's ID
 * Query parameters
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains XML data. See a
     [sample response document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/script.xml).
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 404 Not Found`: Resource not found	
 
### Jobs

#### Create a job

 * HTTP Method: `POST`
 * URI: `/jobs`
 * Query parameters:
   * See [Authentication](#authentication)
 * Request body:
   * jobRequest XML. See two different sample documents
     [inline](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/jobRequest2.xml)
     and
     [multipart](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/jobRequest1.xml).
 * Response(s):
   * `HTTP 201 Created`: Response body contains XML data about the new
     resource. See a
     [sample document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/job.xml).
   * `HTTP 400 Bad Request`: The request was invalid
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.

[Read more about creating jobs](WebServiceCreateJob).

#### Get all jobs

 * HTTP Method: `GET`
 * URI: `/jobs`
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains XML data. See a
     [sample response document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/jobs.xml).
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
 
#### Get a single job
 
 * HTTP Method: `GET` 
 * URI: `/jobs/$ID`
   * Where $ID is the job's unique ID
 * Query parameters: 
   * `msgSeq` (optional): Returned job status messages will be a
     subset of messages where the sequence values are greater than
     that provided.  The default is 0. This parameter allows
     incremental reporting on job status; i.e. "only return messages
     newer than X".
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains XML data. See a
     [sample response document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/job.xml).
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 404 Not Found`: Resource not found	

#### Delete a single job

 * HTTP Method: `DELETE` 
 * URI: `/jobs/$ID`
   * Where $ID is the job's unique ID
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 204 No Content`: Successfully processed the request, no content being returned
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 404 Not Found`: Resource not found

#### Get the result for a job

 * HTTP Method: `GET`
 * URI: `/jobs/$ID/result[/$TYPE/$NAME[/idx/$IDX]]`
   * Where $ID is the job's unique ID
   * $TYPE is whether port or option
   * $NAME is the port or option name
   * $IDX is the index of a single output within the port/option. 
   * An [example](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/job.xml) of a result description in the job xml format.
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains Zip data
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 404 Not Found`: Resource not found
 
#### Get the log file for a job
 
 * HTTP Method: `GET`
 * URI: `/jobs/$ID/log`
   * Where $ID is the job's unique ID
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains a plain text representation of the log file
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 404 Not Found`: Resource not found

### Data types

#### Get all data types

 * HTTP Method: `GET`
 * URI: `/datatypes`
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains XML data. The `datatypes`
    root element has a `href` attribute, and a `datatype` child for
    every existing data type. Each `datatype` element has a `href`
    atrtibute and an `id` attribute containing the data type's unique
    ID.
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.

#### Get a single data type

 * HTTP Method: `GET`
 * URI: `/datatypes/$ID`
   * Where $ID is the data type's unique ID
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains the definition of the data type in XML.
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 404 Not Found`: Resource not found

## Admin

In addition to managing scripts and jobs, the web service also
provides an administrative interface.  Use of this API is restricted
to use by authorized clients; i.e. those with `ADMIN` permissions.

### Stop the web service

 * HTTP Method: `GET`
 * URI: `/admin/halt/$KEY`
   * Where $KEY is generated by the web service upon startup and stored locally as a text file.
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 204 No Content`: Successfully processed the request, no content being returned
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 403 Forbidden`: The service could not be halted. This error would be caused by an invalid key.

### Get all clients

 * HTTP Method: `GET`
 * URI: `/admin/clients`
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains XML data. See a
     [sample response document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/clients.xml).
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 404 Not Found`: Resource not found	

### Create a new client

 * HTTP Method: `POST`
 * URI: `/admin/clients`
 * Query parameters:
   * See [Authentication](#authentication)
 * Request body:
   * Client XML. See a
     [sample](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/client.xml)
 * Response(s): 
   * `HTTP 201 Created`: Response body contains XML data about the new
     resource. See a
     [sample document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/client.xml).
   * `HTTP 400 Bad Request`: The request was invalid
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
 
### Get a single client
 
 * HTTP Method: `GET`
 * URI: `/admin/clients/$ID`
   * Where $ID is the client's unique ID
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains XML data. See a
     [sample response document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/client.xml).
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 404 Not Found`: Resource not found

### Delete a client

 * HTTP Method: `DELETE`
 * URI: `/admin/clients/$ID`
   * Where $ID is the client's unique ID
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 204 No Content`: Successfully processed the request, no content being returned
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 404 Not Found`: Resource not found

### Modify a client

 * HTTP Method: `PUT`
 * URI: `/admin/clients/$ID`
   * Where $ID is the client's unique ID
 * Query parameters:
   * See [Authentication](#authentication)
 * Response(s):
   * `HTTP 200 OK`: Response body contains XML data about the modified
     resource. See a
     [sample response document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/client.xml).
   * `HTTP 400 Bad Request`: The request was invalid
   * `HTTP 401 Unauthorized`: Client was not authorized to perform request.
   * `HTTP 404 Not Found`: Resource not found

### Schema

Schema for all the XML formats used in this API can be found
[here](https://github.com/daisy/pipeline-framework/tree/master/webservice-utils/src/main/resources/org/daisy/pipeline/webservice-utils/resources).

## Authentication

When authentication is enabled, the Pipeline only accepts requests
from authorized clients.  In order to become authorized, a client must
be added to the database of clients using the admin API described in
[#Admin].

The client must add the following query parameters to each request:

 * `authid`: Identifies the client application
 * `time`: Timestamp (UTC)
 * `nonce`: A unique string generated by the client
 * `sign`: Hash of the URI string including all query params except this one

Note that it is possible to start the web service with authentication
disabled.  This is appropriate when the web service is running only
locally or in other similarly controlled environments.  In this case,
none of the authentication parameters are required, and any that are
sent are ignored.

[Read more about authentication](WebServiceAuthentication).

## Local File System mode

The web service is by default run in what is referred to as local FS
mode. The property `org.daisy.pipeline.ws.localfs` must be set to
change it. In local mode, the web service assumes read/write access to
the disk. Documents that would normally be sent as part of a zip
archive or inline request can be referred to by their local file path
instead. Output options are enabled and output is written to disk
instead of being zipped up for download.

See [a sample document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/jobRequest1.localmode.xml).

## Web Service internal errors

In the unlikely but not impossible case that the Pipeline WS suffered
from an internal error, a descriptive xml is returned along with the
HTTP 500 header.

See the [xml sample document](https://raw.githubusercontent.com/daisy/pipeline-framework/master/webservice-utils/doc/xml-formats/error.xml). 
