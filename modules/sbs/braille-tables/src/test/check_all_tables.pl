#!/usr/bin/perl
use warnings;
use strict;
$|++;

# Test all tables with lou_checktable.
#
# Copyright (C) 2011 by Swiss Library for the Blind, Visually Impaired and Print Disabled
#
# Copying and distribution of this file, with or without modification,
# are permitted in any medium without royalty provided the copyright
# notice and this notice are preserved.

# We assume that the productive tables, i.e. the ones that are shipped
# (and need to be tested) are found in the first path in
# LOUIS_TABLEPATH. The subsequent entries are for test tables.
my $tablesdir = (split(',', $ENV{LOUIS_TABLEPATH}))[0];

my $fail = 0;

# get all the tables from the tables directory
my @tables = glob("$tablesdir/*.[cu]tb $tablesdir/*.cti $tablesdir/*.dis");

foreach my $table (@tables) {
    print "lou_checktable $table\n";
    if (system ("lou_checktable $table --quiet") != 0) {
        print "=> FAILED\n";
        $fail = 1;
    }
}

exit $fail;
