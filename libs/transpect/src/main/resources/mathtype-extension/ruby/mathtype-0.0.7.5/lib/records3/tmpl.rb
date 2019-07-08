require_relative "snapshot"

# TMPL record (3):

# Consists of:
# record type (3)
# options
# [nudge] if mtefOPT_NUDGE is set
# [selector] template selector code
# [variation] template variation code (1 or 2 bytes; see below)
# [options] template-specific options
# [subobject list] either a single character (e.g. sigma in a summation) and/or lines
# The template selector and variation codes determine the class of the
# template and various properties of the template, such as which subobjects
# can be deleted by the user (see Templates). The class of a template
# determines the order and meaning of each of its subobjects (see Template
# subobject order).

# The variation code may be 1 or 2 bytes long. If the first byte value has the
# high bit set (0x80), the next byte is read and combined with the first
# according to this formula:

# variation code = (byte1 & 0x7F)  |  (byte2 << 8)

# Template-specific options field only used for integrals & fence templates:
# Fence template option field values (fence alignment):
# 0 center fence on math axis, place math axis of contents on math axis of
# containing line (default);
# 1 center fence on contents, place math axis of contents on math axis of
# containing line;
# 2 center fence on contents, center contents on math axis of containing line.
# Warning: the expanding integral property is duplicated in the integral
# templates variation codes (see Limit variations). On reading, MathType only
# looks at the variation code.

# Integral template option field values:
# 0 fixed-size integral;
# 1 the integral expands vertically to fit its contents.

# Limit variations:

# The following variation codes apply to all templates whose class is BigOpBoxClass or LimBoxClass:

# variation bits  symbol  description
# 0x0001  tvBO_LOWER  lower limit is present
# 0x0002  tvBO_UPPER  upper limit is present
# 0x0040  tvBO_SUM  summation-style limit positions, else integral-style

# Template selectors and variations:
# Fences (parentheses, etc.):
# selector  symbol  description class
# 0 tmANGLE angle brackets  ParBoxClass
# 1 tmPAREN parentheses ParBoxClass
# 2 tmBRACE braces (curly brackets) ParBoxClass
# 3 tmBRACK square brackets ParBoxClass
# 4 tmBAR vertical bars ParBoxClass
# 5 tmDBAR  double vertical bars  ParBoxClass
# 6 tmFLOOR floor brackets  ParBoxClass
# 7 tmCEILING ceiling brackets  ParBoxClass
# 8 tmOBRACK  open (white) brackets ParBoxClass
# variations  variation bits  symbol  description
# 0x0001  tvFENCE_L left fence is present
# 0x0002  tvFENCE_R right fence is present
# Intervals:
# selector  symbol  description class
# 9 tmINTERVAL  unmatched brackets and parentheses  ParBoxClass
# variations  variation bits  symbol  description
# 0x0000  tvINTV_LEFT_LP  left fence is left parenthesis
# 0x0001  tvINTV_LEFT_RP  left fence is right parenthesis
# 0x0002  tvINTV_LEFT_LB  left fence is left bracket
# 0x0003  tvINTV_LEFT_RB  left fence is right bracket
# 0x0000  tvINTV_RIGHT_LP right fence is left parenthesis
# 0x0010  tvINTV_RIGHT_RP right fence is right parenthesis
# 0x0020  tvINTV_RIGHT_LB right fence is left bracket
# 0x0030  tvINTV_RIGHT_RB right fence is right bracket
# Radicals (square and nth roots):
# selector  symbol  description class
# 10  tmROOT  radical RootBoxClass
# variations  variation symbol  description
# 0 tvROOT_SQ square root
# 1 tvROOT_NTH  nth root
# Fractions:
# selector  symbol  description class
# 11  tmFRACT fractions FracBoxClass
# variations  variation bits  symbol  description
# 0x0001  tvFR_SMALL  subscript-size slots (piece fraction)
# 0x0002  tvFR_SLASH  fraction bar is a slash
# 0x0004  tvFR_BASE num. and denom. are baseline aligned
# Over and Underbars:
# selector  symbol  description class
# 12  tmUBAR  underbar  BarBoxClass
# 13  tmOBAR  overbar BarBoxClass
# variations  variation bits  symbol  description
# 0x0001  tvBAR_DOUBLE  bar is doubled, else single
# Arrows:
# selector  symbol  description class
# 14  tmARROW arrow ArroBoxClass
# variations  variation symbol  description
# 0x0000  tvAR_SINGLE single arrow
# 0x0001  tvAR_DOUBLE double arrow
# 0x0002  tvAR_HARPOON  harpoon
# 0x0004  tvAR_TOP  top slot is present
# 0x0008  tvAR_BOTTOM bottom slot is present
# 0x0010  tvAR_LEFT if single, arrow points left
# 0x0020  tvAR_RIGHT  if single, arrow points right
# 0x0010  tvAR_LOS  if double or harpoon, large over small
# 0x0020  tvAR_SOL  if double or harpoon, small over large
# Integrals (see Limit Variations):
# selector  symbol  description class
# 15  tmINTEG integral  BigOpBoxClass
# variations  variation symbol  description
# 0x0001  tvINT_1 single integral sign
# 0x0002  tvINT_2 double integral sign
# 0x0003  tvINT_3 triple integral sign
# 0x0004  tvINT_LOOP  has loop w/o arrows
# 0x0008  tvINT_CW_LOOP has clockwise loop
# 0x000C  tvINT_CCW_LOOP  has counter-clockwise loop
# 0x0100  tvINT_EXPAND  integral signs expand
# Sums, products, coproducts, unions, intersections, etc. (see Limit Variations):
# selector  symbol  description class
# 16  tmSUM sum BigOpBoxClass
# 17  tmPROD  product BigOpBoxClass
# 18  tmCOPROD  coproduct BigOpBoxClass
# 19  tmUNION union BigOpBoxClass
# 20  tmINTER intersection  BigOpBoxClass
# 21  tmINTOP integral-style big operator BigOpBoxClass
# 22  tmSUMOP summation-style big operator  BigOpBoxClass
# Limits (see Limit Variations):
# selector  symbol  description class
# 23  tmLIM limits  LimBoxClass
# variations  variation symbol  description
# 0 tvSUBAR single underbar
# 1 tvDUBAR double underbar
# Horizontal braces and brackets:
# selector  symbol  description class
# 24  tmHBRACE  horizontal brace  HFenceBoxClass
# 25  tmHBRACK  horizontal bracket  HFenceBoxClass
#   variation symbol  description
# 0x0001  tvHB_TOP  slot is on the top, else on the bottom
# Long division:
# selector  symbol  description class
# 26  tmLDIV  long division LDivBoxClass
#   variation symbol  description
# 0x0001  tvLD_UPPER  upper slot is present
# Subscripts and superscripts:
# selector  symbol  description class
# 27  tmSUB subscript ScrBoxClass
# 28  tmSUP superscript ScrBoxClass
# 29  tmSUBSUP  subscript and superscript ScrBoxClass
#   variation symbol  description
# 0x0001  tvSU_PRECEDES script precedes scripted item,
# else follows
# Dirac bra-ket notation:
# selector  symbol  description class
# 30  tmDIRAC bra-ket notation  DiracBoxClass
#   variation symbol  description
# 0x0001  tvDI_LEFT left part is present
# 0x0002  tvDI_RIGHT  right part is present
# Vectors:
# selector  symbol  description class
# 31  tmVEC vector  HatBoxClass
#   variation symbol  description
# 0x0001  tvVE_LEFT arrow points left
# 0x0002  tvVE_RIGHT  arrow points right
# 0x0004  tvVE_UNDER  arrow under slot, else over slot
# 0x0008  tvVE_HARPOON  harpoon
# Hats, arcs, tilde, joint status:
# selector  symbol  description class
# 32  tmTILDE tilde over characters HatBoxClass
# 33  tmHAT hat over characters HatBoxClass
# 34  tmARC arc over characters HatBoxClass
# 35  tmJSTATUS joint status construct  HatBoxClass
# Overstrikes (cross-outs):
# selector  symbol  description class
# 36  tmSTRIKE  overstrike (cross-out)  StrikeBoxClass
#   variation symbol  description
# 0x0001  tvST_HORIZ  line is horizontal, else slashes
# 0x0002  tvST_UP if slashes, slash from lower-left to upper-right is present
# 0x0004  tvST_DOWN if slashes, slash from upper-left to lower-right is present
# Boxes:
# selector  symbol  description class
# 37  tmBOX box TBoxBoxClass
#   variation symbol  description
# 0x0001  tvBX_ROUND  corners are round, else square
# 0x0002  tvBX_LEFT left side is present
# 0x0004  tvBX_RIGHT  right side is present
# 0x0008  tvBX_TOP  top side is present
# 0x0010  tvBX_BOTTOM bottom side is present

module Mathtype3
  class RecordTmpl < BinData::Record
    include Snapshot
    EXPOSED_IN_SNAPSHOT = %i(options selector variation template_specific_options nudge
      subobject_list)

    SCRIPT_SELECTOR = {
      0 => "tmSUP",
      1 => "tmSUB",
      2 => "tmSUBSUP"
    }

    SELECTORS = {
      0 => "tmANGLE",
      1 => "tmPAREN",
      2 => "tmBRACE",
      3 => "tmBRACK",
      4 => "tmBAR",
      5 => "tmDBAR",
      6 => "tmFLOOR",
      7 => "tmCEILING",
      8 => "tmINTERVAL",
      9 => "tmINTERVAL",
      10 => "tmINTERVAL",
      11 => "tmINTERVAL",
      12 => "tmINTERVAL",
      13 => "tmROOT",
      14 => "tmFRACT",
      15 => "tmSCRIPT",
      16 => "tmUBAR",
      17 => "tmOBAR",
      18 => "tmARROW",
      19 => "tmARROW",
      20 => "tmARROW",
      21 => "tmINTEG",
      22 => "tmINTEG",
      23 => "tmINTEG",
      24 => "tmINTEG",
      25 => "tmINTEG",
      26 => "tmINTEG",
      27 => "tmHBRACE",
      28 => "tmHBRACE",
      29 => "tmSUM",
      30 => "tmSUM",
      31 => "tmPROD",
      32 => "tmPROD",
      33 => "tmCOPROD",
      34 => "tmCOPROD",
      35 => "tmUNION",
      36 => "tmUNION",
      37 => "tmINTER",
      38 => "tmINTER",
      39 => "tmLIM",
      40 => "tmLDIV",
      41 => "tmFRACT",
      42 => "tmINTOP",
      43 => "tmSUMOP",
      44 => "tmSCRIPT",
      45 => "tmDIRAC",
      46 => "tmVEC",
      47 => "tmVEC",
      48 => "tmBOX"
    }
    FENCE_BOTH = [
                  "tvFENCE_L",
                  "tvFENCE_R"
                 ]

    FENCE_VARIATIONS = {
      0 => FENCE_BOTH,
      1 => ["tvFENCE_L"], # left fence is present
      2 => ["tvFENCE_R"] # right fence is present
    }

    BAR_VARIATIONS = {
      0 => ["tvBAR_SINGLE"],
      1 => ["tvBAR_DOUBLE"] #  bar is doubled, else single
    }


    VARIATIONS = {
      # Fences (parentheses, etc.):
      0 => FENCE_VARIATIONS,
      1 => FENCE_VARIATIONS,
      2 => FENCE_VARIATIONS,
      3 => FENCE_VARIATIONS,
      4 => FENCE_VARIATIONS,
      5 => FENCE_VARIATIONS,
      6 => {
        0 => FENCE_BOTH
      },
      7 => {
        0 => FENCE_BOTH
      },
      # Intervals:
      # 0x0000 => "tvINTV_LEFT_LP", #  left fence is left parenthesis
      # 0x0001 => "tvINTV_LEFT_RP", #  left fence is right parenthesis
      # 0x0002 => "tvINTV_LEFT_LB", #  left fence is left bracket
      # 0x0003 => "tvINTV_LEFT_RB", #  left fence is right bracket
      # 0x0004 => "tvINTV_RIGHT_LP", # right fence is left parenthesis # WARNING: DOCUMENTATION SAYS 0x0000?
      # 0x0010 => "tvINTV_RIGHT_RP", # right fence is right parenthesis
      # 0x0020 => "tvINTV_RIGHT_LB", # right fence is left bracket
      # 0x0030 => "tvINTV_RIGHT_RB", # right fence is right bracket
      # Replaced above to match MathML translator
      8 => {
        0 => FENCE_BOTH
      },
      9 => {
        0 => FENCE_BOTH
      },
      10 => {
        0 => FENCE_BOTH
      },
      11 => {
        0 => FENCE_BOTH
      },
      12 => {
        0 => FENCE_BOTH
      },

      # Radicals (square and nth roots):
      13 => {
        0 => ["tvROOT_SQ"], # square root
        1 => ["tvROOT_NTH"] # nth root
      },

      # Fractions:
      14 => {
        #TODO: full-size slots?
        0 => ["tvFR_FULL"],
        1 => ["tvFR_SMALL"] #  subscript-size slots (piece fraction)
      },

      # Over and Underbars:
      16 => BAR_VARIATIONS,
      17 => BAR_VARIATIONS,

      # Arrows:
      18 => {
        0 => [
          "tvAR_SINGLE",
          "tvAR_LEFT",
          "tvAR_TOP",
        ], # single arrow
        1 => [
          "tvAR_SINGLE",
          "tvAR_LEFT",
          "tvAR_BOTTOM"
        ]
      },

      19 => {
        0 => [
          "tvAR_SINGLE",
          "tvAR_RIGHT",
          "tvAR_TOP"
        ],
        1 => [
          "tvAR_SINGLE",
          "tvAR_RIGHT",
          "tvAR_TOP"
        ]
      },

      20 => {
        0 => [
          "tvAR_SINGLE",
          "tvAR_RIGHT",
          "tvAR_LEFT",
          "tvAR_TOP"
        ],
        1 => [
          "tvAR_SINGLE",
          "tvAR_RIGHT",
          "tvAR_LEFT",
          "tvAR_TOP"
        ]
      },

      # Integrals (see Limit Variations):
      21 => {
        0 => [
          "tvINT_1" # single integral sign
        ],
        1 => [
          "tvINT_1",
          "tvBO_LOWER"
        ],
        2 => [
          "tvINT_1",
          "tvBO_LOWER",
          "tvBO_UPPER"
        ],
        3 => [
          "tvINT_1",
        ],
        4 => [
          "tvINT_1",
          "tvBO_LOWER"
        ]
      },

      22 => {
        0 => [
          "tvINT_2" # double integral sign
        ],
        1 => [
          "tvINT_2",
          "tvBO_LOWER"
        ],
        2 => [
          "tvINT_2",
          "tvBO_LOWER",
          "tvBO_UPPER"
        ],
        3 => [
          "tvINT_2",
        ],
        4 => [
          "tvINT_2",
          "tvBO_LOWER"
        ]
      },

      23 => {
        0 => [
          "tvINT_3" # triple integral sign
        ],
        1 => [
          "tvINT_3",
          "tvBO_LOWER"
        ],
        2 => [
          "tvINT_3",
          "tvBO_LOWER",
          "tvBO_UPPER"
        ],
        3 => ["tvINT_3"],
        4 => [
          "tvINT_3",
          "tvBO_LOWER"
        ]
      },

      24 => {
        0 => [
          "tvINT_1", # single integral sign
          "tvBO_SUM",
          "tvBO_LOWER",
          "tvBO_UPPER"
        ],
        1 => [
          "tvINT_1",
          "tvBO_SUM",
          "tvBO_LOWER"
        ],
        2 => [
          "tvINT_1",
          "tvBO_SUM",
          "tvBO_LOWER",
        ]
      },

      25 => {
        0 => [
          "tvINT_2", # double integral sign
          "tvBO_SUM",
          "tvBO_LOWER",
        ],
        1 => [
          "tvINT_2",
          "tvBO_SUM",
          "tvBO_LOWER"
        ]
      },

      26 => {
        0 => [
          "tvINT_3", # triple integral sign
          "tvBO_SUM",
          "tvBO_LOWER"
        ],
        1 => [
          "tvINT_3",
          "tvBO_SUM",
          "tvBO_LOWER"
        ]
      },

      # Horizontal braces and brackets:
      27 => {
        0 => ["tvHB_TOP"] # slot is on the top
      },

      28 => {
        #  default on the bottom
        0 => ["tvHB_BOT"]
      },

      # Sums, products, coproducts, unions, intersections, etc.:
      29 => {
        0 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_SUM" # summation-style limit positions,
        ],
        1 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER", # upper limit is present
          "tvBO_SUM"
        ],
        2 => ["tvBO_SUM"]
      },

      30 => {
        0 => [
          "tvBO_LOWER" # lower limit is present
        ],
        1 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER" # upper limit is present
        ]
      },

      31 => {
        0 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_SUM" # summation-style limit positions,
        ],
        1 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER", # upper limit is present
          "tvBO_SUM"
        ],
        2 => ["tvBO_SUM"]
      },

      32 => {
        0 => [
          "tvBO_LOWER" # lower limit is present
        ],
        1 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER" # upper limit is present
        ]
      },

      33 => {
        0 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_SUM" # summation-style limit positions,
        ],
        1 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER", # upper limit is present
          "tvBO_SUM"
        ],
        2 => ["tvBO_SUM"]
      },

      34 => {
        0 => [
          "tvBO_LOWER" # lower limit is present
        ],
        1 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER" # upper limit is present
        ]
      },

      35 => {
        0 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_SUM" # summation-style limit positions,
        ],
        1 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER", # upper limit is present
          "tvBO_SUM"
        ],
        2 => ["tvBO_SUM"]
      },

      36 => {
        0 => [
          "tvBO_LOWER" # lower limit is present
        ],
        1 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER" # upper limit is present
        ]
      },

      37 => {
        0 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_SUM" # summation-style limit positions,
        ],
        1 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER", # upper limit is present
          "tvBO_SUM"
        ],
        2 => ["tvBO_SUM"]
      },

      38 => {
        0 => [
          "tvBO_LOWER" # lower limit is present
        ],
        1 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER" # upper limit is present
        ]
      },

      39 => {
        0 => [
          "tvBO_UPPER" # upper limit is present
        ],
        1 => [
          "tvBO_LOWER" # lower limit is present
        ],
        2 => [
          "tvBO_LOWER", # lower limit is present
          "tvBO_UPPER" # upper limit is present
        ]
      },

      # Long division:
      40 => {
        0 => ["tvLD_UPPER"]
      },

      # Frac with Slash:
      41 => {
        0 => ["tvFR_SLASH"],
        1 => [
              "tvFR_SLASH",
              "tvFR_BASE"
             ],
        2 => [
              "tvFR_SLASH",
              "tvFR_SMALL"
             ]
      },

      # Big integral-style operators:
      42 => {
        0 => ["tvBO_LOWER"],
        1 => ["tvBO_UPPER"],
        2 => [
              "tvBO_LOWER",
              "tvBO_UPPER"
             ]
      },

      # Big summation-style operators:
      43 => {
        0 => [
              "tvBO_SUM",
              "tvBO_LOWER"
             ],
        1 => [
              "tvBO_SUM",
              "tvBO_UPPER"
             ],
        2 => [
              "tvBO_SUM",
              "tvBO_LOWER",
              "tvBO_UPPER"
             ]
      },

      44 => {
        0 => ["tvSU_PRECEDES"],
        1 => ["tvSU_PRECEDES"],
        2 => ["tvSU_PRECEDES"],
        3 => ["tvSU_PRECEDES"]
      },

      45 => {
        0 => [
              "tvDI_LEFT",
              "tvDI_RIGHT"
             ],
        1 => ["tvDI_LEFT"],
        2 => ["tvDI_RIGHT"]
      },

      46 => {
        0 => [
              "tvVE_UNDER",
              "tvVE_LEFT"
              ],
        1 => [
              "tvVE_UNDER",
              "tvVE_RIGHT"
              ],
        2 => [
              "tvVE_UNDER",
              "tvVE_LEFT",
              "tvVE_RIGHT"
              ],
      },

      47 => {
        0 => ["tvVE_LEFT"],
        1 => ["tvVE_RIGHT"],
        2 => [
              "tvVE_LEFT",
              "tvVE_RIGHT"
              ],
      },

      # Selectors with empty variation:
      15 => {0=>nil,1=>nil,2=>nil,3=>nil,4=>nil},
      48 => {0=>nil,1=>nil,2=>nil,3=>nil,4=>nil}
    }


    # When options overlap in the binary space, ordinary bitmasks
    # are not the correct tool to use for detection. We use digit
    # position and presence instead.

    DIGIT_MODE_VARIATIONS = [9]

    # Top-level keys are template identifiers, defined in TEMPLATES.
    # Second-level keys are bits for certain variations, negative keys mean
    # that the variation is present if the bit is absent.

    mandatory_parameter :_options

    virtual :options, :value => lambda{ _options }

    record_nudge :nudge, onlyif: lambda { _options & OPTIONS["xfLMOVE"] > 0 }

    int8 :_selector

    int8 :_variation

    int8 :template_specific_options

    array :subobject_list, read_until: lambda { element.record_type == 0 } do
      named_record
    end

    def selector
      if is_script _selector 
          #SCRIPT has to be handled separate
        SCRIPT_SELECTOR[_variation]
      else
        SELECTORS[_selector]
      end
    end
    def is_script(sel)
      return sel == 44 || sel == 15
    end
  	 def variation
      VARIATIONS[_selector][_variation]
    end
  end
end
