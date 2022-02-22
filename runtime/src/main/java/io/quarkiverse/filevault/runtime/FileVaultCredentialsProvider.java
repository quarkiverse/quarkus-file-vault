package io.quarkiverse.filevault.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.logging.Logger;

import io.quarkus.arc.Unremovable;
import io.quarkus.credentials.CredentialsProvider;

@ApplicationScoped
@Unremovable
@Named("quarkus.file.vault")
public class FileVaultCredentialsProvider implements CredentialsProvider {
    private static String providerName = "quarkus.file.vault.provider";
    private static String defaultKeyStoreFile = "passwords.p12";
    private static String defaultKeyStoreSecret = "secret";
    private static String defaultKeyStoreAlias = "user";

    private static final Logger LOGGER = Logger.getLogger(FileVaultCredentialsProvider.class.getName());

    @Inject
    FileVaultConfig config;

    public FileVaultCredentialsProvider(FileVaultConfig config) {
        this.config = config;
    }

    @Override
    public Map<String, String> getCredentials(String credentialsProviderName) {
        if (!credentialsProviderName.startsWith(providerName)) {
            LOGGER.tracef("Unrecognized provider name: %s", credentialsProviderName);
            return Map.of();
        }
        String[] parts = credentialsProviderName.split("\\.");
        if (parts.length != 5) {
            LOGGER.warnf("Incorrect provider name format: %s, use 'quarkus.file.vault.provider.<name>'",
                    credentialsProviderName);
            return Map.of();
        }

        Map<String, String> providerProps = config.provider.getOrDefault(parts[4], Map.of());

        String keyStoreFile = providerProps.getOrDefault("path", defaultKeyStoreFile);
        String keyStoreSecret = providerProps.getOrDefault("secret", defaultKeyStoreSecret);
        String keyStoreAlias = providerProps.getOrDefault("alias", defaultKeyStoreAlias);

        try (InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreFile)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, keyStoreSecret.toCharArray());

            Map<String, String> properties = new HashMap<>();

            properties.put(USER_PROPERTY_NAME, keyStoreAlias);
            properties.put(PASSWORD_PROPERTY_NAME, loadSecret(keyStore, keyStoreSecret, keyStoreAlias));
            return properties;
        } catch (IOException e) {
            LOGGER.errorf("Keystore %s can not be loaded", keyStoreFile);
            throw new RuntimeException(e);
        } catch (Exception e) {
            LOGGER.errorf("Keystore %s entries can not be loaded", keyStoreFile);
            throw new RuntimeException(e);
        }
    }

    private String loadSecret(KeyStore keyStore, String keyStoreSecret, String keyAlias) throws Exception {
        SecretKey secretKey = (SecretKey) keyStore.getKey(keyAlias, keyStoreSecret.toCharArray());
        return new String(secretKey.getEncoded(), "UTF-8");
    }
}
