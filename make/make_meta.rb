#!/usr/bin/env ruby
require 'rdf/turtle'
require 'rdf/rdfa'

graph = RDF::Graph.new
Dir.glob(ARGV[0]).each do |f|
  graph.load(f, :format => :rdfa)
end
RDF::Turtle::Writer.new do |writer|
   writer << graph
end
