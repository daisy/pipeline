{{#sparql}}
SELECT ?href WHERE {
  []    a        dp2:script ;
        dp2:doc  ?href .
  ?href dc:title ?title ;
        a        dp2:userdoc . }
ORDER BY ?title
* [[{{title}}|{{href}}]]
{{/sparql}}
