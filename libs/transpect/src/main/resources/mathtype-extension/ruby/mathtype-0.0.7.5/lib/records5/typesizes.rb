# Typesize records (10-14):
# Consists of:
# record type (10-14)
# These records are just short ways of specifying a simple typesize where dsize
# is zero. The tag value represents an lsize + 10. So if the tag value is 10,
# it means equation content following it will be Full size (szFULL), tag value
# 11 means szSUB, and so on. See typesize.

module Mathtype5
  class RecordFull < BinData::Record
  end

  class RecordSub < BinData::Record
  end

  class RecordSub2 < BinData::Record
  end

  class RecordSym < BinData::Record
  end

  class RecordSubsym < BinData::Record
  end
end
