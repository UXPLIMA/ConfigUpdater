package com.uxplima.configupdater;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;

public class ConfigUpdaterBuilder {
    private JavaPlugin plugin;
    private Collection<String> files;
    private Collection<UpdateProdiver> updateProdivers;
    private String configVersion;
    private String jarVersion;

    public ConfigUpdaterBuilder setPlugin(JavaPlugin plugin) {
        this.plugin = plugin;
        return this;
    }

    public ConfigUpdaterBuilder setFiles(Collection<String> files) {
        this.files = files;
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

    public ConfigUpdater createConfigUpdater() {
        return new ConfigUpdater(plugin, files, configVersion, jarVersion, updateProdivers);
    }
}