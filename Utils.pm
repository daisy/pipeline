package Utils;

use strict;
use warnings;
use Exporter;

our @ISA= qw( Exporter );

our @EXPORT = qw(
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
	for (`git branch -r --contains $rev`) {
		if ($_ =~ /^ *\Q$remote\E\/(.+)$/) {
			push @branches, $1;
		}
	}
	return @branches;
}

sub cmp_tree {
	my ($revision_a, $revision_b) = @_;
	my $tree_a = `git ls-tree -r $revision_a 2>/dev/null | awk '{print \$3}'`;
	my $tree_b = `git ls-tree -r $revision_b 2>/dev/null | awk '{print \$3}'`;
	return ("$tree_a" eq "$tree_b");
}

sub cmp_tree_with_subtree {
	my ($revision_a, $revision_b, $subdir_b) = @_;
	my $tree_a = `git ls-tree -r $revision_a 2>/dev/null | awk '{print \$3}'`;
	my $tree_b = `git ls-tree -r $revision_b $subdir_b 2>/dev/null | grep -vF $subdir_b/.gitrepo | awk '{print \$3}'`;
	return ("$tree_a" eq "$tree_b");
}

sub cmp_subtree_with_subtree {
	my ($revision_a, $revision_b, $subdir) = @_;
	my $tree_a = `git ls-tree -r $revision_a $subdir 2>/dev/null | grep -vF $subdir/.gitrepo | awk '{print \$3}'`;
	my $tree_b = `git ls-tree -r $revision_b $subdir 2>/dev/null | grep -vF $subdir/.gitrepo | awk '{print \$3}'`;
	return ("$tree_a" eq "$tree_b");
}

sub rev_parse {
	my ($revision) = @_;
	chomp(my $commit = `git rev-parse $revision`);
	return $commit;
}

my %git_subrepo_commits = ();

sub find_last_git_subrepo_commit {
	my ($subdir, $start) = @_;
	$start = $start || "HEAD";
	my @commits; {
		if (exists $git_subrepo_commits{$start}) {
			@commits = @{$git_subrepo_commits{$start}};
		} else {
		    @commits = `git log $start --grep='subrepo:' --format=format:%H`;
			for (@commits) {
				chomp;
			}
		    @{$git_subrepo_commits{$start}} = @commits;
		}
	}
	for my $commit (@commits) {
		my $commit_message = `git log --format=%B -n 1 $commit`;
		if ($commit_message =~ /^subrepo:\n +subdir: *"([^"]*)"\n  merged: *"([0-9a-f]{7,})"/m) {
			if ($1 eq $subdir) {
				if (not revision_exists($2)) {
					print STDERR "ERROR: corrupt git-subrepo commit: $commit";
					print STDERR " (commit $2 does not exist: might have been purged, or was never pushed)\n";
					return;
				} elsif (cmp_tree_with_subtree($2, $commit, $subdir)) {
					return ($commit, rev_parse($2));
				} else {
					print STDERR "ERROR: corrupt git-subrepo commit: $commit";
					print STDERR " (is not tree-equal to commit $2)\n";
					return;
				}
			}
		}
	}
	return;
}

sub distance {
	my ($from, $to) = @_;
	my @commits = defined $from ? `git rev-list $to ^$from` : `git rev-list $to`;
	for (@commits) {
		chomp;
	}
	return @commits;
}

1;
