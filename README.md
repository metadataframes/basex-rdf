# basex-rdf
## RDF parsing for XQuery (in BaseX)

## Table of Contents
- [Status](#status)
- [Dependencies](#dependencies)
- [Packaging](#packaging)
- [Installation](#installation)   
- [Usage](#usage)
  - [Namespaces](#namespaces)
  - [Functions](#functions)    
- [Getting started](#getting-started)
- [TODO](#todo)
- [License](#license)

## Overview and status

This is an extension module for parsing RDF data with the [BaseX XQuery processor](https://github.com/BaseXdb/basex). It provides an XQuery wrapper for a Java parser generated by Gunther Rademacher's [REx Parser Generator](http://www.bottlecaps.de/rex/). The parser was generated from the EBNF for the [TriG serialization of RDF](https://www.w3.org/TR/trig/), which provides a syntax for encoding named graphs, as an extension of the Turtle and N-Triples serializations. The parser itself will generate XML parse trees for any of these three serializations. However, the raw parse trees are not particularly useful and need to be normalized before being further processed and queried. To date, development work has focused exclusively on the Turtle format.

## Dependencies
* BaseX 9.0 (currently in beta; see [latest development snapshot](http://files.basex.org/releases/latest/))
* Saxon-HE 9.8.x (availabe for download from [SourceForge](https://sourceforge.net/projects/saxon/files/latest/download?source=files))

## Packaging
* By default, the BaseX 9.0 [combined packaging feature](http://docs.basex.org/wiki/Repository#Combined) is used. This feature optimizes the packaging of Java extension code in BaseX.
* Alternative packages, such as the EXPath packaging model, may be added in the future (although they would still be BaseX specific).

## Installation
See the [BaseX wiki](http://docs.basex.org/wiki/Main_Page) for detailed documentation about installing and using BaseX.

* Once BaseX has been downloaded, the easiest way to add the `basex-rdf` module is by launching the BaseX GUI. From the `Options` menu, select `Packages` and install the `Graphs.jar` file from the [repo](https://github.com/metadataframes/basex-rdf/tree/master/src/repo) directory of this repository.
* Before executing functions from the module, ensure that the Saxon-HE 9.8.x JAR file is saved in the `lib` subdirectory of the BaseX installation directory.

## Usage
Currently, `basex-rdf` includes an XQuery library module, `basex-rdf.xqm`, and two XSLT stylesheets, `process.xsl` and `postprocess.xsl`. The first stylesheet (`process.xsl`) normalizes the raw XML parse tree, and the second exposes some simple abstractions for querying the RDF data. The XQuery library module acts as a controller for calling the stylesheets.

### Namespaces
The parser component of the combined Java/XQuery module is bound to the following namespace:

`http://basex.org/modules/rdf/Graphs` (here bound to the prefix "graphs")

The XQuery library module takes the following namespace:

`https://metadatafram.es/basex/modules/rdf/graphs/` (here bound to the prefix "basex-rdf")

### Functions
```
graphs:parse(xs:string)
  Parses RDF data as a string and returns an XML parse tree.

basex-rdf:transform(xs:string)
  Accepts a string with RDF data and calls graphs:parse() to return an XML parse tree. Passes the parsed data to the process.xsl stylesheet.



```

## Getting started

## TODO

## License

 