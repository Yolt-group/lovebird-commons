Lovebird-Commons
================

Custom infrastructure components and Spring Boot integrations.

## Building

```
./mvnw clean install
```

## Releasing and Merging
 See[RELEASING.md](RELEASING.md)

## Design & Architecture

With lovebird-commons we aim to achieve following objectives:

 - Provide a zero-configuration solution to run Spring Boot Micro services at Yolt. 
 - Provide a utilities to run unit tests against external cassandra, postgres or kafka instances.
 - Provide a bespoke Cassandra v3 implementation
 - Provide bespoke functionality to request and verify client-tokens
 - Provide bespoke solutions for crosscutting concerns such as logging, user-delete, and gdpr compliance
 - Provide documentation and examples on the use of lovebird-commons

A microservice at yolt should use `lovebird-commons` as a parent and include the `yolt-service-spring-boot-starter` and
`yolt-service-spring-boot-starter-test` dependencies. The starters will take care of customizing any existing Spring
components to conform to standards for the logging, error-handling, zero-trust, ect  This should in principle require no
additional configuration (currently not quite true, but that can be improved on).

## Module name conventions

Please follow these conventions:
 
* `module-name` for the module.
* `module-name-test` for tools and utilities needed toe test the module.
* `module-name-auto-configure` for the autoconfiguration to use the module
* `module-name-auto-configure-test` for the autoconfiguration to use the module when testing
* `module-name-starter` to combine the module with its autoconfiguration
* `module-name-starter-test` to combine the test module with its autoconfiguration 
