# Instructions for candidates

This is the Java version of the Payment Gateway challenge. If you haven't already read this [README.md](https://github.com/cko-recruitment/) on the details of this exercise, please do so now.

## Requirements
- JDK 17
- Docker

## Template structure

src/ - A skeleton SpringBoot Application

test/ - Some simple JUnit tests

imposters/ - contains the bank simulator configuration. Don't change this

.editorconfig - don't change this. It ensures a consistent set of rules for submissions when reformatting code

docker-compose.yml - configures the bank simulator


## API Documentation
For documentation openAPI is included, and it can be found under the following url: **http://localhost:8090/swagger-ui/index.html**

**Feel free to change the structure of the solution, use a different library etc.**

## Testing
- Unit tests: `./gradlew test`
- Integration tests: `./gradlew integrationTest`
- API contract tests are part of integration tests (`src/integrationTest/resources/contracts/`).
- E2E tests (requires bank simulator):
  - `docker-compose up`
  - `./gradlew e2eTest`
- Merged coverage (unit+integration+e2e when available): `./gradlew jacocoMergedReport`

## Configuration
- Supported currencies (max 3): `payment.supported-currencies=USD,EUR,GBP`
