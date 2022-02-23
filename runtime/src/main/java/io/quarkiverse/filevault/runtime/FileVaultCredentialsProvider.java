package io.quarkiverse.filevault.runtime;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import org.jboss.logging.Logger;

import io.quarkus.credentials.CredentialsProvider;

@Named("quarkus.file.vault")
public class FileVaultCredentialsProvider implements CredentialsProvider {
    private static String BASE_PROVIDER_NAME = "quarkus.file.vault.provider";

    private static final Logger LOGGER = Logger.getLogger(FileVaultCredentialsProvider.class.getName());

    private Map<String, Map<String, String>> storeProperties;
    private Map<String, String> defaultAliases;
    private boolean setAliasAsUser;

    public FileVaultCredentialsProvider(Map<String, Map<String, String>> storeProperties,
            Map<String, String> defaultAliases,
            boolean setAliasAsUser) {
        this.storeProperties = storeProperties;
        this.defaultAliases = defaultAliases;
        this.setAliasAsUser = setAliasAsUser;
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
        Map<String, String> keyStoreProperties = storeProperties.get(keyStoreName);
        if (keyStoreProperties == null) {
            LOGGER.warnf("Key store %s properties have not been found", keyStoreName);
            return Map.of();
        }

        Map<String, String> credProviderProperties = new HashMap<>();

        String keyStoreAlias = nameParts.length == 6 ? nameParts[5] : defaultAliases.get(keyStoreName);

        if (keyStoreProperties.containsKey(keyStoreAlias)) {
            credProviderProperties.put(PASSWORD_PROPERTY_NAME, keyStoreProperties.get(keyStoreAlias));
            if (setAliasAsUser) {
                credProviderProperties.put(USER_PROPERTY_NAME, keyStoreAlias);
            }
        } else {
            LOGGER.warnf("Key store %s values do not have a %s value", keyStoreName, keyStoreAlias);
        }

        return credProviderProperties;

    }
}
