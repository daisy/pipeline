require 'sparql'
require 'rdf/turtle'
require 'rdf/rdfa'
require "#{File.dirname(__FILE__)}/lib/relativize"

module Jekyll
  
  class SparqlBlock < Liquid::Block
    
    Syntax = /\A([\w\-]+)\s+in\s+"([^"]*)"|'([^']*)'/o
    
    PREFIXES = %(
      PREFIX dc: <http://purl.org/dc/elements/1.1/>
      PREFIX dp2: <http://www.daisy.org/ns/pipeline/>
    )
    
    def initialize(tag_name, markup, options)
      super
      if markup =~ Syntax
        @variable_name = $1
        @query = SPARQL.parse(PREFIXES + ($2 || $3))
      end
    end
    
    def render(context)
      config = context.registers[:site].config
      site_base_url = RDF::URI(config['site_base'])
      graph = RDF::Graph.load(config['meta_file'])
      solutions = @query.execute(graph)
      result = ''
      context.stack do
        solutions.each do |solution|
          context[@variable_name] = Hash[
            solution.to_hash.map { |k, v|
              [
                k.to_s,
                case v
                when RDF::URI
                  relativize(site_base_url, v).to_s
                else
                  v.to_s
                end
              ]
            }
          ]
          result << render_all(@nodelist, context)
        end
      end
      result
    end
  end
end

Liquid::Template.register_tag('sparql', Jekyll::SparqlBlock)
