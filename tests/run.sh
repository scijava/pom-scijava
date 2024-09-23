#!/bin/sh

#
# run.sh - Tests correctness of the pom-scijava BOM.
#

die () { echo "$*" >&2; exit 1; }

sectionStart() {
  startTime=$(date +%s)
  echo
  printf "$1... "
}

sectionEnd() {
  endTime=$(date +%s)
  echo "Done! [$((endTime-startTime))s]"
}

sectionStart 'Generating mega-melt project'

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

# HACK: List of artifacts with known-duplicate short version properties.
shortVersionClashes=\
'net.sourceforge.findbugs.annotations'\
'|antlr.antlr'\
'|org.jogamp.jocl.jocl'\
'|net.sf.opencsv.opencsv'\
'|org.jetbrains.intellij.deps.trove4j'

rm -rf "$megaMeltDir" && mkdir -p "$megaMeltDir" || die "Creation of $megaMeltDir failed!"
cp "$pom" "$pomParent" &&
mvn -B -f "$pomParent" versions:set -DnewVersion=999-mega-melt > "$versionSwapLog" &&
  mvn -B -f "$pomParent" install:install >> "$versionSwapLog" ||
  die "pom-scijava version update failed:\n$(cat "$versionSwapLog")"
python "$generateMegaMeltScript" "$megaMeltDir" || die 'Generation failed!'
sectionEnd # Generating mega-melt project

# Ensure the mega-melt dependency structure validates.
# In particular, this runs our enforcer rules: the build
# will fail if there are any snapshot dependencies, or
# any duplicate classes across artifacts on the classpath.
sectionStart 'Validating mega-melt project'
mvn -B -f "$megaMeltPOM" dependency:tree > "$dependencyTreeLog" ||
  die "Invalid dependency tree:\n$(cat "$dependencyTreeLog")"
mvn -B -f "$megaMeltPOM" -U clean package > "$validationLog" || {
  python "$filterBuildLogScript" "$validationLog" > "$validationErrorsLog"
  die "Validation build failed!\n\nDependency tree:\n$(cat "$dependencyTreeLog")\n\nBuild log:\n$(cat "$validationErrorsLog")"
}
sectionEnd # Validating mega-melt project

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
  -r scijava.public::::https://maven.scijava.org/content/groups/public \
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
test "$(grep -F "[ERROR]" "$meltingPotLog" | grep -v "using default branch")" &&
  die 'Melting pot generation failed!'

sectionStart 'Adjusting the melting pot: build.sh script'

buildScript="$meltingPotDir/build.sh"
buildScriptTemp="$buildScript.tmp"
cp "$buildScript" "$buildScript.original" &&

# HACK: Remove known-duplicate short version properties, keeping
# the short version declaration only for the more common groupId.
# E.g.: org.antlr:antlr is preferred over antlr:antlr, so we set
# antlr.version to match org.antlr:antlr, not antlr:antlr.
mv -f "$buildScript" "$buildScriptTemp" &&
sed -E 's;(-D('"$shortVersionClashes"').version=[^ ]*) -D[^ ]*;\1;' "$buildScriptTemp" > "$buildScript" &&

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

# HACK: Add explicit kotlin.version to match our pom-scijava-base.
# Otherwise, components built on older pom-scijava-base will have
# mismatched kotlin component versions. The sed expression avoids
# a bug in mvn's batch mode that results in <ESC>[0m<ESC>[0m still
# appearing as a leading ANSI sequence when echoing the property.
kotlinVersion=$(
  mvn -B -U -q -Denforcer.skip=true -Dexec.executable=echo \
  -Dexec.args='${kotlin.version}' --non-recursive validate exec:exec 2>&1 |
  head -n1 | sed 's;\(.\[[0-9]m\)*;;') &&
# TEMP: Also fix the version of maven-enforcer-plugin, to prevent n5 from
# overriding it with a too-old version. Even though we pass enforcer.skip,
# so that the enforcer plugin does not actually do any checking, this version
# mismatch still triggers a problem:
#
#     [ERROR] Failed to execute goal
#     org.apache.maven.plugins:maven-enforcer-plugin:3.0.0-M3:enforce
#     (enforce-rules) on project n5-blosc: Unable to parse configuration of
#     mojo org.apache.maven.plugins:maven-enforcer-plugin:3.0.0-M3:enforce
#     for parameter banDuplicateClasses: Cannot create instance of interface
#     org.apache.maven.enforcer.rule.api.EnforcerRule:
#     org.apache.maven.enforcer.rule.api.EnforcerRule.<init>() -> [Help 1]
#
# Once n5 components stop doing that version pin, we can remove this here.
enforcerVersion=$(
  mvn -B -U -q -Denforcer.skip=true -Dexec.executable=echo \
  -Dexec.args='${maven-enforcer-plugin.version}' --non-recursive validate exec:exec 2>&1 |
  head -n1 | sed 's;\(.\[[0-9]m\)*;;') &&
mv -f "$buildScript" "$buildScriptTemp" &&
sed -E "s;mvn -Denforcer.skip;& -Dmaven-enforcer-plugin.version=$enforcerVersion -Dkotlin.version=$kotlinVersion;" "$buildScriptTemp" > "$buildScript" &&

chmod +x "$buildScript" &&
rm "$buildScriptTemp" ||
  die 'Error adjusting melting pot build script!'

sectionEnd # Adjusting the melting pot: build.sh script

sectionStart 'Adjusting the melting pot: component POMs'

# HACK: Adjust component POMs to satisfy Maven HTTPS strictness.
find "$meltingPotDir" -name pom.xml |
  while read pom
do
  mv "$pom" "$pom.original" &&
  sed -E -e 's_(https?://maven.imagej.net|http://maven.scijava.org)_https://maven.scijava.org_g' \
    -e 's_http://maven.apache.org/xsd_https://maven.apache.org/xsd_g' "$pom.original" > "$pom" ||
    die "Failed to adjust $pom"
done

# HACK: Make component POMs extend the same version of pom-scijava
# being tested. This reduces dependency skew for transitively inherited
# components that were not managed at the time of that component release.
find "$meltingPotDir" -name pom.xml | while read pom
do
  perl -0777 -i -pe 's/(<parent>\s*<groupId>org.scijava<\/groupId>\s*<artifactId>pom-scijava<\/artifactId>\s*<version>)[^\n]*/${1}999-mega-melt<\/version>/igs' "$pom"
done

sectionEnd # Adjusting the melting pot: component POMs

sectionStart 'Adjusting the melting pot: melt.sh script'

# HACK: Skip tests for projects with known problems.

mv "$meltScript" "$meltScript.original" &&
sed 's_\s*"$dir/build.sh"_\
# HACK: If project is on the skipTests list, then skip the tests.\
buildFlags=-Djava.awt.headless=true\
grep -qxF "$f" $dir/skipTests.txt \&\& buildFlags=-DskipTests\
\
& $buildFlags_' "$meltScript.original" > "$meltScript" &&
chmod +x "$meltScript" ||
  die "Failed to adjust $meltScript"

sectionEnd # Adjusting the melting pot: melt.sh script

sectionStart 'Adjusting the melting pot: unit test hacks'

# HACK: Remove flaky tests from imagej-ops builds.
# CachedOpEnvironmentTest fails intermittently. Of course, it should be
# somehow fixed in imagej-ops. But for now, let's not let it ruin the melt.
rm -f "$meltingPotDir/net.imagej/imagej-ops/src/test/java/net/imagej/ops/cached/CachedOpEnvironmentTest.java"

# TEMP: Until saalfeldlab/n5-aws-s3#13 is released.
echo "org.janelia.saalfeldlab/n5-aws-s3" >> "$skipTestsFile" &&

# Error while checking the CLIJ2 installation: null
echo "sc.fiji/labkit-pixel-classification" >> "$skipTestsFile" ||
  die "Failed to generate $skipTestsFile"

sectionEnd # Adjusting the melting pot: unit test hacks

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
  echo
  cd "$meltingPotDir"
  sh melt.sh
  meltResult=$?

  # Dump logs for failing builds and/or tests.
  for d in */*
  do
    test -d "$d" || continue

    # Check for failing build log.
    buildLog="$d/build.log"
    if [ -f "$buildLog" ]
    then
      if grep -qF 'BUILD FAILURE' "$buildLog"
      then
        echo
        echo "[$buildLog]"
        cat "$buildLog"
      fi
    fi

    # Check for failing test logs.
    testLogsDir="$dir/target/surefire-reports"
    if [ -d "$testLogsDir" ]
    then
      find "$testLogsDir" -name '*.txt' |
        while read report
      do
        if grep -qF 'FAILURE!' "$report"
        then
          echo
          echo "[$report]"
          cat "$report"
        fi
      done
    fi
  done

  # Terminate the script with same exit code if the melt failed.
  test "$meltResult" -eq 0 || exit "$meltResult"
else
  echo &&
  echo 'Melting the pot... SKIPPED'
fi

# Complete!
echo
echo 'All checks succeeded! :-D'
