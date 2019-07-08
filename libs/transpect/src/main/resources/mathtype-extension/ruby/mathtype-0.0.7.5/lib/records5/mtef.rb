require_relative 'mtuint'

require_relative "line"
require_relative "embell"
require_relative "char"
require_relative "pile"
require_relative "tmpl"
require_relative "eqn_prefs"
require_relative "font_def"
require_relative "font_style_def"
require_relative "matrix"
require_relative "encoding_def"
require_relative "embell"
require_relative "size"
require_relative "color"
require_relative "color_def"
require_relative "future"
require_relative "comment"

module Mathtype5
  class RecordEnd < Mathtype::RecordEnd; end
  class RecordNudge < Mathtype::RecordNudge; end
  class RecordRuler < Mathtype::RecordRuler; end
  class RecordNudge < Mathtype::RecordNudge; end
  class RecordFull < Mathtype::RecordFull; end
  class RecordSub < Mathtype::RecordSub; end
  class RecordSub2 < Mathtype::RecordSub2; end
  class RecordSym < Mathtype::RecordSym; end
  class RecordSubsym < Mathtype::RecordSubsym; end

  HALIGN = Mathtype::HALIGN
  VALIGN = Mathtype::VALIGN

  RECORD_NAMES = {
    0 => "end",
    1 => "slot",
    2 => "char",
    3 => "tmpl",
    4 => "pile",
    5 => "matrix",
    6 => "embell",
    7 => "ruler",
    8 => "font_style_def",
    9 => "size",
    10 => "full",
    11 => "sub",
    12 => "sub2",
    13 => "sym",
    14 => "subsym",
    15 => "color",
    16 => "color_def",
    17 => "font_def",
    18 => "eqn_prefs",
    19 => "encoding_def",
    100 => "future",
    102 => "mt_comment"
  }

  OPTIONS = {
    # value => symbol                     # description
    # Option flag values for all equation structure records:
    "mtefOPT_NUDGE" => 0x08,              # nudge values follow tag
    # Option flag values for CHAR records:
    "mtefOPT_CHAR_EMBELL" => 0x01,        # character is followed by an embellishment list
    "mtefOPT_CHAR_FUNC_START" => 0x02,    # character starts a function (sin, cos, etc.)
    "mtefOPT_CHAR_ENC_CHAR_8" => 0x04,    # character is written with an 8-bit encoded value
    "mtefOPT_CHAR_ENC_CHAR_16" => 0x10,   # character is written with an 16-bit encoded value
    "mtefOPT_CHAR_ENC_NO_MTCODE" => 0x20, # character is written without an 16-bit MTCode value
    # Option flag values for LINE records:
    "mtefOPT_LINE_NULL" => 0x01,          # line is a placeholder only (i.e. not displayed)
    "mtefOPT_LINE_LSPACE" => 0x04,        # line spacing value follows tag
    # Option flag values for LINE and PILE records:
    "mtefOPT_LP_RULER" => 0x02,           # RULER record follows LINE or PILE record
    # Option flag values for COLOR_DEF records:
    "mtefCOLOR_CMYK" => 0x01,             # color model is CMYK, else RGB
    "mtefCOLOR_SPOT" => 0x02,             # color is a spot color, else a process color
    "mtefCOLOR_NAME" => 0x04,             # color has a name, else no name
  }

  ## Payload is the most important class to understand.
  ## This abstraction allows recursive formats.
  ## eg. lists can contain lists can contain lists.

  class Payload < BinData::Choice
    opt = {:_options => :options}
    record_end 0 # end is a reserved keyword
    record_line 1
    record_char 2
    record_tmpl 3
    record_pile 4
    record_matrix 5
    record_embell 6
    record_ruler 7
    record_font_style_def 8
    record_size 9
    record_full 10
    record_sub 11
    record_sub2 12
    record_sym 13
    record_subsym 14
    record_color 15
    record_color_def 16
    record_font_def 17
    record_eqn_prefs 18
    record_encoding_def 19
    record_future 100
    record_mt_comment 102
  end

  class NamedRecord < BinData::Record
    int8 :record_type
    payload :payload, :onlyif => :not_end_tag?, :selection => :record_type, :options => :record_options

    def not_end_tag?
      record_type != 0
    end
  end

  class Equation < Mathtype::Equation
    EXPOSED_IN_SNAPSHOT = %i(mtef_version platform product product_version
      product_subversion application_key equation_options equation)

    stringz :application_key
    uint8 :_equation_options

    def equation_options
      _equation_options == 1 ? "inline" : "block"
    end

    array :equation, read_until: lambda { element.record_type == 0 } do
      named_record
    end
  end
end
