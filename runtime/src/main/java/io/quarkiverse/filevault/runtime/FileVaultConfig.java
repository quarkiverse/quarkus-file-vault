package io.quarkiverse.filevault.runtime;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "file.vault", phase = ConfigPhase.RUN_TIME)
public class FileVaultConfig {

    /**
     * Java KeyStore configuration for a specific provider such as a database.
     */
    @ConfigItem
    public Map<String, Map<String, String>> provider;

    /**
     * Set the alias which is used to extract a secret from the key store as a 'user' property.
     *
     * If this property is enabled then {@linkplain FileVaultCredentialsProvider} will set the alias as a 'user' property
     * which can be recognized by 'quarkus-agroal' and other extensions which use {@linkplain CredentialsProvider}
     * to prepare the authentication properties.
     * 
     * Disable this property if you'd like to use a property such as 'quarkus.datasource.username' to configure a username,
     * when the username and keystore alias values are different.
     */
    @ConfigItem(defaultValue = "true")
    public boolean setAliasAsUser;
}
