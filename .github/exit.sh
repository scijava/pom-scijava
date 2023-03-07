#!/bin/sh
test -f exit-code || {
  echo "[ERROR] No build exit code was saved!"
  exit 255
}
exitCode=$(cat exit-code)
rm -f exit-code
exit $exitCode
