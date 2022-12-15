#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/master/ci-setup-github-actions.sh
sh ci-setup-github-actions.sh

# Install needed packages.
pkgs="libxml2-utils"       # needed for melting pot
pkgs="$pkgs libxcb-shape0" # org.janelia:H5J_Loader_Plugin (fiji/H5J_Loader_Plugin@d026a1bb)
pkgs="$pkgs libgtk2.0-0"   # net.imagej:imagej-opencv (imagej/imagej-opencv@21113e08)
pkgs="$pkgs libblosc1"     # org.janelia.saalfeldlab:n5-blosc
sudo apt-get update
sudo apt-get -y install $(echo "$pkgs")
