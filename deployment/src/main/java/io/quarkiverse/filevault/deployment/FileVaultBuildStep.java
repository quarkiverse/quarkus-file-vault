package io.quarkiverse.filevault.deployment;

import java.util.function.BooleanSupplier;

import javax.inject.Singleton;

import io.quarkiverse.filevault.runtime.FileVaultConfig;
import io.quarkiverse.filevault.runtime.FileVaultCredentialsProvider;
import io.quarkiverse.filevault.runtime.FileVaultRecorder;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.credentials.CredentialsProvider;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class FileVaultBuildStep {

    @BuildStep(onlyIf = IsEnabled.class)
    public FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem("file-vault");
    }

    @Record(ExecutionTime.STATIC_INIT)
    @BuildStep(onlyIf = IsEnabled.class)
    public SyntheticBeanBuildItem setup(FileVaultConfig config, FileVaultRecorder recorder) {
        return SyntheticBeanBuildItem.configure(FileVaultCredentialsProvider.class).unremovable()
                .types(CredentialsProvider.class)
                .supplier(recorder.createFileVault(config))
                .scope(Singleton.class)
                .done();
    }

    public static class IsEnabled implements BooleanSupplier {
        FileVaultBuildTimeConfig config;

        public boolean getAsBoolean() {
            return config.enabled;
        }
    }
}
