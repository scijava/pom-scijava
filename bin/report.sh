#!/bin/sh
# Generate the status, badges, and team reports for a Maven BOM, writing them
# under target/pombast/ (see pombast.toml).
#
# Run from a BOM checkout (e.g. a pom-scijava working copy):
#
#   bin/report.sh
#
# The status report overlays binary/source compatibility info from
# target/pombast/smelt.json when present. Run bin/check.sh first, or drop a
# published smelt.json into target/pombast/, to populate it; without it, the
# reports are still generated, just without the bytecode classification.
command -v pombast >/dev/null 2>&1 ||
  uv tool install "git+https://github.com/scijava/pombast.git"

smelt=target/pombast/smelt.json
if [ -f "$smelt" ]; then
  pombast status --smelt "$smelt"
else
  echo "== $smelt not found; generating status without bytecode classification =="
  pombast status
fi
pombast badges
pombast team
