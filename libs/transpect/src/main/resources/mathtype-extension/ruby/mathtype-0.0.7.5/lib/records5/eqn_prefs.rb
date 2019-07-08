# EQN_PREFS records (18):

# Consists of:

# record type (18)
# [options] none defined in this version of MTEF
# [sizes] dimension array for typesize definitions
# [spaces] dimension array for spacing definitions (see below)
# [styles] array of style definitions (see below)
# When reading arrays, the number of values may be less than or greater than
# expected. MTEF readers should be driven by the array count.
# If the array is shorter than expected, assume defaults for the missing values.
# If the array is longer than expected, the extra values must be skipped to stay
# in sync with the MTEF stream.

module Mathtype5
  class Entry < BinData::Record
    bit4 :unit
    array :nibbles, read_until: lambda { element == 0xF } do
      bit4
    end
  end

  class RecordEqnPrefs < BinData::Record
    int8 :options

    int8 :sizes_count
    array :sizes, initial_length: :sizes_count do
      entry
    end

    int8 :spaces_count
    array :spaces, initial_length: :spaces_count do
      entry
    end

    int8 :styles_count
    array :styles, initial_length: :styles_count do
      int8 :font_def
      int8 :font_style, onlyif: lambda { font_def != 0x00 }
    end
    resume_byte_alignment
  end
end
