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
      create_spine_item = lambda { |item|
        if item.is_a? String
          path = item
          if path =~ /^(.*\/)index\.html$/o
            path = $1
          elsif path =~ /^(.+)\.html$/o
            path = $1
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
          {
            'group' => merge_title(create_spine_item.call(item['group']), item['title']),
            'pages' => item['pages'].map(&create_spine_item)
          }
        elsif item['group']
          create_spine_item.call(item['group'])
        elsif item['url']
          if item['url'] =~ /^http:/o
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
              spine.map(&create_spine_item)
            end
          ]
        }
      ]
    end
  end
end

