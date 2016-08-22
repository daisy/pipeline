#!/usr/bin/env ruby
require 'nokogiri'
require 'sparql'
require 'rdf/turtle'
require 'rdf/rdfa'

meta_file = ARGV[0]
base_dir = ARGV[1]
site_base = ARGV[2]

graph = RDF::Graph.load(meta_file)

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

Dir.glob(base_dir + '/**/*.html').each do |f|
  doc = File.open(f) { |f| Nokogiri::HTML(f) }
  page_url = RDF::URI(f.dup.sub!(base_dir, site_base))
  doc.css('a').each do |a|
    
    # absolute links
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
