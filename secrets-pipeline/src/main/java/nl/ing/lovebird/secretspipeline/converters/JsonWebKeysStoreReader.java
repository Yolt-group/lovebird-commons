package nl.ing.lovebird.secretspipeline.converters;

import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.bouncycastle.util.encoders.Base64;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static nl.ing.lovebird.secretspipeline.KeyUtils.readFile;
import static nl.ing.lovebird.secretspipeline.KeyUtils.splitAsymmetric;

public class JsonWebKeysStoreReader extends KeyStoreReader {
    @Override
    List<String> getKeyExtensions() {
        return Collections.singletonList("JWKS");
    }

    @Override
    public void read(Path file, VaultKeys keys) throws Exception {
        byte[][] fileContents = splitAsymmetric(readFile(file));
        if (fileContents[0].length > 0) {
            String jwkJson = new String(Base64.decode(fileContents[0]));
            JsonWebKey jsonWebKey = JsonWebKey.Factory.newJwk(jwkJson);
            keys.addPrivate(entryName(file), jsonWebKey);
        }

        if (fileContents[1].length > 0) {
            String publicJWKJson = new String(Base64.decode(fileContents[1]));
            PublicJsonWebKey publicJsonWebKey = PublicJsonWebKey.Factory.newPublicJwk(publicJWKJson);
            keys.addPublic(entryName(file), publicJsonWebKey);
        }
    }
}
