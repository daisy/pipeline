require 'rdf/query'
require 'rdf/turtle'
require 'rdf/rdfa'

module Jekyll
  class CreateSpines < Generator
    def merge_title(page, title)
      if title
        
        # possibly overwriting existing title
        page.data['title'] = title
      end
      return page
    end
    def generate(site)
      pages = site.pages | site.collections.values.map(&:docs).reduce(:|)
      baseurl = site.config['baseurl']
      site_base = site.config['site_base']
      meta_file = site.config['meta_file']
      if File.zero?(meta_file)

        # we are still generating the metadata file
        # leave the spines data empty for now
        site.data['spines'] = {}
        return
      end
      graph = RDF::Graph.load(meta_file)
      q = RDF::Query.new do
        pattern [ :src_url, RDF::URI("http://www.daisy.org/ns/pipeline/permalink"), :dest_url ]
      end
      permalink_mapping = Hash[
        q.execute(graph).map { |r|
          src_url = r['src_url'].to_s
          if not src_url.start_with?(site_base + baseurl)
            raise "coding error"
          end
          src_path = src_url[site_base.length+baseurl.length..-1]
          if src_path =~ /^(.*\/)index\.html$/o
            src_path = $1
          elsif src_path =~ /^(.+)\.html$/o
            src_path = $1
          end
          dest_url = r['dest_url'].to_s
          if not dest_url.start_with?(site_base + baseurl)
            raise "Invalid dp2:base value: must start with #{site_base + baseurl}: #{dest_url}"
          end
          dest_path = dest_url[site_base.length+baseurl.length..-1]
          if dest_path =~ /^(.*\/)index\.html$/o
            dest_path = $1
          elsif dest_path =~ /^(.+)\.html$/o
            dest_path = $1
          end
          [dest_path, src_path]
        }
      ]
      github_wiki_to_spine_items = lambda { |sidebar|
        items = []
        acc = File.readlines(sidebar)
              .reject { |line| line.strip.empty? }
              .reduce([{'indent' => nil, 'item' => {'pages' => items}}]) { |acc, line|
          if line =~ /^( *)\* \[\[(.*)\|(.+)\]\]$/o
            indent = $1
            title = $2
            href = $3
            last_indent = acc.last['indent']
            if not last_indent or indent.length > last_indent.length
            else
              loop do
                item = acc.pop['item']
                if acc.empty?
                  raise "coding error"
                end
                if not acc.last['item']['pages'] or acc.last['item']['pages'].empty?
                  raise "coding error"
                end
                acc.last['item']['pages'].pop
                acc.last['item']['pages'].push(item)
                if indent.length == last_indent.length
                  break
                else
                  last_indent = acc.last['indent']
                  if not last_indent or indent.length > last_indent.length
                    raise "unexpected indentation: #{line}"
                  else
                    next
                  end
                end
              end
            end
            if href =~ /^http.*/o
              site.collections.values.each do |c|
                if c.metadata['alternative_base']
                  if href.start_with?(c.metadata['alternative_base'])
                    path = href[c.metadata['alternative_base'].length..-1]
                    href = baseurl + c.metadata['permalink'].sub(/\/:path\/$/, path)
                    break
                  end
                end
              end
            end
            if href.start_with?(site_base + baseurl)
              path = href[site_base.length+baseurl.length..-1]
              if path =~ /^(.*\/)index\.html$/o
                path = $1
              elsif path =~ /^(.+)\.html$/o
                path = $1
              end
              if permalink_mapping.has_key?(path)
                path = permalink_mapping[path]
              end
              page = pages.detect { |page|
                if path.end_with?('/')
                  [path, path + 'index.html'].include?(page.url)
                else
                  [path, path + '/', path + '.html', path + '/index.html'].include?(page.url)
                end
              }
              if not page
                raise "spine link can not be resolved: #{href}"
              end
              if not title.empty?
                merge_title(page, title)
              end
              if page.path =~ /^(.*)\/index\.(html|md)$/o
                if File.exist?($1 + '/_Sidebar.md')
                  page = {
                    'group' => page,
                    'pages' => github_wiki_to_spine_items.call($1 + '/_Sidebar.md')
                  }
                end
              end
            elsif href =~ /^http.*/o
              page = {'external' => true, 'url' => href, 'title' => title}
            else
              if href.start_with?(baseurl)
                path = href[baseurl.length..-1]
                if path =~ /^(.*\/)index\.html$/o
                  path = $1
                elsif path =~ /^(.+)\.html$/o
                  path = $1
                end
                if permalink_mapping.has_key?(path)
                  path = permalink_mapping[path]
                end
                page = pages.detect { |page|
                  if path.end_with?('/')
                    [path, path + 'index.html'].include?(page.url)
                  else
                    [path, path + '/', path + '.html', path + '/index.html'].include?(page.url)
                  end
                }
              else
                base_path = File.expand_path("..", sidebar) + '/'
                site.collections.values.each do |c|
                  if base_path == "#{site.source}/_#{c.label}/"
                    Dir.glob("#{base_path}**/*").each do |f|
                      if f.end_with?('.md')
                        if href == File.basename(f).sub(/\.md$/, '')
                          page = pages.detect { |page| page.path == f }
                          break
                        end
                      end
                    end
                  end
                end
                if not page
                  path = base_path + href
                  if path =~ /^(.*\/)index\.html$/o
                    path = $1
                  elsif path =~ /^(.+)\.html$/o
                    path = $1
                  end
                  if path.start_with?(Dir.pwd)
                    path = path[Dir.pwd.length..-1]
                  end
                  if permalink_mapping.has_key?(path)
                    path = permalink_mapping[path]
                  end
                  page = pages.detect { |page|
                    if path.end_with?('/')
                      [path, path + 'index.html'].include?(page.url)
                    else
                      [path, path + '/', path + '.html', path + '/index.html'].include?(page.url)
                    end
                  }
                end
                if not page
                  path = base_path + href + '.md'
                  page = pages.detect { |page| page.path == path }
                end
              end
              if not page
                raise "spine link can not be resolved: #{href}"
              end
              if not title.empty?
                merge_title(page, title)
              end
            end
            if not acc.last['item'].is_a? Hash
              acc.last['item'] = {'group' => acc.last['item']}
            end
            if not acc.last['item']['pages']
              acc.last['item'] = {'pages' => []}.merge(acc.last['item'])
            end
            acc.last['item']['pages'].push(page)
            acc.push({'indent' => indent, 'item' => page})
            acc
          else
            raise "expecting item of the form '* [[Title|path]]' but got: #{line}"
          end
        }
        loop do
          item = acc.pop['item']
          if acc.empty?
            break
          end
          if not acc.last['item']['pages'] or acc.last['item']['pages'].empty?
            raise "coding error"
          end
          acc.last['item']['pages'].pop
          acc.last['item']['pages'].push(item)
        end
        return items
      }
      create_spine_item = lambda { |item|
        if item.is_a? String
          path = item
          if path =~ /^(.*\/)index\.html$/o
            path = $1
          elsif path =~ /^(.+)\.html$/o
            path = $1
          end
          if permalink_mapping.has_key?(path)
            path = permalink_mapping[path]
          end
          page = pages.detect { |page|
            if path.end_with?('/')
              [path, path + 'index.html'].include?(baseurl + page.url)
            else
              [path, path + '/', path + '.html', path + '/index.html'].include?(baseurl + page.url)
            end
          }
          if page
            return page
          else
            raise "spine link can not be resolved: #{item}"
          end
        elsif item['pages']
          page = merge_title(create_spine_item.call(item['group']), item['title'])
          if item['pages'].is_a? Hash and item['pages']['github_wiki']
            if item['pages'].length > 1
              raise "github_wiki must be the only key"
            end
            {
              'group' => page,
              'pages' => github_wiki_to_spine_items.call(site.source + item['pages']['github_wiki'])
            }
          else
            {
              'group' => page,
              'pages' => item['pages'].map(&create_spine_item)
            }
          end
        elsif item['group']
          create_spine_item.call(item['group'])
        elsif item['url']
          if item['url'] =~ /^https?:/o
            {'external' => true}.merge(item)
          else
            merge_title(create_spine_item.call(item['url']), item['title'])
          end
        else
          "spine item can not be processed: #{item}"
        end
      }
      site.data['spines'] = Hash[
        site.data['_spines'].map { |name, spine|
          [
            name,
            if spine
              if spine.is_a? Hash and spine['github_wiki']
                if spine.length > 1
                  raise "github_wiki must be the only key"
                end
                github_wiki_to_spine_items.call(site.source + spine['github_wiki'])
              else
                spine.map(&create_spine_item)
              end
            end
          ]
        }
      ]
    end
  end
end

