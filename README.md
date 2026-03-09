# judo-tatami-jsl

[![Build](https://github.com/BlackBeltTechnology/judo-tatami-jsl/actions/workflows/build.yml/badge.svg?branch=develop)](https://github.com/BlackBeltTechnology/judo-tatami-jsl/actions/workflows/build.yml)

## Introduction

Provides [JSL](https://github.com/BlackBeltTechnology/judo-meta-jsl) to [PSM](https://github.com/BlackBeltTechnology/judo-meta-psm) and [UI](https://github.com/BlackBeltTechnology/judo-meta-ui) transformations.

Supports two transformation engines:
- **ETL** (Epsilon Transformation Language) — the original engine using `.etl` rule files
- **Zeta** — a Java-based transformation framework with annotation-driven rules, offering improved performance and debuggability

The transformation engine can be selected via the `transformationMode` parameter (`ETL`, `ZETA`, or `DUAL`).

## Usage

For usage in projects please check the [judo-tatami-jsl-workflow-maven-plugin](docs/pages/judo-tatami-jsl-workflow-maven-plugin.md) documentation.

## Performance Testing

Performance and equivalence tests compare ETL and Zeta outputs:

```bash
# Run performance tests (excluded from default build)
mvn test -Pperformance -pl judo-tatami-jsl-jsl2psm
mvn test -Pperformance -pl judo-tatami-jsl-jsl2ui

# Run with external models
mvn test -Pperformance -pl judo-tatami-jsl-jsl2psm -Djudo.test.discovery.basedir=/path/to/jsl/files

# Run specific performance test
mvn test -Pperformance -pl judo-tatami-jsl-jsl2psm -Dtest=Jsl2PsmRealisticPerformanceTest
```

## Context

This project is a building block of the [judo-community](https://github.com/BlackBeltTechnology/judo-community) aggregator project. In order to better understand how this module fits into our ecosystem, please check the corresponding documentation!

## Contributing to the project

Everyone is welcome to contribute to JUDO! As a starter, please read the corresponding [CONTRIBUTING](CONTRIBUTING.md) guide for details!

## License

This project is licensed under the [Eclipse Public License - v 2.0](https://www.eclipse.org/legal/epl-2.0/).
