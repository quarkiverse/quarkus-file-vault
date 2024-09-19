package io.quarkiverse.filevault.runtime;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

import org.jboss.logging.Logger;

import io.quarkiverse.filevault.util.KeyStoreUtil;
import io.quarkiverse.filevault.util.KeyStoreUtil.KeyStoreEntry;
import io.quarkus.credentials.CredentialsProvider;

@Named("quarkus.file.vault")
@ApplicationScoped
public class FileVaultCredentialsProvider implements CredentialsProvider {
    static String BASE_PROVIDER_NAME = "quarkus.file.vault.provider";
    static String CERTIFICATE_PROPERTY = "certificate";

    private static String DEFAULT_KEY_STORE_FILE = "passwords.p12";

    private static final Logger LOGGER = Logger.getLogger(FileVaultCredentialsProvider.class.getName());

    private Map<String, Map<String, KeyStoreEntry>> storeProperties = new HashMap<>();
    private Map<String, String> defaultAliases = new HashMap<>();
    private boolean setAliasAsUser;

    public FileVaultCredentialsProvider(FileVaultConfig config) {
        for (Map.Entry<String, Map<String, String>> store : config.provider().entrySet()) {
            Map<String, String> keyStoreProps = store.getValue();
            String keyStoreFile = keyStoreProps.getOrDefault("path", DEFAULT_KEY_STORE_FILE);
            String keyStoreSecret = keyStoreProps.get("secret");
            String encryptionKey = keyStoreProps.get("encryption-key");

            storeProperties.put(store.getKey(), KeyStoreUtil.readKeyStore(keyStoreFile, keyStoreSecret, encryptionKey));
            if (store.getValue().containsKey("alias")) {
                defaultAliases.put(store.getKey(), store.getValue().get("alias"));
            }
        }
        this.setAliasAsUser = config.setAliasAsUser();
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

        if (keyStoreAlias == null) {
            for (Map.Entry<String, KeyStoreEntry> storeEntry : keyStoreProperties.entrySet()) {
                credProviderProperties.put(storeEntry.getKey(), storeEntry.getValue().getValue());
            }
        } else if (keyStoreProperties.containsKey(keyStoreAlias)) {
            KeyStoreEntry entry = keyStoreProperties.get(keyStoreAlias);
            String property = entry.isCertificate() ? CERTIFICATE_PROPERTY : PASSWORD_PROPERTY_NAME;
            credProviderProperties.put(property, entry.getValue());
            if (setAliasAsUser) {
                credProviderProperties.put(USER_PROPERTY_NAME, keyStoreAlias);
            }
        } else {
            LOGGER.warnf("Key store %s values do not have a %s value", keyStoreName, keyStoreAlias);
        }

        return credProviderProperties;

    }
}