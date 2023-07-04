package nl.ing.lovebird.secretspipeline.converters;

import com.yolt.securityutils.crypto.KeyPair;
import com.yolt.securityutils.crypto.PrivateKey;
import com.yolt.securityutils.crypto.RSA;
import nl.ing.lovebird.secretspipeline.VaultKeys;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.Security;
import java.util.Arrays;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.ing.lovebird.secretspipeline.converters.KeyStoreReader.TYPE_INDICATOR;
import static org.assertj.core.api.Assertions.assertThat;

class CSRKeyStoreReaderTest {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private final VaultKeys keys = new VaultKeys();

    @Test
    void newCreatedPrivateAndPublicKey(@TempDir Path tempDir) throws Exception {
        KeyPair keyPair = RSA.Builder.generateKeys(2048);
        Path testFile = TestUtil.createAsymmetricFile(tempDir, keyPair.getPrivateKey().toPem(),
                toPEM(createCSR(keyPair)),
                "testCSR", "CSR",
                null,
                true);

        new CSRKeyStoreReader().read(testFile, keys);

        assertThat(keys.getPrivateKey("testCSR")).isEqualTo(keyPair.getPrivateKey());
        assertThat(keys.getPublicKey("testCSR")).isEqualTo(keyPair.getPublicKey());
    }

    @Test
    void withExistingCSRKeyPair(@TempDir Path tempDir) throws Exception {
        String privateKey = "LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcEFJQkFBS0NBUUVBdXc2cmtOeUxkaGlkTHJGMWFJaVZLNWtMNGRZR1FFL2RPZGJhS1lPUXd6c2h5K0RMCnBRckptTGttbU80Q2YrT1ZscEdEbTgvTmIvUjRnKzhtWm0zWUZpT09BNVJpY0JqUEFhRXViMWdGcjh5V2N4dU4KY28xRDNYVG5UbnZIWW45ZG5HS0Q1MlJzQjJEcmhEZVF6d3lwUXJJTnZlb0R6TnJPTEE0QVA1WGp6S0YybnUrYgp6ekdzQVpJNXhldVNyNmR3MFl3T0txaWNmU1A5T0Z2aG1Kem11Zmdsc3FUMFNZRGJoZkdyZlgxSWp0akNCV3VnCmh0NFlFdEp6bGRBODVWeVBUNzkyZTRtWlNFZzFaVGZwTk5UbDROblFyYjk2bDhKSzVsenI2anhLWVlGVkhXNXMKTTkxQUwzeWNPLzdoTUdDK21JM3dwSzFFNjZEVDhLS0xzRkM3RVFJREFRQUJBb0lCQUFDaTU4bzJCUStqQ2VaQQpVTXdWeW5icjJ0RUxIUm1oYUxrYk12Vk1TOVVHL29WSi9JajNwbUNleDIxaXFMSnErendsQzN1NUVCb2FKa0hPCnBPc01ZeDFhS29NcDU3YlRBc2dTRm5PejBUUzhQMG90MjNkaEMvTE9rVnF0UHpqWXp5ejNYcUZudlJva3VWQmoKRldldUhUSkxjRVdoRXV5SklqaTRwVVdydEZGdmllSE1UTFV2YTlXRmszRmhONENVZnJtTWpIeWh2UWdEU3hEdwpWek9DUHZ1Q1R5UDE1bkMyTmM2RDdyeVJhMStGOXhwY0ZFb1h4UmpyQk9Edkd5ZHJWN3R2TGZSYlErR0RHSVNCCmNRclFSMmdXeENWZG9YdDlRVys3M0NhZ3FJclptN1pQVUFJenBhUEpQb0x4UThRa2hpWGluNFk0Nk11R1p3WkgKeWVTcDBMRUNnWUVBOTkyN283MlBxMXQ3T2J2aEYwTk1vdW9ENVFPbGNNZS9CcVo0MURVNjA3WEVRd2hZRXhoTQpUR0FMQ2VqTisxemx4N1hNTjJuYjFSU0RGME1pL2dMc1Z6U2lTaWE3cjdpSUJTcEx0T3BlaHhub3VVbHNHTU1mClNvMytMZnJZb213UUo5eXBkWlRhVEhMS0VlWkg4QkVrWitEbm92Y09GNlViYzZ4N3JoZ1RWamtDZ1lFQXdUSVkKazZ3MXdqLy85Z3NCZlRUbDJiejJhSEZVOTcvUXNTM3N4REJLSk1BOGppSFh3aDVmcDRUUXNwTDBtNkRUTXVQKwo0MEszd0pFMVcxZnJOUHNEdFhaeGFNMVl6bTZKTmdiMFRVSlh6Y3FYQjRKdnNEdDVzVTBRWkdNQm9ZVGJRcGJxCm1yN2VZL0NBWFhRNlNtT2Y1dDVEZklnWDFrSENPN2JuT2RwN3k1a0NnWUI5bmpTQWtZdUVic0tPeWZjVDBSbS8KM3hYR05RamRsWDNzb3VYTnRvUnYwMGo2Si9wckF2OTRIWnk2a3ZBQm1sMGh6N01GeW5LTkZPNHpGZVZnRXA4dwp1dzRjd25DUVo5Y1h0dzNUVEl4SHNBdlFDN1ByQU1pVkFrMTFoQkhTankzSTVmNUVCd0p6aUNGODNOWk1ob09LCjJlTExKcVpQdVcrbU9oNGNaenMxa1FLQmdRQ3BmcUx6US8yeUcyWWIzWklENEJuYnlwM0FDZFlLdWQyRTVJNjUKbk1nZnNBd3dnbDZCYnhacXFPcFVFUHZMWWZDSTBHQjV6N25ZbDB6aGc2UFdTbmI1aUIxOVhkTVE0UWMrUUNHcgplbzMvL1VJOWsyRWJrMEp6bS9IS3NOWG9kOS9KYnhBS2pYUWM2QTRtYjJjSWhwanQrb2ZveEpsdEh5b2lLNWgzCk9FaXQyUUtCZ1FESzBnaTR1UkZldWdXN1ZtYnNqVU5taGxXdi92T0dQK1hxUXFkVzJLYXU2VFY4bXZMTzdoN3oKUEd3MTNxV1JwdnV6MFJJekVUQU9ya0EvOFRodmlRRytFMmF3ZVFvdGtOYWtLK3RhMWpHdDUvb1dTTThsbTc1UQpONGJPWGNmL2QvM2E5dU5NMGx0QmJ1WHJnUlVsVUdHRmV2MnRkajJTZndTMTFnWi9RQ21XVGc9PQotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo=";
        String csr = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURSBSRVFVRVNULS0tLS0KTUlJQ21qQ0NBWUlDQVFBd0l6RU9NQXdHQTFVRUF3d0ZTbFZ1YVhReEVUQVBCZ05WQkFzTUNFeHZkbVZpYVhKawpNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXV3NnJrTnlMZGhpZExyRjFhSWlWCks1a0w0ZFlHUUUvZE9kYmFLWU9Rd3pzaHkrRExwUXJKbUxrbW1PNENmK09WbHBHRG04L05iL1I0Zys4bVptM1kKRmlPT0E1UmljQmpQQWFFdWIxZ0ZyOHlXY3h1TmNvMUQzWFRuVG52SFluOWRuR0tENTJSc0IyRHJoRGVRend5cApRcklOdmVvRHpOck9MQTRBUDVYanpLRjJudStienpHc0FaSTV4ZXVTcjZkdzBZd09LcWljZlNQOU9GdmhtSnptCnVmZ2xzcVQwU1lEYmhmR3JmWDFJanRqQ0JXdWdodDRZRXRKemxkQTg1VnlQVDc5MmU0bVpTRWcxWlRmcE5OVGwKNE5uUXJiOTZsOEpLNWx6cjZqeEtZWUZWSFc1c005MUFMM3ljTy83aE1HQyttSTN3cEsxRTY2RFQ4S0tMc0ZDNwpFUUlEQVFBQm9ESXdNQVlKS29aSWh2Y05BUWtPTVNNd0lUQVBCZ05WSFJNQkFmOEVCVEFEQVFIL01BNEdBMVVkCkR3RUIvd1FFQXdJQ0JEQU5CZ2txaGtpRzl3MEJBUXNGQUFPQ0FRRUFiV1Z3VGNpcmFkeHIxUHRRbWRSRS9hd3kKMVJLSTQ1MC80ckhhakExL1ZGNEI2UHNuY2pVaXdmVE5paEFUcDRSK2JtY1pGMmcxWTdRWklWYmpIb3I3SU5YZgowTUNBbE9Vb001bkpxa3ZFTnd2amhHSnJhcWszblQ4R0RGMEJTOWR0Mjk1QU0rTytYRVpZMUxBNFJsRmxnYXUzCmg1aTNJSFRKT0ppT2daUXYvNHBmZHJIdVZFVE5pTHZLcDI4WW9INHRwaVhVUHZoOE50MEQrZFBpQzhXdkJtTXoKU2NKTEpTSm93VGlQZ1RCWU1UdWhUS2JBUHJJS0dhQ0d4empIL20yUDJPenVoQ2FxZHp0RUtNU2VCYzNOM1lGZgpVK05lbjl1dFNUU3J3cks3eGhkV3VnZTg0SDdmT1NLZ0pzV3k1QTJjU3A5cUNLOFU5aFA2cjN6OWhveVk3Zz09Ci0tLS0tRU5EIENFUlRJRklDQVRFIFJFUVVFU1QtLS0tLQo=";
        String keyPair = privateKey + "----------\n" + csr;
        Path createdFile = tempDir.resolve("stubs-example-key2");
        Files.write(createdFile, Arrays.asList(TYPE_INDICATOR + "CSR" + "\n"));
        Files.write(createdFile, keyPair.getBytes(UTF_8), StandardOpenOption.APPEND);

        new CSRKeyStoreReader().read(createdFile, keys);

        assertThat(keys.getPrivateKey("stubs-example-key2")).isEqualTo(PrivateKey.from(new String(Base64.decode(privateKey.getBytes(UTF_8)))));
    }

    private String toPEM(PKCS10CertificationRequest csr) throws Exception {
        StringWriter sw = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(sw)) {
            pemWriter.writeObject(csr);
        }
        return sw.toString();
    }


    private PKCS10CertificationRequest createCSR(KeyPair pair) throws Exception {
        X500NameBuilder x500NameBld = new X500NameBuilder(BCStyle.INSTANCE);
        x500NameBld.addRDN(BCStyle.CN, "JUnit");
        x500NameBld.addRDN(BCStyle.OU, "Lovebird");

        X500Name subject = x500NameBld.build();

        Extension[] extKeyAgreement = new Extension[]{
                new Extension(Extension.basicConstraints, true,
                        new DEROctetString(new BasicConstraints(true))),
                new Extension(Extension.keyUsage, true,
                        new DEROctetString(new KeyUsage(KeyUsage.keyCertSign))),
        };
        return new JcaPKCS10CertificationRequestBuilder(
                subject,
                pair.getPublicKey().getKey())
                .addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest,
                        new Extensions(extKeyAgreement))
                .build(new JcaContentSignerBuilder("SHA256withRSA")
                        .setProvider("BC")
                        .build(pair.getPrivateKey().getKey()));
    }
}
