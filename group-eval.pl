use Term::ANSIColor;

my @commands;
while (<>) {
	chomp;
	(my $c, my @a) = `eval 'printf "%s\n" $_'`;
 	chomp $c;
 	chomp @a;
	if ($c =~ /^bash \.make\/mvn-release.sh / and my @release_cmd = grep { @{$_}[0] eq $c } @commands) {
		push @{$release_cmd[0]}, @a;
	} elsif ($c =~ /^bash \.make\// and my @prev_cmd = @{$commands[-1]} and $c eq $prev_cmd[0]) {
		push @prev_cmd, @a;
	} else {
		push @commands, [$c, @a];
	}
}
print "-------------- ", color("bold yellow"), "Build order", color("reset"), ": -------------\n";
for (@commands) {
	my @cmd = @{$_};
	if (@cmd > 2) {
		print join(" \\\n   ", @cmd), "\n";
	} else {
		print join(" ", @cmd), "\n";
	}
}
print "-----------------------------------------\n";
for (@commands) {
	my @cmd = @{$_};
    print "--> ", color("bold yellow");
	if (@cmd > 2) {
		print join(" \\\n   ", @cmd), "\n";
	} else {
		print join(" ", @cmd), "\n";
	}
	print color("reset");
    system("bash", "-c", join(" ",@cmd));
	if ($? == 0) {
	} elsif ($? == -1) {
		printf STDERR "\nfailed to execute: $!\n";
	} elsif ($? & 127) {
		printf STDERR "\ncommand died with signal %d\n", ($? & 127);
		exit 1;
	} else {
		my $e = $? >> 8;
		if ($e == 100) {
			printf STDERR "\naction suspended...\n";
		} else {
			printf STDERR color("bold red");
			printf STDERR "\ncommand exited with value %d\n", $e;
			printf STDERR color("reset");
		}
		exit $e;
	}
}
