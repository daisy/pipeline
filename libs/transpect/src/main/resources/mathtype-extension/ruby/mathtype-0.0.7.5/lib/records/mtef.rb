require_relative 'snapshot'
require_relative 'bintypes'
require_relative 'end'
require_relative 'nudge'
require_relative 'ruler'
require_relative 'typesizes'

module Mathtype

  HALIGN = {
    1 => "left",
    2 => "center",
    3 => "right",
    4 => "al", # relational
    5 => "dec" # decimal
  }

  VALIGN = {
    0 => "top_baseline",
    1 => "center_baseline",
    2 => "bottom_baseline",
    3 => "center", # vertical centering
    4 => "axis" # math axis (center of +,-, brace points, etc.)
  }

  class Equation < BinData::Record
    endian :little
    uint8 :mtef_version
    uint8 :platform
    uint8 :product
    uint8 :product_version
    uint8 :product_subversion
    include Snapshot
  end

end
