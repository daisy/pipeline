# RULER record (7):
# Consists of:
# record type (7)
# [n_stops] number of tab-stops
# [tab-stop list] tab-stops in order from left-to-right
# Each tab stop is described by a tab-stop type
# (0 for left, 1 for center, 2 for right, 3 for equal, 4 for decimal),
# followed by a 16-bit integer offset from the left end of the slot or pile
# with which it is associated.

module Mathtype3
  class RecordRuler < BinData::Record
    endian :little

    int8 :n_stops

    array :tab_stops, initial_length: :n_stops do
      int8 :tab_stop_type
      mtef16 :tab_stop
    end
  end
end
