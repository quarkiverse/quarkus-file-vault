package io.quarkiverse.filevault.runtime;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "file.vault", phase = ConfigPhase.RUN_TIME)
public class FileVaultConfig {
    /**
     * Java KeyStore configuration for a specific provider such as database.
     */
    @ConfigItem
    public Map<String, Map<String, String>> provider;
}
