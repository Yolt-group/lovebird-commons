# Changelog / Migration guide

# Unreleased

# 14.0.10
- Update dependency org.springframework.boot:spring-boot-starter-parent to v2.7.3

# 14.0.9
- Update dependency org.yaml.snakeyaml to v1.31

# 14.0.7
- Update dependency org.springframework.boot:spring-boot-starter-parent to v2.7.3

# 14.0.6
- Update dependency org.springframework.boot:spring-boot-starter-parent to v2.7.2
- Update dependency software.amazon.awssdk:bom to v2.17.247 
- Fix CVE-2022-31197

# 14.0.5
- Added client group name claim to the ClientToken, ClientGroupToken and ClientUserToken
- Upgrade awssdk from 2.17.224 to 2.17.236

# 14.0.3
- Added client name claim to the ClientToken

# 14.0.2
- Replaced `testsupport-logging` with `logging-test`. See `CaptureLogEventsDemoTest`.

# 14.0.1
- Add risk insights claim

# 14.0.0
- Upgrade to `spring-boot:2.7.0` see [the release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes)
  - [Migrate from WebSecurityConfigurerAdapter to SecurityFilterChain](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#migrating-from-websecurityconfigureradapter-to-securityfilterchain)
  - [Rename clientName to client_name in metrics](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#metric-tag-keys-renamed)
    - Used in performance/performance-metrics in grafana
    - Used in engineering/pod-dependencies  in grafana
  - [Remove deprecated methods](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#deprecations-from-spring-boot-25)
- Removed module `datetime`
  
# 13.0.25
- Support client-tokens encoded as JSON Strings 

# 13.0.24
- Support `@RestClientTest` slices with test-client-tokens 

# 13.0.23
- Change token claim `ais_consent_starter` into `consent_starter`

# 13.0.22
- Remove exploitable kafka header mapper. 
  - Only using `String` objects as headers
  - Use a `Converter` to safely convert into an expected type

# 13.0.21
- Update dependency `org.springframework.boot:spring-boot-starter-parent` to v2.6.8

# 13.0.20
- Added `springdoc-openapi-security` to dependency management
- Updated `springdoc-openapi` version to `1.6.9`
- Renamed Maven property `springdoc-openapi-ui.version` to `springdoc-openapi.version`

# 13.0.19
- Removed unused `user-context` module
- Removed unused `user-context-authentication` module

# 13.0.18

- Add `ClientToken::hasAISConsentStarter`

# 13.0.16

- Removed unused `testsupport-clienttokens` module
- Removed unused `user-context` module from `compliance` module

# 13.0.15

- Added `yolt-client-tokens-starter-test` module
- Deprecated `test-support-clienttokens`

# 13.0.14
- Configure kafka secrets directories in `yolt-team-starter`
- 
# 13.0.13
- Read `client-token-req-jwks` using `VaultKeys`.
- Configure all secrets directories in `yolt-team-starter`

# 13.0.12
- Remove log splitting workaround. A single line has unbounded size. 

# 13.0.11
- Update dependency `org.springframework.boot:spring-boot-starter-parent` to v2.6.6

# 13.0.10
- Update dependency `org.springframework.boot:spring-boot-starter-parent` to v2.6.5
- Update dependency `software.amazon.awssdk:bom` to v2.17.153
- Update dependency `org.bitbucket.b_c:jose4j` to v0.7.11 
- Removed `YoltPostgresProperties`
  - `logErrorDetail` is set through the spring property `spring.datasource.hikari.data-source-properties.logServerErrorDetail`
  - `logErrorDetail` is defaults to `false` when not set. 

# 13.0.9
- Remove delete-user artifact from the yolt-spring-boot-starter. 
- Add client-token support to delete-user so services can register a function that accepts a clientToken rather than a userId.

# 13.0.7
- Log error when attempting to deserialize a complex object from kafka headers (this will not break any pods)
  - For `UserContext` use `.toJson()`   
  - For client tokens use `.getSerialized()`

# 13.0.6
- Update dependency com.datastax.cassandra:cassandra-driver-core to 3.11.1
- Update dependency com.datastax.cassandra:cassandra-driver-mapping to 3.11.1
- Update dependency com.datastax.cassandra:cassandra-driver-extras to 3.11.1

# 13.0.5
- fix YoltKafkaAutoConfiguration for new client token types which should be sent as a serialized token

# 13.0.4

- Log a clear error when a .cql file contains a change that cannot be applied.
- Update dependency io.swagger:swagger-annotations to v1.6.5
- Update dependency io.swagger.core.v3:swagger-annotations to v2.1.13

# 13.0.3

- Added ClientUserToken to client-tokens
- Remove unused
  - `nl.ing.lovebird.rest.usercontext.KYCAccepted`

# 13.0.2
- Removed unused
  - `nl.ing.lovebird.datetime.Interval`
  - `nl.ing.lovebird.datetime.DateIntervalService`
  - `nl.ing.lovebird.datetime.paydaycyclecalculator.*`
- Deprecated app related
  - `nl.ing.lovebird.datetime.DateIntervalService.PaydayCycleConfig`

# 13.0.1
- Deprecated app related fields on the user context

# 13.0.0
- Upgrade to `spring-boot:2.6.2` see [the release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.6-Release-Notes)
- Upgrade to `spring-boot-cloud:2021.0.0` see [the release notes](https://github.com/spring-cloud/spring-cloud-release/wiki/Spring-Cloud-2021.0-Release-Notes)
- Removed the `documentation` module and with it `springfox-swagger`. The `springfox-swagger` appears to have been abandoned.
- Removed the `translations` module.
- removed the `nl.ing.lovebird:documentation` package, [See instructions below](#migration-guide-for-moving-from-springfox-to-springdoc).
- removed the dependency on `io.swagger:swagger-annotations`.
- Upgrade dependencies
  - `awssdk:2.17.105`
  - `bouncycastle:1.70`
  - `jpgpj:1.3`
  - `springdoc-openapi-ui:1.6.3`
  - `springdoc-swagger-annotations:2.1.12`
 
### Migration guide for moving from springfox to springdoc

Swagger docs from springfox should be migrated to springdoc only iff the pod publishes swagger docs. If your pod does
not have a dependency on `nl.ing.lovebird:documentation` you do not have to migrate anything.

The migration can be done by replacing the annotations provided in the package `io.swagger.annotations` for the
annotations in `io.swagger.v3.oas.annotations`. To make this available you will need to update your POM and
configuration.

#### Maven POM
The following actions need to be taken:
* Update lovebird commons to at least version 12.1.1  
  See [lovebird-commons: Changelog] (https://git.yolt.io/backend/lovebird-commons/-/blob/master/CHANGELOG.md) for instructions
* Add the following dependency:
  ```xml
  <dependency>
      <groupId>nl.ing.lovebird</groupId>
      <artifactId>springdoc-starter</artifactId>
  </dependency>
  ```
* Remove the following dependencies `nl.ing.lovebird:documentation`, `io.swagger:swagger-annotations`.

#### Configuration
If you were running with both Spring Doc and Spring Fox you should have the following configuration which now can be removed.
```yml
springfox:
  documentation:
    open-api:
      # disable Open-API documentation generation
      enabled: false
      v3:
        # fix conflicting endpoints
        path: /springfox-v3/api-docs
springdoc:
  # remove springfox endpoints from the documentation
  paths-to-exclude: /springfox-v3/**, /swagger-resources/**, /v2/api-docs/**
```

When you did not do any upgrades, yet you might want to remove default error handlers from the openApi3 documentation.
To do so add the following config to your pod:
```yaml
springdoc:
  override-with-generic-response: false
```

#### Annotations
* @Api → @Tag
* @ApiIgnore → @Parameter(hidden = true) or @Operation(hidden = true) or @Hidden
* @ApiImplicitParam → @Parameter
* @ApiImplicitParams → @Parameters
* @ApiModel → @Schema
* @ApiModelProperty(hidden = true) → @Schema(accessMode = READ_ONLY)
* @ApiModelProperty → @Schema
* @ApiOperation(value = "foo", notes = "bar") → @Operation(summary = "foo", description = "bar")
* @ApiParam → @Parameter
* @ApiResponse(code = 404, message = "foo") → @ApiResponse(responseCode = "404", description = "foo")

##### example
Old:
```java
@Api(tags = "lists")
@RequestMapping("/lists")
class ClientsController {
    @ApiOperation(value = "Get all elements", response = Element.class, responseContainer = "List")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Retrieved List", response = Element.class, responseContainer = "List"),
            @ApiResponse(code = 204, message = "Retrieved empty list"),
            @ApiResponse(code = 404, message = "List not found", response = ErrorDTO.class)
    })
    @GetMapping(value = "/{listId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Element>> listClientCertificates(@PathVariable final UUID listId) {
        return service.getList(listId);
    }
}

@ApiModel(value = "ElementObject", description = "Object containg some default")
class Element {
	@ApiModelProperty(value = "The property of the element", required = true)
	private String property;
}
```

new:
```java
@Tag(name = "lists")
@RequestMapping("/lists")
class ClientsController {
    @Operation(summary = "Get all elements",
        responses = {
            // You can also drop the `content = ...` part and let springdoc figure the response type out on its own, which now also works for lists.
            @ApiResponse(code = 200, description = "Retrieved List", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Element.class)))),
            @ApiResponse(code = 204, description = "Retrieved empty list", content = @Content),
            @ApiResponse(code = 404, description = "List not found", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
        })
    @GetMapping(value = "/{listId}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Element>> listClientCertificates(@PathVariable final UUID listId) {
        return service.getList(listId);
    }
}

@Schema(name = "ElementObject", description = "Object containg some default")
class Element {
	@Schema(description = "The property of the element", required = true)
	private String property;
}
```

#### resources
* [Spring Doc: Migrating from SpringFox](https://springdoc.org/migrating-from-springfox.html)
* [Migration of tokens](https://git.yolt.io/backend/tokens/-/merge_requests/594/diffs)

# 12.1.3
- Upgrade dependency `log4j:2.15.0`

# 12.1.2
- Used `software.amazon.awssdk:bom` to manage all AWS SDK dependencies.
  - You can remove version numbers for any other AWS SDK dependencies.
- `AwsCredentialsProvider` bean availability is controlled by `yolt.vault.aws.enabled` instead of `yolt.vault.enabled`.
  - This avoids need to enable all vault integrations when testing.

# 12.1.1
- updated Documentation component to also work with the `@ExternalApi` annotation of the Springdoc component
- updated the ErrorDTO with `io.swagger.v3.oas.annotations`

# 12.1.0
- Added support for OpenApi 3
  To use in combination with the documentation module (spring-fox) please add the following config to your pod:
  ```yaml
  springfox:
    documentation:
      open-api:
        # disable Open-API documentation generation
        enabled: false
      v3:
        # fix conflicting endpoints
        path: /springfox-v3/api-docs
  springdoc:
    # remove springfox endpoints from the springdoc documentation
    paths-to-exclude: /springfox-v3/**, /swagger-resources/**, /v2/api-docs/**
  ```
  To remove default error handlers from the openApi3 documentation add the following config to your pod:
  ```yaml
  springdoc:
    override-with-generic-response: false
  ```

# 12.0.0
- Added One-off AIS flag to client-token and user-context
- Upgrade to `spring-boot:2.5.7` see [the release notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.5-Release-Notes#upgrading-from-spring-boot-24)
- Upgrade dependencies
  - `awaitility:4.1.1`
  - `awssdk-s3:2.17.85`
  - `jpgpj:1.2`
  - `logstash-logback-encoder:7.0.1`
  - `swagger-annotations:1.6.3`
  - `swagger-annotations:1.6.3`
  - `testcontainers:16.0.2`
- Use `systemUTC` clock instead of `systemDefaultZone`. Affects:
  - `ClientTokenRequesterService`
  - `YoltKafkaAutoConfiguration`
- Removed `Interval.thisMonth()`
- Removed `Interval.lastMonth()`
- Toggled `yolt.commons.block-sensitive-headers.dry-run` to `false`. Sensitive headers will now be blocked by default
  - See `yolt.commons.additional-sensitive-headers` in the `config-server` for an up-to-date list of headers.
- Removed property `yolt.commons.kafka.enabled`. Kafka is controlled by `YoltKafkaAutoConfiguration`.
- Removed property `yolt.commons.kafka.logging.enabled`. Kafka logging is controlled by `YoltKafkaLoggingAutoConfiguration`.
- Removed property `yolt.commons.cassandra.enabled`. Cassandra is controlled by `YoltCassandra3AutoConfiguration`.

# 11.0.3
- Prevent any in memory db from replacing the data source provided by `@EnableExternalPostgresTestDatabase`
# 11.0.2
- Log additional model mutation info to assist debugging
# 11.0.1
- Log model mutation thread to assist debugging
- Upgrade `awaitility` to 3.1.6
- Use property `awaitility.version` to configure version
- Upgrade `lombok` to 1.18.22
- Use property `lombok.version` to configure version
- The following test-support dependencies are no longer managed to scope test:
  - `testsupport-cassandra`
  - `awaitility`
  - `testsupport-clienttokens`
  - `testsupport-logging`
  - `testsupport-tokens`
  
  This allows these dependencies to be bundled into starters.
  When including these dependencies in your project ensure that they are scoped to `test` e.g:
  ```xml
  <dependency>
    <groupId>nl.ing.lovebird</groupId>
    <artifactId>testsupport-logging</artifactId>
    <scope>test</scope>
  </dependency>
  ```

# 11.0.0
- Added sample apps for cassandra v3, spring data cassandra and running both in the same application
- Cassandra test database must be explicitly enabled by using the `@EnableExternalCassandraTestDatabase`
  - Included as part of the `cassandra-starter-test`
- Kafka test cluster must be explicitly enabled by using the `@EnableExternalKafkaTestCluster`
  - Add `kafka-external-test-cluster` in scope `test` as a dependency 
- External Postgres test database must be explicitly enabled by using the `@EnableExternalPostgresTestDatabase`
  - Add `postgres-external-test-database` in scope `test` as a dependency 
- Enabled support for Spring Data Cassandra
  - To use and test add these dependencies:
    - `org.springframework.data:spring-data-cassandra`
    - `nl.ing.lovebird:spring-data-cassandra-model-mutation`
    - `nl.ing.lovebird:cassandra-external-test-database` in scope `test` 
- Renamed `CassandraVersioner` to `CassandraModelMutationApplier`.
- Removed the `yolt.commons.cassandra.versioning.enabled` property
- Removed the `yolt.commons.cassandra.enabled` property

### Upgrading  Spring Data Cassandra

Since LBC version `11.0.0` we support Spring Data Cassandra as alternative to our homegrown `cassandra` module.
See the official [Spring Data Cassandra documentation](https://docs.spring.io/spring-data/cassandra/docs/3.2.x/reference/html/). 
Spring Data Cassandra is using Cassandra driver version 4.x which keeps back-compatibility with Cassandra DB v3.x

Our cassandra-related Spring auto-configurations are implemented in such a way that both Spring Data Cassandra and our
homegrown cassandra module can work alongside each other in same pod. The same goes for `casssandra-model-mutation` and
`spring-data-cassandra-model-mutation`. In a scenario where pod runs both `spring-data-cassandra-model-mutation` and
`cassandra-model-mutation` first one will take precedence and will be the only model-mutation configured.

##### Migration from homegrown Cassandra client to Spring Data Cassandra Client
0. Our homegrown Cassandra implementation audit-logs every query. This is excessive and generally not needed.
   Nevertheless, before migrating make sure that you have sufficient audit logging in your pod. See the 
   [IT Security Guidelines - Audit logging](https://yolt.atlassian.net/wiki/spaces/LOV/pages/3900258/IT+Security+guidelines)
   for details.

2. Include `spring-data-cassandra` in your pom. When this dependency is on classpath Cassandra-related configurations
   will kick in.
```xml
    <dependency>
      <groupId>org.springframework.data</groupId>
      <artifactId>spring-data-cassandra</artifactId>
    </dependency>
```

2. Include `spring-data-cassandra-model-mutation` in your pom if you need to support cassandra schema updates.
```xml
    <dependency>
      <groupId>nl.ing.lovebird</groupId>
      <artifactId>spring-data-cassandra-model-mutation</artifactId>
    </dependency>
```

3. Include `cassandra-external-test-database` in your pom if you need to use cassandra db instance in tests
```xml
    <dependency>
      <groupId>nl.ing.lovebird</groupId>
      <artifactId>cassandra-external-test-database</artifactId>
      <scope>test</scope>
    </dependency>
```
4. Refactor cassandra repositories that do implement `nl.ing.lovebird.cassandra.CassandraRepository` to use autowired `CassandraTemplate`
- Refactor entities to use annotations from `org.springframework.data.cassandra.core.mapping` instead of `com.datastax.driver.mapping.annotations`:
  - `com.datastax.driver.mapping.annotations.Table` -> `org.springframework.data.cassandra.core.mapping.Table`
  - `com.datastax.driver.mapping.annotations.PartitionKey` and `com.datastax.driver.mapping.annotations.ClusteringColumn` -> `org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn`
  - `com.datastax.driver.mapping.annotations.Column` -> `org.springframework.data.cassandra.core.mapping.Column`

- For examples see following sub-modules in`sample-apps` maven module:
  - `sample-app-cassandra-with-v3-client`
  - `sample-app-cassandra-with-spring-data`
  - `sample-app-cassandra-with-combined-spring-data-and-v3-client`

- If you need to access any other keyspace apart from main one (set by spring.data.cassandra.keyspace-name) you have to spawn an extra template as follows:
```
    private CassandraTemplate cassandraTemplate;

    public ColumnRepository(@Autowired CqlSessionBuilder cqlSessionBuilder) {
        this.cassandraTemplate = new CassandraTemplate(cqlSessionBuilder.withKeyspace("YOUR_EXTRA_KEYSPACE_NAME").build());
    }
```

5. set properties (note: already set via config-server, copy if your pod does not use the config-server):
```
spring.data.cassandra.local-datacenter: eu-central-1
spring.data.cassandra.request.consistency: local_quorum
```
6. if your pod is using batching from homegrown cassandra it is recommended to look into using out-of-the-box throttling mechanism:
see the driver documentation: https://docs.datastax.com/en/developer/java-driver/4.11/manual/core/throttling/
see the spring documentation: https://docs.spring.io/spring-boot/docs/2.5.x/reference/html/application-properties.html#application-properties.data.spring.data.cassandra.request.throttler.type

7. Finally, remove our homegrown cassandra dependencies by removing: 

```xml
    <dependency>
      <groupId>nl.ing.lovebird</groupId>
      <artifactId>cassandra-starter</artifactId>
    </dependency>
    <dependency>
      <groupId>nl.ing.lovebird</groupId>
      <artifactId>cassandra-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
```

## 10.0.6
- Fix issue with @Configuration and @ConfigurationProperties being both present on YoltPostgresProperties

## 10.0.5
- Fix issue with @Configuration and @ConfigurationProperties being both present

## 10.0.4
- Fix Kafka properties to contain the correct bootstrap server for admin client, producer and consumer.

## 10.0.3
- Update to latest Jacoco version

## 10.0.2
- Fix circular dependency for bean `KafkaProperties` while setting up the test container.

## 10.0.1

- Upgrade Lombok version to support Java 17

## 10.0.0

TL;DR: Yolt's homegrown Cassandra v3 implementation has been isolated to its own set of modules. 

- `cassandra` is no longer auto-configured by
  `yolt-service-spring-boot-starter`. Replace `cassandra` with `cassandra-starter` and add `cassandra-starter-test`. All
  other cassandra related dependencies that are transitively included can be removed.
- Removed `yolt.commons.cassandra.enabled` property
- Removed dependencies on JUnit 4 (still included transitively through test containers).
- Upgraded various dependencies:
  - jpgpj:1.4
  - jose4j: 0.7.9
  - commons-io:2.11
  - commons-compress:1.21
  - bouncycastle:1.69
  - awssdk-s3:2.17.48

## 9.0.4
- update spring-cloud.version from 2020.0.3 to 2020.0.4.
  See the release notes https://spring.io/blog/2021/09/23/spring-cloud-2020-0-4-has-been-released


## 9.0.3
- Fix conditional on `BaseThrowableExceptionHandlers` to not conflict with `BaseThrowableWithSpringSecurityExceptionHandlers`.

## 9.0.2
- Fix ErrorHandling precedence for `BaseTomcatExceptionHandlers` and `BaseServletExceptionHandlers`.

## 9.0.1
- Added client_deleted claim in testsupport

## 9.0.0

TL;DR: Projects using lovebird commons no longer have a hard dependency on the servlet stack. However, no support is
provided for the reactive stack yet (e.g. no https certificates, client token-validation, ect). It is now possible to
incrementally add this support.

- When using `@WebMcvTest` as a slice test the `ErrorHandlingAutoConfiguration` and `UserContextAutoConfiguration` are
  automatically included. This means that they do not need to be imported with `@Import`.
- When using `@DataJpa` test all postgres autoconfiguration is included. This means that these do not need to be
  imported with `@Import`.
- The `user-context-authentication` dependency is now opt-in. If your pod does not use the `UserContext` or the
  authentication features you can opt out of this module now. If your pod does use the `UserContext` but not the
  authentication you can switch to the `user-context` dependency. This will remove the dependency on `spring-security`.
- The `spring-boot-starter-web` dependency is now opt-in. If your pod is not a web application you can opt out now.
- Servlet related auto-configuration is now conditional on a servlet application. 
- Expanded the `app-samples` project with minimal samples for web, cassandra, postgress, kafka and client-tokens.
- Added exception handlers for all known subclasses of `ResponseStatusException`. 
- The `client-tokens-autoconfigure` module is no longer included in `yolt-service-spring-boot-starter`. Use
 `client-tokens-starter` instead of `client-tokens`. 

## 8.1.4
- Added `NonDeletedClient` annotation to make methods only available for non deleted clients.

## 8.1.3
- Added `client-tokens-starter` module.

## 8.1.2
- Added isDeleted claim to clientToken which will return true if the client is marked as deleted

## 8.1.1
- Fixed "The driver should never call this method on an object that implements PlainTextAuthProvider" error

## 8.1.0
- Upgrade cassandra-driver to 3.11.0
- Upgrade achilles-junit to 6.1.0
- Extracted `client-tokens-autoconfigure` from `client-tokens` and `yolt-service-spring-boot-starter`.
  - The property `yolt.client-token.verification.enabled` has been removed. `@VerifiedClientToken` will always verify
    client tokens.
  - `ClientTokenVerificationAutoConfiguration` will automatically be included in any tests involving MockMvc, such as
    `@WebMvcTest`.
  - `ClientTokenTestConfiguration` can be used to replace the client token parser with a mock bean. E.g.:
    ```java
    @WebMvcTest(controllers = AccountsController.class)     
    @Import({
      ExceptionHandlingService.class,
      UserContextSecurityConfiguration.class,
      ClientTokenTestConfiguration.class
    })
    ```

## 8.0.9
- Allow optional extra datasources for use in custom entitymanager.
  For more information see https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-access.use-multiple-entity-managers

## 8.0.8

- Fix PF-1289 where a class loader exception will occur due to @ConditionalOnClass on @Bean method if the `documentation` module is not present on the classpath.

## 8.0.7
- Added `consent-starter` as a client token requester service

## 8.0.6
- Upgrade to Spring-Boot v2.4.9
- Upgrade Maven Failsafe to 3.0.0-M5
- @ConfigurationProperties as alternative to @org.springframework.beans.factory.annotation.Value in starters.
- Upgrade to Testcontainers 1.16.0.
- Removed unused ProxyCustomizer.
- Removed redirect from `swagger-ui.html` -> `swagger-ui`. Update your bookmarks.
- Made the `documentation` module (and swagger-ui) opt-in.

## 8.0.4
- Validate `kubernetes-context` in `local-team-starter` 
- Use @ConfigurationProperties as alternative to @org.springframework.beans.factory.annotation.Value where applicable in yolt-service-spring-boot-starter
- Restructured yolt-service-spring-boot-starter while grouping classes components-wise
    ``` Cassandra3VersioningAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.cassandra
        ClientTokenParserAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.clienttoken
        ClientTokenRequesterServiceAutoConfiguration: com.yolt.service.starter.configurations -> com.yolt.service.starter.clienttoken
        ClientTokenRequesterVaultAutoConfiguration: com.yolt.service.starter.configurations -> com.yolt.service.starter.clienttoken
        ClientTokenVerificationAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.clienttoken
        HttpAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.http
        KafkaHealthIndicatorAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.kafka
        ReactiveHttpAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.http
        Swagger2AutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.swagger
        YoltCassandra3AutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.cassandra
        YoltCassandraProperties: com.yolt.service.starter -> com.yolt.service.starter.cassandra  
        YoltCassandraRepositoryAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.cassandra  
        YoltClientTokenRequesterProperties: com.yolt.service.starter.configurations -> com.yolt.service.starter.clienttoken  
        YoltKafkaAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.kafka  
        YoltKafkaJacksonAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.kafka  
        YoltKafkaLoggingAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.kafka  
        YoltThreadPoolTaskExecutorMetricsAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.metrics  
        YoltVaultCredentialsReader: com.yolt.service.starter -> com.yolt.service.starter.vault  
        YoltVaultProperties: com.yolt.service.starter.configuration -> com.yolt.service.starter.vault  
        YoltVersionMetricsAutoConfiguration: com.yolt.service.starter -> com.yolt.service.starter.metrics  
  ```

## 8.0.3
- Added `yolt.team.starter.kubernetes-context` to facilitate deployment to EKS.
  This combines the `environment` and `cluster` properties. E.g: `yolt.team.starter.kubernetes-context=cluster0.team1`.
- The property `yolt.team.starter.cluster` has been removed  

## 8.0.2
- Replace Vault.waitForVaultToProvideFile with Vault.requireFileProvidedByVault

## 8.0.1

- Upgrade to Spring-Boot v2.4.6
 - See the release notes https://github.com/spring-projects/spring-boot/releases/tag/v2.4.0
 - JUnit Vintage (JUnit4 adaptor for JUnit5) is no longer provided as a dependency. 
   - Either upgrade to JUnit 5 or use:
   ```xml
        <dependency>
            <groupId>org.junit.vintage</groupId>
            <artifactId>junit-vintage-engine</artifactId>
            <scope>test</scope>
        </dependency>
    ```
- Upgrade Spring-Cloud Sleuth to v3 0
 - See: https://github.com/spring-cloud/spring-cloud-sleuth/wiki/Spring-Cloud-Sleuth-3.0-Migration-Guide

- Upgrade to Spring-Cloud 2020.3
 - See:
  - https://github.com/spring-cloud/spring-cloud-release/wiki/Spring-Cloud-2020.0-Release-Notes  

- Misc upgrades
 - awssdk 2.16.73
 - testcontainers 1.15.3

- Moved VerifiedClientToken annotation to client-tokens-annotations
 - Added @AIS and @PIS annotations to separate on product access

## 7.6.4
- Update spring-security-rsa to version 1.0.10 due to cve-2021-22112

## 7.6.3
- Upgraded Spring Boot to version 2.3.11.RELEASE

## 7.6.2
- Explicitly register and configure the JSR-310 integration for Jackson in the Audit Logger.
  - Improves display of JSR-310 values in objects that are audit logged.
  - Ensures audit logged objects still works in Java 16 (would otherwise fail, without a clear error message). 

## 7.6.1
- upgrade security-utils to 0.0.20
  - Fixes pem parsing bug
  - Replace `java.security.KeyPair` with `com.yolt.securityutils.crypto.KeyPair`

## 7.6.0
- Improved vault-agent exception message 
- Upgraded Spring Cloud to Hoxton SR11

## 7.5.4

- Upgraded Spring Boot to version 2.3.10.RELEASE

## 7.5.3

- renamed `nl.ing.lovebird.secretspipeline.converters.JsonWebKeyReader` to `nl.ing.lovebird.secretspipeline.converters.JsonWebKeysStoreReader`

## 7.5.2

- Removed `disabledFeatures` field and `isFeatureDisabled(..)` method from UserContext
- Deprecated old `nl.ing.lovebird.secretspipeline.JsonWebKeyReader` in favor of the new  `nl.ing.lovebird.secretspipeline.converters.JsonWebKeyReader` which extends `KeyStoreReader` to add support for
  JsonWebKeys in VaultReader

```java
public class Config {
  @Autowired
  public VaultKeys vaultKeys;

  public methodRequiringSecret() {
    RsaJsonWebKey rsaJsonWebKey = vaultKeys.getRsaJsonWebKey("secretName");
    JsonWebKey jsonWebKey = vaultKeys.getJsonWebKey("secretName");
    PublicJsonWebKey publicJsonWebKey = vaultKeys.getPublicJsonWebKey("secretName");
  }
}
```

## 7.5.1

- Updated awssdk-s3 to version 2.11.14
- Updated Lombok to 1.18.18
- Added ClientGroupIdVerificationService to verify that a client-token has a claim for a specific client-group-id.

## 7.5.0

- Added `setAuditLoggingEnabled` to disable audit logging on `CassandraRepository`

```java
public class UserRepository extends CassandraRepository<User> {
    public UserRepository(Session session) {
        super(session, User.class);
        setAuditLoggingEnabled(false);
    }
}
```
  
## 7.4.3

- Updated to Spring Boot version 2.3.9
- Updated to security-utils version 0.0.19
- Updated to bounce-castle version 1.68 
- Fixed dependency management for bouncy-castle

## 7.4.2
- upgrade testcontainers to 1.15.2 for compatibility with Docker Desktop 3.1.0
  - prevent this exception (on MS Windows) :
    - "com.github.dockerjava.api.exception.NotFoundException: Status 404: {"message":"No such image: testcontainers/ryuk:0.3.0"}"
    - For more info see: https://github.com/testcontainers/testcontainers-java/issues/3574
- Fix for Junit FileStreams in the @TempDir under MS Windows.
  - They need to be closed explicitly otherwise the Junit cleanup will find the file still locked and throw an exception, making the test fail.

## 7.4.1
- Remove datacenter reference from Kafka config map for team starter

## 7.4.0
- Removed unused `nl.ing.lovebird:rest` module
  - Inline the depcreated `LinkDTO` and `HateoasUtils` 
- Removed component scan for `yolt-service-clients`.
  - The property `yolt.service.clients.enabled` can be removed
  - The following unused packages are no longer component scanned.
```
nl.ing.lovebird.accountrecoveryclient
nl.ing.lovebird.accountsclient
nl.ing.lovebird.budgetsclient
nl.ing.lovebird.clientusersclient
nl.ing.lovebird.contentclient
nl.ing.lovebird.counterpartiesclient
nl.ing.lovebird.datascienceclient
nl.ing.lovebird.healthclient
nl.ing.lovebird.insightsclient
nl.ing.lovebird.maintenanceclient
nl.ing.lovebird.partnersclient
nl.ing.lovebird.paymentcyclesclient
nl.ing.lovebird.personalmessagesclient
nl.ing.lovebird.pushclient
nl.ing.lovebird.recategorizationclient
nl.ing.lovebird.salesforceclient
nl.ing.lovebird.securityprofilesclient
nl.ing.lovebird.servicecloudclient
nl.ing.lovebird.serviceclient
nl.ing.lovebird.sitemanagementclient
nl.ing.lovebird.spendingclient
nl.ing.lovebird.transactionsclient
nl.ing.lovebird.usersclient
```

## 7.3.0
- Added `SelectEntityPager` that allows you paginate over all entities, or a specific query.
  Deprecated `SelectAllEntityPager` in favor of the `SelectEntityPager`.

## 7.2.0
- Remove unused `elastic_index` from log markers
- Made `Auditlogger` and `SemaEventLogger` mockable for testing purposes.
- Some tests depend on the implementation details `Marker.toString()` to verify that a message was logged as an audit
  log. These tests will now fail because `elastic_index` is no longer present in the string representation. Consider
  mocking `SemaEventLogger` or `AuditLogger` instead.
- Exposed `lovebird-commons.version` via prometheus

## 7.1.11
- Upgrade to Spring Boot version 2.3.8

## 7.1.10
 - Introduced new bean to read CSR keys, see `CSRKeyStoreReader`

## 7.1.9
 - Deprecate few flags within user-context (emailAddressVerified, bankingEnrollmentCompleted, phoneNumberClaimed) to 
   start using the user privileges in user context instead: `UserContext.getRoles().contains("EMAIL_ADDRESS_VERIFIED");` 

## 7.1.8
 - Enable Kafka Health indicator through autoconfiguration

## 7.1.7
- PF-368: Log Kafka errors with `user-id` rather then `user_id` 
- PF-758: Update to Spring Boot version 2.3.7
- PF-560: Move com.yolt.service.starter.secrets.converters to seperate module.
  - Import for `VaultKeys` changed to `nl.ing.lovebird.secretspipeline.VaultKeys`
  - Import for `JsonWebKeyReader` changed to `nl.ing.lovebird.secretspipeline.JsonWebKeyReader`
  - Introduced `PGPRing` to break direct dependency with BouncyCastle in our spring-starter (Use `PGPRing#getRing()` to get the original Ring back)
    Migration: instead of wiring `Ring` class directly you need to wire `PGPKeyRing` with Spring and use `PGPKeyRing#getRing()` to get the actual Ring instance from Bouncy Castle.

## 7.1.6

- Fix PF-738 reading a JWKS should skip the first line (skip the type line)

## 7.1.5
- Do not use the Kafka headers object mapper as the default object mapper. Fixes various jackson issues. 

## 7.1.4 (Broken don't use)
- Change VaultKeys implementation it will by default read the file provided by the parameter, or it will provide an empty bean. This should cover the way teams are at the moment using this bean.

## 7.1.3 (Broken don't use)
- Update to Tomcat 9.0.40 due to CVE-2020-17527
  
## 7.1.2 (Broken don't use)
- Removed `JsonAutoConfiguration.jacksonObjectMapper()` and `JsonAutoConfiguration.jacksonObjectMapperBuilder()` in favor of corresponding beans provided by Spring Boot Framework.
  - Please see change log entry 7.0.3 for mitigations guidance.
- Introduced ReactiveHttpAutoConfiguration that logs errors on propagation of sensitive headers by WebClient to external systems.
  - Use manually constructed WebClient when making calls to 3rd parties.
  - Use autowired `WebClient.Builder` when making calls to Yolt Services.
- VaultKeys and PGPKeyRing beans are conditional on missing bean to facilitate testing
- `yolt-service-spring-boot-starter-test` provides empty beans to facilitate testing
  - You can use `@TestConfiguration` to override these beans with your own keys when testing

## 7.1.1
- Set time zone defaults in UserContext for country codes GB, FR and IT. When country code is null it defaults to Europe/London.

## 7.1.0
- Added time zone property to the UserContext

## 7.0.20
- YCL-1886: Added client-token claims related to data enrichment

## 7.0.19
- PF-655: Upgrade TestContainers to release version 1.15.0
- PF-645: Replaced "user-context" header with USER_CONTEXT_HEADER_KEY

# 7.0.18
- PF-656: Add extra conditional to `ClientTokenParserAutoConfiguration`

## 7.0.17
- The property `yolt.postgres.set-role` has been removed. The postgress role is always set.
  - Ensure you have `yolt-service-spring-boot-starter-test` as a dependency to disable this behaviour when testing.
  - Remove the `yolt.postgres.set-role` property from your `application.yml` and `config-server`   

## 7.0.16
- Upgrade to Spring-Boot v2.3.6.RELEASE
  - See the release notes: 
    - https://github.com/spring-projects/spring-boot/releases/tag/v2.3.6.RELEASE
    - https://github.com/spring-projects/spring-boot/releases/tag/v2.3.5.RELEASE

## 7.0.15

- Bugfix for reading secrets from `/vault/secrets` sometimes failed with an exception

## 7.0.14

- Removed all checks related to key length from `secrets-pipeline`. Rationale: LBC only imports secrets generated from 
  the secrets pipeline, this one should make sure it does not create keys which are not valid. Adding those checks here
  makes the feedback loop way too long. 
- PF-626

## 7.0.13
- `@RequiredUser` no longer requires the `user-id` header to be present.  

## 7.0.12
- Temporary removed password length check

## 7.0.11
- Updated security utils to version 0.0.17 fixing a private key length bitlength issue

## 7.0.10
- Updated security utils to version 0.0.16 fixing some RSA key strength issues at generation
- Added a guard when importing secrets, that we do not import RSA private keys with more than 0 trailing zeroes signficiantly reducing key strength;

## 7.0.9
- Updated security utils to version 0.0.15 fixing some PKCS8 related private key issues.

## 7.0.8
- Updated Lombok and Jacoco plugins to support JDK15.

## 7.0.7
- Removed `MDCContextCreator.TRACE_ID_HEADER_NAME` and `MDCContextCreator.TRACE_ID_MDC_KEY`
  - Remove where possible. Tracing is handled transparently by Sleuth. 
  - Gateways can use `request_trace_id` as String or drop support for it (their choice)  
- Extracted `backwards-compatibility` module from LBC
- Only invoke `CassandraRepository.setTracingEnabled` when `cassandra.tracing.enabled` is set
- Add PII safe Kafka `BatchErrorHandler`  

## 7.0.6
- Updated `local-team-starter` to connect to Cassandra.

## 7.0.5
- Added `micrometer-registry-prometheus` to enable `actuator/prometheus`.

## 7.0.4
- Add `okhttp` as dependency in profile for local-team-starter.

## 7.0.3
- Refactored`rest` and `http` modules
  - Extracted `user-context-authentication`
  - Extracted `delete-user`
  - Removed `spring-boot-starter-web` dependency from `http`
  - Removed `user-context` dependency from `rest`
  - Removed `error-handling` dependency from `rest`
  - Removed `http` dependency from `rest`
    * For services please always extend `yolt-service-spring-boot-starter`.
    * Please upgrade to the latest version of `yolt-shared-dtos`, `yolt-kafka-clients` and `yolt-service-clients`
    * For libraries prefer API libraries e.g: `slf4j-api`, `jackson-annotations`, `swagger-annotations`.
    * For providers replace `rest` with one or more of the extracted modules 
- Deprecated `HateoasUtils` 
  * Please inline in your service and add `org.springframework.hateoas:spring-hateoas` as a dependency
- Moved `JsonConfiguration` to `JsonAutoConfiguration`.
  * Either @Autowire the `ObjectMapper` in your tests
  * Or construct a new object mapper from scratch (you may not need all modules).
  ```java
  ObjectMapper objectMapper = new ObjectMapper()
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    .disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    .setDateFormat(new StdDateFormat().withColonInTimeZone(false))
    .registerModule(new JavaTimeModule())
    .registerModule(new Jdk8Module())
    .registerModule(new ParameterNamesModule())
    .registerModule(new JsonComponentModule());
  ```
   * Do note that using `JsonConfiguration` outside of a Spring Boot test may not result the expected object mapper. In
     other words; do not assume that the json produced this way was correct.
- Moved `UserNotFoundResponseErrorHandler` to  `nl.ing.lovebird.errorhandling`

## 7.0.2
- Remove `com.google.guava:guava` implicit dependencies across LBC (except for in C* modules)

## 7.0.1 (Broken, don't use)
- Replaced `LovebirdDowngradingConsistencyRetryPolicy` with the default Cassandra retry policy
- Reduced the write-consistency in `CassandraRepository` to `LOCAL_QUORUM`
  * We are running on a single cluster Cassandra. No action needed.
- Removed all dependencies on `org.apache.commons:commons-lang3`
  * Add this dependency to your pod if are using this. Or consider using plain Java. 
  * Please upgrade to the latest version of `yolt-shared-dtos`, `yolt-kafka-clients` and `yolt-service-clients`
- Removed all dependencies on `com.google.guava:guava`
  * Add this dependency to your pod if are using this. Or consider using plain Java.
  * Please upgrade to the latest version of `yolt-shared-dtos`, `yolt-kafka-clients` and `yolt-service-clients` 

## 7.0.0
- Removed nearly all `spring-boot-starter-*` dependencies from all non-starter modules. 
   * For services please always extend `yolt-service-spring-boot-starter`.
   * For libraries prefer API libraries e.g: `slf4j-api`, `jackson-annotations`, `swagger-annotations`.
   * Please upgrade to the latest version of `yolt-shared-dtos`, `yolt-kafka-clients` and `yolt-service-clients`
 
-  The property `yolt.vault.cassandra.enabled` is inferred from the presence of Embedded Cassandra or Cassandra Test Containers. 
  * The property `yolt.vault.cassandra.enabled` can be removed.
  * Ensure either Cassandra test containers or Embedded Cassandra is on the classpath
-  The property `yolt.vault.postgresql.enabled` is inferred from the presence of PostgreSQL Test Containers. 
  * The property `yolt.vault.postgresql.enabled` can be removed. 


## 6.0.3 
- Fix `VaultKafkaKeystoreInitializer` could not be found

## 6.0.2
- Client-tokens can now be created with keypairs generated by the secrets-pipeline. Please see client-tokens/README.md.
- For the sake of adding client-token requesters more easily, removed the requesting services from the ClientTokenConstants and ClientTokenRequester enum. Please inline the values as Strings.
- Deprecate TRACE_ID_MDC_KEY and TRACE_ID_MDC_KEY

## 6.0.1
- Removed `kafka-util`. Please make sure to upgrade to the latest version of `yolt-kafka-clients`.
- Deprecated `nl.ing.lovebird.rest.dto.LinkDTO` please inline into your models 
- Please upgrade to the latest version of `yolt-shared-dtos`.
- Dropped the component scan for `nl.ing.lovebird.kafka`. Move any components to your apps root package.
- `SemaEventLogger` is exposed as a Spring bean from `com.yolt.service.starter.YoltLoggingAutoConfiguration`.

## 6.0.0
- Added new project `local-team-starter` which enables you to connect locally to a team environment. See README.md for more information. 

- Changed the scope of the  dependency `info.archinnov:achilles-junit` (Embedded Cassandra) to provided. This means that
  `lovebird-commons` will no longer add this dependency to every project. 
  * If your pod is still using Embedded Cassandra for tests either:  
    - Use test containers 
        ```xml
            <dependency>
                <groupId>org.testcontainers</groupId>
                <artifactId>cassandra</artifactId>
                <scope>test</scope>
            </dependency>
        ```
        And add to `gitlab-ci.yml`:
        ```
        variables:
            SVC_CASSANDRA_ENABLED: "true"
        ```
    - Or explicitly include `info.archinnov:achilles-junit` to restore embedded cassandra
   
        ```xml
        <dependency>
            <groupId>info.archinnov</groupId>
            <artifactId>achilles-junit</artifactId>
            <scope>test</scope>
        </dependency>
        ```
      Note: `achilles-junit` will prevent your tests from running on Java9+ on Windows. Consider migrating to test
      containers.


## 5.1.7
- Added Kafka health check
- Fixed premature instantiation of thread pool task executors

## 5.1.6
- Use correct timezone in MONTHLY date interval calculations

## 5.1.5
- Broken don't use

## 5.1.4
- Upgrades Spring Boot to v2.3.4

## 5.1.3
- Fixes: `Parameter 0 of method docket in com.yolt.service.starter.Swagger2AutoConfiguration required a single bean, but 2 were found`

## 5.1.2
- Vault-agent might take longer time to provide the files/key stores, in order to prevent startup issues within the application we wait a fixed amount of time in which we check whether the file is available before wiring the Vault secrets or key store.

## 5.1.1
- Upgrade to Spring Boot version 2.3.3.RELEASE

# 5.1.0
Bumping to this version will disable using `awaitility` as a production dependency, it should only be used as a test dependency. 
- Remove the usage of awaitility from `AbstractCassandraVersioner` we should not depend on awaitility for production code.

Because the scope of the awaitility dependency changed it isn't transitive anymore. If your project's tests depend on awaitility add it to your project's pom:
```
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
    <scope>test</scope>
</dependency>
```
# 5.0.19

- Swagger auto configuration no longer enabled by default, use `yolt.commons.swagger2.enabled=true` to enable it
  Please remove `@EnableSwagger2WebMvc` or `@EnableSwagger2` from the project if you use the auto configuration.
- Set the runOrder for the surefire and failsafe tests to random

## 5.0.18
- Renamed kyc status code from `INCLOSING` to `IN_CLOSING`

## 5.0.17
- Made `YoltPostgreSQLAutoConfiguration` unconditional on `yolt.postgres.set-role`. Instead, the datasource will use that property to see if it needs to set the role.

## 5.0.16
- Downgraded Spring Boot to 2.3.1.RELEASE because of regressions introduced in 2.3.2.RELEASE (https://github.com/spring-projects/spring-boot/issues/22562)

## 5.0.15
- Added a ClientTokenParserAutoConfiguration to supply a ClientTokenParser bean when the signature property is set.
- Changed ClientTokenRequesterServiceAutoConfiguration and ClientTokenVerificationAutoConfiguration to use the configured ClientTokenParser

## 5.0.14
- Botched release. Do not use.

## 5.0.13
- Important: first check if you are not using static secrets provided in config-map, this can only be used if Vault is able to provide AWS credentials. Added auto configuration for AWS credentials (such as for S3). Before migration please check https://git.yolt.io/infra/vault-cfg if the AWS role is available (you could be using a static secret in the K8s config map). If your role is defined, use the following steps to start using `vault-agent`:
    - Add the following to your k8s deployment:
    ```yml
     spec:
       template:
         metadata:
           labels:
             aws: 'true'
    ``` 
    - Add the following to `k8s/deployment-config/base`: (will be resolved by YDT to "aws/creds/${{environment}}-<<replace with aws role here>>")
    ```properties
      awsRole=<<fetch role from vault-cfg project>>
    ```
    - Usage: 
        - Autowire bean `AWSCredentialsFromVaultProvider`
        - `S3Client.builder().region(Region.of(s3Properties.getRegion())).credentialsProvider(awsCredentialsFromVaultProvider).build();`

## 5.0.12 
- Upgrade to Spring Boot version 2.3.2 

## 5.0.11
- Added phoneNumberClaimed flag in UserContext.

## 5.0.10
- Move back to `io.springfox` instead of `io.yolt.springfox`. 
    - If you use `@EnableSwagger2WebMvc` remove it (auto configuration will take care of this)
    - swagger-ui location has moved from http://host/context-path/swagger-ui.html to http://host/context-path/swagger-ui/index.html OR http://host/context-path/swagger-ui/ for short.
        - by default LBC supports the old location `swagger-ui.html` with the property `yolt.commons.swagger2.support-old-url` set to `false` you can disable this.
    - if you previously added Guava to the classpath you can remove it (Springfox itself is no longer dependent on Guava)
- Make sure you enabled the property `yolt.commons.swagger2.enabled=true` on the config-server to `true` as well.
- If you choose to override Swagger in your project make sure you adhere to the same defaults defined in LBC, if possible remove definition of `Docket` from your project.
- If you run into problems with the application not starting check regarding Springfox the dependencies if you are using `yolt-shared-dtos` an older version might be in conflict with the new LBC. Upgrade to the latest version of `yolt-shared-dto` which uses LBC version 5 (at the moment not available) or otherwise add exclusions:
  ```
   <dependency>
     <groupId>nl.ing.lovebird</groupId>
     <artifactId>provider-domain</artifactId>
     <version>${yolt-shared-dtos.version}</version>
     <exclusions>
        <exclusion>
          <groupId>io.yolt.springfox</groupId>
          <artifactId>springfox-swagger2</artifactId>
        </exclusion>
     </exclusions>
   </dependency>
  ``` 
- Add a new status to the kyc status enum (`INCLOSING`)

## 5.0.9
- Post process all ThreadPoolTaskExecutor and ThreadPoolTaskSchedulers even when their declared bean type is Executor
- Changed `YoltKafkaAutoConfiguration`: added the `DefaultKafkaConsumerFactoryCustomizers` to allow spring to automatically enable kafka consumer metrics after bumping to spring boot 2.3.0 or higher.

## 5.0.8
- Changed `YoltKafkaAutoConfiguration`: added the `DefaultKafkaConsumerFactoryCustomizers` to allow spring to automatically enable kafka consumer metrics after bumping to spring boot 2.3.0 or higher.

## 5.0.7
- Post process all ThreadPoolTaskScheduler to be SmartLifeCycle aware

## 5.0.6
- Post process all ThreadPoolTaskExecutor to be SmartLifeCycle aware

## 5.0.5
- Removed Spring Vault project completely, only Vault-agent can be used. If you need to use `VaultTemplate` or `VaultAuthentication` beans directly in the code you can wire this automatically with Spring Cloud directly through configuration:

  In `bootstrap.properties` add:
  
  ```
  spring.cloud.vault.uri=${YOLT_VAULT_ADDRESS}
  spring.cloud.vault.authentication=KUBERNETES
  spring.cloud.vault.kubernetes.role=tokens
  spring.cloud.vault.kubernetes.kubernetes-path=${ENVIRONMENT}/k8s/cluster${CLUSTER_INDEX}/pods/${MY_POD_NAMESPACE}
  spring.cloud.vault.kubernetes.service-account-token-file=/var/run/secrets/kubernetes.io/serviceaccount/token
  spring.cloud.vault.config.lifecycle.enabled=false
  ```
  
  In the K8s deployment file add:
  
  ```
    - name: SPRING_CLOUD_VAULT_ENABLED
      value: 'true'
  ```
  below `env` 
  
  Make sure you have this dependency in the `pom.xml`:
  
  ```
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-vault-config</artifactId>
    </dependency>
  ```
  
  Check with `mvn dependency:tree` if you are using the correct versions:
  
  ```
   +- org.springframework.cloud:spring-cloud-vault-config:jar:2.2.3.RELEASE:compile
   +- org.springframework.vault:spring-vault-core:jar:2.2.0.RELEASE:compile
  ```
  
  There was a bug in older version of `spring-vault-core` where `spring.cloud.vault.authentication=KUBERNETES` did not work. 
  
  In your project wire the `VaultTemplate` bean.
 
  
- Fix line ending issue on Windows when reading secrets file
- Expose ThreadPoolTaskExecutor metrics via Prometheus

## 5.0.4
- Made swagger's useDefaultResponseMessages field configurable and false by default. This setting generates default HTTP codes, which is usually non-informative. 

## 5.0.3
- Removed `yolt.gracefulShutdownTimeoutInSeconds`. This is handled by Spring via `server.shutdown=gracefull`.

## 5.0.2
- Add `logServerErrorDetail` to the datasource so we don't accidentally log sensitive information with a SQLException.
- The `spring.data.cassandra.datacenter-name` property can be removed

## 5.0.1
- Upgrade to Spring Boot 2.3.1-RELEASE
    - [Read the release notes](https://spring.io/blog/2020/05/15/spring-boot-2-3-0-available-now)
    - Read Zero-Downtime Deployment and Scaling design
    - Replace `CassandraProperties` with `YoltCassandraProperties`.
    - Note that *http* trace headers are now lowercase
     - `X-B3-TraceId` -> `x-b3-traceid`
     - `X-B3-SpanId"` -> `x-b3-spanid`
    - Stop using deprecated properties. See [deprecations from spring boot 2.2](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.3-Release-Notes#deprecations-from-spring-boot-22) 
        - Note: We do not use spring managed Cassandra.
    - Update health check in k8s manifest to use [liveness and readiness endpoints](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.3-Release-Notes#liveness-and-readiness-probes)
     ```yml
    livenessProbe:
      httpGet:
        path: ${{microservice}}/actuator/health/liveness
        port: 8888
      initialDelaySeconds: 180
      timeoutSeconds: 6
    readinessProbe:
      httpGet:
        path: ${{microservice}}/actuator/health/readiness
        port: 8888
    ```
  
  If you want to enable `/health/readiness` and `/health/liveness` on your local environment add the following property:
  
  ```
  management:
    health:
      probes:
        enabled: true
  ``` 
  
  Note: no need to add them explicitly if you are not interested on having them available locally, they will be automatically enabled on K8s.
  
  Note: as mentioned [here](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html#production-ready-kubernetes-probes-external-state): By default, Spring Boot does not add other Health Indicators to these groups. This means that if you want to check whether the database is up or not and if the probe should fail (meaning K8s will take the pod out of service) one should add:
  
  ```
  management:
    endpoint:
      health:
        group:
          readiness:
            include: readinessState,db
  ```
  
  to the `application.yml`. Without the `db` the pod will report `UP` when calling `/health/readiness` even though `/health` will report `DOWN` if the database is not reachable.

- Version bumps
    - commons-lang3 -> 3.10 (now spring managed)
    - threeten-extra -> 1.4
    - mustache -> 0.9.6
    - security-utils -> 0.0.11
    - bcprov-jdk15on -> 1.65.01
    - jose4j -> 0.6.5

- Removed `vavr` from `datetime` module because of possible dependency clashes with Java collections in consumers
- Removed the fallback on client-id in the subject of client-tokens, using the client-id claim instead.

## 4.1.20 

- Extra claims converted to boolean instead of string (YCL-1385)

## 4.1.19
- Added client-token authorization claims to the client-token test util package. 

## 4.1.18
- Removed KeyStore for importing secrets from Vault. You now have to wire `VaultKeys`. Instead of `getEntry()` these are replaced with specific methods. 
- Removed `KeyStoreTestCreator` and replaced with `VaultKeysReader`
- Added helper methods to the ClientToken for finding out whether a client has certain authorizations: isPsd2Licensed, hasKYCForEntities and hasKYCForPrivateIndividuals.

## 4.1.15
- Upgrade to Spring Boot 2.2.8-RELEASE

## 4.1.14
- Add DateTimeInterval class for filtering purposes.

## 4.1.13
- Bump version of Jacoco, Mockito and Lombok to better support java 13

## 4.1.12
- Reload vault-agent provided cassandra credentials when creating a new connection.

## 4.1.11
- Upgrade postgresql dependency to 42.2.13 due to CVE-2020-13692

## 4.1.10
- Add missing `@EnableConfigurationProperties` to `YoltPostgreSQLAutoConfiguration`
- Replaces `HikariCP` with `spring-boot-starter-data-jpa` as the provided dependency in `yolt-service-spring-boot-starter` 

## 4.1.9
- Fix case-sensitive parsing while reading symmetric key. 

## 4.1.8
- Use different property for environment in password based key store and use prd as default value if property is missing

## 4.1.7
- Allow shorter keys for password based key store entries for non production environments. 
  Rationale: for external sandbox environments we are not always given a password with more than 12 characters. 
  NOTE: this DOES NOT apply for production environments
  
## 4.1.5 & 4.1.6
- Moved the secret-type to the file itself, only an internal change (see https://yolt.atlassian.net/browse/PF-433)
  For local test files: move the secret type from the filename to the file as follows:
  ```
  type: PASSWORD_ALFA_NUMERIC_SPECIAL_CHARS
  sdfassdfaASffsfdasdfAAd==
  ```

## 4.1.4
- Allowing Cassandra system keyspaces usage (read-only mode) on YCS namespace

## 4.1.2 & 4.1.3
- YoltPostgreSQLAutoConfiguration update

## 4.1.1
- Added KeyStoreTestCreator for easy using secrets-pipeline without Spring configuration in test cases.

## 4.1.0
- Keys imported by name (excluding type) so `stubs-example-key2_rsa_2048` must be fetched by `stubs-example-key2`
- Jose4j is no longer a required dependency (only necessary if you use JWKS keys)
- Removed method `KeyStoreCreator.getKeyStorePassword()` in favour of `keyStoreAndEntryPassword` which can be wired as a bean `PasswordProtection`. The method `getPassword()` will give the `char[]` variant.
- Removed class `KeyConverter`
- Introduced new bean to read JWKS keys, see `JsonWebKeyReader`

## 4.0.49
- use the static CLIENT_GROUP_ID as client-group-id claim when creating client-tokens in ClientTokenTestConfiguration

## 4.0.48
- Adding default 'listener-concurrency' as 1 (if not specified) to KafkaTopicsProperties

## 4.0.35
- Upgrade to Spring Boot 2.2.7

## 4.0.34
- Added Kafka test-containers as an alternative to EmbeddedKafka. There are 2 parts that should be updated in projects:
    1. Configuration for local runs
    
        Locally we use test containers, so projects need to include following dependency:
        ```xml
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <scope>test</scope>
        </dependency>
        ```
        As well, topics that would be used in integration tests should be created, Yolt standard setup will do:
        ```yaml
        yolt:
          kafka:
            topics:
              someTopic:
                topic-name: someTopicName
                listener-concurrency: 1
        ```
    
    2. Configuration for pipeline runs
    
        In pipelines we don't use test containers but we rather use Gitlab services instead. So we need to set a flag in 
pipeline that Kafka is used:
        ```yaml
        SVC_KAFKA_ENABLED: "true"
        ```
        As well, we need to specify topics list so that those would be created:
        ```yaml
        SVC_KAFKA_TOPICS: "someTopic:1:1,someOtherTopic:1:1"
        ```
   
    3. If you migrate from EmbeddedKafka
        
        Delete `spring-kafka-test` dependency:
        ```xml
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
        ```
       Delete `spring.kafka.bootstrap-servers` from `application-test.yml`

- PF-406 Restore `endpoint` and `method` in MDC context
- Add `base.xml` with the default logging configuration for Logback. A project can now define their own logback.xml
as follows:
    ```
    <?xml version="1.0" encoding="UTF-8"?>

    <configuration>
        <include resource="nl/ing/lovebird/logging/base.xml" />
    </configuration>

    <!-- extra config for project -->
    ```
    This way not the complete `logback.xml` from LBC needs to be copied, so global changes can easily be applied with 
a new release of LBC.

## 4.0.33
- Added bankingEnrollmentCompleted flag in UserContext.

## 4.0.32
- Removed owasp plugin, local profile and configuration (inlined everything in global-ci-files)

## 4.0.31
- Remove unnecessary bean VaultSSLWebServerFactoryCustomizerVaultAgent` which is also provided by `vaultSSLWebServerFactoryCustomizer`
  definition.

## 4.0.30 
- Bug fix: wrong property name defined in VaultSSLWebServerFactoryCustomizer which made Vault crash (`:` is not allowed in the name)

## 4.0.29
- Added emailAddressVerified flag in UserContext.

## 4.0.28
- Added option to specify different filenames for the cert, issuing ca and private key in the `/vault/secrets` directory
  for the WebServerFactoryCustomizer when using vault-injector. You can use the following properties to specify a 
  different filenames:
  
  ```
  yolt:
      vault:
          secrets:
            tls:
              cert-filename: security_profiles_cert
              issuing-ca-filename: security_profiles_issuing_ca
              private-key-filename: security_profiles_private_key
  ```
  This is only necessary when you have a pod with multiple services, when you have a pod with a single service specifying 
  these properties it not necessary.

## 4.0.27
- Removed sonar plugin, profile and configuration (moved everything to Gitlab runners and global-ci-files)

## 4.0.26
- Remove `MDCRunnable`. Propgation of the MDC context is handled by Sleuth
- Removed deprecated `TimeConstants`. Use `java.util.concurrent.TimeUnits` or `java.time.Duration` instead.
- Removed `LogSLowRequestFilter`.
-
## 4.0.25
- Fix C* snapshot resolution path for tests

## 4.0.24
- Removed checkstyle plugin (moved to global-ci-files)
- Removed Fortify dependency (not needed anymore)

## 4.0.23
- Upgrade to latest Spring Boot 2.2.6 release which will fix some vulnerable dependencies reported by OWASP Dependency Check

## 4.0.22
- Added clientUserId to UserContext

## 4.0.21
- Fixed `CqlStatementParser` issue with not being able to read file contents sometimes
- Renamed `user_id` MDC key to `user-id`.
- Removed `RequiredHeadersFilter`
- Removed `YoltAsyncWebAutoConfiguration`
  - Controllers that return `Callable<ResponseEntity<?>>` will no longer be handled by the LBC provided thread pool. 
    remove the `Callable` part to have Tomcat handle your request.

## 4.0.20
- Added isYolt2User flag in UserContext.

## Vault agent default template

From release 4.0.12 until 4.0.19 Vault configurations have been added for Postgres, Cassandra, TLS, Kafka you only need to add a label to the pod telling Vault agent should be used. During a deployment the correct template will be added automatically.

```
spec:
  templates:
    metadata:
      labels:
        vault-agent: "true"
```

In the `application.yml` enable Vault with the following setting: `yolt.vault.enabled=true`

### Cassandra changes:

```

        # Cassandra configuration

        # DO NOT FORGET:
        #
        # Set the following properties
        # yolt.vault.enabled=true
        # yolt.vault.cassandra.enabled=true 
        #
        # Remove SPRING_DATA_CASSANDRA_USERNAME and SPRING_DATA_CASSANDRA_PASSWORD env config in K8s deployment file

```

### Postgres changes:

```
        # Postgres configuration
        
        # DO NOT FORGET:
        #
        # Set the following properties
        # yolt.postgres.set-role=true
        # yolt.vault.enabled=true
        # yolt.vault.postgresql.enabled=true
        # spring.datasource.driverClassName=org.postgresql.Driver
        # spring.datasource.url=jdbc:postgresql://rds.${environment}.yolt.io:5432/<<name_database>>

  To enable Postgres with Vault add rds to the labels

  spec:
    templates:
      metadata:
        labels:
          rds: "true"
```

## 4.0.19
- Added support for vault-injector for reading C* credentials instead of old vault-cassandra approach.
  Most of the changes in k8s deployment file should be already there since `4.0.15` release (please, check changelog below).
  Additionally, on any environment (from team to production) credentials to C* can be obtained either from properties 
  (`spring.data.cassandra.username` and `spring.data.cassandra.password`) or from vault-agent-injector. For the latter 
  you need to set both `yolt.vault.enabled` and `yolt.vault.cassandra.enabled` to 'true'. Also, make sure that in test 
  profile you specify cassandra credentials in properties and disable either one or both vault properties mentioned above 
  because vault-agent-injector is running as a sidecar in kubernetes.
  
  For migration see above (Vault agent default template)

## 4.0.18
- Added YoltAssistancePortal in the ClientTokenRequester enum

## 4.0.16
- Added support for using vault-injector for reading Kafka certificates from filesystem instead of using VaultTemplate. 
  In a couple of releases the Platform team will remove support for `VaultKafkaKeystoreInitializerVaultTemplate` and 
  we no longer fetch Kafka certificates for the pod using Spring Vault. Everything will be done with the vault-injector
  sidecar as discussed in the backend chapter meeting.
  
  For migration see above (Vault agent default template)


## 4.0.15
- Added support for using vault-injector for reading certificates from filesystem instead of using VaultTemplate. 
  In a couple of releases the Platform team will remove support for `VaultSSLWebServerFactoryCustomizerVaultTemplate` and 
  we no longer fetch certificates for the pod using Spring Vault. Everything will be done with the vault-injector
  sidecar as discussed in the backend chapter meeting. 

  For migration see above (Vault agent default template)

- Added YAP/yolt-assistance-portal as a client-token requester service

## 4.0.14
- Added `yolt.postgres.set-role` to control default role setting for PostgreSQL connection.   
  For migration see above (Vault agent default template)


## 4.0.13
- Added Cassandra test-containers as an alternative to EmbeddedCassandra.

## 4.0.12
- Updated 'testsupport-cassandra' on 'cassandra' module dependencies from 1.47.0 (lost somewhere when migrating to new Nexus) to 1.47.1
- Upgraded 'translation-fetching-maven-plugin' to version 1.6 (no inner LBC dependency, new Nexus, general upgrades)
- PostgresAutoConfiguration now uses vault-agent-injector and no longer uses Spring Vault project to fetch credentials
  For migration see above (Vault agent default template)


## 4.0.8
- Added ClientGroupToken to client-tokens

## 4.0.7
- Removed Callable from DeleteUserController (you may have to change tests that use MockMvc) 
- Bump Springfox version to fix duplicate model declarations

## 4.0.6
- Removed injection of VaultAuthentication configuration in favour of VaultTemplate and others.
- Removed explicit VaultAuthentication instantiation to prevent duplicate Vault SessionManagers
- Getting rid of `jackson-mapper-asl` since it contains a lot of vulnerabilities
- Unification of CQL files parsing between test and prod usages (via `CqlStatementParser` in `cassandra` module)
- Cleaned up `logging` module a bit (removed unused stuff)

## 4.0.5
- Enable encode strings in `KafkaDefaultHeaderMapper` as json strings for backwards compatibility

## 4.0.3 / 4.0.4
- Removed `validation` project, there are better alternatives. PRs are waiting in the projects

## 4.0.2
- Fixed client-token verification exceptional responses.
  Missing client-token header (and such) now responded with a 401 instead of 500.
- Removed `YoltTomcatBackwardsCompatibleInsecureHttpPortCustomizer`

## 4.0.0 / 4.0.1
- Bump Spring Boot to 2.2
  - You can use JUnit 5 to run tests
  - Use `@EmbeddedKafka` instead of `EmbeddedKafkaRule`. 
  - Ensure all Kafka topics are present while testing or use `spring.kafka.listener.missing-topics-fatal=false`
  - Use `application.yml` instead of then `application.properties`
  - Use `org.springframework.hateoas.server.mvc.WebMvcLinkBuilder` instead of `org.springframework.hateoas.mvc.ControllerLinkBuilder` 
  - Ensure your `@WebMvcTest` work with `secure = true`
  - For more see: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.2-Release-Notes

- Bump Swagger to 3.0.0
  - Rename any `io.springfox` dependencies to `io.yolt.springfox`. (We're using pre-release version of swagger with OpenAPI 3.0.0 support).
  - Replace `@EnableSwagger2` with `@EnableSwagger2WebMvc`
     - Do not use `@EnableSwagger2WebMvc` on your `**Application` directly but apply it to `**ApplicationConfiguration`.
  - If your project defines a `Docket` bean note that the regex pattern does not include the `server.servlet.context-path`
    (e.g. `site-management`). You'll have to add this to the pattern.
  - If your project defines `yolt.commons.swagger2.paths` see above.   
  - If necessary add dependency on guava (used to be provided by spring fox)
```xml
<dependency>
    <groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
</dependency>
```
- Bump shared libs
  - yolt-shared-dtos 5.x
  - yolt-kafka-clients 4.x
  - yolt-service-clients 4.x
- Lovebird commons changes
  - Removed `MdcRunnable` from `ThreadPoolTaskExecutor`. Check if custom correlation still works.
  - Removed `MdcUnpackFilter`. Check if custom correlation still works.
  - Excluded Sleuths legacy trace keys from logstash. See `org.springframework.cloud.sleuth.log.Slf4jScopeDecorator`
  - Deprecated `KeyspaceTruncatingTestExecutionListener`. Prefer creating users/accounts/transactions/ect with 
    random UUIDs instead.

## 3.5.2
- Added ClientIdVerificationService to verify that a client-token has a claim for a specific client-id.
- Removed dryrun code for client-tokens
- Added `yolt.vault.auth.max-refresh-before-token-expiry-hours` to control the upper bound of the random number of hours
  before a token is refreshed.

## 3.5.1
- Added kafka header typings for ClientToken.
  Can now be directly used as `@Header(ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME) ClientToken token`.
- Clarified the error when a client-token does not have an allowed isf claim for a @VerifiedClientToken parameter.
- Restricted parsing of client-tokens. Required claims such as isf cannot be null or empty.

## 3.5.0
- Removed deprecated `PaydayCycle` property from `UserContext` (`PaydayCycleConfig` is already used everywhere)

## 3.4.0
- Removed `LovebirdHttpClient` and friends
- Removed `KafkaConsumerWrapper` and friends
- Upgrade to spring-boot 2.1.9
- Upgrade to spring-cloud Greenwich.SR3
- Removed dependency on `io.opentracing.brave:brave-opentracing.`Not used, Brave version conflicts with Sleuth. 
  Use brave directly if you must.
- Removed dependency on `com.squareup.okhttp:okhttp:2.7.5`. Add this to your project if still used:
```xml
<dependency>
    <groupId>com.squareup.okhttp</groupId>
    <artifactId>okhttp</artifactId>
    <version>2.7.5</version>
    <!-- okhttp3 depends on a higher patch version -->
    <exclusions>
        <exclusion>
            <groupId>com.sqaureup.okio</groupId>
            <artifactId>okio</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```
- Deprecated `CassandraHelper.truncate`  We run all all model mutation .cql
  files at application startup. Truncating tables will remove this data. This
  means that one test will have the data, the next won't. This results in
  flakey tests. Prefer creating users/accounts/transactions/ect with random
  UUIDs instead.

## 3.3.5

- Removed the Spring `@Configuration` from Vault project and move creating the beans to YoltVaultAutoConfiguration

## 3.3.4

- Expired client-tokens are no longer invalid, but will trigger a SEMA-event. 

## 3.3.2

- Fixed the problem where the Vault Cassandra integration would keep the service in a zombie state after the Vault goes down while the service is trying to refresh the token. To do this, the `spring-vault-core` dependency has been selectively upgraded to include the fix from Spring Vault. 

## 3.3.1

- Introduces user-context project to be able to only use the UserContext classes without pulling in the endpoints
  provided in the rest project as well. Api-Gateway uses UserContext but does not automatically wants to expose 
  the endpoints by taking a dependency on `lovebird-rest`

## 3.3.0
- Changed ClientTokenRequestService.getClientToken return type to be able to retrieve parsed (and verified) 
  client-tokens.
- Renamed ParsedClientToken to ClientToken for easier usage.
- Upgrade to Jackson 2.10 

## 3.2.46
- Client-token dry-run functionality is changed to return successfully verified client-tokens, instead of always returning null.

## 3.2.45
- Lowered potential noisy client-token warnings to info level, to make sure we do not accidentally alert standby.

## 3.2.44
- Combining client-tokens requester callbacks and flywheel into site-management.

## 3.2.43
- Adding BouncyCastle as SecurityProvider for PSS algorithms.

## 3.2.42
- Support to use `client-tokens` requester functionality without verification and vice-versa.  

## 3.2.41
- Added @VerifiedClientToken annotation for client-token verification (see: [README](client-tokens/README.md))

## 3.2.40
- Added `client-tokens` submodule ([README](client-tokens/README.md))
- Added ClientTokenRequesterService to request a client-token

## 3.2.37
- Splits long log lines

## 3.2.36
- Moved `compliance` auto configuration by Spring Kafka

## 3.2.33
- Moved to new Spring Boot version 2.1.8

## 3.2.28
## Added
- PaydayCycleConfig that should be used on all DTOs for propagating payday cycle configuration
- New methods on DateIntervalService, which take PaydayCycleConfig argument

## 3.2.27
## Added
Added work around for [spring-cloud-sleuth#1220](https://github.com/spring-cloud/spring-cloud-sleuth/issues/1220). Keys
to be propagated must be whitelisted as propagation keys and slf4 whitelisted mdc keys. E.g.:

```
  sleuth:
    propagation-keys:
    - request_trace_id
    log:
      slf4j:
        whitelisted-mdc-keys:
          - request_trace_id
```

## 3.2.26
## Changed
Changed TTLs for certificate requests to the Vault to 90 days. The reason is that ISP generated a CA certificate for Vault that is only valid for one year. On the other hand, it's practically impossible that a pod doesn't restart in 90 days.

## 3.2.24
## Added 

Option to enable forwarding of the User Not Found error status code. Enabled by `yolt.commons.forward-user-not-found-exception`. Disabled by default. 

## 3.2.22
### Added
Support for Kafka Client Certificates from Vault (disabled by default).

## 3.2.12
## Changed
- Query `<pod-namespace>.modelmutation` with `ConsistencyLevel.LOCAL_QUORUM` in CassandraHealthIndicator to determine pod
  health.

## 3.2.6
### Changed
Removed an old `argLine` setting in the Surefire plugin (a workaround for an older version) that was preventing Sonar from processing the code coverage.


## 3.2.0
### Changed
`type` attribute on `PaydayCycle` of `UserContext` is now `String` in order to avoid deserialization issues during "transport" 
when adding new types of payday cycles

## 3.1.25
### Changed
- Fixed a bug in VaultSSLWebServerFactoryCustomizer
- Removed the autoconfiguration of the ProxyCustizer. This should be done manually for external RestTemplates.

## 3.1.23
### Changed

- Changed Vault Cassandra secrets path to accomodate new SRE convention.
- Added ability to provide explicit role name when getting HTTPS certificate (config-server-docker issue)

## 3.1.22
### Changed

Vault Cassandra secrets now supports multi-keyspace credentials.

## 3.1.21
### Changed
`DateIntervalService` supports new payday cycle type.


## 3.1.20
### Changed

VaultCassandraSecretes is no longer only conditional on a property, but also whether cassandra is on the classpath. 
Useful for pods that don't use cassandra.

## 3.1.19
### Changed

VaultSLLWebServerFactoryCustomer now supports custom host names (used in case we have multiple containers in a pod).

## 3.1.17
### Changed

Kafka Producer configuration `request.timeout.ms` changed back to default 30 seconds. We were getting timeouts.

## 3.1.7
### Changed

YoltTomcatBackwardsCompatibleInsecureHttpPortCustomizer has the default port now (8080), since some services did not explicitly set it.

## 3.1.6
### Changed

The VaultCassandraSecrets was changed so it simply looks at the namespace name instead of the service name (which can be different). This deprecates the following property:
yolt.vault.cassandra.secrets.role

## 3.1.5
### Added 

Added `YoltKafkaJsonSerializer` and `YoltKafkaJsonDeserializer` to be set via  

```
 spring:
    kafka:
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer:  nl.ing.lovebird.kafka.YoltKafkaJsonDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: nl.ing.lovebird.kafka.YoltKafkaJsonSerializer
```


### Deprecated

Deprecated `DefaultKafkaConfigurationParameters.getDafaultKafkaListenerErrorHandler` in favour of auto configuration by 
`YoltKafkaAutoConfiguration`.

Deprecated `KafkaTemplateHelper.createKafkaTemplate` in favour of `@Autowire KafkaTemplate`. 

## 3.1.3
### Deprecated

Deprecated `DefaultKafkaConfigurationParameters.getDefaultConsumerConfig` and 
`DefaultKafkaConfigurationParameters.getDefaultProducerConfig`  in favour of `KafkaProperties#buildConsumerProperties()`.


## 3.1.1
### Added
YoltProxySelector. We no longer can distinguish the internal/external http connections based on it being https, since we'll start using https internally as well.
To auto-configure the YoltProxySelector, just add the following property:
isp:
  proxy:
    auto-configure: false

(besides the existing host and ip properties)

If you cannot use the auto-configure function, take a look at the ProxyCustomizer to see how to add it to the
restTemplate yourself.

Note: This change was also back-ported to version 3.0.18

## 3.1.0
### Added
- Added classes to `datetime` which facilitate working with date intervals based on payday cycles

### Removed
- `JavaTimeAwareObjectMapper` (use the `@Autowired ObjectMapper`)
- JSON serializers and deserializers for `java.time` classes (use the `@Autowired ObjectMapper`)
- deprecated `KafkaTemplateHelper.createKafkaTemplate()` (use the other method in the same class)
- `ZonedDateTimeYoltSerializer` and `ZonedDateTimeYoltDeserializer`

## 3.0.15
### Changed
Extracted utility method to check if Kafka is using secure port to a public util class, so it can be used in custom
producer configurations.

## 3.0.14
### Changed
The default Kafka Producer and Consumer configurations now check the broker addresses to enable SSL in case they use the secure port in Kafka (9093). This is the most transparent way to smoothly support this feature, yet is a bit of autoconfiguration magic.

## 3.0.5
Cassandra retry policy to always do a retry on local quorum. In case of Cassandra node patching, we also want to do retries. Since we cannot spot the difference between a whole datacenter going down or just a Cassandra node, we'll just do a retry on LOCAL_QUORUM always.

### Changed:
- `BaseExceptionHandler` logs all `4xx` errors as `WARN` instead of `ERROR`.

As discussed in the chapter meeting of 26-3, client errors are not errors that we should fire alerting on. It is the app
that is in error, rather then the the backend. The log level has been reduced accordingly.

## 3.0.4
- Lombok 1.18.6 (it appeared that we did not upgrade Lombok yet) 
From now on, Lombok no longer generates private constructors. This can be solved by a lombok.properties file in your project with the following:
lombok.anyConstructor.addConstructorProperties=true

This was rolled out by the mass-project-upater a while ago, so the lombok.properties file is probably in all projects already.

## 3.0.2
### Changed
- New attributes on `UserContext`: `paydayCycle` and `testGroupId`.

## 3.0.1
### Changed
- When you enable Vault for HTTPS now it's not required anymore to connect via HTTPS, both connectors are valid for all endpoints.

## 3.0.0
### Changed
- Spring Boot 2.1: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.1-Release-Notes
- Spring Framework 5.1: https://github.com/spring-projects/spring-framework/wiki/Upgrading-to-Spring-Framework-5.x#upgrading-to-version-51
- Spring Cloud Greenwich: https://github.com/spring-projects/spring-cloud/wiki/Spring-Cloud-Greenwich-Release-Notes
- Spring Kafka 2.2: https://docs.spring.io/spring-kafka/docs/2.2.0.RELEASE/reference/html/whats-new-part.html#spring-kafka-intro-new
- Lombok 1.18.4: https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.1-Release-Notes#lombok
### Removed
- `lovebird.max-web-thread-pool-size` property in favor of `lovebird.asyncweb.max-web-thread-pool-size`
# Migration
When you start using this version, you might need to do some magic:
- Overriding of beans is no longer allowed by default, you might need to set the following property for integration tests:
  spring.main.allow-bean-definition-overriding=true
- KafkaEmbedded is deprecated, use EmbeddedKafkaRule instead. NOTE: do not use it as a @ClassRule, since you'll only want to spin it up once. Check here for more info: https://docs.spring.io/spring-kafka/docs/2.2.0.RELEASE/reference/html/_reference.html#_using_the_same_broker_s_for_multiple_test_classes
- containerFactory.getContainerProperties().setErrorHandler(kafkaErrorHandlingService); 
went to: 
containerFactory.setErrorHandler(kafkaErrorHandlingService);

## 2.17.3
- Added a way to copy an existing VaultConfiguration with a different address. ``VaultConfiguration#copyWithDifferentAddress``

## 2.17.2
### Added
- Swagger API docs so they appear in all API docs using them (`ErrorDTO` and `LinkDTO`)
- Added `UserContext` class to be ignored by the Swagger auto configuration

## 2.17.0
- As per backend chapter meeting 12-3 removed some custom code that isn't really necessary:
    (the whole mvc-testsupport module is removed, since it was deprecated per 9-2018)
    * MockMvcHelper: You can do all things manually without generating more LoC. You can drop the 'Callable' return types so you don't have
    to use the asyncDispatch helper methods. Note that the default global config is 200 max tomcat webthreads. If you need to have more,
    you should configure that in your service.
    * HealthMeter/Json/Matchers/Resource. These are rarely used and don't provide much on top of standard libraries. If you use them, you
    might need to do small rewrites on tests. 
- Dropped the custom rules for failsafe. Failsafe is for integration tests and wont fail the build fast. Surefire is for UT and will fail.
 We agreed to dropping custom rules. That means tests will be executed as follows:
 1) Surefire. 
 2) Failsafe (everything that matches default inclusion rules https://maven.apache.org/surefire/maven-failsafe-plugin/examples/inclusion-exclusion.html)
 You likely don't want to use failsafe, but failfast through surefire as we don't do 'maven integration tests' (spinning up containers etc).
 Make sure you will not name your tests as 'IT' because that will continue the build (eventually failing it) while those tests fail.
 
## 2.16.6
- Added a configuration class for the vault key-value store.

## 2.16.5
- Bugfix: `CassandraVersioner` now starts in the right order (before repositories are initiated)  
- Bugfix: `CassandraVersioner` now also works with (open-)tracing enabled.

## 2.16.4
### Changed
- Preparing for HTTPS connection between services: modified logic to detect whether a request is internal or external.
- Simplified Vault configuration and introduced some naming conventions.
- Integrated changes in Embedded Tomcat so it opens both the secure and the insecure port.

## 2.16.3
### Changed
- Set `user_id` on the MDC if it comes in through header `user_id`

## 2.16.2
### Changed
- Vault Authentication as an independent module, that will take care of Cassandra secrets and also retrieving PKI.
- Vault can now be used to get server certificates and run a HTTPS server for the service. 

## 2.16.1
### Changed
- Less logging in `KeyspaceTruncatingTestExecutionListener`

## 2.16.0
### Changed
- Added Cassandra Vault support

## 2.15.17
### Changed
- Bugfix: `CassandraVersioner` is now consistent in filtering: it won't, so it will timeout if you place non-CQL files in that folder

## 2.15.16
### Changed
- Bugfix: `CassandraVersioner` now supports CQL filenames with spaces

## 2.15.15
### Changed
- Bugfix: Message Translator validation

## 2.15.14
### Changed
- Fixed `CassandraVersioner` so now it works with Sleuth.

## 2.15.13
- Used prefix for message translations properties. Rerun the https://git.yolt.io/backend/translation-fetching-maven-plugin

## 2.15.10
### Changed
- CassandraVersioner now waits for CQL updates to be applied for both packaged JAR and resources in file system.
- CONTAINS A BUG! See 2.15.14

## 2.15.2
### Changed
- Added a custom bean validator that redacts the `rejectedValue` from any violation constraint message to prevent logging of sensitive information.

## 2.15.0
### Changed
- `logging` module no longer depends on `spring-boot-starter-web` for smaller footprint in `datascience-commons` / pods.

## 2.14.4
### Added
- `UserContext` now has `toJson()` and `fromJson(String)` methods for de/serialization.
- Added `@RequiredUser` and `@OptionalUser` annotations for MVC controller `UserContext` arguments (required).
- Spring Security support for authorization through for instance `@PreAuthorize("hasAuthority('KYC_ACCEPTED')")`.
- `UserContextSecurityConfiguration` that extends `WebSecurityConfigurerAdapter` to configure user-context header auth.

### Changed
- `user-context` HTTP/Kafka header has changed to `X-user-context`.
- `UserContext` is now handled by Spring Security as pre-authenticated `@AuthenticationPrincipal`.

### Removed
- `UserContextMethodArgumentResolver` and it's `USER_CONTEXT_OBJECT_MAPPER` field have been removed.

## 2.14.0
### Added
- `MdcUnpackFilter` to copy request header values to MDC while waiting for Sleuth Greenwich;
- support for non-`Callable` MVC Controller responses(!) Just return the DTO you want;
- tests might need to use `@AutoConfigureMockMvc` instead of `MockMvcBuilders.webAppContextSetup`.

## 2.13.19
### Changed
- `LogTypeMarker.getAuditMarker` and `getSemAMarker` are now protected. They should never be used directly. Use the 
`AuditLogger` or `SemaEventLogger` instead.

## 2.13.17
### Fixed
- `templating` module deprecated in favour of https://git.yolt.io/backend/translation-fetching-maven-plugin/tree/master

## 2.13.3
### Fixed
- `MonitoringAutoConfiguration` no longer fails on projects without Kafka.

## 2.13.2
### Removed
- Kafka clients; Use: https://git.yolt.io/backend/yolt-kafka-clients
- Shared DTOs: Use: https://git.yolt.io/backend/yolt-shared-dtos

Prefer the following approach for dependency versions in services:

```
    <properties>
        <yolt-kafka-clients.version>3.0.0</yolt-kafka-clients.version>
        <yolt-service-clients.version>2.7.0</yolt-service-clients.version>
        <yolt-shared-dtos.version>3.0.0</yolt-shared-dtos.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>nl.ing.lovebird</groupId>
            <artifactId>kafka-users</artifactId>
            <version>${yolt-kafka-clients.version}</version>
        </dependency>
        <dependency>
            <groupId>com.yolt</groupId>
            <artifactId>accounts-client</artifactId>
            <version>${yolt-service-clients.version}</version>
        </dependency>
        <dependency>
            <groupId>nl.ing.lovebird</groupId>
            <artifactId>activity-events</artifactId>
            <version>${yolt-shared-dtos.version}</version>
        </dependency>
    </dependencies>
```
This allows a Mass Project Updater job to easily update these versions through `mvn versions:update-properties`.


## 2.13.0
### Fixed
- `CassandraHealthIndicator` was not picked up due to unmatched conditionals in `@ComponentScan` of `@Configuration`.

### Removed
- `/liveness`, `/readiness` & `/metrics` endpints; Spring Boot `/actuator` equivalents are now used everywhere.

## 2.12.0
- UserContext changes some fields to allow default serialization.
- KycStatus is added to UserContext

## 2.11.12
- UserContext loses sensitive fields. If needed, add the usersClient to pom and get the full user.

## 2.11.7
- Fix for changes in 2.11.5

## 2.11.6
### Changed:
- Sensitive fields were removed from `UserContext`; for names, email, phone numbers and more make a direct call to users.

## 2.11.5
### Changed
- `BaseExceptionHandler` is now a final class and can't be extended. It will be included in the yolt-starter-service
  as the default exception handler. Functionality in `BaseExceptionHandler` has been moved to `ExceptionHandlerService`.
  The `ExceptionHandlerService` expects a prefix property which can be set with `yolt.commons.error-handling.prefix`.

### Removed
  `ExceptionMapping` has been removed.  For any custom Exceptions in a service please use the Spring way with 
  `@ControllerAdvice` and `@ExceptionHandling`.
  
## 2.11.0
## Added
- `yolt-service-spring-boot-starter-test` which contains an autoconfiguration for embedded cassandra. Just add it as a test dependency. In the future this will contain AutoConfigurations for all of our test code.
- `cassandra-versioning` module has been added. This provides a mechanism for blocking until cassandra updates are applied  by the cassandra updates pod.
- On boot the application will now block until Cassandra updates are applied by making use of the `cassandra-versioner`

## Removed
- `EmbeddedCassandraConfiguration` replaced with `yolt-service-spring-boot-starter`

## 2.10.37
### Added
- AutoConfiguration for Swagger2

```
    Migration:
    
    1. Remove the @EnableSwagger2 and bean definitions in your project.
    
    2. Add application.yml swagger configuration (for example)
        yolt:
          commons:
            swagger2:
              enabled: false
              paths:
                or:
                - /predicted-.*
                - /recurring-.*
                - /transaction.*
    
    3 Enable swagger2 in config-server (e.a. yolt.commons.swagger2.enabled = true) only for the team/ integration environment
```

### Added:
- AutoConfiguration for Cassandra
- AutoConfiguration for Kafka
  
## 2.10.26
## Changed
- `**/*IntegrationTest.java` classes are now run by the Maven failsafe plugin instead of surefire.

## 2.10.0
### Added
- Berlin standard model for accounts and transactions (shared between providers and site-management services).

## 2.9.9
### Changed
- Use AWS Nexus repository mirror by default for faster dependency & plugin downloads.

### Added
- Run owasp dependency check locally using: `mvn -P owasp-preview-local dependency-check:check`
- Added beneficiaries to `ProviderServiceResponseDTO`.

## 2.9.8
### Added
- Added the tag update HATEOAS link for bulk-tagging on the `TransactionDTO`

## 2.9.2
### Fixed
- Unregistered exceptions were logged without additional MDC keys. 

## 2.9.1
### Added
- General `ObjectMapper`, `Jackson2ObjectMapperBuilder`, `Jackson2ObjectMapperBuilderCustomizer` Beans, which should override service bean.
Remove any jackson configuration from the services.

## 2.9.0
### Added
- TransactionDTO input validation for descriptions and notes.
- TransactionResourceAssembler - better handling of responses on internal endpoints.

## 2.8.8
### Added
- Optionally enable OpenTracing for Cassandra driver through `yolt.commons.cassandra.tracing.enabled`.

## 2.8.4
### Added
- Methods, classes and interfaces annotated with @io.micrometer.core.annotation.Timed now register metrics.
- All Cassandra repository methods get metrics when using `yolt.commons.timedaspect.enabled=true`.

## 2.8.1
### Added
- TransactionDTO input validation for categories, merchants and transactionId's.
- `saveBatch()` method on `CassandraRepository`

## 2.7.4
### Fixed:
- ExceptionHandler log messages were lacking MDC variables.

## 2.6.8
- Required headers are now enforced (and configurable) through a filter

## 2.6.7
- Web requests taking longer than 1s are now logged to the monitoring index

## 2.6.6
- UserContextMethodArgumentResolver now uses a custom ObjectMapper bean

## 2.6.5
- `BaseExceptionHandlers` now extends `ResponseEntityExceptionHandler` and handles `Throwable`
- Calls to `BaseExceptionHandlers#addExceptionMapping` can be replaced with plain Spring handling:

```
    @ExceptionHandler(MyServiceException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    @ResponseBody
    public ErrorDTO handleMyServiceException(MyServiceException ex) {
        return logAndConstruct(ErrorConstants.MY_CONSTANT, ex);
    }
```

## 2.6.4
- `LoggingCallable` has been deprecated; Return a normal `Callable` (lambda) instead and rely on below auto-configuration.
- Drop any `WebMvcConfigurer#configureAsyncSupport` setup of `TaskExecutor`; It's provided by `YoltAsyncWebAutoConfiguration`.

## 2.6.0
Made the exception handlers to only log on a custom loglevel if a function is provided that returns an ErrorResponse object. For example:
```
addExceptionMapping(SomeAwesomeException.class, (e) ->
                new ErrorResponse(UNSUPPORTED_LOGIN_TYPE, HttpStatus.BAD_REQUEST, Level.ERROR));
```
This way, you'll have control over everything in the function: the http status, loglevel and error message/code.

## 2.5.10
- removed some LogMarker methods, which are not expected to be used outside the commons project.

## 2.5.8
- Spring Boot 2.0.3 -> 2.0.4 : https://github.com/spring-projects/spring-boot/milestone/113?closed=1
- Springfox-Swagger 2.8.0 -> 2.9.2 : https://github.com/springfox/springfox/releases
- Guava 19.0 -> 20.0 : https://github.com/google/guava/wiki/Release20

## 2.5.0
- Upgrading Spring Cloud from Finchley.RELEASE to Finchley.SR1

## 2.3.0
- `currency` field on `TransactionDTO` has the type `Currency` (was `String`) 
- More HATEOAS links on `TransactionDTO`

## 2.1.0
A Bean Validation ConstraintValidationException is now handled in BaseExceptionHandlers.
When a validation on a controller fails this results in a bad request (http status 400) with error code 1008


## 2.0.0
1. Upgrade to Spring Boot 2 with Spring Framework 5, Spring Cloud Finchley and Spring Kafka 2.0.x:
 - https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.0-Migration-Guide
 - https://spring.io/blog/2018/06/19/spring-cloud-finchley-release-is-available
 - https://docs.spring.io/spring-kafka/docs/2.0.5.RELEASE/reference/html/whats-new-part.html
2. Actuator endpoints are now nested under `/actuator/`; Update `liveness` & `readiness` to use `/actuator/info` and `/actuator/health` respectively.
3. For custom metrics use Micrometer from now on; not codahale metrics. Have prometheus scrape `/actuator/prometheus`; expect/adopt  metric name changes.
4. `PrometheusKafkaHelper`, `PrometheusCassandraHelper` & `PrometheusHttpHelper` have been dropped in favor of out of the box replacements.
5. `@RepositoryHealthMetered` and `@ServiceClientHealthMetered` have been dropped in favor of applying Hystrix.
6. `UrlEncoding` has been dropped.
7. `LovebirdHttpClient` has been deprecated in favor of `RestTemplate` for trace propagation & metrics.
8. Previously deprecated methods and classes have been dropped.

## 1.51.2
1. `UserDeleter` and `/delete-user` need to be enabled explicitly through `yolt.service.userdelete.enabled=true`, to only expose endpoint when needed.
2. Yolt service clients only have to be added to the classpath; Do not `@Import` or `@ComponentScan` them anymore.
3. Ensure your `@SpringBootApplication` doesn't live on `package nl.ing.lovebird`, to prevent scanning that package.
4. Guava needs to be pinned to `compile("com.google.guava:guava:19.0")` while waiting for Maven BOM.


## 1.50.0
Introduce Maven parent POM with managed dependency and plugin versions.
Ideally service projects adopt this Maven parent at their earliest convenience:

```
    <parent>
        <groupId>nl.ing.lovebird</groupId>
        <artifactId>lovebird-commons</artifactId>
        <version>1.50.0</version>
        <relativePath />
    </parent>
```
Additionally a spring-boot-starter is provided to replace `@ComponentScan("nl.ing.lovebird")` and common service dependencies.
It provides `documentation`, `error-handling`, `http`, `logging`, `monitoring`, `rest` and `validation` via:

```
    <dependency>
        <groupId>nl.ing.lovebird</groupId>
        <artifactId>yolt-service-spring-boot-starter</artifactId>
        <version>1.50.0</version>
    </dependency>
```
Both these changes combine to offer easily and consistently managed dependency versions to aid in the upcoming transition to Spring Boot 2.
The basic flow in converting a service involves:
1. Run `git checkout -b maven`.
2. Run `./gradlew install && cp build/poms/default-pom.xml pom.xml`
3. Add above `lovebird-commons` as parent.
4. Add above `yolt-service-spring-boot-starter` as dependency.
5. Remove transitive dependencies such as, but not limited to, `monitoring`, `rest`, `spring-*`.
6. Remove version numbers from retained managed dependencies.
7. Possibly fix any library upgrade issues.
8. Add Maven wrapper using `mvn -N io.takari:maven:wrapper`.
9. Remove Gradle wrapper, `build.gradle`, `settings.gradle` & `parent.gradle`.
10. Push branch & run mass-project-updater to update `.gitlab-ci.yml`.


## 1.47.0
Mockito has been upgraded from 1.10 to 2.17; with some minor breaking changes.
For upgrade see: https://github.com/mockito/mockito/wiki/What%27s-new-in-Mockito-2

## 1.46.0
Breaking change! Moved service clients to separate project: yolt-service-clients

## 1.39.0
Some breaking changes in CassandraHelper (test-dependency)
It is also not possible to use the @Table.keyspace property since this has to be inserted dynamically. The keyspace should be configurable
with the deployment configuration.
You can configure the keyspace in 2 ways:
1) make sure the spring.data.cassandra.keyspaceName is set. This binds the session to a particular keyspace (USE keyspace). Prefixing your queries is not necessary.
2) If you REALLY need to access more keyspaces, it is recommended by datastax to not create multiple sessions, but prefix your queries.
   You will need to create your respository with the constructor that takes the keyspace argument.
   Be aware that the select and selectOne methods which take in a full Statement will need the keyspace defined in the statement.

## 1.38.2
Lombok is now a compileOnly/testCompile dependency, as it should be.
Projects dependending on Lombok transitively should add a compileOnly dependency themselves, to prevent Lombok from being packaged in the final jar.

## 1.32.0
Rename ZonedDateTimeSerializer to ZonedDateTimeYoltSerializer to discern it from ZonedDateTimeISOSerializer.
WARNING: if you used new ZonedDateTimeSerializer() before, you implicitly used the YOLT_DATE_FORMAT. Make sure you import the correct (De)Serializer! 

## 1.27.0
Change UserDTO#id in users-client form String to UUID.


## 1.26.1
Downgrade spring-boot to 1.5.7, since 1.5.8 uses a bundled tomcat (8.5.23) that has a reported CVE.


## 1.26.0
1. Add Account recovery client
2. Upgrade dependencies to latest patch version. e.g spring-boot 1.5.6 -> 1.5.8


## 1.18.0 - > 1.19.0
### Readiness endpoint
The readiness controller is now rewritten to an (actuator) MvcEndpoint.

Short story: This is only breaking if you previously had actuator disabled, or no actuator at all.
Disclaimer: If you already had actuator, it shouldn't break. However, in case something does go wrong here is some information that might help:

The reason for this change is the fact that we need to have actuator exposed on another port. (management.port in application properties)
Changing this property just broke the application.
Changing the port of the actuator endpoints leads to multiple applicationContexts : AnnotationConfigApplicationContext (bootstrap) - >
The 'normal' AnnotationConfigEmbeddedWebApplicationContext (ACEWAC) - > The 'management' ACEWAC.
(in this particular order! first is parent)

What went wrong:
1) Readiness endpoint is fixed, does not comply to other actuator endpoints.
2) From the readinessController the HealthMvcEndpoint is referenced. This is not allowed to happen, since HealthMvcEndpoint lives
only in a child-context (the management context).
See for example:
https://github.com/codecentric/spring-boot-admin/commit/e59383fa560267b75e4fcc46128abdb9736291ac

How it is fixed: Tried to fix it properly. That is, initiate the bean in the management-context with autoconfiguration.
However, after many attempts that did work when a special port was used, but the bean did not end up anywhere when the actuator serves
on the same port. Now, the dependency on HealthMvcEndpoint is just removed. All necessary 'mvc' functionality is just pasted to our own class.

## 1.40.1
Breaking changes to Kafka client settings.

## 1.41.1
Breaking change add in AccountDTO, where the type field was a String. Converted it to the AccountType enum.
