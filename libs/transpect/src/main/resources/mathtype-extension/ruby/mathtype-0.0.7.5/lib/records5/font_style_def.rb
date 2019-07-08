# FONT_STYLE_DEF record (8):
# Consists of:
# record type (8)
# [font_def_index] index of mtefFONT_DEF record (unsigned integer)
# [char_style] character style bits
# This record associates a character style with a font. See Definition records.

module Mathtype5
  class RecordFontStyleDef < BinData::Record
    mt_uint :font_def_index

    int8 :char_style
  end
end
