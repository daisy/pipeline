---
layout: default
---
# Scripts

{% sparql doc in "SELECT ?href ?title WHERE { [] a dp2:script ; dp2:doc ?href . ?href dc:title ?title ; a dp2:userdoc } ORDER BY ?title" %}
* [{{doc.title}}]({{doc.href}})
{% endsparql %}
