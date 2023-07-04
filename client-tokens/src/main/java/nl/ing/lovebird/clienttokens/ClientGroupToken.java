package nl.ing.lovebird.clienttokens;

import org.jose4j.jwt.JwtClaims;

public class ClientGroupToken extends AbstractClientToken {
    public ClientGroupToken(String serialized, JwtClaims claims) {
        super(serialized, claims);
    }
}
