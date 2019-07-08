require "bindata"

module Mathtype3
  module Snapshot
    def snapshot
      snapshot = BinData::Record::Snapshot.new
      exposed = self.class.const_get(:EXPOSED_IN_SNAPSHOT)
      exposed.each do |name|
        obj = find_obj_for_name(name)
        if obj
          snapshot[name] = obj.snapshot if include_obj?(obj)
        else
          snapshot[name] = self.send(name)
        end
      end
      snapshot
    end
  end
end
