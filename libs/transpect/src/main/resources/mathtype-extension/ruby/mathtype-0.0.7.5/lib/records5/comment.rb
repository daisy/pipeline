# If the record type is 100 or greater, it represents a record that will be
# defined in a future version of MTEF. For now, readers can assume that an
# unsigned integer follows the record type and is the number of bytes following
# it in the record (i.e. it doesn't include the record type and length). This
# makes it easy for software that reads MTEF to skip these records. Although it
# might be handy if all records had such a length value, it will only be present
# on future expansion records (i.e. those with record types >= 100).

module Mathtype5
  class RecordMtComment < BinData::Record
    mt_uint :comment_length
    stringz :comment_type
    stringz :comment_data
  end
end
