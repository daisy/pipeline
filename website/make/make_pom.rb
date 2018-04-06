#!/usr/bin/env ruby
require 'yaml'
require 'commaparty'

versions = YAML.load_file(ARGV[0])
modules = YAML.load_file(ARGV[1])
api = YAML.load_file(ARGV[2])

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
               [:outputDirectory, 'doc/' + (group.gsub('.', '/') + '/' + artifact)]]}]
         else
           []
         end      
        ]],
       [:execution,
        [:id, 'unpack-javadoc'],
        [:goals,
         [:goal, 'unpack']],
        [:configuration,
         [:excludes, 'index-all.html,allclasses-frame.html,allclasses-noframe.html,overview-frame.html,overview-summary.html,overview-tree.html,deprecated-list.html,constant-values.html,serialized-form.html,package-list,META-INF,META-INF/**/*'],
         if api
           [:artifactItems,
            api['javadoc'].map {|mod|
              group = mod['group']
              artifact = mod['artifact']
              version = mod['version']
              [:artifactItem,
               [:groupId, group],
               [:artifactId, artifact],
               version && [:version, version],
               [:type, 'jar'],
               [:classifier, 'javadoc'],
               [:outputDirectory, 'javadoc']]}]
         else
           []
         end
        ]],
       [:execution,
        [:id, 'unpack-xprocdoc'],
        [:goals,
         [:goal, 'unpack']],
        [:configuration,
         [:excludes, 'index.html,libraries.html,overview.html,steps.html,META-INF,META-INF/**/*'],
         if api
           [:artifactItems,
            api['xprocdoc'].map {|mod|
              group = mod['group']
              artifact = mod['artifact']
              version = mod['version']
              [:artifactItem,
               [:groupId, group],
               [:artifactId, artifact],
               version && [:version, version],
               [:type, 'jar'],
               [:classifier, 'xprocdoc'],
               [:outputDirectory, 'xprocdoc']]}]
         else
           []
         end
        ]]]]]]])
