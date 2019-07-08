# COLOR records (15):
# Consists of:
# record type (15)
# [color_def_index] index of corresponding COLOR_DEF record (unsigned integer)
# The appearance of this record in the stream indicates that all following
# equation records (until the next COLOR record) have the color defined by the
# indicated COLOR_DEF record.

module Mathtype5
  class RecordColor < BinData::Record
    mt_uint :color_def_index
  end
end
