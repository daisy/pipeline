#!/usr/bin/env ruby
require 'yaml'
require 'commaparty'

versions = YAML.load_file(ARGV[0])
doc_modules = YAML.load_file(ARGV[1])

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
        [:version, versions['engine_version']],
        [:type, 'pom'],
        [:scope, 'import']]]],
   if doc_modules
     [:dependencies,
      doc_modules.map {|mod|
        group = mod['group']
        artifact = mod['artifact']
        version = mod['version']
        version ||= group == 'org.daisy.pipeline.modules.braille' &&
                    artifact =~ /^mod-(celia|dedicon|mtm|nlb|nota|sbs)$/ &&
                    versions['mod_%s_version' % $1]
        [:dependency,
         [:groupId, group],
         [:artifactId, artifact],
         version && [:version, version]]}]
   else
     []
   end ])
