#!/usr/bin/env bash
set -e

function invert() {
    for ((i=$#;i>=1;i--)); do echo "${!i}"; done
}

WIKI_URL=$(git config --file src/_wiki/.gitrepo --get subrepo.remote)
if [[ $WIKI_URL != "git@github.com:daisy/pipeline.wiki.git" ]]; then
    echo "unexpected remote url: $WIKI_URL" >&2
    exit 1
fi
WIKI_BRANCH=master

# If this is a local checkout (not on Travis for example), we should update the existing remotes and
# tracking branches.
for r in $(git remote); do
    if [ "$(git remote get-url $r)" = "$WIKI_URL" ]; then
        WIKI_REMOTE=$r
        break
    fi
done

git fetch ${WIKI_REMOTE:-$WIKI_URL} $WIKI_BRANCH

if [[ -n $WIKI_REMOTE ]]; then
    # Find local branch that tracks wiki
    WIKI_HEAD=$(
        git for-each-ref --format="%(refname:short) %(upstream:short)" refs/heads \
            | grep " $WIKI_REMOTE/$WIKI_BRANCH$" 2>/dev/null \
            | cut -d' ' -f1)
fi

if [[ -z $WIKI_HEAD ]]; then
    # If there is no tracking branch, set WIKI_HEAD it to a commit hash
    WIKI_HEAD=$(git rev-parse FETCH_HEAD)
elif [[ $(git rev-parse $WIKI_HEAD) != $(git rev-parse FETCH_HEAD) ]]; then
    if git merge-base --is-ancestor $WIKI_HEAD FETCH_HEAD; then
        # If the local branch is behind, pull
        git update-ref $WIKI_HEAD FETCH_HEAD
    elif git merge-base --is-ancestor FETCH_HEAD $WIKI_HEAD; then
        # If the local branch is ahead, push
        git push ${WIKI_REMOTE:-$WIKI_URL} $WIKI_HEAD:$WIKI_BRANCH
    else
        echo "Your tracking branch has diverted from the wiki upstream." >&2
        WIKI_HEAD=$(git rev-parse FETCH_HEAD)
    fi
fi

if [ -f .gitrepo ]; then
    # We are on a super branch. Check that it is in sync with a website branch.
    toplevel=$(git rev-parse --show-toplevel)
    cwd=$(pwd)
    subdir=${cwd#$toplevel/}
    # We can make use of git-subrepo because this command is part of the source code.
    cd $toplevel
    if ! status=$(.git-utils/git-subrepo-status --fetch --sha-only $subdir 2>/dev/null); then
        echo "There are unsynced commits in $subdir" >&2
        exit 1
    fi
    WEBSITE_HEAD=$(echo "$status" | sed "s|^$subdir @ ||")
else
    # We are on a website branch.
    WEBSITE_HEAD=$(git rev-parse HEAD)
fi

# Check whether there are changes done to src/_wiki that are not on the wiki yet
last_sync=$(
    git log $WEBSITE_HEAD --grep='subrepo:' --format=format:%H \
        | perl -e 'while (<STDIN>) {
                 chomp;
                 $message=`git log --format=%B -n 1 $_`;
                 if ($message =~ /^subrepo:\n +subdir: *"([^"]*)"\n  merged: *"([0-9a-f]{7,})"/m) {
                   if ($1 eq "src/_wiki") {
                     print "$_ $2";
                     last; }}}' \
               src/_wiki)
last_sync_website=$(echo $last_sync | cut -d' ' -f1)
last_sync_wiki=$(git rev-parse $(echo $last_sync | cut -d' ' -f2))
cherry_pick_commits=$(
    for commit in $(git rev-list $WEBSITE_HEAD ^$last_sync_website); do
        if [[ -n $(git diff-tree -r $commit -- src/_wiki) ]]; then
            echo $commit
        fi
    done)
if [[ -z $cherry_pick_commits ]]; then
    echo "Everything up to date" >&2
    exit 0
else
    if ! git rev-parse --verify $last_sync_wiki >/dev/null 2>/dev/null; then
        echo "Corrupt subrepo commit: $last_sync_website" >&2
        exit 1
    fi
    if ! diff <(git ls-tree -r $last_sync_wiki 2>/dev/null | awk '{print $3}') \
              <(git ls-tree -r $last_sync_website src/_wiki 2>/dev/null | grep -vF src/_wiki/.gitrepo | awk '{print $3}') \
              >/dev/null; then
        echo "Corrupt subrepo commit: $last_sync_website" >&2
        exit 1
    fi
    cherry_pick_commits=($(invert $cherry_pick_commits))
    if [[ $last_sync_wiki != $(git rev-parse $WIKI_HEAD) ]]; then
        if git merge-base --is-ancestor $WIKI_HEAD $last_sync_wiki; then
            # If the wiki upstream is behind, push (see below)
            if [[ $(git rev-parse --abbrev-ref $WIKI_HEAD) == $WIKI_HEAD ]]; then
                git update-ref $WIKI_HEAD $last_sync_wiki
            else
                WIKI_HEAD=$last_sync_wiki
            fi
        elif git merge-base --is-ancestor $last_sync_wiki $WIKI_HEAD; then
            # If the wiki upstream is ahead, check if the new wiki commits are equal to the website commits
            new_upstream_commits=($(invert $(git rev-list $WIKI_HEAD ^$last_sync_wiki)))
            if [ ${#new_upstream_commits[@]} -le ${#cherry_pick_commits[@]} ]; then
                while [ ${#new_upstream_commits[@]} -gt 0 ]; do
                    if diff <(git ls-tree -r ${new_upstream_commits[0]} 2>/dev/null | awk '{print $3}') \
                            <(git ls-tree -r ${cherry_pick_commits[0]} src/_wiki 2>/dev/null \
                                     | grep -vF src/_wiki/.gitrepo \
                                     | awk '{print $3}') \
                            >/dev/null; then
                        unset new_upstream_commits[0]
                        new_upstream_commits=(${new_upstream_commits[@]})
                        unset cherry_pick_commits[0]
                        cherry_pick_commits=(${cherry_pick_commits[@]})
                    else
                        break
                    fi
                done
            fi
            if [ ${#new_upstream_commits[@]} -gt 0 ]; then
                echo "There are changes in the wiki that are not part of the website yet. Manual merge required." >&2
                exit 1
            fi
            if [ ${#cherry_pick_commits[@]} == 0 ]; then
                echo "Everything up to date" >&2
                exit 0
            fi
        else
            echo "There are changes in the wiki that are not part of the website yet. Manual merge required." >&2
            exit 1
        fi
    fi
    # Checkout wiki branch and cherry-pick all website commits that touch src/_wiki
    if ! git diff-index --quiet HEAD; then
        echo "You have uncommitted changes." >&2
        exit 1
    fi
    current=$(git rev-parse --abbrev-ref HEAD)
    git checkout $WIKI_HEAD
    for commit in "${cherry_pick_commits[@]}"; do
        git cherry-pick -Xsubtree=src/_wiki $commit
    done
    if [[ $(git rev-parse --abbrev-ref $WIKI_HEAD) != $WIKI_HEAD ]]; then
        WIKI_HEAD=$(git rev-parse HEAD)
    fi
    # Push
    git push ${WIKI_REMOTE:-$WIKI_URL} $WIKI_HEAD:$WIKI_BRANCH
    # Go back to previous branch
    git checkout $current
fi
