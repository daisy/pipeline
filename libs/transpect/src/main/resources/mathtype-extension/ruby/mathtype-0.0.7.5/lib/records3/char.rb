# CHAR record (2):
# Consists of:
# record type (2)
# options
# [nudge] if mtefOPT_NUDGE is set
# [typeface] typeface value (signed integer; see FONT_STYLE_DEF record below)
# [character] character value (see below)
# [embellishment list] if mtefOPT_CHAR_EMBELL is set (embellishments)
# The character value itself is represented by one or more values. The presence or absence of these value is indicated by options and appear in this order:
# 16-bit integer MTCode value present unless the mtefOPT_CHAR_ENC_NO_MTCODE option is set
# 8-bit font position present if the mtefOPT_CHAR_ENC_CHAR_8 option is set
# 16-bit integer font position  present if the mtefOPT_CHAR_ENC_CHAR_16 option is set
# The MTCode value defines the character independent of its font. MTCode is a superset of Unicode and is described in MTCode Encoding Tables. The 8-bit and 16-bit font positions are mutually exclusive but may both be absent. This is the position of the character within its font. Some of the common font encodings are given in Font Encoding Tables.

require_relative "snapshot"

module Mathtype3
  class RecordEmbell < BinData::Record; end
  class RecordChar < BinData::Record
    include Snapshot
    EXPOSED_IN_SNAPSHOT = %i(options nudge typeface mt_code_value embellishment_list)

    endian :little

    mandatory_parameter :_options

    virtual :_tag_options, :value => lambda{ _options }

    record_nudge :nudge, onlyif: lambda { _options & OPTIONS["xfLMOVE"] > 0 }

    int8 :_typeface

    mtef16 :_mt_code_value

    array :embellishment_list,
        onlyif: lambda { _options & OPTIONS["xfEMBELL"] > 0 },
        read_until: lambda { element.record_type == 0 } do
      named_record
    end

    def mt_code_value
      sprintf("0x%04X", _mt_code_value)
    end

    def typeface
      _typeface + 128
    end

    def options
      _tag_options
    end
  end
end
