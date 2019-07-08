module Mathtype5
  class RecordEnd < BinData::Primitive
    def get; ""; end
    def set(v); end

    def to_formatted_s(indent = 0); to_s; end
  end
end
