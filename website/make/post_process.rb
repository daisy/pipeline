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

def link_error(link, href_attr, source_file)
  raise "link can not be processed: #{link[href_attr]} (source: #{source_file})"
end

def link_warning(link, href_attr, source_file)
  puts "WARNING: link can not be processed: #{link[href_attr]} (source: #{source_file})"
  link['class'] = ((link['class']||'').split(' ') << 'broken-link').join(' ')
end

Dir.glob(base_dir + '/**/*.html').each do |f|
  doc = File.open(f) { |f| Nokogiri::HTML(f) }
  if not f.start_with?(base_dir)
    raise "coding error"
  end
  f_path = f[base_dir.length..-1]
  page_url = RDF::URI(site_base + baseurl + f_path)
  
  ## process links and images
  doc.css('a, img, iframe').each do |a|
    href_attr = (a.name == 'img' or a.name == 'iframe') ? 'src' : 'href';
    
    if not a[href_attr]
      next
    end

    # link to source files with special class attribute
    if a.name == 'a' and ['userdoc','apidoc','source'].include?(a['class'])
      query = SPARQL.parse(%Q{
        BASE <#{page_url}>
        PREFIX dp2: <http://www.daisy.org/ns/pipeline/>
        SELECT ?href WHERE {
          { <#{a['href']}> dp2:doc ?href }
          UNION
          { [] dp2:doc ?href ; dp2:alias '#{a['href']}' } .
          ?href a dp2:#{a['class']} .
        }
      })
      result = query.execute(graph)
      if not result.empty?
        abs_url = result[0]['href']
      elsif a['href'] =~ /http.*/o
        link_warning(a, href_attr, f)
        next
      end
    else

      # absolute path
      if a[href_attr] =~ /^\//o
        abs_path = a[href_attr]
        
      # absolute url
      elsif a[href_attr] =~ /http.*/o
        abs_url = a[href_attr]
      end
    end
    if not abs_path
      if not abs_url
        
        # relative path
        rel_path = a[href_attr]
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

      # external link
      if not abs_url.to_s.start_with?("#{site_base}#{baseurl}")
        if a.name == 'a'
          a['class'] = ((a['class']||'').split(' ') << 'external-link').join(' ')
          a['target'] = '_blank'
        end
        next
      end
      abs_path = abs_url.to_s[site_base.length..-1]
    end
    if not baseurl.empty?
      if not abs_path.start_with?(baseurl)
        link_error(a, href_attr, f)
      end
      abs_path = abs_path[baseurl.length..-1]
    end
    if rel_path and f_path.start_with?('/wiki/') and not abs_path.start_with?('/wiki/')
      link_error(a, href_attr, f)
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
    if File.exist?(base_dir + abs_path) and not File.directory?(base_dir + abs_path)
      a[href_attr] = baseurl + abs_path + fragment
      next
    elsif File.exist?(base_dir + abs_path + '.html')
      # a[href_attr] = basedir + abs_path + fragment
      a[href_attr] = baseurl + abs_path + '.html' + fragment
      next
    elsif File.exist?(base_dir + abs_path + '/index.html')
      a[href_attr] = baseurl + abs_path + fragment
      next
    end

    # link_error(a, href_attr, f)
    link_warning(a, href_attr, f)
  end

  ## work around css shortcomings
  doc.css('ul.spine').each do |ul|
    if ul.xpath(".//li[contains(concat(' ',@class,' '), ' spine-item-current ')]").any?
      ul['class'] = ul['class'] + ' spine-has-current'
    end
  end
  doc.css('li.spine-item').each do |li|
    if li.xpath(".//li[contains(concat(' ',@class,' '), ' spine-item-current ')]").any?
      li['class'] = li['class'] + ' spine-item-has-current'
    end
  end
  
  ## set tab order to how it appears to sighted users (top-level items
  ## are split off from rest using css)
  tabindex = 1
  doc.css('#nav-sitemap > ul > li > a').each do |a|
    a['tabindex'] = tabindex
    tabindex = tabindex + 1
  end
  doc.css('#nav-sitemap > ul ul a').each do |a|
    a['tabindex'] = tabindex
    tabindex = tabindex + 1
  end

  ## FIXME: this is a hack!
  ## mark part of nav-sitemap that is positioned on the left
  doc.css('#nav-sitemap > ul.spine > li.spine-item-current > ul,
           #nav-sitemap > ul.spine > li > ul.spine-has-current').each do |ul|
    ul['id'] = 'nav-sitemap-left'
    break # there is at most one
  end
  
  File.open(f, 'w') { |f| f.write(doc.to_html) }
end
