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

module Mathtype5
  class RecordTmpl < BinData::Record
    include Snapshot
    EXPOSED_IN_SNAPSHOT = %i(selector variation template_specific_options nudge
      subobject_list)

    SELECTORS = {
      # Fences (parentheses, etc.):
      0 => "tmANGLE",
      1 => "tmPAREN",
      2 => "tmBRACE",
      3 => "tmBRACK",
      4 => "tmBAR",
      5 => "tmDBAR",
      6 => "tmFLOOR",
      7 => "tmCEILING",
      8 => "tmOBRACK",
      # Intervals:
      9 => "tmINTERVAL",
      # Radicals (square and nth roots):
      10 => "tmROOT",
      # Fractions:
      11 => "tmFRACT",
      # Over and Underbars:
      12 => "tmUBAR",
      13 => "tmOBAR",
      # Arrows:
      14  => "tmARROW",
      # Integrals (see Limit Variations):
      15  => "tmINTEG",
      # Sums, products, coproducts, unions, intersections, etc. (see Limit Variations):
      16 => "tmSUM",
      17 => "tmPROD",
      18 => "tmCOPROD",
      19 => "tmUNION",
      20 => "tmINTER",
      21 => "tmINTOP",
      22 => "tmSUMOP",
      # Limits (see Limit Variations):
      23 => "tmLIM",
      # Horizontal braces and brackets:
      24 => "tmHBRACE",
      25 => "tmHBRACK",
      # Long division:
      26 => "tmLDIV",
      # Subscripts and superscripts:
      27 => "tmSUB",
      28 => "tmSUP",
      29 => "tmSUBSUP",
      # Dirac bra-ket notation:
      30 => "tmDIRAC",
      # Vectors:
      31 => "tmVEC",
      # Hats, arcs, tilde, joint status:
      32 => "tmTILDE",
      33 => "tmHAT",
      34 => "tmARC",
      35 => "tmJSTATUS",
      # Overstrikes (cross-outs):
      36 => "tmSTRIKE",
      # Boxes:
      37 => "tmBOX"
    }


    # When options overlap in the binary space, ordinary bitmasks
    # are not the correct tool to use for detection. We use digit
    # position and presence instead.

    DIGIT_MODE_VARIATIONS = [9]

    # Top-level keys are template identifiers, defined in TEMPLATES.
    # Second-level keys are bits for certain variations, negative keys mean
    # that the variation is present if the bit is absent.

    VARIATIONS = {
      # Fences (parentheses, etc.):
      0..8 => {
        0x0001 => "tvFENCE_L", # left fence is present
        0x0002 => "tvFENCE_R", # right fence is present
      },

      # Intervals:
      9 => {
        # 0x0000 => "tvINTV_LEFT_LP", #  left fence is left parenthesis
        # 0x0001 => "tvINTV_LEFT_RP", #  left fence is right parenthesis
        # 0x0002 => "tvINTV_LEFT_LB", #  left fence is left bracket
        # 0x0003 => "tvINTV_LEFT_RB", #  left fence is right bracket
        # 0x0004 => "tvINTV_RIGHT_LP", # right fence is left parenthesis # WARNING: DOCUMENTATION SAYS 0x0000?
        # 0x0010 => "tvINTV_RIGHT_RP", # right fence is right parenthesis
        # 0x0020 => "tvINTV_RIGHT_LB", # right fence is left bracket
        # 0x0030 => "tvINTV_RIGHT_RB", # right fence is right bracket
        # Replaced above to match MathML translator
        0x0002 => {
          0x0020 => "tvINTV_LBLB", # left bracket, left bracket
          0x0010 => "tvINTV_LBRP", # left bracket, right parenthesis
        },
        0x0003 => {
          0x0030 => "tvINTV_RBRB", # right bracket, right bracket
          0x0020 => "tvINTV_RBLB", # right bracket, left bracket
        },
        0x0000 => {
          0x0030 => "tvINTV_LPRB", # left parenthesis, right bracket
        }
      },

      # Radicals (square and nth roots):
      10 => {
        0 => "tvROOT_SQ", # square root
        1 => "tvROOT_NTH", # nth root
      },

      # Fractions:
      11 => {
        0x0001 => "tvFR_SMALL", #  subscript-size slots (piece fraction)
        0x0002 => "tvFR_SLASH", #  fraction bar is a slash
        0x0004 => "tvFR_BASE", # num. and denom. are baseline aligned
      },

      # Over and Underbars:
      12..13 => {
        0x0001 => "tvBAR_DOUBLE", #  bar is doubled, else single
      },

      # Arrows:
      14 => {
        0x0000 => "tvAR_SINGLE", # single arrow
        0x0001 => "tvAR_DOUBLE", # double arrow
        0x0010 => {
          0x0001 => "tvAR_LOS", # if double, large over small,
          0x0002 => "tvAR_LOS", # if harpoon, large over small,
          0x0010 => "tvAR_LEFT" # if single, arrow points left
        },
        0x0020 => {
          0x0001 => "tvAR_SOL", # if double, small over large
          0x0002 => "tvAR_SOL", # if harpoon, small over large
          0x0010 => "tvAR_RIGHT", # if single, arrow points right
        },
        0x0002 => "tvAR_HARPOON", # harpoon
        0x0004 => { # top slot is present
          0x0008 => "tvAR_TOPBOTTOM", # both slots are present
          0x0004 => "tvAR_TOP"
        },
        0x0008 => { # bottom slot is present
          0x0004 => "tvAR_TOPBOTTOM", # both slots are present
          0x0008 => "tvAR_BOTTOM"
        }
      },

      # Integrals (see Limit Variations):
      15 => {
        0x0001 => "tvINT_1", # single integral sign
        0x0002 => "tvINT_2", # double integral sign
        0x0003 => "tvINT_3", # triple integral sign
        0x0004 => "tvINT_LOOP", # has loop w/o arrows
        0x0008 => "tvINT_CW_LOOP", # has clockwise loop
        0x000C => "tvINT_CCW_LOOP", # has counter-clockwise loop
        0x0100 => "tvINT_EXPAND", #  integral signs expand
      },

      # Limit variations
      15..23 => {
        0x0010 => "tvBO_LOWER", # lower limit is present
        0x0020 => "tvBO_UPPER", # upper limit is present
        0x0040 => "tvBO_SUM", # summation-style limit positions,
        -0x0040 => "tvBO_INT" # else integral-style
      },

      # Sums, products, coproducts, unions, intersections, etc. (see Limit Variations):
      23 => {
        0 => "tvSUBAR", # single underbar
        1 => "tvDUBAR", # double underbar
      },

      # Horizontal braces and brackets:
      24..25 => {
        0x0001 => "tvHB_TOP", #  slot is on the top, else on the bottom
      },

      # Long division:
      26 => {
        0x0001 => "tvLD_UPPER", #  upper slot is present
      },

      # Subscripts and superscripts:
      27..29 => {
        0x0001 => "tvSU_PRECEDES", # script precedes scripted item, else follows
      },

      # Dirac bra-ket notation:
      30 => {
        0x0001 => "tvDI_LEFT", # left part is present
        0x0002 => "tvDI_RIGHT", #  right part is present
      },

      # Vectors:
      31 => {
        0x0001 => "tvVE_LEFT", # arrow points left
        0x0002 => "tvVE_RIGHT", #  arrow points right
        0x0004 => "tvVE_UNDER", #  arrow under slot, else over slot
        0x0008 => "tvVE_HARPOON", #  harpoon
      },

      # Hats, arcs, tilde, joint status:
      32..35 => {},

      # Overstrikes (cross-outs):
      36 => {
        0x0001 => "tvST_HORIZ", #  line is horizontal, else slashes
        0x0002 => "tvST_UP", # if slashes, slash from lower-left to upper-right is present
        0x0004 => "tvST_DOWN", # if slashes, slash from upper-left to lower-right is present
      },

      # Boxes:
      37 => {
        0x0001 => "tvBX_ROUND", #  corners are round, else square
        0x0002 => "tvBX_LEFT", # left side is present
        0x0004 => "tvBX_RIGHT", #  right side is present
        0x0008 => "tvBX_TOP", #  top side is present
        0x0010 => "tvBX_BOTTOM", # bottom side is present
      }
    }

    int8 :options

    record_nudge :nudge, onlyif: lambda { options & OPTIONS["mtefOPT_NUDGE"] > 0 }

    int8 :_selector

    int8 :_variation_first_byte
    int8 :_variation_second_byte, onlyif: (lambda do
      _variation_first_byte & 0x80 > 0
    end)

    int8 :template_specific_options

    array :subobject_list, read_until: lambda { element.record_type == 0 } do
      named_record
    end

    def variation
      @variation = (_variation_first_byte & 0x7F) | (_variation_second_byte << 8)
      @variations = VARIATIONS.select do |selector, _|
        selector === _selector
      end.values.reduce(Hash.new, :merge)

      process_variations
    end

    def process_variations
      @variations.select do |flag, _|
        if flag < 0 # flag should NOT be active
          !check_flag(-flag)
        else # flag should be active
          check_flag(flag)
        end
      end.map do |flag, value|
        case value
        when Hash # Conditional variations
          result = value.detect do |conditional, _|
            check_flag(conditional)
          end
          result.last if result
        else
          value
        end
      end.uniq
    end

    def check_flag(flag)
      case mode
      when :bitmask
        check_bitmask(flag)
      when :digit
        check_digit(flag)
      end
    end

    def check_bitmask(flag)
      if flag == 0
        @variation & 0xf == 0
      else
        @variation & flag == flag
      end
    end

    # E.g. is 0x3 present in 0x33
    def check_digit(flag)
      digits = if flag == 0
        1
      else
        (Math.log(flag+1)/Math.log(16)).ceil # digits in a hex number
      end
      mask = (15<<(4*digits-4)) # e.g. 0xf0
      variation_digit = (@variation & mask) >> (digits * 4 - 4)
      flag_digit = (flag & mask) >> (digits * 4 - 4)
      variation_digit == flag_digit
    end


    def mode
      DIGIT_MODE_VARIATIONS.include?(_selector) ? :digit : :bitmask
    end

    def selector
      SELECTORS[_selector]
    end
  end
end
