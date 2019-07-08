# LINE record (1):
# Consists of:
# record type (1)
# options
# [nudge] if mtefOPT_NUDGE is set
# [line spacing] if mtefOPT_LINE_LSPACE is set (16-bit integer)
# [RULER record] if mtefOPT_LP_RULER is set
# object list of line (single pile, characters and templates, or nothing)
# The line spacing value, if present, is the distance between the baseline of
# this line and the line above it.

module Mathtype5
  class NamedRecord < BinData::Record; end
  class RecordLine < BinData::Record
    endian :little
    int8 :options

    record_nudge :nudge, onlyif: lambda { options & OPTIONS["mtefOPT_NUDGE"] > 0 }

    mtef16 :line_spacing, onlyif: (lambda do
      options & OPTIONS["mtefOPT_LINE_LSPACE"] > 0
    end)

    record_ruler :ruler, onlyif: (lambda do
      options & OPTIONS["mtefOPT_LP_RULER"] > 0
    end)

    array :object_list,
        onlyif: lambda { options & OPTIONS["mtefOPT_LINE_NULL"] == 0 },
        read_until: lambda { element.record_type == 0 } do
      named_record
    end

    def to_formatted_s(indent = 0); to_s; end
  end
end
