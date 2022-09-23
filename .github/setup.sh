#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/ci-setup-github-actions.sh
sh ci-setup-github-actions.sh

# xmllint needed for melting pot
sudo apt-get install libxml2-utils
