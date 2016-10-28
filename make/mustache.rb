#!/usr/bin/env ruby
require 'mustache'
require 'sparql'
require 'rdf/query'
require 'rdf/turtle'
require 'rdf/rdfa'
# require 'github/markup'
require 'kramdown'
require 'yaml'
require 'nokogiri'
require "#{File.expand_path(File.dirname(__FILE__))}/../src/_plugins/lib/relativize"

meta_file = ARGV[1]
base_dir = ARGV[2]
config = YAML.load_file(ARGV[3])

site_base = config['site_base']
baseurl = config['baseurl'] || ''
site_base_url = RDF::URI(site_base)

$kramdown_config = config['kramdown']

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
      if query[0, idx] =~ /(\s+)$/o
        idx -= $1.length
      end
      text = query[idx, query.length] + text
      query = query[0, idx]
    rescue => e
      raise "Invalid query in #{f}:\n#{input}"
    end
  end
end

def render_markdown(md)
  # GitHub::Markup.render('irrelevant.md', md)
  Kramdown::Document.new(md, $kramdown_config).to_html
end

def render_description(desc)
  lines = desc.lines
  if lines.count > 1
    [lines[0],render_markdown(lines[1..-1].join)]
  else
    lines
  end
end

$data_type_rng = File.open("#{File.dirname(__FILE__)}/data-type.rng", 'r') { |f| Nokogiri::XML::RelaxNG(f) }

def render_data_type(definition, sequence)
  xml = Nokogiri::XML(definition)
  $data_type_rng.validate(xml).each do |error|
    puts error.message
    raise 'Cannot parse data type XML: ' + definition
  end
  render = ''
  node = xml.elements[0]
  case node.name
  when 'data'
    data = node
    type = data.attributes['type']
    case type.value
    when 'string'
      node = data.elements[0]
      case node.name
      when 'documentation'
        documentation = node
        render << render_description(documentation.text).join
        return render
      when 'param'
        param = node
        name = param.attributes['name']
        case name.value
        when 'pattern'
          render << "A string that matches the pattern:<br/><code class='pattern'>#{pattern.text}</code>"
          return render
        end
      end
    end
  when 'choice'
    if sequence
      render << 'Zero or more of the following:'
    else
      render << 'One of the following:'
    end
    render << '<dl class="choice">'
    choice = node
    choice.elements.each do |node|
      case node.name
      when 'value'
        value = node
        render << '<dt><code>'
        render << value.text
        render << '</code></dt>'
      when 'data'
        data = node
        type = data.attributes['type']
        documentation = data.elements[0]
        case type.value
        when 'anyFileURI'
          render << '<dt><var>&lt;anyFileURI&gt;</var></dt>'
          render << '<dd>'
          if documentation
            render << render_description(documentation.text).join
          else
            render << "A relative file path"
          end
          render << '</dd>'
        else
          render << '<dt><var>&lt;anyURI&gt;</var></dt>'
          render << '<dd>'
          if documentation
            render << render_description(documentation.text).join
          else
            render << "An absolute URI"
          end
          render << '</dd>'
        end
      when 'documentation'
        documentation = node
        render << '<dd>'
        render << render_description(documentation.text).join
        render << '</dd>'
      end
    end
    render << '</dl>'
    return render
  end
  raise 'coding error while parsing ' + definition
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

DP2 = "http://www.daisy.org/ns/pipeline/"
DOC = RDF::URI("#{DP2}doc")
SCRIPT = RDF::URI("#{DP2}script")
INPUT = RDF::URI("#{DP2}input")
OUTPUT = RDF::URI("#{DP2}output")
OPTION = RDF::URI("#{DP2}option")
ID = RDF::URI("#{DP2}id")
NAME = RDF::URI("#{DP2}name")
DESC = RDF::URI("#{DP2}desc")
REQUIRED = RDF::URI("#{DP2}required")
DEFAULT = RDF::URI("#{DP2}default")
SEQUENCE = RDF::URI("#{DP2}sequence")
MEDIA_TYPE = RDF::URI("#{DP2}media-type")
DATA_TYPE = RDF::URI("#{DP2}data-type")
DEFINITION = RDF::URI("#{DP2}definition")

Dir.glob(ARGV[0]).each do |f|
  if File.file?(f)
    page_url = RDF::URI(f.dup.sub!(base_dir, site_base + baseurl).gsub(/\.md$/, '.html'))
    page_view = MyMustache.new
    page_view['site'] = {
      'baseurl' => baseurl
    }
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
              relativize(site_base_url, v).to_s
            else
              v.to_s
            end
        end
        solutions_rendered << solution_view.render("#{text}")
      end
      solutions_rendered
    }
    script_info_solutions = RDF::Query.execute(graph) do
      pattern [ :script, DOC, page_url ]
      pattern [ :script, ID, :id ]
      pattern [ :script, NAME, :name ], optional: true
      pattern [ :script, DESC, :desc ], optional: true
    end
    if not script_info_solutions.empty?
      solution = script_info_solutions[0]
      page_view['id'] = solution.id
      if solution.bound?('name')
        page_view['name'] =  solution.name.to_s
      end
      if solution.bound?('desc')
        desc = render_description(solution.desc.to_s)
        page_view['desc'] = {
          'short' => desc[0],
          'long' => desc[1] ? desc.join : nil
        }
      end
    end
    options_query = RDF::Query.new do
      pattern [ :script, DOC, page_url ]
      pattern [ :script, RDF.type, SCRIPT ]
      pattern [ :script, OPTION, :option ]
      pattern [ :option, ID, :id ]
      pattern [ :option, REQUIRED, :required ], optional: true
      pattern [ :option, DEFAULT, :default ], optional: true
      pattern [ :option, SEQUENCE, :sequence ], optional: true
      pattern [ :option, DATA_TYPE, :data_type ], optional: true
      pattern [ :option, NAME, :name ], optional: true
      pattern [ :option, DESC, :desc ], optional: true
    end
    options_solutions = graph.query(options_query)
    if not options_solutions.empty?
      options = Hash.new
      page_view['options'] = options
      options['all'] = options_solutions.map { |solution|
        id = solution.id.to_s
        sequence = solution.bound?('sequence') ? solution.sequence.true? : false
        if solution.bound?('data_type')
          case solution.data_type
          when RDF::Literal
            data_type_id = solution.data_type.to_s
            data_type_solutions = RDF::Query.execute(graph) do
              pattern [ :type, RDF.type, DATA_TYPE ]
              pattern [ :type, ID, data_type_id ]
              pattern [ :type, DEFINITION, :def ]
            end
          else
            data_type_solutions = RDF::Query.execute(graph) do
              pattern [ :script, DOC, page_url ]
              pattern [ :script, OPTION, :option ]
              pattern [ :option, ID, id ]
              pattern [ :option, DATA_TYPE, :data_type ]
              pattern [ :data_type, RDF.type, DATA_TYPE ]
              pattern [ :data_type, ID, :id ]
              pattern [ :data_type, DEFINITION, :def ]
            end
          end
          if not data_type_solutions.empty?
            data_type = render_data_type(data_type_solutions[0].def.to_s, sequence)
          else
            case solution.data_type
            when RDF::Literal
              data_type = '<var>&lt;' + data_type_id + '&gt;</var>'
            else
              data_type = nil
            end
          end
        else
          data_type = '<var>&lt;string&gt;</var>'
        end
        if solution.bound?('name')
          name = solution.name.to_s
          if name =~ /^[^:]+: *(.*)$/o
            name = $1
          end
        else
          name = nil
        end
        if solution.bound?('desc')
          desc = render_description(solution.desc.to_s)
          desc = {
            'short' => desc[0],
            'long' => desc[1] ? desc.join : nil
          }
        else
          desc = nil
        end
        if solution.bound?('default')
          default = solution.default.to_s
        else
          default = nil
        end
        {
          'id' => id,
          'name' => name,
          'desc' => desc,
          'required' => solution.bound?('required') ? solution.required.true? : false,
          'sequence' => sequence,
          'default' => default ? (default.empty? ? '(empty)' : "<code>#{default}</code>") : nil,
          'data-type' => data_type
        }
      }
      options['all'].each do |option|
        options[option['id']] = option
      end
    end
    inputs_query = RDF::Query.new do
      pattern [ :script, DOC, page_url ]
      pattern [ :script, RDF.type, SCRIPT ]
      pattern [ :script, INPUT, :input ]
      pattern [ :input, ID, :id ]
      pattern [ :input, SEQUENCE, :sequence ], optional: true
      pattern [ :input, NAME, :name ], optional: true
      pattern [ :input, DESC, :desc ], optional: true
      pattern [ :input, MEDIA_TYPE, :type ], optional: true
    end
    inputs_solutions = graph.query(inputs_query)
    if not inputs_solutions.empty?
      inputs = Hash.new
      page_view['inputs'] = inputs
      inputs['all'] = inputs_solutions.map { |solution|
        if solution.bound?('name')
          name = solution.name.to_s
          if name =~ /^[^:]+: *(.*)$/o
            name = $1
          end
        else
          name = nil
        end
        if solution.bound?('desc')
          desc = render_description(solution.desc.to_s)
          desc = {
            'short' => desc[0],
            'long' => desc[1] ? desc.join : nil
          }
        else
          desc = nil
        end
        {
          'id' => solution.id.to_s,
          'name' => name,
          'desc' => desc,
          'sequence' => solution.bound?('sequence') ? solution.sequence.true? : false,
          'media-type' => solution.bound?('type') ? solution.type.to_s.split(' ') : nil
        }
      }
      inputs['all'].each do |input|
        inputs[input['id']] = input
      end
    end
    outputs_query = RDF::Query.new do
      pattern [ :script, DOC, page_url ]
      pattern [ :script, RDF.type, SCRIPT ]
      pattern [ :script, OUTPUT, :output ]
      pattern [ :output, ID, :id ]
      pattern [ :output, SEQUENCE, :sequence ], optional: true
      pattern [ :output, NAME, :name ], optional: true
      pattern [ :output, DESC, :desc ], optional: true
      pattern [ :output, MEDIA_TYPE, :type ], optional: true
    end
    outputs_solutions = graph.query(outputs_query)
    if not outputs_solutions.empty?
      outputs = Hash.new
      page_view['outputs'] = outputs
      outputs['all'] = outputs_solutions.map { |solution|
        if solution.bound?('name')
          name = solution.name.to_s
          if name =~ /^[^:]+: *(.*)$/o
            name = $1
          end
        else
          name = nil
        end
        if solution.bound?('desc')
          desc = render_description(solution.desc.to_s)
          desc = {
            'short' => desc[0],
            'long' => desc[1] ? desc.join : nil
          }
        else
          desc = nil
        end
        if solution.bound?('type')
          type = solution.type.to_s.split(' ')
        else
          type = nil
        end
        if not (type and type.include?('application/vnd.pipeline.status+xml'))
          {
            'id' => solution.id.to_s,
            'name' => name,
            'desc' => desc,
            'sequence' => solution.bound?('sequence') ? solution.sequence.true? : false,
            'media-type' => type,
            'is-report' => (type and type.include?('application/vnd.pipeline.report+xml'))
          }
        end
      }.reject{|x| x.nil? }
      report_outputs = outputs['all'].select{ |o| o['is-report'] }
      if not report_outputs.empty?
        reports = Hash.new
        page_view['reports'] = reports
        reports['all'] = report_outputs
        reports['all'].each do |report|
          reports[report['id']] = report
        end
      end
      outputs['all'] = outputs['all'].reject{ |o| o['is-report'] }
      if outputs['all'].empty?
        page_view['outputs'] = nil
      else
        outputs['all'].each do |output|
          outputs[output['id']] = output
        end
      end
    end
    page_view.template_file = f
    file_rendered = page_view.render
    File.open(f, 'w') { |f| f.write(file_rendered) }
  end
end
