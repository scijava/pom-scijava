[![](https://img.shields.io/maven-central/v/org.scijava/pom-scijava.svg)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22org.scijava%22%20AND%20a%3A%22pom-scijava%22)
[![](https://travis-ci.org/scijava/pom-scijava.svg?branch=master)](https://travis-ci.org/scijava/pom-scijava)

The pom-scijava project is a Maven POM that serves as the base for all
Maven-based SciJava software, including:

| Fiji | ImageJ | ImgLib2 | KNIME | LOCI | SCIFIO | SciJava | SLIM Curve |
|:----:|:------:|:-------:|:-----:|:----:|:------:|:-------:|:----------:|
| [![Fiji](http://www.scijava.org/icons/fiji-icon-64.png)](https://github.com/fiji) | [![ImageJ](http://www.scijava.org/icons/imagej2-icon-64.png)](https://github.com/imagej) | [![ImgLib2](http://www.scijava.org/icons/imglib2-icon-64.png)](https://github.com/imglib) | [![KNIME](http://www.scijava.org/icons/knime-icon-64.png)](http://www.knime.org) | [![LOCI](http://www.scijava.org/icons/loci-icon-64.png)](https://github.com/uw-loci) | [![SCIFIO](http://www.scijava.org/icons/scifio-icon-64.png)](https://github.com/scifio) | [![SciJava](http://www.scijava.org/icons/scijava-icon-64.png)](https://github.com/scijava) | [![SLIM Curve](http://www.scijava.org/icons/slim-curve-icon-64.png)](https://github.com/slim-curve) |

## pom-scijava vs. pom-scijava-base

This POM is intended for use as the parent of your own Maven-based code.

| pom-scijava-base | pom-scijava |
|:----------------:|:-----------:|
| "Low level" base POM, _without_ dependency version management. Extend pom-scijava-base only if you are a Maven expert, and have good reasons for doing so. | _Friendly_ base POM for SciJava software, _including_ dependency version management. Extend pom-scijava to inherit the unified SciJava [Bill of Materials](http://imagej.net/BOM): component versions which have been tested to work together. |

See these examples for guidance:

* [ImageJ command template](https://github.com/imagej/example-imagej-command)
* [ImageJ 1.x plugin template](https://github.com/imagej/example-legacy-plugin)
* [ImageJ tutorials](https://github.com/imagej/tutorials)

## Documentation

The pom-scijava wiki contains articles on several topics:

* [Adding a new project to the SciJava POM](https://github.com/scijava/pom-scijava/wiki/Adding-a-new-project-to-the-SciJava-POM)
* [Versioning of SciJava projects](https://github.com/scijava/pom-scijava/wiki/Versioning-of-SciJava-projects)

## Getting help with Maven

For more information about Maven, see:

* [ImageJ Maven overview](http://imagej.net/Maven)
* [ImageJ Maven FAQ](http://imagej.net/Maven_-_Frequently_Asked_Questions)
