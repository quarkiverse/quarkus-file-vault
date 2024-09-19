package io.quarkiverse.filevault.deployment;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Build time configuration for File Vault.
 */
@ConfigMapping(prefix = "quarkus.file.vault")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface FileVaultBuildTimeConfig {
    /**
     * If the File Vault extension is enabled.
     */
    @WithDefault("true")
    boolean enabled();
}
