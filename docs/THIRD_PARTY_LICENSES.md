# Third-Party Licenses

This report lists direct third-party dependencies declared in Maven POMs.
Internal modules under `com.cex` are excluded.

For a full transitive license report, run:

```bash
mvn -q -DskipTests -DincludeScope=runtime -DexcludeGroupIds=com.cex \
  org.apache.maven.plugins:maven-dependency-plugin:3.6.1:list
```

## Direct dependencies

| Dependency | Scope | License |
| --- | --- | --- |
| org.junit.jupiter:junit-jupiter | test | EPL-2.0 |
| org.springframework.boot:spring-boot-starter-web | runtime | Apache-2.0 |
| org.springframework.boot:spring-boot-starter-actuator | runtime | Apache-2.0 |
| org.springframework.boot:spring-boot-starter-aop | runtime | Apache-2.0 |
| org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j | runtime | Apache-2.0 |

## Notes
- Versions are controlled by module POMs and the Spring BOMs in
  `demo-resilience-boot/pom.xml`.
- This file is intended for attribution; re-generate before releases if
  dependencies change.
