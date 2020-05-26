#!/bin/sh

#
# run.sh - Tests correctness of the pom-scijava BOM.
#

die () { echo "$*" >&2; exit 1; }

echo &&
printf 'Generating mega-melt project... ' &&

dir=$(dirname "$0")
pom="$dir/../pom.xml"
test -f "$pom" || die 'Where is pom.xml?!'

generateMegaMeltScript="$dir/generate-mega-melt.py"
filterBuildLogScript="$dir/filter-build-log.py"

megaMeltDir="$dir/../target/mega-melt"
versionSwapLog="$megaMeltDir/version-swap.log"
dependencyTreeLog="$megaMeltDir/dependency-tree.log"
validationLog="$megaMeltDir/validation.log"
validationErrorsLog="$megaMeltDir/validation-errors.log"
megaMeltPOM="$megaMeltDir/pom.xml"
meltingPotURL=https://raw.githubusercontent.com/scijava/scijava-scripts/master/melting-pot.sh
meltingPotScript="$megaMeltDir/melting-pot.sh"

rm -rf "$megaMeltDir" && mkdir -p "$megaMeltDir" || die "Creation of $megaMeltDir failed!"
mvn -f "$pom" versions:set -DnewVersion=999-mega-melt > "$versionSwapLog" &&
mvn -f "$pom" install >> "$versionSwapLog" ||
  die "pom-scijava version swap failed:\n$(cat "$versionSwapLog")"
mv -f "$pom.versionsBackup" "$pom" || die 'POM restoration failed!'
python "$generateMegaMeltScript" "$megaMeltDir" || die 'Generation failed!'
echo 'Done!'

# Ensure the mega-melt dependency structure validates.
# In particular, this runs our enforcer rules: the build
# will fail if there are any snapshots dependencies, or
# any duplicate classes across artifacts on the classpath.
echo &&
printf 'Validating mega-melt project... ' &&
mvn -f "$megaMeltPOM" dependency:tree > "$dependencyTreeLog" ||
  die "Invalid dependency tree:\n$(cat "$dependencyTreeLog")"
mvn -f "$megaMeltPOM" -U clean package > "$validationLog" || {
  python "$filterBuildLogScript" "$validationLog" > "$validationErrorsLog"
  die "Validation build failed!\n\nDependency tree:\n$(cat "$dependencyTreeLog")\n\nBuild log:\n$(cat "$validationErrorsLog")"
}
echo 'Done!'

# Run mega-melt through the melting pot,
# with all SciJava-affiliated groupIds.
echo &&
echo 'Executing melting pot...' &&
curl -fsL "$meltingPotURL" > "$meltingPotScript" &&
chmod +x "$meltingPotScript" &&
"$meltingPotScript" "$megaMeltDir" \
  -o "$megaMeltDir/melting-pot" \
  -i 'ca.mcgill:*' \
  -i 'graphics.scenery:*' \
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
  -i 'sc.iview:*' \
  -f -v $@ || die 'Melting pot build failed!'

echo
echo 'All checks succeeded! :-D'
