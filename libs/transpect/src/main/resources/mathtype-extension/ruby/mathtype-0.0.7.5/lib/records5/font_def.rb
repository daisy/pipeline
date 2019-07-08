# FONT_DEF records (17):
# Consists of:
# record type (17)
# [enc_def_index] index of corresponding ENCODING_DEF record (unsigned integer)
# [name] null-terminated font name
# This record associates an font encoding with a font name. See Definition
# records.

module Mathtype5
  class RecordFontDef < BinData::Record
    mt_uint :enc_def_index
    stringz :font_name
  end
end
