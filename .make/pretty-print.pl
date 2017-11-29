#!/usr/bin/env perl
while (<>) {
	chomp;
	# assuming host platform is batch
	# split lines on '&'
	my @words = split(/\s*&\s*/, $_);
	print join("\n", @words), "\n";
}
