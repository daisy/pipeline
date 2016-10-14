#!/usr/bin/env ruby
require 'nokogiri'
require 'sparql'
require 'rdf/turtle'
require 'rdf/rdfa'
require 'yaml'

meta_file = ARGV[0]
base_dir = ARGV[1]
config = YAML.load_file(ARGV[2])

site_base = config['site_base']
baseurl = config['baseurl'] || ''
graph = RDF::Graph.load(meta_file)

def link_error(link, source_file)
  raise "link can not be processed: #{link['href']} (source: #{source_file})"
end

def link_warning(link, source_file)
  puts "WARNING: link can not be processed: #{link['href']} (source: #{source_file})"
end

Dir.glob(base_dir + '/**/*.html').each do |f|
  doc = File.open(f) { |f| Nokogiri::HTML(f) }
  if not f.start_with?(base_dir)
    raise "coding error"
  end
  f_path = f[base_dir.length..-1]
  page_url = RDF::URI(site_base + baseurl + f_path)
  
  ## process links
  doc.css('a').each do |a|

    # external link
    if a['href'] =~ /http.*/o
      next
    end
    
    # absolute link
    if a['href'] =~ /^\//o
      abs_path = a['href']

    # link to source files with special class attribute
    elsif ['userdoc','apidoc','source'].include?(a['class'])
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
      result = query.execute(graph)
      if not result.empty?
        abs_url = result[0]['href']
      end
    end
    
    if not abs_path
      if not abs_url
        
        # relative link
        rel_path = a['href']
        if rel_path =~ /^([^#]*)(#.*)$/o
          rel_path = $1
          fragment = $2
        else
          fragment = ''
        end
        if rel_path.empty?
          next
        end
        if rel_path =~ /^(.+)\.md$/o
          rel_path = $1 + '.html'
        end
        if f_path.start_with?('/wiki/')
          rel_path = '../' + rel_path
        end
        abs_url = page_url.join(rel_path)
      end
      if not abs_url.to_s.start_with?(site_base)
        link_error(a, f)
      end
      abs_path = abs_url.to_s[site_base.length..-1]
    end
    if not baseurl.empty?
      if not abs_path.start_with?(baseurl)
        link_error(a, f)
      end
      abs_path = abs_path[baseurl.length..-1]
    end
    if rel_path and f_path.start_with?('/wiki/') and not abs_path.start_with?('/wiki/')
      link_error(a, f)
    end
    if not fragment
      if abs_path =~ /^([^#]*)(#.*)$/o
        abs_path = $1
        fragment = $2
      else
        fragment = ''
      end
    end
    if abs_path =~ /^(.*\/)index\.html$/o
      abs_path = $1
    elsif abs_path =~ /^(.+)\.html$/o
      abs_path = $1
    end    
    if abs_path.end_with?('/')
      abs_path = abs_path[0..-2]
    end
    if File.exist?(base_dir + abs_path)
      a['href'] = baseurl + abs_path + fragment
      next
    elsif File.exist?(base_dir + abs_path + '.html')
      # a['href'] = basedir + abs_path + fragment
      a['href'] = baseurl + abs_path + '.html' + fragment
      next
    elsif File.exist?(base_dir + abs_path + '/index.html')
      a['href'] = baseurl + abs_path + fragment
      next
    end

    # link_error(a, f)
    link_warning(a, f)
  end

  ## process spines
  doc.css('ul.spine').each do |ul|
    if ul.xpath(".//li[contains(concat(' ',@class,' '), ' spine-item-current ')]").any?
      ul['class'] = ul['class'] + ' spine-has-current'
    end
  end
  
  File.open(f, 'w') { |f| f.write(doc.to_html) }
end
