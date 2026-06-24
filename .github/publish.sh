#!/bin/sh
# Commit a file to the status.scijava.org gh-pages branch.
# Requires SSH agent to be running with write access to status.scijava.org.
set -e

test $# -gt 1 || {
  echo "Usage: publish.sh \"Commit message\" file1 [file2 ...]"
  exit 2
}

datestamp="$(TZ=UTC date +'%Y-%m-%d %H:%M:%S UTC')"
message="$1 ($datestamp)"
shift

git config --global user.name "SciJava CI"
git config --global user.email ci@scijava.org

dest_dir=site-publish

git clone --depth=1 --branch=gh-pages git@github.com:scijava/status.scijava.org "$dest_dir"

while [ $# -gt 0 ]
do
  file=$1
  shift
  test -f "$file" || { echo "File not found: $file" >&2; exit 1; }
  dest=$(basename "$file")
  cp "$file" "$dest_dir/$dest"
  (cd "$dest_dir" && git add "$dest")
done

cd "$dest_dir"
# Check staged changes (--cached): the files above were already `git add`ed, so
# a plain `git diff` would see an always-clean working tree and never commit.
if git diff --cached --quiet
then
  echo "== No changes =="
else
  echo "== Committing changes =="
  git commit -m "$message"
  # Retry against concurrent publishers (the build and status workflows both
  # push here), rebasing onto whatever landed in between.
  n=0
  until git push
  do
    n=$((n + 1))
    test "$n" -lt 5 || { echo "push failed after $n attempts" >&2; exit 1; }
    git pull --rebase
  done
fi
