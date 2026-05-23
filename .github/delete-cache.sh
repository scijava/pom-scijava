#!/bin/sh
test "$1" || { echo "Usage: delete-cache.sh cache-key"; exit 1; }
gh api --method DELETE \
  -H "Accept: application/vnd.github+json" \
  "/repos/scijava/pom-scijava/actions/caches?key=$1" || true
