#!/usr/bin/env perl
use Term::ANSIColor;

my $MY_DIR = $ENV{'MY_DIR'};
my $VERBOSE = $ENV{'VERBOSE'};

sub group_commands {
	my @commands;
	while (<>) {
		chomp;
		$_ =~ s/'/'"'"'/g;
		(my $c, my @a) = `eval 'printf "%s\n" $_'`;
		chomp $c;
		chomp @a;
		if ($c =~ /^\Q${MY_DIR}\E\/mvn-release.sh / and my @release_cmd = grep { @{$_}[0] eq $c } @commands) {
			push @{$release_cmd[0]}, @a;
		} elsif ($c =~ /^\Q${MY_DIR}\E\// and @{$commands[-1]} and $c eq @{$commands[-1]}[0]) {
			push @{$commands[-1]}, @a;
		} else {
			my @cmd = ($c, @a);
			push @commands, \@cmd;
		}
	}
	@commands;
}

sub pretty_print_and_eval {
	my @commands = @_;
	print "-------------- ", color("bold yellow"), "Build order", color("reset"), ": -------------\n";
	for (@commands) {
		my @cmd = @{$_};
		if (@cmd > 2) {
			print join(" \\\n   ", @cmd), "\n";
		} else {
			print join(" ", @cmd), "\n";
		}
	}
	print "-------------- ", color("bold yellow"), "Environment", color("reset"), ": -------------\n";
	my %old_env; {
		my $env_file = "$ENV{'TARGET_DIR'}/env";
		open(my $env_file_info, $env_file) or die "Could not open $env_file";
		while (<$env_file_info>) {
			chomp;
			if ($_ eq "") {
			} elsif ($_ =~ /^([^=]+)=(.*)$/) {
				$old_env{$1}=$2;
			} else {
				die "Unexpected line: $_";
			}
		}
		close $env_file_info;
	}
	foreach $key (sort keys(%ENV)) {
		if (not $key eq "_" and (not exists($old_env{$key}) or not $old_env{$key} eq $ENV{$key})) {
			$value = $ENV{$key};
			$value =~ s/"/"'"'"/g;
			print "export $key=\"", $value, "\"\n";
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
			printf STDERR color("bold red");
			printf STDERR "\nfailed to execute command: $!\n";
			printf STDERR color("reset");
			exit 1;
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
}

pretty_print_and_eval(group_commands());
