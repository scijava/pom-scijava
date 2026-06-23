#!/bin/sh
# Validate a Maven BOM's managed components against each other:
#
#   melt  -- mega-melt classpath check (duplicate classes, SNAPSHOTs, enforcer)
#   smelt -- per-component build + test against the BOM-pinned dependency set
#
# Both phases always run, so a failure in one (e.g. duplicate classes in melt)
# never hides real incompatibilities surfaced by the other. The script exits
# non-zero if either phase found a problem.
#
# Run from a BOM checkout (e.g. a pom-scijava working copy):
#
#   bin/check.sh
#
# Outputs land under target/pombast/ (see pombast.toml).
command -v pombast >/dev/null 2>&1 ||
  uv tool install "git+https://github.com/scijava/pombast.git"

rc=0
pombast melt  || rc=$?
pombast smelt || rc=$?
exit $rc
