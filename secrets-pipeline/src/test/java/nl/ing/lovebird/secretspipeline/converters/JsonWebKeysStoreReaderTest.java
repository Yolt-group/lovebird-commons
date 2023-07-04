package nl.ing.lovebird.secretspipeline.converters;

import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonWebKeysStoreReaderTest {
    private static final String BASE64_JWK = "eyJkIjoiYUpXRnU2MlNyVDk1cmFjMUJoU2F3b3VvLVVoMkRfdjF5QUhfVkpTY0ZNRkloVWNaNnE2X3Jkb01MWG1nUGlvWGxrdEN1M2tsNVhYdzhDUDg0TVlQaW1qcmdVaFhNLUpjMlMtMmEtc2EweGFoMXVIMmlaam9PZDU3dmYxN1JXNzdMRTduT0lhcjBZdE5DczJDd1NaaTZxSUlrMGREWUJ4UUVsM0tSSTNObUdQSEIzUmZickZBY3NTUkNqOW9aZFdHazBJd0I2YW1jNGtJRWxHMkRPUWJySUNJQnUwUEJ6SXRFc1IyTHFDNEdfcXBsQ050RTd0WVAtNWtvaGFRUnBsWHpvTEJQd2p2anRTZmhrdmRGQTVPUGtkVXFHNkF4NTJ3Ujl2WVZQRUlHcjZpb0l5aVprYnVZcnU4NV9fOEMzeXdLRDhQYjRncFlONjF5OTJZdkdEWEFRIiwiZSI6IkFRQUIiLCJ1c2UiOiJzaWciLCJraWQiOiJiOTU4NTkwMi01MzBkLTRiMjItYTE0Yi00ZDdjMWQ2ODg3NWMiLCJkcCI6Im9rdjFqZE5WMUhkeldtaHFOVk9MMm91M1VJNndMaU9PekFQT0Fpb0RXcXdlcGEwREVsUWdwU0Z3Qm9LcV9lczdXbjRQVmtuRVFub3JGWkdPUmJGODFtVDVHTERUNlhBczZZZXk4WG1vQ1lFV1Z0V0o5VHFUcTczWWNQNkYzdEkzZ1EzbG5EVmF5dXRVSlpQQ1JfU2g3XzJRUW4xUm5CNk5SMFh4YXo5SGwwRSIsImRxIjoiRXFCWVk2QXNXX1dOYThQdWpiXzBlZ2hVUWRYTTliM3M3ekI0RTNoY3V3UnBOc28yTVQ2eFNZOHRMTm00dWRRZXdDcHBJQjhNMnRPd0Q5eW9EUHd5SldiWkFSbnpPR1VCeWZ6NDgxYmp1YUdqVnp0NUVqN01oYTFkVllkekppcUJxYnNfZ0F2aFkxaXd3bXVjU3VXNmM1RE8tTHExSGxxa2R4SXEwUi1WNTljIiwibiI6Im16UkVJYVFOZHd0MXZZYm9jQUVlTEdVR1J4UGJMbG9HQjAxXy1YQUlsbFRmaEl4ZkhfZTJiaFNVTmZlcG1rcGxLeTRGY1JLTDFicF9pdUxUR2lNNU1jQkswSzl3TjIxR1VDT0ZVRVdaNVVYX0ZNODd6eW9KbzR0bWJ0SEloNUQ3Z2xZcFZ2bWpGdXBralhVcHc2WmhtZnNzSlExdDVKRkxBOWtxTV9hWk1EdHc4bXY2YzIzd1NvdF9CNDdxODV2SEN2bTJibUNKaWZGNzJ5WTdQWFRFNk84LS1CcWh3NWxSQzhGdkNVVGtCTmRjNmpyaEJsRzRaNEozLUJmNHJOWm9yRkVwczM2d0VQcmJzVWQ0ZUp0NkYzVjFDZXNTZThGVFFnTEd3cTR1R3NHWmEtZERRRWZEQXJneVA3R2JqdTVLQ2RoaWdCcE81QzZqclF0cERpRVhadyIsInAiOiI0VVJsRi1COG5POVE4NmRDZkZfTVg5VS1QUERXMjg3bnpPQTc0NFpqRHota2ZYclFEVG1MN1RWa19idG1wMWJDdjZyZHlQbVd5VTFSVEZDaHZRU2dyZHFJWTJ5VEIzZGN2NnlIWmlJY2RiTzItcVhtV1N0ZnFIcWtQYkcyMklrTnJOa3dhVjg5S2g5WC1TVjRHS0Z5OWhkNlJ4RDhxWjJmd0c1TjZ0a1hxMEUiLCJrdHkiOiJSU0EiLCJxIjoic0dEZjJ3Z3NpaHo1OGhGNGRnTnBUbzZqeXZYSWt1RHEyTHVVbFFIazA4UThDbFhzcV9UX080aEIxMWhzdXd3SzlaTTUtektzZ3ZYdlZwNFdVX2xQVTRWSk12Z1BMWjJkeTFzamFPNTRPNjB1VGlhTnV4ODF3eS1rWC1kMjU2LUJnY0R4U2hEWWxBaEtGOEt5NWFDeFRHMjZsMzZiZDIyZzJxQ3M3d2w0WUtjIiwia2V5X29wcyI6WyJzaWduIl0sInFpIjoiel92YUxBVHlKeFRYOGNqQldMZU94WGNVdDl0UEdZU09HaTVTZThxeS1jV05ZZnRZTHFfS3BORVRIV0VSQ2ZMZkM4X3YwbzNaaDZ0ZjhEUnBxZkFWWm9vSTBnMk96cUhhNC1RVTI2aWZjbVY3anRTQ3Noc1RncHFhNU5LR0Y1Y3JBYnp6cUh4UjhXRzNzZXpPU0hrWW83QnBtTUctdmFrVkZlNDlFcVdZd0VzIiwiYWxnIjoiUFM1MTIifQ==\n";
    private static final String BASE64_PUBLIC_JWK = "eyJrdHkiOiJSU0EiLCJlIjoiQVFBQiIsInVzZSI6InNpZyIsImtpZCI6ImI5NTg1OTAyLTUzMGQtNGIyMi1hMTRiLTRkN2MxZDY4ODc1YyIsImtleV9vcHMiOlsic2lnbiJdLCJhbGciOiJQUzUxMiIsIm4iOiJtelJFSWFRTmR3dDF2WWJvY0FFZUxHVUdSeFBiTGxvR0IwMV8tWEFJbGxUZmhJeGZIX2UyYmhTVU5mZXBta3BsS3k0RmNSS0wxYnBfaXVMVEdpTTVNY0JLMEs5d04yMUdVQ09GVUVXWjVVWF9GTTg3enlvSm80dG1idEhJaDVEN2dsWXBWdm1qRnVwa2pYVXB3NlpobWZzc0pRMXQ1SkZMQTlrcU1fYVpNRHR3OG12NmMyM3dTb3RfQjQ3cTg1dkhDdm0yYm1DSmlmRjcyeVk3UFhURTZPOC0tQnFodzVsUkM4RnZDVVRrQk5kYzZqcmhCbEc0WjRKMy1CZjRyTlpvckZFcHMzNndFUHJic1VkNGVKdDZGM1YxQ2VzU2U4RlRRZ0xHd3E0dUdzR1phLWREUUVmREFyZ3lQN0dianU1S0NkaGlnQnBPNUM2anJRdHBEaUVYWncifQ==\n";
    private static final String PRIVATE_EXPONENT = "13202515957477580601661366180958830815135881093820018252368015458324285731353465486449875740139451403348278023043240351620097307769291950846624751067966537573827471885752355243540496768326279198897422386271215979241233477535556068521162314915398198945656968943328251874430305838885518358071674728284794525627359727437320458577748059393243739596997529477850249558067796149782517864263201588393663265923681017689226292491059987292800735714653949801894920555932847321252168680548992222708847168071893514573226974230284229994164742027974970269051569443426554713268811291674691615721065868939444249752174306675552489101057";

    private final VaultKeys vaultKeys = new VaultKeys();

    @Test
    void readJWKS(@TempDir Path tempDir) throws Exception {
        Path testFile = TestUtil.createAsymmetricFile(tempDir, BASE64_JWK, BASE64_PUBLIC_JWK, "testJWKSet", "JWKS", null, false);

        new JsonWebKeysStoreReader().read(testFile, vaultKeys);

        RsaJsonWebKey rsaJsonWebKey = vaultKeys.getRsaJsonWebKey("testJWKSet");
        JsonWebKey jsonWebKey = vaultKeys.getJsonWebKey("testJWKSet");
        PublicJsonWebKey publicJsonWebKey = vaultKeys.getPublicJsonWebKey("testJWKSet");

        assertThat(rsaJsonWebKey.getKeyId()).isEqualTo("b9585902-530d-4b22-a14b-4d7c1d68875c");
        assertThat(rsaJsonWebKey.getAlgorithm()).isEqualTo("PS512");
        assertThat(rsaJsonWebKey.getKeyType()).isEqualTo("RSA");
        assertThat(rsaJsonWebKey.getUse()).isEqualTo("sig");
        assertThat(rsaJsonWebKey.getRsaPrivateKey().getPrivateExponent()).isEqualTo(PRIVATE_EXPONENT);

        assertThat(jsonWebKey.getKeyId()).isEqualTo("b9585902-530d-4b22-a14b-4d7c1d68875c");
        assertThat(jsonWebKey.getAlgorithm()).isEqualTo("PS512");
        assertThat(jsonWebKey.getKeyType()).isEqualTo("RSA");
        assertThat(jsonWebKey.getUse()).isEqualTo("sig");

        assertThat(publicJsonWebKey.getKeyId()).isEqualTo("b9585902-530d-4b22-a14b-4d7c1d68875c");
        assertThat(publicJsonWebKey.getAlgorithm()).isEqualTo("PS512");
        assertThat(publicJsonWebKey.getKeyType()).isEqualTo("RSA");
        assertThat(publicJsonWebKey.getUse()).isEqualTo("sig");
    }

    @Test
    void readJWKSMissingPrivate(@TempDir Path tempDir) throws Exception {
        Path testFile = TestUtil.createAsymmetricFile(tempDir, "", BASE64_PUBLIC_JWK, "testJWKSet", "JWKS", null, false);

        new JsonWebKeysStoreReader().read(testFile, vaultKeys);

        assertThrows(IllegalStateException.class, () -> vaultKeys.getRsaJsonWebKey("testJWKSet"));
        assertThrows(IllegalStateException.class, () -> vaultKeys.getJsonWebKey("testJWKSet"));

        PublicJsonWebKey publicJsonWebKey = vaultKeys.getPublicJsonWebKey("testJWKSet");

        assertThat(publicJsonWebKey.getKeyId()).isEqualTo("b9585902-530d-4b22-a14b-4d7c1d68875c");
        assertThat(publicJsonWebKey.getAlgorithm()).isEqualTo("PS512");
        assertThat(publicJsonWebKey.getKeyType()).isEqualTo("RSA");
        assertThat(publicJsonWebKey.getUse()).isEqualTo("sig");
    }

    @Test
    void readJWKSMissingPublic(@TempDir Path tempDir) throws Exception {
        Path testFile = TestUtil.createAsymmetricFile(tempDir, BASE64_JWK, "\n", "testJWKSet", "JWKS", null, false);

        new JsonWebKeysStoreReader().read(testFile, vaultKeys);

        RsaJsonWebKey rsaJsonWebKey = vaultKeys.getRsaJsonWebKey("testJWKSet");
        JsonWebKey jsonWebKey = vaultKeys.getJsonWebKey("testJWKSet");
        assertThrows(IllegalStateException.class, () -> vaultKeys.getPublicJsonWebKey("testJWKSet"));

        assertThat(rsaJsonWebKey.getKeyId()).isEqualTo("b9585902-530d-4b22-a14b-4d7c1d68875c");
        assertThat(rsaJsonWebKey.getAlgorithm()).isEqualTo("PS512");
        assertThat(rsaJsonWebKey.getKeyType()).isEqualTo("RSA");
        assertThat(rsaJsonWebKey.getUse()).isEqualTo("sig");
        assertThat(rsaJsonWebKey.getRsaPrivateKey().getPrivateExponent()).isEqualTo(PRIVATE_EXPONENT);

        assertThat(jsonWebKey.getKeyId()).isEqualTo("b9585902-530d-4b22-a14b-4d7c1d68875c");
        assertThat(jsonWebKey.getAlgorithm()).isEqualTo("PS512");
        assertThat(jsonWebKey.getKeyType()).isEqualTo("RSA");
        assertThat(jsonWebKey.getUse()).isEqualTo("sig");
    }
}