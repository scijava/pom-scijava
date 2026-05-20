#!/bin/sh
# Commit a file to the status.scijava.org gh-pages branch.
# Usage: publish.sh <local-file> [commit-message]
# Requires SSH agent to be running with write access to status.scijava.org.
set -e
file=$1
message=${2:-"Update $(basename "$file")"}
test -f "$file" || { echo "File not found: $file" >&2; exit 1; }
dest=$(basename "$file")

git config --global user.name github-actions
git config --global user.email github-actions@github.com

git clone --depth=1 --branch=gh-pages git@github.com:scijava/status.scijava.org site-publish
cp "$file" "site-publish/$dest"
cd site-publish
if git diff --quiet "$dest"
then
  echo "== No changes to $dest =="
else
  echo "== Committing $dest =="
  git add "$dest"
  git commit -m "$message"
  git push
fi
