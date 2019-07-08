require_relative "snapshot"

# SIZE record (9):
# Consists of one of the following cases:

# if lsize < 0 (explicit point size):
# record type (9)
# 101
# -point size (16 bit integer)
# else if -128 < dsize < +128:
# record type (9)
# lsize (typesize)
# dsize + 128
# else: (large delta)
# record type (9)
# 100
# lsize (typesize)
# dsize (16 bit integer)
# Sizes in MathType are represented as a pair of values, lsize and dsize. Lsize
# stands for "logical size", dsize for "delta size". If it is negative, it is an
# explicit point size (in 32nds of a point) negated and dsize is ignored.
# Otherwise, lsize is a typesize value and dsize is a delta from that size:
# Simple typesizes, without a delta value, are written using the records
# described in the next section.

module Mathtype5
  class RecordSize < BinData::Record
    include Snapshot
    EXPOSED_IN_SNAPSHOT = %i(lsize dsize point_size)

    endian :little
    int8 :_size_select

    mtef16 :_point_size, onlyif: lambda { _size_select == 101 }
    uint8 :_lsize_large_delta, onlyif: lambda { _size_select == 100 }

    uint8 :_dsize, onlyif: lambda { _size_select != 100 && _size_select != 101 }
    mtef16 :_dsize_large, onlyif: lambda { _size_select == 100 }

    def dsize
      __dsize = nil
      case _size_select
      when 100
        if _dsize_large > 255
         __dsize = -(_dsize_large.getlow)
        else
         __dsize = _dsize_large
        end
      when 101
        __dsize = _dsize
      else
        __dsize = (_dsize - 128) # in 32nds of a point
      end
      __dsize / 32
    end

    def lsize
      case _size_select
      when 100
        _lsize_large_delta
      when 101
        nil
      else
        _size_select
      end
    end

    def point_size
      -_point_size if _point_size != 0
    end
  end
end
