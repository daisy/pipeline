module Mathtype
  class EpsFileParser < FileParser
    def initialize(path)
      super(path)
      extract_mtef_from_epscomment(@raw)
    end

    def extract_mtef_from_epscomment(comment)
      string64 = @raw.gsub /\r/, ""
      # d for delimiter
      d = string64[(/MathType(.)MTEF\1/ =~ string64) + 8]
      matches = string64.match /.*MathType#{d}MTEF#{d}(.)#{d}(.)#{d}(.)(.)(.*)#{d}([0-9A-F]{4})#{d}.*/m
      leading_chrs = matches[1].to_i
      trailing_chrs = matches[2].to_i - 1
      a64_chr1 = matches[3]
      a64_chr2 = matches[4]
      A64[a64_chr1] = 62
      A64[a64_chr2] = 63
      A64[62] = a64_chr1
      A64[63] = a64_chr2
      string64   = matches[5]
      mtchecksum = matches[6]
      string64.gsub! /.{#{trailing_chrs}}(\n).{#{leading_chrs}}/m, ""
      @equation = decode string64
      unless checksum == mtchecksum
        raise ArgumentError, "Checksums do not match, expected #{mtchecksum} but got #{checksum}"
      end
    end
  end
end
