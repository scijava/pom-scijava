#!/bin/sh
uv tool install "git+https://github.com/scijava/pombast.git"
pombast status .
pombast badges -o badges.json .
pombast team .
.github/publish.sh "Update status reports" index.html badges.json team.html
