#!/bin/sh
uv tool install "git+https://github.com/scijava/pombast.git@66a1e3abff431846a900703f7450e0c21f1456cc"
pombast smelt --config pombast.toml --json smelt.json .
