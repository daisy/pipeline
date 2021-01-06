#!/usr/bin/env ruby
require 'nokogiri'
require 'sparql'
require 'rdf/turtle'
require 'rdf/rdfa'
require 'rdf/query'
require 'yaml'
require 'fileutils'

meta_file = ARGV[0]
$base_dir = ARGV[1]
$src_base_dir = ARGV[2]
config = YAML.load_file(ARGV[3])

site_base = config['site_base']
baseurl = config['baseurl'] || ''
graph = RDF::Graph.load(meta_file)

q = RDF::Query.new do
  pattern [ :src_url, RDF::URI("http://www.daisy.org/ns/pipeline/permalink"), :dest_url ]
end
$permalink_mapping = Hash[
  q.execute(graph).map { |r|
    src_url = r['src_url'].to_s
    if not src_url.start_with?(site_base + baseurl)
      raise "coding error"
    end
    src_path = src_url[site_base.length+baseurl.length..-1]
    dest_url = r['dest_url'].to_s
    if not dest_url.start_with?(site_base + baseurl)
      raise "Invalid dp2:permalink value: must start with #{site_base + baseurl}: #{dest_url}"
    end
    dest_path = dest_url[site_base.length+baseurl.length..-1]
    if dest_path.end_with?('/')
      dest_path = dest_path + 'index.html'
    else
      dest_path.sub!(/\.md$/, '.html')
    end
    [src_path, dest_path]
  }
]
$reverse_permalink_mapping = Hash[
  $permalink_mapping.map { |k, v| [v, k] }
]

$collection_path_mapping = Hash[
  config['collections'].map { |name, metadata|
    ["/_#{name}/", metadata['permalink'].sub(/\/:path\/$/, '/')]
  }
]

# assume all collections are github wikis
# github wiki flattens directory structure
collection_files = Hash[
  config['collections'].map { |name, _|
    [
      "/_#{name}/",
      Hash[
        Dir.glob("#{$src_base_dir}/_#{name}/**/*").map { |f|
          [
            f.end_with?('.md') ? File.basename(f).sub(/\.md$/, '') : f[$src_base_dir.length+name.length+3..-1],
            f[$src_base_dir.length..-1]
          ]
        }
      ]
    ]
  }
]

# accepts path of a source file (relative to src_base_dir)
# returns path of corresponding destination file (relative to base_dir)
def to_destination(src_path)
  if $permalink_mapping.has_key?(src_path)
    return $permalink_mapping[src_path]
  end
  $collection_path_mapping.each do |src_dir, dest_dir|
    if src_path.start_with?(src_dir)
      dest_path = dest_dir + src_path[src_dir.length..-1]
      dest_path.sub!(/\.md$/, '.html')
      return dest_path
    end
  end
  dest_path = src_path
  dest_path.sub!(/\.md$/, '.html')
  return dest_path
end

# accepts path of a destination file (relative to base_dir)
# the destination file exists and is not a directory
# returns path of corresponding source file (relative to src_base_dir)
def to_source(dest_path)
  if $reverse_permalink_mapping.has_key?(dest_path)
    return $reverse_permalink_mapping[dest_path]
  end
  $collection_path_mapping.each do |src_dir, dest_dir|
    if dest_path.start_with?(dest_dir)
      src_path = src_dir + dest_path[dest_dir.length..-1]
      if File.exist?($src_base_dir + src_path)
        return src_path
      end
      src_path.sub!(/\.html$/, '.md')
      if File.exist?($src_base_dir + src_path)
        return src_path
      end
      src_path.sub!(/\/index\.md$/, '.md')
      if File.exist?($src_base_dir + src_path)
        return src_path
      end
    end
  end
  src_path = dest_path.dup()
  if File.exist?($src_base_dir + src_path)
    return src_path
  end
  src_path.sub!(/\.html$/, '.md')
  if File.exist?($src_base_dir + src_path)
    return src_path
  end
  src_path.sub!(/\/index\.md$/, '.md')
  if File.exist?($src_base_dir + src_path)
    return src_path
  end
  raise "source file of #{dest_path} not found"
end

$error_count = 0

def link_error(link, href_attr, source_file)
  puts "ERROR: link can not be processed: #{link[href_attr]} (source: #{source_file})"
  $error_count = $error_count + 1
end

def link_warning(link, href_attr, source_file)
  puts "WARNING: link can not be processed: #{link[href_attr]} (source: #{source_file})"
  link['class'] = ((link['class']||'').split(' ') << 'broken-link').join(' ')
end

# move files with dp2:permalink metadata
Dir.glob($base_dir + '/**/*.html').each do |f|
  if not f.start_with?($base_dir)
    raise "coding error"
  end
  f_path = f[$base_dir.length..-1]
  if $permalink_mapping.has_key?(f_path)
    f_new = $base_dir + $permalink_mapping[f_path]
    FileUtils.mkdir_p(File.expand_path('..', f_new))
    FileUtils.cp(f, f_new)
  end
end

Dir.glob($base_dir + '/**/*.{html,svg}').each do |f|
  if f.end_with?('html')
    doc = File.open(f) { |f| Nokogiri::HTML(f) }
  else
    doc = File.open(f) { |f| Nokogiri::XML(f) }
  end
  if not f.start_with?($base_dir)
    raise "coding error"
  end
  f_path = f[$base_dir.length..-1]
  src_path = to_source(f_path)
  page_url = RDF::URI(site_base + baseurl + f_path)
  page_type = nil

  ## process links and images
  doc.css(f.end_with?('html') ? 'a, img, iframe, link' : 'a').each do |a|
    if a.name == 'link' and not a['type'] == 'text/css'
      next
    end
    href_attr = f.end_with?('html') ? ((a.name == 'img' or a.name == 'iframe') ? 'src' : 'href') : 'xlink:href'
    if not a[href_attr]
      next
    end
    if a.name == 'a'

      # remove target attribute because there is only one frame
      # note that target="_blank" will be added for external pages, see below
      a.remove_attribute('target')

      if a[href_attr] =~ /^(mailto|javascript):/
        next
      end

      # link to source files with special class attribute
      if ['userdoc','apidoc','source'].include?(a['class'])
        link_class = a['class']
      elsif not a[href_attr] =~ /^https?:\/\//o or a[href_attr].start_with?(site_base + baseurl)

        # when current page is of type userdoc/apidoc/source, link to pages of the same type
        if not page_type

          # pages in /api/ are implicitly of type apidoc
          # disabled because it makes too build too slow
          if false # src_path.start_with?('/api/')
            page_type = 'apidoc'
          else
            q = RDF::Query.new do
              pattern [ page_url, RDF.type, :type ]
            end
            result = q.execute(graph)
            if not result.empty?
              page_type = result[0]['type'].to_s
              if page_type.start_with?('http://www.daisy.org/ns/pipeline/')
                page_type = page_type['http://www.daisy.org/ns/pipeline/'.length..-1]
                if not ['userdoc','apidoc','source'].include?(page_type)
                  page_type = ''
                end
              else
                page_type = ''
              end
            else
              page_type = ''
            end
          end
        end
        if page_type != ''
          link_class = page_type
        end
      end
    end
    if link_class
      q = SPARQL.parse(%Q{
        BASE <#{page_url}>
        PREFIX dp2: <http://www.daisy.org/ns/pipeline/>
        SELECT ?href WHERE {
          { <#{a[href_attr]}> dp2:doc ?href }
          UNION
          { [] dp2:doc ?href ; dp2:alias <#{a[href_attr]}> }
          UNION
          { <#{a[href_attr]}> dp2:alias [ dp2:doc ?href ] } .
          ?href a dp2:#{link_class} .
        }
      })
      result = q.execute(graph)
      if not result.empty?
        abs_url = result[0]['href']
        if abs_url == page_url

          # it could be that a page links to a file that it documents itself
          abs_url = nil
        end
      elsif link_class == a['class'] and a[href_attr] =~ /^https?:\/\//o and not a[href_attr].start_with?(site_base + baseurl)

        # userdoc/apidoc/source links must be internal
        link_error(a, href_attr, f)
        next
      end
      if not abs_url

        # if userdoc/apidoc/source page does not exist keep link to source file
        # link to the htmlized page if present
        # links to java files will be handled further below
        if link_class != 'source' and not a[href_attr].end_with?('.java')
          q = SPARQL.parse(%Q{
            BASE <#{page_url}>
            PREFIX dp2: <http://www.daisy.org/ns/pipeline/>
            SELECT ?href WHERE {
              { <#{a[href_attr]}> dp2:doc ?href }
              UNION
              { [] dp2:doc ?href ; dp2:alias <#{a[href_attr]}> }
              UNION
              { <#{a[href_attr]}> dp2:alias [ dp2:doc ?href ] } .
              ?href a dp2:source .
            }
          })
          result = q.execute(graph)
          if not result.empty?
            abs_url = result[0]['href']
          end
        end
      end
    else

      # absolute path
      if a[href_attr] =~ /^\//o
        abs_path = a[href_attr]
        
      # absolute url
      elsif a[href_attr] =~ /^https?:\/\//o
        abs_url = a[href_attr]
      end
    end
    if not abs_path
      if not abs_url

        # relative path
        rel_path = a[href_attr]

        # get fragment and query
        if rel_path =~ /^([^#]*)(#.*)$/o
          rel_path = $1
          fragment = $2
        else
          fragment = ''
        end
        if rel_path =~ /^([^\?]*)(\?.+)$/o
          rel_path = $1
          query = $2
        else
          query = ''
        end

        # link within same page
        if rel_path.empty?
          next
        end

        # .md -> .html
        if rel_path =~ /^(.+)\.md$/o
          rel_path = $1 + '.html'
        end

        # / -> /index.html
        if rel_path =~ /\/$/o
          rel_path = rel_path + 'index.html'
        end

        # handle relative links within wikis
        collection_files.each do |name, files|
          if src_path.start_with?(name)

            # links are assumed to consist of only a file name (github flattens the directories)
            if rel_path =~ /^(.*)\/index\.html$/o
              rel_path = $1
            end
            if files.key?(rel_path)
              abs_path = baseurl + to_destination(files[rel_path])
            else
              link_error(a, href_attr, f)
            end
            break
          end
        end

        # FIXME: support relative paths from a regular page to a wiki page (relative between source files)?

        # resolve relative link
        if not abs_path
          abs_url = RDF::URI(site_base + baseurl + src_path).join(rel_path).to_s
          if not abs_url.start_with?(site_base + baseurl)
            puts "unexpected relative path"
            link_error(a, href_attr, f)
          end
          abs_path = abs_url[site_base.length+baseurl.length..-1]
          abs_path = baseurl + to_destination(abs_path)
          abs_url = site_base + abs_path
        end
      end

      # at this point either abs_url or abs_path is set

      # resolve github wiki links
      if not abs_path
        if not abs_url.to_s.start_with?("#{site_base}#{baseurl}")
          config['collections'].each do |_, metadata|
            if metadata['alternative_base']
              if abs_url.to_s.start_with?(metadata['alternative_base'])
                if a.xpath("ancestor::*[@class='edit-button']").empty?
                  abs_path = baseurl + metadata['permalink'].sub(/\/:path\/$/, abs_url.to_s[metadata['alternative_base'].length..-1])
                end
                break
              end
            end
          end
        end
      end
      if not abs_path
        
        # external link
        if not abs_url.to_s.start_with?("#{site_base}#{baseurl}")
          if a.name == 'a'
            a['class'] = ((a['class']||'').split(' ') << 'external-link').join(' ')
            a['target'] = '_blank'
          end
          next
        end

        # relativize internal link to site base
        abs_path = abs_url.to_s[site_base.length..-1]
      end
    end
    if not baseurl.empty?
      if not abs_path.start_with?(baseurl)
        link_error(a, href_attr, f)
      end
      abs_path = abs_path[baseurl.length..-1]
    end

    # get fragment and query
    if not fragment
      if abs_path =~ /^([^#]*)(#.*)$/o
        abs_path = $1
        fragment = $2
      else
        fragment = ''
      end
    end
    if not query
      if abs_path =~ /^([^\?]*)(\?.+)$/o
        abs_path = $1
        query = $2
      else
        query = ''
      end
    end

    # remove index.html
    if abs_path =~ /^(.*\/)index\.html$/o
      abs_path = $1

    # remove .html
    elsif abs_path =~ /^(.+)\.html$/o
      abs_path = $1
    end

    # remove trailing '/'
    if abs_path.end_with?('/')
      abs_path = abs_path[0..-2]
    end

    # check that no links are broken
    if File.exist?($base_dir + abs_path) and not File.directory?($base_dir + abs_path)
      target_path = abs_path
    elsif File.exist?($base_dir + abs_path + '.html')
      target_path = abs_path + '.html'
      abs_path = target_path
    elsif File.exist?($base_dir + abs_path + '/index.html')
      target_path = abs_path + '/index.html'

    # change links to java files...
    elsif abs_path =~ /^\/modules\/.+\/java\/(.+)$/o

      # ... into javadoc if class is 'apidoc'
      if link_class == 'apidoc'
        api_path = '/api/' + $1
        if File.exist?($base_dir + api_path) and not File.directory?($base_dir + api_path)
          target_path = abs_path = api_path
        elsif File.exist?($base_dir + api_path + '.html')
          target_path = abs_path = api_path + '.html'
        elsif File.exist?($base_dir + api_path + '/index.html')
          abs_path = api_path
          target_path = api_path + '/index.html'
        elsif File.exist?($base_dir + api_path + '/package-summary.html')
          target_path = abs_path = api_path + '/package-summary.html'
        elsif File.exist?($base_dir + api_path.gsub(/\.java$/, '.html'))
          target_path = abs_path = api_path.gsub(/\.java$/, '.html')
        end
      end
      if not link_class == 'apidoc' or (not target_path and not (link_class == a['class']))

        # ... or into htmlized sources otherwise
        if File.exist?($base_dir + abs_path + '/package-summary.html')
          target_path = abs_path + '/package-summary.html'
          abs_path = target_path
        elsif File.exist?($base_dir + abs_path.gsub(/\.java$/, '.html'))
          target_path = abs_path.gsub(/\.java$/, '.html')
          abs_path = target_path
        end
      end
    end

    # some links will be broken because some javadoc files are omitted
    if not target_path
      if ['/api',
          '/api/overview-summary',
          '/api/overview-tree',
          '/api/index-all',
          '/api/allclasses-noframe',
          '/api/serialized-form',
          '/api/deprecated-list',
          '/api/constant-values',
          '/api/help-doc'].include?(abs_path)
        # FIXME
        #link_warning(a, href_attr, f)
      else
        link_error(a, href_attr, f)
      end
    end
    
    # check that links between wikis are absolute
    if not a[href_attr] =~ /^https?:\/\//o
      ['/_wiki/', '/_wiki_gui/', '/_wiki_webui/'].each do |src_dir|
        if src_path.start_with?(src_dir)
          if a.xpath("ancestor::ul[@class='spine']").empty?
            if target_path.start_with?('/css/')
              break
            end
            target_src_path = to_source(target_path)
            if not target_src_path.start_with?(src_dir)
              link_error(a, href_attr, f)
            end
            break
          end
        end
      end
    end

    # add fragment
    a[href_attr] = baseurl + abs_path + fragment
  end

  if $error_count > 0
    raise "errors were reported"
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

  if f.end_with?('html')
    File.open(f, 'w') { |f| f.write(doc.to_html) }
  else
    File.open(f, 'w') { |f| f.write(doc.to_s) }
  end
end
