#!/usr/bin/perl
#
# This is a straight port of the Braille.java program
# 
use strict;
use warnings;
use utf8; # source code of this very program is utf8

package Braille;
use strict;
use warnings;

my %d2d;
my $mapping;


sub din2dots {
    if( (@_ == 0) || !defined($_[0]) || $_[0] eq ""){
        return "";
    }
    my @din = split(//, $_[0]);
    my $result = $d2d{$din[0]};
    for(my $i = 1; $i < @din; ++$i){
        $result .= "-";
        my $c = $d2d{$din[$i]};
        if(!defined($c)){
            print "no definition for din[$i] : $din[$i] ".ord($din[$i])." on line ".$.." $/";
	    print "Please remove offending characters from source file$/";
	    print "or add offending characters to din2dots mapping.$/";
	    exit(3);
        }
        $result .= $d2d{$din[$i]};
    }
    $result;
}

BEGIN {

# Notation des Mappings:
# unicode whitespace braille # optional: Kommentar
$mapping = <<\END;
A	1
B	12
C	14
D	145
E	15
F	124
G	1245
H	125
I	24
J	245
K	13
L	123
M	134
N	1345
O	135
P	1234
Q	12345
R	1235
S	234
T	2345
U	136
V	1236
X	1346
Y	13456
Z	1356
0	346
1	16
2	126
3	146
4	1456
5	156
6	1246
7	12456
8	1256
9	246
&	12346
%	123456
[	12356
^	2346
]	23456
W	2456
,	2
;	23
:	25
/	256
?	26
+	235
=	2356
(	236
*	35
)	356
.	3
\	34
@	345
#	3456
"	4
!	5
>	45
$	46
_	456
<	56
-	36
'	6
ß	2346
§	2346 # bedingtes Eszett
|	2346 # Eszett bei groß, schließ, ...
à	123568
á	168
â	1678
ã	34678
å	345678
æ	478
ç	1234678
è	23468
é	1234568
ê	12678
ë	12468
ì	348
í	1468
î	14678
ï	124568
ð	23458
ñ	13458
ò	3468
ó	14568
ô	145678
õ	1358
ø	24678
ù	234568
ú	1568
û	15678
ý	24568
þ	12348
ÿ	134568
	246789
b	0
m	d
t	e
w	f
a	cf
n	cb
p	cd
k	ce
v	36a # P36 ohne nachfolgende Trennmarke "m" (für "ver" u.ä.)
END

    for(split($/, $mapping)){
        my ($key, $value, $comment) = split(/\s+/);
        $d2d{$key} = $value;
    }
}

package BrailleTest;
use strict;
use warnings;

sub test {
	eval "use Test::More qw(no_plan);";
	
	is(Braille::din2dots(''),   '');
	is(Braille::din2dots(undef),'');
	is(Braille::din2dots('A'),   '1');
	is(Braille::din2dots('ë'),   '12468');
	is(Braille::din2dots('AA'),  '1-1');
	is(Braille::din2dots('AAë'),  '1-1-12468');
	is(Braille::din2dots('|'),  '2346');
	is(Braille::din2dots(''),  '246789');
	is(Braille::din2dots('v'),  '36a');
	is(Braille::din2dots('\''),  '6');
	is(Braille::din2dots('#'),  '3456');
	is(Braille::din2dots('\\'),  '34');
}

################################################################################
package main;
use strict;
use warnings;
use File::Basename;

if(basename($0) eq "Braille.pm") {
	if(@ARGV){
		if($ARGV[0] eq "test"){
			BrailleTest::test();
			exit 0;
		}
	} else {
		print STDERR "no args given -> performing test$/";
		print STDERR "$0$/";
		BrailleTest::test();
		exit 0;
	}
}

1;
