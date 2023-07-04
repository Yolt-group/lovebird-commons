package nl.ing.lovebird.secretspipeline;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.util.encoders.Base64;
import org.c02e.jpgpj.Key;
import org.c02e.jpgpj.Ring;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static nl.ing.lovebird.secretspipeline.KeyUtils.readFile;
import static nl.ing.lovebird.secretspipeline.KeyUtils.splitAsymmetric;

@Slf4j
public class KeyRingCreator {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static final String GPG = "gpg";
    public static final String GPG_PAIR = "gpg_pair";

    private final URI secretsLocation;

    public KeyRingCreator(final URI secretsLocation) {
        this.secretsLocation = secretsLocation;
    }

    public PGPKeyRing createKeyRing() {
        Ring keyRing = new Ring();
        try (Stream<Path> paths = Files.walk(Paths.get(secretsLocation))) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(this::isGPG)
                    .forEach(path -> {
                        try {
                            for (Key key : processPGPKey(path)) {
                                keyRing.getKeys().add(key);
                            }
                        } catch (IOException | PGPException e) {
                            log.error("Something went wrong with loading the keys into the pgp keyring, aborting", e);
                            throw new IllegalStateException(e);
                        }
                    });
        } catch (IOException e) {
            throw new SecurityException("The path defined for the location of the secrets file seems not accessible or is not existing, crashing.", e);
        }

        return new PGPKeyRing(keyRing);
    }

    private List<Key> processPGPKey(Path file) throws IOException, PGPException {
        if (!(file != null && file.toFile().exists() && !file.toFile().isDirectory())) {
            throw new IllegalArgumentException();
        }
        List<org.c02e.jpgpj.Key> cache = new ArrayList<>();
        String unlockMechanism = "yolt";
        byte[] fileContents = readFile(file);
        if (isGPGPair(file)) {
            byte[][] splittedContents = splitAsymmetric(fileContents);
            cache.add(new org.c02e.jpgpj.Key(new String(Base64.decode(splittedContents[0])), unlockMechanism));
            cache.add(new org.c02e.jpgpj.Key(new String(Base64.decode(splittedContents[1])), unlockMechanism));
        } else {
            cache.add(new org.c02e.jpgpj.Key(new String(Base64.decode(fileContents)), unlockMechanism));
        }

        return cache;
    }

    private boolean isGPG(Path fileName) {
        return KeyUtils.getKeyType(fileName).map(e -> e.toLowerCase().contains(GPG)).orElse(false);
    }

    private boolean isGPGPair(Path fileName) {
        return KeyUtils.getKeyType(fileName).map(e -> e.equalsIgnoreCase(GPG_PAIR)).orElse(false);
    }
}
