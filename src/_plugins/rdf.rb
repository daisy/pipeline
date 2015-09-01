require 'sparql'
require 'rdf/turtle'
require 'rdf/rdfa'

module Jekyll
  
  class SparqlBlock < Liquid::Block
    
    Syntax = /\A([\w\-]+)\s+in\s+"([^"]*)"|'([^']*)'/o
    
    PREFIXES = %(
      PREFIX dc: <http://purl.org/dc/elements/1.1/>
      PREFIX dp: <http://www.daisy.org/ns/pipeline/>
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
      page_url = RDF::URI(config['site_base'] + context.environments.first["page"]["url"])
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
                  relativize(page_url, v).to_s
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
    
    private
    
    ##
    # own implementation of relativize
    #
    def relativize(base, child)
      if !child.hier? || !base.hier? ||
         (base.scheme || '').downcase != (child.scheme || '').downcase ||
         base.authority != child.authority
        return child
      else
        bp = base.normalized_path
        cp = child.normalized_path
        if cp.start_with?('/')
          bp_segments = bp.split('/')[0..-2]
          cp_segments = cp.split('/')
          i = bp_segments.length
          j = 0
          while i > 0 do
            if bp_segments[j] == cp_segments[j]
              i -= 1
              j += 1
            else
              break
            end
          end
          relativized_path = (['..'] * i + cp_segments[j..-1]) * '/'
        else
          relativized_path = cp
        end
        return RDF::URI(path: relativized_path, query: child.query, fragment: child.fragment)
      end
    end
  end
end

Liquid::Template.register_tag('sparql', Jekyll::SparqlBlock)
