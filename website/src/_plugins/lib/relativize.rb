def relativize(base, child)
  if !child.hier? || !base.hier? ||
     (base.scheme || '').downcase != (child.scheme || '').downcase ||
     base.authority != child.authority
    return child
  else
    bp = base.normalized_path
    cp = child.normalized_path
    if cp.start_with?('/')
      bp_segments = bp.split('/')[0..-2]
      cp_segments = cp.split('/')
      i = bp_segments.length
      j = 0
      while i > 0 do
        if bp_segments[j] == cp_segments[j]
          i -= 1
          j += 1
        else
          break
        end
      end
      relativized_path = (['..'] * i + cp_segments[j..-1]) * '/'
    else
      relativized_path = cp
    end
    return RDF::URI(path: relativized_path, query: child.query, fragment: child.fragment)
  end
end
