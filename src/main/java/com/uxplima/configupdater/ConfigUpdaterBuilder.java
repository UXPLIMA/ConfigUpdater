package com.uxplima.configupdater;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ConfigUpdaterBuilder {

    private JavaPlugin plugin;
    private Collection<String> files = new ArrayList<>();
    private Collection<UpdateProdiver> updateProdivers = new ArrayList<>();
    private String configVersion;
    private String jarVersion;
    private boolean mergeMissingNodes, updateConfigVersion;

    public ConfigUpdaterBuilder(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public ConfigUpdaterBuilder setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public ConfigUpdaterBuilder setFiles(Collection<String> files) {
        this.files = files;
        return this;
    }

    public ConfigUpdaterBuilder setFiles(String... files) {
        this.files.addAll(List.of(files));
        return this;
    }

    public ConfigUpdaterBuilder setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
        return this;
    }

    public ConfigUpdaterBuilder setJarVersion(String jarVersion) {
        this.jarVersion = jarVersion;
        return this;
    }

    public ConfigUpdaterBuilder setUpdateProviders(Collection<UpdateProdiver> updateProviders) {
        this.updateProdivers = updateProviders;
        return this;
    }

    public ConfigUpdaterBuilder setUpdateProviders(UpdateProdiver... updateProviders) {
        this.updateProdivers.addAll(List.of(updateProviders));
        return this;
    }

    public ConfigUpdaterBuilder setMergeMissingNodes(boolean mergeMissingNodes) {
        this.mergeMissingNodes = mergeMissingNodes;
        return this;
    }

    public ConfigUpdaterBuilder setUpdateConfigVersion(boolean updateConfigVersion) {
        this.updateConfigVersion = updateConfigVersion;
        return this;
    }

    public ConfigUpdater build() {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(configVersion, "configVersion cannot be null");
        Objects.requireNonNull(jarVersion, "jarVersion cannot be null");

        if (files.isEmpty()) {
            throw new IllegalArgumentException("Files cannot be null");
        }
        if (updateProdivers.isEmpty()) {
            throw new IllegalArgumentException("Files cannot be null");
        }

        return new ConfigUpdater(plugin, files, configVersion, jarVersion, updateProdivers, mergeMissingNodes, updateConfigVersion);
    }

}