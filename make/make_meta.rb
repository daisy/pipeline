#!/usr/bin/env ruby
require 'rdf/query'
require 'rdf/turtle'
require 'rdf/rdfa'
require 'nokogiri'
require 'yaml'

base_dir = ARGV[1]
config = YAML.load_file(ARGV[2])

site_base = config['site_base']
baseurl = config['baseurl'] || ''

TITLE = RDF::URI("http://purl.org/dc/elements/1.1/title")

graph = RDF::Graph.new
Dir.glob(ARGV[0]).each do |f|
  page_url = RDF::URI(f.dup.sub!(base_dir, site_base + baseurl))
  
  # get RDF already present in file
  graph.load(f, :format => :rdfa, :base_uri => page_url)
  
  # infer dc:title if not present
  has_title = RDF::Query.new do
    pattern [ page_url, TITLE, :title ]
  end
  if graph.query(has_title).empty?
    title = nil
    doc = File.open(f) { |f| Nokogiri::HTML(f) }
    node = doc.css('h1, h2, h3, h4, h5, h6')[0]
    if node and not node.content.to_s.empty?
      title = node.content.to_s
    end
    if not title
      title = File.basename(f, '.*').sub('_', ' ')
      if title == 'index'
        title = File.basename(File.expand_path('..', f)).sub('_', ' ')
      end
    end
    graph.data.insert(RDF::Statement.new(page_url, TITLE, title))
  end
end
RDF::Turtle::Writer.new do |writer|
   writer << graph
end
