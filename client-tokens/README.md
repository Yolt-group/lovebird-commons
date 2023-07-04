# Client-Tokens

This project contains functionality which can be used to request and verify client-tokens.
Client-Tokens are created in the Tokens service and are used to give a certain service authorization for executing methods on behalf of a client.

For example, there are certain endpoints in the providers service that can only be called by (saltedge/budget insight) callback processors.
Once a callback from Budget Insight comes in, site-management needs to request a client-token for the client. This token then needs to be added as a header in the requests to providers.   

Additionally, the client-token contains information in the claims, such as the client-group-id, which are important for the retrieving private keys from the HSM.

## Requesting Services
Services that can request client-tokens are listed in the config-server in the tokens directory under `tokens.client-token-requester.services-jwks`.

### Requester
One generic part of this module is the requester part. When a microservice has the right properties when starting up, spring will create a service for retrieving client-tokens.
Based on the presence of `/vault/secrets/client-token-req-jwks` it will autoconfigure a `ClientTokenRequesterService` bean.

Deprecated:
The service is of the type ClientTokenRequesterService, the name of the bean will be the same as the requesting service ("api-gateway", "client-gateway", ...)
This can then be injected by `@Autowired @Qualifier("site-management") ClientTokenRequesterService service`. 

### Allowing a new service to request client-tokens
Please make sure you need to add a client-token requester. Only edge services should be to client token requesters, usually services should just forward client-tokens from calling services.
Especially you should *not* create a client-token requester for something temporary/hacky just to call APIs with something like kubernetes-otc. 
1. Create a JWKS type secret via secrets-pipeline and name it `client-token-req-jwks` (refer to `assistance-portal-yts` for an example). Merge the generated secret to the service.
2. Take the `publicKeyOrCertContent` part per environment, base64 decode them, wrap them in jwkses and put them into the tokens config-server files under `tokens.client-token-requester.services-jwks`.
3. Pin to new config-server version and release tokens
4. In the requesting service: 
    - Set `yolt.client-token.requester.enabled` to true
    - Set `yolt.client-token.requester.vault-based-secret.enabled` to true
    - Tokens URL property: `service.tokens.url` (something like `https://tokens/tokens`)
    - Tokens JSON Web Key Set property: `service.tokens.signature-jwks`
    - The ClientTokenRequesterService can then be autowired.

## Token verification
Services can protect endpoints from unauthorized client calls using the `@VerifiedClientToken`
annotation. Endpoints protected this way can only be called by including a valid client-token in
the `client-token` header of the request.

### Usage
To enable token verification you'll need to set the `yolt.client-token.verification.enabled`
configuration variable.

You'll also need the `tokens` JSON Web Key Set, so that we can validate that a client-token is 
signed by tokens. You'll need to add this as the `service.tokens.signature-jwks` configuration
variable.

Add the @VerifiedClientToken annotation to a ClientToken endpoint parameter. The
ClientToken will be automatically resolved if a valid client-token is provided in the
`client-token` header.
```java
@GetMapping public String protectedEndpoint(
  @VerifiedClientToken ClientToken clientToken
) {
  return String.format("Hello, %s", clientToken.getClientIdClaim());
}
```
See JavaDoc of @VerifiedClientToken for more usage info.

## Kafka
It is also possible to pass client-tokens as a header on kafka messages. You can put the serialized
jwt as value in this header, or a fully parsed and verified ClientToken object. Internally these are
always transported in its serialized jwt form.

Consuming messages with this header is as easy as adding an additional parameter to the 
`@KafkaListener` annotated method.
```java
@KafkaListener(topics = "EXAMPLE_TOPIC", groupId = "example-groupId")
public void consume(
  @Header(value = ClientTokenConstants.CLIENT_TOKEN_HEADER_NAME) ClientToken clientToken
) { 
  // you can use clientToken here
}
```

Note that the message will not be consumed if the client-token in the header is invalid.

## Client-token and client-id comparison
Sometimes it is necessary to compare a specific client-id with the client-id claim of the
client-token. This can be done using the `ClientIdVerificationService`, which verifies that the
client-id inside a client-token matches another client-id. For security reasons a SEMA event is
logged when this happens.

## Client-token and client-group-id comparison
Sometimes it is necessary to compare a specific client-group-id with the client-group-id claim 
of the client-token. This can be done using the `ClientGroupIdVerificationService`, which 
verifies that the client-group-id inside a client-token matches another client-group-id. For 
security reasons a SEMA event is logged when this happens.