package io.quarkiverse.filevault.configsource.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "file.vault-config-source", phase = ConfigPhase.BOOTSTRAP)
public class FileVaultBootstrapConfig {

    /**
     * Microprofile Config ordinal.
     * <p>
     * This is provided as an alternative to the `config_ordinal` property defined by the specification, to
     * make it easier and more natural for applications to override the default ordinal.
     * <p>
     * The default value is higher than the file system or jar ordinals, but lower than env vars.
     */
    @ConfigItem(defaultValue = "270")
    public int configOrdinal;

    /**
     * KeyStore Path.
     */
    @ConfigItem
    public Optional<String> keystorePath;

    /**
     * KeyStore Secret.
     */
    @ConfigItem
    public Optional<String> keystoreSecret;

    /**
     * KeyStore Secret Encryption Key.
     */
    @ConfigItem
    public Optional<String> encryptionKey;
}
