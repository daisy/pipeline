require 'rdf/query'
require 'rdf/turtle'
require 'rdf/rdfa'

module Jekyll
  class InjectMetaAsVars < Generator
    
    TITLE = RDF::URI("http://purl.org/dc/elements/1.1/title")
    
    def generate(site)
      graph = RDF::Graph.load(site.config['meta_file'])
      site_base = site.config['site_base']
      baseurl = site.config['baseurl'] || ''
      
      # note: does not work for pages in collections because Jekyll gives them
      # a title based on the file name by default!
      pages = site.pages # | site.collections.values.map(&:docs).reduce(:|)
      pages.each do | page |
        if not page.data['title']
          page_url = RDF::URI(site_base + baseurl + page.url + (page.url.end_with?('/') ? 'index.html' : ''))
          title_query = RDF::Query.new do
            pattern [ page_url, TITLE, :title ]
          end
          title_solutions = graph.query(title_query)
          if not title_solutions.empty?
            page.data['title'] = title_solutions[0].title.to_s
          end
        end
      end
    end
  end
end
