#!/bin/sh
dir=$(dirname "$0")
pom="$dir/../pom.xml"
mvn -B -f "$pom" help:effective-pom |
  grep -A9999999 '^<?xml' |
  grep -B9999999 '^</project>' > eff.xml
"$dir/catalog.kts" eff.xml
rm eff.xml
