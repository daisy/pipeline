#!/usr/bin/env ruby
require 'yaml'
require 'commaparty'

doc_modules = YAML.load_file(ARGV[0])

$stdout << CommaParty.markup(
  [:project, {xmlns: 'http://maven.apache.org/POM/4.0.0'},
   [:modelVersion, '4.0.0'],
   [:groupId, 'x'],
   [:artifactId, 'x'],
   [:version, 'x'],
   [:packaging, 'pom'],
   [:dependencyManagement,
    [:dependencies,
      [:dependency,
        [:groupId, 'org.daisy.pipeline'],
        [:artifactId, 'assembly'],
        [:version, '1.9.15'],
        [:type, 'pom'],
        [:scope, 'import']]]],
   if doc_modules
     [:dependencies,
      doc_modules.map {|mod|
        group = mod['group']
        artifact = mod['artifact']
        version = mod['version']
        [:dependency,
         [:groupId, group],
         [:artifactId, artifact],
         version && [:version, version]]}]
   else
     []
   end ])
