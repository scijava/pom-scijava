#!/bin/sh
curl -fsLO https://raw.githubusercontent.com/scijava/scijava-scripts/main/ci-setup-github-actions.sh
sh ci-setup-github-actions.sh

# Install native libraries needed to build and test smelt components.
pkgs="libxcb-shape0"  # org.janelia:H5J_Loader_Plugin
pkgs="$pkgs libgtk2.0-0"  # net.imagej:imagej-opencv
pkgs="$pkgs libblosc1"  # org.janelia.saalfeldlab:n5-blosc
sudo apt-get update
sudo apt-get -y install $pkgs
