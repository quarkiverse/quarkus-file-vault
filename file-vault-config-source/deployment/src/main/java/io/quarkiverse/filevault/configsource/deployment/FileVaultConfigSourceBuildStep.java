package io.quarkiverse.filevault.configsource.deployment;

import io.quarkiverse.filevault.configsource.runtime.FileVaultConfigBuilder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.RunTimeConfigBuilderBuildItem;

public class FileVaultConfigSourceBuildStep {

    public FeatureBuildItem featureBuildItem() {
        return new FeatureBuildItem("file-vault-config-source");
    }

    @BuildStep
    void config(BuildProducer<RunTimeConfigBuilderBuildItem> runTimeConfigBuilder) {
        runTimeConfigBuilder.produce(new RunTimeConfigBuilderBuildItem(FileVaultConfigBuilder.class));
    }
}
