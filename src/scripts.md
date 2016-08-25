---
---
# Scripts

{% sparql doc in "SELECT ?href ?title WHERE { [] a dp2:script ; dp2:doc ?href . ?href dc:title ?title }" %}
* [{{doc.title}}]({{doc.href}})
{% endsparql %}
