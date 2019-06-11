# virustotal

Send local files to Virus Total and get a Schematron report

## Description

This XProc step use the Total Virus API to upload and scan local 
files. Scan reports are retrieved with `p:http-request` and 
validated with Schematron.

## Requirements

* Requires a Virus Total API key. You can obtain an API key by register an user account at https://www.virustotal.com/. Later you can receive your API key here: https://www.virustotal.com/de/user/MYUSERNAME/apikey/
* XML Calabash including transparent JSON extension enabled
  * Transparent JSON can be enabled with `-Xtransparent-json -Xjson-flavor=marklogic` command-line option
  * A pipeline can test whether the extension is enabled or not with the `p:system-property` function using `cx:transparent-json`.
  * find more information about transparent JSON in the [XML Calabash Reference](http://xmlcalabash.com/docs/reference/langext.html#ext.transparent-json)

## Ports

* `result` the JSON-XML-representation of the scan report
* `report` a Schematron SVRL document that contain one or more svrl:failed-assert 
elements for each scanner and successful virus detection

## Options

* `api-key` your personal VirusTotal API key
* `href` the file to be checked
* `scan-url` URL for VirusTotal scan request, default: `https://www.virustotal.com/vtapi/v2/file/scan`
* `report-url` URL for VirusTotal scan request, default: `https://www.virustotal.com/vtapi/v2/file/report`

