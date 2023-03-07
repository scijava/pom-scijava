#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/ci-build.sh &&
sh ci-build.sh || { echo "Maven build failed. Skipping melting pot tests."; exit 1; }

# Helper method to get the last cache modified date as seconds since epoch
last_cache_modified () {
  cache_modified=$(find ~/.cache/scijava/melting-pot -printf '%T@\n' | sort -r | head -n1)
  cache_modified=${cache_modified%.*}
  echo "$cache_modified"
}

# Record the last time of cache modification before running melting-pot
cache_modified_pre=0
cache_modified_post=0

if [ -f "~/.cache/scijava/melting-pot" ]; then
  cache_modified_pre=$(last_cache_modified)
fi

# run melting-pot
tests/run.sh
meltResult=$?

# Record the last time of cache modification after running melting-pot
if [ -f "~/.cache/scijava/melting-pot" ]; then
  cache_modified_post=$(last_cache_modified)
fi

# Determine if cache needs to be re-generated
if [ "$cache_modified_post" -gt "$cache_modified_pre" ]; then
  echo "update-cache=true" >> $GITHUB_ENV
else
  echo "update-cache=false" >> $GITHUB_ENV
fi

# NB: This script exits 0, but saves the exit code for a later build step.
echo $meltResult > exit-code
