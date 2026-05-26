#!/bin/sh
uv tool install "git+https://github.com/scijava/pombast.git"
pombast status .
pombast team . --html team.html
.github/publish.sh "Update status reports" index.html team.html
