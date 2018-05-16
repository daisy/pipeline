package Utils;

use strict;
use warnings;
use Exporter;
use Try::Tiny;

our @ISA= qw( Exporter );

our @EXPORT = qw(
	die_with_stack_trace
	git
	revision_exists
	is_ancestor
	in_remote_branches
	cmp_tree
	cmp_tree_with_subtree
	cmp_subtree_with_subtree
	rev_parse
	find_last_git_subrepo_commit
	distance
);

sub stack_trace {
	my $trace = "";
	my $i = 1;
	while ((my @call_details = (caller($i++)))){
		$trace = $trace."  ".$call_details[1].":".$call_details[2]." in function ".$call_details[3]."\n";
	}
	return $trace;
}

sub die_with_stack_trace {
	my @msg = @_;
	# print STDERR "__\n";
	# for (split("\n", join("", @msg)."\nStack Trace:\n".stack_trace())) {
	# 	print STDERR "| $_\n";
	# }
	die join("", @msg)."\nStack Trace:\n".stack_trace();
}

sub git {
	my ($cmd) = @_;
	my @result = `git $cmd`;
	$? == 0 or die_with_stack_trace "git command failed: git $cmd";
	if (wantarray) {
		return @result;
	} elsif (defined wantarray) {
		return join("",@result);
	}
}

sub revision_exists {
	my ($rev) = @_;
	if ($rev =~ /^\$/) {
		return;
	} else {
		return `git rev-parse --verify $rev 2>/dev/null`;
	}
}

sub is_ancestor {
	my ($ancestor, $rev) = @_;
	`git merge-base --is-ancestor $ancestor $rev`;
	not $?;
}

sub in_remote_branches {
	my ($rev, $remote) = @_;
	my @branches;
	for (git "branch -r --contains $rev") {
		if ($_ =~ /^ *\Q$remote\E\/(.+)$/) {
			push @branches, $1;
		}
	}
	return @branches;
}

sub cmp_tree {
	my ($revision_a, $revision_b) = @_;
	my $tree_a = git "ls-tree -r $revision_a 2>/dev/null | awk '{print \$3}'";
	my $tree_b = git "ls-tree -r $revision_b 2>/dev/null | awk '{print \$3}'";
	return ("$tree_a" eq "$tree_b");
}

sub cmp_tree_with_subtree {
	my ($revision_a, $revision_b, $subdir_b) = @_;
	my $tree_a = git "ls-tree -r $revision_a 2>/dev/null | awk '{print \$3}'";
	my $tree_b = git "ls-tree -r $revision_b $subdir_b 2>/dev/null | grep -vF $subdir_b/.gitrepo | awk '{print \$3}'";
	return ("$tree_a" eq "$tree_b");
}

sub cmp_subtree_with_subtree {
	my ($revision_a, $revision_b, $subdir) = @_;
	my $tree_a = git "ls-tree -r $revision_a $subdir 2>/dev/null | grep -vF $subdir/.gitrepo | awk '{print \$3}'";
	my $tree_b = git "ls-tree -r $revision_b $subdir 2>/dev/null | grep -vF $subdir/.gitrepo | awk '{print \$3}'";
	return ("$tree_a" eq "$tree_b");
}

sub rev_parse {
	my ($revision) = @_;
	chomp(my $commit = git "rev-parse $revision");
	return $commit;
}

my %git_subrepo_commits = ();

sub find_last_git_subrepo_commit {
	my ($subdir, $to, $from) = @_;
	$to = $to || "HEAD";
	my @commits; {
		if (exists $git_subrepo_commits{($from||"")."..".$to}) {
			@commits = @{$git_subrepo_commits{($from||"")."..".$to}};
		} else {
			if ($from) {
				@commits = git "log $to ^$from --grep='subrepo:' --format=format:%H";
			} else {
				@commits = git "log $to --grep='subrepo:' --format=format:%H";
			}
			for (@commits) {
				chomp;
			}
		    @{$git_subrepo_commits{($from||"")."..".$to}} = @commits;
		}
	}
	for my $commit (@commits) {
		my $commit_message = git "log --format=%B -n 1 $commit";
		if ($commit_message =~ /^subrepo:\n +subdir: *"([^"]*)"\n  merged: *"([0-9a-f]{7,})"/m) {
			if ($1 eq $subdir) {
				if (not revision_exists($2)) {
					die_with_stack_trace "corrupt git-subrepo commit: "
						. substr($commit, 0, 7)
						. " (commit $2 does not exist: might have been purged, or was never pushed)";
				} elsif (cmp_tree_with_subtree($2, $commit, $subdir)) {
					return ($commit, rev_parse($2));
				} else {
					die_with_stack_trace "corrupt git-subrepo commit: "
						. substr($commit, 0, 7)
						. " (is not tree-equal to commit $2)";
				}
			}
		}
	}
	return;
}

sub distance {
	my ($from, $to) = @_;
	my @commits = defined $from ? git "rev-list $to ^$from" : git "rev-list $to";
	for (@commits) {
		chomp;
	}
	return @commits;
}

1;
