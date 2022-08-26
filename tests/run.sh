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
pomParent="$megaMeltDir/../pom.xml"
versionSwapLog="$megaMeltDir/version-swap.log"
dependencyTreeLog="$megaMeltDir/dependency-tree.log"
validationLog="$megaMeltDir/validation.log"
validationErrorsLog="$megaMeltDir/validation-errors.log"
megaMeltPOM="$megaMeltDir/pom.xml"
meltingPotLocal="$dir/../../scijava-scripts/melting-pot.sh"
meltingPotURL=https://raw.githubusercontent.com/scijava/scijava-scripts/main/melting-pot.sh
meltingPotScript="$megaMeltDir/melting-pot.sh"
meltingPotLog="$megaMeltDir/melting-pot.log"
meltingPotDir="$megaMeltDir/melting-pot"
skipTestsFile="$meltingPotDir/skipTests.txt"
meltScript="$meltingPotDir/melt.sh"

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
if [ -e "$meltingPotLocal" ]
then
  cp "$meltingPotLocal" "$meltingPotScript"
else
  curl -fsL "$meltingPotURL" > "$meltingPotScript"
fi ||
  die "Failed to obtain melting pot script!"

# Build the melting pot structure.
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
  -i 'org.morphonets:*' \
  -i 'org.scijava:*' \
  -i 'sc.fiji:*' \
  -e 'net.imagej:ij' \
  -e 'org.scijava:j3dcore' \
  -e 'org.scijava:j3dutils' \
  -e 'org.scijava:jep' \
  -e 'org.scijava:junit-benchmarks' \
  -e 'org.scijava:vecmath' \
  -f -v -s $@ 2>&1 | tee "$meltingPotLog"

# NB: The pipe to tee eats the melting-pot error code.
# Even with the POSIX-unfriendly pipefail flag set.
# So we resort to this hacky error check of the log.
grep -qF "[ERROR]" "$meltingPotLog" &&
  die 'Melting pot generation failed!'

# HACK: Remove known-duplicate artifactIds from version property overrides.
# The plan is for this step to become unnecessary once the melting pot has
# been improved to include a strategy for dealing with components with same
# artifactId but different groupIds. For now, we just prune these overrides.
buildScript="$meltingPotDir/build.sh"
buildScriptTemp="$buildScript.tmp"
echo &&
printf 'Adjusting melting pot build script... ' &&
cp "$buildScript" "$buildScript.original" &&
mv -f "$buildScript" "$buildScriptTemp" &&
awk '!/-D(annotations|antlr|jocl|kryo|minlog|opencsv|trove4j)\.version/' "$buildScriptTemp" > "$buildScript" &&
# HACK: Add leading underscore to version properties that start with a digit.
mv -f "$buildScript" "$buildScriptTemp" &&
sed -E 's; -D([0-9][^ ]*);& -D_\1;' "$buildScriptTemp" > "$buildScript" &&
# HACK: Add non-standard version properties used prior to
# pom-scijava 32.0.0-beta-1; see d0bf752070d96a2613c42e4e1ab86ebdd07c29ee.
mv -f "$buildScript" "$buildScriptTemp" &&
sed -E 's; -Dsc.fiji.3D_Blob_Segmentation\.version=([^ ]*);& -DFiji_3D_Blob_Segmentation.version=\1;' "$buildScriptTemp" > "$buildScript" &&
mv -f "$buildScript" "$buildScriptTemp" &&
sed -E 's; -Dsc.fiji.(3D_Objects_Counter|3D_Viewer)\.version=([^ ]*);& -DImageJ_\1.version=\2;' "$buildScriptTemp" > "$buildScript" &&
# HACK: Add non-standard net.imagej:ij version property used prior to
# pom-scijava 28.0.0; see 7d2cc442b107b3ac2dcb799d282f2c0b5822649d.
mv -f "$buildScript" "$buildScriptTemp" &&
sed -E 's; -Dij\.version=([^ ]*);& -Dimagej1.version=\1;' "$buildScriptTemp" > "$buildScript" &&
rm "$buildScriptTemp" || die 'Error adjusting melting pot build script!'
echo 'Done!'

echo &&

# HACK: Adjust component POMs to satisfy Maven HTTPS strictness.
echo &&
printf 'Adjusting melting pot project POMs... ' &&
find "$meltingPotDir" -name pom.xml | while read pom
do
  mv "$pom" "$pom.original" &&
  sed -E -e 's_(https?://maven.imagej.net|http://maven.scijava.org)_https://maven.scijava.org_g' \
    -e 's_http://maven.apache.org/xsd_https://maven.apache.org/xsd_g' "$pom.original" > "$pom" ||
    die "Failed to adjust $pom"
done
echo 'Done!'

# HACK: Skip tests for projects with known problems.

echo &&
printf 'Adjusting melting pot melt script... ' &&
mv "$meltScript" "$meltScript.original" &&
sed 's_\s*sh "$dir/build.sh"_\
# HACK: If project is on the skipTests list, then skip the tests.\
buildFlags=\
grep -qF ":$f:" $dir/skipTests.txt \&\& buildFlags=-DskipTests\
\
& $buildFlags_' "$meltScript.original" > "$meltScript" ||
  die "Failed to adjust $meltScript"
echo 'Done!'

# com.amazonaws.services.s3.model.AmazonS3Exception: The specified bucket does
# not exist (Service: Amazon S3; Status Code: 404; Error Code: NoSuchBucket;
# Request ID: null; S3 Extended Request ID: null; Proxy: null)
echo ":org.janelia.saalfeldlab/n5-aws-s3:" > "$skipTestsFile"

# java.lang.UnsatisfiedLinkError: Unable to load library 'blosc'
echo ":org.janelia.saalfeldlab/n5-blosc:" >> "$skipTestsFile"
echo ":org.janelia.saalfeldlab/n5-zarr:" >> "$skipTestsFile"

# Error while checking the CLIJ2 installation: null
echo ":sc.fiji/labkit-pixel-classification:" >> "$skipTestsFile"

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
  cd "$meltingPotDir" && sh melt.sh || die 'Melting pot failed!'
else
  echo &&
  echo 'Melting the pot... SKIPPED'
fi

# Complete!
echo
echo 'All checks succeeded! :-D'
