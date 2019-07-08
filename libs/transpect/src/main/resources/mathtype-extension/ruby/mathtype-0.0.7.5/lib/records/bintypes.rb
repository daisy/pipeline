module Mathtype
  class Mtef16 < BinData::Primitive
    uint8 :low, :initial_value => 0
    uint8 :high, :initial_value => 0

    def get; (high << 8) + low end
    def getlow
      self.low
    end
    def gethigh
      self.high
    end
    def set(v)
      self.low = v & 0xFF
      self.high = (v & 0xFF00) >> 8
    end
  end
end
