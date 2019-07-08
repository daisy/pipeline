module Mathtype
  class WmfFileParser < FileParser
    def initialize(path)
      super(path)
      extract_mtef_from_mfcomment(@raw)
    end

    def extract_mtef_from_mfcomment(comment)
      match = comment.match("MathTypeUU")
      if match
        lenpos = match.end(0)
        len = comment[lenpos..lenpos+1].unpack("H*")[0].to_i 16
        start = lenpos + 2
      else
        match = comment.match("AppsMFCC\x01")
        raise ::NotImplementedError, "No MathType Equation found in wmf" unless match
        len_start = match.end(0)
        totallen = comment[len_start..len_start + 3].reverse.chars.rotate(-1).join.unpack("H*")[0].to_i 16
        datalen = comment[len_start + 4 .. len_start + 7].reverse.chars.rotate(-1).join.unpack("H*")[0].to_i 16
        signature = comment[len_start + 8.. -1].match(/.+?\x00/)
        raise ::NotImplementedError, "Equation split over multiple comments" unless totallen == datalen
        start = len_start + 8 + signature.end(0)
        len = datalen
      end
      @equation = comment[start..(start + len)]
    end
  end
end
