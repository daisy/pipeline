module Mathtype
  class Mt_uint < BinData::Primitive
    uint8 :_first
    uint8 :_second, onlyif: lambda { _first == 0xFF }
    uint8 :_third, onlyif: lambda { _first == 0xFF }

    def get
      _first < 0xFF ?
        _first :
        _third << 0x8 | _second
    end
  end
end

