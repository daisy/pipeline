# COLOR_DEF records (16):
# Consists of:
# record type (16)
# [options] model is RGB unless mtefCOLOR_CMYK bit is set; type is process
# unless mtefCOLOR_SPOT bit is set; color is unnamed unless mtefCOLOR_NAME bit is set
# [color values] if RGB, 3 values (red, green, blue); if CMYK, 4 values (cyan,
# magenta, yellow, black); see below for details
# [name] null-terminated color name appears only if mtefCOLOR_NAME is set
# This record defines a color (see Definition records). Each color value is
# written as a 16-bit integer that ranges between 0 and 1000 where 0 is the
# absence of the color and 1000 is a fully saturated color. So, an RGB color
# definition for black has all three components at 0.

module Mathtype5
  class RecordColorDef < BinData::Record
    endian :little

    int8 :options

    struct :rgb, onlyif: lambda { options & OPTIONS["mtefCOLOR_CMYK"] == 0 } do
      mtef16 :r
      mtef16 :g
      mtef16 :b
    end

    struct :cmyk, onlyif: lambda { options & OPTIONS["mtefCOLOR_CMYK"] > 0} do
      mtef16 :c
      mtef16 :m
      mtef16 :y
      mtef16 :k
    end

    stringz :color_name, onlyif: (lambda do
      options & OPTIONS["mtefCOLOR_NAME"] > 0
    end)
  end
end
