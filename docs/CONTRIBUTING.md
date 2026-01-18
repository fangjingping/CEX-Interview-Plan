# Contributing

Thanks for helping improve this repo. It is a multi-module Maven project that
focuses on a CEX trading system interview preparation path.

## Setup
- Java 17
- Maven 3.9+

## Project layout
- Modules live in top-level directories.
- New docs and tasks go under `docs/` and should be linked from `README.md`.

## Development
- Run all tests: `mvn test`
- Run a module: `mvn -pl matching-engine test`
- Install pre-commit hook: `scripts/install-hooks.sh`

## Code style
- Java 17, 4-space indent, braces on the same line.
- Use `BigDecimal` for prices and quantities.
- Tests use JUnit 5 and name methods by behavior.

## Pull request checklist
- Tests pass (`mvn test` or module-level tests).
- Docs updated (`docs/` + `README.md` index).
- Behavior changes described in the PR.
