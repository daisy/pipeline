# Web Service Job Creation

There are two ways of creating jobs. Both involve first creating an
XML document to represent the job request:

~~~xml
<jobRequest xmlns='http://www.daisy.org/ns/pipeline/data'>
    <script href='http://example.org/ws/scripts/dtbook-to-zedai'/>
    <input name='source'>
        ...
    </input>
    <option name='opt-mods-filename'>the-mods-file.xml</option>
    <option name='opt-css-filename'>the-css-file.css</option>
    <option name='opt-zedai-filename'>the-zedai-file.xml</option>
    <callback type='messages' href='http://example.org/ws/jobmessages' frequency='10'/>
    <callback type='status' href='http://example.org/ws/jobstatus' frequency='10'/>
</jobRequest>
~~~

 * `<script>`: gives the unique URI of the script that this job wishes to use
 * `<input>`: contains the input document(s) for the input port given by `@name`
 * `<option>`: option value
 * `<callback>`: the [WebServicePush push notification] callback

The inputs and options for a script can be retrieved by requesting a
description of that particular script. This data is then used when
building the job request.

A job can be created using inline XML or a multi-part upload.

## 1. Inline XML

This method is appropriate if the only files involved are XML, and no
external files (e.g. JPEGs) are required. The input XML document(s),
in this case DTBook documents, are nested in the job request itself:

~~~xml
<jobRequest xmlns='http://www.daisy.org/ns/pipeline/data'>
    <script href='http://example.org/ws/scripts/dtbook-to-zedai'/>
    <input name='source'>
        <docwrapper>
            <dtbook xmlns='http://www.daisy.org/z3986/2005/dtbook/' version='2005-3' xml:lang='en-US'>
                <head>
                    ...
                </head>
                <book>
                    ...
                </book>
            </dtbook>
        </docwrapper>
    </input>
</jobRequest>
~~~

If a sequence of documents is being passed to a port, then a series of
`<docwrapper>` elements should be used:

~~~xml
<input name="source">
	<docwrapper>
		<dtbook>...</dtbook>
	</docwrapper>
	<docwrapper>
		<dtbook>...</dtbook>
	</docwrapper>
	<docwrapper>
		<dtbook>...</dtbook>
	</docwrapper>
</input>
~~~


## 2. Multipart zip upload

Sometimes the input file's context is required in order to perform the
operation. An example is when the input file references images, and
those images will be copied to the output directory. In cases like
this, it is necessary to zip the input documents along with their data
and submit the zip file as part of a multi-part upload.

The first part of the multi-part upload, identified as `job-data`, is
a zip file containing input data.

The other part of the multi-part upload, identified as `job-request`,
is an XML file describing the job request:

~~~xml
<jobRequest xmlns='http://www.daisy.org/ns/pipeline/data'>
    <script href='http://example.org/ws/scripts/dtbook-to-zedai'/>
    <input name='source'>
        <item value='./dtbook-basic.xml'/>
    </input>
    <option name='opt-mods-filename'>the-mods-file.xml</option>
    <option name='opt-css-filename'>the-css-file.css</option>
    <option name='opt-zedai-filename'>the-zedai-file.xml</option>
</jobRequest>
~~~

The `<input>` element contains one or more `<item>` element children,
each referring to a file in the attached zip by its relative path
(relative to the root of the zip file).

When creating the multi-part request, the following conventions must be observed:

 * The field name for the zip data is "job-data"
 * The field name for the job request XML is "job-request"
 * The content type for the job request is "multipart/form-data"
