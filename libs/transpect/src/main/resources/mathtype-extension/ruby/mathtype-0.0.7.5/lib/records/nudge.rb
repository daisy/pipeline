# Nudge values:
# LINE, CHAR, TMPL, PILE, MATRIX, and EMBELL records may store the result of
# nudging (small offsets applied by the user). A nudged record has the
# mtefOPT_NUDGE option (0x8) and the option byte is followed immediately by
# the nudge offset. The nudge offset consists of either two bytes or six,
# depending on the amount of offset. If -128 <= dx < +128 and
# -128 <= dy < +128, then the offsets are stored as two bytes, dx followed
# by dy, where each value has 128 added to it before it is written. Otherwise,
# two bytes of 128 are stored, followed by the offsets, dx and dy, stored as
# 16-bit values, low byte followed by high byte.

require_relative "snapshot"

module Mathtype
  class RecordNudge < BinData::Record
    include Snapshot
    EXPOSED_IN_SNAPSHOT = %i(dx dy)

    endian :little

    int8 :_small_dx
    int8 :_small_dy
    mtef16 :_large_dx, :onlyif => lambda { has_large_offset }
    mtef16 :_large_dy, :onlyif => lambda { has_large_offset }

    def dx
      has_large_offset ? _large_dx : (_small_dx - 128)
    end

    def dy
      has_large_offset ? _large_dy : (_small_dy - 128)
    end

    def has_large_offset
      _small_dx == -128 and _small_dy == -128
    end
  end
end
