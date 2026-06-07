package com.uxplima.configupdater;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.function.Predicate;

public class ConfigUpdaterBuilder {

    private JavaPlugin plugin;
    private Collection<String> files = new ArrayList<>();
    private Collection<UpdateProvider> updateProviders = new ArrayList<>();
    private String configVersion, jarVersion;
    private String[] supportedLangs;
    private String currentLang, configVersionPath;
    private boolean mergeMissingNodes, deleteUnknownNodes, updateConfigVersion;
    private long backupStart;
    private List<Predicate<String>> deleteConfigNodeConditions, mergeConfigNodeConditions;

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

    public ConfigUpdaterBuilder setUpdateProviders(Collection<UpdateProvider> updateProviders) {
        this.updateProviders = updateProviders;
        return this;
    }

    public ConfigUpdaterBuilder setUpdateProviders(UpdateProvider... updateProviders) {
        this.updateProviders.addAll(List.of(updateProviders));
        return this;
    }

    public ConfigUpdaterBuilder setMergeMissingNodes(boolean mergeMissingNodes) {
        this.mergeMissingNodes = mergeMissingNodes;
        return this;
    }

    public ConfigUpdaterBuilder setDeleteUnknownNodes(boolean deleteUnknownNodes) {
        this.deleteUnknownNodes = deleteUnknownNodes;
        return this;
    }

    public ConfigUpdaterBuilder setUpdateConfigVersion(boolean updateConfigVersion) {
        this.updateConfigVersion = updateConfigVersion;
        return this;
    }

    public ConfigUpdaterBuilder setBackupStart(long backupStart) {
        this.backupStart = backupStart;
        return this;
    }

    public ConfigUpdaterBuilder setDeleteConfigNodeConditions(List<Predicate<String>> deleteConfigNodeConditions) {
        this.deleteConfigNodeConditions = deleteConfigNodeConditions;
        return this;
    }

    public ConfigUpdaterBuilder setMergeConfigNodeConditions(List<Predicate<String>> mergeConfigNodeConditions) {
        this.mergeConfigNodeConditions = mergeConfigNodeConditions;
        return this;
    }

    public ConfigUpdaterBuilder setSupportedLangs(String... supportedLangs) {
        this.supportedLangs = supportedLangs;
        return this;
    }

    public ConfigUpdaterBuilder setCurrentLang(String currentLang) {
        this.currentLang = currentLang;
        return this;
    }

    public ConfigUpdaterBuilder setConfigVersionPath(String configVersionPath) {
        this.configVersionPath = configVersionPath;
        return this;
    }

    public ConfigUpdater build() {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(configVersion, "configVersion cannot be null");
        Objects.requireNonNull(jarVersion, "jarVersion cannot be null");

        if (files.isEmpty()) {
            throw new IllegalArgumentException("Files cannot be null");
        }

        return new ConfigUpdater(plugin, files, supportedLangs, currentLang, configVersionPath, configVersion, jarVersion, updateProviders, mergeMissingNodes, deleteUnknownNodes, updateConfigVersion, backupStart, deleteConfigNodeConditions, mergeConfigNodeConditions);
    }

}