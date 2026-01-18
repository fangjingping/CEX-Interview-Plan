# Third-Party Notices

This project depends on third-party open source libraries via Maven.

The authoritative dependency list is defined in the module POM files.
To generate a dependency tree:

```bash
mvn -q -DskipTests dependency:tree
```

For an attribution-friendly summary, see `docs/THIRD_PARTY_LICENSES.md`.

Known core dependencies include:
- JUnit Jupiter (tests)
- Spring Boot / Spring Cloud (demo-resilience-boot)

Trademarks: Kafka, Redis, Elasticsearch, and MySQL are trademarks of their
respective owners.
