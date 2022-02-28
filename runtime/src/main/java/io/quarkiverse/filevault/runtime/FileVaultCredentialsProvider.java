package io.quarkiverse.filevault.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.KeyStore.Entry;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStore.TrustedCertificateEntry;
import java.security.cert.Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.jboss.logging.Logger;

import io.quarkus.credentials.CredentialsProvider;

@Named("quarkus.file.vault")
@ApplicationScoped
public class FileVaultCredentialsProvider implements CredentialsProvider {
    static String BASE_PROVIDER_NAME = "quarkus.file.vault.provider";
    static String CERTIFICATE_PROPERTY = "certificate";

    private static String DEFAULT_KEY_STORE_FILE = "passwords.p12";
    private static String DEFAULT_KEY_STORE_SECRET = "secret";
    private static String DEFAULT_KEY_STORE_ALIAS = "user";

    private static final Logger LOGGER = Logger.getLogger(FileVaultCredentialsProvider.class.getName());

    private Map<String, Map<String, KeyStoreEntry>> storeProperties = new HashMap<>();
    private Map<String, String> defaultAliases = new HashMap<>();
    private boolean setAliasAsUser;

    public FileVaultCredentialsProvider(FileVaultConfig config) {
        for (Map.Entry<String, Map<String, String>> store : config.provider.entrySet()) {
            storeProperties.put(store.getKey(), readKeyStore(store.getValue()));
            defaultAliases.put(store.getKey(), store.getValue().getOrDefault("alias", DEFAULT_KEY_STORE_ALIAS));
        }
        this.setAliasAsUser = config.setAliasAsUser;
    }

    @Override
    public Map<String, String> getCredentials(String credentialsProviderName) {
        if (!credentialsProviderName.startsWith(BASE_PROVIDER_NAME)) {
            LOGGER.tracef("Unrecognized provider name: %s", credentialsProviderName);
            return Map.of();
        }

        final String[] nameParts = credentialsProviderName.split("\\.");
        if (nameParts.length != 5 && nameParts.length != 6) {
            LOGGER.warnf("Incorrect provider name format: %s,"
                    + "use 'quarkus.file.vault.provider.<storename>' or 'quarkus.file.vault.provider.<storename>.<alias>' format",
                    credentialsProviderName);
            return Map.of();
        }

        final String keyStoreName = nameParts[4];
        Map<String, KeyStoreEntry> keyStoreProperties = storeProperties.get(keyStoreName);
        if (keyStoreProperties == null) {
            LOGGER.warnf("Key store %s properties have not been found", keyStoreName);
            return Map.of();
        }

        Map<String, String> credProviderProperties = new HashMap<>();

        String keyStoreAlias = nameParts.length == 6 ? nameParts[5] : defaultAliases.get(keyStoreName);

        if (keyStoreProperties.containsKey(keyStoreAlias)) {
            KeyStoreEntry entry = keyStoreProperties.get(keyStoreAlias);
            String property = entry.cert ? CERTIFICATE_PROPERTY : PASSWORD_PROPERTY_NAME;
            credProviderProperties.put(property, entry.value);
            if (setAliasAsUser) {
                credProviderProperties.put(USER_PROPERTY_NAME, keyStoreAlias);
            }
        } else {
            LOGGER.warnf("Key store %s values do not have a %s value", keyStoreName, keyStoreAlias);
        }

        return credProviderProperties;

    }

    private static Map<String, KeyStoreEntry> readKeyStore(Map<String, String> keyStoreProps) {

        String keyStoreFile = keyStoreProps.getOrDefault("path", DEFAULT_KEY_STORE_FILE);
        String keyStoreSecret = keyStoreProps.getOrDefault("secret", DEFAULT_KEY_STORE_SECRET);

        try (InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreFile)) {
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
            LOGGER.errorf("Keystore %s can not be loaded", keyStoreFile);
            throw new RuntimeException(e);
        } catch (Exception e) {
            LOGGER.errorf("Keystore %s entries can not be loaded", keyStoreFile);
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

    private static class KeyStoreEntry {
        String value;
        boolean cert;

        KeyStoreEntry(String value) {
            this.value = value;
        }

        KeyStoreEntry(String value, boolean cert) {
            this.value = value;
            this.cert = cert;
        }

    }
}