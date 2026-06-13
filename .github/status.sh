#!/bin/sh
uv tool install "git+https://github.com/scijava/pombast.git"

# Pull the most recently published smelt results so that `pombast status` can
# classify each available version bump by its bytecode-floor blast radius
# (flat / local / cascading / excluded). This is the same smelt.json the status
# page itself fetches client-side, so the generator and the browser agree.
#
# Best-effort: if smelt.json is not published yet (or the fetch fails), status
# still runs -- just without the bytecode classification overlay.
smelt_arg=""
if curl -fsSLO https://status.scijava.org/smelt.json; then
  smelt_arg="--smelt smelt.json"
else
  echo "== smelt.json unavailable; running status without classification =="
fi

pombast status $smelt_arg .
pombast badges -o badges.json .
pombast team .
.github/publish.sh "Update status reports" index.html badges.json team.html
