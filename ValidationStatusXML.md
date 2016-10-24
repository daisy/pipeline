<!--
summary validation status XML format description
-->

# Validation Status XML

Every validator has a port named `validation-status` which provides a succinct summary of the validation results.

There are two use cases that come to mind.

1. A validation job may be "successful" even when the document is invalid. It would be helpful for the UI to be able to give more details about the job results. By using the `validation-status` port, the UI can quickly determine that a. this was a validation script and b. validation passed/failed.

2. Other scripts that incorporate a validator may want to know if the validation was successful or not.

## Details

### Example

~~~xml
<d:validation-status xmlns:d="http://www.daisy.org/ns/pipeline/data" result="error"/>
~~~

### `d:validation-status`
The document root element

#### `result` attribute

- `ok`: if there were no errors
- `error` if there were any errors

### Schema

TBD

## Notes

At this moment, the validators have only errors to report, not warnings. Therefore there is no mention of warnings here. However, this format will evolve if required to accommodate future developments.

One thing to consider is whether it would be useful to report the number of errors.
