package io.quarkiverse.filevault.configsource.runtime;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "quarkus.file.vault-config-source")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface FileVaultBootstrapConfig {

    /**
     * Microprofile Config ordinal.
     * <p>
     * This is provided as an alternative to the `config_ordinal` property defined by the specification, to
     * make it easier and more natural for applications to override the default ordinal.
     * <p>
     * The default value is higher than the file system or jar ordinals, but lower than env vars.
     */
    @WithDefault("270")
    int configOrdinal();

    /**
     * KeyStore Path.
     */
    Optional<String> keystorePath();

    /**
     * KeyStore Secret.
     */
    Optional<String> keystoreSecret();

    /**
     * KeyStore Secret Encryption Key.
     */
    Optional<String> encryptionKey();
}
