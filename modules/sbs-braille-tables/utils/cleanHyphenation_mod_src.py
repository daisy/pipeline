#!/usr/bin/python
# coding=utf-8
"""Clean hyphenation points from tables

This utility script can be used to clean the hyphenation points from
tables that still contain them.

Usage: 

cleanHyphenation_mod_src.py table > cleanTable
"""

import fileinput
import re
import sys

VALID_BRAILLE_RE = re.compile(u"^[-A-Z0-9bvéèà&%[^\],;:/?+=(*).\\\\@#\"!>$_<\']+$")
INVALID_CHARS = 'twnapzkm'

for line in fileinput.input():
    if line.startswith('#') or line.isspace():
        print line,
    else:
        parts = line.split(None, 3)
        if len(parts) < 4:
            parts.append('') # append an empty comment
        command, untranslated, braille, comment = parts
        braille = braille.translate(None, INVALID_CHARS)
        if not VALID_BRAILLE_RE.match(braille):
            print >> sys.stderr, "%s:%s: Braille not valid: %s" % (fileinput.filename(), fileinput.lineno(), braille)
        else:
            print "%s\t%s\t%s\t%s" % (command, untranslated, braille, comment)
