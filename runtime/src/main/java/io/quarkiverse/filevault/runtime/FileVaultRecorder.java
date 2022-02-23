package io.quarkiverse.filevault.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.crypto.SecretKey;

import org.jboss.logging.Logger;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class FileVaultRecorder {
    private static final Logger LOGGER = Logger.getLogger(FileVaultRecorder.class.getName());

    private static String DEFAULT_KEY_STORE_FILE = "passwords.p12";
    private static String DEFAULT_KEY_STORE_SECRET = "secret";
    private static String DEFAULT_KEY_STORE_ALIAS = "user";

    public Supplier<FileVaultCredentialsProvider> createFileVault(FileVaultConfig config) {

        final Map<String, Map<String, String>> storeProperties = new HashMap<>();

        final Map<String, String> defaultAliases = new HashMap<>();

        for (Map.Entry<String, Map<String, String>> store : config.provider.entrySet()) {
            storeProperties.put(store.getKey(), readKeyStore(store.getValue()));
            defaultAliases.put(store.getKey(), store.getValue().getOrDefault("alias", DEFAULT_KEY_STORE_ALIAS));
        }

        return new Supplier<FileVaultCredentialsProvider>() {

            @Override
            public FileVaultCredentialsProvider get() {
                return new FileVaultCredentialsProvider(storeProperties, defaultAliases, config.setAliasAsUser);
            }

        };
    }

    private static Map<String, String> readKeyStore(Map<String, String> keyStoreProps) {

        String keyStoreFile = keyStoreProps.getOrDefault("path", DEFAULT_KEY_STORE_FILE);
        String keyStoreSecret = keyStoreProps.getOrDefault("secret", DEFAULT_KEY_STORE_SECRET);

        try (InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreFile)) {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(fis, keyStoreSecret.toCharArray());

            Map<String, String> properties = new HashMap<>();

            for (Enumeration<String> aliases = keyStore.aliases(); aliases.hasMoreElements();) {
                String alias = aliases.nextElement();
                properties.put(alias, loadSecret(keyStore, keyStoreSecret, alias));
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

    private static String loadSecret(KeyStore keyStore, String keyStoreSecret, String keyAlias) throws Exception {
        SecretKey secretKey = (SecretKey) keyStore.getKey(keyAlias, keyStoreSecret.toCharArray());
        return new String(secretKey.getEncoded(), "UTF-8");
    }
}
