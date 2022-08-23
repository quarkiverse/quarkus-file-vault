package io.quarkiverse.filevault.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.jboss.logging.Logger;

public class KeyStoreUtil {
    private static final Logger LOGGER = Logger.getLogger(KeyStoreUtil.class.getName());

    public static Map<String, KeyStoreEntry> readKeyStore(String keyStoreFile, String keyStoreSecret, String encryptionKey) {
        if (keyStoreFile == null) {
            return Map.of();
        }
        if (keyStoreSecret != null) {
            if (encryptionKey != null) {
                String decodedEncryptionKey = new String(Base64.getUrlDecoder().decode(encryptionKey), StandardCharsets.UTF_8);
                keyStoreSecret = EncryptionUtil.decrypt(keyStoreSecret, decodedEncryptionKey);
            }
        } else {
            LOGGER.errorf("Keystore %s secret is not configured", keyStoreFile);
            throw new RuntimeException();
        }

        URL keyStoreFileUrl = null;
        if ((keyStoreFileUrl = Thread.currentThread().getContextClassLoader().getResource(keyStoreFile)) != null) {
            return readKeyStore(keyStoreFileUrl, keyStoreSecret);
        } else {
            Path filePath = Paths.get(keyStoreFile);
            if (Files.exists(filePath)) {
                try {
                    return readKeyStore(filePath.toUri().toURL(), keyStoreSecret);
                } catch (MalformedURLException e) {
                    LOGGER.errorf("Keystore %s location is not a valid URL", keyStoreFile);
                    throw new RuntimeException(e);
                }
            } else {
                LOGGER.errorf("Keystore %s can not be found on the classpath and the file system", keyStoreFile);
                throw new RuntimeException();
            }
        }
    }

    private static Map<String, KeyStoreEntry> readKeyStore(URL keyStoreFileUrl, String keyStoreSecret) {

        try (InputStream fis = keyStoreFileUrl.openStream()) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, keyStoreSecret.toCharArray());

            Map<String, KeyStoreEntry> properties = new HashMap<>();

            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements();) {
                String alias = aliases.nextElement();
                KeyStoreEntry storeEntry = loadStoreEntry(keyStore, keyStoreSecret, alias);
                if (storeEntry != null) {
                    properties.put(alias, storeEntry);
                }
            }

            return properties;
        } catch (IOException e) {
            LOGGER.errorf("Keystore %s can not be loaded", keyStoreFileUrl.toString());
            throw new RuntimeException(e);
        } catch (Exception e) {
            LOGGER.errorf("Keystore %s entries can not be loaded", keyStoreFileUrl.toString());
            throw new RuntimeException(e);
        }
    }

    private static KeyStoreEntry loadStoreEntry(KeyStore keyStore, String keyStoreSecret, String keyAlias) throws Exception {
        Entry entry = keyStore.getEntry(keyAlias, new PasswordProtection(keyStoreSecret.toCharArray()));
        if (entry instanceof SecretKeyEntry) {
            SecretKey secretKey = ((SecretKeyEntry) entry).getSecretKey();
            return new KeyStoreEntry(new String(secretKey.getEncoded(), "UTF-8"));
        } else if (entry instanceof PrivateKeyEntry) {
            Certificate[] certChain = keyStore.getCertificateChain(keyAlias);
            if (certChain != null && certChain.length > 0) {
                return new KeyStoreEntry(new String(certChain[0].getEncoded(), StandardCharsets.ISO_8859_1), true);
            }
        } else if (entry instanceof TrustedCertificateEntry) {
            return new KeyStoreEntry(new String(((TrustedCertificateEntry) entry).getTrustedCertificate().getEncoded(),
                    StandardCharsets.ISO_8859_1), true);
        }
        LOGGER.tracef("%s entry type %s is not supported", keyAlias, entry.getClass().getName());
        return null;
    }

    public static class KeyStoreEntry {
        private final String value;
        private final boolean certificate;

        KeyStoreEntry(String value) {
            this(value, false);

        }

        KeyStoreEntry(String value, boolean cert) {
            this.value = value;
            this.certificate = cert;
        }

        public boolean isCertificate() {
            return certificate;
        }

        public String getValue() {
            return value;
        }

    }
}