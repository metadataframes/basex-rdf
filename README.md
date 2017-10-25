# basex-rdf
## RDF parsing for XQuery in BaseX

## Table of Contents
- [Status](#status)
- [Installation](#installation)
  - [Dependencies](#dependencies)
  - [Packaging](#packaging)   
- [Usage](#usage)
- [Getting started](#getting-started)
- [TODO](#todo)
# - [Contributing](#contributing)
- [License](#license)

This is an extension module for parsing RDF data with the [BaseX XQuery processor](https://github.com/BaseXdb/basex). It provides an XQuery wrapper for a Java parser generated by Gunther Rademacher's [REx Parser Generator](http://www.bottlecaps.de/rex/). The parser was generated from the EBNF for the [TriG serialization of RDF](https://www.w3.org/TR/trig/). The TriG grammar provides a syntax for encoding named graphs, as an extension of the Turtle and N-Triples serializations. The parser itself will generate XML parse trees for any of these three serializations. However, the raw parse trees are not particularly useful and must be normalized before being further processed and queried. To date, development work has focused exclusively on the Turtle format. 

## Installation
### Dependencies
### Packaging

## Usage

## Getting started

## TODO

## License

 