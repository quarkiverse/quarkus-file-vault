package io.quarkiverse.filevault.configsource.runtime;

import static java.util.Collections.emptyList;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class FileVaultConfigSourceRecorder {

    private static final EmptyConfigSourceProvider EMPTY = new EmptyConfigSourceProvider();

    private static class EmptyConfigSourceProvider implements ConfigSourceProvider {
        @Override
        public Iterable<ConfigSource> getConfigSources(ClassLoader classLoader) {
            return emptyList();
        }
    }

    public RuntimeValue<ConfigSourceProvider> configure(FileVaultBootstrapConfig fileVaultBootstrapConfig) {
        ConfigSourceProvider configSourceProvider = EMPTY;
        if (fileVaultBootstrapConfig.keystorePath.isPresent()) {
            configSourceProvider = new FileVaultConfigSourceProvider(fileVaultBootstrapConfig);
        }
        return new RuntimeValue<>(configSourceProvider);
    }
}
