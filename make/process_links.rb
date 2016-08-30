#!/usr/bin/env ruby
require 'nokogiri'
require 'sparql'
require 'rdf/turtle'
require 'rdf/rdfa'
require 'yaml'
require "#{File.expand_path(File.dirname(__FILE__))}/../src/_plugins/lib/relativize"

meta_file = ARGV[0]
base_dir = ARGV[1]
config = YAML.load_file(ARGV[2])

site_base = config['site_base']
baseurl = config['baseurl'] || ''
graph = RDF::Graph.load(meta_file)

Dir.glob(base_dir + '/**/*.html').each do |f|
  doc = File.open(f) { |f| Nokogiri::HTML(f) }
  page_url = RDF::URI(f.dup.sub!(base_dir, site_base + baseurl))
  doc.css('a').each do |a|
    
    # absolute links (assume external)
    if a['href'] =~ /http.*/o
      next
    end
    
    # relative links to markdown
    if a['href'] =~ /(.+)\.md/o
      a['href'] = $1 + '.html'
    
    # links to source files with special class attribute
    elsif ['userdoc','apidoc','source'].include?(a['class'])
      
      # apidoc falls back to source
      if a['class'] == 'apidoc'
        query = SPARQL.parse(%Q{
          BASE <#{page_url}>
          PREFIX dp2: <http://www.daisy.org/ns/pipeline/>
          SELECT ?href WHERE {
            { <#{a['href']}> dp2:doc ?href }
            UNION
            { [] dp2:doc ?href ; dp2:id '#{a['href']}' } .
            ?href a ?class .
            FILTER ( ?class = dp2:apidoc || ?class = dp2:source )
          }
          ORDER BY ?class
        })
      else
        query = SPARQL.parse(%Q{
          BASE <#{page_url}>
          PREFIX dp2: <http://www.daisy.org/ns/pipeline/>
          SELECT ?href WHERE {
            { <#{a['href']}> dp2:doc ?href }
            UNION
            { [] dp2:doc ?href ; dp2:id '#{a['href']}' } .
            ?href a dp2:#{a['class']} .
          }
        })
      end
      result = query.execute(graph)
      if not result.empty?
        a['href'] = relativize(page_url, result[0]['href']).to_s
      end
    end
  end
  File.open(f, 'w') { |f| f.write(doc.to_html) }
end
