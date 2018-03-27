[![](https://img.shields.io/maven-central/v/org.scijava/pom-scijava.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.scijava%22%20AND%20a%3A%22pom-scijava%22)
[![](https://travis-ci.org/scijava/pom-scijava.svg?branch=master)](https://travis-ci.org/scijava/pom-scijava)

This POM provides a parent from which SciJava-based projects can declare their build configurations. It ensures that projects all use a compatible build environment, as well as versions of dependencies and plugins. Projects extending this POM inherit the unified SciJava [Bill of Materials](http://imagej.net/BOM): component versions which have been tested to work together.

This POM serves as the base for all Maven-based SciJava software, including:

| Fiji | ImageJ | ImgLib2 | KNIME | LOCI | SCIFIO | SciJava | SLIM Curve |
|:----:|:------:|:-------:|:-----:|:----:|:------:|:-------:|:----------:|
| [![Fiji](http://www.scijava.org/icons/fiji-icon-64.png)](https://github.com/fiji) | [![ImageJ](http://www.scijava.org/icons/imagej2-icon-64.png)](https://github.com/imagej) | [![ImgLib2](http://www.scijava.org/icons/imglib2-icon-64.png)](https://github.com/imglib) | [![KNIME](http://www.scijava.org/icons/knime-icon-64.png)](http://www.knime.org) | [![LOCI](http://www.scijava.org/icons/loci-icon-64.png)](https://github.com/uw-loci) | [![SCIFIO](http://www.scijava.org/icons/scifio-icon-64.png)](https://github.com/scifio) | [![SciJava](http://www.scijava.org/icons/scijava-icon-64.png)](https://github.com/scijava) | [![SLIM Curve](http://www.scijava.org/icons/slim-curve-icon-64.png)](https://github.com/slim-curve) |

This POM is intended for use as the parent of your own Maven-based code.

## Examples

* [ImageJ command template](https://github.com/imagej/example-imagej-command)
* [ImageJ 1.x plugin template](https://github.com/imagej/example-legacy-plugin)
* [ImageJ tutorials](https://github.com/imagej/tutorials/tree/master/maven-projects)

## Documentation

The pom-scijava wiki contains articles on several topics:

* [Adding a new project to the SciJava POM](https://github.com/scijava/pom-scijava/wiki/Adding-a-new-project-to-the-SciJava-POM)
* [Versioning of SciJava projects](https://github.com/scijava/pom-scijava/wiki/Versioning-of-SciJava-projects)

## Getting help with Maven

For more information about Maven, see:

* [ImageJ Maven overview](http://imagej.net/Maven)
* [ImageJ Maven FAQ](http://imagej.net/Maven_-_Frequently_Asked_Questions)
