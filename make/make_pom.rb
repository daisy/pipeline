#!/usr/bin/env ruby
require 'yaml'
require 'commaparty'

versions = YAML.load_file(ARGV[0])
modules = YAML.load_file(ARGV[1])

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
        [:version, versions['assembly']],
        [:type, 'pom'],
        [:scope, 'import']]]],
   if modules
     [:dependencies,
      modules.map {|mod|
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
