#!/bin/sh

#
# run.sh - Tests correctness of the pom-scijava BOM.
#

die () { echo "$*" >&2; exit 1; }

# Wait for a launched background command to complete, emitting
# an occasional message to avoid long periods without output.
# Return the same exit code as the launched command.
keep_alive() {
	pid="$1"
	if [ "$pid" = "" ]
	then
		echo "[ERROR] No PID given"
		return
	fi
	i=0
	while kill -0 "$pid" 2>/dev/null; do
		i=$((i+1))
		m=$((i/60))
		s=$((i%60))
		test $s -eq 0 && echo "[$m minutes elapsed]"
		sleep 1
	done
	wait "$pid"
}

echo &&
printf 'Generating mega-melt project... ' &&

dir=$(dirname "$0")
pom="$dir/../pom.xml"
test -f "$pom" || die 'Where is pom.xml?!'

generateMegaMeltScript="$dir/generate-mega-melt.py"
filterBuildLogScript="$dir/filter-build-log.py"

megaMeltDir="$dir/../target/mega-melt"
pomParent="$megaMeltDir/../pom.xml"
versionSwapLog="$megaMeltDir/version-swap.log"
dependencyTreeLog="$megaMeltDir/dependency-tree.log"
validationLog="$megaMeltDir/validation.log"
validationErrorsLog="$megaMeltDir/validation-errors.log"
megaMeltPOM="$megaMeltDir/pom.xml"
meltingPotURL=https://raw.githubusercontent.com/scijava/scijava-scripts/main/melting-pot.sh
meltingPotScript="$megaMeltDir/melting-pot.sh"
meltingPotDir="$megaMeltDir/melting-pot"

rm -rf "$megaMeltDir" && mkdir -p "$megaMeltDir" || die "Creation of $megaMeltDir failed!"
cp "$pom" "$pomParent" &&
mvn -B -f "$pomParent" versions:set -DnewVersion=999-mega-melt > "$versionSwapLog" ||
  die "pom-scijava version update failed:\n$(cat "$versionSwapLog")"
python "$generateMegaMeltScript" "$megaMeltDir" || die 'Generation failed!'
echo 'Done!'

# Ensure the mega-melt dependency structure validates.
# In particular, this runs our enforcer rules: the build
# will fail if there are any snapshot dependencies, or
# any duplicate classes across artifacts on the classpath.
echo &&
printf 'Validating mega-melt project... ' &&
mvn -B -f "$megaMeltPOM" dependency:tree > "$dependencyTreeLog" ||
  die "Invalid dependency tree:\n$(cat "$dependencyTreeLog")"
mvn -B -f "$megaMeltPOM" -U clean package > "$validationLog" || {
  python "$filterBuildLogScript" "$validationLog" > "$validationErrorsLog"
  die "Validation build failed!\n\nDependency tree:\n$(cat "$dependencyTreeLog")\n\nBuild log:\n$(cat "$validationErrorsLog")"
}
echo 'Done!'

# Run mega-melt through the melting pot, with all SciJava-affiliated groupIds,
# minus excluded artifacts (see ignoredArtifacts in generate-mega-melt.py).
echo &&
echo 'Generating melting pot...' &&
curl -fsL "$meltingPotURL" > "$meltingPotScript" &&
chmod +x "$meltingPotScript" &&
"$meltingPotScript" "$megaMeltDir" \
  -o "$meltingPotDir" \
  -i 'ca.mcgill:*' \
  -i 'io.scif:*' \
  -i 'jitk:*' \
  -i 'mpicbg:*' \
  -i 'net.imagej:*' \
  -i 'net.imglib2:*' \
  -i 'net.preibisch:*' \
  -i 'org.bonej:*' \
  -i 'org.janelia.saalfeldlab:*' \
  -i 'org.janelia:*' \
  -i 'org.scijava:*' \
  -i 'sc.fiji:*' \
  -e 'org.janelia.saalfeldlab:n5-blosc' \
  -e 'org.janelia.saalfeldlab:n5-zarr' \
  -e 'org.scijava:j3dcore' \
  -e 'org.scijava:j3dutils' \
  -e 'org.scijava:jep' \
  -e 'org.scijava:junit-benchmarks' \
  -e 'org.scijava:vecmath'  \
  -f -v -s $@ || die 'Melting pot build failed!'

# HACK: Remove known-duplicate artifactIds from version property overrides.
# The plan is for this step to become unnecessary once the melting pot has
# been improved to include a strategy for dealing with components with same
# artifactId but different groupIds. For now, we just prune these overrides.
meltScript="$meltingPotDir/melt.sh"
buildScript="$meltingPotDir/build.sh"
buildScriptBackup="$buildScript.original"
echo &&
printf 'Adjusting melting pot build script... ' &&
mv "$buildScript" "$buildScriptBackup" &&
awk '!/-D(annotations|antlr|jocl|kryo|minlog|opencsv|trove4j)\.version/' "$buildScriptBackup" > "$buildScript" ||
  die 'Error adjusting melting pot build script!'
echo 'Done!'

# Run the melting pot now, unless -s flag was given.
doMelt=t
for arg in "$@"
do
  if [ "$arg" = '-s' ] || [ "$arg" = '--skipBuild' ]
  then
    doMelt=
  fi
done
if [ "$doMelt" ]
then
  echo &&
  (cd "$meltingPotDir" && sh melt.sh) & keep_alive $! || die 'Melting pot failed!'
else
  echo &&
  echo 'Melting the pot... SKIPPED'
fi

# Complete!
echo
echo 'All checks succeeded! :-D'
