#!/usr/bin/env ruby
require 'mustache'
require 'sparql'
require 'rdf/query'
require 'rdf/turtle'
require 'rdf/rdfa'
require "#{File.expand_path(File.dirname(__FILE__))}/../src/_plugins/lib/relativize"

meta_file = ARGV[1]
base_dir = ARGV[2]
site_base = ARGV[3]

PREFIXES = %(
  PREFIX dc: <http://purl.org/dc/elements/1.1/>
  PREFIX dp2: <http://www.daisy.org/ns/pipeline/>
)

graph = RDF::Graph.load(meta_file)

# parse input into query and text
def parse(input)
  query = input
  text = ""
  while true
    begin
      return [SPARQL.parse(PREFIXES + query, validate: true), text]
    rescue EBNF::LL1::Parser::Error => e
      idx = query.rindex(e.token.to_s)
      if not idx
        raise "coding error"
      end
      if idx == 0
        raise "Invalid query in #{f}:\n#{input}"
      end
      text = query[idx, query.length] + text
      query = query[0, idx]
    rescue => e
      raise "Invalid query in #{f}:\n#{input}"
    end
  end
end

class MyMustache < Mustache
  def partial(name)
    path = "#{File.expand_path(File.dirname(__FILE__))}/mustache/#{name}.mustache"
    if File.exist?(path)
      File.read(path)
    else
      super(name)
    end
  end
end

DOC = RDF::URI("http://www.daisy.org/ns/pipeline/doc")
SCRIPT = RDF::URI("http://www.daisy.org/ns/pipeline/script")
OPTION = RDF::URI("http://www.daisy.org/ns/pipeline/option")
ID = RDF::URI("http://www.daisy.org/ns/pipeline/id")
NAME = RDF::URI("http://www.daisy.org/ns/pipeline/name")
DESC = RDF::URI("http://www.daisy.org/ns/pipeline/desc")

Dir.glob(ARGV[0]).each do |f|
  if File.file?(f)
    page_url = RDF::URI(f.dup.sub!(base_dir, site_base).gsub(/\.md$/, '.html'))
    page_view = MyMustache.new
    page_view['sparql'] = lambda { |input|
      begin
        (query, text) = parse(input)
      rescue => e
        print "#{$!}\n\t#{e.backtrace.join('\n\t')}"
        exit 1
      end
      solutions = query.execute(graph)
      solutions_rendered = ''
      solutions.each do |solution|
        solution_view = Mustache.new
        solution.to_hash.each do |k, v|
          solution_view[k.to_s] =
            case v
            when RDF::URI
              relativize(page_url, v).to_s
            else
              v.to_s
            end
        end
        solutions_rendered << solution_view.render("#{text}")
      end
      solutions_rendered
    }
    script_info_q = RDF::Query.new do
      pattern [ :script, DOC, page_url ]
      pattern [ :script, RDF.type, SCRIPT ]
      pattern [ :script, OPTION, :option ]
      pattern [ :option, ID, :id ]
      pattern [ :option, NAME, :name ]
      pattern [ :option, DESC, :desc ]
    end
    script_info = graph.query(script_info_q)
    if not script_info.empty?
      options = Hash.new
      page_view['options'] = options
      script_info.each do |solution|
        options[solution.id.to_s] = {
          'name' => solution.name.to_s,
          'desc' => solution.desc.to_s
        }
      end
      options['all'] = script_info.map { |solution|
        {
          'id' => solution.id.to_s,
          'name' => solution.name.to_s,
          'desc' => solution.desc.to_s
        }
      }
    end
    page_view.template_file = f
    file_rendered = page_view.render
    File.open(f, 'w') { |f| f.write(file_rendered) }
  end
end
