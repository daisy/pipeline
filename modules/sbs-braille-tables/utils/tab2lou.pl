#!/usr/bin/perl
#
# This is a straight port of TabToLiblouis.java
#
use strict;
use warnings;
use utf8;
use File::Basename;
use IO::Handle;
use Braille;

my $gPrgName = basename ($0);

if (@ARGV != 2){
    die "Usage: $gPrgName inputfilename outputfilename";
}
my ($input, $output) = @ARGV;

my ($ifh, $ofh);
open($ifh, "<:crlf:encoding(utf8)", $input) || die "failed to open '$input' for reading :$!";
open($ofh, ">:encoding(utf8)", $output) || die "failed to open '$output' for writing :$!";

translate($ifh, $ofh);

close($ifh) || die "failed to close '$ifh':$!";
close($ofh) || die "failed to close '$ofh':$!";

sub translate {
    my ($ifh, $ofh) = @_;
    while(my $line = $ifh->getline()){

	# copy lines starting with #=# verbatim (excluding '#=#')
        if($line =~ m/^#=#/){
            $ofh->print(substr($line,3));
            next;
        }

	# ignore lines with ###
        next if($line =~ m/^###/);

	# simply copy empty and comment lines
        if(($line =~ m/^$/)||($line =~ m/^#/)){
            $ofh->print($line);
            next;
        }

	my ($opcode, $ink, $brl) = split(/\s+/, $line);
        $opcode =~ s/_/ /g;
        my $ink2 = $ink;
        if($ink =~ m/~/){
            $ink2 =~ s/~/s/g;
            writeLine($ofh, $opcode, $ink2, $brl);
        }
        $ink2 = $ink;
        $ink2 =~ s/s~/ß/g;
        writeLine($ofh, $opcode, $ink2, $brl);
    }
}

sub writeLine {
    my($ofh, $opcode, $ink, $brl) = @_;
    $ofh->print("$opcode $ink ".Braille::din2dots($brl).$/);
}
