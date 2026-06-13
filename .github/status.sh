#!/bin/sh
uv tool install "git+https://github.com/scijava/pombast.git"

# Pull the most recently published smelt results so that `pombast status` can
# classify each available version bump by its bytecode-floor blast radius
# (flat / local / cascading / excluded). Read straight from the gh-pages branch
# rather than https://status.scijava.org/, so we pick up a freshly committed
# smelt.json immediately instead of waiting on the asynchronous Pages deploy.
#
# Best-effort: if smelt.json is not published yet (or the fetch fails), status
# still runs -- just without the bytecode classification overlay.
smelt_arg=""
smelt_url=https://raw.githubusercontent.com/scijava/status.scijava.org/gh-pages/smelt.json
if curl -fsSLO "$smelt_url"; then
  smelt_arg="--smelt smelt.json"
else
  echo "== smelt.json unavailable; running status without classification =="
fi

pombast status $smelt_arg .
pombast badges -o badges.json .
pombast team .
.github/publish.sh "Update status reports" index.html badges.json team.html
