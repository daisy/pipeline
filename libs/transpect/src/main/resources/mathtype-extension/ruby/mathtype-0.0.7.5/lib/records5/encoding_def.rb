# ENCODING_DEF records (19):
# Consists of:
# record type (19)
# [name] null-terminated encoding name
# This record defines (see Definition records) a font encoding and is referred
# to by a FONT_DEF record. In order to reduce the size of the MTEF stream,
# the following 4 encodings are predefined:

# ENCODING_DEF index  encoding name
# 1 MTCode
# 2 Unknown
# 3 Symbol
# 4 MTExtra
# This means that the first ENCODING_DEF record in the MTEF stream is considered
# to have an index of 5. See Extending MathType's Font and Character Information
# and MathType's Character Encodings for more information on font encodings.

module Mathtype5
  class RecordEncodingDef < BinData::Record
    stringz :name
  end
end
