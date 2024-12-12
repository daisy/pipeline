-- see https://pandoc.org/lua-filters.html#pandoc.Figure

function Blocks(blocks)
  local result = {}

  for _, block in ipairs(blocks) do
    if block.t == "Para"
       and #block.content >= 3
       and block.content[1].t == "Image"
       and block.content[2].t == "SoftBreak" then

      -- Ensure no additional line breaks after the caption
      local valid = true
      for i = 3, #block.content do
        if block.content[i].t == "SoftBreak"
           or block.content[i].t == "LineBreak" then
          valid = false
          break
        end
      end

      if valid then
        local img = block.content[1]

        local caption_inlines = {}
        for i = 3, #block.content do
          table.insert(caption_inlines, block.content[i])
        end

        local caption = pandoc.Caption({pandoc.Plain(caption_inlines)})

        table.insert(
          result,
          pandoc.Figure(
            { pandoc.Para { img } },
            caption,
            pandoc.Attr()
          )
        )
      else
        table.insert(result, block)
      end
    else
      table.insert(result, block)
    end
  end

  return result
end