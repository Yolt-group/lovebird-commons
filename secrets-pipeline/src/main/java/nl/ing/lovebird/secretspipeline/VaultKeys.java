package nl.ing.lovebird.secretspipeline;

import com.yolt.securityutils.crypto.PasswordKey;
import com.yolt.securityutils.crypto.PrivateKey;
import com.yolt.securityutils.crypto.PublicKey;
import com.yolt.securityutils.crypto.SecretKey;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;

import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class VaultKeys {

    @NoArgsConstructor
    @Getter
    private class Entry {
        Object privateFragment;
        Object publicFragment;
    }

    private final Map<String, Entry> keys = new HashMap<>();

    public void addPrivate(String name, Object key) {
        getOrCreate(name).privateFragment = key;
    }

    public void addPublic(String name, Object key) {
        getOrCreate(name).publicFragment = key;
    }

    public SecretKey getSymmetricKey(String name) {
        return getEntry(name, Entry::getPrivateFragment, SecretKey.class);
    }

    public PublicKey getPublicKey(String name) {
        return getEntry(name, Entry::getPublicFragment, PublicKey.class);
    }

    public PrivateKey getPrivateKey(String name) {
        return getEntry(name, Entry::getPrivateFragment, PrivateKey.class);
    }

    public Certificate getCertificate(String name) {
        return getEntry(name, Entry::getPublicFragment, Certificate.class);
    }

    public PasswordKey getPassword(String name) {
        return getEntry(name, Entry::getPrivateFragment, PasswordKey.class);
    }

    public JsonWebKey getJsonWebKey(String name) {
        return getEntry(name, Entry::getPrivateFragment, JsonWebKey.class);
    }

    public RsaJsonWebKey getRsaJsonWebKey(String name) {
        return getEntry(name, Entry::getPrivateFragment, RsaJsonWebKey.class);
    }

    public PublicJsonWebKey getPublicJsonWebKey(String name) {
        return getEntry(name, Entry::getPublicFragment, PublicJsonWebKey.class);
    }

    private Entry getOrCreate(String name) {
        return keys.computeIfAbsent(name, key -> new Entry());
    }

    private <T> T getEntry(String name, Function<Entry, Object> entryProjector, Class<T> clazz) {
        Entry entry = keys.get(name);
        if (entry == null) {
            throw new IllegalStateException("Key " + name + " not found!");
        }
        Object key = entryProjector.apply(entry);
        if (key == null) {
            throw new IllegalStateException("Entry " + name + " does not contain public of private fragment!");
        }
        if (clazz.isAssignableFrom(key.getClass())) {
            return (T) key;
        }
        throw new IllegalStateException("Key " + name + " has type '" + entry.getClass().getSimpleName() + "' expected: '" + clazz.getSimpleName() + "'");
    }
}
