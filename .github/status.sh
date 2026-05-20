#!/bin/sh
uv tool install "git+https://github.com/scijava/pombast.git@66a1e3abff431846a900703f7450e0c21f1456cc"
pombast status .
commitNote="$(TZ=UTC date +'%Y-%m-%d %H:%M:%S UTC')"
.github/publish.sh index.html "Update component table ($commitNote)"
