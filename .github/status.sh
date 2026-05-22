#!/bin/sh
uv tool install "git+https://github.com/scijava/pombast.git@1cece3cab4d9be78fe06203db6f9b87d4db99f2f"
pombast status .
pombast team . --html team.html
.github/publish.sh "Update status reports" index.html team.html
