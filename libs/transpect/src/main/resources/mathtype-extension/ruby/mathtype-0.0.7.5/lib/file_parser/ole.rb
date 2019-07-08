require "bindata"
require "ole/storage"

module Mathtype
  class OleFileParser < FileParser
    def initialize(path)
      read_from_file(path)
    end
    def read_from_file(path)
      ole = Ole::Storage.open(path, "rb+")
      @equation = ole.file.read("Equation Native")[28..-1]
      ole.close
    end
  end
end
