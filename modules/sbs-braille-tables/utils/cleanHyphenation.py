#!/usr/bin/python
# coding=utf-8
"""Clean hyphenation points from white lists

This utility script can be used to clean the hyphenation points from
white lists.

Usage: 

cleanHyphenation.py table > cleanTable
"""

import fileinput
import re
import sys
from functools import partial
from string import maketrans

VALID_BRAILLE_RE = re.compile("^[A-Z0-9&%[^\],;:/?+=(*).\\\\@#\"!>$_<\'àáâãåæçèéêëìíîïðñòóôõøùúûýþÿœv]+$")
VALID_UNTRANSLATED_RE = re.compile("^[a-zàáâãåäæçèéêëìíîïðñòóôõöøùúûüýþÿœv~ß']+$")
INVALID_CHARS = 'twnapzk'

entries = set()

def validate_full(untranslated, words):
    for word in words:
        if not VALID_BRAILLE_RE.match(word):
            print >> sys.stderr, "Validate: %s: %s" % (untranslated, word)
            return False
    return True

def print_line(line):
    if line[:2] not in entries:
        entries.add(line[:2])
        print "%s %s\t%s\t%s" % line
    else:
        print >> sys.stderr, "Duplicate: %s %s %s %s" % line
    
for line in fileinput.input():
    if line.startswith('#'):
        print line
    else:
        type, untranslated, grade2, grade1 = line.split()
        validate = partial(validate_full, untranslated)
        grade2, grade1 = map(lambda x: x.translate(None, INVALID_CHARS), (grade2, grade1))
        grade2 = grade2.translate(maketrans('|','^'))
        untranslated = untranslated.translate(None, '#') # these were used for hyphenation hints
        if '|' in untranslated and type == 'h' and validate((grade2, grade1)):
            print_line((type, untranslated, grade2, grade1))
        elif not VALID_UNTRANSLATED_RE.match(untranslated):
            print >> sys.stderr, "%s: %s, %s" % (untranslated, grade2, grade1)
        elif 's~' in untranslated:
            # if the untranslated word contains a 's~' then add two
            # entries: one for German and one for Swiss German
            # spelling
            u, g1, g2 = untranslated.replace('s~','ß'), grade1.replace('§','^'), grade2.replace('§','^')
            if validate((g2, g1)):
                print_line((type, u, g2, g1))
            u, g1, g2 = untranslated.replace('s~','ss'), grade1.replace('§','SS'), grade2.replace('§','^')
            if validate((g2, g1)):
                print_line((type, u, g2, g1))
        elif 'ß' in untranslated:
            # if the untranslated word contains a ß then add a second
            # entry for the swiss german spelling
            u, g1, g2 = untranslated, grade1, grade2.replace('ß','^')
            if validate((g2, g1)):
                print_line((type, u, g2, g1))
            u, g1, g2 = untranslated.replace('ß','ss'), grade1.replace('^','SS'), grade2.replace('ß','^')
            if validate((g2, g1)):
                print_line((type, u, g2, g1))
        else:
            print_line((type, untranslated, grade2, grade1))
