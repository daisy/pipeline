set -e

# XSLT takes too much time (0.45s)
# java -cp $SAXON net.sf.saxon.Transform -s:- -xsl:.make/mvn-get-version.xsl

# much faster with xmllint (0.004s)
xmllint --xpath "string(/*/*[local-name()='version'])" - 2>/dev/null
