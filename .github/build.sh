#!/bin/sh
set -e
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/ci-build.sh
sh ci-build.sh

# Record the last time of cache modification before running melting-pot
cache_modified_pre=0
cache_modified_post=0

if [ -f "~/.cache/scijava/melting-pot" ]; then
  cache_modified_pre=$(find ~/.cache/scijava/melting-pot -printf '%T@\n' | sort -r | head -n 1)
  cache_modified_pre=${cache_modified_pre%.*}
fi

# run melting-pot
sh tests/run.sh

# Record the last time of cache modification after running melting-pot
if [ -f "~/.cache/scijava/melting-pot" ]; then
  cache_modified_post=$(find ~/.cache/scijava/melting-pot -printf '%T@\n' | sort -r | head -n 1)
  cache_modified_post=${cache_modified_post%.*}
fi

# Determine if cache needs to be re-generated
if [ "$cache_modified_post" -gt "$cache_modified_pre" ]; then
  echo "update-cache=true" >> $GITHUB_ENV
else
  echo "update-cache=false" >> $GITHUB_ENV
fi
