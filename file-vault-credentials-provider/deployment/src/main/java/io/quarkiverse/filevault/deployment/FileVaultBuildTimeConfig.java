package io.quarkiverse.filevault.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

/**
 * Build time configuration for File Vault.
 */
@ConfigRoot(name = "file.vault")
public class FileVaultBuildTimeConfig {
    /**
     * If the File Vault extension is enabled.
     */
    @ConfigItem(defaultValue = "true")
    public boolean enabled;
}
