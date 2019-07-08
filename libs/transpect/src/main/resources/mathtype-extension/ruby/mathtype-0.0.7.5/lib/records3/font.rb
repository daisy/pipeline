require_relative "snapshot"

module Mathtype3
  class RecordFont < BinData::Record
    include Snapshot
    EXPOSED_IN_SNAPSHOT = %i(options name typeface style)

    endian :little

    mandatory_parameter :_options

    virtual :_tag_options, :value => lambda{ _options }

    int8 :_typeface

    int8 :style

    stringz :name
    def typeface
      _typeface + 128
    end

    def options
      _tag_options
    end
  end
end
