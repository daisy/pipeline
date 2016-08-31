module Jekyll
  class CreateSpines < Generator
    def generate(site)
      pages = site.pages | site.collections.values.map(&:docs).reduce(:|)
      create_spine_item = lambda { |item|
        if item.is_a? String
          pages.detect { |page| page.url == item }
        else
          {
            'group' => create_spine_item.call(item['group']),
            'pages' => item['pages'].map(&create_spine_item)
          }
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

