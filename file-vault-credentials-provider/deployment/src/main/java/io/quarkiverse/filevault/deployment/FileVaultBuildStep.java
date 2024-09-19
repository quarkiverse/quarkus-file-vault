package io.quarkiverse.filevault.deployment;

import java.util.function.BooleanSupplier;

import io.quarkiverse.filevault.runtime.FileVaultCredentialsProvider;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

public class FileVaultBuildStep {

    @BuildStep(onlyIf = IsEnabled.class)
    public FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem("file-vault");
    }

    @BuildStep(onlyIf = IsEnabled.class)
    public AdditionalBeanBuildItem additionalBeans() {
        AdditionalBeanBuildItem.Builder builder = AdditionalBeanBuildItem.builder().setUnremovable()
                .addBeanClass(FileVaultCredentialsProvider.class);
        return builder.build();
    }

    public static class IsEnabled implements BooleanSupplier {
        FileVaultBuildTimeConfig config;

        public boolean getAsBoolean() {
            return config.enabled();
        }
    }
}
