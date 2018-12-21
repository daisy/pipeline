<!--
summary status XML format description
-->

# Status XML

Scripts may have a port annotated with media type `application/vnd.pipeline.status+xml` which provides a succinct summary of the job status. It may be compared with the exit value of a process.

There are two use cases that come to mind.

1. A job may return documents even if the conversion can not be considered "successful". An example is a validation job which needs to return a validation report when the input document is invalid. With a status port the UI can quickly determine whether the validation passed or failed.

2. When a step is used in other steps, a status port can convey status information to the calling step.

## Details

### Example

~~~xml
<d:status xmlns:d="http://www.daisy.org/ns/pipeline/data" result="error"/>
~~~

### `d:status`
The document root element

#### `result` attribute

- `ok`: if there were no errors
- `error` if there were any errors

### Schema

TBD

## Notes

At this moment, validators have only errors to report, not warnings. Therefore there is no mention of warnings here. However, this format will evolve if required to accommodate future developments.

One thing to consider is whether it would be useful to report the number of errors.
