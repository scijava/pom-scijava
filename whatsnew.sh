#!/bin/sh
dir=$(cd "${0%/*}" && pwd)
mvn -B -U -Dverbose=true -s settings.xml \
  -Dmaven.version.rules="file://$dir/rules.xml" \
  versions:display-dependency-updates
