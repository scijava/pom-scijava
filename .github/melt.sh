#!/bin/sh
uv tool install "git+https://github.com/scijava/pombast.git"
pombast melt . || true
pombast smelt --json smelt.json .
