#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/travis-build.sh
sh travis-build.sh $encrypted_e7761bcf8400_key $encrypted_e7761bcf8400_iv
#sh tests/run.sh
