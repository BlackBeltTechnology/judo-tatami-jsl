# Contributing to JUDO

## Development Environment Setup

Your development environment must meet the requirements described in the parent project's [CONTRIBUTING guide](https://github.com/BlackBeltTechnology/judo-community/blob/develop/CONTRIBUTING.adoc). In summary:

- **Java 21** JDK
- **Maven 3.9.4+**

## Project Structure

This project follows a standard Maven multi-module layout. See the [README](README.md) for the module dependency diagram and purpose of each module.

> **Note:** TODO JNG-3832 — the code structure documentation for individual submodule functionality is being improved.

## Submitting an Issue

Before opening a new issue, search the [issue tracker](https://github.com/BlackBeltTechnology/judo-tatami-jsl/issues) — your problem may already be reported or resolved.

To help us reproduce and fix bugs quickly, please include:

- Output of `java -version` and `mvn -version`
- Your `pom.xml` or `.flattened-pom.xml` (when applicable)
- A **minimal reproduction case** that demonstrates the failure

We will ask for a minimal reproduction if one is not provided, because isolating the problem is the fastest path to a fix.

[Open a new issue](https://github.com/BlackBeltTechnology/judo-tatami-jsl/issues/new/choose)

## Submitting a Pull Request

This project follows [GitHub's standard forking model](https://guides.github.com/activities/forking/). Fork the repository, make your changes, and submit a pull request.

## Common Commands

### Run Tests

```bash
mvn clean test
```

### Full Build

```bash
mvn clean install
```
