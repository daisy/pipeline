---
layout: default
---
# User Guide

## [GUI]({{site.baseurl}}/wiki/gui/DAISY-Pipeline-2-User-Guide/)

## [Web UI]({{site.baseurl}}/wiki/webui/User-Guide/)

## CLI

## Scripts

{% sparql doc in "SELECT ?href ?title WHERE { [] a dp2:script ; dp2:doc ?href . ?href dc:title ?title ; a dp2:userdoc } ORDER BY ?title" %}
* [{{doc.title}}]({{doc.href}})
{% endsparql %}
