# Client-Tokens Test

This project contains functionality which can be used to create client tokens
for testing.

```java
public class Example {

    final TestClientTokens testClientTokens = ...

    @Test
    void example() {
        ClientGroupToken clientGroupToken = testClientTokens.createClientGroupToken(clientGroupId, clientId);
        ClientToken clientToken = testClientTokens.createClientToken(clientGroupId, clientId);
        ClientUserToken clientUserToken = testClientTokens.createClientUserToken(clientGroupId, clientId, userId);
    }
}
```

## Setting claims

Client tokens created by `TestClientTokens` are instantiated with a sensible
set of default claims. To test specific feature toggles these claims can be
modified by passing a mutator function. 

```java
public class MutatorExample {

    final TestClientTokens testClientTokens = ...

    @Test
    void exampleWithMutator() {
        ClientUserToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId, claims -> {
            claims.setClaim(ClientTokenConstants.EXTRA_CLAIM_ISSUED_FOR, "secret-service");
            claims.setClaim(ClientTokenConstants.CLAIM_PSD2_LICENSED, false);
            claims.setClaim(ClientTokenConstants.CLAIM_CLIENT_USERS_KYC_ENTITIES, true);
            claims.setClaim(ClientTokenConstants.CLAIM_CLIENT_USERS_KYC_PRIVATE_INDIVIDUALS, true);
        });
    }
}
```

## Spring Tests

When used through `client-tokens-starter-test` integration and slice tests will
be configured with a `TestClientTokens` and `ClientTokensParser` bean that
create signed and accept signed client tokens.

### Integration tests 

```java
@SpringBootTest
class ExampleIntegrationTest {
    
    @Autowired
    TestRestTemplate resttemplate;
    @Autowired
    TestClientTokens testClientTokens;
    
    @Test
    void test() {
        UUID clientGroupId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ClientUserToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId);

        ResponseEntity<String> response = resttemplate.exchange(RequestEntity
                .get(URI.create("/greet/client-user-token"))
                .header("client-token", token.getSerialized())
                .build(), String.class);

        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
    }
}
```
### Slice tests

```java
@WebMvcTest(GreetingController.class)
class ExampleWebbMvcTest {
    
    @Autowired
    MockMvc mockmvc;
    @Autowired
    TestClientTokens testClientTokens;
    
    @Test
    void test() {
        UUID clientGroupId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        ClientUserToken token = testClientTokens.createClientUserToken(clientGroupId, clientId, userId);
        mockmvc.perform(get("/greet/client-user-token")
                        .header("client-token", token.getSerialized()))
                .andExpect(status().isOk());
    }
}
```

## Stub tests

When used outside of spring boot tests `TestJwtClaims` can be used to quickly
create reasonable client tokens. 

```java
class ExampleStubTest {

    @Test
    void test() {
        UUID clientGroupId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();

        JwtClaims claims = TestJwtClaims.createClientClaims("junit", clientGroupId, clientId);
        claims.setClaim(...);
        ClientToken clientToken = new ClientToken("stub-client-token-serialization", claims);

        // Use client token here
    }
}
```

If the serialized form can not be stubbed, a valid signed serialized
representation and parser accepting that representation can be created. 

Note: `generateJwk` is slow. Consider reusing the `signatureJwk` between tests.

```java
class ExampleStubTest {

    @Test
    void test() {
        RsaJsonWebKey signatureJwk = RsaJwkGenerator.generateJwk(2048);
        TestClientTokens testClientTokens = new TestClientTokens("example-service", signatureJwk);

        UUID clientGroupId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        ClientToken clientToken = testClientTokens.createClientToken(clientGroupId, clientId);

        JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(signatureJwk);
        ClientTokenParser parser = new ClientTokenParser(jsonWebKeySet.toJson());

        ClientToken parsedClientToken = (ClientToken) assertDoesNotThrow(
                () -> parser.parseClientToken(clientToken.getSerialized()));
    }
}
```

