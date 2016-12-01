---
layout: default
---
# Scripts

"Scripts" is the term we use for the conversion tasks you can perform
with DAISY Pipeline 2. The complete list of available scripts is:

{% sparql doc in "SELECT ?href ?title WHERE { [] a dp2:script ; dp2:doc ?href . ?href dc:title ?title ; a dp2:userdoc } ORDER BY ?title" %}
* [{{doc.title}}]({{doc.href}})
{% endsparql %}
