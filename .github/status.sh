#!/bin/sh
# CI orchestrator for the status dashboard: fetch the most recently published
# smelt.json, regenerate the status/badges/team reports from it, and publish
# them to status.scijava.org. The report generation itself lives in
# bin/report.sh so it can be run outside CI too.
set -e

# Pull the most recently published smelt results so that `pombast status` can
# classify each available version bump by its bytecode-floor blast radius
# (flat / local / cascading / excluded). Read straight from the gh-pages branch
# rather than https://status.scijava.org/, so we pick up a freshly committed
# smelt.json immediately instead of waiting on the asynchronous Pages deploy.
#
# Best-effort: if smelt.json is not published yet (or the fetch fails),
# bin/report.sh still generates the reports -- just without the classification.
smelt_url=https://raw.githubusercontent.com/scijava/status.scijava.org/gh-pages/smelt.json
mkdir -p target/pombast
curl -fsSL -o target/pombast/smelt.json "$smelt_url" ||
  echo "== smelt.json unavailable; running status without classification =="

bin/report.sh

.github/publish.sh "Update status reports" \
  target/pombast/index.html \
  target/pombast/badges.json \
  target/pombast/team.html \
  target/pombast/team.json
