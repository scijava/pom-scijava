#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/ci-build.sh &&
sh ci-build.sh || { echo "Maven build failed. Skipping melting pot tests."; exit 1; }

exit 0 # TEMP: Disable mega-melt during pom-scijava release process.

# Helper method to get the last cache modified date as seconds since epoch
last_cache_modified() {
  find "$HOME/.cache/scijava/melting-pot" -type f | while read f
  do
    stat -c '%Y' "$f"
  done | sort -nr | head -n1
}

# Record the last time of cache modification before running melting-pot
cache_modified_pre=0
cache_modified_post=0

if [ -d "$HOME/.cache/scijava/melting-pot" ]; then
  cache_modified_pre=$(last_cache_modified)
fi

# run melting-pot
tests/run.sh
meltResult=$?

# Record the last time of cache modification after running melting-pot
if [ -d "$HOME/.cache/scijava/melting-pot" ]; then
  cache_modified_post=$(last_cache_modified)
fi

# Determine if cache needs to be re-generated
echo "cache_modified_pre=$cache_modified_pre"
echo "cache_modified_post=$cache_modified_post"
if [ "$cache_modified_post" -gt "$cache_modified_pre" ]; then
  echo "cacheChanged=true"
  echo "cacheChanged=true" >> $GITHUB_ENV
else
  echo "cacheChanged=false"
  echo "cacheChanged=false" >> $GITHUB_ENV
fi

# NB: This script exits 0, but saves the exit code for a later build step.
echo $meltResult > exit-code
