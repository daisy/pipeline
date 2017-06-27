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
   [:version, 'x-SNAPSHOT'],
   [:packaging, 'pom'],
   [:dependencyManagement,
    [:dependencies,
     [:dependency,
      [:groupId, 'org.daisy.pipeline'],
      [:artifactId, 'assembly'],
      [:version, versions['assembly']],
      [:type, 'pom'],
      [:scope, 'import']]]],
   [:build,
    [:plugins,
     [:plugin,
      [:artifactId, 'maven-dependency-plugin'],
      [:executions,
       [:execution,
        [:id, 'unpack-doc'],
        [:goals,
         [:goal, 'unpack']],
        [:configuration,
         [:excludes, 'META-INF,META-INF/**/*'],
         if modules
           [:artifactItems,
            modules.map {|mod|
              group = mod['group']
              artifact = mod['artifact']
              version = mod['version']
              if not (group == 'org.daisy.pipeline.modules' or group.start_with?('org.daisy.pipeline.modules.'))
                STDERR.puts 'unexpected groupId: ' + group
                exit 1
              end
              [:artifactItem,
               [:groupId, group],
               [:artifactId, artifact],
               version && [:version, version],
               [:type, 'jar'],
               [:classifier, 'doc'],
               [:outputDirectory, (group.gsub('.', '/') + '/' + artifact).sub(/^org\/daisy\/pipeline\//, '')]]}]
         else
           []
         end      
        ]]]]]]])
